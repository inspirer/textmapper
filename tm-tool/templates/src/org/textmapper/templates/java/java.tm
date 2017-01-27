# The Java� Language Specification
# Java SE 7 Edition

language java(java);

prefix = "Java"
package = "org.textmapper.templates.java"
positions = "line,offset"
endpositions = "offset"
#genast = true
#genastdef = true

:: lexer

# 3.5. Input Elements and Tokens

eoi: /\x1a/

# 3.6 White Space

WhiteSpace: /[\r\n\t\f\x20]|\r\n/	(space)

# 3.7 Comments

EndOfLineComment: /\/\/[^\r\n]*/ (space)

TraditionalComment: /\/\*([^*]|\*+[^\/*])*\*+\// (space)

# 3.8 Identifiers

Identifier {String}: /{JavaLetter}{JavaLetterOrDigit}*/  (class)

JavaLetter = /[a-zA-Z_$\p{L}\p{Nl}]|{UnicodeEscape}/
JavaLetterOrDigit = /[a-zA-Z0-9_$\p{L}\p{Nl}\p{Nd}]|{UnicodeEscape}/

# 3.9 Keywords

kw_abstract: /abstract/
kw_assert: /assert/
kw_boolean: /boolean/
kw_break: /break/
kw_byte: /byte/
kw_case: /case/
kw_catch: /catch/
kw_char: /char/
kw_class: /class/
kw_const: /const/
kw_continue: /continue/
kw_default: /default/
kw_do: /do/
kw_double: /double/
kw_else: /else/
kw_enum: /enum/
kw_extends: /extends/
kw_final: /final/
kw_finally: /finally/
kw_float: /float/
kw_for: /for/
kw_goto: /goto/
kw_if: /if/
kw_implements: /implements/
kw_import: /import/
kw_instanceof: /instanceof/
kw_int: /int/
kw_interface: /interface/
kw_long: /long/
kw_native: /native/
kw_new: /new/
kw_package: /package/
kw_private: /private/
kw_protected: /protected/
kw_public: /public/
kw_return: /return/
kw_short: /short/
kw_static: /static/
kw_strictfp: /strictfp/
kw_super: /super/
kw_switch: /switch/
kw_synchronized: /synchronized/
kw_this: /this/
kw_throw: /throw/
kw_throws: /throws/
kw_transient: /transient/
kw_try: /try/
kw_void: /void/
kw_volatile: /volatile/
kw_while: /while/

# 3.10.1 Integer Literals

IntegerLiteral: /(0|[1-9](_*{Digits})?)[lL]?/
IntegerLiteral: /{HexNumeral}[lL]?/
IntegerLiteral: /0_*{OctalDigits}[lL]?/
IntegerLiteral: /0[bB]{BinaryDigits}[lL]?/

Digits = /{Digit}({DigitsAndUnderscores}?{Digit})?/
Digit = /[0-9]/
DigitsAndUnderscores = /[0-9_]+/

HexNumeral = /0[xX]{HexDigits}/
HexDigits = /{HexDigit}({HexDigitsAndUnderscores}?{HexDigit})?/
HexDigit = /[0-9a-fA-F]/
HexDigitsAndUnderscores = /[0-9a-fA-F_]+/

OctalDigits = /{OctalDigit}({OctalDigitsAndUnderscores}?{OctalDigit})?/
OctalDigit = /[0-7]/
OctalDigitsAndUnderscores = /[0-7_]+/

BinaryDigits = /[01]([01_]*[01])?/

# 3.10.2. Floating-Point Literals

FloatingPointLiteral: /({Digits}\.{Digits}?|\.{Digits}){ExponentPart}?{FloatTypeSuffix}?/

FloatingPointLiteral: /{Digits}{ExponentPart}{FloatTypeSuffix}?/
FloatingPointLiteral: /{Digits}{FloatTypeSuffix}/

ExponentPart = /[eE][+-]?{Digits}/
FloatTypeSuffix = /[fFdD]/


FloatingPointLiteral: /{HexSignificand}{BinaryExponent}[fFdD]?/
BinaryExponent = /[pP][+-]?{Digits}/

HexSignificand = /{HexNumeral}\.?|0[xX]{HexDigits}?\.{HexDigits}/


# 3.10.3. Boolean Literals

BooleanLiteral: /true/
BooleanLiteral: /false/

# 3.10.4-6 String Literals

EscapeSequence = /\\[btnfr"'\\]|{OctalEscape}/
OctalEscape = /\\([0-3]?[0-7])?[0-7]/

CharacterLiteral: /'([^\r\n'\\]|{EscapeSequence}|{UnicodeEscape})'/

StringLiteral: /"([^\r\n"\\]|{EscapeSequence}|{UnicodeEscape})*"/

