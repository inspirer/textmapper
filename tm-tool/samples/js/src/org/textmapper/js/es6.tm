language es6(bison);

module = "es6"

#package = "org.textmapper.js"
#prefix = "Js6"
#breaks = true
#gentree = true
#genast = false
#positions = "line,offset"
#endpositions = "offset"

:: lexer

[initial, div, template, template_div]

# TODO
lookahead1:

WhiteSpace: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)

LineTerminatorSequence: /[\n\r\u2028\u2029]|\r\n/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment: /\/\*{commentChars}\*\// (space)
SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/ (space)

# Note: see http://unicode.org/reports/tr31/
ID_Start = /\p{Lu}|\p{Ll}|\p{Lt}|\p{Lm}|\p{Lo}|\p{Nl}|{Other_ID_Start}/
ID_Continue = /{ID_Start}|\p{Mn}|\p{Mc}|\p{Nd}|\p{Pc}|{Other_ID_Continue}/
Other_ID_Start = /\u2118|\u212E|\u309B|\u309C/
Other_ID_Continue = /\u1369|\u00B7|\u0387|\u19DA/
Join_Control = /\u200c|\u200d/

hex = /[0-9a-fA-F]/
unicodeEscapeSequence = /u(\{{hex}+\}|{hex}{4})/

identifierStart = /{ID_Start}|$|_|\\{unicodeEscapeSequence}/
identifierPart = /{identifierStart}|{ID_Continue}|{Join_Control}/

Identifier: /{identifierStart}{identifierPart}*/    (class)

## TODO: smart keywords? (used in PropertyAssignment)
#	get set

'break': /break/
'case': /case/
'catch': /catch/
'class': /class/
'const': /const/
'continue': /continue/
'debugger': /debugger/
'default': /default/
'delete': /delete/
'do': /do/
'else': /else/
'export': /export/
'extends': /extends/
'finally': /finally/
'for': /for/
'function': /function/
'if': /if/
'import': /import/
'in': /in/
'instanceof': /instanceof/
'new': /new/
'return': /return/
'super': /super/
'switch': /switch/
'this': /this/
'throw': /throw/
'try': /try/
'typeof': /typeof/
'var': /var/
'void': /void/
'while': /while/
'with': /with/
'yield': /yield/

# Future-reserved.
'await': /await/
'enum': /enum/

# In strict mode:
#'implements': /implements/
#'interface': /interface/
#'package': /package/
#'private': /private/
#'protected': /protected/
#'public': /public/

'null': /null/
'true': /true/
'false': /false/


# Soft (contextual) keywords. FIXME
'target': /target/
'of': /of/
'let': /let/
'static': /static/
'as': /as/
'from': /from/
'get': /get/
'set': /set/


