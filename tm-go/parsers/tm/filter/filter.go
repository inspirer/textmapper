// generated by Textmapper; DO NOT EDIT

package filter

import (
	"github.com/inspirer/textmapper/tm-go/parsers/tm"
)

type NodeFilter func(nt tm.NodeType) bool

var (
	KeyValue            = func(t tm.NodeType) bool { return t == tm.KeyValue }
	AnnotationImpl      = func(t tm.NodeType) bool { return t == tm.AnnotationImpl }
	Annotations         = func(t tm.NodeType) bool { return t == tm.Annotations }
	ArgumentFalse       = func(t tm.NodeType) bool { return t == tm.ArgumentFalse }
	ArgumentImpl        = func(t tm.NodeType) bool { return t == tm.ArgumentImpl }
	ArgumentTrue        = func(t tm.NodeType) bool { return t == tm.ArgumentTrue }
	Array               = func(t tm.NodeType) bool { return t == tm.Array }
	Assoc               = func(t tm.NodeType) bool { return t == tm.Assoc }
	BooleanLiteral      = func(t tm.NodeType) bool { return t == tm.BooleanLiteral }
	Command             = func(t tm.NodeType) bool { return t == tm.Command }
	DirectiveAssert     = func(t tm.NodeType) bool { return t == tm.DirectiveAssert }
	DirectiveBrackets   = func(t tm.NodeType) bool { return t == tm.DirectiveBrackets }
	DirectiveInput      = func(t tm.NodeType) bool { return t == tm.DirectiveInput }
	DirectivePrio       = func(t tm.NodeType) bool { return t == tm.DirectivePrio }
	DirectiveSet        = func(t tm.NodeType) bool { return t == tm.DirectiveSet }
	GrammarParts        = func(t tm.NodeType) bool { return t == tm.GrammarParts }
	Header              = func(t tm.NodeType) bool { return t == tm.Header }
	Identifier          = func(t tm.NodeType) bool { return t == tm.Identifier }
	Import              = func(t tm.NodeType) bool { return t == tm.Import }
	InlineParameter     = func(t tm.NodeType) bool { return t == tm.InlineParameter }
	Input               = func(t tm.NodeType) bool { return t == tm.Input }
	Inputref            = func(t tm.NodeType) bool { return t == tm.Inputref }
	IntegerLiteral      = func(t tm.NodeType) bool { return t == tm.IntegerLiteral }
	InterfaceType       = func(t tm.NodeType) bool { return t == tm.InterfaceType }
	Lexeme              = func(t tm.NodeType) bool { return t == tm.Lexeme }
	LexemeAttribute     = func(t tm.NodeType) bool { return t == tm.LexemeAttribute }
	LexemeAttrs         = func(t tm.NodeType) bool { return t == tm.LexemeAttrs }
	LexerState          = func(t tm.NodeType) bool { return t == tm.LexerState }
	ListSeparator       = func(t tm.NodeType) bool { return t == tm.ListSeparator }
	Name                = func(t tm.NodeType) bool { return t == tm.Name }
	NamedPattern        = func(t tm.NodeType) bool { return t == tm.NamedPattern }
	Nonterm             = func(t tm.NodeType) bool { return t == tm.Nonterm }
	NontermParams       = func(t tm.NodeType) bool { return t == tm.NontermParams }
	ParamModifier       = func(t tm.NodeType) bool { return t == tm.ParamModifier }
	ParamRef            = func(t tm.NodeType) bool { return t == tm.ParamRef }
	ParamType           = func(t tm.NodeType) bool { return t == tm.ParamType }
	Pattern             = func(t tm.NodeType) bool { return t == tm.Pattern }
	Predicate           = func(t tm.NodeType) bool { return t == tm.Predicate }
	PredicateAnd        = func(t tm.NodeType) bool { return t == tm.PredicateAnd }
	PredicateEq         = func(t tm.NodeType) bool { return t == tm.PredicateEq }
	PredicateNot        = func(t tm.NodeType) bool { return t == tm.PredicateNot }
	PredicateNotEq      = func(t tm.NodeType) bool { return t == tm.PredicateNotEq }
	PredicateOr         = func(t tm.NodeType) bool { return t == tm.PredicateOr }
	RawType             = func(t tm.NodeType) bool { return t == tm.RawType }
	References          = func(t tm.NodeType) bool { return t == tm.References }
	RhsAnnotated        = func(t tm.NodeType) bool { return t == tm.RhsAnnotated }
	RhsAssignment       = func(t tm.NodeType) bool { return t == tm.RhsAssignment }
	RhsCast             = func(t tm.NodeType) bool { return t == tm.RhsCast }
	RhsIgnored          = func(t tm.NodeType) bool { return t == tm.RhsIgnored }
	RhsNested           = func(t tm.NodeType) bool { return t == tm.RhsNested }
	RhsOptional         = func(t tm.NodeType) bool { return t == tm.RhsOptional }
	RhsPlusAssignment   = func(t tm.NodeType) bool { return t == tm.RhsPlusAssignment }
	RhsPlusList         = func(t tm.NodeType) bool { return t == tm.RhsPlusList }
	RhsPrimary          = func(t tm.NodeType) bool { return t == tm.RhsPrimary }
	RhsQuantifier       = func(t tm.NodeType) bool { return t == tm.RhsQuantifier }
	RhsSet              = func(t tm.NodeType) bool { return t == tm.RhsSet }
	RhsStarList         = func(t tm.NodeType) bool { return t == tm.RhsStarList }
	RhsSuffix           = func(t tm.NodeType) bool { return t == tm.RhsSuffix }
	RhsSymbol           = func(t tm.NodeType) bool { return t == tm.RhsSymbol }
	Rule                = func(t tm.NodeType) bool { return t == tm.Rule }
	RuleAction          = func(t tm.NodeType) bool { return t == tm.RuleAction }
	SetAnd              = func(t tm.NodeType) bool { return t == tm.SetAnd }
	SetComplement       = func(t tm.NodeType) bool { return t == tm.SetComplement }
	SetCompound         = func(t tm.NodeType) bool { return t == tm.SetCompound }
	SetOr               = func(t tm.NodeType) bool { return t == tm.SetOr }
	SetSymbol           = func(t tm.NodeType) bool { return t == tm.SetSymbol }
	StateSelector       = func(t tm.NodeType) bool { return t == tm.StateSelector }
	StringLiteral       = func(t tm.NodeType) bool { return t == tm.StringLiteral }
	SubType             = func(t tm.NodeType) bool { return t == tm.SubType }
	Symref              = func(t tm.NodeType) bool { return t == tm.Symref }
	SymrefArgs          = func(t tm.NodeType) bool { return t == tm.SymrefArgs }
	SyntaxProblem       = func(t tm.NodeType) bool { return t == tm.SyntaxProblem }
	TemplateParam       = func(t tm.NodeType) bool { return t == tm.TemplateParam }
	VoidType            = func(t tm.NodeType) bool { return t == tm.VoidType }
	Annotation          = OneOf(tm.Annotation...)
	Argument            = OneOf(tm.Argument...)
	Expression          = OneOf(tm.Expression...)
	GrammarPart         = OneOf(tm.GrammarPart...)
	LexerPart           = OneOf(tm.LexerPart...)
	Literal             = OneOf(tm.Literal...)
	NontermParam        = OneOf(tm.NontermParam...)
	NontermType         = OneOf(tm.NontermType...)
	Option              = OneOf(tm.Option...)
	ParamValue          = OneOf(tm.ParamValue...)
	PredicateExpression = OneOf(tm.PredicateExpression...)
	RhsPart             = OneOf(tm.RhsPart...)
	Rule0               = OneOf(tm.Rule0...)
	SetExpression       = OneOf(tm.SetExpression...)
)

func OneOf(types ...tm.NodeType) NodeFilter {
	if len(types) == 0 {
		return func(tm.NodeType) bool { return false }
	}
	const bits = 32
	size := (int(types[len(types)-1]) + bits - 1) / bits
	bitarr := make([]int32, size)
	for _, t := range types {
		bitarr[uint(t)/bits] |= 1 << (uint(t) % bits)
	}
	return func(t tm.NodeType) bool {
		return bitarr[uint(t)/bits]&(1<<(uint(t)%bits)) != 0
	}
}
