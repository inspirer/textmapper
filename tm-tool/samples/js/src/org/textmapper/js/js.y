%{
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
%token Lbrace
%token Rbrace
%token Lparen
%token Rparen
%token Lbrack
%token Rbrack
%token Dot
%token Semicolon
%token Comma
%token Lt
%token Gt
%token LtAssign
%token GtAssign
%token AssignAssign
%token ExclAssign
%token AssignAssignAssign
%token ExclAssignAssign
%token Plus
%token Minus
%token Mult
%token Rem
%token PlusPlus
%token MinusMinus
%token LtLt
%token GtGt
%token GtGtGt
%token And
%token Or
%token Xor
%token Excl
%token Tilde
%token AndAnd
%token OrOr
%token Quest
%token Colon
%token Assign
%token PlusAssign
%token MinusAssign
%token MultAssign
%token RemAssign
%token LtLtAssign
%token GtGtAssign
%token GtGtGtAssign
%token AndAssign
%token OrAssign
%token XorAssign
%token null
%token true
%token false
%token NumericLiteral
%token StringLiteral
%token RegularExpressionLiteral
%token Div
%token DivAssign

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
  Lbrack AssignmentExpression_list_Comma_separated Rbrack
;

AssignmentExpression_list_Comma_separated :
  AssignmentExpression_list_Comma_separated Comma AssignmentExpression
| AssignmentExpression_list_Comma_separated Comma
| AssignmentExpression
| %empty
;

ObjectLiteral :
  Lbrace PropertyAssignment_list_Comma_separated Comma Rbrace
| Lbrace PropertyAssignment_list_Comma_separated Rbrace
| Lbrace Rbrace
;

PropertyAssignment_list_Comma_separated :
  PropertyAssignment_list_Comma_separated Comma PropertyAssignment
| PropertyAssignment
;

PropertyAssignment :
  PropertyName Colon AssignmentExpression
| Identifier PropertyName Lparen Rparen Lbrace FunctionBody Rbrace
| Identifier PropertyName Lparen PropertySetParameterList Rparen Lbrace FunctionBody Rbrace
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
| MemberExpression Lbrack Expression Rbrack
| MemberExpression Dot IdentifierName
| new MemberExpression Arguments
;

MemberExpression_ExprStart :
  PrimaryExpression_ExprStart
| MemberExpression_ExprStart Lbrack Expression Rbrack
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
| CallExpression Lbrack Expression Rbrack
| CallExpression Dot IdentifierName
;

CallExpression_ExprStart :
  MemberExpression_ExprStart Arguments
| CallExpression_ExprStart Arguments
| CallExpression_ExprStart Lbrack Expression Rbrack
| CallExpression_ExprStart Dot IdentifierName
;

Arguments :
  Lparen AssignmentExpression_list_Comma_separatedopt Rparen
;

AssignmentExpression_list_Comma_separated1 :
  AssignmentExpression_list_Comma_separated1 Comma AssignmentExpression
| AssignmentExpression
;

AssignmentExpression_list_Comma_separatedopt :
  AssignmentExpression_list_Comma_separated1
| %empty
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
| Excl UnaryExpression
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
| Excl UnaryExpression
;

MultiplicativeExpression :
  UnaryExpression
| MultiplicativeExpression Mult UnaryExpression
| MultiplicativeExpression Div UnaryExpression
| MultiplicativeExpression Rem UnaryExpression
;

MultiplicativeExpression_ExprStart :
  UnaryExpression_ExprStart
| MultiplicativeExpression_ExprStart Mult UnaryExpression
| MultiplicativeExpression_ExprStart Div UnaryExpression
| MultiplicativeExpression_ExprStart Rem UnaryExpression
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
| ShiftExpression LtLt AdditiveExpression
| ShiftExpression GtGt AdditiveExpression
| ShiftExpression GtGtGt AdditiveExpression
;

ShiftExpression_ExprStart :
  AdditiveExpression_ExprStart
| ShiftExpression_ExprStart LtLt AdditiveExpression
| ShiftExpression_ExprStart GtGt AdditiveExpression
| ShiftExpression_ExprStart GtGtGt AdditiveExpression
;

RelationalExpression :
  ShiftExpression
| RelationalExpression Lt ShiftExpression
| RelationalExpression Gt ShiftExpression
| RelationalExpression LtAssign ShiftExpression
| RelationalExpression GtAssign ShiftExpression
| RelationalExpression instanceof ShiftExpression
| RelationalExpression in ShiftExpression
;

RelationalExpression_ExprStart :
  ShiftExpression_ExprStart
| RelationalExpression_ExprStart Lt ShiftExpression
| RelationalExpression_ExprStart Gt ShiftExpression
| RelationalExpression_ExprStart LtAssign ShiftExpression
| RelationalExpression_ExprStart GtAssign ShiftExpression
| RelationalExpression_ExprStart instanceof ShiftExpression
| RelationalExpression_ExprStart in ShiftExpression
;

RelationalExpression_NoIn :
  ShiftExpression
| RelationalExpression_NoIn Lt ShiftExpression
| RelationalExpression_NoIn Gt ShiftExpression
| RelationalExpression_NoIn LtAssign ShiftExpression
| RelationalExpression_NoIn GtAssign ShiftExpression
| RelationalExpression_NoIn instanceof ShiftExpression
;

EqualityExpression :
  RelationalExpression
| EqualityExpression AssignAssign RelationalExpression
| EqualityExpression ExclAssign RelationalExpression
| EqualityExpression AssignAssignAssign RelationalExpression
| EqualityExpression ExclAssignAssign RelationalExpression
;

EqualityExpression_ExprStart :
  RelationalExpression_ExprStart
| EqualityExpression_ExprStart AssignAssign RelationalExpression
| EqualityExpression_ExprStart ExclAssign RelationalExpression
| EqualityExpression_ExprStart AssignAssignAssign RelationalExpression
| EqualityExpression_ExprStart ExclAssignAssign RelationalExpression
;

EqualityExpression_NoIn :
  RelationalExpression_NoIn
| EqualityExpression_NoIn AssignAssign RelationalExpression_NoIn
| EqualityExpression_NoIn ExclAssign RelationalExpression_NoIn
| EqualityExpression_NoIn AssignAssignAssign RelationalExpression_NoIn
| EqualityExpression_NoIn ExclAssignAssign RelationalExpression_NoIn
;

BitwiseANDExpression :
  EqualityExpression
| BitwiseANDExpression And EqualityExpression
;

BitwiseANDExpression_ExprStart :
  EqualityExpression_ExprStart
| BitwiseANDExpression_ExprStart And EqualityExpression
;

BitwiseANDExpression_NoIn :
  EqualityExpression_NoIn
| BitwiseANDExpression_NoIn And EqualityExpression_NoIn
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
| LogicalANDExpression AndAnd BitwiseORExpression
;

LogicalANDExpression_ExprStart :
  BitwiseORExpression_ExprStart
| LogicalANDExpression_ExprStart AndAnd BitwiseORExpression
;

LogicalANDExpression_NoIn :
  BitwiseORExpression_NoIn
| LogicalANDExpression_NoIn AndAnd BitwiseORExpression_NoIn
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
| LogicalORExpression Quest AssignmentExpression Colon AssignmentExpression
;

ConditionalExpression_ExprStart :
  LogicalORExpression_ExprStart
| LogicalORExpression_ExprStart Quest AssignmentExpression Colon AssignmentExpression
;

ConditionalExpression_NoIn :
  LogicalORExpression_NoIn
| LogicalORExpression_NoIn Quest AssignmentExpression_NoIn Colon AssignmentExpression_NoIn
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
  Assign
| MultAssign
| DivAssign
| RemAssign
| PlusAssign
| MinusAssign
| LtLtAssign
| GtGtAssign
| GtGtGtAssign
| AndAssign
| XorAssign
| OrAssign
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
  Lbrace Statement_optlist Rbrace
;

Statement_optlist :
  Statement_optlist Statement
| %empty
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
  Assign AssignmentExpression
;

Initialiser_NoIn :
  Assign AssignmentExpression_NoIn
;

EmptyStatement :
  Semicolon
;

ExpressionStatement :
  Expression_ExprStart Semicolon
;

IfStatement :
  if Lparen Expression Rparen Statement else Statement
| if Lparen Expression Rparen Statement %prec else
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
  Lbrace CaseClause_optlist DefaultClause CaseClause_optlist Rbrace
| Lbrace CaseClause_optlist Rbrace
;

CaseClause_optlist :
  CaseClause_optlist CaseClause
| %empty
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
  function Identifier Lparen FormalParameterListopt Rparen Lbrace FunctionBody Rbrace
;

FunctionExpression :
  function Identifier Lparen FormalParameterListopt Rparen Lbrace FunctionBody Rbrace
| function Lparen FormalParameterListopt Rparen Lbrace FunctionBody Rbrace
;

FormalParameterList :
  Identifier
| FormalParameterList Comma Identifier
;

FunctionBody :
  SourceElement_optlist
;

SourceElement_optlist :
  SourceElement_optlist SourceElement
| %empty
;

Program :
  SourceElement_optlist
;

SourceElement :
  Statement
| FunctionDeclaration
;

Initialiseropt :
  Initialiser
| %empty
;

Initialiseropt_NoIn :
  Initialiser_NoIn
| %empty
;

Expressionopt :
  Expression
| %empty
;

Expressionopt_NoIn :
  Expression_NoIn
| %empty
;

FormalParameterListopt :
  FormalParameterList
| %empty
;

%%