UnicodeEscape = /\\u+{HexDigit}{4}/

# 3.10.7. The Null Literal

NullLiteral: /null/

# 3.11. Separators

'(': /\(/
')': /\)/
'{': /{/
'}': /}/
'[': /\[/
']': /\]/
';': /;/
',': /,/
'.': /\./
'...': /\.\.\./

# 3.12. Operators

'=': /=/
'>': />/
'<': /</
'!': /!/
'~': /~/
'?': /?/
':': /:/
'==': /==/
'<=': /<=/
'>=': />=/
'!=': /!=/
'&&': /&&/
'||': /\|\|/
'++': /\+\+/
'--': /--/
'+': /+/
'-': /-/
'*': /\*/
'/': /\//
'&': /&/
'|': /\|/
'^': /\^/
'%': /%/
'<<': /<</
'>>': />>/
'>>>': />>>/
'+=': /+=/
'-=': /-=/
'*=': /\*=/
'/=': /\/=/
'&=': /&=/
'|=': /\|=/
'^=': /^=/
'%=': /%=/
'<<=': /<<=/
'>>=': />>=/
'>>>=': />>>=/
'@': /@/

:: parser

%input CompilationUnit, MethodBody, GenericMethodDeclaration, ClassBodyDeclaration, Expression, Statement;

QualifiedIdentifier :
	  Identifier
	| QualifiedIdentifier '.' Identifier
;

CompilationUnit :
	  PackageDeclaration? ImportDeclaration+? TypeDeclaration+? ;

PackageDeclaration :
	  Modifiers? kw_package QualifiedIdentifier ';' ;

ImportDeclaration :
	kw_import kw_static? QualifiedIdentifier ('.' '*')? ';' ;

TypeDeclaration :
	  ClassDeclaration
	| InterfaceDeclaration
	| EnumDeclaration
	| AnnotationTypeDeclaration
	| ';'
;

ClassDeclaration :
	  Modifiersopt kw_class Identifier TypeParameters? (kw_extends ClassType)? (kw_implements (InterfaceType separator ',')+)? ClassBody ;

EnumDeclaration :
	  Modifiersopt kw_enum Identifier TypeParameters? (kw_implements (InterfaceType separator ',')+)? EnumBody ;

InterfaceDeclaration :
	  Modifiersopt kw_interface Identifier TypeParameters? (kw_extends (InterfaceType separator ',')+)? InterfaceBody ;

AnnotationTypeDeclaration :
	  Modifiers? '@' kw_interface Identifier TypeParameters? (kw_extends ClassType)? (kw_implements (InterfaceType separator ',')+)? AnnotationTypeBody ;

Literal :
	  IntegerLiteral
	| FloatingPointLiteral
	| CharacterLiteral
	| StringLiteral
	| NullLiteral
	| BooleanLiteral
;

Type :
	  PrimitiveType
	| ReferenceType
;

PrimitiveType class :
	  kind=kw_byte
	| kind=kw_short
	| kind=kw_int
	| kind=kw_long
	| kind=kw_char
	| kind=kw_float
	| kind=kw_double
	| kind=kw_boolean
	| kind=kw_void
;

ReferenceType :
	  ClassOrInterfaceType
	| ArrayType
;

ClassOrInterfaceType :
	  ClassOrInterface
	| GenericType
;

