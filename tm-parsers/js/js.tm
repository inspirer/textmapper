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

%s initial, div, template, templateDiv, jsxTemplate, jsxTemplateDiv;
%x jsxTag, jsxClosingTag, jsxText;

# Accept end-of-input in all states.
<*> eoi: /{eoi}/

invalid_token:
error:

<initial, div, template, templateDiv, jsxTemplate, jsxTemplateDiv, jsxTag, jsxClosingTag> {
  WhiteSpace: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)
}

# LineTerminatorSequence
WhiteSpace: /[\n\r\u2028\u2029]|\r\n/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment:  /\/\*{commentChars}\*\//
# Note: the following rule disables backtracking for incomplete multiline comments, which
# would otherwise be reported as '/', '*', etc.
invalid_token: /\/\*{commentChars}/
SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/

# Note: see http://unicode.org/reports/tr31/
IDStart = /\p{Lu}|\p{Ll}|\p{Lt}|\p{Lm}|\p{Lo}|\p{Nl}/
IDContinue = /{IDStart}|\p{Mn}|\p{Mc}|\p{Nd}|\p{Pc}/
JoinControl = /\u200c|\u200d/

hex = /[0-9a-fA-F]/
unicodeEscapeSequence = /u(\{{hex}+\}|{hex}{4})/
brokenEscapeSequence = /\\(u({hex}{0,3}|\{{hex}*))?/

identifierStart = /{IDStart}|$|_|\\{unicodeEscapeSequence}/
identifierPart =  /{identifierStart}|{IDContinue}|{JoinControl}/

Identifier: /{identifierStart}{identifierPart}*/    (class)
# Note: the following rule disables backtracking for incomplete identifiers.
invalid_token: /({identifierStart}{identifierPart}*)?{brokenEscapeSequence}/

# Keywords.
'await':      /await/
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
'enum':  /enum/

# Literals.
'null':  /null/
'true':  /true/
'false': /false/

# Soft (contextual) keywords.
'as':     /as/
'async':  /async/
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
invalid_token: /\.\./
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

int = /(0+([0-7]*[89][0-9]*)?|[1-9][0-9]*)/
frac = /\.[0-9]*/
exp = /[eE][+-]?[0-9]+/
bad_exp = /[eE][+-]?/
NumericLiteral: /{int}{frac}?{exp}?/
NumericLiteral: /\.[0-9]+{exp}?/
NumericLiteral: /0[xX]{hex}+/
NumericLiteral: /0[oO][0-7]+/
NumericLiteral: /0+[0-7]+/      1 # (Takes priority over the float rule above)
NumericLiteral: /0[bB][01]+/

invalid_token: /0[xXbBoO]/
invalid_token: /{int}{frac}?{bad_exp}/
invalid_token: /\.[0-9]+{bad_exp}/

escape = /\\([^1-9xu\n\r\u2028\u2029]|x{hex}{2}|{unicodeEscapeSequence})/
lineCont = /\\([\n\r\u2028\u2029]|\r\n)/
dsChar = /[^\n\r"\\\u2028\u2029]|{escape}|{lineCont}/
ssChar = /[^\n\r'\\\u2028\u2029]|{escape}|{lineCont}/

# TODO check \0 is valid if [lookahead != DecimalDigit]

StringLiteral: /"{dsChar}*"/
StringLiteral: /'{ssChar}*'/

tplChars = /([^\$`\\]|\$*{escape}|\$*{lineCont}|\$+[^\$\{`\\])*\$*/

<initial, div, jsxTemplate, jsxTemplateDiv> {
  '}': /\}/

  NoSubstitutionTemplate: /`{tplChars}`/
  TemplateHead: /`{tplChars}\$\{/
}

<template, templateDiv> {
  TemplateMiddle: /\}{tplChars}\$\{/
  TemplateTail: /\}{tplChars}`/
}

<initial, template, jsxTemplate> {
  reBS = /\\[^\n\r\u2028\u2029]/
  reClass = /\[([^\n\r\u2028\u2029\]\\]|{reBS})*\]/
  reFirst = /[^\n\r\u2028\u2029\*\[\\\/]|{reBS}|{reClass}/
  reChar = /{reFirst}|\*/
  reFlags = /[a-z]*/

  RegularExpressionLiteral: /\/{reFirst}{reChar}*\/{reFlags}/
}

<div, templateDiv, jsxTemplateDiv> {
  '/': /\//
  '/=': /\/=/
}

<jsxTag, jsxClosingTag> {
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
  # Note: the following rule disables backtracking for incomplete identifiers.
  invalid_token: /({identifierStart}({identifierPart}|-)*)?{brokenEscapeSequence}/
}

<jsxText> {
  '{': /\{/
  '<': /</

  jsxText: /[^{}<>]+/
}

:: parser

%input Module;

%assert empty set(follow error & ~('}' | ')' | ',' | ';' | ']'));

%generate afterErr = set(follow error);

%flag In;
%flag Yield;
%flag Await;
%flag Return;
%flag NoAsync = false;

%lookahead flag NoLet = false;
%lookahead flag NoLetSq = false;
%lookahead flag NoObjLiteral = false;
%lookahead flag NoFuncClass = false;
%lookahead flag StartWithLet = false;

SyntaxError -> SyntaxProblem :
    error ;

IdentifierName :
    Identifier

  # Keywords
  | 'await'
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
  | 'enum'

  # NullLiteral | BooleanLiteral
  | 'null' | 'true' | 'false'

  # Soft keywords
  | 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async'
;

IdentifierNameDecl :
    IdentifierName                                    -> BindingIdentifier
;

IdentifierNameRef :
    IdentifierName                                    -> IdentifierReference
;

# A.2 Expressions

IdentifierReference<Yield, Await, NoAsync> -> IdentifierReference :
# V8 runtime functions start with a percent sign.
# See http://stackoverflow.com/questions/11202824/what-is-in-javascript
    '%'? Identifier
  | [!Yield] 'yield'
  | [!Await] 'await'
  | [!NoLet] 'let'
  | [!NoAsync] 'async'

  # Soft keywords
  | 'as' | 'from' | 'get' | 'of' | 'set' | 'static' | 'target'
;

BindingIdentifier<Yield, Await> -> BindingIdentifier :
    Identifier
  | [!Yield] 'yield'
  | [!Await] 'await'

  # Soft keywords
  | 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async'
;

AsyncArrowBindingIdentifier<Yield> :
  BindingIdentifier<+Await> ;

LabelIdentifier<Yield, Await> -> LabelIdentifier :
    Identifier
  | [!Yield] 'yield'
  | [!Await] 'await'

  # Soft keywords
  | 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async'
;

PrimaryExpression<Yield, Await, NoAsync> -> Expression /* interface */ :
    'this'                                                 -> This
  | IdentifierReference
  | Literal
  | ArrayLiteral
  | [!NoObjLiteral] ObjectLiteral
  | [!NoFuncClass] FunctionExpression
  | [!NoFuncClass] ClassExpression
  | [!NoFuncClass] GeneratorExpression
  | [!NoFuncClass] AsyncFunctionExpression
  | RegularExpressionLiteral                               -> Regexp
  | TemplateLiteral
  | CoverParenthesizedExpressionAndArrowParameterList      -> Parenthesized
  | JSXElement
;

CoverParenthesizedExpressionAndArrowParameterList<Yield, Await> :
    '(' Expression<+In> ')'
  | '(' ')'
  | '(' '...' BindingIdentifier ')'
  | '(' '...' BindingPattern ')'
  | '(' Expression<+In> ',' '...' BindingIdentifier ')'
  | '(' Expression<+In> ',' '...' BindingPattern ')'
  | '(' SyntaxError ')'
;

Literal -> Literal :
    'null'
  | 'true'
  | 'false'
  | NumericLiteral
  | StringLiteral
;

ArrayLiteral<Yield, Await> -> ArrayLiteral :
    '[' Elisionopt ']'
  | '[' list=ElementList ']'
  | '[' list=ElementList ',' Elisionopt ']'
;

ElementList<Yield, Await> :
    Elisionopt AssignmentExpression<+In>
  | Elisionopt SpreadElement
  | ElementList ',' Elisionopt AssignmentExpression<+In>
  | ElementList ',' Elisionopt SpreadElement
;

Elision :
    ','
  | Elision ','
;

SpreadElement<Yield, Await> -> Expression /* interface */ :
    '...' AssignmentExpression<+In>   -> SpreadElement
;

ObjectLiteral<Yield, Await> -> ObjectLiteral :
    '{' '}'
  | '{' PropertyDefinitionList '}'
  | '{' PropertyDefinitionList ',' '}'
;

PropertyDefinitionList<Yield, Await> :
    PropertyDefinition
  | PropertyDefinitionList ',' PropertyDefinition
;

%interface PropertyName, PropertyDefinition;

PropertyDefinition<Yield, Await> -> PropertyDefinition /* interface */ :
    IdentifierReference                                   -> ShorthandProperty
  | PropertyName ':' value=AssignmentExpression<+In>      -> Property
  | MethodDefinition
  | CoverInitializedName                                  -> SyntaxProblem
  | SyntaxError
;

PropertyName<Yield, Await> -> PropertyName /* interface */ :
    LiteralPropertyName
  | ComputedPropertyName
;

LiteralPropertyName -> LiteralPropertyName :
    IdentifierNameDecl
  | StringLiteral
  | NumericLiteral
;

ComputedPropertyName<Yield, Await> -> ComputedPropertyName :
    '[' AssignmentExpression<+In> ']' ;

CoverInitializedName<Yield, Await> :
    IdentifierReference Initializer<+In> ;

Initializer<In, Yield, Await> -> Initializer :
    '=' AssignmentExpression
;

TemplateLiteral<Yield, Await> -> TemplateLiteral :
    template+=NoSubstitutionTemplate
  | template+=TemplateHead substitution+=Expression<+In> TemplateSpans
;

TemplateSpans<Yield, Await> :
    template+=TemplateTail
  | TemplateMiddleList template+=TemplateTail
;

TemplateMiddleList<Yield, Await> :
    template+=TemplateMiddle substitution+=Expression<+In>
  | TemplateMiddleList template+=TemplateMiddle substitution+=Expression<+In>
;

MemberExpression<Yield, Await, NoAsync, flag NoLetOnly = false> -> Expression /* interface */ :
    [!NoLetOnly && !StartWithLet] PrimaryExpression
  | [NoLetOnly && !StartWithLet] PrimaryExpression<+NoLet>
  | [StartWithLet && !NoLetOnly] 'let'                          -> IdentifierReference
  | [StartWithLet] expr=MemberExpression<+NoLetOnly, ~NoAsync> '[' index=Expression<+In> ']'            -> IndexAccess
  | [!StartWithLet] expr=MemberExpression<NoLetOnly: NoLetSq, ~NoAsync> '[' index=Expression<+In> ']'   -> IndexAccess
  | expr=MemberExpression<~NoAsync> '.' selector=IdentifierNameRef        -> PropertyAccess
  | tag=MemberExpression<~NoAsync> literal=TemplateLiteral                -> TaggedTemplate
  | [!StartWithLet] SuperProperty
  | [!StartWithLet] MetaProperty
  | [!StartWithLet] 'new' expr=MemberExpression<~NoAsync> Arguments       -> NewExpression
;

SuperExpression -> Expression /* interface */ :
    'super' -> SuperExpression
;

SuperProperty<Yield, Await> -> Expression /* interface */ :
    expr=SuperExpression '[' index=Expression<+In> ']'          -> IndexAccess
  | expr=SuperExpression '.' selector=IdentifierNameRef         -> PropertyAccess
;

MetaProperty :
    NewTarget ;

NewTarget -> NewTarget :
    'new' '.' 'target' ;

NewExpression<Yield, Await, NoAsync> -> Expression /* interface */ :
    MemberExpression
  | [!StartWithLet] 'new' expr=NewExpression<~NoAsync>      -> NewExpression
;

CallExpression<Yield, Await> -> Expression /* interface */ :
    CoverCallExpressionAndAsyncArrowHead                        -> CallExpression
  | [!StartWithLet] SuperCall                                   -> CallExpression
  | expr=CallExpression Arguments                               -> CallExpression
  | expr=CallExpression '[' index=Expression<+In> ']'           -> IndexAccess
  | expr=CallExpression '.' selector=IdentifierNameRef          -> PropertyAccess
  | tag=CallExpression literal=TemplateLiteral                  -> TaggedTemplate
;

CoverCallExpressionAndAsyncArrowHead<Yield, Await> :
    expr=MemberExpression Arguments ;

SuperCall<Yield, Await> :
    expr=SuperExpression Arguments
;

Arguments<Yield, Await> -> Arguments :
    '(' (list=ArgumentList ','?)? ')' ;

ArgumentList<Yield, Await> :
    AssignmentExpression<+In>
  | SpreadElement
  | ArgumentList ',' AssignmentExpression<+In>
  | ArgumentList ',' SpreadElement
;

LeftHandSideExpression<Yield, Await, NoAsync> -> Expression /* interface */ :
    NewExpression
  | CallExpression
;

UpdateExpression<Yield, Await> -> Expression /* interface */ :
    LeftHandSideExpression
  | LeftHandSideExpression .noLineBreak '++'          -> PostInc
  | LeftHandSideExpression .noLineBreak '--'          -> PostDec
  | [!StartWithLet] '++' UnaryExpression              -> PreInc
  | [!StartWithLet] '--' UnaryExpression              -> PreDec
;

UnaryExpression<Yield, Await> -> Expression /* interface */ :
    UpdateExpression
  | [!StartWithLet] 'delete' UnaryExpression          -> UnaryExpression
  | [!StartWithLet] 'void' UnaryExpression            -> UnaryExpression
  | [!StartWithLet] 'typeof' UnaryExpression          -> UnaryExpression
  | [!StartWithLet] '+' UnaryExpression               -> UnaryExpression
  | [!StartWithLet] '-' UnaryExpression               -> UnaryExpression
  | [!StartWithLet] '~' UnaryExpression               -> UnaryExpression
  | [!StartWithLet] '!' UnaryExpression               -> UnaryExpression
  | [!StartWithLet && Await] AwaitExpression
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

ArithmeticExpression<Yield, Await> -> Expression /* interface */ :
    UnaryExpression
  | left=ArithmeticExpression '+' right=ArithmeticExpression        -> AdditiveExpression
  | left=ArithmeticExpression '-' right=ArithmeticExpression        -> AdditiveExpression
  | left=ArithmeticExpression '<<' right=ArithmeticExpression       -> ShiftExpression
  | left=ArithmeticExpression '>>' right=ArithmeticExpression       -> ShiftExpression
  | left=ArithmeticExpression '>>>' right=ArithmeticExpression      -> ShiftExpression
  | left=ArithmeticExpression '*' right=ArithmeticExpression        -> MultiplicativeExpression
  | left=ArithmeticExpression '/' right=ArithmeticExpression        -> MultiplicativeExpression
  | left=ArithmeticExpression '%' right=ArithmeticExpression        -> MultiplicativeExpression
  | left=UpdateExpression '**' right=ArithmeticExpression           -> ExponentiationExpression
;

BinaryExpression<In, Yield, Await> -> Expression /* interface */ :
    ArithmeticExpression
  | left=BinaryExpression '<' right=BinaryExpression                -> RelationalExpression
  | left=BinaryExpression '>' right=BinaryExpression                -> RelationalExpression
  | left=BinaryExpression '<=' right=BinaryExpression               -> RelationalExpression
  | left=BinaryExpression '>=' right=BinaryExpression               -> RelationalExpression
  | left=BinaryExpression 'instanceof' right=BinaryExpression       -> RelationalExpression
  | [In] left=BinaryExpression 'in' right=BinaryExpression          -> RelationalExpression
  | left=BinaryExpression '==' right=BinaryExpression               -> EqualityExpression
  | left=BinaryExpression '!=' right=BinaryExpression               -> EqualityExpression
  | left=BinaryExpression '===' right=BinaryExpression              -> EqualityExpression
  | left=BinaryExpression '!==' right=BinaryExpression              -> EqualityExpression
  | left=BinaryExpression '&' right=BinaryExpression                -> BitwiseANDExpression
  | left=BinaryExpression '^' right=BinaryExpression                -> BitwiseXORExpression
  | left=BinaryExpression '|' right=BinaryExpression                -> BitwiseORExpression
  | left=BinaryExpression '&&' right=BinaryExpression               -> LogicalANDExpression
  | left=BinaryExpression '||' right=BinaryExpression               -> LogicalORExpression
;

ConditionalExpression<In, Yield, Await> -> Expression /* interface */ :
    BinaryExpression
  | cond=BinaryExpression '?' then=AssignmentExpression<+In> ':' else=AssignmentExpression
        -> ConditionalExpression
;

AssignmentExpression<In, Yield, Await> -> Expression /* interface */ :
    ConditionalExpression
  | [Yield && !StartWithLet] YieldExpression
  | [!StartWithLet] ArrowFunction
  | [!StartWithLet] AsyncArrowFunction
  | left=LeftHandSideExpression '=' right=AssignmentExpression                -> AssignmentExpression
  | left=LeftHandSideExpression AssignmentOperator right=AssignmentExpression -> AssignmentExpression
;

AssignmentOperator -> AssignmentOperator :
    '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|=' | '**=' ;

CommaExpression<In, Yield, Await> -> CommaExpression :
    left=Expression ',' right=AssignmentExpression ;

%interface Expression;

Expression<In, Yield, Await> -> Expression /* interface */ :
    AssignmentExpression
  | CommaExpression
;

# A.3 Statements

%interface Statement, Declaration;

Statement<Yield, Await, Return> -> Statement /* interface */ :
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

Declaration<Yield, Await> -> Declaration /* interface */ :
    HoistableDeclaration
  | ClassDeclaration
  | LexicalDeclaration<+In>
;

HoistableDeclaration<Yield, Await> -> Declaration /* interface */ :
    FunctionDeclaration
  | GeneratorDeclaration
  | AsyncFunctionDeclaration
;

BreakableStatement<Yield, Await, Return> -> Statement /* interface */ :
    IterationStatement
  | SwitchStatement
;

BlockStatement<Yield, Await, Return> :
    Block ;

Block<Yield, Await, Return> -> Block :
    '{' StatementList? '}' ;

StatementList<Yield, Await, Return> :
    StatementListItem
  | StatementList StatementListItem
;

%interface StatementListItem, BindingPattern, PropertyPattern, ElementPattern, CaseClause;

StatementListItem<Yield, Await, Return> -> StatementListItem /* interface */ :
    Statement
  | Declaration
  | error ';'                                         -> SyntaxProblem
;

LexicalDeclaration<In, Yield, Await> -> LexicalDeclaration :
    LetOrConst BindingList ';' ;

LetOrConst :
    'let'
  | 'const'
;

BindingList<In, Yield, Await> :
    LexicalBinding
  | BindingList ',' LexicalBinding
;

LexicalBinding<In, Yield, Await> -> LexicalBinding :
    BindingIdentifier Initializeropt
  | BindingPattern Initializer
;

VariableStatement<Yield, Await> -> VariableStatement :
    'var' VariableDeclarationList<+In> ';'
;

VariableDeclarationList<In, Yield, Await> :
    VariableDeclaration
  | VariableDeclarationList ',' VariableDeclaration
;

VariableDeclaration<In, Yield, Await> -> VariableDeclaration :
    BindingIdentifier Initializeropt
  | BindingPattern Initializer
;

BindingPattern<Yield, Await> -> BindingPattern /* interface */ :
    ObjectBindingPattern
  | ArrayBindingPattern
;

ObjectBindingPattern<Yield, Await> -> ObjectPattern :
    '{' '}'
  | '{' (PropertyPattern separator ',')+ ','? '}'
;

ArrayBindingPattern<Yield, Await> -> ArrayPattern :
    '[' Elisionopt BindingRestElementopt ']'
  | '[' ElementPatternList ']'
  | '[' ElementPatternList ',' Elisionopt BindingRestElementopt ']'
;

ElementPatternList<Yield, Await> :
    BindingElisionElement
  | ElementPatternList ',' BindingElisionElement
;

BindingElisionElement<Yield, Await> :
    Elision? ElementPattern
;

PropertyPattern<Yield, Await> -> PropertyPattern /* interface */ :
    SingleNameBinding
  | PropertyName ':' ElementPattern                   -> PropertyBinding
  | SyntaxError
;

ElementPattern<Yield, Await> -> ElementPattern /* interface */ :
    SingleNameBinding
  | BindingPattern Initializeropt<+In>                -> ElementBinding
  | SyntaxError
;

SingleNameBinding<Yield, Await> -> SingleNameBinding :
    BindingIdentifier Initializeropt<+In>
;

BindingRestElement<Yield, Await> -> BindingRestElement :
    '...' BindingIdentifier
;

EmptyStatement -> EmptyStatement :
    ';' .emptyStatement ;

ExpressionStatement<Yield, Await> -> ExpressionStatement :
    Expression<+In, +NoFuncClass, +NoObjLiteral, +NoLetSq> ';' ;

%right 'else';

IfStatement<Yield, Await, Return> -> IfStatement :
    'if' '(' Expression<+In> ')' then=Statement 'else' else=Statement
  | 'if' '(' Expression<+In> ')' then=Statement %prec 'else'
;

IterationStatement<Yield, Await, Return> -> Statement /* interface */ :
    'do' Statement 'while' '(' Expression<+In> ')' ';' .doWhile       -> DoWhileStatement
  | 'while' '(' Expression<+In> ')' Statement                         -> WhileStatement
  | 'for' '(' var=Expressionopt<~In,+NoLet> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStatement
  | 'for' '(' var=Expression<~In,+StartWithLet> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStatement
  | 'for' '(' 'var' VariableDeclarationList<~In> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStatementWithVar
  | 'for' '(' LetOrConst BindingList<~In> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStatementWithVar
  | 'for' '(' var=LeftHandSideExpression<+NoLet>
          'in' object=Expression<+In> ')' Statement                   -> ForInStatement
  | 'for' '(' var=LeftHandSideExpression<+StartWithLet>
          'in' object=Expression<+In> ')' Statement                   -> ForInStatement
  | 'for' '(' 'var' ForBinding
          'in' object=Expression<+In> ')' Statement                   -> ForInStatementWithVar
  | 'for' '(' ForDeclaration
          'in' object=Expression<+In> ')' Statement                   -> ForInStatementWithVar
  | 'for' '(' var=LeftHandSideExpression<+NoLet, +NoAsync>
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStatement
  | 'for' '(' var=('async' -> IdentifierReference)
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStatement
  | 'for' '(' 'var' ForBinding
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStatementWithVar
  | 'for' '(' ForDeclaration
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStatementWithVar
;

ForDeclaration<Yield, Await> :
    LetOrConst ForBinding
;

ForBinding<Yield, Await> -> ForBinding :
    BindingIdentifier
  | BindingPattern
;

ForCondition<Yield, Await> -> ForCondition :
    Expressionopt<+In> ;

ForFinalExpression<Yield, Await> -> ForFinalExpression :
    Expressionopt<+In> ;

ContinueStatement<Yield, Await> -> ContinueStatement :
    'continue' ';'
  | 'continue' .noLineBreak LabelIdentifier ';'
;

BreakStatement<Yield, Await> -> BreakStatement :
    'break' ';'
  | 'break' .noLineBreak LabelIdentifier ';'
;

ReturnStatement<Yield, Await> -> ReturnStatement :
    'return' ';'
  | 'return' .noLineBreak Expression<+In> ';'
;

WithStatement<Yield, Await, Return> -> WithStatement :
    'with' '(' Expression<+In> ')' Statement
;

SwitchStatement<Yield, Await, Return> -> SwitchStatement :
    'switch' '(' Expression<+In> ')' CaseBlock
;

CaseBlock<Yield, Await, Return> -> Block :
    '{' CaseClausesopt '}'
;

CaseClauses<Yield, Await, Return> :
    CaseClause
  | CaseClauses CaseClause
;

CaseClause<Yield, Await, Return> -> CaseClause /* interface */ :
    'case' Expression<+In> ':' StatementList?         -> Case
  | 'default' ':' StatementList?                      -> Default
;

LabelledStatement<Yield, Await, Return> -> LabelledStatement :
    LabelIdentifier ':' LabelledItem ;

LabelledItem<Yield, Await, Return> :
    Statement
  | FunctionDeclaration
;

ThrowStatement<Yield, Await> -> ThrowStatement :
    'throw' .noLineBreak Expression<+In> ';'
;

TryStatement<Yield, Await, Return> -> TryStatement :
    'try' Block Catch
  | 'try' Block Catch? Finally
;

Catch<Yield, Await, Return> -> Catch :
    'catch' '(' CatchParameter ')' Block
;

Finally<Yield, Await, Return> -> Finally :
    'finally' Block
;

CatchParameter<Yield, Await> :
    BindingIdentifier
  | BindingPattern
;

DebuggerStatement -> DebuggerStatement :
    'debugger' ';'
;

# A.4 Functions and Classes

%interface ClassElement, MethodDefinition;

FunctionDeclaration<Yield, Await> -> Function :
    'function' BindingIdentifier? FormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await> ;

FunctionExpression -> FunctionExpression :
    'function' BindingIdentifier<~Yield, ~Await>? FormalParameters<~Yield, ~Await>
        FunctionBody<~Yield, ~Await> ;

UniqueFormalParameters<Yield, Await> :
    FormalParameters ;

FormalParameters<Yield, Await> -> Parameters :
      '(' FormalParameterList? ')' ;

FormalParameterList<Yield, Await> :
    FunctionRestParameter
  | FormalsList ','?
  | FormalsList ',' FunctionRestParameter
;

FormalsList<Yield, Await> :
    FormalParameter
  | FormalsList ',' FormalParameter
;

FunctionRestParameter<Yield, Await> -> RestParameter :
    BindingRestElement ;

FormalParameter<Yield, Await> -> Parameter :
    ElementPattern ;

FunctionBody<Yield, Await> -> Body :
    '{' StatementList<+Return>? '}' ;

ArrowFunction<In, Yield, Await> -> ArrowFunction :
    ArrowParameters .noLineBreak '=>' ConciseBody ;

ArrowParameters<Yield, Await> -> Parameters :
    BindingIdentifier
  | CoverParenthesizedExpressionAndArrowParameterList
;

ConciseBody<In> :
    AssignmentExpression<~Yield, ~Await, +NoObjLiteral>           -> ConciseBody
  | FunctionBody<~Yield, ~Await>
;

AsyncArrowFunction<In, Yield, Await> -> AsyncArrowFunction :
    'async' .noLineBreak AsyncArrowBindingIdentifier .noLineBreak '=>' AsyncConciseBody
  | CoverCallExpressionAndAsyncArrowHead /* as AsyncArrowHead */  .noLineBreak '=>' AsyncConciseBody
;

# AsyncArrowHead :
#      'async' .noLineBreak ArrowFormalParameters<~Yield, +Await> ;

AsyncConciseBody<In> :
    AssignmentExpression<~Yield, +Await, +NoObjLiteral>           -> ConciseBody
  | AsyncFunctionBody
;

MethodDefinition<Yield, Await> -> MethodDefinition /* interface */ :
    PropertyName UniqueFormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await>      -> Method
  | GeneratorMethod
  | AsyncMethod
  | 'get' PropertyName '(' ')' FunctionBody<~Yield, ~Await>                               -> Getter
  | 'set' PropertyName '(' PropertySetParameterList ')' FunctionBody<~Yield, ~Await>      -> Setter
;

PropertySetParameterList :
    FormalParameter<~Yield, ~Await> ;

GeneratorMethod<Yield, Await> -> GeneratorMethod :
    '*' PropertyName UniqueFormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorDeclaration<Yield, Await> -> Generator :
    'function' '*' BindingIdentifier? FormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorExpression -> GeneratorExpression :
    'function' '*' BindingIdentifier<+Yield, ~Await>? FormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorBody :
    FunctionBody<+Yield, ~Await> ;

YieldExpression<In, Await> -> Yield :
    'yield'
  | 'yield' .afterYield .noLineBreak AssignmentExpression<+Yield>
  | 'yield' .afterYield .noLineBreak '*' AssignmentExpression<+Yield>
;

AsyncMethod<Yield, Await> -> AsyncMethod :
    'async' .noLineBreak PropertyName UniqueFormalParameters<~Yield, +Await> AsyncFunctionBody ;

AsyncFunctionDeclaration<Yield, Await> -> AsyncFunction :
    'async' .noLineBreak 'function' BindingIdentifier? FormalParameters<~Yield> AsyncFunctionBody ;

AsyncFunctionExpression -> AsyncFunctionExpression :
    'async' .noLineBreak 'function' BindingIdentifier<~Yield, +Await>?
          FormalParameters<~Yield, +Await> AsyncFunctionBody ;

AsyncFunctionBody :
    FunctionBody<~Yield, +Await> ;

AwaitExpression<Yield> -> AwaitExpression :
  'await' UnaryExpression<+Await> ;

ClassDeclaration<Yield, Await> -> Declaration /* interface */ :
    'class' BindingIdentifier? ClassTail   -> Class
;

ClassExpression<Yield, Await> -> ClassExpr :
    'class' BindingIdentifier? ClassTail
;

ClassTail<Yield, Await> :
    ClassHeritage? ClassBody ;

ClassHeritage<Yield, Await> -> Extends :
    'extends' LeftHandSideExpression
;

ClassBody<Yield, Await> -> ClassBody :
    '{' ClassElementList? '}' ;

ClassElementList<Yield, Await> :
    ClassElement
  | ClassElementList ClassElement
;

ClassElement<Yield, Await> -> ClassElement /* interface */ :
    MethodDefinition
  | 'static' MethodDefinition                         -> StaticMethod
  | ';'                                               -> EmptyDecl
;

# A.5 Scripts and Modules

%interface ModuleItem, NamedImport, ExportElement;

Module -> Module :
    ModuleBodyopt ;

ModuleBody :
    ModuleItemList ;

ModuleItemList :
    ModuleItem
  | ModuleItemList ModuleItem
;

ModuleItem -> ModuleItem /* interface */ :
    ImportDeclaration
  | ExportDeclaration
  | StatementListItem<~Yield, ~Await, ~Return>
;

ImportDeclaration -> ImportDeclaration :
    'import' ImportClause FromClause ';'
  | 'import' ModuleSpecifier ';'
;

ImportClause :
    ImportedDefaultBinding
  | NameSpaceImport
  | NamedImports
  | ImportedDefaultBinding ',' NameSpaceImport
  | ImportedDefaultBinding ',' NamedImports
;

ImportedDefaultBinding :
    ImportedBinding ;

NameSpaceImport -> NameSpaceImport :
    '*' 'as' ImportedBinding ;

FromClause :
    'from' ModuleSpecifier ;

NamedImports -> NamedImports :
    '{' '}'
  | '{' (NamedImport separator ',')+ ','? '}'
;

NamedImport -> NamedImport /* interface */ :
    ImportedBinding                                   -> ImportSpecifier
  | IdentifierNameRef 'as' ImportedBinding            -> ImportSpecifier
  | error                                             -> SyntaxProblem
;

ModuleSpecifier -> ModuleSpecifier :
    StringLiteral ;

ImportedBinding :
    BindingIdentifier<~Yield, ~Await> ;

ExportDeclaration -> ModuleItem /* interface */ :
    'export' '*' FromClause ';'                       -> ExportDeclaration
  | 'export' ExportClause FromClause ';'              -> ExportDeclaration
  | 'export' ExportClause ';'                         -> ExportDeclaration
  | 'export' VariableStatement<~Yield, ~Await>        -> ExportDeclaration
  | 'export' Declaration<~Yield, ~Await>              -> ExportDeclaration
  | 'export' 'default' HoistableDeclaration<~Yield, ~Await>              -> ExportDefault
  | 'export' 'default' ClassDeclaration<~Yield, ~Await>                  -> ExportDefault
  | 'export' 'default' AssignmentExpression<+In, ~Yield, ~Await, +NoFuncClass> ';' -> ExportDefault
;

ExportClause -> ExportClause :
    '{' '}'
  | '{' (ExportElement separator ',')+ ','? '}'
;

ExportElement -> ExportElement /* interface */ :
    IdentifierNameRef                                 -> ExportSpecifier
  | IdentifierNameRef 'as' IdentifierNameDecl         -> ExportSpecifier
  | error                                             -> SyntaxProblem
;

# Extensions

# JSX (see https://facebook.github.io/jsx/)

%interface JSXAttribute, JSXAttributeValue, JSXChild;

JSXElement<Yield, Await> -> JSXElement :
    JSXSelfClosingElement
  | JSXOpeningElement JSXChild* JSXClosingElement
;

JSXSelfClosingElement<Yield, Await> -> JSXSelfClosingElement :
    '<' JSXElementName JSXAttribute* '/' '>' ;

JSXOpeningElement<Yield, Await> -> JSXOpeningElement :
    '<' JSXElementName JSXAttribute* '>' ;

JSXClosingElement -> JSXClosingElement :
    '<' '/' JSXElementName '>' ;

JSXElementName -> JSXElementName :
    jsxIdentifier
  | jsxIdentifier ':' jsxIdentifier
  | JSXMemberExpression
;

JSXMemberExpression :
    jsxIdentifier '.' jsxIdentifier
  | JSXMemberExpression '.' jsxIdentifier
;

JSXAttribute<Yield, Await> -> JSXAttribute /* interface */ :
    JSXAttributeName '=' JSXAttributeValue            -> JSXNormalAttribute
  | '{' '...' AssignmentExpression<+In> '}'           -> JSXSpreadAttribute
;

JSXAttributeName -> JSXAttributeName :
    jsxIdentifier
  | jsxIdentifier ':' jsxIdentifier
;

JSXAttributeValue<Yield, Await> -> JSXAttributeValue /* interface */ :
    jsxStringLiteral                                  -> JSXLiteral
  | '{' AssignmentExpression<+In> '}'                 -> JSXExpression
  | JSXElement
;

JSXChild<Yield, Await> -> JSXChild /* interface */ :
    jsxText                                           -> JSXText
  | JSXElement
  | '{' AssignmentExpressionopt<+In> '}'              -> JSXExpression
  | '{' '...' AssignmentExpressionopt<+In> '}'        -> JSXSpreadExpression
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
package ${self->go.package()}
${foreach inp in syntax.input.select(it|it.requested)}
func (p *Parser) Parse${self->util.onlyOneUserInput() ? '' : util.toFirstUpper(inp.target.id)}(lexer *Lexer) error {
	return p.parse(${inp.index}, ${parser.finalStates[inp.index]}, lexer)
}

${if self->needExplicitLookahead()-}
${call lookahead}
${end-}
${end-}

${call go_parser.applyRule-}
${if self->go_parser.hasRecovering()}
const errSymbol = ${syntax.error.index}
${end-}
${end}
