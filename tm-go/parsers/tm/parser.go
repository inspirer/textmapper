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
		node.value = &ast.Input{}
	case 3:  // input ::= header import__optlist option_optlist lexer_section
		node.value = &ast.Input{}
	case 4:  // option_optlist ::= option_optlist option
		nn0, _ := rhs[0].value.([]*ast.Option)
nn1, _ := rhs[1].value.(*ast.Option)
node.value = append( nn0,  nn1)
	case 5:  // option_optlist ::=
		node.value = []*ast.Option{}
	case 6:  // header ::= Llanguage name '(' name ')' parsing_algorithmopt ';'
		node.value = &ast.Header{}
	case 7:  // header ::= Llanguage name parsing_algorithmopt ';'
		node.value = &ast.Header{}
	case 8:  // lexer_section ::= '::' Llexer lexer_parts
		nn2, _ := rhs[2].value.([]ast.LexerPart)
node.value =  nn2
	case 9:  // parser_section ::= '::' Lparser grammar_parts
		nn2, _ := rhs[2].value.([]ast.GrammarPart)
node.value =  nn2
	case 10:  // parsing_algorithm ::= Llalr '(' icon ')'
		node.value = &ast.ParsingAlgorithm{}
	case 11:  // import_ ::= Limport ID scon ';'
		node.value = &ast.Import{}
	case 12:  // import_ ::= Limport scon ';'
		node.value = &ast.Import{}
	case 13:  // option ::= ID '=' expression
		node.value = &ast.Option{}
	case 14:  // option ::= syntax_problem
		node.value = &ast.Option{}
	case 15:  // identifier ::= ID
		node.value = &ast.Identifier{}
	case 16:  // symref ::= ID symref_args
		node.value = &ast.Symref{}
	case 17:  // symref ::= ID
		node.value = &ast.Symref{}
	case 18:  // symref_noargs ::= ID
		node.value = &ast.Symref{}
	case 19:  // type ::= '(' scon ')'
		nn1, _ := rhs[1].value.(string)
{ node.value =  nn1; }
	case 20:  // type ::= '(' type_part_list ')'
		{ node.value = "TODO" }
	case 36:  // pattern ::= regexp
		node.value = &ast.Pattern{}
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
		node.value = &ast.NamedPattern{}
	case 45:  // lexeme ::= identifier typeopt ':' pattern lexeme_transitionopt iconopt lexeme_attrsopt commandopt
		node.value = &ast.Lexeme{}
	case 46:  // lexeme ::= identifier typeopt ':'
		node.value = &ast.Lexeme{}
	case 47:  // lexeme_transition ::= '=>' stateref
		nn1, _ := rhs[1].value.(*ast.Stateref)
node.value =  nn1
	case 48:  // lexeme_attrs ::= '(' lexeme_attribute ')'
		node.value = &ast.LexemeAttrs{}
	case 49:  // lexeme_attribute ::= Lsoft
		node.value = ast.LexemeAttribute_LSOFT
	case 50:  // lexeme_attribute ::= Lclass
		node.value = ast.LexemeAttribute_LCLASS
	case 51:  // lexeme_attribute ::= Lspace
		node.value = ast.LexemeAttribute_LSPACE
	case 52:  // lexeme_attribute ::= Llayout
		node.value = ast.LexemeAttribute_LLAYOUT
	case 53:  // lexer_directive ::= '%' Lbrackets symref_noargs symref_noargs ';'
		node.value = &ast.DirectiveBrackets{}
	case 54:  // lexer_state_list_Comma_separated ::= lexer_state_list_Comma_separated ',' lexer_state
		nn0, _ := rhs[0].value.([]*ast.LexerState)
nn2, _ := rhs[2].value.(*ast.LexerState)
node.value = append( nn0,  nn2)
	case 55:  // lexer_state_list_Comma_separated ::= lexer_state
		nn0, _ := rhs[0].value.(*ast.LexerState)
