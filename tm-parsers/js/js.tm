# ECMAScript 2016 Language Grammar (Standard ECMA-262, 7th Edition / June 2016)
# This grammar also covers JSX - a popular language extension for React.

language js(go);

lang = "js"
package = "github.com/inspirer/textmapper/tm-parsers/js"
eventBased = true
eventFields = true
reportTokens = [MultiLineComment, SingleLineComment, invalid_token,
                NoSubstitutionTemplate, TemplateHead, TemplateMiddle, TemplateTail]
extraTypes = ["InsertedSemicolon"]

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

@noast
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

IdentifierNameDecl ::=
    IdentifierName                                    {~BindingIdentifier}
;

IdentifierNameRef ::=
     IdentifierName                                   {~IdentifierReference}
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
PrimaryExpression<Yield> returns Expression ::=
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
  | '[' list=ElementList ']'
  | '[' list=ElementList ',' Elisionopt ']'
;

@noast @listof{"Element"}
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

PropertyDefinition<Yield> interface ::=
    IdentifierReference                                   {~ShorthandProperty}
  | PropertyName ':' value=AssignmentExpression<+In>      {~Property}
  | @noast MethodDefinition
  | CoverInitializedName                                  {~SyntaxError}
  | @noast SyntaxError
;

PropertyName<Yield> interface ::=
    LiteralPropertyName
  | ComputedPropertyName
;

LiteralPropertyName ::=
    IdentifierNameDecl
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
    template+=NoSubstitutionTemplate
  | template+=TemplateHead substitution+=Expression<+In> TemplateSpans
;

@noast
TemplateSpans<Yield> ::=
    template+=TemplateTail
  | TemplateMiddleList template+=TemplateTail
;

@noast
TemplateMiddleList<Yield> ::=
    template+=TemplateMiddle substitution+=Expression<+In>
  | TemplateMiddleList template+=TemplateMiddle substitution+=Expression<+In>
;

@noast
MemberExpression<Yield, flag NoLetOnly = false> returns Expression ::=
    [!NoLetOnly && !StartWithLet] PrimaryExpression
  | [NoLetOnly && !StartWithLet] PrimaryExpression<+NoLet>
  | [StartWithLet && !NoLetOnly] 'let'                          {~IdentifierReference}
  | [StartWithLet] expr=MemberExpression<+NoLetOnly> '[' index=Expression<+In> ']'            {~IndexAccess}
  | [!StartWithLet] expr=MemberExpression<NoLetOnly: NoLetSq> '[' index=Expression<+In> ']'   {~IndexAccess}
  | expr=MemberExpression '.' selector=IdentifierNameRef        {~PropertyAccess}
  | tag=MemberExpression literal=TemplateLiteral                {~TaggedTemplate}
  | [!StartWithLet] SuperProperty
  | [!StartWithLet] MetaProperty
  | [!StartWithLet] 'new' expr=MemberExpression Arguments       {~NewExpression}
;

SuperExpression returns Expression ::=
    'super'
;

SuperProperty<Yield> returns Expression ::=
    expr=SuperExpression '[' index=Expression<+In> ']'          {~IndexAccess}
  | expr=SuperExpression '.' selector=IdentifierNameRef         {~PropertyAccess}
;

@noast
MetaProperty ::=
    NewTarget ;

NewTarget ::=
    'new' '.' 'target' ;

NewExpression<Yield> returns Expression ::=
    @noast MemberExpression
  | [!StartWithLet] 'new' expr=NewExpression
;

CallExpression<Yield> returns Expression ::=
    expr=MemberExpression Arguments
  | [!StartWithLet] SuperCall
  | expr=CallExpression Arguments
  | expr=CallExpression '[' index=Expression<+In> ']'           {~IndexAccess}
  | expr=CallExpression '.' selector=IdentifierNameRef          {~PropertyAccess}
  | tag=CallExpression literal=TemplateLiteral                  {~TaggedTemplate}
;

@noast
SuperCall<Yield> ::=
    expr=SuperExpression Arguments
;

Arguments<Yield> ::=
    '(' list=ArgumentList? ')'
;

@noast @listof{"Argument"}
ArgumentList<Yield> ::=
    AssignmentExpression<+In>
  | SpreadElement
  | ArgumentList ',' AssignmentExpression<+In>
  | ArgumentList ',' SpreadElement
