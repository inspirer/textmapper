%{
#include <stdio.h>
%}

%start Program

%right else
%token space
%token LineTerminatorSequence
%token MultiLineComment
%token SingleLineComment
%token Identifier
%token break
%token case
%token catch
%token continue
%token debugger
%token default
%token delete
%token do
%token finally
%token for
%token function
%token if
%token in
%token instanceof
%token new
%token return
%token switch
%token this
%token throw
%token try
%token typeof
%token var
%token void
%token while
%token with
%token class
%token const
%token enum
%token export
%token extends
%token import
%token super
%token Lcurly
%token Rcurly
%token Lparen
%token Rparen
%token Lsquare
%token Rsquare
%token Dot
%token Semicolon
%token Comma
%token Less
%token Greater
%token LessEqual
%token GreaterEqual
%token EqualEqual
%token ExclamationEqual
%token EqualEqualEqual
%token ExclamationEqualEqual
%token Plus
%token Minus
%token Mult
%token Percent
%token PlusPlus
%token MinusMinus
%token LessLess
%token GreaterGreater
%token GreaterGreaterGreater
%token Ampersand
%token Or
%token Xor
%token Exclamation
%token Tilde
%token AmpersandAmpersand
%token OrOr
%token Questionmark
%token Colon
%token Equal
%token PlusEqual
%token MinusEqual
%token MultEqual
%token PercentEqual
%token LessLessEqual
%token GreaterGreaterEqual
%token GreaterGreaterGreaterEqual
%token AmpersandEqual
%token OrEqual
%token XorEqual
%token null
%token true
%token false
%token NumericLiteral
%token StringLiteral
%token RegularExpressionLiteral
%token Slash
%token SlashEqual

%locations
%%

IdentifierName :
  Identifier
| break
| do
| instanceof
| typeof
| case
| else
| new
| var
| catch
| finally
| return
| void
| continue
| for
| switch
| while
| debugger
| function
| this
| with
| default
| if
| throw
| delete
| in
| try
| class
| enum
| extends
| super
| const
| export
| import
| null
| true
| false
;

Literal :
  null
| true
| false
| NumericLiteral
| StringLiteral
| RegularExpressionLiteral
;

PrimaryExpression :
  this
| Identifier
| Literal
| ArrayLiteral
| ObjectLiteral
| Lparen Expression Rparen
;

PrimaryExpression_ExprStart :
  this
| Identifier
| Literal
| ArrayLiteral
| Lparen Expression Rparen
;

ArrayLiteral :
  Lsquare AssignmentExpression_list_Comma_separated Rsquare
;

AssignmentExpression_list_Comma_separated :
  %empty
| AssignmentExpression_list_Comma_separated Comma AssignmentExpression
| AssignmentExpression_list_Comma_separated Comma
| AssignmentExpression
;

ObjectLiteral :
  Lcurly PropertyAssignment_list_Comma_separated Comma Rcurly
| Lcurly PropertyAssignment_list_Comma_separated Rcurly
| Lcurly Rcurly
;

PropertyAssignment_list_Comma_separated :
  PropertyAssignment_list_Comma_separated Comma PropertyAssignment
| PropertyAssignment
;

PropertyAssignment :
  PropertyName Colon AssignmentExpression
| Identifier PropertyName Lparen Rparen Lcurly FunctionBody Rcurly
| Identifier PropertyName Lparen PropertySetParameterList Rparen Lcurly FunctionBody Rcurly
;

PropertyName :
  IdentifierName
| StringLiteral
| NumericLiteral
;

PropertySetParameterList :
  Identifier
;

MemberExpression :
  PrimaryExpression
| FunctionExpression
| MemberExpression Lsquare Expression Rsquare
| MemberExpression Dot IdentifierName
| new MemberExpression Arguments
;

MemberExpression_ExprStart :
  PrimaryExpression_ExprStart
| MemberExpression_ExprStart Lsquare Expression Rsquare
| MemberExpression_ExprStart Dot IdentifierName
| new MemberExpression Arguments
;

NewExpression :
  MemberExpression
| new NewExpression
;

NewExpression_ExprStart :
  MemberExpression_ExprStart
| new NewExpression
;

CallExpression :
  MemberExpression Arguments
| CallExpression Arguments
| CallExpression Lsquare Expression Rsquare
| CallExpression Dot IdentifierName
;

