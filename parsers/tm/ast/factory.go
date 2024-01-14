// generated by Textmapper; DO NOT EDIT

package ast

import (
	"fmt"

	"github.com/inspirer/textmapper/parsers/tm"
)

func ToTmNode(n *Node) TmNode {
	switch n.Type() {
	case tm.ArgumentFalse:
		return &ArgumentFalse{n}
	case tm.ArgumentTrue:
		return &ArgumentTrue{n}
	case tm.ArgumentVal:
		return &ArgumentVal{n}
	case tm.Array:
		return &Array{n}
	case tm.Assoc:
		return &Assoc{n}
	case tm.BooleanLiteral:
		return &BooleanLiteral{n}
	case tm.Command:
		return &Command{n}
	case tm.DirectiveAssert:
		return &DirectiveAssert{n}
	case tm.DirectiveBrackets:
		return &DirectiveBrackets{n}
	case tm.DirectiveExpect:
		return &DirectiveExpect{n}
	case tm.DirectiveExpectRR:
		return &DirectiveExpectRR{n}
	case tm.DirectiveInject:
		return &DirectiveInject{n}
	case tm.DirectiveInput:
		return &DirectiveInput{n}
	case tm.DirectiveInterface:
		return &DirectiveInterface{n}
	case tm.DirectivePrio:
		return &DirectivePrio{n}
	case tm.DirectiveSet:
		return &DirectiveSet{n}
	case tm.Empty:
		return &Empty{n}
	case tm.ExclusiveStartConds:
		return &ExclusiveStartConds{n}
	case tm.Extend:
		return &Extend{n}
	case tm.File:
		return &File{n}
	case tm.Header:
		return &Header{n}
	case tm.Identifier:
		return &Identifier{n}
	case tm.Import:
		return &Import{n}
	case tm.InclusiveStartConds:
		return &InclusiveStartConds{n}
	case tm.Inline:
		return &Inline{n}
	case tm.InlineParameter:
		return &InlineParameter{n}
	case tm.Inputref:
		return &Inputref{n}
	case tm.IntegerLiteral:
		return &IntegerLiteral{n}
	case tm.Lexeme:
		return &Lexeme{n}
	case tm.LexemeAttribute:
		return &LexemeAttribute{n}
	case tm.LexemeAttrs:
		return &LexemeAttrs{n}
	case tm.LexerSection:
		return &LexerSection{n}
	case tm.LexerState:
		return &LexerState{n}
	case tm.ListSeparator:
		return &ListSeparator{n}
	case tm.LookaheadPredicate:
		return &LookaheadPredicate{n}
	case tm.Name:
		return &Name{n}
	case tm.NamedPattern:
		return &NamedPattern{n}
	case tm.NoEoi:
		return &NoEoi{n}
	case tm.NonEmpty:
		return &NonEmpty{n}
	case tm.Nonterm:
		return &Nonterm{n}
	case tm.NontermParams:
		return &NontermParams{n}
	case tm.Not:
		return &Not{n}
	case tm.Option:
		return &Option{n}
	case tm.ParamModifier:
		return &ParamModifier{n}
	case tm.ParamRef:
		return &ParamRef{n}
	case tm.ParamType:
		return &ParamType{n}
	case tm.ParserSection:
		return &ParserSection{n}
	case tm.Pattern:
		return &Pattern{n}
	case tm.Predicate:
		return &Predicate{n}
	case tm.PredicateAnd:
		return &PredicateAnd{n}
	case tm.PredicateEq:
		return &PredicateEq{n}
	case tm.PredicateNot:
		return &PredicateNot{n}
	case tm.PredicateNotEq:
		return &PredicateNotEq{n}
	case tm.PredicateOr:
		return &PredicateOr{n}
	case tm.RawType:
		return &RawType{n}
	case tm.ReportAs:
		return &ReportAs{n}
	case tm.ReportClause:
		return &ReportClause{n}
	case tm.RhsAsLiteral:
		return &RhsAsLiteral{n}
	case tm.RhsAssignment:
		return &RhsAssignment{n}
	case tm.RhsCast:
		return &RhsCast{n}
	case tm.RhsIgnored:
		return &RhsIgnored{n}
	case tm.RhsLookahead:
		return &RhsLookahead{n}
	case tm.RhsNested:
		return &RhsNested{n}
	case tm.RhsOptional:
		return &RhsOptional{n}
	case tm.RhsPlusAssignment:
		return &RhsPlusAssignment{n}
	case tm.RhsPlusList:
		return &RhsPlusList{n}
	case tm.RhsPlusQuantifier:
		return &RhsPlusQuantifier{n}
	case tm.RhsSet:
		return &RhsSet{n}
	case tm.RhsStarList:
		return &RhsStarList{n}
	case tm.RhsStarQuantifier:
		return &RhsStarQuantifier{n}
	case tm.RhsSuffix:
		return &RhsSuffix{n}
	case tm.RhsSymbol:
		return &RhsSymbol{n}
	case tm.Rule:
		return &Rule{n}
	case tm.SetAnd:
		return &SetAnd{n}
	case tm.SetComplement:
		return &SetComplement{n}
	case tm.SetCompound:
		return &SetCompound{n}
	case tm.SetOr:
		return &SetOr{n}
	case tm.SetSymbol:
		return &SetSymbol{n}
	case tm.StartConditions:
		return &StartConditions{n}
	case tm.StartConditionsScope:
		return &StartConditionsScope{n}
	case tm.StateMarker:
		return &StateMarker{n}
	case tm.Stateref:
		return &Stateref{n}
	case tm.StringLiteral:
		return &StringLiteral{n}
	case tm.Symref:
		return &Symref{n}
	case tm.SymrefArgs:
		return &SymrefArgs{n}
	case tm.SyntaxProblem:
		return &SyntaxProblem{n}
	case tm.TemplateParam:
		return &TemplateParam{n}
	case tm.InvalidToken:
		return &InvalidToken{n}
	case tm.MultilineComment:
		return &MultilineComment{n}
	case tm.Comment:
		return &Comment{n}
	case tm.Templates:
		return &Templates{n}
	case tm.NoType:
		return nilInstance
	}
	panic(fmt.Errorf("ast: unknown node type %v", n.Type()))
	return nil
}
