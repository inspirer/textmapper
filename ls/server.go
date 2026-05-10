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
			DefinitionProvider:   true,
			DocumentSymbolProvider: true,
			HoverProvider:        true,
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

	_, err := compiler.Compile(ctx, uri.Filename(), content, compiler.Params{CheckOnly: true, Verbose: true})
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

func (s *Server) DocumentSymbol(ctx context.Context, params *lsp.DocumentSymbolParams) (result []any, err error) {
	filename := params.TextDocument.URI.Filename()
	doc := s.docs[filename]
	if doc == nil {
		return nil, fmt.Errorf("%s is not opened", filename)
	}

	tree, err := ast.Parse(ctx, filename, doc.content, keepGoing)
	if err != nil || tree == nil {
		s.logger.Info("failed to parse for document symbols", zap.String("filename", filename), zap.Error(err))
		return []any{}, nil
	}

	symbols := s.collectDocumentSymbols(tree, params.TextDocument.URI)
	s.logger.Info("document symbols", zap.String("filename", filename), zap.Int("count", len(symbols)))
	
	// Convert to []any for LSP compatibility
	result = make([]any, len(symbols))
	for i, sym := range symbols {
		result[i] = sym
	}
	return result, nil
}

func (s *Server) Hover(ctx context.Context, params *lsp.HoverParams) (*lsp.Hover, error) {
	filename := params.TextDocument.URI.Filename()
	doc := s.docs[filename]
	if doc == nil {
		return nil, fmt.Errorf("%s is not opened", filename)
	}

	cursor, err := resolvePosition(doc.content, params.Position)
	if err != nil {
		return nil, err
	}

	tree, err := ast.Parse(ctx, filename, doc.content, keepGoing)
	if err != nil || tree == nil {
		s.logger.Info("failed to parse for hover", zap.String("filename", filename), zap.Error(err))
		return nil, nil
	}

	hover := s.getHoverInfo(tree, cursor, doc.content)
	if hover == nil {
		return nil, nil
	}

	s.logger.Info("hover", zap.String("filename", filename), zap.Int("cursor", cursor))
	return hover, nil
}

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

// collectDocumentSymbols extracts terminals and nonterminals from the grammar
func (s *Server) collectDocumentSymbols(tree *ast.Tree, uri lsp.DocumentURI) []lsp.DocumentSymbol {
	var symbols []lsp.DocumentSymbol

	// Walk the AST to find terminals and nonterminals
	var visitor func(n *ast.Node)
	visitor = func(n *ast.Node) {
		switch n.Type() {
		case tm.Lexeme:
			// This is a terminal (lexeme definition)
			if name := n.Child(selector.Identifier); name.IsValid() {
				line, col := name.LineColumn()
				symbols = append(symbols, lsp.DocumentSymbol{
					Name:   name.Text(),
					Kind:   lsp.SymbolKindConstant,
					Detail: "terminal",
					Range: lsp.Range{
						Start: lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1)},
						End:   lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1 + len(name.Text()))},
					},
					SelectionRange: lsp.Range{
						Start: lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1)},
						End:   lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1 + len(name.Text()))},
					},
				})
			}

		case tm.Nonterm:
			// This is a nonterminal (rule definition)
			if name := n.Child(selector.Identifier); name.IsValid() {
				line, col := name.LineColumn()
				
				// Get the rule text for detail
				detail := "nonterminal"
				if ruleBody := n.Child(selector.Rule0); ruleBody.IsValid() {
					// Truncate long rule bodies for readability
					ruleText := strings.TrimSpace(ruleBody.Text())
					if len(ruleText) > 100 {
						ruleText = ruleText[:97] + "..."
					}
					detail = fmt.Sprintf("nonterminal: %s", ruleText)
				}

				symbols = append(symbols, lsp.DocumentSymbol{
					Name:   name.Text(),
					Kind:   lsp.SymbolKindFunction,
					Detail: detail,
					Range: lsp.Range{
						Start: lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1)},
						End:   lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1 + len(name.Text()))},
					},
					SelectionRange: lsp.Range{
						Start: lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1)},
						End:   lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1 + len(name.Text()))},
					},
				})
			}
		}

		// Recurse into children
		for ch := n.Child(selector.Any); ch.IsValid(); ch = ch.Next(selector.Any) {
			visitor(ch)
		}
	}

	visitor(tree.Root())
	return symbols
}

