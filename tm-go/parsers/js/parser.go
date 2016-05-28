package js

func (p *Parser) Parse(lexer *Lexer) bool {
	return p.parse(0, 2689, lexer)
}
