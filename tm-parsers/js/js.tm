# ECMAScript 2016 Language Grammar (Standard ECMA-262, 7th Edition / June 2016)
# This grammar also covers JSX - a popular language extension for React.

language js(go);

lang = "js"
package = "github.com/inspirer/textmapper/tm-parsers/js"
eventBased = true

:: lexer

[initial, div, template, templateDiv, jsxTemplate, jsxTemplateDiv, jsxTag, jsxClosingTag, jsxText]

# Accept end-of-input in all states.
eoi: /{eoi}/

[initial, div, template, templateDiv, jsxTemplate, jsxTemplateDiv, jsxTag, jsxClosingTag]

WhiteSpace: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)

[initial, div, template, templateDiv, jsxTemplate, jsxTemplateDiv]

# LineTerminatorSequence
WhiteSpace: /[\n\r\u2028\u2029]|\r\n/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment:  /\/\*{commentChars}\*\//
SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/

# Note: see http://unicode.org/reports/tr31/
ID_Start = /\p{Lu}|\p{Ll}|\p{Lt}|\p{Lm}|\p{Lo}|\p{Nl}/
ID_Continue = /{ID_Start}|\p{Mn}|\p{Mc}|\p{Nd}|\p{Pc}/
Join_Control = /\u200c|\u200d/

hex = /[0-9a-fA-F]/
unicodeEscapeSequence = /u(\{{hex}+\}|{hex}{4})/

identifierStart = /{ID_Start}|$|_|\\{unicodeEscapeSequence}/
identifierPart =  /{identifierStart}|{ID_Continue}|{Join_Control}/

Identifier: /{identifierStart}{identifierPart}*/    (class)

# Keywords.
'break':      /break/
'case':       /case/
'catch':      /catch/
'class':      /class/
'const':      /const/
'continue':   /continue/
'debugger':   /debugger/
'default':    /default/
'delete':     /delete/
'do':         /do/
'else':       /else/
'export':     /export/
'extends':    /extends/
'finally':    /finally/
'for':        /for/
'function':   /function/
'if':         /if/
'import':     /import/
'in':         /in/
'instanceof': /instanceof/
'new':        /new/
'return':     /return/
'super':      /super/
'switch':     /switch/
'this':       /this/
'throw':      /throw/
'try':        /try/
'typeof':     /typeof/
'var':        /var/
'void':       /void/
'while':      /while/
'with':       /with/
'yield':      /yield/

# Future-reserved.
'await': /await/
'enum':  /enum/

# Literals.
'null':  /null/
'true':  /true/
'false': /false/

# Soft (contextual) keywords.
'as':     /as/
'from':   /from/
'get':    /get/
'let':    /let/
'of':     /of/
'set':    /set/
'static': /static/
'target': /target/

# Strict mode keywords:
#   implements interface package private protected public