;

@noast
LeftHandSideExpression<Yield> returns Expression ::=
    NewExpression
  | CallExpression
;

UpdateExpression<Yield> returns Expression ::=
    @noast LeftHandSideExpression
  | LeftHandSideExpression .noLineBreak '++'          {~PostInc}
  | LeftHandSideExpression .noLineBreak '--'          {~PostDec}
  | [!StartWithLet] '++' UnaryExpression              {~PreInc}
  | [!StartWithLet] '--' UnaryExpression              {~PreDec}
;

UnaryExpression<Yield> returns Expression ::=
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

ArithmeticExpression<Yield> returns Expression ::=
    @noast UnaryExpression
  | left=ArithmeticExpression '+' right=ArithmeticExpression        {~AdditiveExpression}
  | left=ArithmeticExpression '-' right=ArithmeticExpression        {~AdditiveExpression}
  | left=ArithmeticExpression '<<' right=ArithmeticExpression       {~ShiftExpression}
  | left=ArithmeticExpression '>>' right=ArithmeticExpression       {~ShiftExpression}
  | left=ArithmeticExpression '>>>' right=ArithmeticExpression      {~ShiftExpression}
  | left=ArithmeticExpression '*' right=ArithmeticExpression        {~MultiplicativeExpression}
  | left=ArithmeticExpression '/' right=ArithmeticExpression        {~MultiplicativeExpression}
  | left=ArithmeticExpression '%' right=ArithmeticExpression        {~MultiplicativeExpression}
  | left=UpdateExpression '**' right=ArithmeticExpression           {~ExponentiationExpression}
;

BinaryExpression<In, Yield> returns Expression ::=
    @noast ArithmeticExpression
  | left=BinaryExpression '<' right=BinaryExpression                {~RelationalExpression}
  | left=BinaryExpression '>' right=BinaryExpression                {~RelationalExpression}
  | left=BinaryExpression '<=' right=BinaryExpression               {~RelationalExpression}
  | left=BinaryExpression '>=' right=BinaryExpression               {~RelationalExpression}
  | left=BinaryExpression 'instanceof' right=BinaryExpression       {~RelationalExpression}
  | [In] left=BinaryExpression 'in' right=BinaryExpression          {~RelationalExpression}
  | left=BinaryExpression '==' right=BinaryExpression               {~EqualityExpression}
  | left=BinaryExpression '!=' right=BinaryExpression               {~EqualityExpression}
  | left=BinaryExpression '===' right=BinaryExpression              {~EqualityExpression}
  | left=BinaryExpression '!==' right=BinaryExpression              {~EqualityExpression}
  | left=BinaryExpression '&' right=BinaryExpression                {~BitwiseANDExpression}
  | left=BinaryExpression '^' right=BinaryExpression                {~BitwiseXORExpression}
  | left=BinaryExpression '|' right=BinaryExpression                {~BitwiseORExpression}
  | left=BinaryExpression '&&' right=BinaryExpression               {~LogicalANDExpression}
  | left=BinaryExpression '||' right=BinaryExpression               {~LogicalORExpression}
;

ConditionalExpression<In, Yield> returns Expression ::=
    @noast BinaryExpression
  | cond=BinaryExpression '?' then=AssignmentExpression<+In> ':' else=AssignmentExpression
;

AssignmentExpression<In, Yield> returns Expression ::=
    @noast ConditionalExpression
  | [Yield && !StartWithLet] @noast YieldExpression
  | [!StartWithLet] @noast ArrowFunction
  | left=LeftHandSideExpression '=' right=AssignmentExpression
  | left=LeftHandSideExpression AssignmentOperator right=AssignmentExpression
;

AssignmentOperator ::=
    '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|=' | '**=' ;

CommaExpression<In, Yield> returns Expression ::=
    left=Expression ',' right=AssignmentExpression ;

Expression<In, Yield> interface ::=
    AssignmentExpression
  | CommaExpression
;

# A.3 Statements

Statement<Yield, Return> interface ::=
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

Declaration<Yield> interface ::=
    HoistableDeclaration<~Default>
  | ClassDeclaration<~Default>
  | LexicalDeclaration<+In>