node.value = []*ast.LexerState{ nn0}
	case 56:  // state_selector ::= '[' lexer_state_list_Comma_separated ']'
		node.value = &ast.StateSelector{}
	case 57:  // stateref ::= ID
		node.value = &ast.Stateref{}
	case 58:  // lexer_state ::= identifier '=>' stateref
		node.value = &ast.LexerState{}
	case 59:  // lexer_state ::= identifier
		node.value = &ast.LexerState{}
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
		node.value = &ast.Nonterm{}
	case 67:  // nonterm ::= annotations identifier nonterm_params '::=' rules ';'
		node.value = &ast.Nonterm{}
	case 68:  // nonterm ::= annotations identifier nonterm_type '::=' rules ';'
		node.value = &ast.Nonterm{}
	case 69:  // nonterm ::= annotations identifier '::=' rules ';'
		node.value = &ast.Nonterm{}
	case 70:  // nonterm ::= identifier nonterm_params nonterm_type '::=' rules ';'
		node.value = &ast.Nonterm{}
	case 71:  // nonterm ::= identifier nonterm_params '::=' rules ';'
		node.value = &ast.Nonterm{}
	case 72:  // nonterm ::= identifier nonterm_type '::=' rules ';'
		node.value = &ast.Nonterm{}
	case 73:  // nonterm ::= identifier '::=' rules ';'
		node.value = &ast.Nonterm{}
	case 74:  // nonterm_type ::= Lreturns symref_noargs
		node.value = &ast.NontermTypeAST{}
	case 75:  // nonterm_type ::= Linline Lclass identifieropt implementsopt
		node.value = &ast.NontermTypeHint{}
	case 76:  // nonterm_type ::= Lclass identifieropt implementsopt
		node.value = &ast.NontermTypeHint{}
	case 77:  // nonterm_type ::= Linterface identifieropt implementsopt
		node.value = &ast.NontermTypeHint{}
	case 78:  // nonterm_type ::= Lvoid
		node.value = &ast.NontermTypeHint{}
	case 79:  // nonterm_type ::= type
		node.value = &ast.NontermTypeRaw{}
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
		node.value = &ast.TemplateParam{}
	case 88:  // template_param ::= '%' param_modifier param_type identifier ';'
		node.value = &ast.TemplateParam{}
	case 89:  // template_param ::= '%' param_type identifier '=' param_value ';'
		node.value = &ast.TemplateParam{}
	case 90:  // template_param ::= '%' param_type identifier ';'
		node.value = &ast.TemplateParam{}
	case 91:  // directive ::= '%' assoc references ';'
		node.value = &ast.DirectivePrio{}
	case 92:  // directive ::= '%' Linput inputref_list_Comma_separated ';'
		node.value = &ast.DirectiveInput{}
	case 93:  // directive ::= '%' Lassert Lempty rhsSet ';'
		node.value = &ast.DirectiveAssert{}
	case 94:  // directive ::= '%' Lassert Lnonempty rhsSet ';'
		node.value = &ast.DirectiveAssert{}
	case 95:  // directive ::= '%' Lgenerate ID '=' rhsSet ';'
		node.value = &ast.DirectiveSet{}
	case 96:  // inputref_list_Comma_separated ::= inputref_list_Comma_separated ',' inputref
		nn0, _ := rhs[0].value.([]*ast.Inputref)
nn2, _ := rhs[2].value.(*ast.Inputref)
node.value = append( nn0,  nn2)
	case 97:  // inputref_list_Comma_separated ::= inputref
		nn0, _ := rhs[0].value.(*ast.Inputref)
node.value = []*ast.Inputref{ nn0}
	case 98:  // inputref ::= symref_noargs Lnoeoi
		node.value = &ast.Inputref{}
	case 99:  // inputref ::= symref_noargs
		node.value = &ast.Inputref{}
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
		node.value = &ast.Rule0{}
	case 108:  // rule0 ::= predicate rhsPrefix rhsParts rhsSuffixopt
		node.value = &ast.Rule0{}
	case 109:  // rule0 ::= predicate rhsPrefix ruleAction rhsSuffixopt
		node.value = &ast.Rule0{}
	case 110:  // rule0 ::= predicate rhsPrefix rhsSuffixopt
		node.value = &ast.Rule0{}
	case 111:  // rule0 ::= predicate rhsParts ruleAction rhsSuffixopt
		node.value = &ast.Rule0{}
	case 112:  // rule0 ::= predicate rhsParts rhsSuffixopt
		node.value = &ast.Rule0{}
	case 113:  // rule0 ::= predicate ruleAction rhsSuffixopt
		node.value = &ast.Rule0{}
	case 114:  // rule0 ::= predicate rhsSuffixopt
		node.value = &ast.Rule0{}
	case 115:  // rule0 ::= rhsPrefix rhsParts ruleAction rhsSuffixopt
		node.value = &ast.Rule0{}
	case 116:  // rule0 ::= rhsPrefix rhsParts rhsSuffixopt
		node.value = &ast.Rule0{}
	case 117:  // rule0 ::= rhsPrefix ruleAction rhsSuffixopt
		node.value = &ast.Rule0{}
	case 118:  // rule0 ::= rhsPrefix rhsSuffixopt
		node.value = &ast.Rule0{}
	case 119:  // rule0 ::= rhsParts ruleAction rhsSuffixopt
		node.value = &ast.Rule0{}
	case 120:  // rule0 ::= rhsParts rhsSuffixopt
		node.value = &ast.Rule0{}
	case 121:  // rule0 ::= ruleAction rhsSuffixopt
		node.value = &ast.Rule0{}
	case 122:  // rule0 ::= rhsSuffixopt
		node.value = &ast.Rule0{}
	case 123:  // rule0 ::= syntax_problem
		node.value = &ast.Rule0{}
	case 124:  // predicate ::= '[' predicate_expression ']'
		nn1, _ := rhs[1].value.(ast.PredicateExpression)
