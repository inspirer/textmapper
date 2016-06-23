# Typescript 1.8, Jan 2016

# A.1 Types

TypeParameters:
	'<' TypeParameterList '>'

TypeParameterList:
	TypeParameter
	TypeParameterList ',' TypeParameter

TypeParameter:
	BindingIdentifier Constraintopt

Constraint:
	'extends' Type

TypeArguments:
	'<' TypeArgumentList '>'

TypeArgumentList:
	TypeArgument
	TypeArgumentList ',' TypeArgument

TypeArgument:
	Type

Type:
	UnionOrIntersectionOrPrimaryType
	FunctionType
	ConstructorType

UnionOrIntersectionOrPrimaryType:
	UnionType
	IntersectionOrPrimaryType

IntersectionOrPrimaryType:
	IntersectionType
	PrimaryType

PrimaryType:
	ParenthesizedType
	PredefinedType
	TypeReference
	ObjectType
	ArrayType
	TupleType
	TypeQuery
	ThisType

ParenthesizedType:
	'(' Type ')'

PredefinedType:
	'any'
	'number'
	'boolean'
	'string'
	'symbol'
	'void'

TypeReference:
	TypeName /*no LineTerminator here*/ TypeArgumentsopt

TypeName:
	IdentifierReference
	NamespaceName '.' IdentifierReference

NamespaceName:
	IdentifierReference
	NamespaceName '.' IdentifierReference

ObjectType:
	'{' TypeBodyopt '}'

TypeBody:
	TypeMemberList ';'opt
	TypeMemberList ','opt

TypeMemberList:
	TypeMember
	TypeMemberList ';' TypeMember
	TypeMemberList ',' TypeMember

TypeMember:
	PropertySignature
	CallSignature
	ConstructSignature
	IndexSignature
	MethodSignature

ArrayType:
	PrimaryType /*no LineTerminator here*/ '[' ']'

TupleType:
	'[' TupleElementTypes ']'

TupleElementTypes:
	TupleElementType
	TupleElementTypes ',' TupleElementType

TupleElementType:
	Type

UnionType:
	UnionOrIntersectionOrPrimaryType '|' IntersectionOrPrimaryType

IntersectionType:
	IntersectionOrPrimaryType '&' PrimaryType

FunctionType:
	TypeParametersopt '(' ParameterListopt ')' '=>' Type

ConstructorType:
	'new' TypeParametersopt '(' ParameterListopt ')' '=>' Type

TypeQuery:
	'typeof' TypeQueryExpression

TypeQueryExpression:
	IdentifierReference
	TypeQueryExpression '.' IdentifierName

ThisType:
	'this'

PropertySignature:
	PropertyName '?'opt TypeAnnotationopt

PropertyName:
	IdentifierName
	StringLiteral
	NumericLiteral

TypeAnnotation:
	':' Type

CallSignature:
	TypeParametersopt '(' ParameterListopt ')' TypeAnnotationopt

ParameterList:
	RequiredParameterList
	OptionalParameterList
	RestParameter
	RequiredParameterList ',' OptionalParameterList
	RequiredParameterList ',' RestParameter
	OptionalParameterList ',' RestParameter
	RequiredParameterList ',' OptionalParameterList ',' RestParameter

RequiredParameterList:
	RequiredParameter
	RequiredParameterList ',' RequiredParameter

RequiredParameter:
	AccessibilityModifieropt BindingIdentifierOrPattern TypeAnnotationopt
	BindingIdentifier ':' StringLiteral

AccessibilityModifier:
	'public'
	'private'
	'protected'

BindingIdentifierOrPattern:
	BindingIdentifier
	BindingPattern

OptionalParameterList:
	OptionalParameter
	OptionalParameterList ',' OptionalParameter

OptionalParameter:
	AccessibilityModifieropt BindingIdentifierOrPattern '?' TypeAnnotationopt
	AccessibilityModifieropt BindingIdentifierOrPattern TypeAnnotationopt Initializer
	BindingIdentifier '?' ':' StringLiteral