ClassOrInterface :
	  QualifiedIdentifier
	| GenericType '.' QualifiedIdentifier
;

GenericType :
	  ClassOrInterface TypeArguments
	| ClassOrInterface '<' '>'
;

ArrayType :
	  PrimitiveType Dims
	| QualifiedIdentifier Dims
	| GenericType '.' QualifiedIdentifier Dims
	| GenericType Dims
;

ClassType :
	  ClassOrInterfaceType ;

Modifiers :
	  Modifier
	| Modifiers Modifier
;

Modifier interface :
	  kind=kw_public
	| kind=kw_protected
	| kind=kw_private
	| kind=kw_static
	| kind=kw_abstract
	| kind=kw_final
	| kind=kw_native
	| kind=kw_synchronized
	| kind=kw_transient
	| kind=kw_volatile
	| kind=kw_strictfp
	| Annotation
;

InterfaceType :
	  ClassOrInterfaceType ;

ClassBody :
	  '{' ClassBodyDeclaration* '}' ;

ClassBodyDeclaration :
	  ClassMemberDeclaration
	| StaticInitializer
	| ConstructorDeclaration
	| Block
;

ClassMemberDeclaration :
	  FieldDeclaration
	| MethodDeclaration
	| ClassDeclaration
	| InterfaceDeclaration
	| EnumDeclaration
	| AnnotationTypeDeclaration
	| ';'
;

GenericMethodDeclaration :
	  MethodDeclaration
	| ConstructorDeclaration
;

FieldDeclaration :
	  Modifiersopt Type VariableDeclarators ';' ;

VariableDeclarators :
	  VariableDeclarator
	| VariableDeclarators ',' VariableDeclarator
;

VariableDeclarator :
	  VariableDeclaratorId ('=' VariableInitializer)? ;

VariableDeclaratorId :
	  Identifier Dimsopt ;

VariableInitializer :
	  Expression
	| ArrayInitializer
;

MethodDeclaration :
	  AbstractMethodDeclaration
	| MethodHeader MethodBody
;

AbstractMethodDeclaration :
	  MethodHeader ';' ;

MethodHeader :
	  Modifiersopt TypeParameters? Type Identifier '(' (FormalParameter separator ',')* ')' Dimsopt MethodHeaderThrowsClauseopt ;

MethodHeaderThrowsClause :
	  kw_throws (ClassType separator ',')+ ;

FormalParameter :
	  Modifiersopt Type '...'? VariableDeclaratorId ;

CatchFormalParameter :
	  Modifiersopt CatchType VariableDeclaratorId ;

CatchType :
	  (Type separator '|')+ ;

MethodBody :
	  Block ;

StaticInitializer :
	  kw_static Block ;

ConstructorDeclaration :
	  Modifiersopt TypeParameters? Identifier '(' (FormalParameter separator ',')* ')' MethodHeaderThrowsClauseopt (MethodBody | ';') ;

ExplicitConstructorInvocation :
	  ExplicitConstructorId '(' (Expression separator ',')* ')' ';' ;

ExplicitConstructorId :
	  (Primary '.' | QualifiedIdentifier '.')? TypeArguments? ThisOrSuper ;

ThisOrSuper :
	  kw_this | kw_super ;

InterfaceBody :
	  '{' InterfaceMemberDeclaration* '}'  ;

InterfaceMemberDeclaration :
	  ConstantDeclaration
	| MethodHeader MethodBody
	| AbstractMethodDeclaration
	| ClassDeclaration
	| InterfaceDeclaration
	| EnumDeclaration
	| AnnotationTypeDeclaration
	| ';'
;

ConstantDeclaration :
	  FieldDeclaration ;

ArrayInitializer :
	  '{' (VariableInitializer separator ',')+? ','? '}' ;

Block :
	  '{' BlockStatement* '}' ;

BlockStatement :
	  LocalVariableDeclarationStatement
	| Statement
	| ClassDeclaration
	| InterfaceDeclaration
	| AnnotationTypeDeclaration
	| EnumDeclaration
;

LocalVariableDeclarationStatement :
	  LocalVariableDeclaration ';' ;