node.value =  nn1
	case 125:  // rhsPrefix ::= annotations ':'
		node.value = &ast.RhsPrefix{}
	case 126:  // rhsSuffix ::= '%' Lprec symref_noargs
		node.value = &ast.RhsSuffix{}
	case 127:  // rhsSuffix ::= '%' Lshift symref_noargs
		node.value = &ast.RhsSuffix{}
	case 128:  // ruleAction ::= '{~' identifier scon '}'
		node.value = &ast.RuleAction{}
	case 129:  // ruleAction ::= '{~' identifier '}'
		node.value = &ast.RuleAction{}
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
		node.value = &ast.RhsAnnotated{}
	case 139:  // rhsAssignment ::= identifier '=' rhsOptional
		node.value = &ast.RhsAssignment{}
	case 140:  // rhsAssignment ::= identifier '+=' rhsOptional
		node.value = &ast.RhsAssignment{}
	case 142:  // rhsOptional ::= rhsCast '?'
		node.value = &ast.RhsQuantifier{}
	case 144:  // rhsCast ::= rhsClass Las symref
		node.value = &ast.RhsCast{}
	case 145:  // rhsCast ::= rhsClass Las literal
		node.value = &ast.RhsAsLiteral{}
	case 146:  // rhsUnordered ::= rhsPart '&' rhsPart
		node.value = &ast.RhsUnordered{}
	case 148:  // rhsClass ::= identifier ':' rhsPrimary
		node.value = &ast.RhsClass{}
	case 149:  // rhsPrimary ::= symref
		node.value = &ast.RhsSymbol{}
	case 150:  // rhsPrimary ::= '(' rules ')'
		node.value = &ast.RhsNested{}
	case 151:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '+'
		node.value = &ast.RhsList{}
	case 152:  // rhsPrimary ::= '(' rhsParts Lseparator references ')' '*'
		node.value = &ast.RhsList{}
	case 153:  // rhsPrimary ::= rhsPrimary '*'
		node.value = &ast.RhsQuantifier{}
	case 154:  // rhsPrimary ::= rhsPrimary '+'
		node.value = &ast.RhsQuantifier{}
	case 155:  // rhsPrimary ::= '$' '(' rules ')'
		node.value = &ast.RhsIgnored{}
	case 157:  // rhsSet ::= Lset '(' setExpression ')'
		node.value = &ast.RhsSet{}
	case 158:  // setPrimary ::= ID symref
		node.value = &ast.SetSymbol{}
	case 159:  // setPrimary ::= symref
		node.value = &ast.SetSymbol{}
	case 160:  // setPrimary ::= '(' setExpression ')'
		node.value = &ast.SetCompound{}
	case 161:  // setPrimary ::= '~' setPrimary
		node.value = &ast.SetComplement{}
	case 163:  // setExpression ::= setExpression '|' setExpression
		node.value = &ast.SetBinary{}
	case 164:  // setExpression ::= setExpression '&' setExpression
		node.value = &ast.SetBinary{}
	case 165:  // annotation_list ::= annotation_list annotation
		nn0, _ := rhs[0].value.([]*ast.Annotation)
nn1, _ := rhs[1].value.(*ast.Annotation)
node.value = append( nn0,  nn1)
	case 166:  // annotation_list ::= annotation
		nn0, _ := rhs[0].value.(*ast.Annotation)