RestParameter:
	'...' BindingIdentifier TypeAnnotationopt

ConstructSignature:
	'new' TypeParametersopt '(' ParameterListopt ')' TypeAnnotationopt

IndexSignature:
	'[' BindingIdentifier ':' 'string' ']' TypeAnnotation
	'[' BindingIdentifier ':' 'number' ']' TypeAnnotation

MethodSignature:
	PropertyName '?'opt CallSignature

TypeAliasDeclaration:
	'type' BindingIdentifier TypeParametersopt '=' Type ';'

# A.2 Expressions

PropertyDefinition: /* Modified */
	IdentifierReference
	CoverInitializedName
	PropertyName ':' AssignmentExpression
	PropertyName CallSignature '{' FunctionBody '}'
	GetAccessor
	SetAccessor

GetAccessor:
	'get' PropertyName '(' ')' TypeAnnotationopt '{' FunctionBody '}'

SetAccessor:
	'set' PropertyName '(' BindingIdentifierOrPattern TypeAnnotationopt ')' '{' FunctionBody '}'

FunctionExpression: /* Modified */
	'function' BindingIdentifieropt CallSignature '{' FunctionBody '}'

ArrowFormalParameters: /* Modified */
	CallSignature

Arguments: /* Modified */
	TypeArgumentsopt '(' ArgumentListopt ')'

UnaryExpression: /* Extension */
	'<' Type '>' UnaryExpression

A.3 Statements

Declaration: /* Extension */
	InterfaceDeclaration
	TypeAliasDeclaration
	EnumDeclaration

VariableDeclaration: /* Modified */
	SimpleVariableDeclaration
	DestructuringVariableDeclaration

SimpleVariableDeclaration:
	BindingIdentifier TypeAnnotationopt Initializeropt

DestructuringVariableDeclaration:
	BindingPattern TypeAnnotationopt Initializer

LexicalBinding: /* Modified */
	SimpleLexicalBinding
	DestructuringLexicalBinding

SimpleLexicalBinding:
	BindingIdentifier TypeAnnotationopt Initializeropt

DestructuringLexicalBinding:
	BindingPattern TypeAnnotationopt Initializeropt

A.4 Functions

FunctionDeclaration: /* Modified */
	'function' BindingIdentifieropt CallSignature '{' FunctionBody '}'
	'function' BindingIdentifieropt CallSignature ';'

A.5 Interfaces

InterfaceDeclaration:
	'interface' BindingIdentifier TypeParametersopt InterfaceExtendsClauseopt ObjectType

InterfaceExtendsClause:
	'extends' ClassOrInterfaceTypeList

ClassOrInterfaceTypeList:
	ClassOrInterfaceType
	ClassOrInterfaceTypeList ',' ClassOrInterfaceType

ClassOrInterfaceType:
	TypeReference

A.6 Classes

ClassDeclaration: /* Modified */
	'class' BindingIdentifieropt TypeParametersopt ClassHeritage '{' ClassBody '}'

ClassHeritage: /* Modified */
	ClassExtendsClauseopt ImplementsClauseopt

ClassExtendsClause:
	'extends' ClassType

ClassType:
	TypeReference

ImplementsClause:
	'implements' ClassOrInterfaceTypeList

ClassElement: /* Modified */
	ConstructorDeclaration
	PropertyMemberDeclaration
	IndexMemberDeclaration

ConstructorDeclaration:
	AccessibilityModifieropt 'constructor' '(' ParameterListopt ')' '{' FunctionBody '}'
	AccessibilityModifieropt 'constructor' '(' ParameterListopt ')' ';'

PropertyMemberDeclaration:
	MemberVariableDeclaration
	MemberFunctionDeclaration
	MemberAccessorDeclaration

MemberVariableDeclaration:
	AccessibilityModifieropt 'static'opt PropertyName TypeAnnotationopt Initializeropt ';'

