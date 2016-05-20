package js

type NodeType int

type Listener interface {
	Node(t NodeType, offset, endoffset int)
}

const (
	IdentifierName NodeType = iota + 1
	IdentifierReference
	BindingIdentifier
	LabelIdentifier
	PrimaryExpression
	CoverParenthesizedExpressionAndArrowParameterList
	Literal
	ArrayLiteral
	SpreadElement
	ObjectLiteral
	PropertyDefinition
	LiteralPropertyName
	ComputedPropertyName
	CoverInitializedName
	Initializer
	TemplateLiteral
	TemplateSpans
	MemberExpression
	SuperProperty
	MetaProperty
	NewTarget
	NewExpression
	CallExpression
	SuperCall
	Arguments
	LeftHandSideExpression
	PostfixExpression
	UnaryExpression
	MultiplicativeExpression
	MultiplicativeOperator
	AdditiveExpression
	ShiftExpression
	RelationalExpression
	EqualityExpression
	BitwiseANDExpression
	BitwiseXORExpression
	BitwiseORExpression
	LogicalANDExpression
	LogicalORExpression
	ConditionalExpression
	AssignmentExpression
	AssignmentOperator
	Expression
	Statement
	Declaration
	HoistableDeclaration
	BreakableStatement
	BlockStatement
	Block
	StatementListItem
	LexicalDeclaration
	LetOrConst
	LexicalBinding
	VariableStatement
	VariableDeclaration
	BindingPattern
	ObjectBindingPattern
	ArrayBindingPattern
	BindingElisionElement
	BindingProperty
	BindingElement
	SingleNameBinding
	BindingRestElement
	EmptyStatement
	ExpressionStatement
	IfStatement
	IterationStatement
	ForDeclaration
	ForBinding
	ContinueStatement
	BreakStatement
	ReturnStatement
	WithStatement
	SwitchStatement
	CaseBlock
	CaseClause
	DefaultClause
	LabelledStatement
	LabelledItem
	ThrowStatement
	TryStatement
	Catch
	Finally
	CatchParameter
	DebuggerStatement
	FunctionDeclaration
	FunctionExpression
	StrictFormalParameters
	FormalParameterList
	FunctionRestParameter
	FormalParameter
	ArrowFunction
	ArrowParameters
	ConciseBody
	MethodDefinition
	PropertySetParameterList
	GeneratorMethod
	GeneratorDeclaration
	GeneratorExpression
	GeneratorBody
	YieldExpression
	ClassDeclaration
	ClassExpression
	ClassTail
	ClassHeritage
	ClassBody
	ClassElement
	Module
	ModuleBody
	ModuleItem
	ImportDeclaration
	ImportClause
	ImportedDefaultBinding
	NameSpaceImport
	NamedImports
	FromClause
	ImportSpecifier
	ModuleSpecifier
	ImportedBinding
	ExportDeclaration
	ExportClause
	ExportSpecifier
)