LocalVariableDeclaration :
	  Modifiers? Type VariableDeclarators ;

Statement :
	  AssertStatement
	| Block
	| EmptyStatement
	| ExpressionStatement
	| SwitchStatement
	| DoStatement
	| BreakStatement
	| ContinueStatement
	| ReturnStatement
	| SynchronizedStatement
	| ThrowStatement
	| TryStatement
	| LabeledStatement
	| IfStatement
	| WhileStatement
	| ForStatement
	| EnhancedForStatement
;

EmptyStatement :
	  ';' ;

LabeledStatement :
	  Label ':' Statement ;

Label :
	  Identifier ;

ExpressionStatement :
	  StatementExpression ';'
	| ExplicitConstructorInvocation
;

StatementExpression :
	  Assignment
	| PreIncrementExpression
	| PreDecrementExpression
	| PostIncrementExpression
	| PostDecrementExpression
	| MethodInvocation
	| ClassInstanceCreationExpression
;

%right kw_else;

IfStatement :
	  kw_if '(' Expression ')' Statement (kw_else Statement)? %prec kw_else ;

SwitchStatement :
	  kw_switch '(' Expression ')' '{' SwitchBlockStatementGroup* SwitchLabel+? '}' ;

SwitchBlockStatementGroup :
	SwitchLabel+ BlockStatement+ ;

SwitchLabel :
	  kw_case ConstantExpression ':'
	| kw_default ':'
;

WhileStatement :
	  kw_while '(' Expression ')' Statement ;

DoStatement :
	  kw_do Statement kw_while '(' Expression ')' ';' ;

ForStatement :
	  kw_for '(' ForInitopt ';' Expressionopt ';' (StatementExpression separator ',')* ')' Statement ;

EnhancedForStatement :
	  kw_for '(' Modifiers? Type Identifier Dimsopt ':' Expression ')' Statement ;

ForInit :
	  (StatementExpression separator ',')+
	| LocalVariableDeclaration
;

AssertStatement :
	  kw_assert Expression (':' Expression)? ';' ;

BreakStatement :
	  kw_break Identifieropt ';' ;

ContinueStatement :
	  kw_continue Identifieropt ';' ;

ReturnStatement :
	  kw_return Expressionopt ';' ;

ThrowStatement :
	  kw_throw Expression ';' ;

SynchronizedStatement :
	  kw_synchronized '(' Expression ')' Block ;

TryStatement :
	  kw_try ('(' (Resource separator ';')+ (';')? ')')? Block CatchClause* Finallyopt ;

Resource :
	  Modifiersopt Type VariableDeclaratorId '=' VariableInitializer ;

CatchClause :
	  kw_catch '(' CatchFormalParameter ')' Block ;

Finally :
	  kw_finally Block ;

Primary :
	  PrimaryNoNewArray
	| ArrayCreationWithArrayInitializer
	| ArrayCreationWithoutArrayInitializer
;

PrimaryNoNewArray :
	  Literal
	| kw_this
	| ParenthesizedExpression
	| ClassInstanceCreationExpression
	| FieldAccess
	| QualifiedIdentifier '.' ThisOrSuper
	| QualifiedIdentifier Dims? '.' kw_class
	| PrimitiveType Dims? '.' kw_class
	| MethodInvocation
	| ArrayAccess
;

ParenthesizedExpression :
	  '(' ExpressionNotName ')'
	| '(' QualifiedIdentifier ')'
;

ClassInstanceCreationExpression :
	  (Primary '.' | QualifiedIdentifier '.')? kw_new TypeArguments? ClassType '(' (Expression separator ',')* ')' ClassBodyopt ;

NonArrayType :
	  PrimitiveType
	| ClassOrInterfaceType
;

ArrayCreationWithoutArrayInitializer :
	  kw_new NonArrayType DimWithOrWithOutExpr+
;

ArrayCreationWithArrayInitializer :
	  kw_new NonArrayType DimWithOrWithOutExpr+ ArrayInitializer
;

DimWithOrWithOutExpr :
	  '[' Expression? ']' ;

