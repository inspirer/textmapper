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

'null': /null/
'true': /true/
'false': /false/

# Soft (contextual) keywords.
'as':		/as/
'from':		/from/
'get':		/get/
'let':		/let/
'of':		/of/
'set':		/set/
'static':	/static/
'target':	/target/

# In strict mode:
#'implements': /implements/
#'interface': /interface/
#'package': /package/
#'private': /private/
#'protected': /protected/
#'public': /public/

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

%flag In;
%flag Yield;
%flag Default;
%flag Return;

%flag NoLet = false;
%flag NoLetSq = false;


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

	# Soft keywords
	| 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target'
;

# A.2 Expressions

IdentifierReference<Yield, NoLet> ::=
	  Identifier
	| [!Yield] 'yield'
	| [!NoLet] 'let'

	# Soft keywords
	| 'as' | 'from' | 'get' | 'of' | 'set' | 'static' | 'target'
;

BindingIdentifier<Yield> ::=
	  Identifier
	| [!Yield] 'yield'

	# Soft keywords
	| 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target'
;

LabelIdentifier<Yield> ::=
	  Identifier
	| [!Yield] 'yield'

	# Soft keywords
	| 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target'
;

PrimaryExpression<Yield, NoLet> ::=
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
	  '(' Expression<+In> ')'
	| '(' ')'
	| '(' '.' '.' '.' BindingIdentifier ')'
	| '(' Expression<+In> ',' '.' '.' '.' BindingIdentifier ')'
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
	| '[' ElementList ']'
	| '[' ElementList ',' Elisionopt ']'
;

ElementList<Yield> ::=
	  Elisionopt AssignmentExpression<+In>
	| Elisionopt SpreadElement
	| ElementList ',' Elisionopt AssignmentExpression<+In>
	| ElementList ',' Elisionopt SpreadElement
;

Elision ::=
	  ','
	| Elision ','
;

SpreadElement<Yield> ::=
	  '.' '.' '.' AssignmentExpression<+In>
;

ObjectLiteral<Yield> ::=
	  '{' '}'
	| '{' PropertyDefinitionList '}'
	| '{' PropertyDefinitionList ',' '}'
;

PropertyDefinitionList<Yield> ::=
	  PropertyDefinition
	| PropertyDefinitionList ',' PropertyDefinition
;

PropertyDefinition<Yield> ::=
	  IdentifierReference
	| CoverInitializedName
	| PropertyName ':' AssignmentExpression<+In>
	| MethodDefinition
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
	'[' AssignmentExpression<+In> ']'
;

CoverInitializedName<Yield> ::=
	  IdentifierReference Initializer<+In>
;

Initializer<In, Yield> ::=
	  '=' AssignmentExpression
;

TemplateLiteral<Yield> ::=
	  NoSubstitutionTemplate
	| TemplateHead Expression<+In> TemplateSpans
;

TemplateSpans<Yield> ::=
	  TemplateTail
	| TemplateMiddleList TemplateTail
;

TemplateMiddleList<Yield> ::=
	  TemplateMiddle Expression<+In>
	| TemplateMiddleList TemplateMiddle Expression<+In>
;

MemberExpression<Yield, NoLet, NoLetSq, flag NoLetOnly = false> ::=
	  [!NoLetOnly] PrimaryExpression
	| [NoLetOnly] PrimaryExpression<+NoLet>
	| MemberExpression<NoLetOnly: NoLetSq> '[' Expression<+In> ']'
	| MemberExpression '.' IdentifierName
	| MemberExpression TemplateLiteral
	| SuperProperty
	| MetaProperty
	| 'new' MemberExpression<~NoLet, ~NoLetSq> Arguments
;

SuperProperty<Yield> ::=
	  'super' '[' Expression<+In> ']'
	| 'super' '.' IdentifierName
;

MetaProperty ::=
	  NewTarget
;

NewTarget ::=
	  'new' '.' 'target'
;

NewExpression<Yield, NoLet, NoLetSq> ::=
	  MemberExpression
	| 'new' NewExpression<~NoLet, ~NoLetSq>
;

CallExpression<Yield, NoLet, NoLetSq> ::=
	  MemberExpression Arguments
	| SuperCall
	| CallExpression Arguments
	| CallExpression '[' Expression<+In> ']'
	| CallExpression '.' IdentifierName
	| CallExpression TemplateLiteral
;

SuperCall<Yield> ::=
	  'super' Arguments
;

Arguments<Yield> ::=
	  '(' ')'
	| '(' ArgumentList ')'
;

ArgumentList<Yield> ::=
	  AssignmentExpression<+In>
	| '.' '.' '.' AssignmentExpression<+In>
	| ArgumentList ',' AssignmentExpression<+In>
	| ArgumentList ',' '.' '.' '.' AssignmentExpression<+In>
;

LeftHandSideExpression<Yield> ::=
	  NewExpression<+NoLet>
	| CallExpression<+NoLet>
;

