package ls

import (
	"context"
	"fmt"
	"strings"
	"unicode/utf8"

	"github.com/inspirer/textmapper/compiler"
	"github.com/inspirer/textmapper/parsers/tm"
	"github.com/inspirer/textmapper/parsers/tm/ast"
	"github.com/inspirer/textmapper/parsers/tm/selector"
	"github.com/inspirer/textmapper/status"
	lsp "go.lsp.dev/protocol"
	"go.lsp.dev/uri"
	"go.uber.org/zap"
)

// Server is a language server for Textmapper (see langserver.org).
type Server struct {
	root   string
	client lsp.Client
	logger *zap.Logger
	docs   map[string]*document

	unimpl
}

type document struct {
	content string
	version uint32
}

func NewServer(logger *zap.Logger) *Server {
	return &Server{logger: logger, docs: make(map[string]*document)}
}

func (s *Server) SetClient(client lsp.Client) {
	s.client = client
}

func (s *Server) Initialize(ctx context.Context, params *lsp.InitializeParams) (*lsp.InitializeResult, error) {
	if len := len(params.WorkspaceFolders); len != 1 {
		return nil, fmt.Errorf("we support only 1 workspace folder, got %v", len)
	}
	uri, err := uri.Parse(params.WorkspaceFolders[0].URI)
	if err != nil {
		return nil, fmt.Errorf("failed to parse uri %q: %v", params.WorkspaceFolders[0].URI, err)
	}
	s.root = uri.Filename()

	ret := &lsp.InitializeResult{
		ServerInfo: &lsp.ServerInfo{
			Name:    "tm-lsp",
			Version: "0.1",
		},
		Capabilities: lsp.ServerCapabilities{
			TextDocumentSync: &lsp.TextDocumentSyncOptions{
				OpenClose: true,
				Change:    lsp.TextDocumentSyncKindFull,
			},
			DefinitionProvider: true,
		},
	}
	return ret, nil
}

func (s *Server) DidOpen(ctx context.Context, params *lsp.DidOpenTextDocumentParams) error {
	s.logger.Info("opened",
		zap.String("filename", params.TextDocument.URI.Filename()),
		zap.Uint32("version", uint32(params.TextDocument.Version)),
		zap.String("lang", string(params.TextDocument.LanguageID)))

	filename := params.TextDocument.URI.Filename()
	content := params.TextDocument.Text
	s.docs[filename] = &document{
		version: uint32(params.TextDocument.Version),
		content: content,
	}
	return s.typecheck(ctx, params.TextDocument.URI, uint32(params.TextDocument.Version), content)
}

func (s *Server) DidSave(ctx context.Context, params *lsp.DidSaveTextDocumentParams) error {
	s.logger.Info("saved", zap.String("filename", params.TextDocument.URI.Filename()))
	return nil
}

func (s *Server) DidChange(ctx context.Context, params *lsp.DidChangeTextDocumentParams) error {
	filename := params.TextDocument.URI.Filename()
	content := params.ContentChanges[0].Text
	s.docs[filename] = &document{
		version: uint32(params.TextDocument.Version),
		content: content,
	}
	return s.typecheck(ctx, params.TextDocument.URI, uint32(params.TextDocument.Version), content)
}

func (s *Server) DidClose(ctx context.Context, params *lsp.DidCloseTextDocumentParams) error {
	filename := params.TextDocument.URI.Filename()
	delete(s.docs, filename)
	s.logger.Info("closed", zap.String("filename", params.TextDocument.URI.Filename()))
	return nil
}

func (s *Server) typecheck(ctx context.Context, uri lsp.DocumentURI, version uint32, content string) error {
	var res []lsp.Diagnostic

	_, err := compiler.Compile(ctx, uri.Filename(), content, compiler.Params{CheckOnly: true})
	for _, p := range status.FromError(err) {
		rng, _, _ := strings.Cut(content[p.Origin.Offset:p.Origin.EndOffset], "\n")
		res = append(res, lsp.Diagnostic{
			Range: lsp.Range{
				Start: lsp.Position{Line: uint32(p.Origin.Line - 1), Character: uint32(p.Origin.Column - 1)},
				End:   lsp.Position{Line: uint32(p.Origin.Line - 1), Character: uint32(p.Origin.Column - 1 + len(rng))},
			},
			Severity: lsp.DiagnosticSeverityError,
			Message:  p.Msg,
			Source:   "textmapper",
			Code:     "compile",
		})
	}

	s.logger.Info("typecheck", zap.Uint32("version", version), zap.Int("count", len(res)))
	if len(res) == 0 {
		// nil and empty arrays are marshalled differently.
		res = []lsp.Diagnostic{}
	}
	return s.client.PublishDiagnostics(ctx, &lsp.PublishDiagnosticsParams{
		URI:         uri,
		Version:     version,
		Diagnostics: res,
	})
}