Dims :
	  ('[' ']')+ ;

FieldAccess :
	  Primary '.' Identifier
	| kw_super '.' Identifier
;

MethodInvocation :
	  QualifiedIdentifier ('.' TypeArguments Identifier)? '(' (Expression separator ',')* ')'
	| Primary '.' TypeArguments? Identifier '(' (Expression separator ',')* ')'
	| kw_super '.' TypeArguments? Identifier '(' (Expression separator ',')* ')'
;

ArrayAccess :
	  QualifiedIdentifier '[' Expression ']'
	| PrimaryNoNewArray '[' Expression ']'
	| ArrayCreationWithArrayInitializer '[' Expression ']'
;

PostfixExpression :
	  Primary
	| QualifiedIdentifier
	| PostIncrementExpression
	| PostDecrementExpression
;

PostIncrementExpression :
	  PostfixExpression '++' ;

PostDecrementExpression :
	  PostfixExpression '--' ;

UnaryExpression :
	  PreIncrementExpression
	| PreDecrementExpression
	| '+' UnaryExpression
	| '-' UnaryExpression
	| UnaryExpressionNotPlusMinus
;

PreIncrementExpression :
	  '++' UnaryExpression ;

PreDecrementExpression :
	  '--' UnaryExpression ;

UnaryExpressionNotPlusMinus :
	  PostfixExpression
	| '~' UnaryExpression
	| '!' UnaryExpression
	| CastExpression
;

CastExpression :
	  '(' PrimitiveType Dimsopt ')' UnaryExpression
	| '(' QualifiedIdentifier TypeArguments ('.' ClassOrInterfaceType)? Dimsopt ')' UnaryExpressionNotPlusMinus
	| '(' QualifiedIdentifier Dims? ')' UnaryExpressionNotPlusMinus
;

ConditionalExpression :
	  ConditionalExpressionNotName
	| QualifiedIdentifier
;

AssignmentExpression :
	  ConditionalExpression
	| Assignment
;

LValue :
 	  ArrayAccess
 	| FieldAccess
 	| QualifiedIdentifier
;

Assignment :
	  LValue AssignmentOperator AssignmentExpression ;

AssignmentOperator :
	  '='
	| '*='
	| '/='
	| '%='
	| '+='
	| '-='
	| '<<='
	| '>>='
	| '>>>='
	| '&='
	| '^='
	| '|='
;

Expression :
	  AssignmentExpression ;

ConstantExpression :
	  Expression ;

EnumBody :
	  '{' (EnumConstant separator ',')+? ','? (';' ClassBodyDeclaration*)? '}' ;

EnumConstant :
	  Modifiersopt Identifier ('(' (Expression separator ',')* ')')? ClassBodyopt ;

TypeArguments :
	  '<' TypeArgumentList '>'
	| '<' (TypeArgumentList ',')? DeeperTypeArgument '>>'
	| '<' (TypeArgumentList ',')? ('?' (kw_extends|kw_super))? ClassOrInterface '<' (TypeArgumentList ',')? DeeperTypeArgument '>>>'
;

TypeArgumentList :
	  TypeArgument
	| TypeArgumentList ',' TypeArgument
;

TypeArgument :
	  ReferenceType
	| Wildcard
;

ReferenceType1 :
	  ReferenceType '>'
	| ClassOrInterface '<' TypeArgumentList '>>'
	| ClassOrInterface '<' (TypeArgumentList ',')? DeeperTypeArgument '>>>'
;

Wildcard :
	  '?' ((kw_extends|kw_super) ReferenceType)? ;

DeeperTypeArgument :
	  ('?' (kw_extends|kw_super))? ClassOrInterface '<' TypeArgumentList ;

TypeParameters :
	  '<' (TypeParameterList ',')? TypeParameter1 ;

TypeParameterList :
	  TypeParameter
	| TypeParameterList ',' TypeParameter
;

TypeParameter :
	  Identifier (kw_extends ReferenceType AdditionalBoundList?)? ;

