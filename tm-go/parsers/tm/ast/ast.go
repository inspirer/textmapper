package ast

type LexemeAttribute int
const (
	LexemeAttribute_LSOFT LexemeAttribute = iota
	LexemeAttribute_LCLASS
	LexemeAttribute_LSPACE
	LexemeAttribute_LLAYOUT
)

type Assoc int
const (
	Assoc_LLEFT Assoc = iota
	Assoc_LRIGHT
	Assoc_LNONASSOC
)

type ParamModifier int
const (
	ParamModifier_LEXPLICIT ParamModifier = iota
	ParamModifier_LGLOBAL
	ParamModifier_LLOOKAHEAD
)

type ParamType int
const (
	ParamType_LFLAG ParamType = iota
	ParamType_LPARAM
)

type LexerPart interface {
	lexerPart()
}

type GrammarPart interface {
	grammarPart()
}

type NontermType interface {
	nontermType()
}

type NontermTypeAST struct {
	Reference *Symref
}

func (*NontermTypeAST) nontermType() {}

type NontermTypeHint struct {
	Inline bool
	Kind NontermTypeHint_KindKind
	Name *Identifier
	Implements []*Symref
}

func (*NontermTypeHint) nontermType() {}

type NontermTypeHint_KindKind int
const (
	NontermTypeHint_LCLASS NontermTypeHint_KindKind = iota
	NontermTypeHint_LVOID
	NontermTypeHint_LINTERFACE
)

type NontermTypeRaw struct {
	TypeText string
}

func (*NontermTypeRaw) nontermType() {}

type RhsPart interface {
	rhsPart()
}

type SetExpression interface {
	setExpression()
}

type SetBinary struct {
	Left SetExpression
	Kind SetBinary_KindKind
	Right SetExpression
}

func (*SetBinary) setExpression() {}

type SetBinary_KindKind int
const (
	SetBinary_OR SetBinary_KindKind = iota
	SetBinary_AND
)

type NontermParam interface {
	nontermParam()
}

type InlineParameter struct {
	ParamType string
	Name *Identifier
	ParamValue ParamValue
}

func (*InlineParameter) nontermParam() {}

type ParamValue interface {
	paramValue()
}

type PredicateExpression interface {
	predicateExpression()
}

type PredicateBinary struct {
	Left PredicateExpression
	Kind PredicateBinary_KindKind
	Right PredicateExpression
}

func (*PredicateBinary) predicateExpression() {}

type PredicateBinary_KindKind int
const (
	PredicateBinary_ANDAND PredicateBinary_KindKind = iota
	PredicateBinary_OROR
)

type Expression interface {
	expression()
}

type Instance struct {
	ClassName *Name
	Entries []*MapEntry
}

func (*Instance) expression() {}

type Array struct {
	Content []Expression
}

func (*Array) expression() {}

type Input struct {
	Header *Header
	Imports []*Import
	Options []*Option
	Lexer []LexerPart
	Parser []GrammarPart
}

type Header struct {
	Name *Name
	Target *Name
	ParsingAlgorithm *ParsingAlgorithm
}

type ParsingAlgorithm struct {
	La int
}

type Import struct {
	Alias string
	File string
}

type Option struct {
	Key string
	Value Expression
	SyntaxProblem *SyntaxProblem
}

type Identifier struct {
	ID string
}

type Symref struct {
	Name string
	Args *SymrefArgs
}

func (*Symref) expression() {}
func (*Symref) paramValue() {}

type Pattern struct {
	REGEXP string
}

type NamedPattern struct {
	Name string
	Pattern *Pattern
}

func (*NamedPattern) lexerPart() {}

type Lexeme struct {
	Name *Identifier
	Type string
	Pattern *Pattern
	Transition *Stateref
	Priority int
	Attrs *LexemeAttrs
	Command *Command
}

func (*Lexeme) lexerPart() {}

type LexemeAttrs struct {
	Kind LexemeAttribute
}

type StateSelector struct {
	States []*LexerState
}

func (*StateSelector) lexerPart() {}

type Stateref struct {
	Name string
}

type LexerState struct {
	Name *Identifier
	DefaultTransition *Stateref
}

type Nonterm struct {
	Annotations *Annotations
	Name *Identifier
	Params *NontermParams
	Type NontermType
	Rules []*Rule0
}

func (*Nonterm) grammarPart() {}

type Inputref struct {
	Reference *Symref
	Noeoi bool
}

type Rule0 struct {
	Predicate PredicateExpression
	Prefix *RhsPrefix
	List []RhsPart
	Action *RuleAction
	Suffix *RhsSuffix
	Error *SyntaxProblem
}

type RhsPrefix struct {
	Annotations *Annotations
}

type RhsSuffix struct {
	Kind RhsSuffix_KindKind
	Symref *Symref
}

type RhsSuffix_KindKind int
const (
	RhsSuffix_LPREC RhsSuffix_KindKind = iota
	RhsSuffix_LSHIFT
)

