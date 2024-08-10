package ls

import (
	"context"

	"go.lsp.dev/jsonrpc2"
	lsp "go.lsp.dev/protocol"
)

type unimpl struct{}

var errUnimplemented = jsonrpc2.ErrMethodNotFound

var _ lsp.Server = unimpl{}

func (unimpl) Initialize(ctx context.Context, params *lsp.InitializeParams) (result *lsp.InitializeResult, err error) {
	return nil, errUnimplemented
}

func (unimpl) Initialized(ctx context.Context, params *lsp.InitializedParams) error {
	return errUnimplemented
}

func (unimpl) Shutdown(ctx context.Context) error { return errUnimplemented }

func (unimpl) Exit(ctx context.Context) error { return errUnimplemented }

func (unimpl) WorkDoneProgressCancel(ctx context.Context, params *lsp.WorkDoneProgressCancelParams) error {
	return errUnimplemented
}

func (unimpl) LogTrace(ctx context.Context, params *lsp.LogTraceParams) error {
	return errUnimplemented
}

func (unimpl) SetTrace(ctx context.Context, params *lsp.SetTraceParams) error {
	return errUnimplemented
}

func (unimpl) CodeAction(ctx context.Context, params *lsp.CodeActionParams) (result []lsp.CodeAction, err error) {
	return nil, errUnimplemented
}

func (unimpl) CodeLens(ctx context.Context, params *lsp.CodeLensParams) (result []lsp.CodeLens, err error) {
	return nil, errUnimplemented
}

func (unimpl) CodeLensResolve(ctx context.Context, params *lsp.CodeLens) (result *lsp.CodeLens, err error) {
	return nil, errUnimplemented
}

func (unimpl) ColorPresentation(ctx context.Context, params *lsp.ColorPresentationParams) (result []lsp.ColorPresentation, err error) {
	return nil, errUnimplemented
}

func (unimpl) Completion(ctx context.Context, params *lsp.CompletionParams) (result *lsp.CompletionList, err error) {
	return nil, errUnimplemented
}

func (unimpl) CompletionResolve(ctx context.Context, params *lsp.CompletionItem) (result *lsp.CompletionItem, err error) {
	return nil, errUnimplemented
}

func (unimpl) Declaration(ctx context.Context, params *lsp.DeclarationParams) (result []lsp.Location, err error) {
	return nil, errUnimplemented
}

func (unimpl) Definition(ctx context.Context, params *lsp.DefinitionParams) (result []lsp.Location, err error) {
	return nil, errUnimplemented
}

func (unimpl) DidChange(ctx context.Context, params *lsp.DidChangeTextDocumentParams) error {
	return errUnimplemented
}

func (unimpl) DidChangeConfiguration(ctx context.Context, params *lsp.DidChangeConfigurationParams) error {
	return errUnimplemented
}

func (unimpl) DidChangeWatchedFiles(ctx context.Context, params *lsp.DidChangeWatchedFilesParams) error {
	return errUnimplemented
}

func (unimpl) DidChangeWorkspaceFolders(ctx context.Context, params *lsp.DidChangeWorkspaceFoldersParams) error {
	return errUnimplemented
}

func (unimpl) DidClose(ctx context.Context, params *lsp.DidCloseTextDocumentParams) error {
	return errUnimplemented
}

func (unimpl) DidOpen(ctx context.Context, params *lsp.DidOpenTextDocumentParams) error {
	return errUnimplemented
}

func (unimpl) DidSave(ctx context.Context, params *lsp.DidSaveTextDocumentParams) error {
	return errUnimplemented
}

func (unimpl) DocumentColor(ctx context.Context, params *lsp.DocumentColorParams) (result []lsp.ColorInformation, err error) {
	return nil, errUnimplemented
}

func (unimpl) DocumentHighlight(ctx context.Context, params *lsp.DocumentHighlightParams) (result []lsp.DocumentHighlight, err error) {
	return nil, errUnimplemented
}

func (unimpl) DocumentLink(ctx context.Context, params *lsp.DocumentLinkParams) (result []lsp.DocumentLink, err error) {
	return nil, errUnimplemented
}

func (unimpl) DocumentLinkResolve(ctx context.Context, params *lsp.DocumentLink) (result *lsp.DocumentLink, err error) {
	return nil, errUnimplemented
}

func (unimpl) DocumentSymbol(ctx context.Context, params *lsp.DocumentSymbolParams) (result []any, err error) {
	return nil, errUnimplemented
}

func (unimpl) ExecuteCommand(ctx context.Context, params *lsp.ExecuteCommandParams) (result any, err error) {
	return nil, errUnimplemented
}

func (unimpl) FoldingRanges(ctx context.Context, params *lsp.FoldingRangeParams) (result []lsp.FoldingRange, err error) {
	return nil, errUnimplemented
}

