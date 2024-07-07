package js

// Copy forks the lexer in its current state.
func (l *Lexer) Copy() Lexer {
	ret := *l
	// Note: empty stack is okay for lookahead purposes, since the stack is
	// used for JSX tags and not within TS/JS code.
	ret.Stack = nil
	return ret
}