'{': /\{/
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
'=>': /=>/

exp = /[eE][+-]?[0-9]+/
NumericLiteral: /(0|[1-9][0-9]*)(\.[0-9]*)?{exp}?/
NumericLiteral: /\.[0-9]+{exp}?/
NumericLiteral: /0[Xx]{hex}+/
NumericLiteral: /0[oO][0-7]+/
NumericLiteral: /0[bB][01]+/

escape = /\\([^1-9xu\n\r\u2028\u2029]|x{hex}{2}|{unicodeEscapeSequence})/
lineCont = /\\([\n\r\u2028\u2029]|\r\n)/
dsChar = /[^\n\r"\\\u2028\u2029]|{escape}|{lineCont}/
ssChar = /[^\n\r'\\\u2028\u2029]|{escape}|{lineCont}/

# TODO check \0 is valid if [lookahead != DecimalDigit]

StringLiteral: /"{dsChar}*"/
StringLiteral: /'{ssChar}*'/

tplChars = /([^\$`\\]|\$*{escape}|\$*{lineCont}|\$+[^\$\{`\\])*\$*/

[initial, div]

'}': /\}/

NoSubstitutionTemplate: /`{tplChars}`/
TemplateHead: /`{tplChars}\$\{/

[template, template_div]

TemplateMiddle: /\}{tplChars}\$\{/
TemplateTail: /\}{tplChars}`/

[initial, template]

reBS = /\\[^\n\r\u2028\u2029]/
reClass = /\[([^\n\r\u2028\u2029\]\\]|{reBS})*\]/
reFirst = /[^\n\r\u2028\u2029\*\[\\\/]|{reBS}|{reClass}/
reChar = /{reFirst}|\*/

RegularExpressionLiteral: /\/{reFirst}{reChar}*\/{identifierPart}*/

[div, template_div]

'/': /\//
'/=': /\/=/


:: parser

%input Module;

%flag In = false;
%flag Yield = false;
%flag Default = false;
%flag Return = false;

IdentifierName ::=
	  Identifier

	# Keywords
	| 'break' 		| 'do' 			| 'in' 			| 'typeof'
	| 'case' 		| 'else' 		| 'instanceof'	| 'var'
	| 'catch'		| 'export'		| 'new'			| 'void'
	| 'class'		| 'extends'		| 'return'		| 'while'
	| 'const'		| 'finally'		| 'super'		| 'with'
	| 'continue'	| 'for'			| 'switch'		| 'yield'
	| 'debugger'	| 'function'	| 'this'
	| 'default'		| 'if'			| 'throw'
	| 'delete'		| 'import'		| 'try'

	# Future-reserved.
	| 'enum' | 'await'

	# NullLiteral | BooleanLiteral
	| 'null' | 'true' | 'false'
;

# A.2 Expressions

IdentifierReference<Yield> ::=
	  Identifier
	| [!Yield] 'yield'
;

BindingIdentifier<Yield> ::=
	  Identifier
	| [!Yield] 'yield'
;

LabelIdentifier<Yield> ::=
	  Identifier
	| [!Yield] 'yield'
;

PrimaryExpression<Yield> ::=
	  'this'
	| IdentifierReference
	| Literal
	| ArrayLiteral
	| ObjectLiteral
	| FunctionExpression
	| ClassExpression
	| GeneratorExpression
	| RegularExpressionLiteral
	| TemplateLiteral
	| CoverParenthesizedExpressionAndArrowParameterList
;

CoverParenthesizedExpressionAndArrowParameterList<Yield> ::=
	  '(' Expression<+In,Yield> ')'
	| '(' ')'
	| '(' '.' '.' '.' BindingIdentifier<Yield> ')'
	| '(' Expression<+In,Yield> ',' '.' '.' '.' BindingIdentifier<Yield> ')'
;

Literal ::=
	  'null'
	| 'true'
	| 'false'
	| NumericLiteral
	| StringLiteral
;

ArrayLiteral<Yield> ::=
	  '[' Elisionopt ']'
	| '[' ElementList<Yield> ']'
	| '[' ElementList<Yield> ',' Elisionopt ']'
;

ElementList<Yield> ::=
	  Elisionopt AssignmentExpression<+In,Yield>
	| Elisionopt SpreadElement<Yield>
	| ElementList<Yield> ',' Elisionopt AssignmentExpression<+In,Yield>
	| ElementList<Yield> ',' Elisionopt SpreadElement<Yield>
;

Elision ::=
	  ','
	| Elision ','
;

SpreadElement<Yield> ::=
	  '.' '.' '.' AssignmentExpression<+In,Yield>
;

ObjectLiteral<Yield> ::=
	  '{' '}'
	| '{' PropertyDefinitionList<Yield> '}'
	| '{' PropertyDefinitionList<Yield> ',' '}'
;

PropertyDefinitionList<Yield> ::=
	  PropertyDefinition<Yield>
	| PropertyDefinitionList<Yield> ',' PropertyDefinition<Yield>
;

PropertyDefinition<Yield> ::=
	  IdentifierReference<Yield>
	| CoverInitializedName<Yield>
	| PropertyName<Yield> ':' AssignmentExpression<+In,Yield>
	| MethodDefinition<Yield>
;

PropertyName<Yield> ::=
	  LiteralPropertyName
	| ComputedPropertyName
;

LiteralPropertyName ::=
	  IdentifierName
	| StringLiteral
	| NumericLiteral
;

ComputedPropertyName<Yield> ::=
	'[' AssignmentExpression<+In,Yield> ']'
;

CoverInitializedName<Yield> ::=
	  IdentifierReference<Yield> Initializer<+In,Yield>
;

Initializer<In, Yield> ::=
	  '=' AssignmentExpression
;

TemplateLiteral<Yield> ::=
	  NoSubstitutionTemplate
	| TemplateHead Expression<+In,Yield> TemplateSpans
;

TemplateSpans<Yield> ::=
	  TemplateTail
	| TemplateMiddleList TemplateTail
;

TemplateMiddleList<Yield> ::=
	  TemplateMiddle Expression<+In,Yield>
	| TemplateMiddleList TemplateMiddle Expression<+In,Yield>
;

MemberExpression<Yield> ::=
	  PrimaryExpression<Yield>
	| MemberExpression<Yield> '[' Expression<+In,Yield> ']'
	| MemberExpression<Yield> '.' IdentifierName
	| MemberExpression<Yield> TemplateLiteral<Yield>
	| SuperProperty<Yield>
	| MetaProperty
	| 'new' MemberExpression<Yield> Arguments<Yield>
;

SuperProperty<Yield> ::=
	  'super' '[' Expression<+In,Yield> ']'
	| 'super' '.' IdentifierName
;

MetaProperty ::=
	  NewTarget
;

NewTarget ::=
	  'new' '.' 'target'
;

NewExpression<Yield> ::=
	  MemberExpression<Yield>
	| 'new' NewExpression<Yield>
;

CallExpression<Yield> ::=
	  MemberExpression<Yield> Arguments<Yield>
	| SuperCall<Yield>
	| CallExpression<Yield> Arguments<Yield>
	| CallExpression<Yield> '[' Expression<+In,Yield> ']'
	| CallExpression<Yield> '.' IdentifierName
	| CallExpression<Yield> TemplateLiteral<Yield>
;

SuperCall<Yield> ::=
	  'super' Arguments
;

Arguments<Yield> ::=
	  '(' ')'
	| '(' ArgumentList ')'
;

ArgumentList<Yield> ::=
	  AssignmentExpression<+In,Yield>
	| '.' '.' '.' AssignmentExpression<+In,Yield>
	| ArgumentList ',' AssignmentExpression<+In,Yield>
	| ArgumentList ',' '.' '.' '.' AssignmentExpression<+In,Yield>
;

LeftHandSideExpression<Yield> ::=
	  NewExpression
	| CallExpression
;

PostfixExpression<Yield> ::=
	  LeftHandSideExpression
	| LeftHandSideExpression /* no LineTerminator */ '++'
	| LeftHandSideExpression /* no LineTerminator */ '--'
;

UnaryExpression<Yield> ::=
	  PostfixExpression<Yield>
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

MultiplicativeExpression<Yield> ::=
	  UnaryExpression
	| MultiplicativeExpression<Yield> MultiplicativeOperator UnaryExpression
;

MultiplicativeOperator ::=
	  '*' | '/' | '%' ;

AdditiveExpression<Yield> ::=
	  MultiplicativeExpression<Yield>
	| AdditiveExpression<Yield> '+' MultiplicativeExpression<Yield>
	| AdditiveExpression<Yield> '-' MultiplicativeExpression<Yield>
;

ShiftExpression<Yield> ::=
	  AdditiveExpression<Yield>
	| ShiftExpression<Yield> '<<' AdditiveExpression<Yield>
	| ShiftExpression<Yield> '>>' AdditiveExpression<Yield>
	| ShiftExpression<Yield> '>>>' AdditiveExpression<Yield>
;

RelationalExpression<In, Yield> ::=
	  ShiftExpression<Yield>
	| RelationalExpression '<' ShiftExpression<Yield>
	| RelationalExpression '>' ShiftExpression<Yield>
	| RelationalExpression '<=' ShiftExpression<Yield>
	| RelationalExpression '>=' ShiftExpression<Yield>
	| RelationalExpression 'instanceof' ShiftExpression<Yield>
	| [In] RelationalExpression<+In,Yield> 'in' ShiftExpression<Yield>
;

EqualityExpression<In, Yield> ::=
	  RelationalExpression
	| EqualityExpression '==' RelationalExpression
	| EqualityExpression '!=' RelationalExpression
	| EqualityExpression '===' RelationalExpression
	| EqualityExpression '!==' RelationalExpression
;

BitwiseANDExpression<In, Yield> ::=
	  EqualityExpression
	| BitwiseANDExpression '&' EqualityExpression
;

BitwiseXORExpression<In, Yield> ::=
	  BitwiseANDExpression
	| BitwiseXORExpression '^' BitwiseANDExpression
;

BitwiseORExpression<In, Yield> ::=
	  BitwiseXORExpression
	| BitwiseORExpression '|' BitwiseXORExpression
;

LogicalANDExpression<In, Yield> ::=
	  BitwiseORExpression
	| LogicalANDExpression '&&' BitwiseORExpression
;

LogicalORExpression<In, Yield> ::=
	  LogicalANDExpression
	| LogicalORExpression '||' LogicalANDExpression
;

ConditionalExpression<In, Yield> ::=
	  LogicalORExpression
	| LogicalORExpression '?' AssignmentExpression<+In,Yield> ':' AssignmentExpression
;

AssignmentExpression<In, Yield> ::=
	  ConditionalExpression
	| [Yield] YieldExpression<In>
	| ArrowFunction
	| LeftHandSideExpression<Yield> '=' AssignmentExpression
	| LeftHandSideExpression<Yield> AssignmentOperator AssignmentExpression
;

AssignmentOperator ::=
	  '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|=' ;

Expression<In, Yield> ::=
	  AssignmentExpression
	| Expression ',' AssignmentExpression
;

# A.3 Statements

Statement<Yield, Return> ::=
	  BlockStatement
	| VariableStatement<Yield>
	| EmptyStatement
	| ExpressionStatement<Yield>
	| IfStatement
	| BreakableStatement
	| ContinueStatement<Yield>
	| BreakStatement<Yield>
	| [Return] ReturnStatement<Yield>
	| WithStatement<Yield,Return>
	| LabelledStatement
	| ThrowStatement<Yield>
	| TryStatement
	| DebuggerStatement
;

Declaration<Yield> ::=
	  HoistableDeclaration<Yield>
	| ClassDeclaration<Yield>
	| LexicalDeclaration<+In,Yield>
;

HoistableDeclaration<Yield, Default> ::=
	  FunctionDeclaration
	| GeneratorDeclaration
;

BreakableStatement<Yield, Return> ::=
	  IterationStatement
	| SwitchStatement
;

BlockStatement<Yield, Return> ::=
	  Block
;

Block<Yield, Return> ::=
	  '{' StatementList? '}'
;

StatementList<Yield, Return> ::=
	  StatementListItem
	| StatementList StatementListItem
;

StatementListItem<Yield, Return> ::=
	  Statement
	| Declaration<Yield>
;

LexicalDeclaration<In, Yield> ::=
	  LetOrConst BindingList ';'
;

LetOrConst ::=
	  'let'
	| 'const'
;

BindingList<In, Yield> ::=
	  LexicalBinding
	| BindingList ',' LexicalBinding
;

LexicalBinding<In, Yield> ::=
	  BindingIdentifier<Yield> Initializeropt
	| BindingPattern<Yield> Initializer
;

VariableStatement<Yield> ::=
	  'var' VariableDeclarationList<+In,Yield> ';'
;

VariableDeclarationList<In, Yield> ::=
	  VariableDeclaration
	| VariableDeclarationList ',' VariableDeclaration
;

VariableDeclaration<In, Yield> ::=
	  BindingIdentifier<Yield> Initializeropt
	| BindingPattern<Yield> Initializer
;

BindingPattern<Yield> ::=
	  ObjectBindingPattern
	| ArrayBindingPattern
;

ObjectBindingPattern<Yield> ::=
	  '{' '}'
	| '{' BindingPropertyList '}'
	| '{' BindingPropertyList ',' '}'
;

ArrayBindingPattern<Yield> ::=
	  '[' Elisionopt BindingRestElementopt ']'
	| '[' BindingElementList ']'
	| '[' BindingElementList ',' Elisionopt BindingRestElementopt ']'
;

BindingPropertyList<Yield> ::=
	  BindingProperty
	| BindingPropertyList ',' BindingProperty
;

BindingElementList<Yield> ::=
	  BindingElisionElement
	| BindingElementList ',' BindingElisionElement
;

BindingElisionElement<Yield> ::=
	  Elisionopt BindingElement
;

BindingProperty<Yield> ::=
	  SingleNameBinding
	| PropertyName ':' BindingElement
;

BindingElement<Yield> ::=
	  SingleNameBinding
	| BindingPattern Initializeropt<+In,Yield>
;

SingleNameBinding<Yield> ::=
	  BindingIdentifier Initializeropt<+In,Yield>
;

BindingRestElement<Yield> ::=
	  '.' '.' '.' BindingIdentifier
;

EmptyStatement ::=
	  ';' ;

ExpressionStatement<Yield> ::=
# TODO: -- ClassExpression, FunctionExpression, GeneratorExpression, ObjectLiteral
	lookahead1
	/* lookahead != {'{', function, class, let '[' } */ Expression<+In,Yield> ';'
;

%right 'else';

IfStatement<Yield, Return> ::=
	  'if' '(' Expression<+In,Yield> ')' Statement 'else' Statement
	| 'if' '(' Expression<+In,Yield> ')' Statement %prec 'else'
;

IterationStatement<Yield, Return> ::=
	  'do' Statement 'while' '(' Expression<+In,Yield> ')' ';'
	| 'while' '(' Expression<+In,Yield> ')' Statement
	| 'for' '(' Expressionopt<Yield> ';' Expressionopt<+In,Yield> ';' Expressionopt<+In,Yield> ')' Statement
	| 'for' '(' 'var' VariableDeclarationList<Yield> ';' Expressionopt<+In,Yield> ';' Expressionopt<+In,Yield> ')' Statement
	| 'for' '(' LexicalDeclaration<Yield> Expressionopt<+In,Yield> ';' Expressionopt<+In,Yield> ')' Statement
	| 'for' '(' LeftHandSideExpression<Yield> 'in' Expression<+In,Yield> ')' Statement
	| 'for' '(' 'var' ForBinding<Yield> 'in' Expression<+In,Yield> ')' Statement
	| 'for' '(' ForDeclaration<Yield> 'in' Expression<+In,Yield> ')' Statement
	| 'for' '(' LeftHandSideExpression<Yield> 'of' AssignmentExpression<+In,Yield> ')' Statement
	| 'for' '(' 'var' ForBinding<Yield> 'of' AssignmentExpression<+In,Yield> ')' Statement
	| 'for' '(' ForDeclaration<Yield> 'of' AssignmentExpression<+In,Yield> ')' Statement
;

ForDeclaration<Yield> ::=
	  LetOrConst ForBinding<Yield>
;

ForBinding<Yield> ::=
	  BindingIdentifier<Yield>
	| BindingPattern<Yield>
;

ContinueStatement<Yield> ::=
	  'continue' ';'
	| 'continue' /* no LineTerminator */ LabelIdentifier ';'
;

BreakStatement<Yield> ::=
	  'break' ';'
	| 'break' /* no LineTerminator */ LabelIdentifier ';'
;

ReturnStatement<Yield> ::=
	  'return' ';'
	| 'return' /* no LineTerminator */ Expression<+In,Yield> ';'
;

WithStatement<Yield, Return> ::=
	  'with' '(' Expression<+In,Yield> ')' Statement
;

SwitchStatement<Yield, Return> ::=
	  'switch' '(' Expression<+In,Yield> ')' CaseBlock<Yield,Return>
;

CaseBlock<Yield, Return> ::=
	  '{' CaseClausesopt<Yield,Return> '}'
	| '{' CaseClausesopt<Yield,Return> DefaultClause<Yield,Return> CaseClausesopt<Yield,Return> '}'
;

CaseClauses<Yield, Return> ::=
	  CaseClause<Yield,Return>
	| CaseClauses<Yield,Return> CaseClause<Yield,Return>
;

CaseClause<Yield, Return> ::=
	  'case' Expression<+In,Yield> ':' StatementList<Yield,Return>?
;

DefaultClause<Yield, Return> ::=
	  'default' ':' StatementList<Yield,Return>?
;

LabelledStatement<Yield, Return> ::=
	  Identifier ':' LabelledItem
	| [!Yield] 'yield' ':' LabelledItem
;

LabelledItem<Yield, Return> ::=
	  Statement
	| FunctionDeclaration<Yield>
;

ThrowStatement<Yield> ::=
	  'throw' /* no LineTerminator */ Expression<+In,Yield> ';'
;

TryStatement<Yield, Return> ::=
	  'try' Block<Yield,Return> Catch<Yield,Return>
	| 'try' Block<Yield,Return> Finally<Yield,Return>
	| 'try' Block<Yield,Return> Catch<Yield,Return> Finally<Yield,Return>
;

Catch<Yield, Return> ::=
	  'catch' '(' CatchParameter<Yield> ')' Block<Yield,Return>
;

Finally<Yield, Return> ::=
	  'finally' Block
;

CatchParameter<Yield> ::=
	  BindingIdentifier
	| BindingPattern
;

DebuggerStatement ::=
	  'debugger' ';'
;

# A.4 Functions and Classes

FunctionDeclaration<Yield, Default> ::=
	  'function' BindingIdentifier<Yield> '(' FormalParameters<~Yield> ')' '{' FunctionBody<~Yield> '}'
	| [Default] 'function' '(' FormalParameters ')' '{' FunctionBody '}'
;

FunctionExpression ::=
	  'function' BindingIdentifier? '(' FormalParameters ')' '{' FunctionBody '}'
;

StrictFormalParameters<Yield> ::=
	  FormalParameters ;

FormalParameters<Yield> ::=
      FormalParameterList? ;

FormalParameterList<Yield> ::=
	  FunctionRestParameter
	| FormalsList
	| FormalsList ',' FunctionRestParameter
;

FormalsList<Yield> ::=
	  FormalParameter
	| FormalsList ',' FormalParameter
;

FunctionRestParameter<Yield> ::=
	  BindingRestElement ;

FormalParameter<Yield> ::=
	  BindingElement ;

FunctionBody<Yield> ::=
	  StatementList<Yield,+Return>? ;

ArrowFunction<In, Yield> ::=
	  ArrowParameters<Yield> /* no LineTerminator */ '=>' ConciseBody<In> ;

ArrowParameters<Yield> ::=
	  BindingIdentifier
	| CoverParenthesizedExpressionAndArrowParameterList
;

ConciseBody<In> ::=
# TODO: -- ObjectLiteral
	  lookahead1
			  /* lookahead != { */ AssignmentExpression<In>
	| '{' FunctionBody '}'
;

MethodDefinition<Yield> ::=
	  PropertyName<Yield> '(' StrictFormalParameters ')' '{' FunctionBody '}'
	| GeneratorMethod<Yield>
	| 'get' PropertyName<Yield> '(' ')' '{' FunctionBody '}'
	| 'set' PropertyName<Yield> '(' PropertySetParameterList ')' '{' FunctionBody '}'
;

PropertySetParameterList ::=
	  FormalParameter ;

GeneratorMethod<Yield> ::=
	  '*' PropertyName<Yield> '('StrictFormalParameters<+Yield> ')' '{' GeneratorBody '}' ;

GeneratorDeclaration<Yield, Default> ::=
	  'function' '*' BindingIdentifier<Yield> '(' FormalParameters<+Yield> ')' '{' GeneratorBody '}'
	| [Default] 'function' '*' '(' FormalParameters<+Yield> ')' '{' GeneratorBody '}'
;

GeneratorExpression ::=
	  'function' '*' BindingIdentifier<+Yield>? '(' FormalParameters<+Yield> ')' '{' GeneratorBody '}' ;

GeneratorBody ::=
	  FunctionBody<+Yield> ;

YieldExpression<In> ::=
	  'yield'
	| 'yield' /* no LineTerminator */ AssignmentExpression<In,+Yield>
	| 'yield' /* no LineTerminator */ '*' AssignmentExpression<In,+Yield>
;

ClassDeclaration<Yield, Default> ::=
	  'class' BindingIdentifier<Yield> ClassTail<Yield>
	| [Default] 'class' ClassTail<Yield>
;

ClassExpression<Yield> ::=
	  'class' BindingIdentifier<Yield>? ClassTail<Yield> ;

ClassTail<Yield> ::=
	  ClassHeritage? '{' ClassBodyopt<Yield> '}' ;

ClassHeritage<Yield> ::=
	  'extends' LeftHandSideExpression<Yield> ;

ClassBody<Yield> ::=
	  ClassElementList<Yield> ;

ClassElementList<Yield> ::=
	  ClassElement<Yield>
	| ClassElementList<Yield> ClassElement<Yield>
;

ClassElement<Yield> ::=
	  MethodDefinition<Yield>
	| 'static' MethodDefinition<Yield>
	| ';'
;

# A.5 Scripts and Modules

Script ::=
	  ScriptBodyopt ;

ScriptBody ::=
	  StatementList ;

Module ::=
	  ModuleBodyopt ;

ModuleBody ::=
	  ModuleItemList ;

ModuleItemList ::=
	  ModuleItem
	| ModuleItemList ModuleItem
;

ModuleItem ::=
	  ImportDeclaration
	| ExportDeclaration
	| StatementListItem
;

ImportDeclaration ::=
	  'import' ImportClause FromClause ';'
	| 'import' ModuleSpecifier ';'
;

ImportClause ::=
	  ImportedDefaultBinding
	| NameSpaceImport
	| NamedImports
	| ImportedDefaultBinding ',' NameSpaceImport
	| ImportedDefaultBinding ',' NamedImports
;

ImportedDefaultBinding ::=
	  ImportedBinding ;

NameSpaceImport ::=
	  '*' 'as' ImportedBinding ;

NamedImports ::=
	  '{' '}'
	| '{' ImportsList '}'
	| '{' ImportsList ',' '}'
;

FromClause ::=
	  'from' ModuleSpecifier ;

ImportsList ::=
	  ImportSpecifier
	| ImportsList ',' ImportSpecifier
;

ImportSpecifier ::=
	  ImportedBinding
	| IdentifierName 'as' ImportedBinding
;

ModuleSpecifier ::=
	  StringLiteral ;

ImportedBinding ::=
	  BindingIdentifier ;

ExportDeclaration ::=
	  'export' '*' FromClause ';'
	| 'export' ExportClause FromClause ';'
	| 'export' ExportClause ';'
	| 'export' VariableStatement
	| 'export' Declaration
	| 'export' 'default' HoistableDeclaration<+Default>
	| 'export' 'default' ClassDeclaration<+Default>
# TODO: -- FunctionExpression, GeneratorExpression, ClassExpression
	| 'export' 'default' lookahead1 /* lookahead != { function, class } */ AssignmentExpression<+In> ';'
;

ExportClause ::=
	  '{' '}'
	| '{' ExportsList '}'
	| '{' ExportsList ',' '}'
;

ExportsList ::=
	  ExportSpecifier
	| ExportsList ',' ExportSpecifier
;

ExportSpecifier ::=
	  IdentifierName
	| IdentifierName 'as' IdentifierName
;

