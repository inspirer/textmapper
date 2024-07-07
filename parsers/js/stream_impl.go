package js

import (
	"github.com/inspirer/textmapper/parsers/js/token"
)

// next transforms the lexer stream into a stream of symbols for the parser.
//
// This function also takes care of semicolons by implementing the "Automatic
// Semicolon Insertion" rules.
//
// Note: "stack" and "endState" are nil and -1 respectively during lookaheads
// and error recovery.
func (s *TokenStream) next(stack []stackEntry, endState int16) symbol {
	if s.delayed.symbol != noToken {
		ret := s.delayed
		s.delayed.symbol = noToken
		return ret
	}

	lastToken := s.lastToken
	lastEnd := s.lastEnd
restart:
	tok := s.lexer.Next()
	switch tok {
	case token.MULTILINECOMMENT, token.SINGLELINECOMMENT, token.INVALID_TOKEN:
		start, end := s.lexer.Pos()
		tok := symbol{int32(tok), start, end}
		s.pending = append(s.pending, tok)
		goto restart
	case token.GTGT, token.GTGTGT:
		if endState == -1 {
			// If we are error recovering, we cannot rely on the current state of the parser.
			break
		}
		if _, success := reduceAll(stack[:len(stack)-1], stack[len(stack)-1].state, int32(tok), endState); !success {
			tok = token.GT
			s.lexer.offset = s.lexer.tokenOffset + 1
			s.lexer.scanOffset = s.lexer.offset + 1
			s.lexer.ch = '>'
			s.lexer.token = tok
		}
	}
	start, end := s.lexer.Pos()
	ret := symbol{int32(tok), start, end}
	line := s.lexer.Line()

	newLine := line != s.lastLine
	s.lastToken = tok
	s.lastEnd = end
	s.lastLine = line

	if !(newLine || tok == token.RBRACE || tok == token.EOI || lastToken == token.RPAREN) || lastToken == token.SEMICOLON || s.listener == nil {
		// Note: no semicolon insertion during lookaheads or error recovery.
		return ret
	}

	if s.recoveryMode {
		// When recovering from a syntax error, we cannot rely on the current state
		// of the stack and assume that the next token won't be accepted by the
		// parser, so in general we insert more semicolons than needed. This is
		// exactly what we want.
		if newLine || tok == token.RBRACE || tok == token.EOI {
			return s.insertSC(ret, -1 /* no state */, lastEnd)
		}
		return ret
	}

	// We might need to insert a semicolon.
	// See 12.9.1 Rules of Automatic Semicolon Insertion
	if newLine {
		// All but one of the restricted productions can be detected by looking
		// at the last and current tokens.
		restricted := tok == token.ASSIGNGT
		switch lastToken {
		case token.CONTINUE, token.BREAK, token.RETURN, token.THROW:
			restricted = true
		case token.YIELD:
			// No reduce actions are expected, so we can take a shortcut and check
			// the current state.
			restricted = afterYieldStates[int(stack[len(stack)-1].state)]
		case token.ASYNC:
			// No reduce actions are expected, so we can take a shortcut and check
			// the current state.
			restricted = afterAsyncStates[int(stack[len(stack)-1].state)]
		case token.STRINGLITERAL:
			// Assert clauses should appear on the same line.
			restricted = tok == token.ASSERT && noLineBreakStates[int(stack[len(stack)-1].state)]
		}

		if restricted {
			return s.insertSC(ret, stack[len(stack)-1].state, lastEnd)
		}
	}

	// Simulate all pending reductions and check if the current next token
	// will be accepted by the parser.
	state, success := reduceAll(stack[:len(stack)-1], stack[len(stack)-1].state, ret.symbol, endState)

	if newLine && success && (tok == token.PLUSPLUS || tok == token.MINUSMINUS || tok == token.AS || tok == token.EXCL) {
		if noLineBreakStates[int(state)] {
			return s.insertSC(ret, state, lastEnd)
		}
	}

	if success {
		return ret
	}

	if tok == token.RBRACE {
		// Not all closing braces require a semicolon. Double checking.
		if _, success = reduceAll(stack[:len(stack)-1], stack[len(stack)-1].state, int32(token.SEMICOLON), endState); success {
			return s.insertSC(ret, state, lastEnd)
		}
		return ret
	}

	if newLine || tok == token.EOI {
		return s.insertSC(ret, state, lastEnd)
	}

	if lastToken == token.RPAREN && doWhileStates[int(gotoState(state, int32(token.SEMICOLON)))] {
		return s.insertSC(ret, state, lastEnd)
	}
	return ret
}

// insertSC inserts and reports a semicolon, unless there is a overriding rule
// forbidding insertion in this particular location.
func (s *TokenStream) insertSC(next symbol, state int16, offset int) symbol {
	if !s.recoveryMode {
		stateAfterSC := gotoState(state, int32(token.SEMICOLON))
		if stateAfterSC == emptyStatementState || forSCStates[int(stateAfterSC)] {
			// ".. a semicolon is never inserted automatically if the semicolon would
			// then be parsed as an empty statement or if that semicolon would become
			// one of the two semicolons in the header of a for statement."
			return next
		}
	}

	s.delayed = next
	ret := symbol{int32(token.SEMICOLON), offset, offset}
	s.listener(InsertedSemicolon, offset, offset)
	return ret
}
