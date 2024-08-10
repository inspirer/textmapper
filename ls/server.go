package ls

import (
	"context"
	"fmt"
	"path"
	"strings"

	"github.com/inspirer/textmapper/compiler"
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

	unimpl
}

func NewServer(logger *zap.Logger) *Server {
	return &Server{logger: logger}
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
		},
	}
	return ret, nil
}

func (s *Server) DidOpen(ctx context.Context, params *lsp.DidOpenTextDocumentParams) error {
	s.logger.Info("opened",
		zap.String("filename", params.TextDocument.URI.Filename()),
		zap.Uint32("version", uint32(params.TextDocument.Version)),
		zap.String("lang", string(params.TextDocument.LanguageID)))

	ext := path.Ext(params.TextDocument.URI.Filename())
	if ext != ".tm" {
		return nil
	}
	content := params.TextDocument.Text
	return s.typecheck(ctx, params.TextDocument.URI, uint32(params.TextDocument.Version), content)
}

func (s *Server) DidSave(ctx context.Context, params *lsp.DidSaveTextDocumentParams) error {
	s.logger.Info("saved", zap.String("filename", params.TextDocument.URI.Filename()))
	return nil
}

func (s *Server) DidChange(ctx context.Context, params *lsp.DidChangeTextDocumentParams) error {
	ext := path.Ext(params.TextDocument.URI.Filename())
	if ext != ".tm" {
		return nil
	}
	content := params.ContentChanges[0].Text
	return s.typecheck(ctx, params.TextDocument.URI, uint32(params.TextDocument.Version), content)
}

func (s *Server) DidClose(ctx context.Context, params *lsp.DidCloseTextDocumentParams) error {
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