func (unimpl) Formatting(ctx context.Context, params *lsp.DocumentFormattingParams) (result []lsp.TextEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) Hover(ctx context.Context, params *lsp.HoverParams) (result *lsp.Hover, err error) {
	return nil, errUnimplemented
}

func (unimpl) Implementation(ctx context.Context, params *lsp.ImplementationParams) (result []lsp.Location, err error) {
	return nil, errUnimplemented
}

func (unimpl) OnTypeFormatting(ctx context.Context, params *lsp.DocumentOnTypeFormattingParams) (result []lsp.TextEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) PrepareRename(ctx context.Context, params *lsp.PrepareRenameParams) (result *lsp.Range, err error) {
	return nil, errUnimplemented
}

func (unimpl) RangeFormatting(ctx context.Context, params *lsp.DocumentRangeFormattingParams) (result []lsp.TextEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) References(ctx context.Context, params *lsp.ReferenceParams) (result []lsp.Location, err error) {
	return nil, errUnimplemented
}

func (unimpl) Rename(ctx context.Context, params *lsp.RenameParams) (result *lsp.WorkspaceEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) SignatureHelp(ctx context.Context, params *lsp.SignatureHelpParams) (result *lsp.SignatureHelp, err error) {
	return nil, errUnimplemented
}

func (unimpl) Symbols(ctx context.Context, params *lsp.WorkspaceSymbolParams) (result []lsp.SymbolInformation, err error) {
	return nil, errUnimplemented
}

func (unimpl) TypeDefinition(ctx context.Context, params *lsp.TypeDefinitionParams) (result []lsp.Location, err error) {
	return nil, errUnimplemented
}

func (unimpl) WillSave(ctx context.Context, params *lsp.WillSaveTextDocumentParams) error {
	return errUnimplemented
}

func (unimpl) WillSaveWaitUntil(ctx context.Context, params *lsp.WillSaveTextDocumentParams) (result []lsp.TextEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) ShowDocument(ctx context.Context, params *lsp.ShowDocumentParams) (result *lsp.ShowDocumentResult, err error) {
	return nil, errUnimplemented
}

func (unimpl) WillCreateFiles(ctx context.Context, params *lsp.CreateFilesParams) (result *lsp.WorkspaceEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) DidCreateFiles(ctx context.Context, params *lsp.CreateFilesParams) error {
	return errUnimplemented
}

func (unimpl) WillRenameFiles(ctx context.Context, params *lsp.RenameFilesParams) (result *lsp.WorkspaceEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) DidRenameFiles(ctx context.Context, params *lsp.RenameFilesParams) error {
	return errUnimplemented
}

func (unimpl) WillDeleteFiles(ctx context.Context, params *lsp.DeleteFilesParams) (result *lsp.WorkspaceEdit, err error) {
	return nil, errUnimplemented
}

func (unimpl) DidDeleteFiles(ctx context.Context, params *lsp.DeleteFilesParams) error {
	return errUnimplemented
}

func (unimpl) CodeLensRefresh(ctx context.Context) error { return errUnimplemented }

func (unimpl) PrepareCallHierarchy(ctx context.Context, params *lsp.CallHierarchyPrepareParams) (result []lsp.CallHierarchyItem, err error) {
	return nil, errUnimplemented
}

func (unimpl) IncomingCalls(ctx context.Context, params *lsp.CallHierarchyIncomingCallsParams) (result []lsp.CallHierarchyIncomingCall, err error) {
	return nil, errUnimplemented
}

func (unimpl) OutgoingCalls(ctx context.Context, params *lsp.CallHierarchyOutgoingCallsParams) (result []lsp.CallHierarchyOutgoingCall, err error) {
	return nil, errUnimplemented
}

func (unimpl) SemanticTokensFull(ctx context.Context, params *lsp.SemanticTokensParams) (result *lsp.SemanticTokens, err error) {
	return nil, errUnimplemented
}

func (unimpl) SemanticTokensFullDelta(ctx context.Context, params *lsp.SemanticTokensDeltaParams) (result any, err error) {
	return nil, errUnimplemented
}

func (unimpl) SemanticTokensRange(ctx context.Context, params *lsp.SemanticTokensRangeParams) (result *lsp.SemanticTokens, err error) {
	return nil, errUnimplemented
}

func (unimpl) SemanticTokensRefresh(ctx context.Context) error { return errUnimplemented }

func (unimpl) LinkedEditingRange(ctx context.Context, params *lsp.LinkedEditingRangeParams) (result *lsp.LinkedEditingRanges, err error) {
	return nil, errUnimplemented
}

func (unimpl) Moniker(ctx context.Context, params *lsp.MonikerParams) (result []lsp.Moniker, err error) {
	return nil, errUnimplemented
}

func (unimpl) Request(ctx context.Context, method string, params any) (result any, err error) {
	return nil, errUnimplemented
}
