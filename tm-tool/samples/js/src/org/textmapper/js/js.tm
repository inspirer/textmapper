language javascript(java);

package = "org.textmapper.js"
prefix = "Js"
breaks = true
gentree = true
genast = false
positions = "line,offset"
endpositions = "offset"
genbison = true

:: lexer

[initial, div]

space: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)

# Note: LineTerminator: /[\n\r\u2028\u2029]/
LineTerminatorSequence: /[\n\r\u2028\u2029]|\r\n/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
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
'case': /case/
'catch': /catch/
'continue': /continue/
'debugger': /debugger/
'default': /default/
'delete': /delete/
'do': /do/
'else': /else/
'finally': /finally/
'for': /for/
'function': /function/
'if': /if/
'in': /in/
'instanceof': /instanceof/
'new': /new/
'return': /return/
'switch': /switch/
'this': /this/
'throw': /throw/
'try': /try/
'typeof': /typeof/
'var': /var/
'void': /void/
'while': /while/
'with': /with/

# Future-reserved.
'class': /class/
'const': /const/
'enum': /enum/
'export': /export/
'extends': /extends/
'import': /import/
'super': /super/


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

# The following sets are used to figure out in which state the lexer
# should continue in order to correctly tell apart Division from RegexpLiteral.
#  http://stackoverflow.com/questions/5519596#11766233

%generate beforeDIV = set(precede '/' | precede '/=');
%generate beforeRE = set(precede RegularExpressionLiteral);
%generate intersection = set(precede RegularExpressionLiteral & (precede '/' | precede '/='));
%generate nonExprParens = set('with' | 'for' | 'while' | 'if');
%generate kwBeforeRE = set('new' | 'delete' | 'void' | 'typeof' | 'instanceof' |
                           'in' | 'do' | 'return' | 'case' | 'throw' | 'else');

# TODO set of keywords
IdentifierName ::=
	  Identifier

	# Keywords
	| 'break'		| 'do'			| 'instanceof'	| 'typeof'
	| 'case'        | 'else'		| 'new'			| 'var'
	| 'catch'		| 'finally'		| 'return'		| 'void'
	| 'continue'	| 'for'			| 'switch'		| 'while'
	| 'debugger'	| 'function'	| 'this'		| 'with'
	| 'default'		| 'if'			| 'throw'
	| 'delete'		| 'in'			| 'try'

	# Future-reserved.
	| 'class' 		| 'enum'		| 'extends'		| 'super'
	| 'const'		| 'export'		| 'import'

	# NullLiteral | BooleanLiteral
	| 'null' | 'true' | 'false'
;

Literal ::=
	  'null'
	| 'true'
	| 'false'
	| NumericLiteral
	| StringLiteral
	| RegularExpressionLiteral
;

%flag ExprStart = false;

PrimaryExpression<ExprStart> ::=
	  'this'
	| Identifier
	| Literal
	| ArrayLiteral
	| [!ExprStart] ObjectLiteral
	| '(' Expression<~ExprStart> ')'
;

ArrayLiteral ::=
	  '[' (AssignmentExpression? separator ',')+ ']'
;

ObjectLiteral ::=
	  '{' ((PropertyAssignment separator ',')+ ','?)? '}'
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
	  Identifier ;

MemberExpression<ExprStart> ::=
	  PrimaryExpression
	| [!ExprStart] FunctionExpression
	| MemberExpression '[' Expression<~ExprStart> ']'
	| MemberExpression '.' IdentifierName
	| 'new' MemberExpression<~ExprStart> Arguments
;

NewExpression<ExprStart> ::=
	  MemberExpression
	| 'new' NewExpression<~ExprStart>
;

CallExpression<ExprStart> ::=
	  MemberExpression Arguments
	| CallExpression Arguments
	| CallExpression '[' Expression<~ExprStart> ']'
	| CallExpression '.' IdentifierName
;

Arguments ::=
	  '(' (AssignmentExpression separator ',')* ')' ;

LeftHandSideExpression<ExprStart> ::=
	  NewExpression
	| CallExpression
;

# Note: no LineTerminator after LeftHandSideExpression
PostfixExpression<ExprStart> ::=
	  LeftHandSideExpression
	| LeftHandSideExpression '++'
	| LeftHandSideExpression '--'
;

UnaryExpression<ExprStart> ::=
	  PostfixExpression
	| 'delete' UnaryExpression<~ExprStart>
	| 'void' UnaryExpression<~ExprStart>
	| 'typeof' UnaryExpression<~ExprStart>
	| '++' UnaryExpression<~ExprStart>
	| '--' UnaryExpression<~ExprStart>
	| '+' UnaryExpression<~ExprStart>
	| '-' UnaryExpression<~ExprStart>
	| '~' UnaryExpression<~ExprStart>
	| '!' UnaryExpression<~ExprStart>
;

MultiplicativeExpression<ExprStart> ::=
	  UnaryExpression
	| MultiplicativeExpression '*' UnaryExpression<~ExprStart>
	| MultiplicativeExpression '/' UnaryExpression<~ExprStart>
	| MultiplicativeExpression '%' UnaryExpression<~ExprStart>
;

AdditiveExpression<ExprStart> ::=
	  MultiplicativeExpression
	| AdditiveExpression '+' MultiplicativeExpression<~ExprStart>
	| AdditiveExpression '-' MultiplicativeExpression<~ExprStart>
;

ShiftExpression<ExprStart> ::=
	  AdditiveExpression
	| ShiftExpression '<<' AdditiveExpression<~ExprStart>
	| ShiftExpression '>>' AdditiveExpression<~ExprStart>
	| ShiftExpression '>>>' AdditiveExpression<~ExprStart>
