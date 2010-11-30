
lang = "java"
prefix = "Java"
package = "org.textway.lapg.gen.java"
lexemend = "on"
gentree = "on"
genast = "on"
packLexems = "true"
packTables = "true"
astprefix = "Java"
positions = "line,offset"
endpositions = "offset"

[0]

Identifier:     /[a-zA-Z_][A-Za-z_0-9]*/ -1

abstract:   	/abstract/
assert:     	/assert/
boolean:    	/boolean/
break:      	/break/
byte:       	/byte/
case:       	/case/
catch:      	/catch/
char:       	/char/
class:      	/class/
continue:   	/continue/
const:      	/const/
default:    	/default/
do:         	/do/
double:     	/double/
else:       	/else/
enum:       	/enum/
extends:    	/extends/
boolFalse:      /false/
final:      	/final/
finally:    	/finally/
float:      	/float/
for:        	/for/
goto:       	/goto/
if:         	/if/
implements: 	/implements/
import:     	/import/
instanceof: 	/instanceof/
int:        	/int/
interface:  	/interface/
long:       	/long/
native:     	/native/
new:        	/new/
null:       	/null/
package:    	/package/
private:    	/private/
protected:  	/protected/
public:     	/public/
return:     	/return/
short:      	/short/
static:     	/static/
strictfp:   	/strictfp/
super:      	/super/
switch:     	/switch/
synchronized:	/synchronized/
this:       	/this/
throw:      	/throw/
throws:     	/throws/
transient:  	/transient/
boolTrue:       /true/
try:        	/try/
void:       	/void/
volatile:   	/volatile/
while:      	/while/

IntegerLiteral:			/([1-9][0-9]*|0[0-7]*|0[xX][0-9a-fA-F]+)([uU](l|L|ll|LL)?|(l|L|ll|LL)[uU]?)?/

FloatingPointLiteral:		/([0-9]*\.[0-9]+|[0-9]+\.)([eE][+-]?[0-9]+)?[flFL]?/
FloatingPointLiteral:		/[0-9]+[eE][+-]?[0-9]+[flFL]?|[0-9]+[fF]/
FloatingPointLiteral:		/0[xX]([0-9a-fA-F]*\.[0-9a-fA-F]+|[0-9a-fA-F]+\.?)[pP][+-]?[0-9]+[flFL]?/