MemberFunctionDeclaration:
	AccessibilityModifieropt 'static'opt PropertyName CallSignature '{' FunctionBody '}'
	AccessibilityModifieropt 'static'opt PropertyName CallSignature ';'

MemberAccessorDeclaration:
	AccessibilityModifieropt 'static'opt GetAccessor
	AccessibilityModifieropt 'static'opt SetAccessor

IndexMemberDeclaration:
	IndexSignature ';'

A.7 Enums

EnumDeclaration:
	'const'opt 'enum' BindingIdentifier '{' EnumBodyopt '}'

EnumBody:
	EnumMemberList ','opt

EnumMemberList:
	EnumMember
	EnumMemberList ',' EnumMember

EnumMember:
	PropertyName
	PropertyName '=' EnumValue

EnumValue:
	AssignmentExpression

A.8 Namespaces

NamespaceDeclaration:
	'namespace' IdentifierPath '{' NamespaceBody '}'

IdentifierPath:
	BindingIdentifier
	IdentifierPath '.' BindingIdentifier

NamespaceBody:
	NamespaceElementsopt

NamespaceElements:
	NamespaceElement
	NamespaceElements NamespaceElement

NamespaceElement:
	Statement
	LexicalDeclaration
	FunctionDeclaration
	GeneratorDeclaration
	ClassDeclaration
	InterfaceDeclaration
	TypeAliasDeclaration
	EnumDeclaration
	NamespaceDeclaration
	AmbientDeclaration
	ImportAliasDeclaration
	ExportNamespaceElement

ExportNamespaceElement:
	'export' VariableStatement
	'export' LexicalDeclaration
	'export' FunctionDeclaration
	'export' GeneratorDeclaration
	'export' ClassDeclaration
	'export' InterfaceDeclaration
	'export' TypeAliasDeclaration
	'export' EnumDeclaration
	'export' NamespaceDeclaration
	'export' AmbientDeclaration
	'export' ImportAliasDeclaration

ImportAliasDeclaration:
	'import' BindingIdentifier '=' EntityName ';'

EntityName:
	NamespaceName
	NamespaceName '.' IdentifierReference

A.9 Scripts and Modules

SourceFile:
	ImplementationSourceFile
	DeclarationSourceFile

ImplementationSourceFile:
	ImplementationScript
	ImplementationModule

DeclarationSourceFile:
	DeclarationScript
	DeclarationModule

ImplementationScript:
	ImplementationScriptElementsopt

ImplementationScriptElements:
	ImplementationScriptElement
	ImplementationScriptElements ImplementationScriptElement

ImplementationScriptElement:
	ImplementationElement
	AmbientModuleDeclaration

ImplementationElement:
	Statement
	LexicalDeclaration
	FunctionDeclaration
	GeneratorDeclaration
	ClassDeclaration
	InterfaceDeclaration
	TypeAliasDeclaration
	EnumDeclaration
	NamespaceDeclaration
	AmbientDeclaration
	ImportAliasDeclaration

DeclarationScript:
	DeclarationScriptElementsopt

DeclarationScriptElements:
	DeclarationScriptElement
	DeclarationScriptElements DeclarationScriptElement

DeclarationScriptElement:
	DeclarationElement
	AmbientModuleDeclaration

DeclarationElement:
	InterfaceDeclaration
	TypeAliasDeclaration
	NamespaceDeclaration
	AmbientDeclaration
	ImportAliasDeclaration

ImplementationModule:
	ImplementationModuleElementsopt

ImplementationModuleElements:
	ImplementationModuleElement
	ImplementationModuleElements ImplementationModuleElement

ImplementationModuleElement:
	ImplementationElement
	ImportDeclaration
	ImportAliasDeclaration
	ImportRequireDeclaration
	ExportImplementationElement
	ExportDefaultImplementationElement
	ExportListDeclaration
	ExportAssignment

DeclarationModule:
	DeclarationModuleElementsopt

DeclarationModuleElements:
	DeclarationModuleElement
	DeclarationModuleElements DeclarationModuleElement