PostfixExpression<Yield> ::=
	  LeftHandSideExpression
	| LeftHandSideExpression /* no LineTerminator */ '++'
	| LeftHandSideExpression /* no LineTerminator */ '--'
;

UnaryExpression<Yield> ::=
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

MultiplicativeExpression<Yield> ::=
	  UnaryExpression
	| MultiplicativeExpression MultiplicativeOperator UnaryExpression
;

MultiplicativeOperator ::=
	  '*' | '/' | '%' ;

AdditiveExpression<Yield> ::=
	  MultiplicativeExpression
	| AdditiveExpression '+' MultiplicativeExpression
	| AdditiveExpression '-' MultiplicativeExpression
;

ShiftExpression<Yield> ::=
	  AdditiveExpression
	| ShiftExpression '<<' AdditiveExpression
	| ShiftExpression '>>' AdditiveExpression
	| ShiftExpression '>>>' AdditiveExpression
;

RelationalExpression<In, Yield> ::=
	  ShiftExpression
	| RelationalExpression '<' ShiftExpression
	| RelationalExpression '>' ShiftExpression
	| RelationalExpression '<=' ShiftExpression
	| RelationalExpression '>=' ShiftExpression
	| RelationalExpression 'instanceof' ShiftExpression
	| [In] RelationalExpression<+In> 'in' ShiftExpression
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
	| LogicalORExpression '?' AssignmentExpression<+In> ':' AssignmentExpression
;

AssignmentExpression<In, Yield> ::=
	  ConditionalExpression
	| [Yield] YieldExpression
	| ArrowFunction
	| LeftHandSideExpression '=' AssignmentExpression
	| LeftHandSideExpression AssignmentOperator AssignmentExpression
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
	| VariableStatement
	| EmptyStatement
	| ExpressionStatement
	| IfStatement
	| BreakableStatement
	| ContinueStatement
	| BreakStatement
	| [Return] ReturnStatement
	| WithStatement
	| LabelledStatement
	| ThrowStatement
	| TryStatement
	| DebuggerStatement
;

Declaration<Yield> ::=
	  HoistableDeclaration<~Default>
	| ClassDeclaration<~Default>
	| LexicalDeclaration<+In>
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
	| Declaration
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
	  BindingIdentifier Initializeropt
	| BindingPattern Initializer
;

VariableStatement<Yield> ::=
	  'var' VariableDeclarationList<+In> ';'
;

VariableDeclarationList<In, Yield> ::=
	  VariableDeclaration
	| VariableDeclarationList ',' VariableDeclaration
;

VariableDeclaration<In, Yield> ::=
	  BindingIdentifier Initializeropt
	| BindingPattern Initializer
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
	| BindingPattern Initializeropt<+In>
;

SingleNameBinding<Yield> ::=
	  BindingIdentifier Initializeropt<+In>
;

BindingRestElement<Yield> ::=
	  '.' '.' '.' BindingIdentifier
;

EmptyStatement ::=
	  ';' ;

ExpressionStatement<Yield> ::=
# TODO: -- ClassExpression, FunctionExpression, GeneratorExpression, ObjectLiteral
	lookahead1
	/* lookahead != {'{', function, class, let '[' } */ Expression<+In> ';'
;

%right 'else';

IfStatement<Yield, Return> ::=
	  'if' '(' Expression<+In> ')' Statement 'else' Statement
	| 'if' '(' Expression<+In> ')' Statement %prec 'else'
;

IterationStatement<Yield, Return> ::=
	  'do' Statement 'while' '(' Expression<+In> ')' ';'
	| 'while' '(' Expression<+In> ')' Statement
	| 'for' '(' Expressionopt<~In> ';' Expressionopt<+In> ';' Expressionopt<+In> ')' Statement
	| 'for' '(' 'var' VariableDeclarationList<~In> ';' Expressionopt<+In> ';' Expressionopt<+In> ')' Statement
	| 'for' '(' LexicalDeclaration<~In> Expressionopt<+In> ';' Expressionopt<+In> ')' Statement
	| 'for' '(' LeftHandSideExpression 'in' Expression<+In> ')' Statement
	| 'for' '(' 'var' ForBinding 'in' Expression<+In> ')' Statement
	| 'for' '(' ForDeclaration 'in' Expression<+In> ')' Statement
	| 'for' '(' LeftHandSideExpression 'of' AssignmentExpression<+In> ')' Statement
	| 'for' '(' 'var' ForBinding 'of' AssignmentExpression<+In> ')' Statement
	| 'for' '(' ForDeclaration 'of' AssignmentExpression<+In> ')' Statement
;

ForDeclaration<Yield> ::=
	  LetOrConst ForBinding
;

ForBinding<Yield> ::=
	  BindingIdentifier
	| BindingPattern
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
	| 'return' /* no LineTerminator */ Expression<+In> ';'
;

WithStatement<Yield, Return> ::=
	  'with' '(' Expression<+In> ')' Statement
;

SwitchStatement<Yield, Return> ::=
	  'switch' '(' Expression<+In> ')' CaseBlock
