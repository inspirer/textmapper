// generated by Textmapper; DO NOT EDIT

package js

import (
	"context"
	"fmt"

	"github.com/inspirer/textmapper/parsers/js/token"
)

// TokenStream post-processes lexer output for consumption by the parser.
type TokenStream struct {
	lexer        Lexer
	listener     Listener // for ingesting tokens into the AST, nil during lookaheads
	pending      []symbol
	delayed      symbol // by semicolon insertion
	recoveryMode bool   // forces use of simplified semicolon insertion rules during error recovery

	lastToken token.Type
	lastEnd   int
	lastLine  int
}

type symbol struct {
	symbol    int32
	offset    int
	endoffset int
}

func (s *TokenStream) Init(content string, l Listener) {
	s.lexer.Init(content)
	s.listener = l

	if cap(s.pending) < startTokenBufferSize {
		s.pending = make([]symbol, 0, startTokenBufferSize)
	}
	s.pending = s.pending[:0]
	s.delayed.symbol = noToken
	s.recoveryMode = false
	s.lastToken = token.UNAVAILABLE
}

func (s *TokenStream) Copy() TokenStream {
	ret := *s
	ret.lexer = s.lexer.Copy()
	ret.listener = nil
	ret.pending = nil
	return ret
}

func (s *TokenStream) reportIgnored(ctx context.Context, tok symbol) {
	var t NodeType
	switch token.Type(tok.symbol) {
	case token.MULTILINECOMMENT:
		t = MultiLineComment
	case token.SINGLELINECOMMENT:
		t = SingleLineComment
	case token.INVALID_TOKEN:
		t = InvalidToken
	default:
		return
	}
	if debugSyntax {
		fmt.Printf("ignored: %v as %v\n", token.Type(tok.symbol), t)
	}
	s.listener(t, tok.offset, tok.endoffset)
}

// flush reports all pending tokens up to a given symbol.
func (s *TokenStream) flush(ctx context.Context, sym symbol) {
	if len(s.pending) > 0 && s.listener != nil {
		for i, tok := range s.pending {
			if tok.endoffset > sym.endoffset {
				// Note: this copying should not happen during normal operation, only
				// during error recovery.
				s.pending = append(s.pending[:0], s.pending[i:]...)
				return
			}
			s.reportIgnored(ctx, tok)
		}
		s.pending = s.pending[:0]
	}
}

func (s *TokenStream) text(sym symbol) string {
	return s.lexer.source[sym.offset:sym.endoffset]
}

func (s *TokenStream) line() int {
	return s.lexer.tokenLine
}

func (s *TokenStream) SetDialect(d Dialect) {
	s.lexer.Dialect = d
}