;

%flag NoIn = false;

RelationalExpression<NoIn, ExprStart> ::=
	  ShiftExpression<ExprStart>
	| RelationalExpression '<' ShiftExpression<~ExprStart>
	| RelationalExpression '>' ShiftExpression<~ExprStart>
	| RelationalExpression '<=' ShiftExpression<~ExprStart>
	| RelationalExpression '>=' ShiftExpression<~ExprStart>
	| RelationalExpression 'instanceof' ShiftExpression<~ExprStart>
	| [!NoIn] RelationalExpression 'in' ShiftExpression<~ExprStart>
;

EqualityExpression<NoIn, ExprStart> ::=
	  RelationalExpression
	| EqualityExpression '==' RelationalExpression<NoIn, ~ExprStart>
	| EqualityExpression '!=' RelationalExpression<NoIn, ~ExprStart>
	| EqualityExpression '===' RelationalExpression<NoIn, ~ExprStart>
	| EqualityExpression '!==' RelationalExpression<NoIn, ~ExprStart>
;

BitwiseANDExpression<NoIn, ExprStart> ::=
	  EqualityExpression
	| BitwiseANDExpression '&' EqualityExpression<NoIn, ~ExprStart>
;

BitwiseXORExpression<NoIn, ExprStart> ::=
	  BitwiseANDExpression
	| BitwiseXORExpression '^' BitwiseANDExpression<NoIn, ~ExprStart>
;

BitwiseORExpression<NoIn, ExprStart> ::=
	  BitwiseXORExpression
	| BitwiseORExpression '|' BitwiseXORExpression<NoIn, ~ExprStart>
;

LogicalANDExpression<NoIn, ExprStart> ::=
	  BitwiseORExpression
	| LogicalANDExpression '&&' BitwiseORExpression<NoIn, ~ExprStart>
;

LogicalORExpression<NoIn, ExprStart> ::=
	  LogicalANDExpression
	| LogicalORExpression '||' LogicalANDExpression<NoIn, ~ExprStart>
;

ConditionalExpression<NoIn, ExprStart> ::=
	  LogicalORExpression
	| LogicalORExpression '?' AssignmentExpression<NoIn, ~ExprStart> ':' AssignmentExpression<NoIn, ~ExprStart>
;

AssignmentExpression<NoIn, ExprStart> ::=
	  ConditionalExpression
	| LeftHandSideExpression<ExprStart> AssignmentOperator AssignmentExpression<NoIn, ~ExprStart>
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

Expression<NoIn, ExprStart> ::=
	  AssignmentExpression
	| Expression ',' AssignmentExpression<NoIn, ~ExprStart>
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
	  '{' Statement* '}' ;

VariableStatement ::=
	  'var' VariableDeclarationList ';' ;

VariableDeclarationList<NoIn> ::=
	  VariableDeclaration
	| VariableDeclarationList ',' VariableDeclaration
;

VariableDeclaration<NoIn> ::=
	  Identifier Initialiseropt ;

Initialiser<NoIn> ::=
	  '=' AssignmentExpression<NoIn> ;

EmptyStatement ::=
	  ';' ;

ExpressionStatement ::=
	Expression<+ExprStart> ';' ;

%right 'else';

IfStatement ::=
	  'if' '(' Expression ')' Statement 'else' Statement
	| 'if' '(' Expression ')' Statement %prec 'else'
;

IterationStatement ::=
	  'do' Statement 'while' '(' Expression ')' ';'
	| 'while' '(' Expression ')' Statement
	| 'for' '(' Expressionopt<+NoIn> ';' Expressionopt ';' Expressionopt ')' Statement
	| 'for' '(' 'var' VariableDeclarationList<+NoIn> ';' Expressionopt ';' Expressionopt ')' Statement
	| 'for' '(' LeftHandSideExpression 'in' Expression ')' Statement
	| 'for' '(' 'var' VariableDeclaration<+NoIn> 'in' Expression ')' Statement
;

ContinueStatement ::=
# Note: no LineTerminator after 'continue'
	'continue' Identifier? ';' ;

BreakStatement ::=
# Note: no LineTerminator after 'break'
	'break' Identifier? ';' ;

ReturnStatement ::=
# Note: no LineTerminator after 'return'
    'return' Expressionopt ';' ;

WithStatement ::=
	  'with' '(' Expression ')' Statement ;

SwitchStatement ::=
	  'switch' '(' Expression ')' CaseBlock ;

CaseBlock ::=
	  '{' CaseClause* (DefaultClause CaseClause*)? '}' ;

CaseClause ::=
	  'case' Expression ':' Statement* ;

DefaultClause ::=
	  'default' ':' Statement* ;

LabelledStatement ::=
	  Identifier ':' Statement ;

ThrowStatement ::=
# Note: no LineTerminator after 'throw'
	'throw' Expression ';'
;

TryStatement ::=
	  'try' Block (Catch | Finally | Catch Finally) ;

Catch ::=
	  'catch' '(' Identifier ')' Block ;

Finally ::=
	  'finally' Block ;

DebuggerStatement ::=
	  'debugger' ';' ;

FunctionDeclaration ::=
	  'function' Identifier '(' FormalParameterListopt ')' '{' FunctionBody '}' ;

FunctionExpression ::=
	  'function' Identifier? '(' FormalParameterListopt ')' '{' FunctionBody '}' ;

FormalParameterList ::=
	  Identifier
	| FormalParameterList ',' Identifier
;

FunctionBody ::=
	  SourceElement* ;

Program ::=
	  SourceElement* ;

SourceElement ::=
	  Statement
	| FunctionDeclaration
;
