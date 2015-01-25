language javascript(java);

prefix = "Js"
breaks = true
gentree = true
genast = false
positions = "line,offset"
endpositions = "offset"

:: lexer

[initial, div]

space: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)

# Note: LineTerminator: /[\n\r\u2028\u2029]/
LineTerminatorSequence: /[\n\r\u2028\u2029]|\r\n/ (space)

commentChars = /([^*]*\*+[^*\/])*([^*]*\**)?/
MultiLineComment: /\/\*{commentChars}?\*\// (space)
SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/ (space)

hex = /[0-9a-fA-F]/
unicodeLetter = /\p{Lu}|\p{Ll}|\p{Lt}|\p{Lm}|\p{Lo}|\p{Nl}/
identifierStart = /{unicodeLetter}|$|_|\\u{hex}{4}/
identifierPart = /{identifierStart}|\p{Mn}|\p{Mc}|\p{Nd}|\p{Pc}|\u200c|\u200d/

Identifier: /{identifierStart}{identifierPart}*/    (class)

## TODO: smart keywords? (used in PropertyAssignment)
#	get set


'break': /break/
'do': /do/
'instanceof': /instanceof/
'typeof': /typeof/
'case': /case/
'else': /else/
'new': /new/
'var': /var/
'catch': /catch/
'finally': /finally/
'return': /return/
'void': /void/
'continue': /continue/
'for': /for/
'switch': /switch/
'while': /while/
'debugger': /debugger/
'function': /function/
'this': /this/
'with': /with/
'default': /default/
'if': /if/
'throw': /throw/
'delete': /delete/
'in': /in/
'try': /try/

'class': /class/
'enum': /enum/
'extends': /extends/
'super': /super/
'const': /const/
'export': /export/
'import': /import/


# In strict mode:
#'implements': /implements/
#'let': /let/
#'private': /private/
#'public': /public/
#'interface': /interface/
#'package': /package/
#'protected': /protected/
#'static': /static/
#'yield': /yield/

'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/
'[': /\[/
']': /\]/
'.': /\./
';': /;/
',': /,/
'<': /</
'>': />/
'<=': /<=/
'>=': />=/
'==': /==/
'!=': /!=/
'===': /===/
'!==': /!==/
'+': /\+/
'-': /-/
'*': /\*/
'%': /%/
'++': /\+\+/
'--': /--/
'<<': /<</
'>>': />>/
'>>>': />>>/
'&': /&/
'|': /\|/
'^': /^/
'!': /!/
'~': /~/
'&&': /&&/
'||': /\|\|/
'?': /\?/
':': /:/
'=': /=/
'+=': /\+=/
'-=': /-=/
'*=': /\*=/
'%=': /%=/
'<<=': /<<=/
'>>=': />>=/
'>>>=': />>>=/
'&=': /&=/
'|=': /\|=/
'^=': /^=/

'null': /null/
'true': /true/
'false': /false/


exp = /[eE][+-]?[0-9]+/
NumericLiteral: /(0|[1-9][0-9]*)(\.[0-9]*)?{exp}?/
NumericLiteral: /\.[0-9]+{exp}?/
NumericLiteral: /0[Xx]{hex}+/