TypeParameter1 :
	  Identifier '>'
	| Identifier kw_extends (ReferenceType AdditionalBoundList? '&')? ReferenceType1
;

AdditionalBoundList :
	  AdditionalBound
	| AdditionalBoundList AdditionalBound
;

AdditionalBound :
	  '&' ReferenceType ;

PostfixExpressionNotName :
	  Primary
	| PostIncrementExpression
	| PostDecrementExpression
;

UnaryExpressionNotName :
	  PreIncrementExpression
	| PreDecrementExpression
	| '+' UnaryExpression
	| '-' UnaryExpression
	| UnaryExpressionNotPlusMinusNotName
;

UnaryExpressionNotPlusMinusNotName :
	  PostfixExpressionNotName
	| '~' UnaryExpression
	| '!' UnaryExpression
	| CastExpression
;

%left '||';
%left '&&';
%left '|';
%left '^';
%left '&';
%left '==' '!=';
%nonassoc '>' '<' '>=' '<=' kw_instanceof;
%left '<<' '>>' '>>>';
%left '+' '-';
%left '*' '/' '%';

ArithmeticExpressionNotName :
	  UnaryExpressionNotName
	| ArithmeticPart '*' ArithmeticPart
	| ArithmeticPart '/' ArithmeticPart
	| ArithmeticPart '%' ArithmeticPart
	| ArithmeticPart '+' ArithmeticPart
	| ArithmeticPart '-' ArithmeticPart
	| ArithmeticPart '<<' ArithmeticPart
	| ArithmeticPart '>>' ArithmeticPart
	| ArithmeticPart '>>>' ArithmeticPart
;

ArithmeticPart :
	  ArithmeticExpressionNotName
	| QualifiedIdentifier
;

RelationalExpressionNotName :
	  ArithmeticExpressionNotName
	| ArithmeticExpressionNotName '<' ArithmeticPart
	| QualifiedIdentifier '<' ArithmeticPart
	| ArithmeticPart '>' ArithmeticPart
	| ArithmeticPart '<=' ArithmeticPart
	| ArithmeticPart '>=' ArithmeticPart
	| ArithmeticPart kw_instanceof ReferenceType
	| RelationalPart '==' RelationalPart
	| RelationalPart '!=' RelationalPart
;

RelationalPart :
	  RelationalExpressionNotName
	| QualifiedIdentifier
;

LogicalExpressionNotName :
	  RelationalExpressionNotName
	| BooleanOrBitwisePart '&' BooleanOrBitwisePart
	| BooleanOrBitwisePart '^' BooleanOrBitwisePart
	| BooleanOrBitwisePart '|' BooleanOrBitwisePart
	| BooleanOrBitwisePart '&&' BooleanOrBitwisePart
	| BooleanOrBitwisePart '||' BooleanOrBitwisePart
;

BooleanOrBitwisePart :
	  LogicalExpressionNotName
	| QualifiedIdentifier
;

ConditionalExpressionNotName :
	  LogicalExpressionNotName
	| BooleanOrBitwisePart '?' Expression ':' ConditionalExpression
;

ExpressionNotName :
	  ConditionalExpressionNotName
	| Assignment
;

AnnotationTypeBody :
	  '{' AnnotationTypeMemberDeclaration* '}' ;

AnnotationTypeMemberDeclaration :
	  Modifiersopt TypeParameters? Type Identifier '(' (FormalParameter separator ',')* ')' Dimsopt DefaultValueopt ';'
	| ConstantDeclaration
	| ConstructorDeclaration
	| TypeDeclaration
;

DefaultValue :
	  kw_default MemberValue ;

Annotation :
	  '@' QualifiedIdentifier
	| '@' QualifiedIdentifier '(' MemberValue ')'
	| '@' QualifiedIdentifier '(' (MemberValuePair separator ',')* ')'
;

MemberValuePair :
	  Identifier '=' MemberValue ;

MemberValue :
	  ConditionalExpression
	| Annotation
	| MemberValueArrayInitializer
;

MemberValueArrayInitializer :
	  '{' (MemberValue separator ',')+? ','? '}' ;