var ruleNodeType = [...]NodeType{
	IdentifierName, // IdentifierName ::= Identifier
	IdentifierName, // IdentifierName ::= 'break'
	IdentifierName, // IdentifierName ::= 'do'
	IdentifierName, // IdentifierName ::= 'in'
	IdentifierName, // IdentifierName ::= 'typeof'
	IdentifierName, // IdentifierName ::= 'case'
	IdentifierName, // IdentifierName ::= 'else'
	IdentifierName, // IdentifierName ::= 'instanceof'
	IdentifierName, // IdentifierName ::= 'var'
	IdentifierName, // IdentifierName ::= 'catch'
	IdentifierName, // IdentifierName ::= 'export'
	IdentifierName, // IdentifierName ::= 'new'
	IdentifierName, // IdentifierName ::= 'void'
	IdentifierName, // IdentifierName ::= 'class'
	IdentifierName, // IdentifierName ::= 'extends'
	IdentifierName, // IdentifierName ::= 'return'
	IdentifierName, // IdentifierName ::= 'while'
	IdentifierName, // IdentifierName ::= 'const'
	IdentifierName, // IdentifierName ::= 'finally'
	IdentifierName, // IdentifierName ::= 'super'
	IdentifierName, // IdentifierName ::= 'with'
	IdentifierName, // IdentifierName ::= 'continue'
	IdentifierName, // IdentifierName ::= 'for'
	IdentifierName, // IdentifierName ::= 'switch'
	IdentifierName, // IdentifierName ::= 'yield'
	IdentifierName, // IdentifierName ::= 'debugger'
	IdentifierName, // IdentifierName ::= 'function'
	IdentifierName, // IdentifierName ::= 'this'
	IdentifierName, // IdentifierName ::= 'default'
	IdentifierName, // IdentifierName ::= 'if'
	IdentifierName, // IdentifierName ::= 'throw'
	IdentifierName, // IdentifierName ::= 'delete'
	IdentifierName, // IdentifierName ::= 'import'
	IdentifierName, // IdentifierName ::= 'try'
	IdentifierName, // IdentifierName ::= 'enum'
	IdentifierName, // IdentifierName ::= 'await'
	IdentifierName, // IdentifierName ::= 'null'
	IdentifierName, // IdentifierName ::= 'true'
	IdentifierName, // IdentifierName ::= 'false'
	IdentifierName, // IdentifierName ::= 'as'
	IdentifierName, // IdentifierName ::= 'from'
	IdentifierName, // IdentifierName ::= 'get'
	IdentifierName, // IdentifierName ::= 'let'
	IdentifierName, // IdentifierName ::= 'of'
	IdentifierName, // IdentifierName ::= 'set'
	IdentifierName, // IdentifierName ::= 'static'
	IdentifierName, // IdentifierName ::= 'target'
	IdentifierReference, // IdentifierReference ::= Identifier
	IdentifierReference, // IdentifierReference ::= 'yield'
	IdentifierReference, // IdentifierReference ::= 'let'
	IdentifierReference, // IdentifierReference ::= 'as'
	IdentifierReference, // IdentifierReference ::= 'from'
	IdentifierReference, // IdentifierReference ::= 'get'
	IdentifierReference, // IdentifierReference ::= 'of'
	IdentifierReference, // IdentifierReference ::= 'set'
	IdentifierReference, // IdentifierReference ::= 'static'
	IdentifierReference, // IdentifierReference ::= 'target'
	IdentifierReference, // IdentifierReference_NoLet ::= Identifier
	IdentifierReference, // IdentifierReference_NoLet ::= 'yield'
	IdentifierReference, // IdentifierReference_NoLet ::= 'as'
	IdentifierReference, // IdentifierReference_NoLet ::= 'from'
	IdentifierReference, // IdentifierReference_NoLet ::= 'get'
	IdentifierReference, // IdentifierReference_NoLet ::= 'of'
	IdentifierReference, // IdentifierReference_NoLet ::= 'set'
	IdentifierReference, // IdentifierReference_NoLet ::= 'static'
	IdentifierReference, // IdentifierReference_NoLet ::= 'target'
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= Identifier
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= 'as'
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= 'from'
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= 'get'
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= 'of'
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= 'set'
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= 'static'
	IdentifierReference, // IdentifierReference_NoLet_Yield ::= 'target'
	IdentifierReference, // IdentifierReference_Yield ::= Identifier
	IdentifierReference, // IdentifierReference_Yield ::= 'let'
	IdentifierReference, // IdentifierReference_Yield ::= 'as'
	IdentifierReference, // IdentifierReference_Yield ::= 'from'
	IdentifierReference, // IdentifierReference_Yield ::= 'get'
	IdentifierReference, // IdentifierReference_Yield ::= 'of'
	IdentifierReference, // IdentifierReference_Yield ::= 'set'
	IdentifierReference, // IdentifierReference_Yield ::= 'static'
	IdentifierReference, // IdentifierReference_Yield ::= 'target'
	BindingIdentifier, // BindingIdentifier ::= Identifier
	BindingIdentifier, // BindingIdentifier ::= 'yield'
	BindingIdentifier, // BindingIdentifier ::= 'as'
	BindingIdentifier, // BindingIdentifier ::= 'from'
	BindingIdentifier, // BindingIdentifier ::= 'get'
	BindingIdentifier, // BindingIdentifier ::= 'let'
	BindingIdentifier, // BindingIdentifier ::= 'of'
	BindingIdentifier, // BindingIdentifier ::= 'set'
	BindingIdentifier, // BindingIdentifier ::= 'static'
	BindingIdentifier, // BindingIdentifier ::= 'target'
	BindingIdentifier, // BindingIdentifier_Yield ::= Identifier
	BindingIdentifier, // BindingIdentifier_Yield ::= 'as'
	BindingIdentifier, // BindingIdentifier_Yield ::= 'from'
	BindingIdentifier, // BindingIdentifier_Yield ::= 'get'
	BindingIdentifier, // BindingIdentifier_Yield ::= 'let'
	BindingIdentifier, // BindingIdentifier_Yield ::= 'of'
	BindingIdentifier, // BindingIdentifier_Yield ::= 'set'
	BindingIdentifier, // BindingIdentifier_Yield ::= 'static'
	BindingIdentifier, // BindingIdentifier_Yield ::= 'target'
	LabelIdentifier, // LabelIdentifier ::= Identifier
	LabelIdentifier, // LabelIdentifier ::= 'yield'
	LabelIdentifier, // LabelIdentifier ::= 'as'
	LabelIdentifier, // LabelIdentifier ::= 'from'
	LabelIdentifier, // LabelIdentifier ::= 'get'
	LabelIdentifier, // LabelIdentifier ::= 'let'
	LabelIdentifier, // LabelIdentifier ::= 'of'
	LabelIdentifier, // LabelIdentifier ::= 'set'
	LabelIdentifier, // LabelIdentifier ::= 'static'
	LabelIdentifier, // LabelIdentifier ::= 'target'
	LabelIdentifier, // LabelIdentifier_Yield ::= Identifier
	LabelIdentifier, // LabelIdentifier_Yield ::= 'as'
	LabelIdentifier, // LabelIdentifier_Yield ::= 'from'
	LabelIdentifier, // LabelIdentifier_Yield ::= 'get'
	LabelIdentifier, // LabelIdentifier_Yield ::= 'let'
	LabelIdentifier, // LabelIdentifier_Yield ::= 'of'
	LabelIdentifier, // LabelIdentifier_Yield ::= 'set'
	LabelIdentifier, // LabelIdentifier_Yield ::= 'static'
	LabelIdentifier, // LabelIdentifier_Yield ::= 'target'
	PrimaryExpression, // PrimaryExpression ::= 'this'
	PrimaryExpression, // PrimaryExpression ::= IdentifierReference
	PrimaryExpression, // PrimaryExpression ::= Literal
	PrimaryExpression, // PrimaryExpression ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression ::= ObjectLiteral
	PrimaryExpression, // PrimaryExpression ::= FunctionExpression
	PrimaryExpression, // PrimaryExpression ::= ClassExpression
	PrimaryExpression, // PrimaryExpression ::= GeneratorExpression
	PrimaryExpression, // PrimaryExpression ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= IdentifierReference
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= Literal
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= ObjectLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= IdentifierReference_NoLet
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= Literal
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= ObjectLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral ::= IdentifierReference_NoLet
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral ::= Literal
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield ::= IdentifierReference_NoLet_Yield
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield ::= Literal
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield ::= ArrayLiteral_Yield
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield ::= TemplateLiteral_Yield
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield ::= CoverParenthesizedExpressionAndArrowParameterList_Yield
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral ::= IdentifierReference
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral ::= Literal
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral_Yield ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral_Yield ::= IdentifierReference_Yield
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral_Yield ::= Literal
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral_Yield ::= ArrayLiteral_Yield
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral_Yield ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral_Yield ::= TemplateLiteral_Yield
	PrimaryExpression, // PrimaryExpression_NoFuncClass_NoObjLiteral_Yield ::= CoverParenthesizedExpressionAndArrowParameterList_Yield
	PrimaryExpression, // PrimaryExpression_NoLet ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoLet ::= IdentifierReference_NoLet
	PrimaryExpression, // PrimaryExpression_NoLet ::= Literal
	PrimaryExpression, // PrimaryExpression_NoLet ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression_NoLet ::= ObjectLiteral
	PrimaryExpression, // PrimaryExpression_NoLet ::= FunctionExpression
	PrimaryExpression, // PrimaryExpression_NoLet ::= ClassExpression
	PrimaryExpression, // PrimaryExpression_NoLet ::= GeneratorExpression
	PrimaryExpression, // PrimaryExpression_NoLet ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoLet ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression_NoLet ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= IdentifierReference_NoLet
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= Literal
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= FunctionExpression
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= ClassExpression
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= GeneratorExpression
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression_NoLet_NoObjLiteral ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= IdentifierReference_NoLet_Yield
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= Literal
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= ArrayLiteral_Yield
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= ObjectLiteral_Yield
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= FunctionExpression
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= ClassExpression_Yield
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= GeneratorExpression
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= TemplateLiteral_Yield
	PrimaryExpression, // PrimaryExpression_NoLet_Yield ::= CoverParenthesizedExpressionAndArrowParameterList_Yield
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= 'this'
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= IdentifierReference
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= Literal
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= ArrayLiteral
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= FunctionExpression
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= ClassExpression
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= GeneratorExpression
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= TemplateLiteral
	PrimaryExpression, // PrimaryExpression_NoObjLiteral ::= CoverParenthesizedExpressionAndArrowParameterList
	PrimaryExpression, // PrimaryExpression_Yield ::= 'this'
	PrimaryExpression, // PrimaryExpression_Yield ::= IdentifierReference_Yield
	PrimaryExpression, // PrimaryExpression_Yield ::= Literal
	PrimaryExpression, // PrimaryExpression_Yield ::= ArrayLiteral_Yield
	PrimaryExpression, // PrimaryExpression_Yield ::= ObjectLiteral_Yield
	PrimaryExpression, // PrimaryExpression_Yield ::= FunctionExpression
	PrimaryExpression, // PrimaryExpression_Yield ::= ClassExpression_Yield
	PrimaryExpression, // PrimaryExpression_Yield ::= GeneratorExpression
	PrimaryExpression, // PrimaryExpression_Yield ::= RegularExpressionLiteral
	PrimaryExpression, // PrimaryExpression_Yield ::= TemplateLiteral_Yield
	PrimaryExpression, // PrimaryExpression_Yield ::= CoverParenthesizedExpressionAndArrowParameterList_Yield
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList ::= '(' Expression_In ')'
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList ::= '(' ')'
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList ::= '(' '.' '.' '.' BindingIdentifier ')'
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList ::= '(' Expression_In ',' '.' '.' '.' BindingIdentifier ')'
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList_Yield ::= '(' Expression_In_Yield ')'
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList_Yield ::= '(' ')'
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList_Yield ::= '(' '.' '.' '.' BindingIdentifier_Yield ')'
	CoverParenthesizedExpressionAndArrowParameterList, // CoverParenthesizedExpressionAndArrowParameterList_Yield ::= '(' Expression_In_Yield ',' '.' '.' '.' BindingIdentifier_Yield ')'
	Literal, // Literal ::= 'null'
	Literal, // Literal ::= 'true'
	Literal, // Literal ::= 'false'
	Literal, // Literal ::= NumericLiteral
	Literal, // Literal ::= StringLiteral
	ArrayLiteral, // ArrayLiteral ::= '[' Elisionopt ']'
	ArrayLiteral, // ArrayLiteral ::= '[' ElementList ']'
	ArrayLiteral, // ArrayLiteral ::= '[' ElementList ',' Elisionopt ']'
	ArrayLiteral, // ArrayLiteral_Yield ::= '[' Elisionopt ']'
	ArrayLiteral, // ArrayLiteral_Yield ::= '[' ElementList_Yield ']'
	ArrayLiteral, // ArrayLiteral_Yield ::= '[' ElementList_Yield ',' Elisionopt ']'
	0, // ElementList ::= Elisionopt AssignmentExpression_In
	0, // ElementList ::= Elisionopt SpreadElement
	0, // ElementList ::= ElementList ',' Elisionopt AssignmentExpression_In
	0, // ElementList ::= ElementList ',' Elisionopt SpreadElement
	0, // ElementList_Yield ::= Elisionopt AssignmentExpression_In_Yield
	0, // ElementList_Yield ::= Elisionopt SpreadElement_Yield
	0, // ElementList_Yield ::= ElementList_Yield ',' Elisionopt AssignmentExpression_In_Yield
	0, // ElementList_Yield ::= ElementList_Yield ',' Elisionopt SpreadElement_Yield
	0, // Elision ::= ','
	0, // Elision ::= Elision ','
	SpreadElement, // SpreadElement ::= '.' '.' '.' AssignmentExpression_In
	SpreadElement, // SpreadElement_Yield ::= '.' '.' '.' AssignmentExpression_In_Yield
	ObjectLiteral, // ObjectLiteral ::= '{' '}'
	ObjectLiteral, // ObjectLiteral ::= '{' PropertyDefinitionList '}'
	ObjectLiteral, // ObjectLiteral ::= '{' PropertyDefinitionList ',' '}'
	ObjectLiteral, // ObjectLiteral_Yield ::= '{' '}'
	ObjectLiteral, // ObjectLiteral_Yield ::= '{' PropertyDefinitionList_Yield '}'
	ObjectLiteral, // ObjectLiteral_Yield ::= '{' PropertyDefinitionList_Yield ',' '}'
	0, // PropertyDefinitionList ::= PropertyDefinition
	0, // PropertyDefinitionList ::= PropertyDefinitionList ',' PropertyDefinition
	0, // PropertyDefinitionList_Yield ::= PropertyDefinition_Yield
	0, // PropertyDefinitionList_Yield ::= PropertyDefinitionList_Yield ',' PropertyDefinition_Yield
	PropertyDefinition, // PropertyDefinition ::= IdentifierReference
	PropertyDefinition, // PropertyDefinition ::= CoverInitializedName
	PropertyDefinition, // PropertyDefinition ::= PropertyName ':' AssignmentExpression_In
	PropertyDefinition, // PropertyDefinition ::= MethodDefinition
	PropertyDefinition, // PropertyDefinition_Yield ::= IdentifierReference_Yield
	PropertyDefinition, // PropertyDefinition_Yield ::= CoverInitializedName_Yield
	PropertyDefinition, // PropertyDefinition_Yield ::= PropertyName_Yield ':' AssignmentExpression_In_Yield
	PropertyDefinition, // PropertyDefinition_Yield ::= MethodDefinition_Yield
	0, // PropertyName ::= LiteralPropertyName
	0, // PropertyName ::= ComputedPropertyName
	0, // PropertyName_Yield ::= LiteralPropertyName
	0, // PropertyName_Yield ::= ComputedPropertyName_Yield
	LiteralPropertyName, // LiteralPropertyName ::= IdentifierName
	LiteralPropertyName, // LiteralPropertyName ::= StringLiteral
	LiteralPropertyName, // LiteralPropertyName ::= NumericLiteral
	ComputedPropertyName, // ComputedPropertyName ::= '[' AssignmentExpression_In ']'
	ComputedPropertyName, // ComputedPropertyName_Yield ::= '[' AssignmentExpression_In_Yield ']'
	CoverInitializedName, // CoverInitializedName ::= IdentifierReference Initializer_In
	CoverInitializedName, // CoverInitializedName_Yield ::= IdentifierReference_Yield Initializer_In_Yield
	Initializer, // Initializer ::= '=' AssignmentExpression
	Initializer, // Initializer_In ::= '=' AssignmentExpression_In
	Initializer, // Initializer_In_Yield ::= '=' AssignmentExpression_In_Yield
	Initializer, // Initializer_Yield ::= '=' AssignmentExpression_Yield
	TemplateLiteral, // TemplateLiteral ::= NoSubstitutionTemplate
	TemplateLiteral, // TemplateLiteral ::= TemplateHead Expression_In TemplateSpans
	TemplateLiteral, // TemplateLiteral_Yield ::= NoSubstitutionTemplate
	TemplateLiteral, // TemplateLiteral_Yield ::= TemplateHead Expression_In_Yield TemplateSpans_Yield
	TemplateSpans, // TemplateSpans ::= TemplateTail
	TemplateSpans, // TemplateSpans ::= TemplateMiddleList TemplateTail
	TemplateSpans, // TemplateSpans_Yield ::= TemplateTail
	TemplateSpans, // TemplateSpans_Yield ::= TemplateMiddleList_Yield TemplateTail
	0, // TemplateMiddleList ::= TemplateMiddle Expression_In
	0, // TemplateMiddleList ::= TemplateMiddleList TemplateMiddle Expression_In
	0, // TemplateMiddleList_Yield ::= TemplateMiddle Expression_In_Yield
	0, // TemplateMiddleList_Yield ::= TemplateMiddleList_Yield TemplateMiddle Expression_In_Yield
	MemberExpression, // MemberExpression ::= PrimaryExpression
	MemberExpression, // MemberExpression ::= MemberExpression '[' Expression_In ']'
	MemberExpression, // MemberExpression ::= MemberExpression '.' IdentifierName
	MemberExpression, // MemberExpression ::= MemberExpression TemplateLiteral
	MemberExpression, // MemberExpression ::= SuperProperty
	MemberExpression, // MemberExpression ::= MetaProperty
	MemberExpression, // MemberExpression ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoFuncClass ::= PrimaryExpression_NoFuncClass
	MemberExpression, // MemberExpression_NoFuncClass ::= MemberExpression_NoFuncClass '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoFuncClass ::= MemberExpression_NoFuncClass '.' IdentifierName
	MemberExpression, // MemberExpression_NoFuncClass ::= MemberExpression_NoFuncClass TemplateLiteral
	MemberExpression, // MemberExpression_NoFuncClass ::= SuperProperty
	MemberExpression, // MemberExpression_NoFuncClass ::= MetaProperty
	MemberExpression, // MemberExpression_NoFuncClass ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= PrimaryExpression_NoFuncClass_NoObjLiteral
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral '.' IdentifierName
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= SuperProperty
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MetaProperty
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= PrimaryExpression_NoFuncClass_NoObjLiteral_Yield
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= SuperProperty_Yield
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MetaProperty
	MemberExpression, // MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= 'new' MemberExpression_Yield Arguments_Yield
	MemberExpression, // MemberExpression_NoLet ::= PrimaryExpression_NoLet
	MemberExpression, // MemberExpression_NoLet ::= MemberExpression_NoLet '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoLet ::= MemberExpression_NoLet '.' IdentifierName
	MemberExpression, // MemberExpression_NoLet ::= MemberExpression_NoLet TemplateLiteral
	MemberExpression, // MemberExpression_NoLet ::= SuperProperty
	MemberExpression, // MemberExpression_NoLet ::= MetaProperty
	MemberExpression, // MemberExpression_NoLet ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoLet_Yield ::= PrimaryExpression_NoLet_Yield
	MemberExpression, // MemberExpression_NoLet_Yield ::= MemberExpression_NoLet_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_NoLet_Yield ::= MemberExpression_NoLet_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_NoLet_Yield ::= MemberExpression_NoLet_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_NoLet_Yield ::= SuperProperty_Yield
	MemberExpression, // MemberExpression_NoLet_Yield ::= MetaProperty
	MemberExpression, // MemberExpression_NoLet_Yield ::= 'new' MemberExpression_Yield Arguments_Yield
	MemberExpression, // MemberExpression_NoLetOnly ::= PrimaryExpression_NoLet
	MemberExpression, // MemberExpression_NoLetOnly ::= MemberExpression '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoLetOnly ::= MemberExpression '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly ::= MemberExpression TemplateLiteral
	MemberExpression, // MemberExpression_NoLetOnly ::= SuperProperty
	MemberExpression, // MemberExpression_NoLetOnly ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass ::= PrimaryExpression_NoFuncClass_NoLet
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass ::= MemberExpression_NoFuncClass '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass ::= MemberExpression_NoFuncClass '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass ::= MemberExpression_NoFuncClass TemplateLiteral
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass ::= SuperProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral ::= PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral ::= SuperProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= SuperProperty_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= 'new' MemberExpression_Yield Arguments_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoLet ::= PrimaryExpression_NoLet
	MemberExpression, // MemberExpression_NoLetOnly_NoLet ::= MemberExpression_NoLet '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoLetOnly_NoLet ::= MemberExpression_NoLet '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_NoLet ::= MemberExpression_NoLet TemplateLiteral
	MemberExpression, // MemberExpression_NoLetOnly_NoLet ::= SuperProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoLet ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoLet ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoLetOnly_NoLet_Yield ::= PrimaryExpression_NoLet_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoLet_Yield ::= MemberExpression_NoLet_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_NoLetOnly_NoLet_Yield ::= MemberExpression_NoLet_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_NoLet_Yield ::= MemberExpression_NoLet_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoLet_Yield ::= SuperProperty_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoLet_Yield ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoLet_Yield ::= 'new' MemberExpression_Yield Arguments_Yield
	MemberExpression, // MemberExpression_NoLetOnly_NoObjLiteral ::= PrimaryExpression_NoLet_NoObjLiteral
	MemberExpression, // MemberExpression_NoLetOnly_NoObjLiteral ::= MemberExpression_NoObjLiteral '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoLetOnly_NoObjLiteral ::= MemberExpression_NoObjLiteral '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_NoObjLiteral ::= MemberExpression_NoObjLiteral TemplateLiteral
	MemberExpression, // MemberExpression_NoLetOnly_NoObjLiteral ::= SuperProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoObjLiteral ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly_NoObjLiteral ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_NoLetOnly_StartWithLet ::= MemberExpression_NoLetOnly_StartWithLet '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoLetOnly_StartWithLet ::= MemberExpression_StartWithLet '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_StartWithLet ::= MemberExpression_StartWithLet TemplateLiteral
	MemberExpression, // MemberExpression_NoLetOnly_StartWithLet_Yield ::= MemberExpression_NoLetOnly_StartWithLet_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_NoLetOnly_StartWithLet_Yield ::= MemberExpression_StartWithLet_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_StartWithLet_Yield ::= MemberExpression_StartWithLet_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_NoLetOnly_Yield ::= PrimaryExpression_NoLet_Yield
	MemberExpression, // MemberExpression_NoLetOnly_Yield ::= MemberExpression_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_NoLetOnly_Yield ::= MemberExpression_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_NoLetOnly_Yield ::= MemberExpression_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_NoLetOnly_Yield ::= SuperProperty_Yield
	MemberExpression, // MemberExpression_NoLetOnly_Yield ::= MetaProperty
	MemberExpression, // MemberExpression_NoLetOnly_Yield ::= 'new' MemberExpression_Yield Arguments_Yield
	MemberExpression, // MemberExpression_NoObjLiteral ::= PrimaryExpression_NoObjLiteral
	MemberExpression, // MemberExpression_NoObjLiteral ::= MemberExpression_NoObjLiteral '[' Expression_In ']'
	MemberExpression, // MemberExpression_NoObjLiteral ::= MemberExpression_NoObjLiteral '.' IdentifierName
	MemberExpression, // MemberExpression_NoObjLiteral ::= MemberExpression_NoObjLiteral TemplateLiteral
	MemberExpression, // MemberExpression_NoObjLiteral ::= SuperProperty
	MemberExpression, // MemberExpression_NoObjLiteral ::= MetaProperty
	MemberExpression, // MemberExpression_NoObjLiteral ::= 'new' MemberExpression Arguments
	MemberExpression, // MemberExpression_StartWithLet ::= 'let'
	MemberExpression, // MemberExpression_StartWithLet ::= MemberExpression_NoLetOnly_StartWithLet '[' Expression_In ']'
	MemberExpression, // MemberExpression_StartWithLet ::= MemberExpression_StartWithLet '.' IdentifierName
	MemberExpression, // MemberExpression_StartWithLet ::= MemberExpression_StartWithLet TemplateLiteral
	MemberExpression, // MemberExpression_StartWithLet_Yield ::= 'let'
	MemberExpression, // MemberExpression_StartWithLet_Yield ::= MemberExpression_NoLetOnly_StartWithLet_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_StartWithLet_Yield ::= MemberExpression_StartWithLet_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_StartWithLet_Yield ::= MemberExpression_StartWithLet_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_Yield ::= PrimaryExpression_Yield
	MemberExpression, // MemberExpression_Yield ::= MemberExpression_Yield '[' Expression_In_Yield ']'
	MemberExpression, // MemberExpression_Yield ::= MemberExpression_Yield '.' IdentifierName
	MemberExpression, // MemberExpression_Yield ::= MemberExpression_Yield TemplateLiteral_Yield
	MemberExpression, // MemberExpression_Yield ::= SuperProperty_Yield
	MemberExpression, // MemberExpression_Yield ::= MetaProperty
	MemberExpression, // MemberExpression_Yield ::= 'new' MemberExpression_Yield Arguments_Yield
	SuperProperty, // SuperProperty ::= 'super' '[' Expression_In ']'
	SuperProperty, // SuperProperty ::= 'super' '.' IdentifierName
	SuperProperty, // SuperProperty_Yield ::= 'super' '[' Expression_In_Yield ']'
	SuperProperty, // SuperProperty_Yield ::= 'super' '.' IdentifierName
	MetaProperty, // MetaProperty ::= NewTarget
	NewTarget, // NewTarget ::= 'new' '.' 'target'
	NewExpression, // NewExpression ::= MemberExpression
	NewExpression, // NewExpression ::= 'new' NewExpression
	NewExpression, // NewExpression_NoFuncClass ::= MemberExpression_NoFuncClass
	NewExpression, // NewExpression_NoFuncClass ::= 'new' NewExpression
	NewExpression, // NewExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral
	NewExpression, // NewExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= 'new' NewExpression
	NewExpression, // NewExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	NewExpression, // NewExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= 'new' NewExpression_Yield
	NewExpression, // NewExpression_NoLet ::= MemberExpression_NoLet
	NewExpression, // NewExpression_NoLet ::= 'new' NewExpression
	NewExpression, // NewExpression_NoLet_Yield ::= MemberExpression_NoLet_Yield
	NewExpression, // NewExpression_NoLet_Yield ::= 'new' NewExpression_Yield
	NewExpression, // NewExpression_NoObjLiteral ::= MemberExpression_NoObjLiteral
	NewExpression, // NewExpression_NoObjLiteral ::= 'new' NewExpression
	NewExpression, // NewExpression_StartWithLet ::= MemberExpression_StartWithLet
	NewExpression, // NewExpression_StartWithLet_Yield ::= MemberExpression_StartWithLet_Yield
	NewExpression, // NewExpression_Yield ::= MemberExpression_Yield
	NewExpression, // NewExpression_Yield ::= 'new' NewExpression_Yield
	CallExpression, // CallExpression ::= MemberExpression Arguments
	CallExpression, // CallExpression ::= SuperCall
	CallExpression, // CallExpression ::= CallExpression Arguments
	CallExpression, // CallExpression ::= CallExpression '[' Expression_In ']'
	CallExpression, // CallExpression ::= CallExpression '.' IdentifierName
	CallExpression, // CallExpression ::= CallExpression TemplateLiteral
	CallExpression, // CallExpression_NoFuncClass ::= MemberExpression_NoFuncClass Arguments
	CallExpression, // CallExpression_NoFuncClass ::= SuperCall
	CallExpression, // CallExpression_NoFuncClass ::= CallExpression_NoFuncClass Arguments
	CallExpression, // CallExpression_NoFuncClass ::= CallExpression_NoFuncClass '[' Expression_In ']'
	CallExpression, // CallExpression_NoFuncClass ::= CallExpression_NoFuncClass '.' IdentifierName
	CallExpression, // CallExpression_NoFuncClass ::= CallExpression_NoFuncClass TemplateLiteral
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral Arguments
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= SuperCall
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral Arguments
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral '[' Expression_In ']'
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral '.' IdentifierName
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Arguments_Yield
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= SuperCall_Yield
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Arguments_Yield
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '[' Expression_In_Yield ']'
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '.' IdentifierName
	CallExpression, // CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
	CallExpression, // CallExpression_NoLet ::= MemberExpression_NoLet Arguments
	CallExpression, // CallExpression_NoLet ::= SuperCall
	CallExpression, // CallExpression_NoLet ::= CallExpression_NoLet Arguments
	CallExpression, // CallExpression_NoLet ::= CallExpression_NoLet '[' Expression_In ']'
	CallExpression, // CallExpression_NoLet ::= CallExpression_NoLet '.' IdentifierName
	CallExpression, // CallExpression_NoLet ::= CallExpression_NoLet TemplateLiteral
	CallExpression, // CallExpression_NoLet_Yield ::= MemberExpression_NoLet_Yield Arguments_Yield
	CallExpression, // CallExpression_NoLet_Yield ::= SuperCall_Yield
	CallExpression, // CallExpression_NoLet_Yield ::= CallExpression_NoLet_Yield Arguments_Yield
	CallExpression, // CallExpression_NoLet_Yield ::= CallExpression_NoLet_Yield '[' Expression_In_Yield ']'
	CallExpression, // CallExpression_NoLet_Yield ::= CallExpression_NoLet_Yield '.' IdentifierName
	CallExpression, // CallExpression_NoLet_Yield ::= CallExpression_NoLet_Yield TemplateLiteral_Yield
	CallExpression, // CallExpression_NoObjLiteral ::= MemberExpression_NoObjLiteral Arguments
	CallExpression, // CallExpression_NoObjLiteral ::= SuperCall
	CallExpression, // CallExpression_NoObjLiteral ::= CallExpression_NoObjLiteral Arguments
	CallExpression, // CallExpression_NoObjLiteral ::= CallExpression_NoObjLiteral '[' Expression_In ']'
	CallExpression, // CallExpression_NoObjLiteral ::= CallExpression_NoObjLiteral '.' IdentifierName
	CallExpression, // CallExpression_NoObjLiteral ::= CallExpression_NoObjLiteral TemplateLiteral
	CallExpression, // CallExpression_StartWithLet ::= MemberExpression_StartWithLet Arguments
	CallExpression, // CallExpression_StartWithLet ::= CallExpression_StartWithLet Arguments
	CallExpression, // CallExpression_StartWithLet ::= CallExpression_StartWithLet '[' Expression_In ']'
	CallExpression, // CallExpression_StartWithLet ::= CallExpression_StartWithLet '.' IdentifierName
	CallExpression, // CallExpression_StartWithLet ::= CallExpression_StartWithLet TemplateLiteral
	CallExpression, // CallExpression_StartWithLet_Yield ::= MemberExpression_StartWithLet_Yield Arguments_Yield
	CallExpression, // CallExpression_StartWithLet_Yield ::= CallExpression_StartWithLet_Yield Arguments_Yield
	CallExpression, // CallExpression_StartWithLet_Yield ::= CallExpression_StartWithLet_Yield '[' Expression_In_Yield ']'
	CallExpression, // CallExpression_StartWithLet_Yield ::= CallExpression_StartWithLet_Yield '.' IdentifierName
	CallExpression, // CallExpression_StartWithLet_Yield ::= CallExpression_StartWithLet_Yield TemplateLiteral_Yield
	CallExpression, // CallExpression_Yield ::= MemberExpression_Yield Arguments_Yield
	CallExpression, // CallExpression_Yield ::= SuperCall_Yield
	CallExpression, // CallExpression_Yield ::= CallExpression_Yield Arguments_Yield
	CallExpression, // CallExpression_Yield ::= CallExpression_Yield '[' Expression_In_Yield ']'
	CallExpression, // CallExpression_Yield ::= CallExpression_Yield '.' IdentifierName
	CallExpression, // CallExpression_Yield ::= CallExpression_Yield TemplateLiteral_Yield
	SuperCall, // SuperCall ::= 'super' Arguments
	SuperCall, // SuperCall_Yield ::= 'super' Arguments_Yield
	Arguments, // Arguments ::= '(' ')'
	Arguments, // Arguments ::= '(' ArgumentList ')'
	Arguments, // Arguments_Yield ::= '(' ')'
	Arguments, // Arguments_Yield ::= '(' ArgumentList_Yield ')'
	0, // ArgumentList ::= AssignmentExpression_In
	0, // ArgumentList ::= '.' '.' '.' AssignmentExpression_In
	0, // ArgumentList ::= ArgumentList ',' AssignmentExpression_In
	0, // ArgumentList ::= ArgumentList ',' '.' '.' '.' AssignmentExpression_In
	0, // ArgumentList_Yield ::= AssignmentExpression_In_Yield
	0, // ArgumentList_Yield ::= '.' '.' '.' AssignmentExpression_In_Yield
	0, // ArgumentList_Yield ::= ArgumentList_Yield ',' AssignmentExpression_In_Yield
	0, // ArgumentList_Yield ::= ArgumentList_Yield ',' '.' '.' '.' AssignmentExpression_In_Yield
	LeftHandSideExpression, // LeftHandSideExpression ::= NewExpression
	LeftHandSideExpression, // LeftHandSideExpression ::= CallExpression
	LeftHandSideExpression, // LeftHandSideExpression_NoFuncClass ::= NewExpression_NoFuncClass
	LeftHandSideExpression, // LeftHandSideExpression_NoFuncClass ::= CallExpression_NoFuncClass
	LeftHandSideExpression, // LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= NewExpression_NoFuncClass_NoLetSq_NoObjLiteral
	LeftHandSideExpression, // LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral
	LeftHandSideExpression, // LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= NewExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	LeftHandSideExpression, // LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	LeftHandSideExpression, // LeftHandSideExpression_NoLet ::= NewExpression_NoLet
	LeftHandSideExpression, // LeftHandSideExpression_NoLet ::= CallExpression_NoLet
	LeftHandSideExpression, // LeftHandSideExpression_NoLet_Yield ::= NewExpression_NoLet_Yield
	LeftHandSideExpression, // LeftHandSideExpression_NoLet_Yield ::= CallExpression_NoLet_Yield
	LeftHandSideExpression, // LeftHandSideExpression_NoObjLiteral ::= NewExpression_NoObjLiteral
	LeftHandSideExpression, // LeftHandSideExpression_NoObjLiteral ::= CallExpression_NoObjLiteral
	LeftHandSideExpression, // LeftHandSideExpression_StartWithLet ::= NewExpression_StartWithLet
	LeftHandSideExpression, // LeftHandSideExpression_StartWithLet ::= CallExpression_StartWithLet
	LeftHandSideExpression, // LeftHandSideExpression_StartWithLet_Yield ::= NewExpression_StartWithLet_Yield
	LeftHandSideExpression, // LeftHandSideExpression_StartWithLet_Yield ::= CallExpression_StartWithLet_Yield
	LeftHandSideExpression, // LeftHandSideExpression_Yield ::= NewExpression_Yield
	LeftHandSideExpression, // LeftHandSideExpression_Yield ::= CallExpression_Yield
	PostfixExpression, // PostfixExpression ::= LeftHandSideExpression
	PostfixExpression, // PostfixExpression ::= LeftHandSideExpression '++'
	PostfixExpression, // PostfixExpression ::= LeftHandSideExpression '--'
	PostfixExpression, // PostfixExpression_NoFuncClass ::= LeftHandSideExpression_NoFuncClass
	PostfixExpression, // PostfixExpression_NoFuncClass ::= LeftHandSideExpression_NoFuncClass '++'
	PostfixExpression, // PostfixExpression_NoFuncClass ::= LeftHandSideExpression_NoFuncClass '--'
	PostfixExpression, // PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral
	PostfixExpression, // PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral '++'
	PostfixExpression, // PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral '--'
	PostfixExpression, // PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	PostfixExpression, // PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '++'
	PostfixExpression, // PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '--'
	PostfixExpression, // PostfixExpression_NoLet ::= LeftHandSideExpression_NoLet
	PostfixExpression, // PostfixExpression_NoLet ::= LeftHandSideExpression_NoLet '++'
	PostfixExpression, // PostfixExpression_NoLet ::= LeftHandSideExpression_NoLet '--'
	PostfixExpression, // PostfixExpression_NoLet_Yield ::= LeftHandSideExpression_NoLet_Yield
	PostfixExpression, // PostfixExpression_NoLet_Yield ::= LeftHandSideExpression_NoLet_Yield '++'
	PostfixExpression, // PostfixExpression_NoLet_Yield ::= LeftHandSideExpression_NoLet_Yield '--'
	PostfixExpression, // PostfixExpression_NoObjLiteral ::= LeftHandSideExpression_NoObjLiteral
	PostfixExpression, // PostfixExpression_NoObjLiteral ::= LeftHandSideExpression_NoObjLiteral '++'
	PostfixExpression, // PostfixExpression_NoObjLiteral ::= LeftHandSideExpression_NoObjLiteral '--'
	PostfixExpression, // PostfixExpression_StartWithLet ::= LeftHandSideExpression_StartWithLet
	PostfixExpression, // PostfixExpression_StartWithLet ::= LeftHandSideExpression_StartWithLet '++'
	PostfixExpression, // PostfixExpression_StartWithLet ::= LeftHandSideExpression_StartWithLet '--'
	PostfixExpression, // PostfixExpression_StartWithLet_Yield ::= LeftHandSideExpression_StartWithLet_Yield
	PostfixExpression, // PostfixExpression_StartWithLet_Yield ::= LeftHandSideExpression_StartWithLet_Yield '++'
	PostfixExpression, // PostfixExpression_StartWithLet_Yield ::= LeftHandSideExpression_StartWithLet_Yield '--'
	PostfixExpression, // PostfixExpression_Yield ::= LeftHandSideExpression_Yield
	PostfixExpression, // PostfixExpression_Yield ::= LeftHandSideExpression_Yield '++'
	PostfixExpression, // PostfixExpression_Yield ::= LeftHandSideExpression_Yield '--'
	UnaryExpression, // UnaryExpression ::= PostfixExpression
	UnaryExpression, // UnaryExpression ::= 'delete' UnaryExpression
	UnaryExpression, // UnaryExpression ::= 'void' UnaryExpression
	UnaryExpression, // UnaryExpression ::= 'typeof' UnaryExpression
	UnaryExpression, // UnaryExpression ::= '++' UnaryExpression
	UnaryExpression, // UnaryExpression ::= '--' UnaryExpression
	UnaryExpression, // UnaryExpression ::= '+' UnaryExpression
	UnaryExpression, // UnaryExpression ::= '-' UnaryExpression
	UnaryExpression, // UnaryExpression ::= '~' UnaryExpression
	UnaryExpression, // UnaryExpression ::= '!' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= PostfixExpression_NoFuncClass
	UnaryExpression, // UnaryExpression_NoFuncClass ::= 'delete' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= 'void' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= 'typeof' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= '++' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= '--' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= '+' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= '-' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= '~' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass ::= '!' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= 'delete' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= 'void' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= 'typeof' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= '++' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= '--' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= '+' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= '-' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= '~' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= '!' UnaryExpression
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= 'delete' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= 'void' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= 'typeof' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= '++' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= '--' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= '+' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= '-' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= '~' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= '!' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet ::= PostfixExpression_NoLet
	UnaryExpression, // UnaryExpression_NoLet ::= 'delete' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= 'void' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= 'typeof' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= '++' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= '--' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= '+' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= '-' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= '~' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet ::= '!' UnaryExpression
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= PostfixExpression_NoLet_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= 'delete' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= 'void' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= 'typeof' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= '++' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= '--' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= '+' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= '-' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= '~' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoLet_Yield ::= '!' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= PostfixExpression_NoObjLiteral
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= 'delete' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= 'void' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= 'typeof' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= '++' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= '--' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= '+' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= '-' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= '~' UnaryExpression
	UnaryExpression, // UnaryExpression_NoObjLiteral ::= '!' UnaryExpression
	UnaryExpression, // UnaryExpression_StartWithLet ::= PostfixExpression_StartWithLet
	UnaryExpression, // UnaryExpression_StartWithLet_Yield ::= PostfixExpression_StartWithLet_Yield
	UnaryExpression, // UnaryExpression_Yield ::= PostfixExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= 'delete' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= 'void' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= 'typeof' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= '++' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= '--' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= '+' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= '-' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= '~' UnaryExpression_Yield
	UnaryExpression, // UnaryExpression_Yield ::= '!' UnaryExpression_Yield
	MultiplicativeExpression, // MultiplicativeExpression ::= UnaryExpression
	MultiplicativeExpression, // MultiplicativeExpression ::= MultiplicativeExpression MultiplicativeOperator UnaryExpression
	MultiplicativeExpression, // MultiplicativeExpression_NoFuncClass ::= UnaryExpression_NoFuncClass
	MultiplicativeExpression, // MultiplicativeExpression_NoFuncClass ::= MultiplicativeExpression_NoFuncClass MultiplicativeOperator UnaryExpression
	MultiplicativeExpression, // MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral
	MultiplicativeExpression, // MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral MultiplicativeOperator UnaryExpression
	MultiplicativeExpression, // MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	MultiplicativeExpression, // MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield MultiplicativeOperator UnaryExpression_Yield
	MultiplicativeExpression, // MultiplicativeExpression_NoLet ::= UnaryExpression_NoLet
	MultiplicativeExpression, // MultiplicativeExpression_NoLet ::= MultiplicativeExpression_NoLet MultiplicativeOperator UnaryExpression
	MultiplicativeExpression, // MultiplicativeExpression_NoLet_Yield ::= UnaryExpression_NoLet_Yield
	MultiplicativeExpression, // MultiplicativeExpression_NoLet_Yield ::= MultiplicativeExpression_NoLet_Yield MultiplicativeOperator UnaryExpression_Yield
	MultiplicativeExpression, // MultiplicativeExpression_NoObjLiteral ::= UnaryExpression_NoObjLiteral
	MultiplicativeExpression, // MultiplicativeExpression_NoObjLiteral ::= MultiplicativeExpression_NoObjLiteral MultiplicativeOperator UnaryExpression
	MultiplicativeExpression, // MultiplicativeExpression_StartWithLet ::= UnaryExpression_StartWithLet
	MultiplicativeExpression, // MultiplicativeExpression_StartWithLet ::= MultiplicativeExpression_StartWithLet MultiplicativeOperator UnaryExpression
	MultiplicativeExpression, // MultiplicativeExpression_StartWithLet_Yield ::= UnaryExpression_StartWithLet_Yield
	MultiplicativeExpression, // MultiplicativeExpression_StartWithLet_Yield ::= MultiplicativeExpression_StartWithLet_Yield MultiplicativeOperator UnaryExpression_Yield
	MultiplicativeExpression, // MultiplicativeExpression_Yield ::= UnaryExpression_Yield
	MultiplicativeExpression, // MultiplicativeExpression_Yield ::= MultiplicativeExpression_Yield MultiplicativeOperator UnaryExpression_Yield
	MultiplicativeOperator, // MultiplicativeOperator ::= '*'
	MultiplicativeOperator, // MultiplicativeOperator ::= '/'
	MultiplicativeOperator, // MultiplicativeOperator ::= '%'
	AdditiveExpression, // AdditiveExpression ::= MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression ::= AdditiveExpression '+' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression ::= AdditiveExpression '-' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoFuncClass ::= MultiplicativeExpression_NoFuncClass
	AdditiveExpression, // AdditiveExpression_NoFuncClass ::= AdditiveExpression_NoFuncClass '+' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoFuncClass ::= AdditiveExpression_NoFuncClass '-' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral
	AdditiveExpression, // AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral '+' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral '-' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	AdditiveExpression, // AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '+' MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '-' MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_NoLet ::= MultiplicativeExpression_NoLet
	AdditiveExpression, // AdditiveExpression_NoLet ::= AdditiveExpression_NoLet '+' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoLet ::= AdditiveExpression_NoLet '-' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoLet_Yield ::= MultiplicativeExpression_NoLet_Yield
	AdditiveExpression, // AdditiveExpression_NoLet_Yield ::= AdditiveExpression_NoLet_Yield '+' MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_NoLet_Yield ::= AdditiveExpression_NoLet_Yield '-' MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_NoObjLiteral ::= MultiplicativeExpression_NoObjLiteral
	AdditiveExpression, // AdditiveExpression_NoObjLiteral ::= AdditiveExpression_NoObjLiteral '+' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_NoObjLiteral ::= AdditiveExpression_NoObjLiteral '-' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_StartWithLet ::= MultiplicativeExpression_StartWithLet
	AdditiveExpression, // AdditiveExpression_StartWithLet ::= AdditiveExpression_StartWithLet '+' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_StartWithLet ::= AdditiveExpression_StartWithLet '-' MultiplicativeExpression
	AdditiveExpression, // AdditiveExpression_StartWithLet_Yield ::= MultiplicativeExpression_StartWithLet_Yield
	AdditiveExpression, // AdditiveExpression_StartWithLet_Yield ::= AdditiveExpression_StartWithLet_Yield '+' MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_StartWithLet_Yield ::= AdditiveExpression_StartWithLet_Yield '-' MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_Yield ::= MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_Yield ::= AdditiveExpression_Yield '+' MultiplicativeExpression_Yield
	AdditiveExpression, // AdditiveExpression_Yield ::= AdditiveExpression_Yield '-' MultiplicativeExpression_Yield
	ShiftExpression, // ShiftExpression ::= AdditiveExpression
	ShiftExpression, // ShiftExpression ::= ShiftExpression '<<' AdditiveExpression
	ShiftExpression, // ShiftExpression ::= ShiftExpression '>>' AdditiveExpression
	ShiftExpression, // ShiftExpression ::= ShiftExpression '>>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoFuncClass ::= AdditiveExpression_NoFuncClass
	ShiftExpression, // ShiftExpression_NoFuncClass ::= ShiftExpression_NoFuncClass '<<' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoFuncClass ::= ShiftExpression_NoFuncClass '>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoFuncClass ::= ShiftExpression_NoFuncClass '>>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral '<<' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral '>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral '>>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '<<' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '>>' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '>>>' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_NoLet ::= AdditiveExpression_NoLet
	ShiftExpression, // ShiftExpression_NoLet ::= ShiftExpression_NoLet '<<' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoLet ::= ShiftExpression_NoLet '>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoLet ::= ShiftExpression_NoLet '>>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoLet_Yield ::= AdditiveExpression_NoLet_Yield
	ShiftExpression, // ShiftExpression_NoLet_Yield ::= ShiftExpression_NoLet_Yield '<<' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_NoLet_Yield ::= ShiftExpression_NoLet_Yield '>>' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_NoLet_Yield ::= ShiftExpression_NoLet_Yield '>>>' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_NoObjLiteral ::= AdditiveExpression_NoObjLiteral
	ShiftExpression, // ShiftExpression_NoObjLiteral ::= ShiftExpression_NoObjLiteral '<<' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoObjLiteral ::= ShiftExpression_NoObjLiteral '>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_NoObjLiteral ::= ShiftExpression_NoObjLiteral '>>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_StartWithLet ::= AdditiveExpression_StartWithLet
	ShiftExpression, // ShiftExpression_StartWithLet ::= ShiftExpression_StartWithLet '<<' AdditiveExpression
	ShiftExpression, // ShiftExpression_StartWithLet ::= ShiftExpression_StartWithLet '>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_StartWithLet ::= ShiftExpression_StartWithLet '>>>' AdditiveExpression
	ShiftExpression, // ShiftExpression_StartWithLet_Yield ::= AdditiveExpression_StartWithLet_Yield
	ShiftExpression, // ShiftExpression_StartWithLet_Yield ::= ShiftExpression_StartWithLet_Yield '<<' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_StartWithLet_Yield ::= ShiftExpression_StartWithLet_Yield '>>' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_StartWithLet_Yield ::= ShiftExpression_StartWithLet_Yield '>>>' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_Yield ::= AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_Yield ::= ShiftExpression_Yield '<<' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_Yield ::= ShiftExpression_Yield '>>' AdditiveExpression_Yield
	ShiftExpression, // ShiftExpression_Yield ::= ShiftExpression_Yield '>>>' AdditiveExpression_Yield
	RelationalExpression, // RelationalExpression ::= ShiftExpression
	RelationalExpression, // RelationalExpression ::= RelationalExpression '<' ShiftExpression
	RelationalExpression, // RelationalExpression ::= RelationalExpression '>' ShiftExpression
	RelationalExpression, // RelationalExpression ::= RelationalExpression '<=' ShiftExpression
	RelationalExpression, // RelationalExpression ::= RelationalExpression '>=' ShiftExpression
	RelationalExpression, // RelationalExpression ::= RelationalExpression 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_In ::= ShiftExpression
	RelationalExpression, // RelationalExpression_In ::= RelationalExpression_In '<' ShiftExpression
	RelationalExpression, // RelationalExpression_In ::= RelationalExpression_In '>' ShiftExpression
	RelationalExpression, // RelationalExpression_In ::= RelationalExpression_In '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_In ::= RelationalExpression_In '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_In ::= RelationalExpression_In 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_In ::= RelationalExpression_In 'in' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass ::= ShiftExpression_NoFuncClass
	RelationalExpression, // RelationalExpression_In_NoFuncClass ::= RelationalExpression_In_NoFuncClass '<' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass ::= RelationalExpression_In_NoFuncClass '>' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass ::= RelationalExpression_In_NoFuncClass '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass ::= RelationalExpression_In_NoFuncClass '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass ::= RelationalExpression_In_NoFuncClass 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass ::= RelationalExpression_In_NoFuncClass 'in' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '<' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '>' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral 'in' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '<' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '>' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '<=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '>=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield 'instanceof' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield 'in' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoLet ::= ShiftExpression_NoLet
	RelationalExpression, // RelationalExpression_In_NoLet ::= RelationalExpression_In_NoLet '<' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoLet ::= RelationalExpression_In_NoLet '>' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoLet ::= RelationalExpression_In_NoLet '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoLet ::= RelationalExpression_In_NoLet '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoLet ::= RelationalExpression_In_NoLet 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoLet ::= RelationalExpression_In_NoLet 'in' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoLet_Yield ::= ShiftExpression_NoLet_Yield
	RelationalExpression, // RelationalExpression_In_NoLet_Yield ::= RelationalExpression_In_NoLet_Yield '<' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoLet_Yield ::= RelationalExpression_In_NoLet_Yield '>' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoLet_Yield ::= RelationalExpression_In_NoLet_Yield '<=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoLet_Yield ::= RelationalExpression_In_NoLet_Yield '>=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoLet_Yield ::= RelationalExpression_In_NoLet_Yield 'instanceof' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoLet_Yield ::= RelationalExpression_In_NoLet_Yield 'in' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_NoObjLiteral ::= ShiftExpression_NoObjLiteral
	RelationalExpression, // RelationalExpression_In_NoObjLiteral ::= RelationalExpression_In_NoObjLiteral '<' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoObjLiteral ::= RelationalExpression_In_NoObjLiteral '>' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoObjLiteral ::= RelationalExpression_In_NoObjLiteral '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoObjLiteral ::= RelationalExpression_In_NoObjLiteral '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoObjLiteral ::= RelationalExpression_In_NoObjLiteral 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_In_NoObjLiteral ::= RelationalExpression_In_NoObjLiteral 'in' ShiftExpression
	RelationalExpression, // RelationalExpression_In_StartWithLet ::= ShiftExpression_StartWithLet
	RelationalExpression, // RelationalExpression_In_StartWithLet ::= RelationalExpression_In_StartWithLet '<' ShiftExpression
	RelationalExpression, // RelationalExpression_In_StartWithLet ::= RelationalExpression_In_StartWithLet '>' ShiftExpression
	RelationalExpression, // RelationalExpression_In_StartWithLet ::= RelationalExpression_In_StartWithLet '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_StartWithLet ::= RelationalExpression_In_StartWithLet '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_In_StartWithLet ::= RelationalExpression_In_StartWithLet 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_In_StartWithLet ::= RelationalExpression_In_StartWithLet 'in' ShiftExpression
	RelationalExpression, // RelationalExpression_In_StartWithLet_Yield ::= ShiftExpression_StartWithLet_Yield
	RelationalExpression, // RelationalExpression_In_StartWithLet_Yield ::= RelationalExpression_In_StartWithLet_Yield '<' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_StartWithLet_Yield ::= RelationalExpression_In_StartWithLet_Yield '>' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_StartWithLet_Yield ::= RelationalExpression_In_StartWithLet_Yield '<=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_StartWithLet_Yield ::= RelationalExpression_In_StartWithLet_Yield '>=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_StartWithLet_Yield ::= RelationalExpression_In_StartWithLet_Yield 'instanceof' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_StartWithLet_Yield ::= RelationalExpression_In_StartWithLet_Yield 'in' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_Yield ::= ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_Yield ::= RelationalExpression_In_Yield '<' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_Yield ::= RelationalExpression_In_Yield '>' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_Yield ::= RelationalExpression_In_Yield '<=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_Yield ::= RelationalExpression_In_Yield '>=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_Yield ::= RelationalExpression_In_Yield 'instanceof' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_In_Yield ::= RelationalExpression_In_Yield 'in' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_NoLet ::= ShiftExpression_NoLet
	RelationalExpression, // RelationalExpression_NoLet ::= RelationalExpression_NoLet '<' ShiftExpression
	RelationalExpression, // RelationalExpression_NoLet ::= RelationalExpression_NoLet '>' ShiftExpression
	RelationalExpression, // RelationalExpression_NoLet ::= RelationalExpression_NoLet '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_NoLet ::= RelationalExpression_NoLet '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_NoLet ::= RelationalExpression_NoLet 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_NoLet_Yield ::= ShiftExpression_NoLet_Yield
	RelationalExpression, // RelationalExpression_NoLet_Yield ::= RelationalExpression_NoLet_Yield '<' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_NoLet_Yield ::= RelationalExpression_NoLet_Yield '>' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_NoLet_Yield ::= RelationalExpression_NoLet_Yield '<=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_NoLet_Yield ::= RelationalExpression_NoLet_Yield '>=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_NoLet_Yield ::= RelationalExpression_NoLet_Yield 'instanceof' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_NoObjLiteral ::= ShiftExpression_NoObjLiteral
	RelationalExpression, // RelationalExpression_NoObjLiteral ::= RelationalExpression_NoObjLiteral '<' ShiftExpression
	RelationalExpression, // RelationalExpression_NoObjLiteral ::= RelationalExpression_NoObjLiteral '>' ShiftExpression
	RelationalExpression, // RelationalExpression_NoObjLiteral ::= RelationalExpression_NoObjLiteral '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_NoObjLiteral ::= RelationalExpression_NoObjLiteral '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_NoObjLiteral ::= RelationalExpression_NoObjLiteral 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_StartWithLet ::= ShiftExpression_StartWithLet
	RelationalExpression, // RelationalExpression_StartWithLet ::= RelationalExpression_StartWithLet '<' ShiftExpression
	RelationalExpression, // RelationalExpression_StartWithLet ::= RelationalExpression_StartWithLet '>' ShiftExpression
	RelationalExpression, // RelationalExpression_StartWithLet ::= RelationalExpression_StartWithLet '<=' ShiftExpression
	RelationalExpression, // RelationalExpression_StartWithLet ::= RelationalExpression_StartWithLet '>=' ShiftExpression
	RelationalExpression, // RelationalExpression_StartWithLet ::= RelationalExpression_StartWithLet 'instanceof' ShiftExpression
	RelationalExpression, // RelationalExpression_StartWithLet_Yield ::= ShiftExpression_StartWithLet_Yield
	RelationalExpression, // RelationalExpression_StartWithLet_Yield ::= RelationalExpression_StartWithLet_Yield '<' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_StartWithLet_Yield ::= RelationalExpression_StartWithLet_Yield '>' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_StartWithLet_Yield ::= RelationalExpression_StartWithLet_Yield '<=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_StartWithLet_Yield ::= RelationalExpression_StartWithLet_Yield '>=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_StartWithLet_Yield ::= RelationalExpression_StartWithLet_Yield 'instanceof' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_Yield ::= ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_Yield ::= RelationalExpression_Yield '<' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_Yield ::= RelationalExpression_Yield '>' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_Yield ::= RelationalExpression_Yield '<=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_Yield ::= RelationalExpression_Yield '>=' ShiftExpression_Yield
	RelationalExpression, // RelationalExpression_Yield ::= RelationalExpression_Yield 'instanceof' ShiftExpression_Yield
	EqualityExpression, // EqualityExpression ::= RelationalExpression
	EqualityExpression, // EqualityExpression ::= EqualityExpression '==' RelationalExpression
	EqualityExpression, // EqualityExpression ::= EqualityExpression '!=' RelationalExpression
	EqualityExpression, // EqualityExpression ::= EqualityExpression '===' RelationalExpression
	EqualityExpression, // EqualityExpression ::= EqualityExpression '!==' RelationalExpression
	EqualityExpression, // EqualityExpression_In ::= RelationalExpression_In
	EqualityExpression, // EqualityExpression_In ::= EqualityExpression_In '==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In ::= EqualityExpression_In '!=' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In ::= EqualityExpression_In '===' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In ::= EqualityExpression_In '!==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass ::= RelationalExpression_In_NoFuncClass
	EqualityExpression, // EqualityExpression_In_NoFuncClass ::= EqualityExpression_In_NoFuncClass '==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass ::= EqualityExpression_In_NoFuncClass '!=' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass ::= EqualityExpression_In_NoFuncClass '===' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass ::= EqualityExpression_In_NoFuncClass '!==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '!=' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '===' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '!==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '==' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '!=' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '===' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '!==' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_NoObjLiteral ::= RelationalExpression_In_NoObjLiteral
	EqualityExpression, // EqualityExpression_In_NoObjLiteral ::= EqualityExpression_In_NoObjLiteral '==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoObjLiteral ::= EqualityExpression_In_NoObjLiteral '!=' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoObjLiteral ::= EqualityExpression_In_NoObjLiteral '===' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_NoObjLiteral ::= EqualityExpression_In_NoObjLiteral '!==' RelationalExpression_In
	EqualityExpression, // EqualityExpression_In_Yield ::= RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_Yield ::= EqualityExpression_In_Yield '==' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_Yield ::= EqualityExpression_In_Yield '!=' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_Yield ::= EqualityExpression_In_Yield '===' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_In_Yield ::= EqualityExpression_In_Yield '!==' RelationalExpression_In_Yield
	EqualityExpression, // EqualityExpression_NoLet ::= RelationalExpression_NoLet
	EqualityExpression, // EqualityExpression_NoLet ::= EqualityExpression_NoLet '==' RelationalExpression
	EqualityExpression, // EqualityExpression_NoLet ::= EqualityExpression_NoLet '!=' RelationalExpression
	EqualityExpression, // EqualityExpression_NoLet ::= EqualityExpression_NoLet '===' RelationalExpression
	EqualityExpression, // EqualityExpression_NoLet ::= EqualityExpression_NoLet '!==' RelationalExpression
	EqualityExpression, // EqualityExpression_NoLet_Yield ::= RelationalExpression_NoLet_Yield
	EqualityExpression, // EqualityExpression_NoLet_Yield ::= EqualityExpression_NoLet_Yield '==' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_NoLet_Yield ::= EqualityExpression_NoLet_Yield '!=' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_NoLet_Yield ::= EqualityExpression_NoLet_Yield '===' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_NoLet_Yield ::= EqualityExpression_NoLet_Yield '!==' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_NoObjLiteral ::= RelationalExpression_NoObjLiteral
	EqualityExpression, // EqualityExpression_NoObjLiteral ::= EqualityExpression_NoObjLiteral '==' RelationalExpression
	EqualityExpression, // EqualityExpression_NoObjLiteral ::= EqualityExpression_NoObjLiteral '!=' RelationalExpression
	EqualityExpression, // EqualityExpression_NoObjLiteral ::= EqualityExpression_NoObjLiteral '===' RelationalExpression
	EqualityExpression, // EqualityExpression_NoObjLiteral ::= EqualityExpression_NoObjLiteral '!==' RelationalExpression
	EqualityExpression, // EqualityExpression_StartWithLet ::= RelationalExpression_StartWithLet
	EqualityExpression, // EqualityExpression_StartWithLet ::= EqualityExpression_StartWithLet '==' RelationalExpression
	EqualityExpression, // EqualityExpression_StartWithLet ::= EqualityExpression_StartWithLet '!=' RelationalExpression
	EqualityExpression, // EqualityExpression_StartWithLet ::= EqualityExpression_StartWithLet '===' RelationalExpression
	EqualityExpression, // EqualityExpression_StartWithLet ::= EqualityExpression_StartWithLet '!==' RelationalExpression
	EqualityExpression, // EqualityExpression_StartWithLet_Yield ::= RelationalExpression_StartWithLet_Yield
	EqualityExpression, // EqualityExpression_StartWithLet_Yield ::= EqualityExpression_StartWithLet_Yield '==' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_StartWithLet_Yield ::= EqualityExpression_StartWithLet_Yield '!=' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_StartWithLet_Yield ::= EqualityExpression_StartWithLet_Yield '===' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_StartWithLet_Yield ::= EqualityExpression_StartWithLet_Yield '!==' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_Yield ::= RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_Yield ::= EqualityExpression_Yield '==' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_Yield ::= EqualityExpression_Yield '!=' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_Yield ::= EqualityExpression_Yield '===' RelationalExpression_Yield
	EqualityExpression, // EqualityExpression_Yield ::= EqualityExpression_Yield '!==' RelationalExpression_Yield
	0, // BitwiseANDExpression ::= EqualityExpression
	0, // BitwiseANDExpression ::= BitwiseANDExpression '&' EqualityExpression
	0, // BitwiseANDExpression_In ::= EqualityExpression_In
	0, // BitwiseANDExpression_In ::= BitwiseANDExpression_In '&' EqualityExpression_In
	BitwiseANDExpression, // BitwiseANDExpression_In_NoFuncClass ::= EqualityExpression_In_NoFuncClass
	BitwiseANDExpression, // BitwiseANDExpression_In_NoFuncClass ::= BitwiseANDExpression_In_NoFuncClass '&' EqualityExpression_In
	BitwiseANDExpression, // BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	BitwiseANDExpression, // BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '&' EqualityExpression_In
	BitwiseANDExpression, // BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	BitwiseANDExpression, // BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '&' EqualityExpression_In_Yield
	BitwiseANDExpression, // BitwiseANDExpression_In_NoObjLiteral ::= EqualityExpression_In_NoObjLiteral
	BitwiseANDExpression, // BitwiseANDExpression_In_NoObjLiteral ::= BitwiseANDExpression_In_NoObjLiteral '&' EqualityExpression_In
	0, // BitwiseANDExpression_In_Yield ::= EqualityExpression_In_Yield
	0, // BitwiseANDExpression_In_Yield ::= BitwiseANDExpression_In_Yield '&' EqualityExpression_In_Yield
	BitwiseANDExpression, // BitwiseANDExpression_NoLet ::= EqualityExpression_NoLet
	BitwiseANDExpression, // BitwiseANDExpression_NoLet ::= BitwiseANDExpression_NoLet '&' EqualityExpression
	BitwiseANDExpression, // BitwiseANDExpression_NoLet_Yield ::= EqualityExpression_NoLet_Yield
	BitwiseANDExpression, // BitwiseANDExpression_NoLet_Yield ::= BitwiseANDExpression_NoLet_Yield '&' EqualityExpression_Yield
	BitwiseANDExpression, // BitwiseANDExpression_NoObjLiteral ::= EqualityExpression_NoObjLiteral
	BitwiseANDExpression, // BitwiseANDExpression_NoObjLiteral ::= BitwiseANDExpression_NoObjLiteral '&' EqualityExpression
	BitwiseANDExpression, // BitwiseANDExpression_StartWithLet ::= EqualityExpression_StartWithLet
	BitwiseANDExpression, // BitwiseANDExpression_StartWithLet ::= BitwiseANDExpression_StartWithLet '&' EqualityExpression
	BitwiseANDExpression, // BitwiseANDExpression_StartWithLet_Yield ::= EqualityExpression_StartWithLet_Yield
	BitwiseANDExpression, // BitwiseANDExpression_StartWithLet_Yield ::= BitwiseANDExpression_StartWithLet_Yield '&' EqualityExpression_Yield
	0, // BitwiseANDExpression_Yield ::= EqualityExpression_Yield
	0, // BitwiseANDExpression_Yield ::= BitwiseANDExpression_Yield '&' EqualityExpression_Yield
	0, // BitwiseXORExpression ::= BitwiseANDExpression
	0, // BitwiseXORExpression ::= BitwiseXORExpression '^' BitwiseANDExpression
	0, // BitwiseXORExpression_In ::= BitwiseANDExpression_In
	0, // BitwiseXORExpression_In ::= BitwiseXORExpression_In '^' BitwiseANDExpression_In
	BitwiseXORExpression, // BitwiseXORExpression_In_NoFuncClass ::= BitwiseANDExpression_In_NoFuncClass
	BitwiseXORExpression, // BitwiseXORExpression_In_NoFuncClass ::= BitwiseXORExpression_In_NoFuncClass '^' BitwiseANDExpression_In
	BitwiseXORExpression, // BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	BitwiseXORExpression, // BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '^' BitwiseANDExpression_In
	BitwiseXORExpression, // BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	BitwiseXORExpression, // BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '^' BitwiseANDExpression_In_Yield
	BitwiseXORExpression, // BitwiseXORExpression_In_NoObjLiteral ::= BitwiseANDExpression_In_NoObjLiteral
	BitwiseXORExpression, // BitwiseXORExpression_In_NoObjLiteral ::= BitwiseXORExpression_In_NoObjLiteral '^' BitwiseANDExpression_In
	0, // BitwiseXORExpression_In_Yield ::= BitwiseANDExpression_In_Yield
	0, // BitwiseXORExpression_In_Yield ::= BitwiseXORExpression_In_Yield '^' BitwiseANDExpression_In_Yield
	BitwiseXORExpression, // BitwiseXORExpression_NoLet ::= BitwiseANDExpression_NoLet
	BitwiseXORExpression, // BitwiseXORExpression_NoLet ::= BitwiseXORExpression_NoLet '^' BitwiseANDExpression
	BitwiseXORExpression, // BitwiseXORExpression_NoLet_Yield ::= BitwiseANDExpression_NoLet_Yield
	BitwiseXORExpression, // BitwiseXORExpression_NoLet_Yield ::= BitwiseXORExpression_NoLet_Yield '^' BitwiseANDExpression_Yield
	BitwiseXORExpression, // BitwiseXORExpression_NoObjLiteral ::= BitwiseANDExpression_NoObjLiteral
	BitwiseXORExpression, // BitwiseXORExpression_NoObjLiteral ::= BitwiseXORExpression_NoObjLiteral '^' BitwiseANDExpression
	BitwiseXORExpression, // BitwiseXORExpression_StartWithLet ::= BitwiseANDExpression_StartWithLet
	BitwiseXORExpression, // BitwiseXORExpression_StartWithLet ::= BitwiseXORExpression_StartWithLet '^' BitwiseANDExpression
	BitwiseXORExpression, // BitwiseXORExpression_StartWithLet_Yield ::= BitwiseANDExpression_StartWithLet_Yield
	BitwiseXORExpression, // BitwiseXORExpression_StartWithLet_Yield ::= BitwiseXORExpression_StartWithLet_Yield '^' BitwiseANDExpression_Yield
	0, // BitwiseXORExpression_Yield ::= BitwiseANDExpression_Yield
	0, // BitwiseXORExpression_Yield ::= BitwiseXORExpression_Yield '^' BitwiseANDExpression_Yield
	0, // BitwiseORExpression ::= BitwiseXORExpression
	0, // BitwiseORExpression ::= BitwiseORExpression '|' BitwiseXORExpression
	0, // BitwiseORExpression_In ::= BitwiseXORExpression_In
	0, // BitwiseORExpression_In ::= BitwiseORExpression_In '|' BitwiseXORExpression_In
	BitwiseORExpression, // BitwiseORExpression_In_NoFuncClass ::= BitwiseXORExpression_In_NoFuncClass
	BitwiseORExpression, // BitwiseORExpression_In_NoFuncClass ::= BitwiseORExpression_In_NoFuncClass '|' BitwiseXORExpression_In
	BitwiseORExpression, // BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	BitwiseORExpression, // BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '|' BitwiseXORExpression_In
	BitwiseORExpression, // BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	BitwiseORExpression, // BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '|' BitwiseXORExpression_In_Yield
	BitwiseORExpression, // BitwiseORExpression_In_NoObjLiteral ::= BitwiseXORExpression_In_NoObjLiteral
	BitwiseORExpression, // BitwiseORExpression_In_NoObjLiteral ::= BitwiseORExpression_In_NoObjLiteral '|' BitwiseXORExpression_In
	0, // BitwiseORExpression_In_Yield ::= BitwiseXORExpression_In_Yield
	0, // BitwiseORExpression_In_Yield ::= BitwiseORExpression_In_Yield '|' BitwiseXORExpression_In_Yield
	BitwiseORExpression, // BitwiseORExpression_NoLet ::= BitwiseXORExpression_NoLet
	BitwiseORExpression, // BitwiseORExpression_NoLet ::= BitwiseORExpression_NoLet '|' BitwiseXORExpression
	BitwiseORExpression, // BitwiseORExpression_NoLet_Yield ::= BitwiseXORExpression_NoLet_Yield
	BitwiseORExpression, // BitwiseORExpression_NoLet_Yield ::= BitwiseORExpression_NoLet_Yield '|' BitwiseXORExpression_Yield
	BitwiseORExpression, // BitwiseORExpression_NoObjLiteral ::= BitwiseXORExpression_NoObjLiteral
	BitwiseORExpression, // BitwiseORExpression_NoObjLiteral ::= BitwiseORExpression_NoObjLiteral '|' BitwiseXORExpression
	BitwiseORExpression, // BitwiseORExpression_StartWithLet ::= BitwiseXORExpression_StartWithLet
	BitwiseORExpression, // BitwiseORExpression_StartWithLet ::= BitwiseORExpression_StartWithLet '|' BitwiseXORExpression
	BitwiseORExpression, // BitwiseORExpression_StartWithLet_Yield ::= BitwiseXORExpression_StartWithLet_Yield
	BitwiseORExpression, // BitwiseORExpression_StartWithLet_Yield ::= BitwiseORExpression_StartWithLet_Yield '|' BitwiseXORExpression_Yield
	0, // BitwiseORExpression_Yield ::= BitwiseXORExpression_Yield
	0, // BitwiseORExpression_Yield ::= BitwiseORExpression_Yield '|' BitwiseXORExpression_Yield
	0, // LogicalANDExpression ::= BitwiseORExpression
	0, // LogicalANDExpression ::= LogicalANDExpression '&&' BitwiseORExpression
	0, // LogicalANDExpression_In ::= BitwiseORExpression_In
	0, // LogicalANDExpression_In ::= LogicalANDExpression_In '&&' BitwiseORExpression_In
	LogicalANDExpression, // LogicalANDExpression_In_NoFuncClass ::= BitwiseORExpression_In_NoFuncClass
	LogicalANDExpression, // LogicalANDExpression_In_NoFuncClass ::= LogicalANDExpression_In_NoFuncClass '&&' BitwiseORExpression_In
	LogicalANDExpression, // LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	LogicalANDExpression, // LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '&&' BitwiseORExpression_In
	LogicalANDExpression, // LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	LogicalANDExpression, // LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '&&' BitwiseORExpression_In_Yield
	LogicalANDExpression, // LogicalANDExpression_In_NoObjLiteral ::= BitwiseORExpression_In_NoObjLiteral
	LogicalANDExpression, // LogicalANDExpression_In_NoObjLiteral ::= LogicalANDExpression_In_NoObjLiteral '&&' BitwiseORExpression_In
	0, // LogicalANDExpression_In_Yield ::= BitwiseORExpression_In_Yield
	0, // LogicalANDExpression_In_Yield ::= LogicalANDExpression_In_Yield '&&' BitwiseORExpression_In_Yield
	LogicalANDExpression, // LogicalANDExpression_NoLet ::= BitwiseORExpression_NoLet
	LogicalANDExpression, // LogicalANDExpression_NoLet ::= LogicalANDExpression_NoLet '&&' BitwiseORExpression
	LogicalANDExpression, // LogicalANDExpression_NoLet_Yield ::= BitwiseORExpression_NoLet_Yield
	LogicalANDExpression, // LogicalANDExpression_NoLet_Yield ::= LogicalANDExpression_NoLet_Yield '&&' BitwiseORExpression_Yield
	LogicalANDExpression, // LogicalANDExpression_NoObjLiteral ::= BitwiseORExpression_NoObjLiteral
	LogicalANDExpression, // LogicalANDExpression_NoObjLiteral ::= LogicalANDExpression_NoObjLiteral '&&' BitwiseORExpression
	LogicalANDExpression, // LogicalANDExpression_StartWithLet ::= BitwiseORExpression_StartWithLet
	LogicalANDExpression, // LogicalANDExpression_StartWithLet ::= LogicalANDExpression_StartWithLet '&&' BitwiseORExpression
	LogicalANDExpression, // LogicalANDExpression_StartWithLet_Yield ::= BitwiseORExpression_StartWithLet_Yield
	LogicalANDExpression, // LogicalANDExpression_StartWithLet_Yield ::= LogicalANDExpression_StartWithLet_Yield '&&' BitwiseORExpression_Yield
	0, // LogicalANDExpression_Yield ::= BitwiseORExpression_Yield
	0, // LogicalANDExpression_Yield ::= LogicalANDExpression_Yield '&&' BitwiseORExpression_Yield
	0, // LogicalORExpression ::= LogicalANDExpression
	0, // LogicalORExpression ::= LogicalORExpression '||' LogicalANDExpression
	0, // LogicalORExpression_In ::= LogicalANDExpression_In
	0, // LogicalORExpression_In ::= LogicalORExpression_In '||' LogicalANDExpression_In
	LogicalORExpression, // LogicalORExpression_In_NoFuncClass ::= LogicalANDExpression_In_NoFuncClass
	LogicalORExpression, // LogicalORExpression_In_NoFuncClass ::= LogicalORExpression_In_NoFuncClass '||' LogicalANDExpression_In
	LogicalORExpression, // LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	LogicalORExpression, // LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '||' LogicalANDExpression_In
	LogicalORExpression, // LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	LogicalORExpression, // LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '||' LogicalANDExpression_In_Yield
	LogicalORExpression, // LogicalORExpression_In_NoObjLiteral ::= LogicalANDExpression_In_NoObjLiteral
	LogicalORExpression, // LogicalORExpression_In_NoObjLiteral ::= LogicalORExpression_In_NoObjLiteral '||' LogicalANDExpression_In
	0, // LogicalORExpression_In_Yield ::= LogicalANDExpression_In_Yield
	0, // LogicalORExpression_In_Yield ::= LogicalORExpression_In_Yield '||' LogicalANDExpression_In_Yield
	LogicalORExpression, // LogicalORExpression_NoLet ::= LogicalANDExpression_NoLet
	LogicalORExpression, // LogicalORExpression_NoLet ::= LogicalORExpression_NoLet '||' LogicalANDExpression
	LogicalORExpression, // LogicalORExpression_NoLet_Yield ::= LogicalANDExpression_NoLet_Yield
	LogicalORExpression, // LogicalORExpression_NoLet_Yield ::= LogicalORExpression_NoLet_Yield '||' LogicalANDExpression_Yield
	LogicalORExpression, // LogicalORExpression_NoObjLiteral ::= LogicalANDExpression_NoObjLiteral
	LogicalORExpression, // LogicalORExpression_NoObjLiteral ::= LogicalORExpression_NoObjLiteral '||' LogicalANDExpression
	LogicalORExpression, // LogicalORExpression_StartWithLet ::= LogicalANDExpression_StartWithLet
	LogicalORExpression, // LogicalORExpression_StartWithLet ::= LogicalORExpression_StartWithLet '||' LogicalANDExpression
	LogicalORExpression, // LogicalORExpression_StartWithLet_Yield ::= LogicalANDExpression_StartWithLet_Yield
	LogicalORExpression, // LogicalORExpression_StartWithLet_Yield ::= LogicalORExpression_StartWithLet_Yield '||' LogicalANDExpression_Yield
	0, // LogicalORExpression_Yield ::= LogicalANDExpression_Yield
	0, // LogicalORExpression_Yield ::= LogicalORExpression_Yield '||' LogicalANDExpression_Yield
	ConditionalExpression, // ConditionalExpression ::= LogicalORExpression
	ConditionalExpression, // ConditionalExpression ::= LogicalORExpression '?' AssignmentExpression_In ':' AssignmentExpression
	ConditionalExpression, // ConditionalExpression_In ::= LogicalORExpression_In
	ConditionalExpression, // ConditionalExpression_In ::= LogicalORExpression_In '?' AssignmentExpression_In ':' AssignmentExpression_In
	ConditionalExpression, // ConditionalExpression_In_NoFuncClass ::= LogicalORExpression_In_NoFuncClass
	ConditionalExpression, // ConditionalExpression_In_NoFuncClass ::= LogicalORExpression_In_NoFuncClass '?' AssignmentExpression_In ':' AssignmentExpression_In
	ConditionalExpression, // ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	ConditionalExpression, // ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral '?' AssignmentExpression_In ':' AssignmentExpression_In
	ConditionalExpression, // ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	ConditionalExpression, // ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield '?' AssignmentExpression_In_Yield ':' AssignmentExpression_In_Yield
	ConditionalExpression, // ConditionalExpression_In_NoObjLiteral ::= LogicalORExpression_In_NoObjLiteral
	ConditionalExpression, // ConditionalExpression_In_NoObjLiteral ::= LogicalORExpression_In_NoObjLiteral '?' AssignmentExpression_In ':' AssignmentExpression_In
	ConditionalExpression, // ConditionalExpression_In_Yield ::= LogicalORExpression_In_Yield
	ConditionalExpression, // ConditionalExpression_In_Yield ::= LogicalORExpression_In_Yield '?' AssignmentExpression_In_Yield ':' AssignmentExpression_In_Yield
	ConditionalExpression, // ConditionalExpression_NoLet ::= LogicalORExpression_NoLet
	ConditionalExpression, // ConditionalExpression_NoLet ::= LogicalORExpression_NoLet '?' AssignmentExpression_In ':' AssignmentExpression
	ConditionalExpression, // ConditionalExpression_NoLet_Yield ::= LogicalORExpression_NoLet_Yield
	ConditionalExpression, // ConditionalExpression_NoLet_Yield ::= LogicalORExpression_NoLet_Yield '?' AssignmentExpression_In_Yield ':' AssignmentExpression_Yield
	ConditionalExpression, // ConditionalExpression_NoObjLiteral ::= LogicalORExpression_NoObjLiteral
	ConditionalExpression, // ConditionalExpression_NoObjLiteral ::= LogicalORExpression_NoObjLiteral '?' AssignmentExpression_In ':' AssignmentExpression
	ConditionalExpression, // ConditionalExpression_StartWithLet ::= LogicalORExpression_StartWithLet
	ConditionalExpression, // ConditionalExpression_StartWithLet ::= LogicalORExpression_StartWithLet '?' AssignmentExpression_In ':' AssignmentExpression
	ConditionalExpression, // ConditionalExpression_StartWithLet_Yield ::= LogicalORExpression_StartWithLet_Yield
	ConditionalExpression, // ConditionalExpression_StartWithLet_Yield ::= LogicalORExpression_StartWithLet_Yield '?' AssignmentExpression_In_Yield ':' AssignmentExpression_Yield
	ConditionalExpression, // ConditionalExpression_Yield ::= LogicalORExpression_Yield
	ConditionalExpression, // ConditionalExpression_Yield ::= LogicalORExpression_Yield '?' AssignmentExpression_In_Yield ':' AssignmentExpression_Yield
	AssignmentExpression, // AssignmentExpression ::= ConditionalExpression
	AssignmentExpression, // AssignmentExpression ::= ArrowFunction
	AssignmentExpression, // AssignmentExpression ::= LeftHandSideExpression '=' AssignmentExpression
	AssignmentExpression, // AssignmentExpression ::= LeftHandSideExpression AssignmentOperator AssignmentExpression
	AssignmentExpression, // AssignmentExpression_In ::= ConditionalExpression_In
	AssignmentExpression, // AssignmentExpression_In ::= ArrowFunction_In
	AssignmentExpression, // AssignmentExpression_In ::= LeftHandSideExpression '=' AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In ::= LeftHandSideExpression AssignmentOperator AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass ::= ConditionalExpression_In_NoFuncClass
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass ::= ArrowFunction_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass ::= LeftHandSideExpression_NoFuncClass '=' AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass ::= LeftHandSideExpression_NoFuncClass AssignmentOperator AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= ArrowFunction_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral '=' AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral AssignmentOperator AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= YieldExpression_In
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= ArrowFunction_In_Yield
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield '=' AssignmentExpression_In_Yield
	AssignmentExpression, // AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield AssignmentOperator AssignmentExpression_In_Yield
	AssignmentExpression, // AssignmentExpression_In_NoObjLiteral ::= ConditionalExpression_In_NoObjLiteral
	AssignmentExpression, // AssignmentExpression_In_NoObjLiteral ::= ArrowFunction_In
	AssignmentExpression, // AssignmentExpression_In_NoObjLiteral ::= LeftHandSideExpression_NoObjLiteral '=' AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In_NoObjLiteral ::= LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression_In
	AssignmentExpression, // AssignmentExpression_In_Yield ::= ConditionalExpression_In_Yield
	AssignmentExpression, // AssignmentExpression_In_Yield ::= YieldExpression_In
	AssignmentExpression, // AssignmentExpression_In_Yield ::= ArrowFunction_In_Yield
	AssignmentExpression, // AssignmentExpression_In_Yield ::= LeftHandSideExpression_Yield '=' AssignmentExpression_In_Yield
	AssignmentExpression, // AssignmentExpression_In_Yield ::= LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_In_Yield
	AssignmentExpression, // AssignmentExpression_NoLet ::= ConditionalExpression_NoLet
	AssignmentExpression, // AssignmentExpression_NoLet ::= ArrowFunction
	AssignmentExpression, // AssignmentExpression_NoLet ::= LeftHandSideExpression_NoLet '=' AssignmentExpression
	AssignmentExpression, // AssignmentExpression_NoLet ::= LeftHandSideExpression_NoLet AssignmentOperator AssignmentExpression
	AssignmentExpression, // AssignmentExpression_NoLet_Yield ::= ConditionalExpression_NoLet_Yield
	AssignmentExpression, // AssignmentExpression_NoLet_Yield ::= YieldExpression
	AssignmentExpression, // AssignmentExpression_NoLet_Yield ::= ArrowFunction_Yield
	AssignmentExpression, // AssignmentExpression_NoLet_Yield ::= LeftHandSideExpression_NoLet_Yield '=' AssignmentExpression_Yield
	AssignmentExpression, // AssignmentExpression_NoLet_Yield ::= LeftHandSideExpression_NoLet_Yield AssignmentOperator AssignmentExpression_Yield
	AssignmentExpression, // AssignmentExpression_NoObjLiteral ::= ConditionalExpression_NoObjLiteral
	AssignmentExpression, // AssignmentExpression_NoObjLiteral ::= ArrowFunction
	AssignmentExpression, // AssignmentExpression_NoObjLiteral ::= LeftHandSideExpression_NoObjLiteral '=' AssignmentExpression
	AssignmentExpression, // AssignmentExpression_NoObjLiteral ::= LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression
	AssignmentExpression, // AssignmentExpression_StartWithLet ::= ConditionalExpression_StartWithLet
	AssignmentExpression, // AssignmentExpression_StartWithLet ::= LeftHandSideExpression_StartWithLet '=' AssignmentExpression
	AssignmentExpression, // AssignmentExpression_StartWithLet ::= LeftHandSideExpression_StartWithLet AssignmentOperator AssignmentExpression
	AssignmentExpression, // AssignmentExpression_StartWithLet_Yield ::= ConditionalExpression_StartWithLet_Yield
	AssignmentExpression, // AssignmentExpression_StartWithLet_Yield ::= LeftHandSideExpression_StartWithLet_Yield '=' AssignmentExpression_Yield
	AssignmentExpression, // AssignmentExpression_StartWithLet_Yield ::= LeftHandSideExpression_StartWithLet_Yield AssignmentOperator AssignmentExpression_Yield
	AssignmentExpression, // AssignmentExpression_Yield ::= ConditionalExpression_Yield
	AssignmentExpression, // AssignmentExpression_Yield ::= YieldExpression
	AssignmentExpression, // AssignmentExpression_Yield ::= ArrowFunction_Yield
	AssignmentExpression, // AssignmentExpression_Yield ::= LeftHandSideExpression_Yield '=' AssignmentExpression_Yield
	AssignmentExpression, // AssignmentExpression_Yield ::= LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_Yield
	AssignmentOperator, // AssignmentOperator ::= '*='
	AssignmentOperator, // AssignmentOperator ::= '/='
	AssignmentOperator, // AssignmentOperator ::= '%='
	AssignmentOperator, // AssignmentOperator ::= '+='
	AssignmentOperator, // AssignmentOperator ::= '-='
	AssignmentOperator, // AssignmentOperator ::= '<<='
	AssignmentOperator, // AssignmentOperator ::= '>>='
	AssignmentOperator, // AssignmentOperator ::= '>>>='
	AssignmentOperator, // AssignmentOperator ::= '&='
	AssignmentOperator, // AssignmentOperator ::= '^='
	AssignmentOperator, // AssignmentOperator ::= '|='
	0, // Expression_In ::= AssignmentExpression_In
	0, // Expression_In ::= Expression_In ',' AssignmentExpression_In
	Expression, // Expression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
	Expression, // Expression_In_NoFuncClass_NoLetSq_NoObjLiteral ::= Expression_In_NoFuncClass_NoLetSq_NoObjLiteral ',' AssignmentExpression_In
	Expression, // Expression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
	Expression, // Expression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ::= Expression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ',' AssignmentExpression_In_Yield
	0, // Expression_In_Yield ::= AssignmentExpression_In_Yield
	0, // Expression_In_Yield ::= Expression_In_Yield ',' AssignmentExpression_In_Yield
	Expression, // Expression_NoLet ::= AssignmentExpression_NoLet
	Expression, // Expression_NoLet ::= Expression_NoLet ',' AssignmentExpression
	Expression, // Expression_NoLet_Yield ::= AssignmentExpression_NoLet_Yield
	Expression, // Expression_NoLet_Yield ::= Expression_NoLet_Yield ',' AssignmentExpression_Yield
	Expression, // Expression_StartWithLet ::= AssignmentExpression_StartWithLet
	Expression, // Expression_StartWithLet ::= Expression_StartWithLet ',' AssignmentExpression
	Expression, // Expression_StartWithLet_Yield ::= AssignmentExpression_StartWithLet_Yield
	Expression, // Expression_StartWithLet_Yield ::= Expression_StartWithLet_Yield ',' AssignmentExpression_Yield
	Statement, // Statement ::= BlockStatement
	Statement, // Statement ::= VariableStatement
	Statement, // Statement ::= EmptyStatement
	Statement, // Statement ::= ExpressionStatement
	Statement, // Statement ::= IfStatement
	Statement, // Statement ::= BreakableStatement
	Statement, // Statement ::= ContinueStatement
	Statement, // Statement ::= BreakStatement
	Statement, // Statement ::= WithStatement
	Statement, // Statement ::= LabelledStatement
	Statement, // Statement ::= ThrowStatement
	Statement, // Statement ::= TryStatement
	Statement, // Statement ::= DebuggerStatement
	Statement, // Statement_Return ::= BlockStatement_Return
	Statement, // Statement_Return ::= VariableStatement
	Statement, // Statement_Return ::= EmptyStatement
	Statement, // Statement_Return ::= ExpressionStatement
	Statement, // Statement_Return ::= IfStatement_Return
	Statement, // Statement_Return ::= BreakableStatement_Return
	Statement, // Statement_Return ::= ContinueStatement
	Statement, // Statement_Return ::= BreakStatement
	Statement, // Statement_Return ::= ReturnStatement
	Statement, // Statement_Return ::= WithStatement_Return
	Statement, // Statement_Return ::= LabelledStatement_Return
	Statement, // Statement_Return ::= ThrowStatement
	Statement, // Statement_Return ::= TryStatement_Return
	Statement, // Statement_Return ::= DebuggerStatement
	Statement, // Statement_Return_Yield ::= BlockStatement_Return_Yield
	Statement, // Statement_Return_Yield ::= VariableStatement_Yield
	Statement, // Statement_Return_Yield ::= EmptyStatement
	Statement, // Statement_Return_Yield ::= ExpressionStatement_Yield
	Statement, // Statement_Return_Yield ::= IfStatement_Return_Yield
	Statement, // Statement_Return_Yield ::= BreakableStatement_Return_Yield
	Statement, // Statement_Return_Yield ::= ContinueStatement_Yield
	Statement, // Statement_Return_Yield ::= BreakStatement_Yield
	Statement, // Statement_Return_Yield ::= ReturnStatement_Yield
	Statement, // Statement_Return_Yield ::= WithStatement_Return_Yield
	Statement, // Statement_Return_Yield ::= LabelledStatement_Return_Yield
	Statement, // Statement_Return_Yield ::= ThrowStatement_Yield
	Statement, // Statement_Return_Yield ::= TryStatement_Return_Yield
	Statement, // Statement_Return_Yield ::= DebuggerStatement
	Declaration, // Declaration ::= HoistableDeclaration
	Declaration, // Declaration ::= ClassDeclaration
	Declaration, // Declaration ::= LexicalDeclaration_In
	Declaration, // Declaration_Yield ::= HoistableDeclaration_Yield
	Declaration, // Declaration_Yield ::= ClassDeclaration_Yield
	Declaration, // Declaration_Yield ::= LexicalDeclaration_In_Yield
	HoistableDeclaration, // HoistableDeclaration ::= FunctionDeclaration
	HoistableDeclaration, // HoistableDeclaration ::= GeneratorDeclaration
	HoistableDeclaration, // HoistableDeclaration_Default ::= FunctionDeclaration_Default
	HoistableDeclaration, // HoistableDeclaration_Default ::= GeneratorDeclaration_Default
	HoistableDeclaration, // HoistableDeclaration_Yield ::= FunctionDeclaration_Yield
	HoistableDeclaration, // HoistableDeclaration_Yield ::= GeneratorDeclaration_Yield
	BreakableStatement, // BreakableStatement ::= IterationStatement
	BreakableStatement, // BreakableStatement ::= SwitchStatement
	BreakableStatement, // BreakableStatement_Return ::= IterationStatement_Return
	BreakableStatement, // BreakableStatement_Return ::= SwitchStatement_Return
	BreakableStatement, // BreakableStatement_Return_Yield ::= IterationStatement_Return_Yield
	BreakableStatement, // BreakableStatement_Return_Yield ::= SwitchStatement_Return_Yield
	BlockStatement, // BlockStatement ::= Block
	BlockStatement, // BlockStatement_Return ::= Block_Return
	BlockStatement, // BlockStatement_Return_Yield ::= Block_Return_Yield
	Block, // Block ::= '{' StatementList '}'
	Block, // Block ::= '{' '}'
	Block, // Block_Return ::= '{' StatementList_Return '}'
	Block, // Block_Return ::= '{' '}'
	Block, // Block_Return_Yield ::= '{' StatementList_Return_Yield '}'
	Block, // Block_Return_Yield ::= '{' '}'
	0, // StatementList ::= StatementListItem
	0, // StatementList ::= StatementList StatementListItem
	0, // StatementList_Return ::= StatementListItem_Return
	0, // StatementList_Return ::= StatementList_Return StatementListItem_Return
	0, // StatementList_Return_Yield ::= StatementListItem_Return_Yield
	0, // StatementList_Return_Yield ::= StatementList_Return_Yield StatementListItem_Return_Yield
	StatementListItem, // StatementListItem ::= Statement
	StatementListItem, // StatementListItem ::= Declaration
	StatementListItem, // StatementListItem_Return ::= Statement_Return
	StatementListItem, // StatementListItem_Return ::= Declaration
	StatementListItem, // StatementListItem_Return_Yield ::= Statement_Return_Yield
	StatementListItem, // StatementListItem_Return_Yield ::= Declaration_Yield
	LexicalDeclaration, // LexicalDeclaration ::= LetOrConst BindingList ';'
	LexicalDeclaration, // LexicalDeclaration_In ::= LetOrConst BindingList_In ';'
	LexicalDeclaration, // LexicalDeclaration_In_Yield ::= LetOrConst BindingList_In_Yield ';'
	LexicalDeclaration, // LexicalDeclaration_Yield ::= LetOrConst BindingList_Yield ';'
	LetOrConst, // LetOrConst ::= 'let'
	LetOrConst, // LetOrConst ::= 'const'
	0, // BindingList ::= LexicalBinding
	0, // BindingList ::= BindingList ',' LexicalBinding
	0, // BindingList_In ::= LexicalBinding_In
	0, // BindingList_In ::= BindingList_In ',' LexicalBinding_In
	0, // BindingList_In_Yield ::= LexicalBinding_In_Yield
	0, // BindingList_In_Yield ::= BindingList_In_Yield ',' LexicalBinding_In_Yield
	0, // BindingList_Yield ::= LexicalBinding_Yield
	0, // BindingList_Yield ::= BindingList_Yield ',' LexicalBinding_Yield
	LexicalBinding, // LexicalBinding ::= BindingIdentifier Initializeropt
	LexicalBinding, // LexicalBinding ::= BindingPattern Initializer
	LexicalBinding, // LexicalBinding_In ::= BindingIdentifier Initializeropt_In
	LexicalBinding, // LexicalBinding_In ::= BindingPattern Initializer_In
	LexicalBinding, // LexicalBinding_In_Yield ::= BindingIdentifier_Yield Initializeropt_In_Yield
	LexicalBinding, // LexicalBinding_In_Yield ::= BindingPattern_Yield Initializer_In_Yield
	LexicalBinding, // LexicalBinding_Yield ::= BindingIdentifier_Yield Initializeropt_Yield
	LexicalBinding, // LexicalBinding_Yield ::= BindingPattern_Yield Initializer_Yield
	VariableStatement, // VariableStatement ::= 'var' VariableDeclarationList_In ';'
	VariableStatement, // VariableStatement_Yield ::= 'var' VariableDeclarationList_In_Yield ';'
	0, // VariableDeclarationList ::= VariableDeclaration
	0, // VariableDeclarationList ::= VariableDeclarationList ',' VariableDeclaration
	0, // VariableDeclarationList_In ::= VariableDeclaration_In
	0, // VariableDeclarationList_In ::= VariableDeclarationList_In ',' VariableDeclaration_In
	0, // VariableDeclarationList_In_Yield ::= VariableDeclaration_In_Yield
	0, // VariableDeclarationList_In_Yield ::= VariableDeclarationList_In_Yield ',' VariableDeclaration_In_Yield
	0, // VariableDeclarationList_Yield ::= VariableDeclaration_Yield
	0, // VariableDeclarationList_Yield ::= VariableDeclarationList_Yield ',' VariableDeclaration_Yield
	VariableDeclaration, // VariableDeclaration ::= BindingIdentifier Initializeropt
	VariableDeclaration, // VariableDeclaration ::= BindingPattern Initializer
	VariableDeclaration, // VariableDeclaration_In ::= BindingIdentifier Initializeropt_In
	VariableDeclaration, // VariableDeclaration_In ::= BindingPattern Initializer_In
	VariableDeclaration, // VariableDeclaration_In_Yield ::= BindingIdentifier_Yield Initializeropt_In_Yield
	VariableDeclaration, // VariableDeclaration_In_Yield ::= BindingPattern_Yield Initializer_In_Yield
	VariableDeclaration, // VariableDeclaration_Yield ::= BindingIdentifier_Yield Initializeropt_Yield
	VariableDeclaration, // VariableDeclaration_Yield ::= BindingPattern_Yield Initializer_Yield
	BindingPattern, // BindingPattern ::= ObjectBindingPattern
	BindingPattern, // BindingPattern ::= ArrayBindingPattern
	BindingPattern, // BindingPattern_Yield ::= ObjectBindingPattern_Yield
	BindingPattern, // BindingPattern_Yield ::= ArrayBindingPattern_Yield
	ObjectBindingPattern, // ObjectBindingPattern ::= '{' '}'
	ObjectBindingPattern, // ObjectBindingPattern ::= '{' BindingPropertyList '}'
	ObjectBindingPattern, // ObjectBindingPattern ::= '{' BindingPropertyList ',' '}'
	ObjectBindingPattern, // ObjectBindingPattern_Yield ::= '{' '}'
	ObjectBindingPattern, // ObjectBindingPattern_Yield ::= '{' BindingPropertyList_Yield '}'
	ObjectBindingPattern, // ObjectBindingPattern_Yield ::= '{' BindingPropertyList_Yield ',' '}'
	ArrayBindingPattern, // ArrayBindingPattern ::= '[' Elisionopt BindingRestElementopt ']'
	ArrayBindingPattern, // ArrayBindingPattern ::= '[' BindingElementList ']'
	ArrayBindingPattern, // ArrayBindingPattern ::= '[' BindingElementList ',' Elisionopt BindingRestElementopt ']'
	ArrayBindingPattern, // ArrayBindingPattern_Yield ::= '[' Elisionopt BindingRestElementopt_Yield ']'
	ArrayBindingPattern, // ArrayBindingPattern_Yield ::= '[' BindingElementList_Yield ']'
	ArrayBindingPattern, // ArrayBindingPattern_Yield ::= '[' BindingElementList_Yield ',' Elisionopt BindingRestElementopt_Yield ']'
	0, // BindingPropertyList ::= BindingProperty
	0, // BindingPropertyList ::= BindingPropertyList ',' BindingProperty
	0, // BindingPropertyList_Yield ::= BindingProperty_Yield
	0, // BindingPropertyList_Yield ::= BindingPropertyList_Yield ',' BindingProperty_Yield
	0, // BindingElementList ::= BindingElisionElement
	0, // BindingElementList ::= BindingElementList ',' BindingElisionElement
	0, // BindingElementList_Yield ::= BindingElisionElement_Yield
	0, // BindingElementList_Yield ::= BindingElementList_Yield ',' BindingElisionElement_Yield
	BindingElisionElement, // BindingElisionElement ::= Elisionopt BindingElement
	BindingElisionElement, // BindingElisionElement_Yield ::= Elisionopt BindingElement_Yield
	BindingProperty, // BindingProperty ::= SingleNameBinding
	BindingProperty, // BindingProperty ::= PropertyName ':' BindingElement
	BindingProperty, // BindingProperty_Yield ::= SingleNameBinding_Yield
	BindingProperty, // BindingProperty_Yield ::= PropertyName_Yield ':' BindingElement_Yield
	BindingElement, // BindingElement ::= SingleNameBinding
	BindingElement, // BindingElement ::= BindingPattern Initializeropt_In
	BindingElement, // BindingElement_Yield ::= SingleNameBinding_Yield
	BindingElement, // BindingElement_Yield ::= BindingPattern_Yield Initializeropt_In_Yield
	SingleNameBinding, // SingleNameBinding ::= BindingIdentifier Initializeropt_In
	SingleNameBinding, // SingleNameBinding_Yield ::= BindingIdentifier_Yield Initializeropt_In_Yield
	BindingRestElement, // BindingRestElement ::= '.' '.' '.' BindingIdentifier
	BindingRestElement, // BindingRestElement_Yield ::= '.' '.' '.' BindingIdentifier_Yield
	EmptyStatement, // EmptyStatement ::= ';'
	ExpressionStatement, // ExpressionStatement ::= Expression_In_NoFuncClass_NoLetSq_NoObjLiteral ';'
	ExpressionStatement, // ExpressionStatement_Yield ::= Expression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ';'
	IfStatement, // IfStatement ::= 'if' '(' Expression_In ')' Statement 'else' Statement
	IfStatement, // IfStatement ::= 'if' '(' Expression_In ')' Statement %prec 'else'
	IfStatement, // IfStatement_Return ::= 'if' '(' Expression_In ')' Statement_Return 'else' Statement_Return
	IfStatement, // IfStatement_Return ::= 'if' '(' Expression_In ')' Statement_Return %prec 'else'
	IfStatement, // IfStatement_Return_Yield ::= 'if' '(' Expression_In_Yield ')' Statement_Return_Yield 'else' Statement_Return_Yield
	IfStatement, // IfStatement_Return_Yield ::= 'if' '(' Expression_In_Yield ')' Statement_Return_Yield %prec 'else'
	IterationStatement, // IterationStatement ::= 'do' Statement 'while' '(' Expression_In ')' ';'
	IterationStatement, // IterationStatement ::= 'while' '(' Expression_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' Expressionopt_NoLet ';' Expressionopt_In ';' Expressionopt_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' Expression_StartWithLet ';' Expressionopt_In ';' Expressionopt_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' 'var' VariableDeclarationList ';' Expressionopt_In ';' Expressionopt_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' LexicalDeclaration Expressionopt_In ';' Expressionopt_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' LeftHandSideExpression_NoLet 'in' Expression_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' LeftHandSideExpression_StartWithLet 'in' Expression_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' 'var' ForBinding 'in' Expression_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' ForDeclaration 'in' Expression_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' LeftHandSideExpression_NoLet 'of' AssignmentExpression_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' 'var' ForBinding 'of' AssignmentExpression_In ')' Statement
	IterationStatement, // IterationStatement ::= 'for' '(' ForDeclaration 'of' AssignmentExpression_In ')' Statement
	IterationStatement, // IterationStatement_Return ::= 'do' Statement_Return 'while' '(' Expression_In ')' ';'
	IterationStatement, // IterationStatement_Return ::= 'while' '(' Expression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' Expressionopt_NoLet ';' Expressionopt_In ';' Expressionopt_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' Expression_StartWithLet ';' Expressionopt_In ';' Expressionopt_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' 'var' VariableDeclarationList ';' Expressionopt_In ';' Expressionopt_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' LexicalDeclaration Expressionopt_In ';' Expressionopt_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' LeftHandSideExpression_NoLet 'in' Expression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' LeftHandSideExpression_StartWithLet 'in' Expression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' 'var' ForBinding 'in' Expression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' ForDeclaration 'in' Expression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' LeftHandSideExpression_NoLet 'of' AssignmentExpression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' 'var' ForBinding 'of' AssignmentExpression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return ::= 'for' '(' ForDeclaration 'of' AssignmentExpression_In ')' Statement_Return
	IterationStatement, // IterationStatement_Return_Yield ::= 'do' Statement_Return_Yield 'while' '(' Expression_In_Yield ')' ';'
	IterationStatement, // IterationStatement_Return_Yield ::= 'while' '(' Expression_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' Expressionopt_NoLet_Yield ';' Expressionopt_In_Yield ';' Expressionopt_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' Expression_StartWithLet_Yield ';' Expressionopt_In_Yield ';' Expressionopt_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' 'var' VariableDeclarationList_Yield ';' Expressionopt_In_Yield ';' Expressionopt_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' LexicalDeclaration_Yield Expressionopt_In_Yield ';' Expressionopt_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' LeftHandSideExpression_NoLet_Yield 'in' Expression_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' LeftHandSideExpression_StartWithLet_Yield 'in' Expression_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' 'var' ForBinding_Yield 'in' Expression_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' ForDeclaration_Yield 'in' Expression_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' LeftHandSideExpression_NoLet_Yield 'of' AssignmentExpression_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' 'var' ForBinding_Yield 'of' AssignmentExpression_In_Yield ')' Statement_Return_Yield
	IterationStatement, // IterationStatement_Return_Yield ::= 'for' '(' ForDeclaration_Yield 'of' AssignmentExpression_In_Yield ')' Statement_Return_Yield
	ForDeclaration, // ForDeclaration ::= LetOrConst ForBinding
	ForDeclaration, // ForDeclaration_Yield ::= LetOrConst ForBinding_Yield
	ForBinding, // ForBinding ::= BindingIdentifier
	ForBinding, // ForBinding ::= BindingPattern
	ForBinding, // ForBinding_Yield ::= BindingIdentifier_Yield
	ForBinding, // ForBinding_Yield ::= BindingPattern_Yield
	ContinueStatement, // ContinueStatement ::= 'continue' ';'
	ContinueStatement, // ContinueStatement ::= 'continue' LabelIdentifier ';'
	ContinueStatement, // ContinueStatement_Yield ::= 'continue' ';'
	ContinueStatement, // ContinueStatement_Yield ::= 'continue' LabelIdentifier_Yield ';'
	BreakStatement, // BreakStatement ::= 'break' ';'
	BreakStatement, // BreakStatement ::= 'break' LabelIdentifier ';'
	BreakStatement, // BreakStatement_Yield ::= 'break' ';'
	BreakStatement, // BreakStatement_Yield ::= 'break' LabelIdentifier_Yield ';'
	ReturnStatement, // ReturnStatement ::= 'return' ';'
	ReturnStatement, // ReturnStatement ::= 'return' Expression_In ';'
	ReturnStatement, // ReturnStatement_Yield ::= 'return' ';'
	ReturnStatement, // ReturnStatement_Yield ::= 'return' Expression_In_Yield ';'
	WithStatement, // WithStatement ::= 'with' '(' Expression_In ')' Statement
	WithStatement, // WithStatement_Return ::= 'with' '(' Expression_In ')' Statement_Return
	WithStatement, // WithStatement_Return_Yield ::= 'with' '(' Expression_In_Yield ')' Statement_Return_Yield
	SwitchStatement, // SwitchStatement ::= 'switch' '(' Expression_In ')' CaseBlock
	SwitchStatement, // SwitchStatement_Return ::= 'switch' '(' Expression_In ')' CaseBlock_Return
	SwitchStatement, // SwitchStatement_Return_Yield ::= 'switch' '(' Expression_In_Yield ')' CaseBlock_Return_Yield
	CaseBlock, // CaseBlock ::= '{' CaseClausesopt '}'
	CaseBlock, // CaseBlock ::= '{' CaseClausesopt DefaultClause CaseClausesopt '}'
	CaseBlock, // CaseBlock_Return ::= '{' CaseClausesopt_Return '}'
	CaseBlock, // CaseBlock_Return ::= '{' CaseClausesopt_Return DefaultClause_Return CaseClausesopt_Return '}'
	CaseBlock, // CaseBlock_Return_Yield ::= '{' CaseClausesopt_Return_Yield '}'
	CaseBlock, // CaseBlock_Return_Yield ::= '{' CaseClausesopt_Return_Yield DefaultClause_Return_Yield CaseClausesopt_Return_Yield '}'
	0, // CaseClauses ::= CaseClause
	0, // CaseClauses ::= CaseClauses CaseClause
	0, // CaseClauses_Return ::= CaseClause_Return
	0, // CaseClauses_Return ::= CaseClauses_Return CaseClause_Return
	0, // CaseClauses_Return_Yield ::= CaseClause_Return_Yield
	0, // CaseClauses_Return_Yield ::= CaseClauses_Return_Yield CaseClause_Return_Yield
	CaseClause, // CaseClause ::= 'case' Expression_In ':' StatementList
	CaseClause, // CaseClause ::= 'case' Expression_In ':'
	CaseClause, // CaseClause_Return ::= 'case' Expression_In ':' StatementList_Return
	CaseClause, // CaseClause_Return ::= 'case' Expression_In ':'
	CaseClause, // CaseClause_Return_Yield ::= 'case' Expression_In_Yield ':' StatementList_Return_Yield
	CaseClause, // CaseClause_Return_Yield ::= 'case' Expression_In_Yield ':'
	DefaultClause, // DefaultClause ::= 'default' ':' StatementList
	DefaultClause, // DefaultClause ::= 'default' ':'
	DefaultClause, // DefaultClause_Return ::= 'default' ':' StatementList_Return
	DefaultClause, // DefaultClause_Return ::= 'default' ':'
	DefaultClause, // DefaultClause_Return_Yield ::= 'default' ':' StatementList_Return_Yield
	DefaultClause, // DefaultClause_Return_Yield ::= 'default' ':'
	LabelledStatement, // LabelledStatement ::= Identifier ':' LabelledItem
	LabelledStatement, // LabelledStatement ::= 'yield' ':' LabelledItem
	LabelledStatement, // LabelledStatement_Return ::= Identifier ':' LabelledItem_Return
	LabelledStatement, // LabelledStatement_Return ::= 'yield' ':' LabelledItem_Return
	LabelledStatement, // LabelledStatement_Return_Yield ::= Identifier ':' LabelledItem_Return_Yield
	LabelledItem, // LabelledItem ::= Statement
	LabelledItem, // LabelledItem ::= FunctionDeclaration
	LabelledItem, // LabelledItem_Return ::= Statement_Return
	LabelledItem, // LabelledItem_Return ::= FunctionDeclaration
	LabelledItem, // LabelledItem_Return_Yield ::= Statement_Return_Yield
	LabelledItem, // LabelledItem_Return_Yield ::= FunctionDeclaration_Yield
	ThrowStatement, // ThrowStatement ::= 'throw' Expression_In ';'
	ThrowStatement, // ThrowStatement_Yield ::= 'throw' Expression_In_Yield ';'
	TryStatement, // TryStatement ::= 'try' Block Catch
	TryStatement, // TryStatement ::= 'try' Block Finally
	TryStatement, // TryStatement ::= 'try' Block Catch Finally
	TryStatement, // TryStatement_Return ::= 'try' Block_Return Catch_Return
	TryStatement, // TryStatement_Return ::= 'try' Block_Return Finally_Return
	TryStatement, // TryStatement_Return ::= 'try' Block_Return Catch_Return Finally_Return
	TryStatement, // TryStatement_Return_Yield ::= 'try' Block_Return_Yield Catch_Return_Yield
	TryStatement, // TryStatement_Return_Yield ::= 'try' Block_Return_Yield Finally_Return_Yield
	TryStatement, // TryStatement_Return_Yield ::= 'try' Block_Return_Yield Catch_Return_Yield Finally_Return_Yield
	Catch, // Catch ::= 'catch' '(' CatchParameter ')' Block
	Catch, // Catch_Return ::= 'catch' '(' CatchParameter ')' Block_Return
	Catch, // Catch_Return_Yield ::= 'catch' '(' CatchParameter_Yield ')' Block_Return_Yield
	Finally, // Finally ::= 'finally' Block
	Finally, // Finally_Return ::= 'finally' Block_Return
	Finally, // Finally_Return_Yield ::= 'finally' Block_Return_Yield
	CatchParameter, // CatchParameter ::= BindingIdentifier
	CatchParameter, // CatchParameter ::= BindingPattern
	CatchParameter, // CatchParameter_Yield ::= BindingIdentifier_Yield
	CatchParameter, // CatchParameter_Yield ::= BindingPattern_Yield
	DebuggerStatement, // DebuggerStatement ::= 'debugger' ';'
	FunctionDeclaration, // FunctionDeclaration ::= 'function' BindingIdentifier '(' FormalParameters ')' '{' FunctionBody '}'
	FunctionDeclaration, // FunctionDeclaration_Default ::= 'function' BindingIdentifier '(' FormalParameters ')' '{' FunctionBody '}'
	FunctionDeclaration, // FunctionDeclaration_Default ::= 'function' '(' FormalParameters ')' '{' FunctionBody '}'
	FunctionDeclaration, // FunctionDeclaration_Yield ::= 'function' BindingIdentifier_Yield '(' FormalParameters ')' '{' FunctionBody '}'
	FunctionExpression, // FunctionExpression ::= 'function' BindingIdentifier '(' FormalParameters ')' '{' FunctionBody '}'
	FunctionExpression, // FunctionExpression ::= 'function' '(' FormalParameters ')' '{' FunctionBody '}'
	StrictFormalParameters, // StrictFormalParameters ::= FormalParameters
	StrictFormalParameters, // StrictFormalParameters_Yield ::= FormalParameters_Yield
	0, // FormalParameters ::= FormalParameterList
	0, // FormalParameters ::=
	0, // FormalParameters_Yield ::= FormalParameterList_Yield
	0, // FormalParameters_Yield ::=
	FormalParameterList, // FormalParameterList ::= FunctionRestParameter
	FormalParameterList, // FormalParameterList ::= FormalsList
	FormalParameterList, // FormalParameterList ::= FormalsList ',' FunctionRestParameter
	FormalParameterList, // FormalParameterList_Yield ::= FunctionRestParameter_Yield
	FormalParameterList, // FormalParameterList_Yield ::= FormalsList_Yield
	FormalParameterList, // FormalParameterList_Yield ::= FormalsList_Yield ',' FunctionRestParameter_Yield
	0, // FormalsList ::= FormalParameter
	0, // FormalsList ::= FormalsList ',' FormalParameter
	0, // FormalsList_Yield ::= FormalParameter_Yield
	0, // FormalsList_Yield ::= FormalsList_Yield ',' FormalParameter_Yield
	FunctionRestParameter, // FunctionRestParameter ::= BindingRestElement
	FunctionRestParameter, // FunctionRestParameter_Yield ::= BindingRestElement_Yield
	FormalParameter, // FormalParameter ::= BindingElement
	FormalParameter, // FormalParameter_Yield ::= BindingElement_Yield
	0, // FunctionBody ::= StatementList_Return
	0, // FunctionBody ::=
	0, // FunctionBody_Yield ::= StatementList_Return_Yield
	0, // FunctionBody_Yield ::=
	ArrowFunction, // ArrowFunction ::= ArrowParameters '=>' ConciseBody
	ArrowFunction, // ArrowFunction_In ::= ArrowParameters '=>' ConciseBody_In
	ArrowFunction, // ArrowFunction_In_Yield ::= ArrowParameters_Yield '=>' ConciseBody_In
	ArrowFunction, // ArrowFunction_Yield ::= ArrowParameters_Yield '=>' ConciseBody
	ArrowParameters, // ArrowParameters ::= BindingIdentifier
	ArrowParameters, // ArrowParameters ::= CoverParenthesizedExpressionAndArrowParameterList
	ArrowParameters, // ArrowParameters_Yield ::= BindingIdentifier_Yield
	ArrowParameters, // ArrowParameters_Yield ::= CoverParenthesizedExpressionAndArrowParameterList_Yield
	ConciseBody, // ConciseBody ::= AssignmentExpression_NoObjLiteral
	ConciseBody, // ConciseBody ::= '{' FunctionBody '}'
	ConciseBody, // ConciseBody_In ::= AssignmentExpression_In_NoObjLiteral
	ConciseBody, // ConciseBody_In ::= '{' FunctionBody '}'
	MethodDefinition, // MethodDefinition ::= PropertyName '(' StrictFormalParameters ')' '{' FunctionBody '}'
	MethodDefinition, // MethodDefinition ::= GeneratorMethod
	MethodDefinition, // MethodDefinition ::= 'get' PropertyName '(' ')' '{' FunctionBody '}'
	MethodDefinition, // MethodDefinition ::= 'set' PropertyName '(' PropertySetParameterList ')' '{' FunctionBody '}'
	MethodDefinition, // MethodDefinition_Yield ::= PropertyName_Yield '(' StrictFormalParameters_Yield ')' '{' FunctionBody_Yield '}'
	MethodDefinition, // MethodDefinition_Yield ::= GeneratorMethod_Yield
	MethodDefinition, // MethodDefinition_Yield ::= 'get' PropertyName_Yield '(' ')' '{' FunctionBody_Yield '}'
	MethodDefinition, // MethodDefinition_Yield ::= 'set' PropertyName_Yield '(' PropertySetParameterList ')' '{' FunctionBody_Yield '}'
	PropertySetParameterList, // PropertySetParameterList ::= FormalParameter
	GeneratorMethod, // GeneratorMethod ::= '*' PropertyName '(' StrictFormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorMethod, // GeneratorMethod_Yield ::= '*' PropertyName_Yield '(' StrictFormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorDeclaration, // GeneratorDeclaration ::= 'function' '*' BindingIdentifier '(' FormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorDeclaration, // GeneratorDeclaration_Default ::= 'function' '*' BindingIdentifier '(' FormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorDeclaration, // GeneratorDeclaration_Default ::= 'function' '*' '(' FormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorDeclaration, // GeneratorDeclaration_Yield ::= 'function' '*' BindingIdentifier_Yield '(' FormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorExpression, // GeneratorExpression ::= 'function' '*' BindingIdentifier_Yield '(' FormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorExpression, // GeneratorExpression ::= 'function' '*' '(' FormalParameters_Yield ')' '{' GeneratorBody '}'
	GeneratorBody, // GeneratorBody ::= FunctionBody_Yield
	YieldExpression, // YieldExpression ::= 'yield'
	YieldExpression, // YieldExpression ::= 'yield' AssignmentExpression_Yield
	YieldExpression, // YieldExpression ::= 'yield' '*' AssignmentExpression_Yield
	YieldExpression, // YieldExpression_In ::= 'yield'
	YieldExpression, // YieldExpression_In ::= 'yield' AssignmentExpression_In_Yield
	YieldExpression, // YieldExpression_In ::= 'yield' '*' AssignmentExpression_In_Yield
	ClassDeclaration, // ClassDeclaration ::= 'class' BindingIdentifier ClassTail
	ClassDeclaration, // ClassDeclaration_Default ::= 'class' BindingIdentifier ClassTail
	ClassDeclaration, // ClassDeclaration_Default ::= 'class' ClassTail
	ClassDeclaration, // ClassDeclaration_Yield ::= 'class' BindingIdentifier_Yield ClassTail_Yield
	ClassExpression, // ClassExpression ::= 'class' BindingIdentifier ClassTail
	ClassExpression, // ClassExpression ::= 'class' ClassTail
	ClassExpression, // ClassExpression_Yield ::= 'class' BindingIdentifier_Yield ClassTail_Yield
	ClassExpression, // ClassExpression_Yield ::= 'class' ClassTail_Yield
	ClassTail, // ClassTail ::= ClassHeritage '{' ClassBodyopt '}'
	ClassTail, // ClassTail ::= '{' ClassBodyopt '}'
	ClassTail, // ClassTail_Yield ::= ClassHeritage_Yield '{' ClassBodyopt_Yield '}'
	ClassTail, // ClassTail_Yield ::= '{' ClassBodyopt_Yield '}'
	ClassHeritage, // ClassHeritage ::= 'extends' LeftHandSideExpression
	ClassHeritage, // ClassHeritage_Yield ::= 'extends' LeftHandSideExpression_Yield
	ClassBody, // ClassBody ::= ClassElementList
	ClassBody, // ClassBody_Yield ::= ClassElementList_Yield
	0, // ClassElementList ::= ClassElement
	0, // ClassElementList ::= ClassElementList ClassElement
	0, // ClassElementList_Yield ::= ClassElement_Yield
	0, // ClassElementList_Yield ::= ClassElementList_Yield ClassElement_Yield
	ClassElement, // ClassElement ::= MethodDefinition
	ClassElement, // ClassElement ::= 'static' MethodDefinition
	ClassElement, // ClassElement ::= ';'
	ClassElement, // ClassElement_Yield ::= MethodDefinition_Yield
	ClassElement, // ClassElement_Yield ::= 'static' MethodDefinition_Yield
	ClassElement, // ClassElement_Yield ::= ';'
	Module, // Module ::= ModuleBodyopt
	ModuleBody, // ModuleBody ::= ModuleItemList
	0, // ModuleItemList ::= ModuleItem
	0, // ModuleItemList ::= ModuleItemList ModuleItem
	ModuleItem, // ModuleItem ::= ImportDeclaration
	ModuleItem, // ModuleItem ::= ExportDeclaration
	ModuleItem, // ModuleItem ::= StatementListItem
	ImportDeclaration, // ImportDeclaration ::= 'import' ImportClause FromClause ';'
	ImportDeclaration, // ImportDeclaration ::= 'import' ModuleSpecifier ';'
	ImportClause, // ImportClause ::= ImportedDefaultBinding
	ImportClause, // ImportClause ::= NameSpaceImport
	ImportClause, // ImportClause ::= NamedImports
	ImportClause, // ImportClause ::= ImportedDefaultBinding ',' NameSpaceImport
	ImportClause, // ImportClause ::= ImportedDefaultBinding ',' NamedImports
	ImportedDefaultBinding, // ImportedDefaultBinding ::= ImportedBinding
	NameSpaceImport, // NameSpaceImport ::= '*' 'as' ImportedBinding
	NamedImports, // NamedImports ::= '{' '}'
	NamedImports, // NamedImports ::= '{' ImportsList '}'
	NamedImports, // NamedImports ::= '{' ImportsList ',' '}'
	FromClause, // FromClause ::= 'from' ModuleSpecifier
	0, // ImportsList ::= ImportSpecifier
	0, // ImportsList ::= ImportsList ',' ImportSpecifier
	ImportSpecifier, // ImportSpecifier ::= ImportedBinding
	ImportSpecifier, // ImportSpecifier ::= IdentifierName 'as' ImportedBinding
	ModuleSpecifier, // ModuleSpecifier ::= StringLiteral
	ImportedBinding, // ImportedBinding ::= BindingIdentifier
	ExportDeclaration, // ExportDeclaration ::= 'export' '*' FromClause ';'
	ExportDeclaration, // ExportDeclaration ::= 'export' ExportClause FromClause ';'
	ExportDeclaration, // ExportDeclaration ::= 'export' ExportClause ';'
	ExportDeclaration, // ExportDeclaration ::= 'export' VariableStatement
	ExportDeclaration, // ExportDeclaration ::= 'export' Declaration
	ExportDeclaration, // ExportDeclaration ::= 'export' 'default' HoistableDeclaration_Default
	ExportDeclaration, // ExportDeclaration ::= 'export' 'default' ClassDeclaration_Default
	ExportDeclaration, // ExportDeclaration ::= 'export' 'default' AssignmentExpression_In_NoFuncClass ';'
	ExportClause, // ExportClause ::= '{' '}'
	ExportClause, // ExportClause ::= '{' ExportsList '}'
	ExportClause, // ExportClause ::= '{' ExportsList ',' '}'
	0, // ExportsList ::= ExportSpecifier
	0, // ExportsList ::= ExportsList ',' ExportSpecifier
	ExportSpecifier, // ExportSpecifier ::= IdentifierName
	ExportSpecifier, // ExportSpecifier ::= IdentifierName 'as' IdentifierName
	0, // Elisionopt ::= Elision
	0, // Elisionopt ::=
	0, // Initializeropt ::= Initializer
	0, // Initializeropt ::=
	0, // Initializeropt_In ::= Initializer_In
	0, // Initializeropt_In ::=
	0, // Initializeropt_In_Yield ::= Initializer_In_Yield
	0, // Initializeropt_In_Yield ::=
	0, // Initializeropt_Yield ::= Initializer_Yield
	0, // Initializeropt_Yield ::=
	0, // BindingRestElementopt ::= BindingRestElement
	0, // BindingRestElementopt ::=
	0, // BindingRestElementopt_Yield ::= BindingRestElement_Yield
	0, // BindingRestElementopt_Yield ::=
	0, // Expressionopt_In ::= Expression_In
	0, // Expressionopt_In ::=
	0, // Expressionopt_In_Yield ::= Expression_In_Yield
	0, // Expressionopt_In_Yield ::=
	0, // Expressionopt_NoLet ::= Expression_NoLet
	0, // Expressionopt_NoLet ::=
	0, // Expressionopt_NoLet_Yield ::= Expression_NoLet_Yield
	0, // Expressionopt_NoLet_Yield ::=
	0, // CaseClausesopt ::= CaseClauses
	0, // CaseClausesopt ::=
	0, // CaseClausesopt_Return ::= CaseClauses_Return
	0, // CaseClausesopt_Return ::=
	0, // CaseClausesopt_Return_Yield ::= CaseClauses_Return_Yield
	0, // CaseClausesopt_Return_Yield ::=
	0, // ClassBodyopt ::= ClassBody
	0, // ClassBodyopt ::=
	0, // ClassBodyopt_Yield ::= ClassBody_Yield
	0, // ClassBodyopt_Yield ::=
	0, // ModuleBodyopt ::= ModuleBody
	0, // ModuleBodyopt ::=
}