// getHoverInfo provides hover information for the symbol at the given cursor position
func (s *Server) getHoverInfo(tree *ast.Tree, cursor int, fileContent string) *lsp.Hover {
	// Find the AST node at the cursor position
	var targetNode *ast.Node
	var visitor func(n *ast.Node)
	visitor = func(n *ast.Node) {
		if n.Offset() <= cursor && cursor < n.Endoffset() {
			targetNode = n
		}
		for ch := n.Child(selector.Any); ch.IsValid(); ch = ch.Next(selector.Any) {
			visitor(ch)
		}
	}
	visitor(tree.Root())

	if targetNode == nil {
		return nil
	}

	// Check if we're hovering over a symbol reference or definition
	var symbolName string
	var symbolNode *ast.Node

	// Walk up the tree to find if we're in a symbol reference or definition
	// We need to manually walk up since Parent() is not public
	symbolNode = s.findParentIdentifier(tree, targetNode)
	if symbolNode != nil && symbolNode.Type() == tm.Identifier {
		symbolName = symbolNode.Text()
	}

	if symbolName == "" {
		return nil
	}

	// Find the definition of this symbol
	symbolDef := s.findSymbolDefinition(tree, symbolName)
	if symbolDef == nil {
		return nil
	}

	// Create hover content
	var hoverContent []string

	// Add original definition
	defText := s.getDefinitionText(symbolDef, tree.Text())
	if defText != "" {
		hoverContent = append(hoverContent, "**Definition:**")
		hoverContent = append(hoverContent, "```textmapper")
		hoverContent = append(hoverContent, defText)
		hoverContent = append(hoverContent, "```")
	}

	// Add expanded/desugared version for nonterminals
	if symbolDef.symbolType == "nonterminal" {
		expanded := s.getExpandedDefinition(symbolDef, tree.Text())
		if expanded != "" && expanded != defText {
			hoverContent = append(hoverContent, "")
			hoverContent = append(hoverContent, "**Expanded:**")
			hoverContent = append(hoverContent, "```textmapper")
			hoverContent = append(hoverContent, expanded)
			hoverContent = append(hoverContent, "```")
		}
	}

	if len(hoverContent) == 0 {
		return nil
	}

	line, col := symbolNode.LineColumn()
	return &lsp.Hover{
		Contents: lsp.MarkupContent{
			Kind:  lsp.Markdown,
			Value: strings.Join(hoverContent, "\n"),
		},
		Range: &lsp.Range{
			Start: lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1)},
			End:   lsp.Position{Line: uint32(line - 1), Character: uint32(col - 1 + len(symbolName))},
		},
	}
}

type symbolDefinition struct {
	name       string
	symbolType string // "terminal" or "nonterminal"
	node       *ast.Node
	ruleNode   *ast.Node // For nonterminals, the rule body
}

// findParentIdentifier finds an identifier node that contains the target node
func (s *Server) findParentIdentifier(tree *ast.Tree, targetNode *ast.Node) *ast.Node {
	// If the target node itself is an identifier, return it
	if targetNode.Type() == tm.Identifier {
		return targetNode
	}

	// Look for identifier nodes that contain the target cursor position
	var identifierNode *ast.Node
	var visitor func(n *ast.Node)
	visitor = func(n *ast.Node) {
		if n.Type() == tm.Identifier {
			if n.Offset() <= targetNode.Offset() && targetNode.Endoffset() <= n.Endoffset() {
				identifierNode = n
			}
		}
		for ch := n.Child(selector.Any); ch.IsValid(); ch = ch.Next(selector.Any) {
			visitor(ch)
		}
	}
	
	visitor(tree.Root())
	return identifierNode
}

// findSymbolDefinition locates the definition of a symbol by name
func (s *Server) findSymbolDefinition(tree *ast.Tree, symbolName string) *symbolDefinition {
	var result *symbolDefinition

	var visitor func(n *ast.Node)
	visitor = func(n *ast.Node) {
		switch n.Type() {
		case tm.Lexeme:
			if name := n.Child(selector.Identifier); name.IsValid() && name.Text() == symbolName {
				result = &symbolDefinition{
					name:       symbolName,
					symbolType: "terminal",
					node:       n,
				}
				return
			}

		case tm.Nonterm:
			if name := n.Child(selector.Identifier); name.IsValid() && name.Text() == symbolName {
				result = &symbolDefinition{
					name:       symbolName,
					symbolType: "nonterminal",
					node:       n,
					ruleNode:   n.Child(selector.Rule0),
				}
				return
			}
		}

		// Continue searching if not found
		if result == nil {
			for ch := n.Child(selector.Any); ch.IsValid(); ch = ch.Next(selector.Any) {
				visitor(ch)
			}
		}
	}

	visitor(tree.Root())
	return result
}

// getDefinitionText extracts the definition text without semantic actions
func (s *Server) getDefinitionText(def *symbolDefinition, content string) string {
	if def.node == nil {
		return ""
	}

	// Get the text of the definition
	text := def.node.Text()
	
	// For nonterminals, remove semantic actions
	if def.symbolType == "nonterminal" {
		text = s.removeSemanticActions(text)
	}

	return text
}

// getExpandedDefinition returns the expanded/desugared version of a nonterminal
func (s *Server) getExpandedDefinition(def *symbolDefinition, content string) string {
	if def.symbolType != "nonterminal" || def.ruleNode == nil {
		return ""
	}

	// For now, return the same as definition text but with expanded syntax sugar removed
	// This is a simplified implementation - a full implementation would expand
	// operators like ?, *, +, etc.
	text := def.ruleNode.Text()
	expanded := s.expandSyntaxSugar(s.removeSemanticActions(text))
	return fmt.Sprintf("%s: %s", def.name, expanded)
}

// removeSemanticActions removes { ... } blocks from rule text
func (s *Server) removeSemanticActions(text string) string {
	var result strings.Builder
	braceDepth := 0
	inAction := false

	for _, r := range text {
		if r == '{' {
			if braceDepth == 0 {
				inAction = true
			}
			braceDepth++
		} else if r == '}' {
			braceDepth--
			if braceDepth == 0 {
				inAction = false
			}
		} else if !inAction {
			result.WriteRune(r)
		}
	}

	// Clean up extra whitespace
	return strings.Join(strings.Fields(result.String()), " ")
}

// expandSyntaxSugar provides basic expansion of syntax sugar (simplified)
func (s *Server) expandSyntaxSugar(text string) string {
	// This is a simplified expansion - a full implementation would parse the grammar
	// and properly expand operators
	
	// Replace some common patterns
	text = strings.ReplaceAll(text, "?", "| %empty")
	
	// For * and +, this would need more complex parsing to properly handle
	// For now, just add a comment about expansion
	if strings.Contains(text, "*") || strings.Contains(text, "+") {
		text = text + " /* list expansion omitted for brevity */"
	}

	return text
}