;

@noast
HoistableDeclaration<Yield, Default> returns Declaration ::=
    FunctionDeclaration
  | GeneratorDeclaration
;

@noast
BreakableStatement<Yield, Return> returns Statement::=
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
StatementListItem<Yield, Return> interface ::=
    Statement
  | Declaration
  | error ';'                                         {~SyntaxError}
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

BindingPattern<Yield> interface::=
    ObjectBindingPattern
  | ArrayBindingPattern
;

ObjectBindingPattern<Yield> ::=
    '{' '}'                                           {~ObjectPattern}
  | '{' (PropertyPattern separator ',')+ ','? '}'     {~ObjectPattern}
;

ArrayBindingPattern<Yield> ::=
    '[' Elisionopt BindingRestElementopt ']'                          {~ArrayPattern}
  | '[' ElementPatternList ']'                                        {~ArrayPattern}
  | '[' ElementPatternList ',' Elisionopt BindingRestElementopt ']'   {~ArrayPattern}
;

@noast
ElementPatternList<Yield> ::=
    BindingElisionElement
  | ElementPatternList ',' BindingElisionElement
;

@noast
BindingElisionElement<Yield> ::=
    Elision? ElementPattern
;

PropertyPattern<Yield> interface ::=
    @noast SingleNameBinding
  | PropertyName ':' ElementPattern                   {~PropertyBinding}
  | @noast SyntaxError
;

ElementPattern<Yield> interface ::=
    @noast SingleNameBinding
  | BindingPattern Initializeropt<+In>                {~ElementBinding}
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
    'if' '(' Expression<+In> ')' then=Statement 'else' else=Statement
  | 'if' '(' Expression<+In> ')' then=Statement %prec 'else'
;

IterationStatement<Yield, Return> returns Statement ::=
    'do' Statement 'while' '(' Expression<+In> ')' ';' .doWhile       {~DoWhileStatement}
  | 'while' '(' Expression<+In> ')' Statement                         {~WhileStatement}
  | 'for' '(' var=Expressionopt<~In,+NoLet> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 {~ForStatement}
  | 'for' '(' var=Expression<~In,+StartWithLet> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 {~ForStatement}
  | 'for' '(' 'var' VariableDeclarationList<~In> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 {~ForStatementWithVar}
  | 'for' '(' LetOrConst BindingList<~In> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 {~ForStatementWithVar}
  | 'for' '(' var=LeftHandSideExpression<+NoLet>
          'in' object=Expression<+In> ')' Statement                   {~ForInStatement}
  | 'for' '(' var=LeftHandSideExpression<+StartWithLet>
          'in' object=Expression<+In> ')' Statement                   {~ForInStatement}
  | 'for' '(' 'var' ForBinding
          'in' object=Expression<+In> ')' Statement                   {~ForInStatementWithVar}
  | 'for' '(' ForDeclaration
          'in' object=Expression<+In> ')' Statement                   {~ForInStatementWithVar}
  | 'for' '(' var=LeftHandSideExpression<+NoLet>
          'of' iterable=AssignmentExpression<+In> ')' Statement       {~ForOfStatement}
  | 'for' '(' 'var' ForBinding
          'of' iterable=AssignmentExpression<+In> ')' Statement       {~ForOfStatementWithVar}
  | 'for' '(' ForDeclaration
          'of' iterable=AssignmentExpression<+In> ')' Statement       {~ForOfStatementWithVar}
;

@noast
ForDeclaration<Yield> ::=
    LetOrConst ForBinding
;

ForBinding<Yield> ::=
    BindingIdentifier
  | BindingPattern
;

ForCondition<Yield> ::=
    Expressionopt<+In> ;

ForFinalExpression<Yield> ::=
    Expressionopt<+In> ;

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
    '{' CaseClausesopt '}'                            {~Block}
;

@noast
CaseClauses<Yield, Return> ::=
    CaseClause
  | CaseClauses CaseClause
;

CaseClause<Yield, Return> interface ::=
    'case' Expression<+In> ':' StatementList?         {~Case}
  | 'default' ':' StatementList?                      {~Default}
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

@noast
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
      '(' FormalParameterList? ')'                    {~Parameters}
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
    BindingRestElement                                {~RestParameter}