CallExpression_ExprStart :
  MemberExpression_ExprStart Arguments
| CallExpression_ExprStart Arguments
| CallExpression_ExprStart Lsquare Expression Rsquare
| CallExpression_ExprStart Dot IdentifierName
;

Arguments :
  Lparen AssignmentExpression_list_Comma_separated_opt Rparen
;

AssignmentExpression_list_Comma_separated1 :
  AssignmentExpression_list_Comma_separated1 Comma AssignmentExpression
| AssignmentExpression
;

AssignmentExpression_list_Comma_separated_opt :
  %empty
| AssignmentExpression_list_Comma_separated1
;

LeftHandSideExpression :
  NewExpression
| CallExpression
;

LeftHandSideExpression_ExprStart :
  NewExpression_ExprStart
| CallExpression_ExprStart
;

PostfixExpression :
  LeftHandSideExpression
| LeftHandSideExpression PlusPlus
| LeftHandSideExpression MinusMinus
;

PostfixExpression_ExprStart :
  LeftHandSideExpression_ExprStart
| LeftHandSideExpression_ExprStart PlusPlus
| LeftHandSideExpression_ExprStart MinusMinus
;

UnaryExpression :
  PostfixExpression
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Exclamation UnaryExpression
;

UnaryExpression_ExprStart :
  PostfixExpression_ExprStart
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Exclamation UnaryExpression
;

MultiplicativeExpression :
  UnaryExpression
| MultiplicativeExpression Mult UnaryExpression
| MultiplicativeExpression Slash UnaryExpression
| MultiplicativeExpression Percent UnaryExpression
;

MultiplicativeExpression_ExprStart :
  UnaryExpression_ExprStart
| MultiplicativeExpression_ExprStart Mult UnaryExpression
| MultiplicativeExpression_ExprStart Slash UnaryExpression
| MultiplicativeExpression_ExprStart Percent UnaryExpression
;

AdditiveExpression :
  MultiplicativeExpression
| AdditiveExpression Plus MultiplicativeExpression
| AdditiveExpression Minus MultiplicativeExpression
;

AdditiveExpression_ExprStart :
  MultiplicativeExpression_ExprStart
| AdditiveExpression_ExprStart Plus MultiplicativeExpression
| AdditiveExpression_ExprStart Minus MultiplicativeExpression
;

ShiftExpression :
  AdditiveExpression
| ShiftExpression LessLess AdditiveExpression
| ShiftExpression GreaterGreater AdditiveExpression
| ShiftExpression GreaterGreaterGreater AdditiveExpression
;

ShiftExpression_ExprStart :
  AdditiveExpression_ExprStart
| ShiftExpression_ExprStart LessLess AdditiveExpression
| ShiftExpression_ExprStart GreaterGreater AdditiveExpression
| ShiftExpression_ExprStart GreaterGreaterGreater AdditiveExpression
;

RelationalExpression :
  ShiftExpression
| RelationalExpression Less ShiftExpression
| RelationalExpression Greater ShiftExpression
| RelationalExpression LessEqual ShiftExpression
| RelationalExpression GreaterEqual ShiftExpression
| RelationalExpression instanceof ShiftExpression
| RelationalExpression in ShiftExpression
;

RelationalExpression_ExprStart :
  ShiftExpression_ExprStart
| RelationalExpression_ExprStart Less ShiftExpression
| RelationalExpression_ExprStart Greater ShiftExpression
| RelationalExpression_ExprStart LessEqual ShiftExpression
| RelationalExpression_ExprStart GreaterEqual ShiftExpression
| RelationalExpression_ExprStart instanceof ShiftExpression
| RelationalExpression_ExprStart in ShiftExpression
;

RelationalExpression_NoIn :
  ShiftExpression
| RelationalExpression_NoIn Less ShiftExpression
| RelationalExpression_NoIn Greater ShiftExpression
| RelationalExpression_NoIn LessEqual ShiftExpression
| RelationalExpression_NoIn GreaterEqual ShiftExpression
| RelationalExpression_NoIn instanceof ShiftExpression
;

EqualityExpression :
  RelationalExpression
| EqualityExpression EqualEqual RelationalExpression
| EqualityExpression ExclamationEqual RelationalExpression
| EqualityExpression EqualEqualEqual RelationalExpression
| EqualityExpression ExclamationEqualEqual RelationalExpression
;