var nodeTypeStr = [...]string{
	"IdentifierName",
	"IdentifierReference",
	"BindingIdentifier",
	"LabelIdentifier",
	"PrimaryExpression",
	"CoverParenthesizedExpressionAndArrowParameterList",
	"Literal",
	"ArrayLiteral",
	"SpreadElement",
	"ObjectLiteral",
	"PropertyDefinition",
	"LiteralPropertyName",
	"ComputedPropertyName",
	"CoverInitializedName",
	"Initializer",
	"TemplateLiteral",
	"TemplateSpans",
	"MemberExpression",
	"SuperProperty",
	"MetaProperty",
	"NewTarget",
	"NewExpression",
	"CallExpression",
	"SuperCall",
	"Arguments",
	"LeftHandSideExpression",
	"PostfixExpression",
	"UnaryExpression",
	"MultiplicativeExpression",
	"MultiplicativeOperator",
	"AdditiveExpression",
	"ShiftExpression",
	"RelationalExpression",
	"EqualityExpression",
	"BitwiseANDExpression",
	"BitwiseXORExpression",
	"BitwiseORExpression",
	"LogicalANDExpression",
	"LogicalORExpression",
	"ConditionalExpression",
	"AssignmentExpression",
	"AssignmentOperator",
	"Expression",
	"Statement",
	"Declaration",
	"HoistableDeclaration",
	"BreakableStatement",
	"BlockStatement",
	"Block",
	"StatementListItem",
	"LexicalDeclaration",
	"LetOrConst",
	"LexicalBinding",
	"VariableStatement",
	"VariableDeclaration",
	"BindingPattern",
	"ObjectBindingPattern",
	"ArrayBindingPattern",
	"BindingElisionElement",
	"BindingProperty",
	"BindingElement",
	"SingleNameBinding",
	"BindingRestElement",
	"EmptyStatement",
	"ExpressionStatement",
	"IfStatement",
	"IterationStatement",
	"ForDeclaration",
	"ForBinding",
	"ContinueStatement",
	"BreakStatement",
	"ReturnStatement",
	"WithStatement",
	"SwitchStatement",
	"CaseBlock",
	"CaseClause",
	"DefaultClause",
	"LabelledStatement",
	"LabelledItem",
	"ThrowStatement",
	"TryStatement",
	"Catch",
	"Finally",
	"CatchParameter",
	"DebuggerStatement",
	"FunctionDeclaration",
	"FunctionExpression",
	"StrictFormalParameters",
	"FormalParameterList",
	"FunctionRestParameter",
	"FormalParameter",
	"ArrowFunction",
	"ArrowParameters",
	"ConciseBody",
	"MethodDefinition",
	"PropertySetParameterList",
	"GeneratorMethod",
	"GeneratorDeclaration",
	"GeneratorExpression",
	"GeneratorBody",
	"YieldExpression",
	"ClassDeclaration",
	"ClassExpression",
	"ClassTail",
	"ClassHeritage",
	"ClassBody",
	"ClassElement",
	"Module",
	"ModuleBody",
	"ModuleItem",
	"ImportDeclaration",
	"ImportClause",
	"ImportedDefaultBinding",
	"NameSpaceImport",
	"NamedImports",
	"FromClause",
	"ImportSpecifier",
	"ModuleSpecifier",
	"ImportedBinding",
	"ExportDeclaration",
	"ExportClause",
	"ExportSpecifier",
}