;

FormalParameter<Yield> ::=
    ElementPattern                                    {~Parameter}
;

FunctionBody<Yield> ::=
    '{' StatementList<+Return>? '}'                   {~Body}
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

MethodDefinition<Yield> interface ::=
    PropertyName StrictFormalParameters FunctionBody                      {~Method}
  | @noast GeneratorMethod
  | 'get' PropertyName '(' ')' FunctionBody                               {~Getter}
  | 'set' PropertyName '(' PropertySetParameterList ')' FunctionBody      {~Setter}
;

@noast
PropertySetParameterList ::=
    FormalParameter<~Yield> ;

GeneratorMethod<Yield> ::=
    '*' PropertyName StrictFormalParameters<+Yield> GeneratorBody ;

GeneratorDeclaration<Yield, Default> ::=
    'function' '*' BindingIdentifier FormalParameters<+Yield> GeneratorBody  {~Generator}
  | [Default] 'function' '*' FormalParameters<+Yield> GeneratorBody          {~Generator}
;

GeneratorExpression ::=
    'function' '*' BindingIdentifier<+Yield>? FormalParameters<+Yield> GeneratorBody ;

@noast
GeneratorBody ::=
    FunctionBody<+Yield> ;

YieldExpression<In> ::=
    'yield'                                                                {~Yield}
  | 'yield' .afterYield .noLineBreak AssignmentExpression<+Yield>          {~Yield}
  | 'yield' .afterYield .noLineBreak '*' AssignmentExpression<+Yield>      {~Yield}
;

ClassDeclaration<Yield, Default> returns Declaration ::=
    'class' BindingIdentifier ClassTail               {~Class}
  | [Default] 'class' ClassTail                       {~Class}
;

ClassExpression<Yield> ::=
    'class' BindingIdentifier? ClassTail              {~ClassExpr}
;

@noast
ClassTail<Yield> ::=
    ClassHeritage? ClassBody ;

ClassHeritage<Yield> ::=
    'extends' LeftHandSideExpression                  {~Extends}
;

ClassBody<Yield> ::=
    '{' ClassElementList? '}' ;

@noast
ClassElementList<Yield> ::=
    ClassElement
  | ClassElementList ClassElement
;

ClassElement<Yield> interface ::=
    @noast MethodDefinition
  | 'static' MethodDefinition                         {~StaticMethod}
  | ';'                                               {~EmptyDecl}
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
ModuleItem interface ::=
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

@noast
FromClause ::=
    'from' ModuleSpecifier ;

NamedImports ::=
    '{' '}'
  | '{' (NamedImport separator ',')+ ','? '}'
;

NamedImport interface ::=
    ImportedBinding                                   {~ImportSpecifier}
  | IdentifierNameRef 'as' ImportedBinding            {~ImportSpecifier}
  | error                                             {~SyntaxError}
;

ModuleSpecifier ::=
    StringLiteral ;

@noast
ImportedBinding ::=
    BindingIdentifier<~Yield> ;

ExportDeclaration returns ModuleItem ::=
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
  | '{' (ExportElement separator ',')+ ','? '}'
;

ExportElement interface ::=
    IdentifierNameRef                                 {~ExportSpecifier}
  | IdentifierNameRef 'as' IdentifierNameDecl         {~ExportSpecifier}
  | error                                             {~SyntaxError}
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

JSXAttribute<Yield> interface ::=
    JSXAttributeName '=' JSXAttributeValue            {~JSXNormalAttribute}
  | '{' '...' AssignmentExpression<+In> '}'           {~JSXSpreadAttribute}
;

JSXAttributeName ::=
    jsxIdentifier
  | jsxIdentifier ':' jsxIdentifier
;

JSXAttributeValue<Yield> interface ::=
    jsxStringLiteral                                  {~JSXLiteral}
  | '{' AssignmentExpression<+In> '}'                 {~JSXExpression}
  | JSXElement
;

JSXChild<Yield> interface ::=
    jsxText                                           {~JSXText}
  | JSXElement
  | '{' AssignmentExpressionopt<+In> '}'              {~JSXExpression}
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

${template go_parser.parser-}
package ${self->go.shortPackage()}
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
