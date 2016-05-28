language js(go);

lang = "js"
package = "github.com/inspirer/textmapper/tm-go/parsers/json"
eventBased = true

:: lexer

[initial, div, template, template_div]

# Accept end-of input in all states.
eoi: /{eoi}/

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

# Keywords.
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

# Literals.
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

# Punctuation
'{': /\{/
'}':          /* See below */
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
'/':          /* See below */
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
'/=':         /* See below */
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

%lookahead flag NoLet = false;
%lookahead flag NoLetSq = false;
%lookahead flag NoObjLiteral = false;
%lookahead flag NoFuncClass = false;
%lookahead flag StartWithLet = false;

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

IdentifierReference<Yield> ::=
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

@noast
PrimaryExpression<Yield> ::=
	  'this'                                                         {~ThisExpression}
	| IdentifierReference
	| Literal
	| ArrayLiteral
	| [!NoObjLiteral] ObjectLiteral
	| [!NoFuncClass] FunctionExpression
	| [!NoFuncClass] ClassExpression
	| [!NoFuncClass] GeneratorExpression
	| RegularExpressionLiteral                                    {~RegularExpression}
	| TemplateLiteral
	| CoverParenthesizedExpressionAndArrowParameterList     {~ParenthesizedExpression}
;

@noast
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
	| CoverInitializedName										{~SyntaxError}
	| PropertyName ':' AssignmentExpression<+In>
	| @noast MethodDefinition
;

@noast
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

@noast
TemplateSpans<Yield> ::=
	  TemplateTail
	| TemplateMiddleList TemplateTail
;

@noast
TemplateMiddleList<Yield> ::=
	  TemplateMiddle Expression<+In>
	| TemplateMiddleList TemplateMiddle Expression<+In>
;

@noast
MemberExpression<Yield, flag NoLetOnly = false> ::=
	  [!NoLetOnly && !StartWithLet] PrimaryExpression
	| [NoLetOnly && !StartWithLet] PrimaryExpression<+NoLet>
	| [StartWithLet && !NoLetOnly] 'let'                                     {~IdentifierReference}
	| [StartWithLet] MemberExpression<+NoLetOnly> '[' Expression<+In> ']'            {~IndexAccess}
	| [!StartWithLet] MemberExpression<NoLetOnly: NoLetSq> '[' Expression<+In> ']'   {~IndexAccess}
	| MemberExpression '.' IdentifierName                                         {~PropertyAccess}
	| MemberExpression TemplateLiteral                                            {~TaggedTemplate}
	| [!StartWithLet] SuperProperty
	| [!StartWithLet] MetaProperty
	| [!StartWithLet] 'new' MemberExpression Arguments                             {~NewExpression}
;

SuperExpression ::=
	  'super'
;

SuperProperty<Yield> ::=
	  SuperExpression '[' Expression<+In> ']'           {~IndexAccess}
	| SuperExpression '.' IdentifierName                {~PropertyAccess}
;

@noast
MetaProperty ::=
	  NewTarget
;

NewTarget ::=
	  'new' '.' 'target'
;

NewExpression<Yield> ::=
	  @noast MemberExpression
	| [!StartWithLet] 'new' NewExpression
;

CallExpression<Yield> ::=
	  MemberExpression Arguments
	| [!StartWithLet] SuperCall
	| CallExpression Arguments
	| CallExpression '[' Expression<+In> ']'      {~IndexAccess}
	| CallExpression '.' IdentifierName           {~PropertyAccess}
	| CallExpression TemplateLiteral              {~TaggedTemplate}
;

@noast
SuperCall<Yield> ::=
	  SuperExpression Arguments
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

@noast
LeftHandSideExpression<Yield> ::=
	  NewExpression
	| CallExpression
;

PostfixExpression<Yield> ::=
	  @noast LeftHandSideExpression
	| LeftHandSideExpression .noLineBreak '++'
	| LeftHandSideExpression .noLineBreak '--'
;

UnaryExpression<Yield> ::=
	  @noast PostfixExpression
	| [!StartWithLet] 'delete' UnaryExpression
	| [!StartWithLet] 'void' UnaryExpression
	| [!StartWithLet] 'typeof' UnaryExpression
	| [!StartWithLet] '++' UnaryExpression
	| [!StartWithLet] '--' UnaryExpression
	| [!StartWithLet] '+' UnaryExpression
	| [!StartWithLet] '-' UnaryExpression
	| [!StartWithLet] '~' UnaryExpression
	| [!StartWithLet] '!' UnaryExpression
;

@ast
MultiplicativeExpression<Yield> ::=
	  @noast UnaryExpression
	| MultiplicativeExpression MultiplicativeOperator UnaryExpression
;

MultiplicativeOperator ::=
	  '*' | '/' | '%' ;

@ast
AdditiveExpression<Yield> ::=
	  @noast MultiplicativeExpression
	| AdditiveExpression '+' MultiplicativeExpression
	| AdditiveExpression '-' MultiplicativeExpression
;

@ast
ShiftExpression<Yield> ::=
	  @noast AdditiveExpression
	| ShiftExpression '<<' AdditiveExpression
	| ShiftExpression '>>' AdditiveExpression
	| ShiftExpression '>>>' AdditiveExpression
;

@ast
RelationalExpression<In, Yield> ::=
	  @noast ShiftExpression
	| RelationalExpression '<' ShiftExpression
	| RelationalExpression '>' ShiftExpression
	| RelationalExpression '<=' ShiftExpression
	| RelationalExpression '>=' ShiftExpression
	| RelationalExpression 'instanceof' ShiftExpression
	| [In] RelationalExpression<+In> 'in' ShiftExpression
;

@ast
EqualityExpression<In, Yield> ::=
	  @noast RelationalExpression
	| EqualityExpression '==' RelationalExpression
	| EqualityExpression '!=' RelationalExpression
	| EqualityExpression '===' RelationalExpression
	| EqualityExpression '!==' RelationalExpression
;

@ast
BitwiseANDExpression<In, Yield> ::=
	  @noast EqualityExpression
	| BitwiseANDExpression '&' EqualityExpression
;

@ast
BitwiseXORExpression<In, Yield> ::=
	  @noast BitwiseANDExpression
	| BitwiseXORExpression '^' BitwiseANDExpression
;

@ast
BitwiseORExpression<In, Yield> ::=
	  @noast BitwiseXORExpression
	| BitwiseORExpression '|' BitwiseXORExpression
;

@ast
LogicalANDExpression<In, Yield> ::=
	  @noast BitwiseORExpression
	| LogicalANDExpression '&&' BitwiseORExpression
;

@ast
LogicalORExpression<In, Yield> ::=
	  @noast LogicalANDExpression
	| LogicalORExpression '||' LogicalANDExpression
;

ConditionalExpression<In, Yield> ::=
	  @noast LogicalORExpression
	| LogicalORExpression '?' AssignmentExpression<+In> ':' AssignmentExpression
;

AssignmentExpression<In, Yield> ::=
	  @noast ConditionalExpression
	| [Yield && !StartWithLet] @noast YieldExpression
	| [!StartWithLet] @noast ArrowFunction
	| LeftHandSideExpression '=' AssignmentExpression
	| LeftHandSideExpression AssignmentOperator AssignmentExpression
;

AssignmentOperator ::=
	  '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|=' ;

@noast
Expression<In, Yield> ::=
	  AssignmentExpression
	| Expression ',' AssignmentExpression
;

# A.3 Statements

@noast
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

@noast
Declaration<Yield> ::=
	  HoistableDeclaration<~Default>
	| ClassDeclaration<~Default>
	| LexicalDeclaration<+In>
;

@noast
HoistableDeclaration<Yield, Default> ::=
	  FunctionDeclaration
	| GeneratorDeclaration
;

@noast
BreakableStatement<Yield, Return> ::=
	  IterationStatement
	| SwitchStatement
;

@noast
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

@noast
StatementListItem<Yield, Return> ::=
	  Statement
	| Declaration
;

LexicalDeclaration<In, Yield> ::=
	  LetOrConst BindingList ';'
;

@noast
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

@noast
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
	  ';' .emptyStatement ;

ExpressionStatement<Yield> ::=
	Expression<+In, +NoFuncClass, +NoObjLiteral, +NoLetSq> ';' ;

%right 'else';

IfStatement<Yield, Return> ::=
	  'if' '(' Expression<+In> ')' Statement 'else' Statement
	| 'if' '(' Expression<+In> ')' Statement %prec 'else'
;

IterationStatement<Yield, Return> ::=
	  'do' Statement 'while' '(' Expression<+In> ')' ';' .doWhile                                {~DoWhileStatement}
	| 'while' '(' Expression<+In> ')' Statement                                                  {~WhileStatement}
	| 'for' '(' Expressionopt<~In,+NoLet> ';' .forSC Expressionopt<+In> ';' .forSC Expressionopt<+In> ')' Statement           {~ForStatement}
	| 'for' '(' Expression<~In,+StartWithLet> ';' .forSC Expressionopt<+In> ';' .forSC Expressionopt<+In> ')' Statement       {~ForStatement}
	| 'for' '(' 'var' VariableDeclarationList<~In> ';' .forSC Expressionopt<+In> ';' .forSC Expressionopt<+In> ')' Statement  {~ForStatement}
	| 'for' '(' LetOrConst BindingList<~In> ';' .forSC Expressionopt<+In> ';' .forSC Expressionopt<+In> ')' Statement                {~ForStatement}
	| 'for' '(' LeftHandSideExpression<+NoLet> 'in' Expression<+In> ')' Statement                {~ForInStatement}
	| 'for' '(' LeftHandSideExpression<+StartWithLet> 'in' Expression<+In> ')' Statement         {~ForInStatement}
	| 'for' '(' 'var' ForBinding 'in' Expression<+In> ')' Statement                              {~ForInStatement}
	| 'for' '(' ForDeclaration 'in' Expression<+In> ')' Statement                                {~ForInStatement}
	| 'for' '(' LeftHandSideExpression<+NoLet> 'of' AssignmentExpression<+In> ')' Statement      {~ForOfStatement}
	| 'for' '(' 'var' ForBinding 'of' AssignmentExpression<+In> ')' Statement                    {~ForOfStatement}
	| 'for' '(' ForDeclaration 'of' AssignmentExpression<+In> ')' Statement                      {~ForOfStatement}
;

@noast
ForDeclaration<Yield> ::=
	  LetOrConst ForBinding
;

ForBinding<Yield> ::=
	  BindingIdentifier
	| BindingPattern
;

ContinueStatement<Yield> ::=
	  'continue' ';'
	| 'continue' .noLineBreak LabelIdentifier ';'
;

BreakStatement<Yield> ::=
	  'break' ';'
	| 'break' .noLineBreak LabelIdentifier ';'
;

ReturnStatement<Yield> ::=
	  'return' ';'
	| 'return' .noLineBreak Expression<+In> ';'
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

@noast
LabelledItem<Yield, Return> ::=
	  Statement
	| FunctionDeclaration<~Default>
;

ThrowStatement<Yield> ::=
	  'throw' .noLineBreak Expression<+In> ';'
;

TryStatement<Yield, Return> ::=
	  'try' Block Catch
	| 'try' Block Catch? Finally
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

@noast
StrictFormalParameters<Yield> ::=
	  FormalParameters ;

@ast
FormalParameters<Yield> ::=
      FormalParameterList? ;

@noast
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
	  ArrowParameters .noLineBreak '=>' ConciseBody ;

ArrowParameters<Yield> ::=
	  BindingIdentifier
	| CoverParenthesizedExpressionAndArrowParameterList
;

ConciseBody<In> ::=
	  AssignmentExpression<~Yield, +NoObjLiteral>
	| '{' FunctionBody<~Yield> '}'
;

MethodDefinition<Yield> ::=
	  PropertyName '(' StrictFormalParameters ')' '{' FunctionBody '}'
	| @noast GeneratorMethod
	| 'get' PropertyName '(' ')' '{' FunctionBody '}'
	| 'set' PropertyName '(' PropertySetParameterList ')' '{' FunctionBody '}'
;

@noast
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

@noast
GeneratorBody ::=
	  FunctionBody<+Yield> ;

YieldExpression<In> ::=
	  'yield'
	| 'yield' .afterYield .noLineBreak AssignmentExpression<+Yield>
	| 'yield' .afterYield .noLineBreak '*' AssignmentExpression<+Yield>
;

ClassDeclaration<Yield, Default> ::=
	  'class' BindingIdentifier ClassTail
	| [Default] 'class' ClassTail
;

ClassExpression<Yield> ::=
	  'class' BindingIdentifier? ClassTail ;

@noast
ClassTail<Yield> ::=
	  ClassHeritage? ClassBody ;

ClassHeritage<Yield> ::=
	  'extends' LeftHandSideExpression ;

ClassBody<Yield> ::=
	  '{' ClassElementList? '}' ;

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

Module ::=
	  ModuleBodyopt ;

@noast
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

@noast
ImportClause ::=
	  ImportedDefaultBinding
	| NameSpaceImport
	| NamedImports
	| ImportedDefaultBinding ',' NameSpaceImport
	| ImportedDefaultBinding ',' NamedImports
;

@noast
ImportedDefaultBinding ::=
	  ImportedBinding ;

NameSpaceImport ::=
	  '*' 'as' ImportedBinding ;

NamedImports ::=
	  '{' '}'
	| '{' ImportsList '}'
	| '{' ImportsList ',' '}'
;

@noast
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

@noast
ImportedBinding ::=
	  BindingIdentifier<~Yield> ;

ExportDeclaration ::=
	  'export' '*' FromClause ';'
	| 'export' ExportClause FromClause ';'
	| 'export' ExportClause ';'
	| 'export' VariableStatement<~Yield>
	| 'export' Declaration<~Yield>
	| 'export' 'default' HoistableDeclaration<+Default,~Yield>              {~ExportDefault}
	| 'export' 'default' ClassDeclaration<+Default,~Yield>                  {~ExportDefault}
	| 'export' 'default' AssignmentExpression<+In,~Yield,+NoFuncClass> ';'  {~ExportDefault}
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

%%

${template go_lexer.onBeforeNext-}
	prevLine := l.tokenLine
${end}

${template go_lexer.onAfterNext}
	// There is an ambiguity in the language that a slash can either represent
	// a division operator, or start a regular expression literal. This gets
	// disambiguated at the grammar level - division always follows an
	// expression, while regex literals are expressions themselves. Here we use
	// some knowledge about the grammar to decide whether the next token can be
	// a regular expression literal.
	//
	// See the following thread for more details:
	// http://stackoverflow.com/questions/5519596/when-parsing-javascript-what

	inTemplate := l.State >= State_template
	var reContext bool
	switch token {
	case NEW, DELETE, VOID, TYPEOF, INSTANCEOF, IN, DO, RETURN, CASE, THROW, ELSE:
		reContext = true
	case TEMPLATEHEAD, TEMPLATEMIDDLE:
		reContext = true
		inTemplate = true
	case TEMPLATETAIL:
		reContext = false
		inTemplate = false
	case RPAREN, RBRACK:
		// TODO support if (...) /aaaa/;
		reContext = false
	case PLUSPLUS, MINUSMINUS:
		if prevLine != l.tokenLine {
			// This is a pre-increment/decrement, so we expect a regular expression.
			reContext = true
		} else {
			// If we were in reContext before this token, this is a
			// pre-increment/decrement, otherwise, this is a post. We can just
			// propagate the previous value of reContext.
			reContext = l.State == State_template || l.State == State_initial
		}
	default:
		reContext = token >= punctuationStart && token < punctuationEnd
	}
	if inTemplate {
		if reContext {
			l.State = State_template
		} else {
			l.State = State_template_div
		}
	} else if reContext {
		l.State = State_initial
	} else {
		l.State = State_div
	}
${end}