DeclarationModuleElement:
	DeclarationElement
	ImportDeclaration
	ImportAliasDeclaration
	ExportDeclarationElement
	ExportDefaultDeclarationElement
	ExportListDeclaration
	ExportAssignment

ImportRequireDeclaration:
	'import' BindingIdentifier '=' 'require' '(' StringLiteral ')' ';'

ExportImplementationElement:
	'export' VariableStatement
	'export' LexicalDeclaration
	'export' FunctionDeclaration
	'export' GeneratorDeclaration
	'export' ClassDeclaration
	'export' InterfaceDeclaration
	'export' TypeAliasDeclaration
	'export' EnumDeclaration
	'export' NamespaceDeclaration
	'export' AmbientDeclaration
	'export' ImportAliasDeclaration

ExportDeclarationElement:
	'export' InterfaceDeclaration
	'export' TypeAliasDeclaration
	'export' AmbientDeclaration
	'export' ImportAliasDeclaration

ExportDefaultImplementationElement:
	'export' 'default' FunctionDeclaration
	'export' 'default' GeneratorDeclaration
	'export' 'default' ClassDeclaration
	'export' 'default' AssignmentExpression ';'

ExportDefaultDeclarationElement:
	'export' 'default' AmbientFunctionDeclaration
	'export' 'default' AmbientClassDeclaration
	'export' 'default' IdentifierReference ';'

ExportListDeclaration:
	'export' '*' FromClause ';'
	'export' ExportClause FromClause ';'
	'export' ExportClause ';'

ExportAssignment:
	'export' '=' IdentifierReference ';'

A.10 Ambients

AmbientDeclaration:
	'declare' AmbientVariableDeclaration
	'declare' AmbientFunctionDeclaration
	'declare' AmbientClassDeclaration
	'declare' AmbientEnumDeclaration
	'declare' AmbientNamespaceDeclaration

AmbientVariableDeclaration:
	'var' AmbientBindingList ';'
	'let' AmbientBindingList ';'
	'const' AmbientBindingList ';'

AmbientBindingList:
	AmbientBinding
	AmbientBindingList ',' AmbientBinding

AmbientBinding:
	BindingIdentifier TypeAnnotationopt

AmbientFunctionDeclaration:
	'function' BindingIdentifier CallSignature ';'

AmbientClassDeclaration:
	'class' BindingIdentifier TypeParametersopt ClassHeritage '{' AmbientClassBody '}'

AmbientClassBody:
	AmbientClassBodyElementsopt

AmbientClassBodyElements:
	AmbientClassBodyElement
	AmbientClassBodyElements AmbientClassBodyElement

AmbientClassBodyElement:
	AmbientConstructorDeclaration
	AmbientPropertyMemberDeclaration
	IndexSignature

AmbientConstructorDeclaration:
	'constructor' '(' ParameterListopt ')' ';'

AmbientPropertyMemberDeclaration:
	AccessibilityModifieropt 'static'opt PropertyName TypeAnnotationopt ';'
	AccessibilityModifieropt 'static'opt PropertyName CallSignature ';'

AmbientEnumDeclaration:
	EnumDeclaration

AmbientNamespaceDeclaration:
	'namespace' IdentifierPath '{' AmbientNamespaceBody '}'

AmbientNamespaceBody:
	AmbientNamespaceElementsopt

AmbientNamespaceElements:
	AmbientNamespaceElement
	AmbientNamespaceElements AmbientNamespaceElement

AmbientNamespaceElement:
	'export'opt AmbientVariableDeclaration
	'export'opt AmbientLexicalDeclaration
	'export'opt AmbientFunctionDeclaration
	'export'opt AmbientClassDeclaration
	'export'opt InterfaceDeclaration
	'export'opt AmbientEnumDeclaration
	'export'opt AmbientNamespaceDeclaration
	'export'opt ImportAliasDeclaration

AmbientModuleDeclaration:
	'declare' 'module' StringLiteral '{' DeclarationModule '}'