StringLiteral:      /"([^"\\]|\\(['"?\\abfnrtv]|x[0-9a-fA-F]+|[0-7]([0-7][0-7]?)?))*"/
CharacterLiteral:	/'([^\n\\']|\\(['"?\\abfnrtv]|x[0-9a-fA-F]+|[0-7]([0-7][0-7]?)?))+'/


'++':  	/\+\+/
'--':  	/\-\-/
'==':  	/==/
'<=':  	/<=/
'>=':  	/>=/
'!=':  	/!=/
'<<':  	/<</
'>>':  	/>>/
'>>>': 	/>>>/
'+=':  	/\+=/
'-=':  	/\-=/
'*=':  	/\*=/
'/=':  	/\/=/
'&=':  	/&=/
'|=':  	/\|=/
'^=':  	/\^=/
'%=':  	/%=/
'<<=': 	/<<=/
'>>=': 	/>>=/
'>>>=':	/>>>=/
'||':  	/\|\|/
'&&':  	/&&/
'+':   	/\+/
'-':   	/\-/
'!':   	/!/
'%':   	/%/
'^':   	/\^/
'&':   	/&/
'*':   	/\*/
'|':   	/\|/
'~':   	/~/
'/':   	/\//
'>':   	/>/
'<':   	/</
'(':   	/\(/
')':   	/\)/
'{':   	/{/
'}':   	/}/
'[':   	/\[/
']':   	/\]/
';':   	/;/
'?':   	/\?/
':':   	/:/
',':   	/,/
'.':   	/\./
'=':   	/=/
'@':   	/@/
'...': 	/\.\.\./


comment:	/\/\/\/.*/		{ return false; }

_skip:      /\/\/([^\/\n\r].*)?/
_skip:		/#.*/
_skip:      /[\t\r\n ]+/    { return false; }

'/*':   /\/\*/          { group = 1; return false; }
[1]
anysym1: /[^*]+/		{ return false; }
anysym1: /*/
'*/':    /\*\//         { group = 0; return false; }

input ::=
	  CompilationUnit ;

Literal ::=
	  IntegerLiteral
	| FloatingPointLiteral
	| CharacterLiteral
	| StringLiteral
	| null
	| BooleanLiteral
;

BooleanLiteral ::=
	  boolTrue
	| boolFalse
;

Type ::=
	  PrimitiveType
	| ReferenceType
;

PrimitiveType ::=
	  NumericType
	| boolean
	| void
;

NumericType ::=
	  IntegralType
	| FloatingPointType
;

IntegralType ::=
	  byte
	| short
	| int
	| long
	| char
;

FloatingPointType ::=
	  float
	| double
;

ReferenceType ::=
	  ClassOrInterfaceType
	| ArrayType
;

ClassOrInterfaceType ::=
	  ClassOrInterface
	| GenericType
;

ClassOrInterface ::=
	  Name
	| GenericType '.' Name
;

GenericType ::=
	  ClassOrInterface TypeArguments ;

ArrayTypeWithTypeArgumentsName ::=
	  GenericType '.' Name ;

ArrayType ::=
	  PrimitiveType Dims
	| Name Dims
	| ArrayTypeWithTypeArgumentsName Dims
	| GenericType Dims
;

ClassType ::=
	  ClassOrInterfaceType ;

Name ::=
	  SimpleName
	| QualifiedName
;

SimpleName ::=
	  Identifier ;

QualifiedName ::=
	  Name '.' SimpleName ;

CompilationUnit ::=
	  InternalCompilationUnit ;

InternalCompilationUnit ::=
	  PackageDeclaration
	| PackageDeclaration ImportDeclarations
	| PackageDeclaration ImportDeclarations TypeDeclarations
	| PackageDeclaration TypeDeclarations
	| ImportDeclarations
	| TypeDeclarations
	| ImportDeclarations TypeDeclarations
	| 
;

Header ::=
	  ImportDeclaration
	| PackageDeclaration
	| ClassHeader
	| InterfaceHeader
	| EnumHeader
	| AnnotationTypeDeclarationHeader
	| StaticInitializer
	| RecoveryMethodHeader
	| FieldDeclaration
	| AllocationHeader
	| ArrayCreationHeader
;

Header1 ::=
	  Header
	| ConstructorHeader
;

Header2 ::=
	  Header
	| EnumConstantHeader
;

CatchHeader ::=
	  catch '(' FormalParameter ')' '{' ;

ImportDeclarations ::=
	  ImportDeclaration
	| ImportDeclarations ImportDeclaration
;

TypeDeclarations ::=
	  TypeDeclaration
	| TypeDeclarations TypeDeclaration
;

PackageDeclaration ::=
	  PackageDeclarationName ';' ;

PackageDeclarationName ::=
	  Modifiers package Name
	| package Name
;

ImportDeclaration ::=
	  SingleTypeImportDeclaration
	| TypeImportOnDemandDeclaration
	| SingleStaticImportDeclaration
	| StaticImportOnDemandDeclaration
;

SingleTypeImportDeclaration ::=
	  SingleTypeImportDeclarationName ';' ;

SingleTypeImportDeclarationName ::=
	  import Name ;

TypeImportOnDemandDeclaration ::=
	  TypeImportOnDemandDeclarationName ';' ;

TypeImportOnDemandDeclarationName ::=
	  import Name '.' '*' ;

TypeDeclaration ::=
	  ClassDeclaration
	| InterfaceDeclaration
	| ';'
	| EnumDeclaration
	| AnnotationTypeDeclaration
;

Modifiers ::=
	  Modifier
	| Modifiers Modifier
;

Modifier ::=
	  public
	| protected
	| private
	| static
	| abstract
	| final
	| native
	| synchronized
	| transient
	| volatile
	| strictfp
	| Annotation
;

ClassDeclaration ::=
	  ClassHeader ClassBody ;

ClassHeader ::=
	  ClassHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt ;

ClassHeaderName ::=
	  ClassHeaderName1 TypeParameters
	| ClassHeaderName1
;

ClassHeaderName1 ::=
	  Modifiersopt class Identifier ;

ClassHeaderExtends ::=
	  extends ClassType ;

ClassHeaderImplements ::=
	  implements InterfaceTypeList ;

InterfaceTypeList ::=
	  InterfaceType
	| InterfaceTypeList ',' InterfaceType
;

InterfaceType ::=
	  ClassOrInterfaceType ;

ClassBody ::=
	  '{' ClassBodyDeclarationsopt '}' ;

ClassBodyDeclarations ::=
	  ClassBodyDeclaration
	| ClassBodyDeclarations ClassBodyDeclaration
;

ClassBodyDeclaration ::=
	  ClassMemberDeclaration
	| StaticInitializer
	| ConstructorDeclaration
	| Block
;

Initializer ::=
	  Block ;

ClassMemberDeclaration ::=
	  FieldDeclaration
	| MethodDeclaration
	| ClassDeclaration
	| InterfaceDeclaration
	| EnumDeclaration
	| AnnotationTypeDeclaration
	| ';'
;

GenericMethodDeclaration ::=
	  MethodDeclaration
	| ConstructorDeclaration
;

FieldDeclaration ::=
	  Modifiersopt Type VariableDeclarators ';' ;

VariableDeclarators ::=
	  VariableDeclarator
	| VariableDeclarators ',' VariableDeclarator
;

VariableDeclarator ::=
	  VariableDeclaratorId
	| VariableDeclaratorId '=' VariableInitializer
;

VariableDeclaratorId ::=
	  Identifier Dimsopt ;

VariableInitializer ::=
	  Expression
	| ArrayInitializer
;

MethodDeclaration ::=
	  AbstractMethodDeclaration
	| MethodHeader MethodBody
;

AbstractMethodDeclaration ::=
	  MethodHeader ';' ;

MethodHeader ::=
	  MethodHeaderName FormalParameterListopt ')' MethodHeaderExtendedDims MethodHeaderThrowsClauseopt ;

MethodHeaderName ::=
	  Modifiersopt TypeParameters Type Identifier '('
	| Modifiersopt Type Identifier '('
;

MethodHeaderExtendedDims ::=
	  Dimsopt ;

MethodHeaderThrowsClause ::=
	  throws ClassTypeList ;

ConstructorHeader ::=
	  ConstructorHeaderName FormalParameterListopt ')' MethodHeaderThrowsClauseopt ;

ConstructorHeaderName ::=
	  Modifiersopt TypeParameters Identifier '('
	| Modifiersopt Identifier '('
;

FormalParameterList ::=
	  FormalParameter
	| FormalParameterList ',' FormalParameter
;

FormalParameter ::=
	  Modifiersopt Type VariableDeclaratorId
	| Modifiersopt Type '...' VariableDeclaratorId
;

ClassTypeList ::=
	  ClassTypeElt
	| ClassTypeList ',' ClassTypeElt
;

ClassTypeElt ::=
	  ClassType ;

MethodBody ::=
	  '{' BlockStatementsopt '}' ;

StaticInitializer ::=
	  StaticOnly Block ;

StaticOnly ::=
	  static ;

ConstructorDeclaration ::=
	  ConstructorHeader MethodBody
	| ConstructorHeader ';'
;

ExplicitConstructorInvocation ::=
	  this '(' ArgumentListopt ')' ';'
	| OnlyTypeArguments this '(' ArgumentListopt ')' ';'
	| super '(' ArgumentListopt ')' ';'
	| OnlyTypeArguments super '(' ArgumentListopt ')' ';'
	| Primary '.' super '(' ArgumentListopt ')' ';'
	| Primary '.' OnlyTypeArguments super '(' ArgumentListopt ')' ';'
	| Name '.' super '(' ArgumentListopt ')' ';'
	| Name '.' OnlyTypeArguments super '(' ArgumentListopt ')' ';'
	| Primary '.' this '(' ArgumentListopt ')' ';'
	| Primary '.' OnlyTypeArguments this '(' ArgumentListopt ')' ';'
	| Name '.' this '(' ArgumentListopt ')' ';'
	| Name '.' OnlyTypeArguments this '(' ArgumentListopt ')' ';'
;

InterfaceDeclaration ::=
	  InterfaceHeader InterfaceBody ;

InterfaceHeader ::=
	  InterfaceHeaderName InterfaceHeaderExtendsopt ;

InterfaceHeaderName ::=
	  InterfaceHeaderName1 TypeParameters
	| InterfaceHeaderName1
;

InterfaceHeaderName1 ::=
	  Modifiersopt interface Identifier ;

InterfaceHeaderExtends ::=
	  extends InterfaceTypeList ;

InterfaceBody ::=
	  '{' InterfaceMemberDeclarationsopt '}' ;

InterfaceMemberDeclarations ::=
	  InterfaceMemberDeclaration
	| InterfaceMemberDeclarations InterfaceMemberDeclaration
;

InterfaceMemberDeclaration ::=
	  ';'
	| ConstantDeclaration
	| MethodHeader MethodBody
	| AbstractMethodDeclaration
	| InvalidConstructorDeclaration
	| ClassDeclaration
	| InterfaceDeclaration
	| EnumDeclaration
	| AnnotationTypeDeclaration
;

InvalidConstructorDeclaration ::=
	  ConstructorHeader MethodBody
	| ConstructorHeader ';'
;

ConstantDeclaration ::=
	  FieldDeclaration ;

ArrayInitializer ::=
	  '{' Commaopt '}'
	| '{' VariableInitializers '}'
	| '{' VariableInitializers ',' '}'
;

VariableInitializers ::=
	  VariableInitializer
	| VariableInitializers ',' VariableInitializer
;

Block ::=
	  '{' BlockStatementsopt '}' ;

BlockStatements ::=
	  BlockStatement
	| BlockStatements BlockStatement
;

BlockStatement ::=
	  LocalVariableDeclarationStatement
	| Statement
	| ClassDeclaration
	| InterfaceDeclaration
	| AnnotationTypeDeclaration
	| EnumDeclaration
;

LocalVariableDeclarationStatement ::=
	  LocalVariableDeclaration ';' ;

LocalVariableDeclaration ::=
	  Type VariableDeclarators
	| Modifiers Type VariableDeclarators
;

Statement ::=
	  StatementWithoutTrailingSubstatement
	| LabeledStatement
	| IfThenStatement
	| IfThenElseStatement
	| WhileStatement
	| ForStatement
	| EnhancedForStatement
;

StatementNoShortIf ::=
	  StatementWithoutTrailingSubstatement
	| LabeledStatementNoShortIf
	| IfThenElseStatementNoShortIf
	| WhileStatementNoShortIf
	| ForStatementNoShortIf
	| EnhancedForStatementNoShortIf
;

StatementWithoutTrailingSubstatement ::=
	  AssertStatement
	| Block
	| ';'
	| ExpressionStatement
	| SwitchStatement
	| DoStatement
	| BreakStatement
	| ContinueStatement
	| ReturnStatement
	| SynchronizedStatement
	| ThrowStatement
	| TryStatement
;

LabeledStatement ::=
	  Label ':' Statement ;

LabeledStatementNoShortIf ::=
	  Label ':' StatementNoShortIf ;

Label ::=
	  Identifier ;

ExpressionStatement ::=
	  StatementExpression ';'
	| ExplicitConstructorInvocation
;

StatementExpression ::=
	  Assignment
	| PreIncrementExpression
	| PreDecrementExpression
	| PostIncrementExpression
	| PostDecrementExpression
	| MethodInvocation
	| ClassInstanceCreationExpression
;

IfThenStatement ::=
	  if '(' Expression ')' Statement ;

IfThenElseStatement ::=
	  if '(' Expression ')' StatementNoShortIf else Statement ;

IfThenElseStatementNoShortIf ::=
	  if '(' Expression ')' StatementNoShortIf else StatementNoShortIf ;

SwitchStatement ::=
	  switch '(' Expression ')' SwitchBlock ;

SwitchBlock ::=
	  '{' '}'
	| '{' SwitchBlockStatements '}'
	| '{' SwitchLabels '}'
	| '{' SwitchBlockStatements SwitchLabels '}'
;

SwitchBlockStatements ::=
	  SwitchBlockStatement
	| SwitchBlockStatements SwitchBlockStatement
;

SwitchBlockStatement ::=
	  SwitchLabels BlockStatements ;

SwitchLabels ::=
	  SwitchLabel
	| SwitchLabels SwitchLabel
;

SwitchLabel ::=
	  case ConstantExpression ':'
	| default ':'
;

WhileStatement ::=
	  while '(' Expression ')' Statement ;

WhileStatementNoShortIf ::=
	  while '(' Expression ')' StatementNoShortIf ;

DoStatement ::=
	  do Statement while '(' Expression ')' ';' ;

ForStatement ::=
	  for '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' Statement ;

ForStatementNoShortIf ::=
	  for '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf ;

ForInit ::=
	  StatementExpressionList
	| LocalVariableDeclaration
;

ForUpdate ::=
	  StatementExpressionList ;

StatementExpressionList ::=
	  StatementExpression
	| StatementExpressionList ',' StatementExpression
;

AssertStatement ::=
	  assert Expression ';'
	| assert Expression ':' Expression ';'
;

BreakStatement ::=
	  break ';'
	| break Identifier ';'
;

ContinueStatement ::=
	  continue ';'
	| continue Identifier ';'
;

ReturnStatement ::=
	  return Expressionopt ';' ;

ThrowStatement ::=
	  throw Expression ';' ;

SynchronizedStatement ::=
	  OnlySynchronized '(' Expression ')' Block ;

OnlySynchronized ::=
	  synchronized ;

TryStatement ::=
	  try TryBlock Catches
	| try TryBlock Catchesopt Finally
;

TryBlock ::=
	  Block ;

Catches ::=
	  CatchClause
	| Catches CatchClause
;

CatchClause ::=
	  catch '(' FormalParameter ')' Block ;

Finally ::=
	  finally Block ;

Primary ::=
	  PrimaryNoNewArray
	| ArrayCreationWithArrayInitializer
	| ArrayCreationWithoutArrayInitializer
;

PrimaryNoNewArray ::=
	  Literal
	| this
	| '(' Expression_NotName ')'
	| '(' Name ')'
	| ClassInstanceCreationExpression
	| FieldAccess
	| Name '.' this
	| Name '.' super
	| Name '.' class
	| Name Dims '.' class
	| PrimitiveType Dims '.' class
	| PrimitiveType '.' class
	| MethodInvocation
	| ArrayAccess
;

AllocationHeader ::=
	  new ClassType '(' ArgumentListopt ')' ;

ClassInstanceCreationExpression ::=
	  new OnlyTypeArguments ClassType '(' ArgumentListopt ')' ClassBodyopt
	| new ClassType '(' ArgumentListopt ')' ClassBodyopt
	| Primary '.' new OnlyTypeArguments ClassType '(' ArgumentListopt ')' ClassBodyopt
	| Primary '.' new ClassType '(' ArgumentListopt ')' ClassBodyopt
	| ClassInstanceCreationExpressionName new ClassType '(' ArgumentListopt ')' ClassBodyopt
	| ClassInstanceCreationExpressionName new OnlyTypeArguments ClassType '(' ArgumentListopt ')' ClassBodyopt
;

ClassInstanceCreationExpressionName ::=
	  Name '.' ;

ArgumentList ::=
	  Expression
	| ArgumentList ',' Expression
;

ArrayCreationHeader ::=
	  new PrimitiveType DimWithOrWithOutExprs
	| new ClassOrInterfaceType DimWithOrWithOutExprs
;

ArrayCreationWithoutArrayInitializer ::=
	  new PrimitiveType DimWithOrWithOutExprs
	| new ClassOrInterfaceType DimWithOrWithOutExprs
;

ArrayCreationWithArrayInitializer ::=
	  new PrimitiveType DimWithOrWithOutExprs ArrayInitializer
	| new ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer
;

DimWithOrWithOutExprs ::=
	  DimWithOrWithOutExpr
	| DimWithOrWithOutExprs DimWithOrWithOutExpr
;

DimWithOrWithOutExpr ::=
	  '[' Expression ']'
	| '[' ']'
;

Dims ::=
	  DimsLoop ;

DimsLoop ::=
	  OneDimLoop
	| DimsLoop OneDimLoop
;

OneDimLoop ::=
	  '[' ']' ;

FieldAccess ::=
	  Primary '.' Identifier
	| super '.' Identifier
;

MethodInvocation ::=
	  Name '(' ArgumentListopt ')'
	| Name '.' OnlyTypeArguments Identifier '(' ArgumentListopt ')'
	| Primary '.' OnlyTypeArguments Identifier '(' ArgumentListopt ')'
	| Primary '.' Identifier '(' ArgumentListopt ')'
	| super '.' OnlyTypeArguments Identifier '(' ArgumentListopt ')'
	| super '.' Identifier '(' ArgumentListopt ')'
;

ArrayAccess ::=
	  Name '[' Expression ']'
	| PrimaryNoNewArray '[' Expression ']'
	| ArrayCreationWithArrayInitializer '[' Expression ']'
;

PostfixExpression ::=
	  Primary
	| Name
	| PostIncrementExpression
	| PostDecrementExpression
;

PostIncrementExpression ::=
	  PostfixExpression '++' ;

PostDecrementExpression ::=
	  PostfixExpression '--' ;

UnaryExpression ::=
	  PreIncrementExpression
	| PreDecrementExpression
	| '+' UnaryExpression
	| '-' UnaryExpression
	| UnaryExpressionNotPlusMinus
;

PreIncrementExpression ::=
	  '++' UnaryExpression ;

PreDecrementExpression ::=
	  '--' UnaryExpression ;

UnaryExpressionNotPlusMinus ::=
	  PostfixExpression
	| '~' UnaryExpression
	| '!' UnaryExpression
	| CastExpression
;

CastExpression ::=
	  '(' PrimitiveType Dimsopt ')' UnaryExpression
	| '(' Name OnlyTypeArgumentsForCastExpression Dimsopt ')' UnaryExpressionNotPlusMinus
	| '(' Name OnlyTypeArgumentsForCastExpression '.' ClassOrInterfaceType Dimsopt ')' UnaryExpressionNotPlusMinus
	| '(' Name ')' UnaryExpressionNotPlusMinus
	| '(' Name Dims ')' UnaryExpressionNotPlusMinus
;

OnlyTypeArgumentsForCastExpression ::=
	  OnlyTypeArguments ;

MultiplicativeExpression ::=
	  UnaryExpression
	| MultiplicativeExpression '*' UnaryExpression
	| MultiplicativeExpression '/' UnaryExpression
	| MultiplicativeExpression '%' UnaryExpression
;

AdditiveExpression ::=
	  MultiplicativeExpression
	| AdditiveExpression '+' MultiplicativeExpression
	| AdditiveExpression '-' MultiplicativeExpression
;

ShiftExpression ::=
	  AdditiveExpression
	| ShiftExpression '<<' AdditiveExpression
	| ShiftExpression '>>' AdditiveExpression
	| ShiftExpression '>>>' AdditiveExpression
;

RelationalExpression ::=
	  ShiftExpression
	| RelationalExpression '<' ShiftExpression
	| RelationalExpression '>' ShiftExpression
	| RelationalExpression '<=' ShiftExpression
	| RelationalExpression '>=' ShiftExpression
;

InstanceofExpression ::=
	  RelationalExpression
	| InstanceofExpression instanceof ReferenceType
;

EqualityExpression ::=
	  InstanceofExpression
	| EqualityExpression '==' InstanceofExpression
	| EqualityExpression '!=' InstanceofExpression
;

AndExpression ::=
	  EqualityExpression
	| AndExpression '&' EqualityExpression
;

ExclusiveOrExpression ::=
	  AndExpression
	| ExclusiveOrExpression '^' AndExpression
;

InclusiveOrExpression ::=
	  ExclusiveOrExpression
	| InclusiveOrExpression '|' ExclusiveOrExpression
;

ConditionalAndExpression ::=
	  InclusiveOrExpression
	| ConditionalAndExpression '&&' InclusiveOrExpression
;

ConditionalOrExpression ::=
	  ConditionalAndExpression
	| ConditionalOrExpression '||' ConditionalAndExpression
;

ConditionalExpression ::=
	  ConditionalOrExpression
	| ConditionalOrExpression '?' Expression ':' ConditionalExpression
;

AssignmentExpression ::=
	  ConditionalExpression
	| Assignment
;

Assignment ::=
	  PostfixExpression AssignmentOperator AssignmentExpression
	| InvalidArrayInitializerAssignement
;

InvalidArrayInitializerAssignement ::=
	  PostfixExpression AssignmentOperator ArrayInitializer ;

AssignmentOperator ::=
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

Expression ::=
	  AssignmentExpression ;

ConstantExpression ::=
	  Expression ;

Commaopt ::=
	  
	| ','
;

EnumDeclaration ::=
	  EnumHeader EnumBody ;

EnumHeader ::=
	  EnumHeaderName ClassHeaderImplementsopt ;

EnumHeaderName ::=
	  Modifiersopt enum Identifier
	| Modifiersopt enum Identifier TypeParameters
;

EnumBody ::=
	  '{' EnumDeclarationsopt '}'
	| '{' ',' EnumDeclarationsopt '}'
	| '{' EnumConstants ',' EnumDeclarationsopt '}'
	| '{' EnumConstants EnumDeclarationsopt '}'
;

EnumConstants ::=
	  EnumConstant
	| EnumConstants ',' EnumConstant
;

EnumConstantHeaderName ::=
	  Modifiersopt Identifier ;

EnumConstantHeader ::=
	  EnumConstantHeaderName Argumentsopt ;

EnumConstant ::=
	  EnumConstantHeader ClassBody
	| EnumConstantHeader
;

Arguments ::=
	  '(' ArgumentListopt ')' ;

EnumDeclarations ::=
	  ';' ClassBodyDeclarationsopt ;

EnhancedForStatement ::=
	  EnhancedForStatementHeader Statement ;

EnhancedForStatementNoShortIf ::=
	  EnhancedForStatementHeader StatementNoShortIf ;

EnhancedForStatementHeaderInit ::=
	  for '(' Type Identifier Dimsopt
	| for '(' Modifiers Type Identifier Dimsopt
;

EnhancedForStatementHeader ::=
	  EnhancedForStatementHeaderInit ':' Expression ')' ;

SingleStaticImportDeclaration ::=
	  SingleStaticImportDeclarationName ';' ;

SingleStaticImportDeclarationName ::=
	  import static Name ;

StaticImportOnDemandDeclaration ::=
	  StaticImportOnDemandDeclarationName ';' ;

StaticImportOnDemandDeclarationName ::=
	  import static Name '.' '*' ;

TypeArguments ::=
	  '<' TypeArgumentList1 ;

OnlyTypeArguments ::=
	  '<' TypeArgumentList1 ;

TypeArgumentList1 ::=
	  TypeArgument1
	| TypeArgumentList ',' TypeArgument1
;

TypeArgumentList ::=
	  TypeArgument
	| TypeArgumentList ',' TypeArgument
;

TypeArgument ::=
	  ReferenceType
	| Wildcard
;

TypeArgument1 ::=
	  ReferenceType1
	| Wildcard1
;

ReferenceType1 ::=
	  ReferenceType '>'
	| ClassOrInterface '<' TypeArgumentList2
;

TypeArgumentList2 ::=
	  TypeArgument2
	| TypeArgumentList ',' TypeArgument2
;

TypeArgument2 ::=
	  ReferenceType2
	| Wildcard2
;

ReferenceType2 ::=
	  ReferenceType '>>'
	| ClassOrInterface '<' TypeArgumentList3
;

TypeArgumentList3 ::=
	  TypeArgument3
	| TypeArgumentList ',' TypeArgument3
;

TypeArgument3 ::=
	  ReferenceType3
	| Wildcard3
;

ReferenceType3 ::=
	  ReferenceType '>>>' ;

Wildcard ::=
	  '?'
	| '?' WildcardBounds
;

WildcardBounds ::=
	  extends ReferenceType
	| super ReferenceType
;

Wildcard1 ::=
	  '?' '>'
	| '?' WildcardBounds1
;

WildcardBounds1 ::=
	  extends ReferenceType1
	| super ReferenceType1
;

Wildcard2 ::=
	  '?' '>>'
	| '?' WildcardBounds2
;

WildcardBounds2 ::=
	  extends ReferenceType2
	| super ReferenceType2
;

Wildcard3 ::=
	  '?' '>>>'
	| '?' WildcardBounds3
;

WildcardBounds3 ::=
	  extends ReferenceType3
	| super ReferenceType3
;

TypeParameterHeader ::=
	  Identifier ;

TypeParameters ::=
	  '<' TypeParameterList1 ;

TypeParameterList ::=
	  TypeParameter
	| TypeParameterList ',' TypeParameter
;

TypeParameter ::=
	  TypeParameterHeader
	| TypeParameterHeader extends ReferenceType
	| TypeParameterHeader extends ReferenceType AdditionalBoundList
;

AdditionalBoundList ::=
	  AdditionalBound
	| AdditionalBoundList AdditionalBound
;

AdditionalBound ::=
	  '&' ReferenceType ;

TypeParameterList1 ::=
	  TypeParameter1
	| TypeParameterList ',' TypeParameter1
;

TypeParameter1 ::=
	  TypeParameterHeader '>'
	| TypeParameterHeader extends ReferenceType1
	| TypeParameterHeader extends ReferenceType AdditionalBoundList1
;

AdditionalBoundList1 ::=
	  AdditionalBound1
	| AdditionalBoundList AdditionalBound1
;

AdditionalBound1 ::=
	  '&' ReferenceType1 ;

PostfixExpression_NotName ::=
	  Primary
	| PostIncrementExpression
	| PostDecrementExpression
;

UnaryExpression_NotName ::=
	  PreIncrementExpression
	| PreDecrementExpression
	| '+' UnaryExpression
	| '-' UnaryExpression
	| UnaryExpressionNotPlusMinus_NotName
;

UnaryExpressionNotPlusMinus_NotName ::=
	  PostfixExpression_NotName
	| '~' UnaryExpression
	| '!' UnaryExpression
	| CastExpression
;

MultiplicativeExpression_NotName ::=
	  UnaryExpression_NotName
	| MultiplicativeExpression_NotName '*' UnaryExpression
	| Name '*' UnaryExpression
	| MultiplicativeExpression_NotName '/' UnaryExpression
	| Name '/' UnaryExpression
	| MultiplicativeExpression_NotName '%' UnaryExpression
	| Name '%' UnaryExpression
;

AdditiveExpression_NotName ::=
	  MultiplicativeExpression_NotName
	| AdditiveExpression_NotName '+' MultiplicativeExpression
	| Name '+' MultiplicativeExpression
	| AdditiveExpression_NotName '-' MultiplicativeExpression
	| Name '-' MultiplicativeExpression
;

ShiftExpression_NotName ::=
	  AdditiveExpression_NotName
	| ShiftExpression_NotName '<<' AdditiveExpression
	| Name '<<' AdditiveExpression
	| ShiftExpression_NotName '>>' AdditiveExpression
	| Name '>>' AdditiveExpression
	| ShiftExpression_NotName '>>>' AdditiveExpression
	| Name '>>>' AdditiveExpression
;

RelationalExpression_NotName ::=
	  ShiftExpression_NotName
	| ShiftExpression_NotName '<' ShiftExpression
	| Name '<' ShiftExpression
	| ShiftExpression_NotName '>' ShiftExpression
	| Name '>' ShiftExpression
	| RelationalExpression_NotName '<=' ShiftExpression
	| Name '<=' ShiftExpression
	| RelationalExpression_NotName '>=' ShiftExpression
	| Name '>=' ShiftExpression
;

InstanceofExpression_NotName ::=
	  RelationalExpression_NotName
	| Name instanceof ReferenceType
	| InstanceofExpression_NotName instanceof ReferenceType
;

EqualityExpression_NotName ::=
	  InstanceofExpression_NotName
	| EqualityExpression_NotName '==' InstanceofExpression
	| Name '==' InstanceofExpression
	| EqualityExpression_NotName '!=' InstanceofExpression
	| Name '!=' InstanceofExpression
;

AndExpression_NotName ::=
	  EqualityExpression_NotName
	| AndExpression_NotName '&' EqualityExpression
	| Name '&' EqualityExpression
;

ExclusiveOrExpression_NotName ::=
	  AndExpression_NotName
	| ExclusiveOrExpression_NotName '^' AndExpression
	| Name '^' AndExpression
;

InclusiveOrExpression_NotName ::=
	  ExclusiveOrExpression_NotName
	| InclusiveOrExpression_NotName '|' ExclusiveOrExpression
	| Name '|' ExclusiveOrExpression
;

ConditionalAndExpression_NotName ::=
	  InclusiveOrExpression_NotName
	| ConditionalAndExpression_NotName '&&' InclusiveOrExpression
	| Name '&&' InclusiveOrExpression
;

ConditionalOrExpression_NotName ::=
	  ConditionalAndExpression_NotName
	| ConditionalOrExpression_NotName '||' ConditionalAndExpression
	| Name '||' ConditionalAndExpression
;

ConditionalExpression_NotName ::=
	  ConditionalOrExpression_NotName
	| ConditionalOrExpression_NotName '?' Expression ':' ConditionalExpression
	| Name '?' Expression ':' ConditionalExpression
;

AssignmentExpression_NotName ::=
	  ConditionalExpression_NotName
	| Assignment
;

Expression_NotName ::=
	  AssignmentExpression_NotName ;

AnnotationTypeDeclarationHeaderName ::=
	  Modifiers '@' interface Identifier
	| Modifiers '@' interface Identifier TypeParameters
	| '@' interface Identifier TypeParameters
	| '@' interface Identifier
;

AnnotationTypeDeclarationHeader ::=
	  AnnotationTypeDeclarationHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt ;

AnnotationTypeDeclaration ::=
	  AnnotationTypeDeclarationHeader AnnotationTypeBody ;

AnnotationTypeBody ::=
	  '{' AnnotationTypeMemberDeclarationsopt '}' ;

AnnotationTypeMemberDeclarations ::=
	  AnnotationTypeMemberDeclaration
	| AnnotationTypeMemberDeclarations AnnotationTypeMemberDeclaration
;

AnnotationMethodHeaderName ::=
	  Modifiersopt TypeParameters Type Identifier '('
	| Modifiersopt Type Identifier '('
;

AnnotationMethodHeader ::=
	  AnnotationMethodHeaderName FormalParameterListopt ')' MethodHeaderExtendedDims DefaultValueopt ;

AnnotationTypeMemberDeclaration ::=
	  AnnotationMethodHeader ';'
	| ConstantDeclaration
	| ConstructorDeclaration
	| TypeDeclaration
;

DefaultValue ::=
	  default MemberValue ;

Annotation ::=
	  NormalAnnotation
	| MarkerAnnotation
	| SingleMemberAnnotation
;

AnnotationName ::=
	  '@' Name ;

NormalAnnotation ::=
	  AnnotationName '(' MemberValuePairsopt ')' ;

MemberValuePairs ::=
	  MemberValuePair
	| MemberValuePairs ',' MemberValuePair
;

MemberValuePair ::=
	  SimpleName '=' MemberValue ;

MemberValue ::=
	  ConditionalExpression_NotName
	| Name
	| Annotation
	| MemberValueArrayInitializer
;

MemberValueArrayInitializer ::=
	  '{' MemberValues ',' '}'
	| '{' MemberValues '}'
	| '{' ',' '}'
	| '{' '}'
;

MemberValues ::=
	  MemberValue
	| MemberValues ',' MemberValue
;

MarkerAnnotation ::=
	  AnnotationName ;

SingleMemberAnnotationMemberValue ::=
	  MemberValue ;

SingleMemberAnnotation ::=
	  AnnotationName '(' SingleMemberAnnotationMemberValue ')' ;

RecoveryMethodHeaderName ::=
	  Modifiersopt TypeParameters Type Identifier '('
	| Modifiersopt Type Identifier '('
;

RecoveryMethodHeader ::=
	  RecoveryMethodHeaderName FormalParameterListopt ')' MethodHeaderExtendedDims DefaultValueopt
	| RecoveryMethodHeaderName FormalParameterListopt ')' MethodHeaderExtendedDims MethodHeaderThrowsClause
;