escape = /\\([^1-9xu\n\r\u2028\u2029]|x{hex}{2}|u{hex}{4})/
lineCont = /\\([\n\r\u2028\u2029]|\r\n)/
dsChar = /[^\n\r"\\\u2028\u2029]|{escape}|{lineCont}/
ssChar = /[^\n\r'\\\u2028\u2029]|{escape}|{lineCont}/

# TODO check \0(?![0-9])

StringLiteral: /"{dsChar}*"/
StringLiteral: /'{ssChar}*'/

[initial]

reBS = /\\[^\n\r\u2028\u2029]/
reClass = /\[([^\n\r\u2028\u2029\]\\]|{reBS})*\]/
reFirst = /[^\n\r\u2028\u2029\*\[\\\/]|{reBS}|{reClass}/
reChar = /{reFirst}|\*/

RegularExpressionLiteral: /\/{reFirst}{reChar}*\/{identifierPart}*/

[div]
'/': /\//
'/=': /\/=/


:: parser

%input Program;

%generate beforeDIV = set(precede '/' | precede '/=');
%generate beforeRE = set(precede RegularExpressionLiteral);
%generate intersection = set(precede RegularExpressionLiteral & (precede '/' | precede '/='));

# TODO set of keywords
IdentifierName ::=
	  Identifier
	| 'break'		| 'do'			| 'instanceof'	| 'typeof'
	| 'case'        | 'else'		| 'new'			| 'var'
	| 'catch'		| 'finally'		| 'return'		| 'void'
	| 'continue'	| 'for'			| 'switch'		| 'while'
	| 'debugger'	| 'function'	| 'this'		| 'with'
	| 'default'		| 'if'			| 'throw'
	| 'delete'		| 'in'			| 'try'

	| 'class' 		| 'enum'		| 'extends'		| 'super'
	| 'const'		| 'export'		| 'import'
;

Literal ::=
	  'null'
	| 'true'
	| 'false'
	| NumericLiteral
	| StringLiteral
	| RegularExpressionLiteral
;

PrimaryExpression ::=
	  'this'
	| Identifier
	| Literal
	| ArrayLiteral
	| ObjectLiteral
	| '(' Expression ')'
;

ArrayLiteral ::=
	  '[' Elisionopt ']'
	| '[' ElementList ']'
	| '[' ElementList ',' Elisionopt ']'
;

ElementList ::=
	  Elisionopt AssignmentExpression
	| ElementList ',' Elisionopt AssignmentExpression
;

Elision ::=
	  ','
	| Elision ','
;

ObjectLiteral ::=
	  '{' '}'
	| '{' PropertyNameAndValueList '}'
	| '{' PropertyNameAndValueList ',' '}'
;

PropertyNameAndValueList ::=
	  PropertyAssignment
	| PropertyNameAndValueList ',' PropertyAssignment
;

# TODO use 'get' and 'set'
PropertyAssignment ::=
	  PropertyName ':' AssignmentExpression
	| Identifier PropertyName '(' ')' '{' FunctionBody '}'
	| Identifier PropertyName '(' PropertySetParameterList ')' '{' FunctionBody '}'
;

PropertyName ::=
	  IdentifierName
	| StringLiteral
	| NumericLiteral
;

PropertySetParameterList ::=
	  Identifier
;

MemberExpression ::=
	  PrimaryExpression
	| FunctionExpression
	| MemberExpression '[' Expression ']'
	| MemberExpression '.' IdentifierName
	| 'new' MemberExpression Arguments
;

NewExpression ::=
	  MemberExpression
	| 'new' NewExpression
;

CallExpression ::=
	  MemberExpression Arguments
	| CallExpression Arguments
	| CallExpression '[' Expression ']'
	| CallExpression '.' IdentifierName
;

Arguments ::=
	  '(' ')'
	| '(' ArgumentList ')'
;

ArgumentList ::=
	  AssignmentExpression
	| ArgumentList ',' AssignmentExpression
;

LeftHandSideExpression ::=
	  NewExpression
	| CallExpression
;

# TODO no LineTerminator after LeftHandSideExpression
PostfixExpression ::=
	  LeftHandSideExpression
	| LeftHandSideExpression '++'
	| LeftHandSideExpression '--'
;

UnaryExpression ::=
	  PostfixExpression
	| 'delete' UnaryExpression
	| 'void' UnaryExpression
	| 'typeof' UnaryExpression
	| '++' UnaryExpression
	| '--' UnaryExpression
	| '+' UnaryExpression
	| '-' UnaryExpression
	| '~' UnaryExpression
	| '!' UnaryExpression
;

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
	| RelationalExpression 'instanceof' ShiftExpression
	| RelationalExpression 'in' ShiftExpression
;

RelationalExpressionNoIn ::=
	  ShiftExpression
	| RelationalExpressionNoIn '<' ShiftExpression
	| RelationalExpressionNoIn '>' ShiftExpression
	| RelationalExpressionNoIn '<=' ShiftExpression
	| RelationalExpressionNoIn '>=' ShiftExpression
	| RelationalExpressionNoIn 'instanceof' ShiftExpression
;

EqualityExpression ::=
	  RelationalExpression
	| EqualityExpression '==' RelationalExpression
	| EqualityExpression '!=' RelationalExpression
	| EqualityExpression '===' RelationalExpression
	| EqualityExpression '!==' RelationalExpression
;

EqualityExpressionNoIn ::=
	  RelationalExpressionNoIn
	| EqualityExpressionNoIn '==' RelationalExpressionNoIn
	| EqualityExpressionNoIn '!=' RelationalExpressionNoIn
	| EqualityExpressionNoIn '===' RelationalExpressionNoIn
	| EqualityExpressionNoIn '!==' RelationalExpressionNoIn
;

BitwiseANDExpression ::=
	  EqualityExpression
	| BitwiseANDExpression '&' EqualityExpression
;

BitwiseANDExpressionNoIn ::=
	  EqualityExpressionNoIn
	| BitwiseANDExpressionNoIn '&' EqualityExpressionNoIn
;

BitwiseXORExpression ::=
	  BitwiseANDExpression
	| BitwiseXORExpression '^' BitwiseANDExpression
;

BitwiseXORExpressionNoIn ::=
	  BitwiseANDExpressionNoIn
	| BitwiseXORExpressionNoIn '^' BitwiseANDExpressionNoIn
;

BitwiseORExpression ::=
	  BitwiseXORExpression
	| BitwiseORExpression '|' BitwiseXORExpression
;

BitwiseORExpressionNoIn ::=
	  BitwiseXORExpressionNoIn
	| BitwiseORExpressionNoIn '|' BitwiseXORExpressionNoIn
;

LogicalANDExpression ::=
	  BitwiseORExpression
	| LogicalANDExpression '&&' BitwiseORExpression
;

LogicalANDExpressionNoIn ::=
	  BitwiseORExpressionNoIn
	| LogicalANDExpressionNoIn '&&' BitwiseORExpressionNoIn
;

LogicalORExpression ::=
	  LogicalANDExpression
	| LogicalORExpression '||' LogicalANDExpression
;

LogicalORExpressionNoIn ::=
	  LogicalANDExpressionNoIn
	| LogicalORExpressionNoIn '||' LogicalANDExpressionNoIn
;

ConditionalExpression ::=
	  LogicalORExpression
	| LogicalORExpression '?' AssignmentExpression ':' AssignmentExpression
;

ConditionalExpressionNoIn ::=
	  LogicalORExpressionNoIn
	| LogicalORExpressionNoIn '?' AssignmentExpressionNoIn ':' AssignmentExpressionNoIn
;

AssignmentExpression ::=
	  ConditionalExpression
	| LeftHandSideExpression AssignmentOperator AssignmentExpression
;

AssignmentExpressionNoIn ::=
	  ConditionalExpressionNoIn
	| LeftHandSideExpression AssignmentOperator AssignmentExpressionNoIn
;

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
	  AssignmentExpression
	| Expression ',' AssignmentExpression
;

ExpressionNoIn ::=
	  AssignmentExpressionNoIn
	| ExpressionNoIn ',' AssignmentExpressionNoIn
;

Statement ::=
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

Block ::=
	  '{' StatementList? '}'
;

StatementList ::=
	  Statement
	| StatementList Statement
;

VariableStatement ::=
	  'var' VariableDeclarationList ';'
;

VariableDeclarationList ::=
	  VariableDeclaration
	| VariableDeclarationList ',' VariableDeclaration
;

VariableDeclarationListNoIn ::=
	  VariableDeclarationNoIn
	| VariableDeclarationListNoIn ',' VariableDeclarationNoIn
;

VariableDeclaration ::=
	  Identifier Initialiseropt
;

VariableDeclarationNoIn ::=
	  Identifier InitialiserNoInopt
;

Initialiser ::=
	  '=' AssignmentExpression
;

InitialiserNoIn ::=
	  '=' AssignmentExpressionNoIn
;

EmptyStatement ::=
	  ';'
;

# TODO Expression doesn't start with '{' or 'function'
ExpressionStatement ::=
	'++' Expression ';' ;
# FIXME ^^ remove '++'


%right 'else';

IfStatement ::=
	  'if' '(' Expression ')' Statement 'else' Statement
	| 'if' '(' Expression ')' Statement %prio 'else'
;

IterationStatement ::=
	  'do' Statement 'while' '(' Expression ')' ';'
	| 'while' '(' Expression ')' Statement
	| 'for' '(' ExpressionNoInopt ';' Expressionopt ';' Expressionopt ')' Statement
	| 'for' '(' 'var' VariableDeclarationListNoIn ';' Expressionopt ';' Expressionopt ')' Statement
	| 'for' '(' LeftHandSideExpression 'in' Expression ')' Statement
	| 'for' '(' 'var' VariableDeclarationNoIn 'in' Expression ')' Statement
;

# TODO no LineTerminator after 'continue'
ContinueStatement ::=
	'continue' Identifier? ';' ;

# TODO no LineTerminator after 'break'
BreakStatement ::=
	'break' Identifier? ';' ;

# TODO no LineTerminator after 'return'
ReturnStatement ::=
    'return' Expressionopt ';' ;

WithStatement ::=
	  'with' '(' Expression ')' Statement
;

SwitchStatement ::=
	  'switch' '(' Expression ')' CaseBlock
;

CaseBlock ::=
	  '{' CaseClausesopt '}'
	| '{' CaseClausesopt DefaultClause CaseClausesopt '}'
;

CaseClauses ::=
	  CaseClause
	| CaseClauses CaseClause
;

CaseClause ::=
	  'case' Expression ':' StatementListopt
;

DefaultClause ::=
	  'default' ':' StatementListopt
;

LabelledStatement ::=
	  Identifier ':' Statement
;

ThrowStatement ::=
# TODO no LineTerminator after 'throw'
	'throw' Expression ';'
;

TryStatement ::=
	  'try' Block Catch
	| 'try' Block Finally
	| 'try' Block Catch Finally
;

Catch ::=
	  'catch' '(' Identifier ')' Block
;

Finally ::=
	  'finally' Block
;

DebuggerStatement ::=
	  'debugger' ';'
;

FunctionDeclaration ::=
	  'function' Identifier '(' FormalParameterListopt ')' '{' FunctionBody '}'
;

FunctionExpression ::=
	  'function' Identifier? '(' FormalParameterListopt ')' '{' FunctionBody '}'
;

FormalParameterList ::=
	  Identifier
	| FormalParameterList ',' Identifier
;

FunctionBody ::=
	  SourceElementsopt
;

Program ::=
	  SourceElementsopt
;

SourceElements ::=
	  SourceElement
	| SourceElements SourceElement
;

SourceElement ::=
	  Statement
	| FunctionDeclaration
;