# Punctuation
'{': /\{/
'}':          /* See below */
'(': /\(/
')': /\)/
'[': /\[/
']': /\]/
'.': /\./
'...': /\.\.\./
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
'**': /\*\*/
'**=': /\*\*=/

# Numeric literals starting with zero in V8:
#   00000 == 0, 00001 = 1, 00.0 => error
#   055 == 45, 099 == 99
#   09.5 == 9.5, 059.5 == 59.5, 05.5 => error, 05.9 => error

exp = /[eE][+-]?[0-9]+/
NumericLiteral: /(0+([0-7]*[89][0-9]*)?|[1-9][0-9]*)(\.[0-9]*)?{exp}?/
NumericLiteral: /\.[0-9]+{exp}?/
NumericLiteral: /0[Xx]{hex}+/
NumericLiteral: /0[oO][0-7]+/
NumericLiteral: /0+[0-7]+/      1 # (Takes priority over the float rule above)
NumericLiteral: /0[bB][01]+/

escape = /\\([^1-9xu\n\r\u2028\u2029]|x{hex}{2}|{unicodeEscapeSequence})/
lineCont = /\\([\n\r\u2028\u2029]|\r\n)/
dsChar = /[^\n\r"\\\u2028\u2029]|{escape}|{lineCont}/
ssChar = /[^\n\r'\\\u2028\u2029]|{escape}|{lineCont}/

# TODO check \0 is valid if [lookahead != DecimalDigit]

StringLiteral: /"{dsChar}*"/
StringLiteral: /'{ssChar}*'/

tplChars = /([^\$`\\]|\$*{escape}|\$*{lineCont}|\$+[^\$\{`\\])*\$*/

[initial, div, jsxTemplate, jsxTemplateDiv]

'}': /\}/

NoSubstitutionTemplate: /`{tplChars}`/
TemplateHead: /`{tplChars}\$\{/

[template, templateDiv]

TemplateMiddle: /\}{tplChars}\$\{/
TemplateTail: /\}{tplChars}`/

[initial, template, jsxTemplate]

reBS = /\\[^\n\r\u2028\u2029]/
reClass = /\[([^\n\r\u2028\u2029\]\\]|{reBS})*\]/
reFirst = /[^\n\r\u2028\u2029\*\[\\\/]|{reBS}|{reClass}/
reChar = /{reFirst}|\*/

RegularExpressionLiteral: /\/{reFirst}{reChar}*\/{identifierPart}*/

[div, templateDiv, jsxTemplateDiv]

'/': /\//
'/=': /\/=/

[jsxTag, jsxClosingTag]

'<': /</
'>': />/
'/': /\//
'{': /\{/
':': /:/
'.': /\./
'=': /=/

jsxStringLiteral: /'[^']*'/
jsxStringLiteral: /"[^"]*"/

jsxIdentifier: /{identifierStart}({identifierPart}|-)*/

[jsxText]

'{': /\{/
'<': /</

jsxText: /[^{}<>]+/

error:
invalid_token:

:: parser

%input Module;

%assert empty set(follow error & ~('}' | ')' | ',' | ';' | ']'));

%generate afterErr = set(follow error);

%flag In;
%flag Yield;
%flag Default;
%flag Return;

%lookahead flag NoLet = false;
%lookahead flag NoLetSq = false;
%lookahead flag NoObjLiteral = false;
%lookahead flag NoFuncClass = false;
%lookahead flag StartWithLet = false;

SyntaxError ::=
    error ;

IdentifierName ::=
    Identifier

  # Keywords
  | 'break'      | 'do'         | 'in'         | 'typeof'
  | 'case'       | 'else'       | 'instanceof' | 'var'
  | 'catch'      | 'export'     | 'new'        | 'void'
  | 'class'      | 'extends'    | 'return'     | 'while'
  | 'const'      | 'finally'    | 'super'      | 'with'
  | 'continue'   | 'for'        | 'switch'     | 'yield'
  | 'debugger'   | 'function'   | 'this'
  | 'default'    | 'if'         | 'throw'
  | 'delete'     | 'import'     | 'try'

  # Future-reserved.
  | 'enum' | 'await'

  # NullLiteral | BooleanLiteral
  | 'null' | 'true' | 'false'

  # Soft keywords
  | 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target'
;

# A.2 Expressions

IdentifierReference<Yield> ::=
# V8 runtime functions start with a percent sign.
# See http://stackoverflow.com/questions/11202824/what-is-in-javascript
    '%'? Identifier
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
    'this'                                                 {~This}
  | IdentifierReference
  | Literal
  | ArrayLiteral
  | [!NoObjLiteral] ObjectLiteral
  | [!NoFuncClass] FunctionExpression
  | [!NoFuncClass] ClassExpression
  | [!NoFuncClass] GeneratorExpression
  | RegularExpressionLiteral                               {~Regexp}
  | TemplateLiteral
  | CoverParenthesizedExpressionAndArrowParameterList      {~Parenthesized}
  | JSXElement
;

@noast
CoverParenthesizedExpressionAndArrowParameterList<Yield> ::=
    '(' Expression<+In> ')'
  | '(' ')'
  | '(' '...' BindingIdentifier ')'
  | '(' '...' BindingPattern ')'
  | '(' Expression<+In> ',' '...' BindingIdentifier ')'
  | '(' Expression<+In> ',' '...' BindingPattern ')'
  | '(' SyntaxError ')'
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

@noast
ElementList<Yield> ::=
    Elisionopt AssignmentExpression<+In>
  | Elisionopt SpreadElement
  | ElementList ',' Elisionopt AssignmentExpression<+In>
  | ElementList ',' Elisionopt SpreadElement
;

@noast
Elision ::=
    ','
  | Elision ','
;

SpreadElement<Yield> ::=
    '...' AssignmentExpression<+In>
;

ObjectLiteral<Yield> ::=
    '{' '}'
  | '{' PropertyDefinitionList '}'
  | '{' PropertyDefinitionList ',' '}'
;

@noast
PropertyDefinitionList<Yield> ::=
    PropertyDefinition
  | PropertyDefinitionList ',' PropertyDefinition
;

PropertyDefinition<Yield> ::=
    IdentifierReference                                   {~Property}
  | PropertyName ':' AssignmentExpression<+In>            {~Property}
  | @noast MethodDefinition
  | CoverInitializedName                                  {~SyntaxError}
  | @noast SyntaxError
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
    '[' AssignmentExpression<+In> ']' ;

@noast
CoverInitializedName<Yield> ::=
    IdentifierReference Initializer<+In> ;

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
  | [StartWithLet && !NoLetOnly] 'let'                    {~IdentifierReference}
  | [StartWithLet] MemberExpression<+NoLetOnly> '[' Expression<+In> ']'            {~IndexAccess}
  | [!StartWithLet] MemberExpression<NoLetOnly: NoLetSq> '[' Expression<+In> ']'   {~IndexAccess}
  | MemberExpression '.' IdentifierName                   {~PropertyAccess}
  | MemberExpression TemplateLiteral                      {~TaggedTemplate}
  | [!StartWithLet] SuperProperty
  | [!StartWithLet] MetaProperty
  | [!StartWithLet] 'new' MemberExpression Arguments      {~NewExpression}
;

SuperExpression ::=
    'super'
;

SuperProperty<Yield> ::=
    SuperExpression '[' Expression<+In> ']'               {~IndexAccess}
  | SuperExpression '.' IdentifierName                    {~PropertyAccess}
;

@noast
MetaProperty ::=
    NewTarget ;

NewTarget ::=
    'new' '.' 'target' ;

NewExpression<Yield> ::=
    @noast MemberExpression
  | [!StartWithLet] 'new' NewExpression
;

CallExpression<Yield> ::=
    MemberExpression Arguments
  | [!StartWithLet] SuperCall
  | CallExpression Arguments
  | CallExpression '[' Expression<+In> ']'                {~IndexAccess}
  | CallExpression '.' IdentifierName                     {~PropertyAccess}
  | CallExpression TemplateLiteral                        {~TaggedTemplate}
;

@noast
SuperCall<Yield> ::=
    SuperExpression Arguments
;

Arguments<Yield> ::=
    '(' ')'
  | '(' ArgumentList ')'
;

@noast
ArgumentList<Yield> ::=
    AssignmentExpression<+In>
  | SpreadElement
  | ArgumentList ',' AssignmentExpression<+In>
  | ArgumentList ',' SpreadElement
;

@noast
LeftHandSideExpression<Yield> ::=
    NewExpression
  | CallExpression
;

UpdateExpression<Yield> ::=
    @noast LeftHandSideExpression
  | LeftHandSideExpression .noLineBreak '++'             {~PostInc}
  | LeftHandSideExpression .noLineBreak '--'             {~PostDec}
  | [!StartWithLet] '++' UnaryExpression                 {~PreInc}
  | [!StartWithLet] '--' UnaryExpression                 {~PreDec}
;

UnaryExpression<Yield> ::=
    @noast UpdateExpression
  | [!StartWithLet] 'delete' UnaryExpression
  | [!StartWithLet] 'void' UnaryExpression
  | [!StartWithLet] 'typeof' UnaryExpression
  | [!StartWithLet] '+' UnaryExpression
  | [!StartWithLet] '-' UnaryExpression
  | [!StartWithLet] '~' UnaryExpression
  | [!StartWithLet] '!' UnaryExpression
;

%left '||';
%left '&&';
%left '|';
%left '^';
%left '&';
%left '==' '!=' '===' '!==';
%left '<' '>' '<=' '>=' 'instanceof' 'in';
%left '<<' '>>' '>>>';
%left '-' '+';
%left '*' '/' '%';
%right '**';

ArithmeticExpression<Yield> ::=
    @noast UnaryExpression
  | ArithmeticExpression '+' ArithmeticExpression        {~AdditiveExpression}
  | ArithmeticExpression '-' ArithmeticExpression        {~AdditiveExpression}
  | ArithmeticExpression '<<' ArithmeticExpression       {~ShiftExpression}
  | ArithmeticExpression '>>' ArithmeticExpression       {~ShiftExpression}
  | ArithmeticExpression '>>>' ArithmeticExpression      {~ShiftExpression}
  | ArithmeticExpression '*' ArithmeticExpression        {~MultiplicativeExpression}
  | ArithmeticExpression '/' ArithmeticExpression        {~MultiplicativeExpression}
  | ArithmeticExpression '%' ArithmeticExpression        {~MultiplicativeExpression}
  | UpdateExpression '**' ArithmeticExpression           {~ExponentiationExpression}
;

BinaryExpression<In, Yield> ::=
    @noast ArithmeticExpression
  | BinaryExpression '<' BinaryExpression                {~RelationalExpression}
  | BinaryExpression '>' BinaryExpression                {~RelationalExpression}
  | BinaryExpression '<=' BinaryExpression               {~RelationalExpression}
  | BinaryExpression '>=' BinaryExpression               {~RelationalExpression}
  | BinaryExpression 'instanceof' BinaryExpression       {~RelationalExpression}
  | [In] BinaryExpression 'in' BinaryExpression          {~RelationalExpression}
  | BinaryExpression '==' BinaryExpression               {~EqualityExpression}
  | BinaryExpression '!=' BinaryExpression               {~EqualityExpression}
  | BinaryExpression '===' BinaryExpression              {~EqualityExpression}
  | BinaryExpression '!==' BinaryExpression              {~EqualityExpression}
  | BinaryExpression '&' BinaryExpression                {~BitwiseANDExpression}
  | BinaryExpression '^' BinaryExpression                {~BitwiseXORExpression}
  | BinaryExpression '|' BinaryExpression                {~BitwiseORExpression}
  | BinaryExpression '&&' BinaryExpression               {~LogicalANDExpression}
  | BinaryExpression '||' BinaryExpression               {~LogicalORExpression}
;

ConditionalExpression<In, Yield> ::=
    @noast BinaryExpression
  | BinaryExpression '?' AssignmentExpression<+In> ':' AssignmentExpression
;

AssignmentExpression<In, Yield> ::=
    @noast ConditionalExpression
  | [Yield && !StartWithLet] @noast YieldExpression
  | [!StartWithLet] @noast ArrowFunction
  | LeftHandSideExpression '=' AssignmentExpression
  | LeftHandSideExpression AssignmentOperator AssignmentExpression
;

AssignmentOperator ::=
    '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|=' | '**=' ;

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
    Block ;

Block<Yield, Return> ::=
    '{' StatementList? '}' ;

@noast
StatementList<Yield, Return> ::=
    StatementListItem
  | StatementList StatementListItem
;

@noast
StatementListItem<Yield, Return> ::=
    Statement
  | Declaration
  | error ';'                       {~SyntaxError}
;

LexicalDeclaration<In, Yield> ::=
    LetOrConst BindingList ';' ;

@noast
LetOrConst ::=
    'let'
  | 'const'
;

@noast
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

@noast
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
    '{' '}'                                               {~ObjectPattern}
  | '{' BindingPropertyList ','? '}'                      {~ObjectPattern}
;

ArrayBindingPattern<Yield> ::=
    '[' Elisionopt BindingRestElementopt ']'
  | '[' BindingElementList ']'
  | '[' BindingElementList ',' Elisionopt BindingRestElementopt ']'
;

@noast
BindingPropertyList<Yield> ::=
    BindingProperty
  | BindingPropertyList ',' BindingProperty
;

@noast
BindingElementList<Yield> ::=
    BindingElisionElement
  | BindingElementList ',' BindingElisionElement
;

@noast
BindingElisionElement<Yield> ::=
    Elision? BindingElement
;

BindingProperty<Yield> ::=
    @noast SingleNameBinding
  | PropertyName ':' BindingElement
  | @noast SyntaxError
;

BindingElement<Yield> ::=
    @noast SingleNameBinding
  | BindingPattern Initializeropt<+In>
  | @noast SyntaxError
;

SingleNameBinding<Yield> ::=
    BindingIdentifier Initializeropt<+In>
;

BindingRestElement<Yield> ::=
    '...' BindingIdentifier
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
    '{' CaseClausesopt '}'                                   {~Block}
  | '{' CaseClausesopt DefaultClause CaseClausesopt '}'      {~Block}
;

@noast
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
    'function' BindingIdentifier FormalParameters<~Yield> FunctionBody<~Yield>  {~Function}
# TODO ~Yield?
  | [Default] 'function' FormalParameters FunctionBody                          {~Function}
;

FunctionExpression ::=
    'function' BindingIdentifier<~Yield>? FormalParameters<~Yield>
        FunctionBody<~Yield> ;

@noast
StrictFormalParameters<Yield> ::=
    FormalParameters ;

FormalParameters<Yield> ::=
      '(' FormalParameterList? ')'                        {~Parameters}
;

@noast
FormalParameterList<Yield> ::=
    FunctionRestParameter
  | FormalsList
  | FormalsList ',' FunctionRestParameter
;

@noast
FormalsList<Yield> ::=
    FormalParameter
  | FormalsList ',' FormalParameter
;

FunctionRestParameter<Yield> ::=
    BindingRestElement                                    {~RestParameter}
;

FormalParameter<Yield> ::=
    BindingElement                                        {~Parameter}
;

FunctionBody<Yield> ::=
    '{' StatementList<+Return>? '}'                       {~Body}
;

ArrowFunction<In, Yield> ::=
    ArrowParameters .noLineBreak '=>' ConciseBody ;

ArrowParameters<Yield> ::=
    BindingIdentifier                                     {~Parameters}
  | CoverParenthesizedExpressionAndArrowParameterList     {~Parameters}
;

ConciseBody<In> ::=
    AssignmentExpression<~Yield, +NoObjLiteral>
  | @noast FunctionBody<~Yield>
;

MethodDefinition<Yield> ::=
    PropertyName StrictFormalParameters FunctionBody
  | @noast GeneratorMethod
  | 'get' PropertyName '(' ')' FunctionBody                               {~PropertyGetter}
  | 'set' PropertyName '(' PropertySetParameterList ')' FunctionBody      {~PropertySetter}
;

@noast
PropertySetParameterList ::=
    FormalParameter<~Yield> ;

GeneratorMethod<Yield> ::=
    '*' PropertyName StrictFormalParameters<+Yield> GeneratorBody ;

GeneratorDeclaration<Yield, Default> ::=
    'function' '*' BindingIdentifier FormalParameters<+Yield> GeneratorBody
  | [Default] 'function' '*' FormalParameters<+Yield> GeneratorBody
;

GeneratorExpression ::=
    'function' '*' BindingIdentifier<+Yield>? FormalParameters<+Yield> GeneratorBody ;

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

@noast
ClassElementList<Yield> ::=
    ClassElement
  | ClassElementList ClassElement
;

ClassElement<Yield> ::=
    MethodDefinition
  | 'static' MethodDefinition                                 {~StaticClassElement}
  | ';'
;

# A.5 Scripts and Modules

Module ::=
    ModuleBodyopt ;

@noast
ModuleBody ::=
    ModuleItemList ;

@noast
ModuleItemList ::=
    ModuleItem
  | ModuleItemList ModuleItem
;

@noast
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

@noast
ImportsList ::=
    ImportSpecifier
  | ImportsList ',' ImportSpecifier
;

ImportSpecifier ::=
    ImportedBinding
  | IdentifierName 'as' ImportedBinding
  | error                                                 {~SyntaxError}
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

@noast
ExportsList ::=
    ExportSpecifier
  | ExportsList ',' ExportSpecifier
;

ExportSpecifier ::=
    IdentifierName
  | IdentifierName 'as' IdentifierName
  | error                                                 {~SyntaxError}
;

# Extensions

# JSX (see https://facebook.github.io/jsx/)

JSXElement<Yield> ::=
    JSXSelfClosingElement
  | JSXOpeningElement JSXChild* JSXClosingElement
;

JSXSelfClosingElement<Yield> ::=
    '<' JSXElementName JSXAttribute* '/' '>' ;

JSXOpeningElement<Yield> ::=
    '<' JSXElementName JSXAttribute* '>' ;

JSXClosingElement ::=
    '<' '/' JSXElementName '>' ;

JSXElementName ::=
    jsxIdentifier
  | jsxIdentifier ':' jsxIdentifier
  | JSXMemberExpression
;

@noast
JSXMemberExpression ::=
    jsxIdentifier '.' jsxIdentifier
  | JSXMemberExpression '.' jsxIdentifier
;

JSXAttribute<Yield> ::=
    JSXAttributeName '=' JSXAttributeValue
  | '{' '...' AssignmentExpression<+In> '}'             {~JSXSpreadAttribute}
;

JSXAttributeName ::=
    jsxIdentifier
  | jsxIdentifier ':' jsxIdentifier
;

JSXAttributeValue<Yield> ::=
    jsxStringLiteral
  | '{' AssignmentExpression<+In> '}'
  | JSXElement
;

JSXChild<Yield> ::=
    jsxText                                                   {~JSXText}
  | JSXElement
  | '{' AssignmentExpressionopt<+In> '}'
;

%%

${template go_lexer.stateVars}
	token  Token // last token
	Stack  []int // stack of JSX states, non-empty for StateJsx*
	Opened []int // number of opened curly braces per jsxTemplate* state
${end}

${template go_lexer.initStateVars-}
	l.token = UNAVAILABLE
	l.Stack = nil
	l.Opened = nil
${end}

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

	if l.State <= StateJsxTemplateDiv {
		// The lowest bit of "l.State" determines how to interpret a forward
		// slash if it happens to be the next character.
		//   unset: start of a regular expression literal
		//   set:   start of a division operator (/ or /=)
		switch token {
		case NEW, DELETE, VOID, TYPEOF, INSTANCEOF, IN, DO, RETURN, CASE, THROW, ELSE:
			l.State &^= 1
		case TEMPLATEHEAD, TEMPLATEMIDDLE:
			l.State = StateTemplate
		case TEMPLATETAIL:
			l.State = StateDiv
		case RPAREN, RBRACK:
			// TODO support if (...) /aaaa/;
			l.State |= 1
		case PLUSPLUS, MINUSMINUS:
			if prevLine != l.tokenLine {
				// This is a pre-increment/decrement, so we expect a regular expression.
				l.State &^= 1
			}
			// Otherwise: if we were expecting a regular expression literal before this
			// token, this is a pre-increment/decrement, otherwise, this is a post. We
			// can just propagate the previous value of the lowest bit of the state.
		case LT:
			if l.State&1 == 0 {
				// Start a new JSX tag.
				l.Stack = append(l.Stack, l.State|1)
				l.State = StateJsxTag
			} else {
				l.State &^= 1
			}
		case LBRACE:
			if l.State >= StateJsxTemplate {
				l.Opened[len(l.Opened)-1]++
			}
			l.State &^= 1
		case RBRACE:
			if l.State >= StateJsxTemplate {
				last := len(l.Opened) - 1
				l.Opened[last]--
				if l.Opened[last] == 0 {
					l.Opened = l.Opened[:last]
					l.State = l.Stack[len(l.Stack) - 1]
					l.Stack = l.Stack[:len(l.Stack) - 1]
					break
				}
			}
			l.State &^= 1
		case SINGLELINECOMMENT, MULTILINECOMMENT:
			break
		default:
			if token >= punctuationStart && token < punctuationEnd {
				l.State &^= 1
			} else {
				l.State |= 1
			}
		}
	} else {
		// Handling JSX states.
		switch token {
		case DIV:
			if l.State == StateJsxTag && l.token == LT && l.Stack[len(l.Stack)-1] == StateJsxText {
				l.State = StateJsxClosingTag
				l.Stack = l.Stack[:len(l.Stack) - 1]
			}
		case GT:
			if l.State == StateJsxClosingTag || l.token == DIV {
				l.State = l.Stack[len(l.Stack) - 1]
				l.Stack = l.Stack[:len(l.Stack) - 1]
			} else {
					l.State = StateJsxText
			}
		case LBRACE:
			l.Opened = append(l.Opened, 1)
			l.Stack = append(l.Stack, l.State)
			l.State = StateJsxTemplate
		case LT:
			// Start a new JSX tag.
			l.Stack = append(l.Stack, l.State)
			l.State = StateJsxTag
		}
	}
	l.token = token
${end}

${query go_parser.additionalNodeTypes() = ['InsertedSemicolon', 'Comment', 'BlockComment', 'InvalidToken']}

${template go_parser.parser-}
package ${opts.lang}
${foreach inp in syntax.input}
func (p *Parser) Parse${self->util.needFinalState() ? util.toFirstUpper(inp.target.id) : ''}(lexer *Lexer) bool {
	return p.parse(${index}, ${parser.finalStates[index]}, lexer)
}
${end-}

${call go_parser.applyRule-}
${if self->go_parser.hasRecovering()}
const errSymbol = ${syntax.error.index}
${end-}
${end}