func keepGoing(err tm.SyntaxError) bool { return true }

func (s *Server) Definition(ctx context.Context, params *lsp.DefinitionParams) (result []lsp.Location, err error) {
	filename := params.TextDocument.URI.Filename()
	doc := s.docs[filename]
	if doc == nil {
		return nil, fmt.Errorf("%s is not opened", filename)
	}

	cursor, err := resolvePosition(doc.content, params.Position)
	if err != nil {
		return nil, err
	}

	ids := collectIDs(ctx, filename, doc.content)
	var curr id
	for _, id := range ids {
		if id.Offset() <= cursor && cursor <= id.Endoffset() {
			curr = id
			break
		}
	}
	kind := curr.Kind()

	var ret, refs []lsp.Location
	if kind > 0 {
		for _, id := range ids {
			if id.Kind() != kind || id.Text() != curr.Text() {
				continue
			}
			if id.IsDecl() {
				ret = append(ret, id.Location(params.TextDocument.URI))
			} else {
				refs = append(refs, id.Location(params.TextDocument.URI))
			}
		}
	}

	if len(ret) != 1 || curr.IsDecl() {
		ret = append(ret, refs...)
	}
	if len(ret) == 0 {
		ret = []lsp.Location{}
	}
	s.logger.Info("definition", zap.String("filename", filename), zap.Int("cursor", cursor), zap.Int("count", len(ret)))
	return ret, nil
}

type id struct {
	*ast.Node
	parent tm.NodeType
}

func (id id) IsDecl() bool {
	switch id.parent {
	case tm.LexerState, tm.Nonterm, tm.Lexeme, tm.DirectiveInterface, tm.TemplateParam, tm.InlineParameter:
		return true
	}
	return false
}

func (id id) Kind() int {
	switch id.parent {
	case tm.Symref, tm.Lexeme, tm.Nonterm:
		return 1
	case tm.Stateref, tm.LexerState:
		return 2
	case tm.ReportAs, tm.ReportClause, tm.DirectiveInterface:
		return 3
	case tm.ParamRef, tm.InlineParameter, tm.TemplateParam:
		return 4
	}
	return 0
}

func (id id) Location(uri lsp.DocumentURI) lsp.Location {
	line, col := id.Node.LineColumn()

	// Note: this function does not handle Unicode correctly
	return lsp.Location{
		URI: uri,
		Range: lsp.Range{
			Start: lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1)},
			End:   lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1 + len(id.Node.Text()))},
		},
	}
}

func collectIDs(ctx context.Context, filename, content string) []id {
	tree, _ := ast.Parse(ctx, filename, content, keepGoing)
	if tree == nil {
		return nil
	}

	var ret []id
	var visitor func(n *ast.Node, parent tm.NodeType)
	visitor = func(n *ast.Node, parent tm.NodeType) {
		if n.Type() == tm.Identifier {
			ret = append(ret, id{n, parent})
		}
		for ch := n.Child(selector.Any); ch.IsValid(); ch = ch.Next(selector.Any) {
			visitor(ch, n.Type())
		}
	}
	visitor(tree.Root(), tree.Root().Type())
	return ret
}

func resolvePosition(content string, pos lsp.Position) (int, error) {
	var ret int
	line := pos.Line
	for line > 0 {
		nl := strings.IndexByte(content[ret:], '\n')
		if nl == -1 {
			return 0, fmt.Errorf("line %v does not exist", pos.Line)
		}
		line--
		ret += nl + 1
	}
	col := pos.Character
	for col > 0 {
		r, w := utf8.DecodeRuneInString(content[ret:])
		if r == '\n' || w == 0 {
			return 0, fmt.Errorf("invalid column %v (line %v)", pos.Character, pos.Line)
		}
		ret += w
		col--
		if r > 0xffff {
			if col == 0 {
				return 0, fmt.Errorf("invalid column %v (line %v): between the utf-16 code units", pos.Character, pos.Line)
			}
			col--
		}
	}
	return ret, nil
}