type RuleAction struct {
	Action *Identifier
	Parameter string
}

type Annotations struct {
	Annotations []*Annotation
}

type Annotation struct {
	Name string
	Expression Expression
	SyntaxProblem *SyntaxProblem
}

type NontermParams struct {
	List []NontermParam
}

type ParamRef struct {
	Ref *Identifier
}

func (*ParamRef) nontermParam() {}

type SymrefArgs struct {
	ArgList []*Argument
}

type Argument struct {
	Name *ParamRef
	Val ParamValue
	Bool Argument_BoolKind
}

type Argument_BoolKind int
const (
	Argument_PLUS Argument_BoolKind = iota
	Argument_TILDE
)

type MapEntry struct {
	Name string
	Value Expression
}

type Literal struct {
	Value interface{}
}

func (*Literal) paramValue() {}
func (*Literal) expression() {}

type Name struct {
	QualifiedId string
}

type Command struct {
}

func (*Command) rhsPart() {}

type SyntaxProblem struct {
}

func (*SyntaxProblem) lexerPart() {}
func (*SyntaxProblem) grammarPart() {}
func (*SyntaxProblem) rhsPart() {}
func (*SyntaxProblem) expression() {}

type DirectiveBrackets struct {
	Opening *Symref
	Closing *Symref
}

func (*DirectiveBrackets) lexerPart() {}

type TemplateParam struct {
	Modifier ParamModifier
	ParamType ParamType
	Name *Identifier
	ParamValue ParamValue
}

func (*TemplateParam) grammarPart() {}

type DirectivePrio struct {
	Assoc Assoc
	Symbols []*Symref
}

func (*DirectivePrio) grammarPart() {}

type DirectiveInput struct {
	InputRefs []*Inputref
}

func (*DirectiveInput) grammarPart() {}

type DirectiveAssert struct {
	Kind DirectiveAssert_KindKind
	RhsSet *RhsSet
}

func (*DirectiveAssert) grammarPart() {}

type DirectiveAssert_KindKind int
const (
	DirectiveAssert_LEMPTY DirectiveAssert_KindKind = iota
	DirectiveAssert_LNONEMPTY
)

type DirectiveSet struct {
	Name string
	RhsSet *RhsSet
}

func (*DirectiveSet) grammarPart() {}

type RhsAnnotated struct {
	Annotations *Annotations
	Inner RhsPart
}

func (*RhsAnnotated) rhsPart() {}

type RhsAssignment struct {
	Id *Identifier
	Addition bool
	Inner RhsPart
}

func (*RhsAssignment) rhsPart() {}

type RhsQuantifier struct {
	Inner RhsPart
	Quantifier RhsQuantifier_QuantifierKind
}

func (*RhsQuantifier) rhsPart() {}

type RhsQuantifier_QuantifierKind int
const (
	RhsQuantifier_QUEST RhsQuantifier_QuantifierKind = iota
	RhsQuantifier_PLUS
	RhsQuantifier_MULT
)

type RhsCast struct {
	Inner RhsPart
	Target *Symref
}

func (*RhsCast) rhsPart() {}

type RhsAsLiteral struct {
	Inner RhsPart
	Literal *Literal
}

func (*RhsAsLiteral) rhsPart() {}

type RhsUnordered struct {
	Left RhsPart
	Right RhsPart
}

func (*RhsUnordered) rhsPart() {}

type RhsClass struct {
	Identifier *Identifier
	Inner RhsPart
}

func (*RhsClass) rhsPart() {}

type RhsSymbol struct {
	Reference *Symref
}

func (*RhsSymbol) rhsPart() {}

type RhsNested struct {
	Rules []*Rule0
}

func (*RhsNested) rhsPart() {}

type RhsList struct {
	RuleParts []RhsPart
	Separator []*Symref
	AtLeastOne bool
}

func (*RhsList) rhsPart() {}

type RhsIgnored struct {
	Rules []*Rule0
}

func (*RhsIgnored) rhsPart() {}

type RhsSet struct {
	Expr SetExpression
}

func (*RhsSet) rhsPart() {}

type SetSymbol struct {
	Operator string
	Symbol *Symref
}

func (*SetSymbol) setExpression() {}

type SetCompound struct {
	Inner SetExpression
}

func (*SetCompound) setExpression() {}

type SetComplement struct {
	Inner SetExpression
}

func (*SetComplement) setExpression() {}

type BoolPredicate struct {
	Negated bool
	ParamRef *ParamRef
}

func (*BoolPredicate) predicateExpression() {}

type ComparePredicate struct {
	ParamRef *ParamRef
	Kind ComparePredicate_KindKind
	Literal *Literal
}

func (*ComparePredicate) predicateExpression() {}

type ComparePredicate_KindKind int
const (
	ComparePredicate_ASSIGNASSIGN ComparePredicate_KindKind = iota
	ComparePredicate_EXCLASSIGN
)