;

CaseBlock<Yield, Return> ::=
	  '{' CaseClausesopt '}'
	| '{' CaseClausesopt DefaultClause CaseClausesopt '}'
;

CaseClauses<Yield, Return> ::=
	  CaseClause
	| CaseClauses CaseClause
;

CaseClause<Yield, Return> ::=
	  'case' Expression<+In> ':' StatementList?
;

DefaultClause<Yield, Return> ::=
	  'default' ':' StatementList?
;

LabelledStatement<Yield, Return> ::=
	  Identifier ':' LabelledItem
	| [!Yield] 'yield' ':' LabelledItem
;

LabelledItem<Yield, Return> ::=
	  Statement
	| FunctionDeclaration<~Default>
;

ThrowStatement<Yield> ::=
	  'throw' /* no LineTerminator */ Expression<+In> ';'
;

TryStatement<Yield, Return> ::=
	  'try' Block Catch
	| 'try' Block Finally
	| 'try' Block Catch Finally
;

Catch<Yield, Return> ::=
	  'catch' '(' CatchParameter ')' Block
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
	  'function' BindingIdentifier '(' FormalParameters<~Yield> ')' '{' FunctionBody<~Yield> '}'
# TODO ~Yield?
	| [Default] 'function' '(' FormalParameters ')' '{' FunctionBody '}'
;

FunctionExpression ::=
	  'function' BindingIdentifier<~Yield>? '(' FormalParameters<~Yield> ')' '{' FunctionBody<~Yield> '}'
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
	  StatementList<+Return>? ;

ArrowFunction<In, Yield> ::=
	  ArrowParameters /* no LineTerminator */ '=>' ConciseBody ;

ArrowParameters<Yield> ::=
	  BindingIdentifier
	| CoverParenthesizedExpressionAndArrowParameterList
;

ConciseBody<In> ::=
# TODO: -- ObjectLiteral
	  lookahead1
			  /* lookahead != { */ AssignmentExpression<~Yield>
	| '{' FunctionBody<~Yield> '}'
;

MethodDefinition<Yield> ::=
	  PropertyName '(' StrictFormalParameters ')' '{' FunctionBody '}'
	| GeneratorMethod
	| 'get' PropertyName '(' ')' '{' FunctionBody '}'
	| 'set' PropertyName '(' PropertySetParameterList ')' '{' FunctionBody '}'
;

PropertySetParameterList ::=
	  FormalParameter<~Yield> ;

GeneratorMethod<Yield> ::=
	  '*' PropertyName '('StrictFormalParameters<+Yield> ')' '{' GeneratorBody '}' ;

GeneratorDeclaration<Yield, Default> ::=
	  'function' '*' BindingIdentifier '(' FormalParameters<+Yield> ')' '{' GeneratorBody '}'
	| [Default] 'function' '*' '(' FormalParameters<+Yield> ')' '{' GeneratorBody '}'
;

GeneratorExpression ::=
	  'function' '*' BindingIdentifier<+Yield>? '(' FormalParameters<+Yield> ')' '{' GeneratorBody '}' ;

GeneratorBody ::=
	  FunctionBody<+Yield> ;

YieldExpression<In> ::=
	  'yield'
	| 'yield' /* no LineTerminator */ AssignmentExpression<+Yield>
	| 'yield' /* no LineTerminator */ '*' AssignmentExpression<+Yield>
;

ClassDeclaration<Yield, Default> ::=
	  'class' BindingIdentifier ClassTail
	| [Default] 'class' ClassTail
;

ClassExpression<Yield> ::=
	  'class' BindingIdentifier? ClassTail ;

ClassTail<Yield> ::=
	  ClassHeritage? '{' ClassBodyopt '}' ;

ClassHeritage<Yield> ::=
	  'extends' LeftHandSideExpression ;

ClassBody<Yield> ::=
	  ClassElementList ;

ClassElementList<Yield> ::=
	  ClassElement
	| ClassElementList ClassElement
;

ClassElement<Yield> ::=
	  MethodDefinition
	| 'static' MethodDefinition
	| ';'
;

# A.5 Scripts and Modules

Script ::=
	  ScriptBodyopt ;

ScriptBody ::=
	  StatementList<~Yield, ~Return> ;

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
	| StatementListItem<~Yield,~Return>
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
	  BindingIdentifier<~Yield> ;

ExportDeclaration ::=
	  'export' '*' FromClause ';'
	| 'export' ExportClause FromClause ';'
	| 'export' ExportClause ';'
	| 'export' VariableStatement<~Yield>
	| 'export' Declaration<~Yield>
	| 'export' 'default' HoistableDeclaration<+Default,~Yield>
	| 'export' 'default' ClassDeclaration<+Default,~Yield>
# TODO: -- FunctionExpression, GeneratorExpression, ClassExpression
	| 'export' 'default' lookahead1 /* lookahead != { function, class } */ AssignmentExpression<+In,~Yield> ';'
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