EqualityExpression_ExprStart :
  RelationalExpression_ExprStart
| EqualityExpression_ExprStart EqualEqual RelationalExpression
| EqualityExpression_ExprStart ExclamationEqual RelationalExpression
| EqualityExpression_ExprStart EqualEqualEqual RelationalExpression
| EqualityExpression_ExprStart ExclamationEqualEqual RelationalExpression
;

EqualityExpression_NoIn :
  RelationalExpression_NoIn
| EqualityExpression_NoIn EqualEqual RelationalExpression_NoIn
| EqualityExpression_NoIn ExclamationEqual RelationalExpression_NoIn
| EqualityExpression_NoIn EqualEqualEqual RelationalExpression_NoIn
| EqualityExpression_NoIn ExclamationEqualEqual RelationalExpression_NoIn
;

BitwiseANDExpression :
  EqualityExpression
| BitwiseANDExpression Ampersand EqualityExpression
;

BitwiseANDExpression_ExprStart :
  EqualityExpression_ExprStart
| BitwiseANDExpression_ExprStart Ampersand EqualityExpression
;

BitwiseANDExpression_NoIn :
  EqualityExpression_NoIn
| BitwiseANDExpression_NoIn Ampersand EqualityExpression_NoIn
;

BitwiseXORExpression :
  BitwiseANDExpression
| BitwiseXORExpression Xor BitwiseANDExpression
;

BitwiseXORExpression_ExprStart :
  BitwiseANDExpression_ExprStart
| BitwiseXORExpression_ExprStart Xor BitwiseANDExpression
;

BitwiseXORExpression_NoIn :
  BitwiseANDExpression_NoIn
| BitwiseXORExpression_NoIn Xor BitwiseANDExpression_NoIn
;

BitwiseORExpression :
  BitwiseXORExpression
| BitwiseORExpression Or BitwiseXORExpression
;

BitwiseORExpression_ExprStart :
  BitwiseXORExpression_ExprStart
| BitwiseORExpression_ExprStart Or BitwiseXORExpression
;

BitwiseORExpression_NoIn :
  BitwiseXORExpression_NoIn
| BitwiseORExpression_NoIn Or BitwiseXORExpression_NoIn
;

LogicalANDExpression :
  BitwiseORExpression
| LogicalANDExpression AmpersandAmpersand BitwiseORExpression
;

LogicalANDExpression_ExprStart :
  BitwiseORExpression_ExprStart
| LogicalANDExpression_ExprStart AmpersandAmpersand BitwiseORExpression
;

LogicalANDExpression_NoIn :
  BitwiseORExpression_NoIn
| LogicalANDExpression_NoIn AmpersandAmpersand BitwiseORExpression_NoIn
;

LogicalORExpression :
  LogicalANDExpression
| LogicalORExpression OrOr LogicalANDExpression
;

LogicalORExpression_ExprStart :
  LogicalANDExpression_ExprStart
| LogicalORExpression_ExprStart OrOr LogicalANDExpression
;

LogicalORExpression_NoIn :
  LogicalANDExpression_NoIn
| LogicalORExpression_NoIn OrOr LogicalANDExpression_NoIn
;

ConditionalExpression :
  LogicalORExpression
| LogicalORExpression Questionmark AssignmentExpression Colon AssignmentExpression
;

ConditionalExpression_ExprStart :
  LogicalORExpression_ExprStart
| LogicalORExpression_ExprStart Questionmark AssignmentExpression Colon AssignmentExpression
;

ConditionalExpression_NoIn :
  LogicalORExpression_NoIn
| LogicalORExpression_NoIn Questionmark AssignmentExpression_NoIn Colon AssignmentExpression_NoIn
;

AssignmentExpression :
  ConditionalExpression
| LeftHandSideExpression AssignmentOperator AssignmentExpression
;

AssignmentExpression_ExprStart :
  ConditionalExpression_ExprStart
| LeftHandSideExpression_ExprStart AssignmentOperator AssignmentExpression
;

AssignmentExpression_NoIn :
  ConditionalExpression_NoIn
| LeftHandSideExpression AssignmentOperator AssignmentExpression_NoIn
;

AssignmentOperator :
  Equal
| MultEqual
| SlashEqual
| PercentEqual
| PlusEqual
| MinusEqual
| LessLessEqual
| GreaterGreaterEqual
| GreaterGreaterGreaterEqual
| AmpersandEqual
| XorEqual
| OrEqual
;