node.value = []*ast.Annotation{ nn0}
	case 167:  // annotations ::= annotation_list
		node.value = &ast.Annotations{}
	case 168:  // annotation ::= '@' ID '{' expression '}'
		node.value = &ast.Annotation{}
	case 169:  // annotation ::= '@' ID
		node.value = &ast.Annotation{}
	case 170:  // annotation ::= '@' syntax_problem
		node.value = &ast.Annotation{}
	case 171:  // nonterm_param_list_Comma_separated ::= nonterm_param_list_Comma_separated ',' nonterm_param
		nn0, _ := rhs[0].value.([]ast.NontermParam)
nn2, _ := rhs[2].value.(ast.NontermParam)
node.value = append( nn0,  nn2)
	case 172:  // nonterm_param_list_Comma_separated ::= nonterm_param
		nn0, _ := rhs[0].value.(ast.NontermParam)
node.value = []ast.NontermParam{ nn0}
	case 173:  // nonterm_params ::= '<' nonterm_param_list_Comma_separated '>'
		node.value = &ast.NontermParams{}
	case 175:  // nonterm_param ::= ID identifier '=' param_value
		node.value = &ast.InlineParameter{}
	case 176:  // nonterm_param ::= ID identifier
		node.value = &ast.InlineParameter{}
	case 177:  // param_ref ::= identifier
		node.value = &ast.ParamRef{}
	case 178:  // argument_list_Comma_separated ::= argument_list_Comma_separated ',' argument
		nn0, _ := rhs[0].value.([]*ast.Argument)
nn2, _ := rhs[2].value.(*ast.Argument)
node.value = append( nn0,  nn2)
	case 179:  // argument_list_Comma_separated ::= argument
		nn0, _ := rhs[0].value.(*ast.Argument)
node.value = []*ast.Argument{ nn0}
	case 182:  // symref_args ::= '<' argument_list_Comma_separated_opt '>'
		node.value = &ast.SymrefArgs{}
	case 183:  // argument ::= param_ref ':' param_value
		node.value = &ast.Argument{}
	case 184:  // argument ::= '+' param_ref
		node.value = &ast.Argument{}
	case 185:  // argument ::= '~' param_ref
		node.value = &ast.Argument{}
	case 186:  // argument ::= param_ref
		node.value = &ast.Argument{}
	case 187:  // param_type ::= Lflag
		node.value = ast.ParamType_LFLAG
	case 188:  // param_type ::= Lparam
		node.value = ast.ParamType_LPARAM
	case 191:  // predicate_primary ::= '!' param_ref
		node.value = &ast.BoolPredicate{}
	case 192:  // predicate_primary ::= param_ref
		node.value = &ast.BoolPredicate{}
	case 193:  // predicate_primary ::= param_ref '==' literal
		node.value = &ast.ComparePredicate{}
	case 194:  // predicate_primary ::= param_ref '!=' literal
		node.value = &ast.ComparePredicate{}
	case 196:  // predicate_expression ::= predicate_expression '&&' predicate_expression
		node.value = &ast.PredicateBinary{}
	case 197:  // predicate_expression ::= predicate_expression '||' predicate_expression
		node.value = &ast.PredicateBinary{}
	case 200:  // expression ::= Lnew name '(' map_entry_list_Comma_separated_opt ')'
		node.value = &ast.Instance{}
	case 201:  // expression ::= '[' expression_list_Comma_separated_opt ']'
		node.value = &ast.Array{}
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
		node.value = &ast.MapEntry{}
	case 212:  // literal ::= scon
		node.value = &ast.Literal{}
	case 213:  // literal ::= icon
		node.value = &ast.Literal{}
	case 214:  // literal ::= Ltrue
		node.value = &ast.Literal{}
	case 215:  // literal ::= Lfalse
		node.value = &ast.Literal{}
	case 216:  // name ::= qualified_id
		node.value = &ast.Name{}
	case 218:  // qualified_id ::= qualified_id '.' ID
		nn0, _ := rhs[0].value.(string)
nn2, _ := rhs[2].value.(string)
{ node.value =  nn0 + "." +  nn2; }
	case 219:  // command ::= code
		node.value = &ast.Command{}
	case 220:  // syntax_problem ::= error
		node.value = &ast.SyntaxProblem{}
	}
}
