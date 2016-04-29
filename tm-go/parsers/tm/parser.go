package tm

import (
	"fmt"
	"github.com/inspirer/textmapper/tm-go/parsers/tm/ast"
)

// Parser is a table-driven LALR parser for tm.
type Parser struct {
	err ErrorHandler

	stack []node
	lexer *Lexer
	next Token
}

type node struct {
	symbol    int32
	state     int32
	value     interface{}
	offset    int
	endoffset int
}

func (p *Parser) Init(err ErrorHandler) {
	p.err = err
}

const (
	startStackSize = 512
	debugSyntax    = false
)

func (p *Parser) ParseInput(lexer *Lexer) (bool, *ast.Input) {
	ok, v := p.parse(0, 417, lexer)
	val, _ := v.(*ast.Input)
	return ok, val
}

func (p *Parser) ParseExpression(lexer *Lexer) (bool, ast.Expression) {
	ok, v := p.parse(1, 418, lexer)
	val, _ := v.(ast.Expression)
	return ok, val
}

func (p *Parser) parse(start, end int32, lexer *Lexer) (bool, interface{}) {
	if cap(p.stack) < startStackSize {
		p.stack = make([]node, 0, startStackSize)
	}
	state := start
	recovering := 0

	p.stack = append(p.stack[:0], node{state: state})
	p.lexer = lexer
	p.next = lexer.Next()

	for state != end {
		action := p.action(state)

		if action >= 0 {
			// Reduce.
			rule := action
			ln := int(tmRuleLen[rule])

			var node node
			node.symbol = tmRuleSymbol[rule]
			if debugSyntax {
				fmt.Printf("reduce to: %v\n", tmSymbolNames[node.symbol-int32(terminalEnd)])
			}
			if ln == 0 {
				node.offset, _ = lexer.Pos()
				node.endoffset = node.offset
			} else {
				node.offset = p.stack[len(p.stack)-ln].offset
				node.endoffset = p.stack[len(p.stack)-1].endoffset
			}
			p.applyRule(rule, &node, p.stack[len(p.stack)-ln:])
			p.stack = p.stack[:len(p.stack)-ln]
			state = p.gotoState(p.stack[len(p.stack)-1].state, node.symbol)
			node.state = state
			p.stack = append(p.stack, node)

		} else if action == -1 {
			// Shift.
			if p.next == UNAVAILABLE {
				p.next = lexer.Next()
			}
			state = p.gotoState(state, int32(p.next))
			s, e := lexer.Pos()
			p.stack = append(p.stack, node{
				symbol:    int32(p.next),
				state:     state,
				value:     lexer.Value(),
				offset:    s,
				endoffset: e,
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", p.next, lexer.Text())
			}
			if state != -1 && p.next != EOI {
				p.next = UNAVAILABLE
			}
			if recovering > 0 {
				recovering--
			}
		}

		if action == -2 || state == -1 {
			if p.recover() {
				state = p.stack[len(p.stack)-1].state
				if recovering == 0 {
					offset, endoffset := lexer.Pos()
					line := lexer.Line()
					p.err(line, offset, endoffset - offset, "syntax error")
				}
				if recovering >= 3 {
					p.next = lexer.Next()
				}
				recovering = 4
				continue
			}
			if len(p.stack) == 0 {
				state = start
				p.stack = append(p.stack, node{state: state})
			}
			break
		}
	}

	if state != end {
		if recovering > 0 {
			return false, nil
		}
		offset, endoffset := lexer.Pos()
		line := lexer.Line()
		p.err(line, offset, endoffset - offset, "syntax error")
		return false, nil
	}

	return true, p.stack[len(p.stack)-2].value
}

const errSymbol = 38

func (p *Parser) recover() bool {
	if p.next == UNAVAILABLE {
		p.next = p.lexer.Next()
	}
	if p.next == EOI {
		return false
	}
	e, _ := p.lexer.Pos()
	s := e
	for len(p.stack) > 0 && p.gotoState(p.stack[len(p.stack)-1].state, errSymbol) == -1 {
	    // TODO cleanup
		p.stack = p.stack[:len(p.stack)-1]
		s = p.stack[len(p.stack)-1].offset
	}
	if len(p.stack) > 0 {
	    state := p.gotoState(p.stack[len(p.stack)-1].state, errSymbol)
		p.stack = append(p.stack, node{
			symbol:    errSymbol,
			state:     state,
			offset:    s,
			endoffset: e,
		})
		return true
	}
	return false
}

func (p *Parser) action(state int32) int32 {
	a := tmAction[state]
	if a < -2 {
		// Lookahead is needed.
		if p.next == UNAVAILABLE {
			p.next = p.lexer.Next()
		}
		a = -a - 3
		for ; tmLalr[a] >= 0; a += 2 {
			if tmLalr[a] == int32(p.next) {
				break
			}
		}
		return tmLalr[a+1]
	}
	return a
}

func (p *Parser) gotoState(state, symbol int32) int32 {
	min := tmGoto[symbol]
	max := tmGoto[symbol+1] - 1

	for min <= max {
		e := (min + max) >> 1
		i := tmFrom[e]
		if i == state {
			return tmTo[e]
		} else if i < state {
			min = e + 1
		} else {
			max = e - 1
		}
	}
	return -1
}

func (p* Parser) applyRule(rule int32, node *node, rhs []node) {
	switch (rule) {
	case 0:  // import__optlist ::= import__optlist import_
		nn0, _ := rhs[0].value.([]*ast.Import)
nn1, _ := rhs[1].value.(*ast.Import)
node.value = append( nn0,  nn1)
	case 1:  // import__optlist ::=
		node.value = []*ast.Import{}
	case 2:  // input ::= header import__optlist option_optlist lexer_section parser_section
		nn0, _ := rhs[0].value.(*ast.Header)
nn1, _ := rhs[1].value.([]*ast.Import)
nn2, _ := rhs[2].value.([]*ast.Option)
nn3, _ := rhs[3].value.([]ast.LexerPart)
nn4, _ := rhs[4].value.([]ast.GrammarPart)
node.value = &ast.Input{
			Header:  nn0,
			Imports:  nn1,
			Options:  nn2,
			Lexer:  nn3,
			Parser:  nn4,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 3:  // input ::= header import__optlist option_optlist lexer_section
		nn0, _ := rhs[0].value.(*ast.Header)
nn1, _ := rhs[1].value.([]*ast.Import)
nn2, _ := rhs[2].value.([]*ast.Option)
nn3, _ := rhs[3].value.([]ast.LexerPart)
node.value = &ast.Input{
			Header:  nn0,
			Imports:  nn1,
			Options:  nn2,
			Lexer:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 4:  // option_optlist ::= option_optlist option
		nn0, _ := rhs[0].value.([]*ast.Option)
nn1, _ := rhs[1].value.(*ast.Option)
node.value = append( nn0,  nn1)
	case 5:  // option_optlist ::=
		node.value = []*ast.Option{}
	case 6:  // header ::= Llanguage name '(' name ')' parsing_algorithmopt ';'
		nn1, _ := rhs[1].value.(*ast.Name)
nn3, _ := rhs[3].value.(*ast.Name)
nn5, _ := rhs[5].value.(*ast.ParsingAlgorithm)
node.value = &ast.Header{
			Name:  nn1,
			Target:  nn3,
			ParsingAlgorithm:  nn5,
			Pos: ast.Pos{rhs[0].offset, rhs[6].endoffset},
}
	case 7:  // header ::= Llanguage name parsing_algorithmopt ';'
		nn1, _ := rhs[1].value.(*ast.Name)
nn2, _ := rhs[2].value.(*ast.ParsingAlgorithm)
node.value = &ast.Header{
			Name:  nn1,
			ParsingAlgorithm:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 8:  // lexer_section ::= '::' Llexer lexer_parts
		nn2, _ := rhs[2].value.([]ast.LexerPart)
node.value =  nn2
	case 9:  // parser_section ::= '::' Lparser grammar_parts
		nn2, _ := rhs[2].value.([]ast.GrammarPart)
node.value =  nn2
	case 10:  // parsing_algorithm ::= Llalr '(' icon ')'
		nn2, _ := rhs[2].value.(int)
node.value = &ast.ParsingAlgorithm{
			La:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 11:  // import_ ::= Limport ID scon ';'
		nn1, _ := rhs[1].value.(string)
nn2, _ := rhs[2].value.(string)
node.value = &ast.Import{
			Alias:  nn1,
			File:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 12:  // import_ ::= Limport scon ';'
		nn1, _ := rhs[1].value.(string)
node.value = &ast.Import{
			File:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 13:  // option ::= ID '=' expression
		nn0, _ := rhs[0].value.(string)
nn2, _ := rhs[2].value.(ast.Expression)
node.value = &ast.Option{
			Key:  nn0,
			Value:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 14:  // option ::= syntax_problem
		nn0, _ := rhs[0].value.(*ast.SyntaxProblem)
node.value = &ast.Option{
			SyntaxProblem:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 15:  // identifier ::= ID
		nn0, _ := rhs[0].value.(string)
node.value = &ast.Identifier{
			ID:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 16:  // symref ::= ID symref_args
		nn0, _ := rhs[0].value.(string)
nn1, _ := rhs[1].value.(*ast.SymrefArgs)
node.value = &ast.Symref{
			Name:  nn0,
			Args:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 17:  // symref ::= ID
		nn0, _ := rhs[0].value.(string)
node.value = &ast.Symref{
			Name:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 18:  // symref_noargs ::= ID
		nn0, _ := rhs[0].value.(string)
node.value = &ast.Symref{
			Name:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 19:  // type ::= '(' scon ')'
		nn1, _ := rhs[1].value.(string)
{ node.value =  nn1; }
	case 20:  // type ::= '(' type_part_list ')'
		{ node.value = "TODO" }
	case 36:  // pattern ::= regexp
		nn0, _ := rhs[0].value.(string)
node.value = &ast.Pattern{
			REGEXP:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 37:  // lexer_parts ::= lexer_part
		nn0, _ := rhs[0].value.(ast.LexerPart)
node.value = []ast.LexerPart{ nn0}
	case 38:  // lexer_parts ::= lexer_parts lexer_part
		nn0, _ := rhs[0].value.([]ast.LexerPart)
nn1, _ := rhs[1].value.(ast.LexerPart)
node.value = append( nn0,  nn1)
	case 39:  // lexer_parts ::= lexer_parts syntax_problem
		nn0, _ := rhs[0].value.([]ast.LexerPart)
nn1, _ := rhs[1].value.(*ast.SyntaxProblem)
node.value = append( nn0,  nn1)
	case 44:  // named_pattern ::= ID '=' pattern
		nn0, _ := rhs[0].value.(string)
nn2, _ := rhs[2].value.(*ast.Pattern)
node.value = &ast.NamedPattern{
			Name:  nn0,
			Pattern:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 45:  // lexeme ::= identifier typeopt ':' pattern lexeme_transitionopt iconopt lexeme_attrsopt commandopt
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn1, _ := rhs[1].value.(string)
nn3, _ := rhs[3].value.(*ast.Pattern)
nn4, _ := rhs[4].value.(*ast.Stateref)
nn5, _ := rhs[5].value.(int)
nn6, _ := rhs[6].value.(*ast.LexemeAttrs)
nn7, _ := rhs[7].value.(*ast.Command)
node.value = &ast.Lexeme{
			Name:  nn0,
			Type:  nn1,
			Pattern:  nn3,
			Transition:  nn4,
			Priority:  nn5,
			Attrs:  nn6,
			Command:  nn7,
			Pos: ast.Pos{rhs[0].offset, rhs[7].endoffset},
}
	case 46:  // lexeme ::= identifier typeopt ':'
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn1, _ := rhs[1].value.(string)
node.value = &ast.Lexeme{
			Name:  nn0,
			Type:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 47:  // lexeme_transition ::= '=>' stateref
		nn1, _ := rhs[1].value.(*ast.Stateref)
node.value =  nn1
	case 48:  // lexeme_attrs ::= '(' lexeme_attribute ')'
		nn1, _ := rhs[1].value.(ast.LexemeAttribute)
node.value = &ast.LexemeAttrs{
			Kind:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 49:  // lexeme_attribute ::= Lsoft
		node.value = ast.LexemeAttribute_LSOFT
	case 50:  // lexeme_attribute ::= Lclass
		node.value = ast.LexemeAttribute_LCLASS
	case 51:  // lexeme_attribute ::= Lspace
		node.value = ast.LexemeAttribute_LSPACE
	case 52:  // lexeme_attribute ::= Llayout
		node.value = ast.LexemeAttribute_LLAYOUT
	case 53:  // lexer_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
		nn2, _ := rhs[2].value.(*ast.Symref)
nn3, _ := rhs[3].value.(*ast.Symref)
node.value = &ast.DirectiveBrackets{
			Opening:  nn2,
			Closing:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 54:  // lexer_state_list_Comma_separated ::= lexer_state_list_Comma_separated ',' lexer_state
		nn0, _ := rhs[0].value.([]*ast.LexerState)
nn2, _ := rhs[2].value.(*ast.LexerState)
node.value = append( nn0,  nn2)
	case 55:  // lexer_state_list_Comma_separated ::= lexer_state
		nn0, _ := rhs[0].value.(*ast.LexerState)
node.value = []*ast.LexerState{ nn0}
	case 56:  // state_selector ::= '[' lexer_state_list_Comma_separated ']'
		nn1, _ := rhs[1].value.([]*ast.LexerState)
node.value = &ast.StateSelector{
			States:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 57:  // stateref ::= ID
		nn0, _ := rhs[0].value.(string)
node.value = &ast.Stateref{
			Name:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 58:  // lexer_state ::= identifier '=>' stateref
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(*ast.Stateref)
node.value = &ast.LexerState{
			Name:  nn0,
			DefaultTransition:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 59:  // lexer_state ::= identifier
		nn0, _ := rhs[0].value.(*ast.Identifier)
node.value = &ast.LexerState{
			Name:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 60:  // grammar_parts ::= grammar_part
		nn0, _ := rhs[0].value.(ast.GrammarPart)
node.value = []ast.GrammarPart{ nn0}
	case 61:  // grammar_parts ::= grammar_parts grammar_part
		nn0, _ := rhs[0].value.([]ast.GrammarPart)
nn1, _ := rhs[1].value.(ast.GrammarPart)
node.value = append( nn0,  nn1)
	case 62:  // grammar_parts ::= grammar_parts syntax_problem
		nn0, _ := rhs[0].value.([]ast.GrammarPart)
nn1, _ := rhs[1].value.(*ast.SyntaxProblem)
node.value = append( nn0,  nn1)
	case 66:  // nonterm ::= annotations identifier nonterm_params nonterm_type '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Annotations)
nn1, _ := rhs[1].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(*ast.NontermParams)
nn3, _ := rhs[3].value.(ast.NontermType)
nn5, _ := rhs[5].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Annotations:  nn0,
			Name:  nn1,
			Params:  nn2,
			Type:  nn3,
			Rules:  nn5,
			Pos: ast.Pos{rhs[0].offset, rhs[6].endoffset},
}
	case 67:  // nonterm ::= annotations identifier nonterm_params '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Annotations)
nn1, _ := rhs[1].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(*ast.NontermParams)
nn4, _ := rhs[4].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Annotations:  nn0,
			Name:  nn1,
			Params:  nn2,
			Rules:  nn4,
			Pos: ast.Pos{rhs[0].offset, rhs[5].endoffset},
}
	case 68:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Annotations)
nn1, _ := rhs[1].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(ast.NontermType)
nn4, _ := rhs[4].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Annotations:  nn0,
			Name:  nn1,
			Type:  nn2,
			Rules:  nn4,
			Pos: ast.Pos{rhs[0].offset, rhs[5].endoffset},
}
	case 69:  // nonterm ::= annotations identifier '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Annotations)
nn1, _ := rhs[1].value.(*ast.Identifier)
nn3, _ := rhs[3].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Annotations:  nn0,
			Name:  nn1,
			Rules:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 70:  // nonterm ::= identifier nonterm_params nonterm_type '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn1, _ := rhs[1].value.(*ast.NontermParams)
nn2, _ := rhs[2].value.(ast.NontermType)
nn4, _ := rhs[4].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Name:  nn0,
			Params:  nn1,
			Type:  nn2,
			Rules:  nn4,
			Pos: ast.Pos{rhs[0].offset, rhs[5].endoffset},
}
	case 71:  // nonterm ::= identifier nonterm_params '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn1, _ := rhs[1].value.(*ast.NontermParams)
nn3, _ := rhs[3].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Name:  nn0,
			Params:  nn1,
			Rules:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 72:  // nonterm ::= identifier nonterm_type '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn1, _ := rhs[1].value.(ast.NontermType)
nn3, _ := rhs[3].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Name:  nn0,
			Type:  nn1,
			Rules:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 73:  // nonterm ::= identifier '::=' rules ';'
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn2, _ := rhs[2].value.([]*ast.Rule0)
node.value = &ast.Nonterm{
			Name:  nn0,
			Rules:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 74:  // nonterm_type ::= Lreturns symref_noargs
		nn1, _ := rhs[1].value.(*ast.Symref)
node.value = &ast.NontermTypeAST{
			Reference:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 75:  // nonterm_type ::= Linline Lclass identifieropt implementsopt
		nn2, _ := rhs[2].value.(*ast.Identifier)
nn3, _ := rhs[3].value.([]*ast.Symref)
node.value = &ast.NontermTypeHint{
			Inline: true,
			Kind: ast.NontermTypeHint_LCLASS,
			Name:  nn2,
			Implements:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 76:  // nonterm_type ::= Lclass identifieropt implementsopt
		nn1, _ := rhs[1].value.(*ast.Identifier)
nn2, _ := rhs[2].value.([]*ast.Symref)
node.value = &ast.NontermTypeHint{
			Kind: ast.NontermTypeHint_LCLASS,
			Name:  nn1,
			Implements:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 77:  // nonterm_type ::= Linterface identifieropt implementsopt
		nn1, _ := rhs[1].value.(*ast.Identifier)
nn2, _ := rhs[2].value.([]*ast.Symref)
node.value = &ast.NontermTypeHint{
			Kind: ast.NontermTypeHint_LINTERFACE,
			Name:  nn1,
			Implements:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 78:  // nonterm_type ::= Lvoid
		node.value = &ast.NontermTypeHint{
			Kind: ast.NontermTypeHint_LVOID,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 79:  // nonterm_type ::= type
		nn0, _ := rhs[0].value.(string)
node.value = &ast.NontermTypeRaw{
			TypeText:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 80:  // implements ::= ':' references_cs
		nn1, _ := rhs[1].value.([]*ast.Symref)
node.value =  nn1
	case 81:  // assoc ::= Lleft
		node.value = ast.Assoc_LLEFT
	case 82:  // assoc ::= Lright
		node.value = ast.Assoc_LRIGHT
	case 83:  // assoc ::= Lnonassoc
		node.value = ast.Assoc_LNONASSOC
	case 84:  // param_modifier ::= Lexplicit
		node.value = ast.ParamModifier_LEXPLICIT
	case 85:  // param_modifier ::= Lglobal
		node.value = ast.ParamModifier_LGLOBAL
	case 86:  // param_modifier ::= Llookahead
		node.value = ast.ParamModifier_LLOOKAHEAD
	case 87:  // template_param ::= '%' param_modifier param_type identifier '=' param_value ';'
		nn1, _ := rhs[1].value.(ast.ParamModifier)
nn2, _ := rhs[2].value.(ast.ParamType)
nn3, _ := rhs[3].value.(*ast.Identifier)
nn5, _ := rhs[5].value.(ast.ParamValue)
node.value = &ast.TemplateParam{
			Modifier:  nn1,
			ParamType:  nn2,
			Name:  nn3,
			ParamValue:  nn5,
			Pos: ast.Pos{rhs[0].offset, rhs[6].endoffset},
}
	case 88:  // template_param ::= '%' param_modifier param_type identifier ';'
		nn1, _ := rhs[1].value.(ast.ParamModifier)
nn2, _ := rhs[2].value.(ast.ParamType)
nn3, _ := rhs[3].value.(*ast.Identifier)
node.value = &ast.TemplateParam{
			Modifier:  nn1,
			ParamType:  nn2,
			Name:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 89:  // template_param ::= '%' param_type identifier '=' param_value ';'
		nn1, _ := rhs[1].value.(ast.ParamType)
nn2, _ := rhs[2].value.(*ast.Identifier)
nn4, _ := rhs[4].value.(ast.ParamValue)
node.value = &ast.TemplateParam{
			ParamType:  nn1,
			Name:  nn2,
			ParamValue:  nn4,
			Pos: ast.Pos{rhs[0].offset, rhs[5].endoffset},
}
	case 90:  // template_param ::= '%' param_type identifier ';'
		nn1, _ := rhs[1].value.(ast.ParamType)
nn2, _ := rhs[2].value.(*ast.Identifier)
node.value = &ast.TemplateParam{
			ParamType:  nn1,
			Name:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 91:  // directive ::= '%' assoc references ';'
		nn1, _ := rhs[1].value.(ast.Assoc)
nn2, _ := rhs[2].value.([]*ast.Symref)
node.value = &ast.DirectivePrio{
			Assoc:  nn1,
			Symbols:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 92:  // directive ::= '%' Linput inputref_list_Comma_separated ';'
		nn2, _ := rhs[2].value.([]*ast.Inputref)
node.value = &ast.DirectiveInput{
			InputRefs:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 93:  // directive ::= '%' Lassert Lempty rhsSet ';'
		nn3, _ := rhs[3].value.(*ast.RhsSet)
node.value = &ast.DirectiveAssert{
			Kind: ast.DirectiveAssert_LEMPTY,
			RhsSet:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 94:  // directive ::= '%' Lassert Lnonempty rhsSet ';'
		nn3, _ := rhs[3].value.(*ast.RhsSet)
node.value = &ast.DirectiveAssert{
			Kind: ast.DirectiveAssert_LNONEMPTY,
			RhsSet:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 95:  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
		nn2, _ := rhs[2].value.(string)
nn4, _ := rhs[4].value.(*ast.RhsSet)
node.value = &ast.DirectiveSet{
			Name:  nn2,
			RhsSet:  nn4,
			Pos: ast.Pos{rhs[0].offset, rhs[5].endoffset},
}
	case 96:  // inputref_list_Comma_separated ::= inputref_list_Comma_separated ',' inputref
		nn0, _ := rhs[0].value.([]*ast.Inputref)
nn2, _ := rhs[2].value.(*ast.Inputref)
node.value = append( nn0,  nn2)
	case 97:  // inputref_list_Comma_separated ::= inputref
		nn0, _ := rhs[0].value.(*ast.Inputref)
node.value = []*ast.Inputref{ nn0}
	case 98:  // inputref ::= symref_noargs Lnoeoi
		nn0, _ := rhs[0].value.(*ast.Symref)
node.value = &ast.Inputref{
			Reference:  nn0,
			Noeoi: true,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 99:  // inputref ::= symref_noargs
		nn0, _ := rhs[0].value.(*ast.Symref)
node.value = &ast.Inputref{
			Reference:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 100:  // references ::= symref_noargs
		nn0, _ := rhs[0].value.(*ast.Symref)
node.value = []*ast.Symref{ nn0}
	case 101:  // references ::= references symref_noargs
		nn0, _ := rhs[0].value.([]*ast.Symref)
nn1, _ := rhs[1].value.(*ast.Symref)
node.value = append( nn0,  nn1)
	case 102:  // references_cs ::= symref_noargs
		nn0, _ := rhs[0].value.(*ast.Symref)
node.value = []*ast.Symref{ nn0}
	case 103:  // references_cs ::= references_cs ',' symref_noargs
		nn0, _ := rhs[0].value.([]*ast.Symref)
nn2, _ := rhs[2].value.(*ast.Symref)
node.value = append( nn0,  nn2)
	case 104:  // rule0_list_Or_separated ::= rule0_list_Or_separated '|' rule0
		nn0, _ := rhs[0].value.([]*ast.Rule0)
nn2, _ := rhs[2].value.(*ast.Rule0)
node.value = append( nn0,  nn2)
	case 105:  // rule0_list_Or_separated ::= rule0
		nn0, _ := rhs[0].value.(*ast.Rule0)
node.value = []*ast.Rule0{ nn0}
	case 107:  // rule0 ::= predicate rhsPrefix rhsParts ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.(*ast.RhsPrefix)
nn2, _ := rhs[2].value.([]ast.RhsPart)
nn3, _ := rhs[3].value.(*ast.RuleAction)
nn4, _ := rhs[4].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			Prefix:  nn1,
			List:  nn2,
			Action:  nn3,
			Suffix:  nn4,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 108:  // rule0 ::= predicate rhsPrefix rhsParts rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.(*ast.RhsPrefix)
nn2, _ := rhs[2].value.([]ast.RhsPart)
nn3, _ := rhs[3].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			Prefix:  nn1,
			List:  nn2,
			Suffix:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 109:  // rule0 ::= predicate rhsPrefix ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.(*ast.RhsPrefix)
nn2, _ := rhs[2].value.(*ast.RuleAction)
nn3, _ := rhs[3].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			Prefix:  nn1,
			Action:  nn2,
			Suffix:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 110:  // rule0 ::= predicate rhsPrefix rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.(*ast.RhsPrefix)
nn2, _ := rhs[2].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			Prefix:  nn1,
			Suffix:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 111:  // rule0 ::= predicate rhsParts ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.([]ast.RhsPart)
nn2, _ := rhs[2].value.(*ast.RuleAction)
nn3, _ := rhs[3].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			List:  nn1,
			Action:  nn2,
			Suffix:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 112:  // rule0 ::= predicate rhsParts rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.([]ast.RhsPart)
nn2, _ := rhs[2].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			List:  nn1,
			Suffix:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 113:  // rule0 ::= predicate ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.(*ast.RuleAction)
nn2, _ := rhs[2].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			Action:  nn1,
			Suffix:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 114:  // rule0 ::= predicate rhsSuffixopt
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn1, _ := rhs[1].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Predicate:  nn0,
			Suffix:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 115:  // rule0 ::= rhsPrefix rhsParts ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.(*ast.RhsPrefix)
nn1, _ := rhs[1].value.([]ast.RhsPart)
nn2, _ := rhs[2].value.(*ast.RuleAction)
nn3, _ := rhs[3].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Prefix:  nn0,
			List:  nn1,
			Action:  nn2,
			Suffix:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 116:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
		nn0, _ := rhs[0].value.(*ast.RhsPrefix)
nn1, _ := rhs[1].value.([]ast.RhsPart)
nn2, _ := rhs[2].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Prefix:  nn0,
			List:  nn1,
			Suffix:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 117:  // rule0 ::= rhsPrefix ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.(*ast.RhsPrefix)
nn1, _ := rhs[1].value.(*ast.RuleAction)
nn2, _ := rhs[2].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Prefix:  nn0,
			Action:  nn1,
			Suffix:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 118:  // rule0 ::= rhsPrefix rhsSuffixopt
		nn0, _ := rhs[0].value.(*ast.RhsPrefix)
nn1, _ := rhs[1].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Prefix:  nn0,
			Suffix:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 119:  // rule0 ::= rhsParts ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.([]ast.RhsPart)
nn1, _ := rhs[1].value.(*ast.RuleAction)
nn2, _ := rhs[2].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			List:  nn0,
			Action:  nn1,
			Suffix:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 120:  // rule0 ::= rhsParts rhsSuffixopt
		nn0, _ := rhs[0].value.([]ast.RhsPart)
nn1, _ := rhs[1].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			List:  nn0,
			Suffix:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 121:  // rule0 ::= ruleAction rhsSuffixopt
		nn0, _ := rhs[0].value.(*ast.RuleAction)
nn1, _ := rhs[1].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Action:  nn0,
			Suffix:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 122:  // rule0 ::= rhsSuffixopt
		nn0, _ := rhs[0].value.(*ast.RhsSuffix)
node.value = &ast.Rule0{
			Suffix:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 123:  // rule0 ::= syntax_problem
		nn0, _ := rhs[0].value.(*ast.SyntaxProblem)
node.value = &ast.Rule0{
			Error:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 124:  // predicate ::= '[' predicate_expression ']'
		nn1, _ := rhs[1].value.(ast.PredicateExpression)
node.value =  nn1
	case 125:  // rhsPrefix ::= annotations ':'
		nn0, _ := rhs[0].value.(*ast.Annotations)
node.value = &ast.RhsPrefix{
			Annotations:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 126:  // rhsSuffix ::= '%' Lprec symref_noargs
		nn2, _ := rhs[2].value.(*ast.Symref)
node.value = &ast.RhsSuffix{
			Kind: ast.RhsSuffix_LPREC,
			Symref:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 127:  // rhsSuffix ::= '%' Lshift symref_noargs
		nn2, _ := rhs[2].value.(*ast.Symref)
node.value = &ast.RhsSuffix{
			Kind: ast.RhsSuffix_LSHIFT,
			Symref:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 128:  // ruleAction ::= '{~' identifier scon '}'
		nn1, _ := rhs[1].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(string)
node.value = &ast.RuleAction{
			Action:  nn1,
			Parameter:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 129:  // ruleAction ::= '{~' identifier '}'
		nn1, _ := rhs[1].value.(*ast.Identifier)
node.value = &ast.RuleAction{
			Action:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 130:  // rhsParts ::= rhsPart
		nn0, _ := rhs[0].value.(ast.RhsPart)
node.value = []ast.RhsPart{ nn0}
	case 131:  // rhsParts ::= rhsParts rhsPart
		nn0, _ := rhs[0].value.([]ast.RhsPart)
nn1, _ := rhs[1].value.(ast.RhsPart)
node.value = append( nn0,  nn1)
	case 132:  // rhsParts ::= rhsParts syntax_problem
		nn0, _ := rhs[0].value.([]ast.RhsPart)
nn1, _ := rhs[1].value.(*ast.SyntaxProblem)
node.value = append( nn0,  nn1)
	case 137:  // rhsAnnotated ::= annotations rhsAssignment
		nn0, _ := rhs[0].value.(*ast.Annotations)
nn1, _ := rhs[1].value.(ast.RhsPart)
node.value = &ast.RhsAnnotated{
			Annotations:  nn0,
			Inner:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 139:  // rhsAssignment ::= identifier '=' rhsOptional
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(ast.RhsPart)
node.value = &ast.RhsAssignment{
			Id:  nn0,
			Inner:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 140:  // rhsAssignment ::= identifier '+=' rhsOptional
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(ast.RhsPart)
node.value = &ast.RhsAssignment{
			Id:  nn0,
			Addition: true,
			Inner:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 142:  // rhsOptional ::= rhsCast '?'
		nn0, _ := rhs[0].value.(ast.RhsPart)
node.value = &ast.RhsQuantifier{
			Inner:  nn0,
			Quantifier: ast.RhsQuantifier_QUEST,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 144:  // rhsCast ::= rhsClass Las symref
		nn0, _ := rhs[0].value.(ast.RhsPart)
nn2, _ := rhs[2].value.(*ast.Symref)
node.value = &ast.RhsCast{
			Inner:  nn0,
			Target:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 145:  // rhsCast ::= rhsClass Las literal
		nn0, _ := rhs[0].value.(ast.RhsPart)
nn2, _ := rhs[2].value.(*ast.Literal)
node.value = &ast.RhsAsLiteral{
			Inner:  nn0,
			Literal:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 146:  // rhsUnordered ::= rhsPart '&' rhsPart
		nn0, _ := rhs[0].value.(ast.RhsPart)
nn2, _ := rhs[2].value.(ast.RhsPart)
node.value = &ast.RhsUnordered{
			Left:  nn0,
			Right:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 148:  // rhsClass ::= identifier ':' rhsPrimary
		nn0, _ := rhs[0].value.(*ast.Identifier)
nn2, _ := rhs[2].value.(ast.RhsPart)
node.value = &ast.RhsClass{
			Identifier:  nn0,
			Inner:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 149:  // rhsPrimary ::= symref
		nn0, _ := rhs[0].value.(*ast.Symref)
node.value = &ast.RhsSymbol{
			Reference:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 150:  // rhsPrimary ::= '(' rules ')'
		nn1, _ := rhs[1].value.([]*ast.Rule0)
node.value = &ast.RhsNested{
			Rules:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 151:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		nn1, _ := rhs[1].value.([]ast.RhsPart)
nn3, _ := rhs[3].value.([]*ast.Symref)
node.value = &ast.RhsList{
			RuleParts:  nn1,
			Separator:  nn3,
			AtLeastOne: true,
			Pos: ast.Pos{rhs[0].offset, rhs[5].endoffset},
}
	case 152:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		nn1, _ := rhs[1].value.([]ast.RhsPart)
nn3, _ := rhs[3].value.([]*ast.Symref)
node.value = &ast.RhsList{
			RuleParts:  nn1,
			Separator:  nn3,
			AtLeastOne: false,
			Pos: ast.Pos{rhs[0].offset, rhs[5].endoffset},
}
	case 153:  // rhsPrimary ::= rhsPrimary '*'
		nn0, _ := rhs[0].value.(ast.RhsPart)
node.value = &ast.RhsQuantifier{
			Inner:  nn0,
			Quantifier: ast.RhsQuantifier_MULT,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 154:  // rhsPrimary ::= rhsPrimary '+'
		nn0, _ := rhs[0].value.(ast.RhsPart)
node.value = &ast.RhsQuantifier{
			Inner:  nn0,
			Quantifier: ast.RhsQuantifier_PLUS,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 155:  // rhsPrimary ::= '$' '(' rules ')'
		nn2, _ := rhs[2].value.([]*ast.Rule0)
node.value = &ast.RhsIgnored{
			Rules:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 157:  // rhsSet ::= Lset '(' setExpression ')'
		nn2, _ := rhs[2].value.(ast.SetExpression)
node.value = &ast.RhsSet{
			Expr:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 158:  // setPrimary ::= ID symref
		nn0, _ := rhs[0].value.(string)
nn1, _ := rhs[1].value.(*ast.Symref)
node.value = &ast.SetSymbol{
			Operator:  nn0,
			Symbol:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 159:  // setPrimary ::= symref
		nn0, _ := rhs[0].value.(*ast.Symref)
node.value = &ast.SetSymbol{
			Symbol:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 160:  // setPrimary ::= '(' setExpression ')'
		nn1, _ := rhs[1].value.(ast.SetExpression)
node.value = &ast.SetCompound{
			Inner:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 161:  // setPrimary ::= '~' setPrimary
		nn1, _ := rhs[1].value.(ast.SetExpression)
node.value = &ast.SetComplement{
			Inner:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 163:  // setExpression ::= setExpression '|' setExpression
		nn0, _ := rhs[0].value.(ast.SetExpression)
nn2, _ := rhs[2].value.(ast.SetExpression)
node.value = &ast.SetBinary{
			Left:  nn0,
			Kind: ast.SetBinary_OR,
			Right:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 164:  // setExpression ::= setExpression '&' setExpression
		nn0, _ := rhs[0].value.(ast.SetExpression)
nn2, _ := rhs[2].value.(ast.SetExpression)
node.value = &ast.SetBinary{
			Left:  nn0,
			Kind: ast.SetBinary_AND,
			Right:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 165:  // annotation_list ::= annotation_list annotation
		nn0, _ := rhs[0].value.([]*ast.Annotation)
nn1, _ := rhs[1].value.(*ast.Annotation)
node.value = append( nn0,  nn1)
	case 166:  // annotation_list ::= annotation
		nn0, _ := rhs[0].value.(*ast.Annotation)
node.value = []*ast.Annotation{ nn0}
	case 167:  // annotations ::= annotation_list
		nn0, _ := rhs[0].value.([]*ast.Annotation)
node.value = &ast.Annotations{
			Annotations:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 168:  // annotation ::= '@' ID '{' expression '}'
		nn1, _ := rhs[1].value.(string)
nn3, _ := rhs[3].value.(ast.Expression)
node.value = &ast.Annotation{
			Name:  nn1,
			Expression:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 169:  // annotation ::= '@' ID
		nn1, _ := rhs[1].value.(string)
node.value = &ast.Annotation{
			Name:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 170:  // annotation ::= '@' syntax_problem
		nn1, _ := rhs[1].value.(*ast.SyntaxProblem)
node.value = &ast.Annotation{
			SyntaxProblem:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 171:  // nonterm_param_list_Comma_separated ::= nonterm_param_list_Comma_separated ',' nonterm_param
		nn0, _ := rhs[0].value.([]ast.NontermParam)
nn2, _ := rhs[2].value.(ast.NontermParam)
node.value = append( nn0,  nn2)
	case 172:  // nonterm_param_list_Comma_separated ::= nonterm_param
		nn0, _ := rhs[0].value.(ast.NontermParam)
node.value = []ast.NontermParam{ nn0}
	case 173:  // nonterm_params ::= '<' nonterm_param_list_Comma_separated '>'
		nn1, _ := rhs[1].value.([]ast.NontermParam)
node.value = &ast.NontermParams{
			List:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 175:  // nonterm_param ::= ID identifier '=' param_value
		nn0, _ := rhs[0].value.(string)
nn1, _ := rhs[1].value.(*ast.Identifier)
nn3, _ := rhs[3].value.(ast.ParamValue)
node.value = &ast.InlineParameter{
			ParamType:  nn0,
			Name:  nn1,
			ParamValue:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[3].endoffset},
}
	case 176:  // nonterm_param ::= ID identifier
		nn0, _ := rhs[0].value.(string)
nn1, _ := rhs[1].value.(*ast.Identifier)
node.value = &ast.InlineParameter{
			ParamType:  nn0,
			Name:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 177:  // param_ref ::= identifier
		nn0, _ := rhs[0].value.(*ast.Identifier)
node.value = &ast.ParamRef{
			Ref:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 178:  // argument_list_Comma_separated ::= argument_list_Comma_separated ',' argument
		nn0, _ := rhs[0].value.([]*ast.Argument)
nn2, _ := rhs[2].value.(*ast.Argument)
node.value = append( nn0,  nn2)
	case 179:  // argument_list_Comma_separated ::= argument
		nn0, _ := rhs[0].value.(*ast.Argument)
node.value = []*ast.Argument{ nn0}
	case 182:  // symref_args ::= '<' argument_list_Comma_separated_opt '>'
		nn1, _ := rhs[1].value.([]*ast.Argument)
node.value = &ast.SymrefArgs{
			ArgList:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 183:  // argument ::= param_ref ':' param_value
		nn0, _ := rhs[0].value.(*ast.ParamRef)
nn2, _ := rhs[2].value.(ast.ParamValue)
node.value = &ast.Argument{
			Name:  nn0,
			Val:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 184:  // argument ::= '+' param_ref
		nn1, _ := rhs[1].value.(*ast.ParamRef)
node.value = &ast.Argument{
			Name:  nn1,
			Bool: ast.Argument_PLUS,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 185:  // argument ::= '~' param_ref
		nn1, _ := rhs[1].value.(*ast.ParamRef)
node.value = &ast.Argument{
			Name:  nn1,
			Bool: ast.Argument_TILDE,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 186:  // argument ::= param_ref
		nn0, _ := rhs[0].value.(*ast.ParamRef)
node.value = &ast.Argument{
			Name:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 187:  // param_type ::= Lflag
		node.value = ast.ParamType_LFLAG
	case 188:  // param_type ::= Lparam
		node.value = ast.ParamType_LPARAM
	case 191:  // predicate_primary ::= '!' param_ref
		nn1, _ := rhs[1].value.(*ast.ParamRef)
node.value = &ast.BoolPredicate{
			Negated: true,
			ParamRef:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[1].endoffset},
}
	case 192:  // predicate_primary ::= param_ref
		nn0, _ := rhs[0].value.(*ast.ParamRef)
node.value = &ast.BoolPredicate{
			ParamRef:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 193:  // predicate_primary ::= param_ref '==' literal
		nn0, _ := rhs[0].value.(*ast.ParamRef)
nn2, _ := rhs[2].value.(*ast.Literal)
node.value = &ast.ComparePredicate{
			ParamRef:  nn0,
			Kind: ast.ComparePredicate_ASSIGNASSIGN,
			Literal:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 194:  // predicate_primary ::= param_ref '!=' literal
		nn0, _ := rhs[0].value.(*ast.ParamRef)
nn2, _ := rhs[2].value.(*ast.Literal)
node.value = &ast.ComparePredicate{
			ParamRef:  nn0,
			Kind: ast.ComparePredicate_EXCLASSIGN,
			Literal:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 196:  // predicate_expression ::= predicate_expression '&&' predicate_expression
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn2, _ := rhs[2].value.(ast.PredicateExpression)
node.value = &ast.PredicateBinary{
			Left:  nn0,
			Kind: ast.PredicateBinary_ANDAND,
			Right:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 197:  // predicate_expression ::= predicate_expression '||' predicate_expression
		nn0, _ := rhs[0].value.(ast.PredicateExpression)
nn2, _ := rhs[2].value.(ast.PredicateExpression)
node.value = &ast.PredicateBinary{
			Left:  nn0,
			Kind: ast.PredicateBinary_OROR,
			Right:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 200:  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
		nn1, _ := rhs[1].value.(*ast.Name)
nn3, _ := rhs[3].value.([]*ast.MapEntry)
node.value = &ast.Instance{
			ClassName:  nn1,
			Entries:  nn3,
			Pos: ast.Pos{rhs[0].offset, rhs[4].endoffset},
}
	case 201:  // expression ::= '[' expression_list_Comma_separated_opt ']'
		nn1, _ := rhs[1].value.([]ast.Expression)
node.value = &ast.Array{
			Content:  nn1,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 203:  // expression_list_Comma_separated ::= expression_list_Comma_separated ',' expression
		nn0, _ := rhs[0].value.([]ast.Expression)
nn2, _ := rhs[2].value.(ast.Expression)
node.value = append( nn0,  nn2)
	case 204:  // expression_list_Comma_separated ::= expression
		nn0, _ := rhs[0].value.(ast.Expression)
node.value = []ast.Expression{ nn0}
	case 207:  // map_entry_list_Comma_separated ::= map_entry_list_Comma_separated ',' map_entry
		nn0, _ := rhs[0].value.([]*ast.MapEntry)
nn2, _ := rhs[2].value.(*ast.MapEntry)
node.value = append( nn0,  nn2)
	case 208:  // map_entry_list_Comma_separated ::= map_entry
		nn0, _ := rhs[0].value.(*ast.MapEntry)
node.value = []*ast.MapEntry{ nn0}
	case 211:  // map_entry ::= ID ':' expression
		nn0, _ := rhs[0].value.(string)
nn2, _ := rhs[2].value.(ast.Expression)
node.value = &ast.MapEntry{
			Name:  nn0,
			Value:  nn2,
			Pos: ast.Pos{rhs[0].offset, rhs[2].endoffset},
}
	case 212:  // literal ::= scon
		nn0, _ := rhs[0].value.(string)
node.value = &ast.Literal{
			Value:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 213:  // literal ::= icon
		nn0, _ := rhs[0].value.(int)
node.value = &ast.Literal{
			Value:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 214:  // literal ::= Ltrue
		node.value = &ast.Literal{
			Value: true,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 215:  // literal ::= Lfalse
		node.value = &ast.Literal{
			Value: false,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 216:  // name ::= qualified_id
		nn0, _ := rhs[0].value.(string)
node.value = &ast.Name{
			QualifiedId:  nn0,
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 217:  // qualified_id ::= ID
		nn0, _ := rhs[0].value.(string)
{ node.value =  nn0; }
	case 218:  // qualified_id ::= qualified_id '.' ID
		nn0, _ := rhs[0].value.(string)
nn2, _ := rhs[2].value.(string)
{ node.value =  nn0 + "." +  nn2; }
	case 219:  // command ::= code
		node.value = &ast.Command{
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	case 220:  // syntax_problem ::= error
		node.value = &ast.SyntaxProblem{
			Pos: ast.Pos{rhs[0].offset, rhs[0].endoffset},
}
	}
}