Expression :
  AssignmentExpression
| Expression Comma AssignmentExpression
;

Expression_ExprStart :
  AssignmentExpression_ExprStart
| Expression_ExprStart Comma AssignmentExpression
;

Expression_NoIn :
  AssignmentExpression_NoIn
| Expression_NoIn Comma AssignmentExpression_NoIn
;

Statement :
  Block
| VariableStatement
| EmptyStatement
| ExpressionStatement
| IfStatement
| IterationStatement
| ContinueStatement
| BreakStatement
| ReturnStatement
| WithStatement
| LabelledStatement
| SwitchStatement
| ThrowStatement
| TryStatement
| DebuggerStatement
;

Block :
  Lcurly Statement_optlist Rcurly
;

Statement_optlist :
  %empty
| Statement_optlist Statement
;

VariableStatement :
  var VariableDeclarationList Semicolon
;

VariableDeclarationList :
  VariableDeclaration
| VariableDeclarationList Comma VariableDeclaration
;

VariableDeclarationList_NoIn :
  VariableDeclaration_NoIn
| VariableDeclarationList_NoIn Comma VariableDeclaration_NoIn
;

VariableDeclaration :
  Identifier Initialiseropt
;

VariableDeclaration_NoIn :
  Identifier Initialiseropt_NoIn
;

Initialiser :
  Equal AssignmentExpression
;

Initialiser_NoIn :
  Equal AssignmentExpression_NoIn
;

EmptyStatement :
  Semicolon
;

ExpressionStatement :
  Expression_ExprStart Semicolon
;

IfStatement :
  if Lparen Expression Rparen Statement else Statement
| if Lparen Expression Rparen Statement
;

IterationStatement :
  do Statement while Lparen Expression Rparen Semicolon
| while Lparen Expression Rparen Statement
| for Lparen Expressionopt_NoIn Semicolon Expressionopt Semicolon Expressionopt Rparen Statement
| for Lparen var VariableDeclarationList_NoIn Semicolon Expressionopt Semicolon Expressionopt Rparen Statement
| for Lparen LeftHandSideExpression in Expression Rparen Statement
| for Lparen var VariableDeclaration_NoIn in Expression Rparen Statement
;

ContinueStatement :
  continue Identifier Semicolon
| continue Semicolon
;

BreakStatement :
  break Identifier Semicolon
| break Semicolon
;

ReturnStatement :
  return Expressionopt Semicolon
;

WithStatement :
  with Lparen Expression Rparen Statement
;

SwitchStatement :
  switch Lparen Expression Rparen CaseBlock
;

CaseBlock :
  Lcurly CaseClause_optlist DefaultClause CaseClause_optlist Rcurly
| Lcurly CaseClause_optlist Rcurly
;

CaseClause_optlist :
  %empty
| CaseClause_optlist CaseClause
;

CaseClause :
  case Expression Colon Statement_optlist
;

DefaultClause :
  default Colon Statement_optlist
;

LabelledStatement :
  Identifier Colon Statement
;

ThrowStatement :
  throw Expression Semicolon
;

TryStatement :
  try Block Catch
| try Block Finally
| try Block Catch Finally
;

Catch :
  catch Lparen Identifier Rparen Block
;

Finally :
  finally Block
;

DebuggerStatement :
  debugger Semicolon
;

FunctionDeclaration :
  function Identifier Lparen FormalParameterListopt Rparen Lcurly FunctionBody Rcurly
;

FunctionExpression :
  function Identifier Lparen FormalParameterListopt Rparen Lcurly FunctionBody Rcurly
| function Lparen FormalParameterListopt Rparen Lcurly FunctionBody Rcurly
;

FormalParameterList :
  Identifier
| FormalParameterList Comma Identifier
;

FunctionBody :
  SourceElement_optlist
;

SourceElement_optlist :
  %empty
| SourceElement_optlist SourceElement
;

Program :
  SourceElement_optlist
;

SourceElement :
  Statement
| FunctionDeclaration
;

Initialiseropt :
  %empty
| Initialiser
;

Initialiseropt_NoIn :
  %empty
| Initialiser_NoIn
;

Expressionopt :
  %empty
| Expression
;

Expressionopt_NoIn :
  %empty
| Expression_NoIn
;

FormalParameterListopt :
  %empty
| FormalParameterList
;

%%

