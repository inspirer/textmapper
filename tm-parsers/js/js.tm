# ECMAScript 2016 Language Grammar (Standard ECMA-262, 7th Edition / June 2016)
# This grammar also covers:
#   JSX - a popular language extension for React.
#   TypeScript 2.8 (see typescriptlang.org)
language js(go);

lang = "js"
package = "github.com/inspirer/textmapper/tm-parsers/js"
eventBased = true
eventFields = true
eventAST = true
fileNode = "Module"
cancellable = true
recursiveLookaheads = true
reportTokens = [MultiLineComment, SingleLineComment, invalid_token,
                NoSubstitutionTemplate, TemplateHead, TemplateMiddle, TemplateTail]
extraTypes = ["InsertedSemicolon"]

:: lexer

%s initial, div, template, templateDiv, templateExpr, templateExprDiv, jsxTypeArgs;
%x jsxTag, jsxClosingTag, jsxText;

# Accept end-of-input in all states.
<*> eoi: /{eoi}/

invalid_token:
error:

<initial, div, template, templateDiv, templateExpr, templateExprDiv, jsxTag, jsxClosingTag> {
WhiteSpace: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)
}

# LineTerminatorSequence
WhiteSpace: /[\n\r\u2028\u2029]|\r\n/ (space)

commentChars = /([^*]|\*+[^*\/])*\**/
MultiLineComment:  /\/\*{commentChars}\*\//     (space)
# Note: the following rule disables backtracking for incomplete multiline comments, which
# would otherwise be reported as '/', '*', etc.
invalid_token: /\/\*{commentChars}/
SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/   (space)

# Shebang.
SingleLineComment: /#![^\n\r\u2028\u2029]*/   (space)

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

# Typescript. A.0 Keywords

# The following keywords cannot be used as identifiers in strict mode code, but
# are otherwise not restricted:
'implements':   /implements/
'interface':    /interface/
'private':      /private/
'protected':    /protected/
'public':       /public/

# The following keywords cannot be used as user defined type names, but are
# otherwise not restricted:
'any':     /any/
'unknown': /unknown/
'boolean': /boolean/
'number':  /number/
'string':  /string/
'symbol':  /symbol/

# The following keywords have special meaning in certain contexts, but are
# valid identifiers:
'abstract':    /abstract/
'constructor': /constructor/
'declare':     /declare/
'is':          /is/
'module':      /module/
'namespace':   /namespace/
'require':     /require/
'type':        /type/

# Typescript 2.0+
'readonly': /readonly/
'keyof': /keyof/
'unique': /unique/
'infer': /infer/

# End of typescript keywords.

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
'@': /@/
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
'??': /\?\?/
invalid_token: /\?\.[0-9]/   { l.rewind(l.tokenOffset+1); token = QUEST }
'?.': /\?\./
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

int = /(0+(([1-7](_*{oct})*_*)?[89](_*[0-9])*)?|[1-9](_*[0-9])*)/
frac = /\.([0-9](_*[0-9])*)?/
exp = /[eE][+-]?[0-9](_*[0-9])*/
bad_exp = /[eE][+-]?([0-9](_*[0-9])*_+)?/
oct = /[0-7]/
NumericLiteral: /{int}{frac}?{exp}?/
NumericLiteral: /\.[0-9](_*[0-9])*{exp}?/
NumericLiteral: /0[xX]{hex}(_*{hex})*/
NumericLiteral: /0[oO]{oct}(_*{oct})*/
NumericLiteral: /0+[1-7](_*{oct})*/
NumericLiteral: /0[bB][01](_*[01])*/

invalid_token: /0[xXbBoO]/
invalid_token: /0[xX]{hex}(_*{hex})*_+/
invalid_token: /0[oO]{oct}(_*{oct})*_+([89a-fA-F][0-9a-fA-F_]*)?/
invalid_token: /0[bB][01](_*[01])*_+([2-9a-fA-F][0-9a-fA-F_]*)?/
invalid_token: /{int}{frac}?({bad_exp}|_+)|0+_+[0-9a-fA-F_]*|{int}_+{frac}_*({exp}|{bad_exp})?/
invalid_token: /\.[0-9](_*[0-9])*({bad_exp}|_+)/
invalid_token: /0+[1-7](_*{oct})*_+/

escape = /\\([^1-9xu\n\r\u2028\u2029]|x{hex}{2}|{unicodeEscapeSequence})/
lineCont = /\\([\n\r\u2028\u2029]|\r\n)/
dsChar = /[^\n\r"\\\u2028\u2029]|{escape}|{lineCont}/
ssChar = /[^\n\r'\\\u2028\u2029]|{escape}|{lineCont}/

# TODO check \0 is valid if [lookahead != DecimalDigit]

StringLiteral: /"{dsChar}*"/
StringLiteral: /'{ssChar}*'/

tplChars = /([^\$`\\]|\$*{escape}|\$*{lineCont}|\$+[^\$\{`\\])*\$*/

<initial, div, templateExpr, templateExprDiv>
'}': /\}/

<initial, div, template, templateDiv, templateExpr, templateExprDiv> {
NoSubstitutionTemplate: /`{tplChars}`/
TemplateHead: /`{tplChars}\$\{/
}

<template, templateDiv> {
TemplateMiddle: /\}{tplChars}\$\{/
TemplateTail: /\}{tplChars}`/
}

<initial, template, templateExpr> {
reBS = /\\[^\n\r\u2028\u2029]/
reClass = /\[([^\n\r\u2028\u2029\]\\]|{reBS})*\]/
reFirst = /[^\n\r\u2028\u2029\*\[\\\/]|{reBS}|{reClass}/
reChar = /{reFirst}|\*/
reFlags = /[a-z]*/

RegularExpressionLiteral: /\/{reFirst}{reChar}*\/{reFlags}/
}

<div, templateDiv, templateExprDiv> {
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

# For precedence resolution.
resolveShift:

:: parser

%input Module;

%assert empty set(follow error & ~('}' | ')' | ',' | ';' | ']'));

%generate afterErr = set(follow error);

%flag In;
%flag Yield;
%flag Await;
%flag NoAsync = false;

%flag WithoutNew = false;
%flag WithoutPredefinedTypes = false;
%flag WithoutImplements = false;

%lookahead flag NoLet = false;
%lookahead flag NoLetSq = false;
%lookahead flag NoObjLiteral = false;
%lookahead flag NoFuncClass = false;
%lookahead flag NoAs = false;
%lookahead flag StartWithLet = false;

SyntaxError -> SyntaxProblem :
    error ;

IdentifierName<WithoutNew> :
    Identifier

# Keywords
  | [!WithoutNew] 'new'
  | 'await'
  | 'break'      | 'do'         | 'in'         | 'typeof'
  | 'case'       | 'else'       | 'instanceof' | 'var'
  | 'catch'      | 'export'     | 'void'
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

  # Typescript.
  | 'implements' | 'interface' | 'private' | 'protected' | 'public'
  | 'any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol'
  | 'abstract' | 'constructor' | 'declare' | 'is' | 'module' | 'namespace' | 'require' | 'type'
  | 'readonly' | 'keyof' | 'unique' | 'infer'
;

IdentifierNameDecl<WithoutNew> :
    IdentifierName                                    -> BindingIdentifier
;

IdentifierNameRef :
    IdentifierName                                    -> IdentifierReference
;

# A.2 Expressions

IdentifierReference<Yield, Await, NoAsync, WithoutPredefinedTypes> -> IdentifierReference :
# V8 runtime functions start with a percent sign.
# See http://stackoverflow.com/questions/11202824/what-is-in-javascript
    '%'? Identifier
  | [!Yield] 'yield'
  | [!Await] 'await'
  | [!NoLet] 'let'
  | [!NoAsync] 'async' (?= !StartOfArrowFunction)

  # Soft keywords
  | 'as' | 'from' | 'get' | 'of' | 'set' | 'static' | 'target'

  # Typescript.
  | 'implements' | 'interface' | 'private' | 'protected' | 'public'
  | [!WithoutPredefinedTypes] ('any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol')
  | 'abstract' | 'constructor' | 'declare' | 'is' | 'module' | 'namespace' | 'require' | 'type'
  | [!WithoutPredefinedTypes] ('keyof' | 'unique' | 'readonly' | 'infer')
;

BindingIdentifier<WithoutImplements> -> BindingIdentifier :
    Identifier

  # These are allowed or not, depending on the context.
  | 'yield' | 'await'

  # Soft keywords
  | 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async'

  # Typescript.
  | [!WithoutImplements] 'implements'
  | 'interface' | 'private' | 'protected' | 'public'
  | 'any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol'
  | 'abstract' | 'constructor' | 'declare' | 'is' | 'module' | 'namespace' | 'require' | 'type'
  | 'readonly' | 'keyof' | 'unique' | 'infer'
;

LabelIdentifier -> LabelIdentifier :
    Identifier

  # These are allowed or not, depending on the context.
  | 'yield' | 'await'

  # Soft keywords
  | 'as' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async'

  # Typescript.
  | 'implements' | 'interface' | 'private' | 'protected' | 'public'
  | 'any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol'
  | 'abstract' | 'constructor' | 'declare' | 'is' | 'module' | 'namespace' | 'require' | 'type'
  | 'readonly' | 'keyof' | 'unique' | 'infer'
;

PrimaryExpression<Yield, Await, NoAsync> -> Expression /* interface */:
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
  | (?= !StartOfArrowFunction) Parenthesized
  | (?= !StartOfArrowFunction) JSXElement
;

Parenthesized<Yield, Await> -> Parenthesized:
    '(' Expression<+In> ')'
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

SpreadElement<Yield, Await> -> Expression /* interface */:
    '...' AssignmentExpression<+In>   -> SpreadElement
;

ObjectLiteral<Yield, Await> -> ObjectLiteral :
    '{' '}'
  | '{' .recoveryScope PropertyDefinitionList '}'
  | '{' .recoveryScope PropertyDefinitionList ',' '}'
;

PropertyDefinitionList<Yield, Await> :
    PropertyDefinition
  | PropertyDefinitionList ',' PropertyDefinition
;

%interface PropertyName, PropertyDefinition;

PropertyDefinition<Yield, Await> -> PropertyDefinition /* interface */:
    IdentifierReference                                   -> ShorthandProperty
  | Modifiers? PropertyName ':' value=AssignmentExpression<+In>      -> Property
  | Modifiers? MethodDefinition                           -> ObjectMethod
  | CoverInitializedName                                  -> SyntaxProblem
  | SyntaxError
  | '...' AssignmentExpression<+In>                       -> SpreadProperty
;

PropertyName<Yield, Await, WithoutNew> -> PropertyName /* interface */:
    LiteralPropertyName
  | ComputedPropertyName
;

LiteralPropertyName<WithoutNew> -> LiteralPropertyName :
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

MemberExpression<Yield, Await, NoAsync, flag NoLetOnly = false> -> Expression /* interface */:
    [!NoLetOnly && !StartWithLet] PrimaryExpression
  | [NoLetOnly && !StartWithLet] PrimaryExpression<+NoLet>
  | [StartWithLet && !NoLetOnly] 'let'                          -> IdentifierReference
  | [StartWithLet] expr=MemberExpression<+NoLetOnly, ~NoAsync> '[' index=Expression<+In> ']'            -> IndexAccess
  | [!StartWithLet] expr=MemberExpression<NoLetOnly: NoLetSq, ~NoAsync> '[' index=Expression<+In> ']'   -> IndexAccess
  | expr=MemberExpression<~NoAsync> '.' selector=IdentifierNameRef        -> PropertyAccess
  | tag=MemberExpression<~NoAsync> literal=TemplateLiteral                -> TaggedTemplate
  | expr=MemberExpression<~NoAsync> .noLineBreak '!'                      -> TsNonNull
  | [!StartWithLet] SuperProperty
  | [!StartWithLet] MetaProperty
  | [!StartWithLet] 'new' expr=MemberExpression<~NoAsync> Arguments       -> NewExpression
;

SuperExpression -> Expression /* interface */:
    'super' -> SuperExpression
;

SuperProperty<Yield, Await> -> Expression /* interface */:
    expr=SuperExpression '[' index=Expression<+In> ']'          -> IndexAccess
  | expr=SuperExpression '.' selector=IdentifierNameRef         -> PropertyAccess
;

MetaProperty :
    NewTarget ;

NewTarget -> NewTarget :
    'new' '.' 'target' ;

NewExpression<Yield, Await, NoAsync> -> Expression /* interface */:
    MemberExpression  (?= !StartOfParametrizedCall)
  | [!StartWithLet] 'new' expr=NewExpression<~NoAsync>      -> NewExpression
;

CallExpression<Yield, Await> -> Expression /* interface */:
    expr=MemberExpression Arguments                             -> CallExpression
  | [!StartWithLet] SuperCall                                   -> CallExpression
  | [!StartWithLet] 'import' Arguments                          -> TsDynamicImport
  | expr=CallExpression Arguments                               -> CallExpression
  | expr=CallExpression '[' index=Expression<+In> ']'           -> IndexAccess
  | expr=CallExpression '.' selector=IdentifierNameRef          -> PropertyAccess
  | expr=CallExpression .noLineBreak '!'                        -> TsNonNull
  | tag=CallExpression literal=TemplateLiteral                  -> TaggedTemplate
;

SuperCall<Yield, Await> :
    expr=SuperExpression Arguments
;

Arguments<Yield, Await> -> Arguments :
    (?= StartOfParametrizedCall) TypeArguments '(' (list=ArgumentList ','?)? ')'
  | '(' (list=ArgumentList ','?)? ')'
;

StartOfParametrizedCall:
    TypeArguments '(' ;

ArgumentList<Yield, Await> :
    AssignmentExpression<+In>
  | SpreadElement
  | ArgumentList ',' AssignmentExpression<+In>
  | ArgumentList ',' SpreadElement
;

OptionalLHS<Yield, Await> :
    MemberExpression  | CallExpression | OptionalExpression ;

OptionalExpression<Yield, Await> -> Expression:
    expr=OptionalLHS '?.' '[' index=Expression<+In> ']'      -> OptionalIndexAccess
  | expr=OptionalLHS '?.' selector=IdentifierNameRef         -> OptionalPropertyAccess
  | expr=OptionalLHS '?.' Arguments                          -> OptionalCallExpression
  | tag=OptionalLHS '?.' literal=TemplateLiteral             -> OptionalTaggedTemplate
  | expr=OptionalExpression '[' index=Expression<+In> ']'    -> IndexAccess
  | expr=OptionalExpression '.' selector=IdentifierNameRef   -> PropertyAccess
  | expr=OptionalExpression Arguments                        -> CallExpression
  | tag=OptionalExpression literal=TemplateLiteral           -> TaggedTemplate
;

LeftHandSideExpression<Yield, Await, NoAsync> -> Expression /* interface */:
    NewExpression
  | CallExpression (?= !StartOfParametrizedCall)
  | OptionalExpression (?= !StartOfParametrizedCall)
;

UpdateExpression<Yield, Await> -> Expression /* interface */:
    LeftHandSideExpression
  | LeftHandSideExpression .noLineBreak '++'          -> PostInc
  | LeftHandSideExpression .noLineBreak '--'          -> PostDec
  | [!StartWithLet] '++' UnaryExpression              -> PreInc
  | [!StartWithLet] '--' UnaryExpression              -> PreDec
;

UnaryExpression<Yield, Await> -> Expression /* interface */:
    UpdateExpression
  | [!StartWithLet] 'delete' UnaryExpression          -> UnaryExpression
  | [!StartWithLet] 'void' UnaryExpression            -> UnaryExpression
  | [!StartWithLet] 'typeof' UnaryExpression          -> UnaryExpression
  | [!StartWithLet] '+' UnaryExpression               -> UnaryExpression
  | [!StartWithLet] '-' UnaryExpression               -> UnaryExpression
  | [!StartWithLet] '~' UnaryExpression               -> UnaryExpression
  | [!StartWithLet] '!' UnaryExpression               -> UnaryExpression
  | [!StartWithLet && Await] AwaitExpression
  | [!StartWithLet] (?= !StartOfArrowFunction) '<' Type '>' UnaryExpression -> TsCastExpression
;

%left resolveShift;

%left '||';
%left '&&';
%left '??';
%left '|';
%left '^';
%left '&';
%left '==' '!=' '===' '!==';
%left '<' '>' '<=' '>=' 'instanceof' 'in' 'as';
%left '<<' '>>' '>>>';
%left '-' '+';
%left '*' '/' '%';
%right '**';

ArithmeticExpression<Yield, Await> -> Expression /* interface */:
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

BinaryExpression<In, Yield, Await> -> Expression /* interface */:
    ArithmeticExpression
  | left=BinaryExpression '<' right=BinaryExpression                -> RelationalExpression
  | left=BinaryExpression '>' right=BinaryExpression                -> RelationalExpression
  | left=BinaryExpression '<=' right=BinaryExpression               -> RelationalExpression
  | left=BinaryExpression '>=' right=BinaryExpression               -> RelationalExpression
  | left=BinaryExpression 'instanceof' right=BinaryExpression       -> RelationalExpression
  | [In] left=BinaryExpression 'in' right=BinaryExpression          -> RelationalExpression
  | [!NoAs] left=BinaryExpression .noLineBreak 'as' Type<~AllowQuest> -> TsAsExpression
  | left=BinaryExpression '==' right=BinaryExpression               -> EqualityExpression
  | left=BinaryExpression '!=' right=BinaryExpression               -> EqualityExpression
  | left=BinaryExpression '===' right=BinaryExpression              -> EqualityExpression
  | left=BinaryExpression '!==' right=BinaryExpression              -> EqualityExpression
  | left=BinaryExpression '&' right=BinaryExpression                -> BitwiseANDExpression
  | left=BinaryExpression '^' right=BinaryExpression                -> BitwiseXORExpression
  | left=BinaryExpression '|' right=BinaryExpression                -> BitwiseORExpression
  | left=BinaryExpression '&&' right=BinaryExpression               -> LogicalANDExpression
  | left=BinaryExpression '||' right=BinaryExpression               -> LogicalORExpression
  | left=BinaryExpression '??' right=BinaryExpression               -> CoalesceExpression
;

ConditionalExpression<In, Yield, Await> -> Expression /* interface */:
    BinaryExpression
  | cond=BinaryExpression '?' then=AssignmentExpression<+In> ':' else=AssignmentExpression
        -> ConditionalExpression
;

AssignmentExpression<In, Yield, Await> -> Expression /* interface */:
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

Expression<In, Yield, Await> -> Expression /* interface */:
    AssignmentExpression
  | CommaExpression
;

# A.3 Statements

%interface Statement, Declaration;

Statement<Yield, Await> -> Statement /* interface */:
    BlockStatement
  | VariableStatement
  | EmptyStatement
  | ExpressionStatement
  | IfStatement
  | BreakableStatement
  | ContinueStatement
  | BreakStatement
  | ReturnStatement
  | WithStatement
  | LabelledStatement
  | ThrowStatement
  | TryStatement
  | DebuggerStatement
;

Declaration<Yield, Await> -> Declaration /* interface */:
    HoistableDeclaration
  | ClassDeclaration
  | LexicalDeclaration<+In>
  | TypeAliasDeclaration
  | NamespaceDeclaration
  | InterfaceDeclaration
  | EnumDeclaration
  | ImportAliasDeclaration
  | AmbientDeclaration
;

HoistableDeclaration<Await> -> Declaration /* interface */:
    FunctionDeclaration
  | GeneratorDeclaration
  | AsyncFunctionDeclaration
;

BreakableStatement<Yield, Await> -> Statement /* interface */:
    IterationStatement
  | SwitchStatement
;

BlockStatement<Yield, Await> :
    Block ;

Block<Yield, Await> -> Block :
    '{' .recoveryScope StatementList? '}' ;

StatementList<Yield, Await> :
    StatementListItem
  | StatementList StatementListItem
;

%interface StatementListItem, BindingPattern, PropertyPattern, ElementPattern, CaseClause;

StatementListItem<Yield, Await> -> StatementListItem /* interface */:
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

ExclToken -> TsExclToken:
    '!' ;

LexicalBinding<In, Yield, Await> -> LexicalBinding :
    BindingIdentifier ExclToken? TypeAnnotationopt Initializeropt
  | BindingPattern ExclToken? TypeAnnotationopt Initializer
;

VariableStatement<Yield, Await> -> VariableStatement :
    'var' VariableDeclarationList<+In> ';'
;

VariableDeclarationList<In, Yield, Await> :
    VariableDeclaration
  | VariableDeclarationList ',' VariableDeclaration
;

VariableDeclaration<In, Yield, Await> -> VariableDeclaration :
    BindingIdentifier ExclToken? TypeAnnotationopt Initializeropt
  | BindingPattern ExclToken? TypeAnnotationopt Initializer
;

BindingPattern<Yield, Await> -> BindingPattern /* interface */:
    ObjectBindingPattern
  | ArrayBindingPattern
;

ObjectBindingPattern<Yield, Await> -> ObjectPattern :
    '{' .recoveryScope BindingRestElementopt '}'
  | '{' .recoveryScope (PropertyPattern separator ',')+ (',' BindingRestElementopt)? '}'
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

PropertyPattern<Yield, Await> -> PropertyPattern /* interface */:
    SingleNameBinding
  | PropertyName ':' ElementPattern                   -> PropertyBinding
  | SyntaxError
;

ElementPattern<Yield, Await> -> ElementPattern /* interface */:
    SingleNameBinding
  | BindingPattern Initializeropt<+In>                -> ElementBinding
  | SyntaxError
;

SingleNameBinding<Yield, Await> -> SingleNameBinding :
    BindingIdentifier Initializeropt<+In>
;

BindingRestElement -> BindingRestElement :
    '...' BindingIdentifier
;

EmptyStatement -> EmptyStatement :
    ';' .emptyStatement ;

ExpressionStatement<Yield, Await> -> ExpressionStatement :
    Expression<+In, +NoFuncClass, +NoAs, +NoObjLiteral, +NoLetSq> ';' ;

%right 'else';

IfStatement<Yield, Await> -> IfStatement :
    'if' '(' Expression<+In> ')' then=Statement 'else' else=Statement
  | 'if' '(' Expression<+In> ')' then=Statement %prec 'else'
;

IterationStatement<Yield, Await> -> Statement /* interface */:
    'do' Statement 'while' '(' Expression<+In> ')' ';' .doWhile       -> DoWhileStatement
  | 'while' '(' Expression<+In> ')' Statement                         -> WhileStatement
  | 'for' '(' var=Expressionopt<~In,+NoLet> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStatement
  | 'for' '(' var=Expression<~In,+StartWithLet, +NoAs> ';' .forSC ForCondition
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
  | 'for' '(' var=('async' -> IdentifierReference) (?= !StartOfArrowFunction)
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

ContinueStatement -> ContinueStatement :
    'continue' ';'
  | 'continue' .noLineBreak LabelIdentifier ';'
;

BreakStatement -> BreakStatement :
    'break' ';'
  | 'break' .noLineBreak LabelIdentifier ';'
;

ReturnStatement<Yield, Await> -> ReturnStatement :
    'return' ';'
  | 'return' .noLineBreak Expression<+In> ';'
;

WithStatement<Yield, Await> -> WithStatement :
    'with' '(' Expression<+In> ')' Statement
;

SwitchStatement<Yield, Await> -> SwitchStatement :
    'switch' '(' Expression<+In> ')' CaseBlock
;

CaseBlock<Yield, Await> -> Block :
    '{' .recoveryScope CaseClausesopt '}'
;

CaseClauses<Yield, Await> :
    CaseClause
  | CaseClauses CaseClause
;

CaseClause<Yield, Await> -> CaseClause /* interface */:
    'case' Expression<+In> ':' StatementList?         -> Case
  | 'default' ':' StatementList?                      -> Default
;

LabelledStatement<Yield, Await> -> LabelledStatement :
    LabelIdentifier ':' LabelledItem ;

LabelledItem<Yield, Await> :
    Statement
  | FunctionDeclaration
;

ThrowStatement<Yield, Await> -> ThrowStatement :
    'throw' .noLineBreak Expression<+In> ';'
;

TryStatement<Yield, Await> -> TryStatement :
    'try' Block Catch
  | 'try' Block Catch? Finally
;

Catch<Yield, Await> -> Catch :
    'catch' ('(' CatchParameter ')')? Block
;

Finally<Yield, Await> -> Finally :
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

FunctionDeclaration -> Function :
    'function' BindingIdentifier? FormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await> ;

FunctionExpression -> FunctionExpression :
    'function' BindingIdentifier? FormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await> ;

UniqueFormalParameters<Yield, Await> :
    FormalParameters ;

FunctionBody<Yield, Await> -> Body :
    '{' .recoveryScope StatementList? '}'
  | ';'
;

ArrowFunction<In> -> ArrowFunction :
    BindingIdentifier .noLineBreak '=>' ConciseBody
  | (?= StartOfArrowFunction) FormalParameters<~Yield, ~Await> .noLineBreak '=>' ConciseBody
;

ArrowParameters:
    BindingIdentifier
  | FormalParameters<~Yield, ~Await>
;

ConciseBody<In> :
    AssignmentExpression<~Yield, ~Await, +NoObjLiteral>           -> ConciseBody
  | FunctionBody<~Yield, ~Await>
;

StartOfArrowFunction:
    BindingIdentifier '=>'
  | TypeParameters? ParameterList<~Yield, ~Await> TypeAnnotation? '=>'
;

AsyncArrowFunction<In> -> AsyncArrowFunction :
    'async' .afterAsync .noLineBreak (?= StartOfArrowFunction) ArrowParameters .noLineBreak '=>' AsyncConciseBody ;

# AsyncArrowHead :
#      'async' .noLineBreak ArrowFormalParameters<~Yield, +Await> ;

AsyncConciseBody<In> :
    AssignmentExpression<~Yield, +Await, +NoObjLiteral>           -> ConciseBody
  | AsyncFunctionBody
;

MethodDefinition<Yield, Await> -> MethodDefinition /* interface */:
    PropertyName '?'? UniqueFormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await> -> Method
  | GeneratorMethod
  | AsyncMethod
  | 'get' PropertyName '(' ')' TypeAnnotationopt FunctionBody<~Yield, ~Await>             -> Getter
  | 'set' PropertyName '(' PropertySetParameterList ')'  FunctionBody<~Yield, ~Await>     -> Setter
;

PropertySetParameterList :
    Parameter<~Yield, ~Await> ;

GeneratorMethod<Yield, Await> -> GeneratorMethod :
    '*' PropertyName UniqueFormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorDeclaration -> Generator :
    'function' '*' BindingIdentifier? FormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorExpression -> GeneratorExpression :
    'function' '*' BindingIdentifier/* no yield*/? FormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorBody :
    FunctionBody<+Yield, ~Await> ;

YieldExpression<In, Await> -> Yield :
    'yield'
  | 'yield' .afterYield .noLineBreak AssignmentExpression<+Yield>
  | 'yield' .afterYield .noLineBreak '*' AssignmentExpression<+Yield>
;

AsyncMethod<Yield, Await> -> AsyncMethod :
    'async' .afterAsync .noLineBreak PropertyName UniqueFormalParameters<~Yield, +Await> AsyncFunctionBody ;

AsyncFunctionDeclaration<Await> -> AsyncFunction :
    'async' .afterAsync .noLineBreak 'function' BindingIdentifier? FormalParameters<~Yield> AsyncFunctionBody ;

AsyncFunctionExpression -> AsyncFunctionExpression :
    'async' .afterAsync .noLineBreak 'function' BindingIdentifier/* no await*/? FormalParameters<~Yield, +Await> AsyncFunctionBody ;

AsyncFunctionBody :
    FunctionBody<~Yield, +Await> ;

AwaitExpression<Yield> -> AwaitExpression :
    'await' UnaryExpression<+Await> ;

ClassDeclaration<Yield, Await> -> Declaration /* interface */:
    Modifiers? 'class' BindingIdentifier<+WithoutImplements>? TypeParametersopt ClassTail   -> Class
;

ClassExpression<Yield, Await> -> ClassExpr :
    Modifiers? 'class' BindingIdentifier<+WithoutImplements>? TypeParameters? ClassTail
;

ClassTail<Yield, Await> :
    ClassHeritage ClassBody ;

ClassHeritage<Yield, Await>:
    ClassExtendsClause? ImplementsClause? ;

StartOfExtendsTypeRef:
    TypeReference ('implements' | '{') ;

ClassExtendsClause<Yield, Await> -> Extends:
    'extends' (?= StartOfExtendsTypeRef) TypeReference
  | 'extends' (?= !StartOfExtendsTypeRef) LeftHandSideExpression
;

ImplementsClause -> TsImplementsClause:
    'implements' (TypeReference separator ',')+ ;

ClassBody<Yield, Await> -> ClassBody :
    '{' .recoveryScope ClassElementList? '}' ;

ClassElementList<Yield, Await> :
    ClassElement
  | ClassElementList ClassElement
;

%interface Modifier;

Modifier -> Modifier:
    AccessibilityModifier
  | Decorator<~Await, ~Yield>
  | 'static'                -> Static
  | 'abstract'              -> Abstract
  | 'readonly'              -> Readonly
;

Modifiers:
    Modifier
  | Modifiers Modifier
;

ClassElement<Yield, Await> -> ClassElement /* interface */:
    Modifiers? MethodDefinition                 -> MemberMethod
  | Modifiers? PropertyName ('?'|'!')? TypeAnnotationopt Initializeropt<+In> ';' -> MemberVar
  | IndexSignature ';'                          -> TsIndexMemberDeclaration
  | ';'                                         -> EmptyDecl
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

ModuleItem -> ModuleItem /* interface */:
    ImportDeclaration
  | ImportRequireDeclaration
  | ExportDeclaration
  | StatementListItem<~Yield, ~Await>
;

ImportDeclaration -> ImportDeclaration :
    'import' ImportClause FromClause ';'
  | 'import' ModuleSpecifier ';'
;

ImportRequireDeclaration -> TsImportRequireDeclaration:
    'export'? 'import' BindingIdentifier '=' 'require' '(' StringLiteral ')' ';' ;

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

NamedImport -> NamedImport /* interface */:
    ImportedBinding                                   -> ImportSpecifier
  | IdentifierNameRef 'as' ImportedBinding            -> ImportSpecifier
  | error                                             -> SyntaxProblem
;

ModuleSpecifier -> ModuleSpecifier :
    StringLiteral ;

ImportedBinding :
    BindingIdentifier ;

ExportDeclaration -> ModuleItem /* interface */:
    'export' '*' FromClause ';'                       -> ExportDeclaration
  | 'export' ExportClause FromClause ';'              -> ExportDeclaration
  | 'export' ExportClause ';'                         -> ExportDeclaration
  | 'export' VariableStatement<~Yield, ~Await>        -> ExportDeclaration
  | Modifiers? 'export' Declaration<~Yield, ~Await>              -> ExportDeclaration
  | 'export' 'default' HoistableDeclaration<~Await>                                -> ExportDefault
  | Modifiers? 'export' 'default' ClassDeclaration<~Yield, ~Await>                            -> ExportDefault
  | 'export' 'default' AssignmentExpression<+In, ~Yield, ~Await, +NoFuncClass> ';' -> ExportDefault
  | 'export' '=' IdentifierReference<~Yield, ~Await> ';'     -> TsExportAssignment
  | 'export' 'as' 'namespace' BindingIdentifier ';'   -> TsNamespaceExportDeclaration
;

ExportClause -> ExportClause :
    '{' '}'
  | '{' (ExportElement separator ',')+ ','? '}'
;

ExportElement -> ExportElement /* interface */:
    IdentifierNameRef                                 -> ExportSpecifier
  | IdentifierNameRef 'as' IdentifierNameDecl         -> ExportSpecifier
  | error                                             -> SyntaxProblem
;

# ES next: https://github.com/tc39/proposal-decorators

%interface Decorator;

Decorator<Yield, Await> -> Decorator /* interface */:
    '@' DecoratorMemberExpression       -> DecoratorExpr
  | '@' DecoratorCallExpression         -> DecoratorCall
;

DecoratorMemberExpression<Yield, Await>:
    IdentifierReference
  | DecoratorMemberExpression '.' IdentifierName
;

DecoratorCallExpression<Yield, Await>:
  DecoratorMemberExpression Arguments ;

# Extensions

# JSX (see https://facebook.github.io/jsx/)

%interface JSXAttribute, JSXAttributeValue, JSXChild;

JSXElement<Yield, Await> -> JSXElement :
    JSXSelfClosingElement
  | JSXOpeningElement JSXChild* JSXClosingElement
;

JSXSelfClosingElement<Yield, Await> -> JSXSelfClosingElement :
    '<' JSXElementName TypeArguments? JSXAttribute* '/' '>' ;

JSXOpeningElement<Yield, Await> -> JSXOpeningElement :
    '<' JSXElementName TypeArguments? JSXAttribute* '>' ;

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

JSXAttribute<Yield, Await> -> JSXAttribute /* interface */:
    JSXAttributeName ('=' JSXAttributeValue)?         -> JSXNormalAttribute
  | '{' .recoveryScope '...' AssignmentExpression<+In> '}'           -> JSXSpreadAttribute
;

JSXAttributeName -> JSXAttributeName :
    jsxIdentifier
  | jsxIdentifier ':' jsxIdentifier
;

JSXAttributeValue<Yield, Await> -> JSXAttributeValue /* interface */:
    jsxStringLiteral                                  -> JSXLiteral
  | '{' .recoveryScope AssignmentExpression<+In> '}'                 -> JSXExpression
  | JSXElement
;

JSXChild<Yield, Await> -> JSXChild /* interface */:
    jsxText                                           -> JSXText
  | JSXElement
  | '{' .recoveryScope AssignmentExpressionopt<+In> '}'              -> JSXExpression
  | '{' .recoveryScope '...' AssignmentExpressionopt<+In> '}'        -> JSXSpreadExpression
;

# Typescript

# Note: yield and await do not apply to type contexts.

# A.1 Types

%interface TsType, TypeMember;

%flag AllowQuest = true;

Type<AllowQuest> -> TsType /* interface */:
    UnionOrIntersectionOrPrimaryType %prec resolveShift
  | [AllowQuest] check=UnionOrIntersectionOrPrimaryType 'extends' ext=Type<~AllowQuest> '?' truet=Type ':' falset=Type  -> TsConditional
  | FunctionType
  | ConstructorType
  | paramref=IdentifierNameRef 'is' Type -> TypePredicate
;

TypeParameters -> TypeParameters :
    '<' (TypeParameter separator ',')+ '>' ;

TypeParameter -> TypeParameter :
    BindingIdentifier Constraint? ('=' Type)?;

Constraint -> TypeConstraint :
    'extends' Type ;

TypeArguments -> TypeArguments :
    '<' (Type separator ',')+ '>' ;

UnionOrIntersectionOrPrimaryType<AllowQuest> -> TsType /* interface */:
    inner+=UnionOrIntersectionOrPrimaryType? '|' inner+=IntersectionOrPrimaryType -> UnionType
  | IntersectionOrPrimaryType %prec resolveShift
;

IntersectionOrPrimaryType<AllowQuest> -> TsType /* interface */:
    inner+=IntersectionOrPrimaryType? '&' inner+=TypeOperator -> IntersectionType
  | TypeOperator
;

TypeOperator<AllowQuest> -> TsType /* interface */:
    PrimaryType
  | 'keyof' TypeOperator      -> KeyOfType
  | 'unique' TypeOperator     -> UniqueType
  | 'readonly' TypeOperator   -> ReadonlyType
  | 'infer' IdentifierName    -> TypeVar
;

PrimaryType<AllowQuest> -> TsType /* interface */:
    ParenthesizedType
  | PredefinedType
  | TypeReference
  | ObjectType
  | MappedType
  | ArrayType
  | IndexedAccessType
  | LiteralType
  | TupleType
  | TypeQuery
  | ImportType
  | 'this'                                        -> ThisType
  | PrimaryType .noLineBreak '!'                  -> NonNullableType
  | [AllowQuest] PrimaryType .noLineBreak '?'     -> NullableType
;

ParenthesizedType -> ParenthesizedType :
    '(' (?= !StartOfFunctionType) Type ')' ;

# 2.0
LiteralType -> LiteralType :
    StringLiteral
  | '-'? NumericLiteral
  | 'null'
  | 'true'
  | 'false'
;

PredefinedType -> PredefinedType :
    'any'
  | 'unknown'
  | 'number'
  | 'boolean'
  | 'string'
  | 'symbol'
  | 'void'
  # TODO add bigint, undefined, never, object
;

TypeReference -> TypeReference :
    TypeName .noLineBreak TypeArguments? %prec resolveShift ;

TypeName -> TypeName :
    ref+=IdentifierReference<+WithoutPredefinedTypes, ~Yield, ~Await>
  | NamespaceName '.' ref+=IdentifierReference<~Yield, ~Await>
;

NamespaceName :
    ref+=IdentifierReference<~Yield, ~Await>
  | NamespaceName '.' ref+=IdentifierReference<~Yield, ~Await>
;

ObjectType -> ObjectType :
    '{' .recoveryScope (?= !StartOfMappedType) TypeBody? '}' ;

TypeBody :
    TypeMemberList
  | TypeMemberList ','
  | TypeMemberList ';'
;

TypeMemberList :
    TypeMember
  | TypeMemberList ';' TypeMember
  | TypeMemberList ',' TypeMember
;

TypeMember -> TypeMember /* interface */:
    PropertySignature
  | MethodSignature
  | CallSignature
  | ConstructSignature
  | IndexSignature
;

ArrayType<AllowQuest> -> ArrayType :
    PrimaryType .noLineBreak '[' ']' ;

# 2.1
IndexedAccessType<AllowQuest> -> IndexedAccessType :
    left=PrimaryType .noLineBreak '[' index=Type ']' ;

# 2.1
StartOfMappedType :
    ('+' | '-') 'readonly'
  | 'readonly'? '[' IdentifierName 'in'
;

# 2.1
MappedType -> MappedType :
    '{' .recoveryScope (?= StartOfMappedType) (('+'|'-')? 'readonly')? '[' Identifier 'in' Type ']' (('+'|'-')? '?')? TypeAnnotation ';'? '}' ;

TupleType -> TupleType :
    '[' (TupleElementType separator ',')+? ']' ;

TupleElementType -> TsType:
    Type
  | '...' Type  -> RestType
;

# This lookahead rule disambiguates FunctionType vs ParenthesizedType
# productions by enumerating all prefixes of FunctionType that would
# lead to parse failure if interpreted as Type.
# (partially inspired by isUnambiguouslyStartOfFunctionType() in
# src/compiler/parser.ts)
StartOfFunctionType :
    Modifiers? BindingIdentifier (':' | ',' | '?' | '=' | ')' '=>')
  | Modifiers? BindingPattern<~Yield, ~Await> (':' | ',' | '?' | '=' | ')' '=>')
  | '...'
  | 'this' ':'
  | ')'
;

FunctionType<AllowQuest> -> FunctionType :
    TypeParameters? FunctionTypeParameterList '=>' Type ;

FunctionTypeParameterList -> Parameters :
    '(' (?= StartOfFunctionType) (Parameter<~Yield, ~Await> separator ',')+? ','? ')' ;

ConstructorType<AllowQuest> -> ConstructorType :
    'new' TypeParameters? ParameterList<~Yield, ~Await> '=>' Type ;

%left 'keyof' 'typeof' 'unique' 'readonly' 'infer';
%nonassoc 'is';

TypeQuery -> TypeQuery :
    'typeof' TypeQueryExpression ;

# 2.9
ImportType -> ImportType :
    'typeof'? 'import' '(' Type ')' ('.' IdentifierReference<~Yield, ~Await> )+? .noLineBreak TypeArguments? %prec resolveShift ;

TypeQueryExpression :
    IdentifierReference<~Yield, ~Await>
  | TypeQueryExpression '.' IdentifierName
;

PropertySignature -> PropertySignature :
    Modifiers? PropertyName<+WithoutNew, ~Yield, ~Await> '?'? TypeAnnotation? ;

TypeAnnotation -> TypeAnnotation :
    ':' Type ;

FormalParameters<Yield, Await> :
    TypeParameters? ParameterList TypeAnnotation? ;

CallSignature -> CallSignature :
    TypeParameters? ParameterList<~Yield, ~Await> TypeAnnotation? ;

ParameterList<Yield, Await> -> Parameters :
    '(' (Parameter separator ',')+? ','? ')' ;

%interface Parameter;

Parameter<Yield, Await> -> Parameter :
    Modifiers? BindingIdentifier '?'? TypeAnnotation?              -> DefaultParameter
  | Modifiers? BindingPattern '?'? TypeAnnotation?                 -> DefaultParameter
  | Modifiers? BindingIdentifier TypeAnnotation? Initializer<+In>  -> DefaultParameter
  | Modifiers? BindingPattern TypeAnnotation? Initializer<+In>     -> DefaultParameter
  | '...' BindingIdentifier TypeAnnotation?                        -> RestParameter
  | 'this' TypeAnnotation                                          -> TsThisParameter
  | SyntaxError
;

AccessibilityModifier -> AccessibilityModifier :
    'public'
  | 'private'
  | 'protected'
;

BindingIdentifierOrPattern<Yield, Await> :
    BindingIdentifier
  | BindingPattern
;

ConstructSignature -> ConstructSignature :
    Modifiers? 'new' TypeParameters? ParameterList<~Yield, ~Await> TypeAnnotation? ;

# Note: using IdentifierName instead of BindingIdentifier to avoid r/r
# conflicts with ComputedPropertyName.
IndexSignature -> IndexSignature :
    Modifiers? '[' IdentifierName ':' 'string' ']' TypeAnnotation
  | Modifiers? '[' IdentifierName ':' 'number' ']' TypeAnnotation
;

MethodSignature -> MethodSignature :
    Modifiers? PropertyName<+WithoutNew, ~Yield, ~Await> '?'? FormalParameters<~Yield, ~Await> ;

TypeAliasDeclaration -> TypeAliasDeclaration :
    'type' BindingIdentifier TypeParameters? '=' Type ';' ;

# A.5 Interfaces

InterfaceDeclaration -> TsInterface:
    'interface' BindingIdentifier TypeParametersopt InterfaceExtendsClause? ObjectType ;

InterfaceExtendsClause -> TsInterfaceExtends:
    'extends' (TypeReference separator ',')+ ;

# A.7 Enums

EnumDeclaration -> TsEnum:
    'const'? 'enum' BindingIdentifier EnumBody ;

EnumBody -> TsEnumBody:
    '{' .recoveryScope ((EnumMember separator ',')+ ','?)? '}' ;

EnumMember -> TsEnumMember:
    PropertyName<~Yield, ~Await>
  | PropertyName<~Yield, ~Await> '=' AssignmentExpression<+In, ~Yield, ~Await>
;

# A.8 Namespaces

NamespaceDeclaration -> TsNamespace:
    ('namespace' | 'module') IdentifierPath NamespaceBody ;

IdentifierPath:
    BindingIdentifier
  | IdentifierPath '.' BindingIdentifier
;

NamespaceBody -> TsNamespaceBody:
    '{' .recoveryScope ModuleItemList? '}' ;

ImportAliasDeclaration -> TsImportAliasDeclaration:
    'import' BindingIdentifier '=' EntityName ';' ;

EntityName:
    NamespaceName ;

# A.10 Ambients

%interface TsAmbientElement, TsAmbientClassElement;

AmbientDeclaration -> TsAmbientElement /* interface */:
    'declare' AmbientVariableDeclaration        -> TsAmbientVar
  | 'declare' AmbientFunctionDeclaration        -> TsAmbientFunction
  | 'declare' AmbientClassDeclaration           -> TsAmbientClass
  | 'declare' AmbientInterfaceDeclaration       -> TsAmbientInterface
  | 'declare' AmbientEnumDeclaration            -> TsAmbientEnum
  | 'declare' AmbientNamespaceDeclaration       -> TsAmbientNamespace
  | 'declare' AmbientModuleDeclaration          -> TsAmbientModule
  | 'declare' TypeAliasDeclaration              -> TsAmbientTypeAlias
;

AmbientVariableDeclaration:
    'var' AmbientBindingList ';'
  | 'let' AmbientBindingList ';'
  | 'const' AmbientBindingList ';'
;

AmbientBindingList:
    AmbientBinding
  | AmbientBindingList ',' AmbientBinding
;

AmbientBinding -> TsAmbientBinding:
    BindingIdentifier TypeAnnotation? Initializer<+In, ~Yield, ~Await>? ;

AmbientFunctionDeclaration:
    'function' BindingIdentifier FormalParameters<~Yield, ~Await> ';' ;

AmbientClassDeclaration:
    Modifiers? 'class' BindingIdentifier TypeParametersopt ClassHeritage<~Yield, ~Await> AmbientClassBody ;

AmbientInterfaceDeclaration:
    Modifiers? 'interface' BindingIdentifier TypeParametersopt InterfaceExtendsClause? ObjectType ;

AmbientClassBody -> TsAmbientClassBody:
    '{' .recoveryScope AmbientClassBodyElement+? '}' ;

AmbientClassBodyElement -> TsAmbientClassElement /* interface */:
    Modifiers? PropertyName<~Yield, ~Await> '?'? TypeAnnotationopt ';'                -> TsAmbientPropertyMember
  | Modifiers? PropertyName<~Yield, ~Await> '?'? FormalParameters<~Yield, ~Await> ';' -> TsAmbientFunctionMember
  | IndexSignature ';' -> TsAmbientIndexMember
;

AmbientEnumDeclaration:
    EnumDeclaration ;

AmbientNamespaceDeclaration:
    'namespace' IdentifierPath AmbientNamespaceBody ;

AmbientModuleDeclaration:
    'module' (StringLiteral | IdentifierPath) ('{' .recoveryScope ModuleBodyopt '}' | ';') ;

AmbientNamespaceBody:
    '{' .recoveryScope AmbientNamespaceElement+? '}' ;

AmbientNamespaceElement -> TsAmbientElement /* interface */:
    'export'? AmbientVariableDeclaration    -> TsAmbientVar
  | 'export'? AmbientFunctionDeclaration    -> TsAmbientFunction
  | 'export'? AmbientClassDeclaration       -> TsAmbientClass
  | 'export'? AmbientInterfaceDeclaration   -> TsAmbientInterface
  | 'export'? AmbientEnumDeclaration        -> TsAmbientEnum
  | 'export'? AmbientNamespaceDeclaration   -> TsAmbientNamespace
  | 'export'? AmbientModuleDeclaration      -> TsAmbientModule
  | 'export'? ImportAliasDeclaration        -> TsAmbientImportAlias
  | 'export'? TypeAliasDeclaration          -> TsAmbientTypeAlias
;

%%

${template go_lexer.lexerType}
type Dialect int

const (
	Javascript Dialect = iota
	Typescript
	TypescriptJsx
)
${call base-}
${end}

${template go_lexer.stateVars-}
	Dialect Dialect
	token   Token // last token
	Stack   []int // stack of JSX states, non-empty for StateJsx*
${end}

${template go_lexer.initStateVars-}
	l.Dialect = Javascript
	l.token = UNAVAILABLE
	l.Stack = nil
${end}

${template go_parser.setupLookaheadLexer-}
	var lexer Lexer
	lexer.source = l.source
	lexer.ch= l.ch
	lexer.offset= l.offset
	lexer.tokenOffset = l.tokenOffset
	lexer.line = l.line
	lexer.tokenLine = l.tokenLine
	lexer.scanOffset = l.scanOffset
	lexer.State = l.State
	lexer.Dialect = l.Dialect
	lexer.token = l.token
	// Note: Stack is intentionally omitted.
${end}

${template go_lexer.onBeforeNext-}
	prevLine := l.tokenLine
${end}

${template go_lexer.onAfterNext-}

	// There is an ambiguity in the language that a slash can either represent
	// a division operator, or start a regular expression literal. This gets
	// disambiguated at the grammar level - division always follows an
	// expression, while regex literals are expressions themselves. Here we use
	// some knowledge about the grammar to decide whether the next token can be
	// a regular expression literal.
	//
	// See the following thread for more details:
	// http://stackoverflow.com/questions/5519596/when-parsing-javascript-what

	if l.State <= StateTemplateExprDiv {
		// The lowest bit of "l.State" determines how to interpret a forward
		// slash if it happens to be the next character.
		//   unset: start of a regular expression literal
		//   set:   start of a division operator (/ or /=)
		switch token {
		case NEW, DELETE, VOID, TYPEOF, INSTANCEOF, IN, DO, RETURN, CASE, THROW, ELSE:
			l.State &^= 1
		case TEMPLATEHEAD:
			l.State |= 1
			l.pushState(StateTemplate)
		case TEMPLATEMIDDLE:
			l.State = StateTemplate
		case TEMPLATETAIL:
			l.popState()
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
				if l.Dialect != Typescript {
					l.State |= 1
					l.pushState(StateJsxTag)
				}
			} else {
				l.State &^= 1
			}
		case LBRACE:
			l.State &^= 1
			if l.State >= StateTemplate {
				l.pushState(StateTemplateExpr)
			}
		case RBRACE:
			l.State &^= 1
			if l.State >= StateTemplate {
				l.popState()
			}
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
			if l.State == StateJsxTag && l.token == LT {
				l.State = StateJsxClosingTag
				if len(l.Stack) > 0 {
					l.Stack = l.Stack[:len(l.Stack)-1]
				}
			}
		case GT:
			if l.State == StateJsxTypeArgs || l.State == StateJsxClosingTag || l.token == DIV {
				l.popState()
			} else {
				l.State = StateJsxText
			}
		case LBRACE:
			if l.State != StateJsxTypeArgs {
				l.pushState(StateTemplateExpr)
			}
		case LT:
			if l.Dialect == TypescriptJsx && l.State != StateJsxText && l.token != ASSIGN {
				// Type arguments.
				l.pushState(StateJsxTypeArgs)
			} else {
				// Start a new JSX tag.
				l.pushState(StateJsxTag)
			}
		}
	}
	l.token = token
${end}

${template go_lexer.lexer-}
${call base-}

func (l *Lexer) pushState(newState int) {
	l.Stack = append(l.Stack, l.State)
	l.State = newState
}

func (l *Lexer) popState() {
	if ln := len(l.Stack); ln > 0 {
		l.State = l.Stack[ln-1]
		l.Stack = l.Stack[:ln-1]
	} else {
		l.State = StateDiv
	}
}
${end}

${template go_parser.parser-}
package ${self->go.package()}

${call errorHandler}
${call SyntaxError}
${foreach inp in syntax.input.select(it|it.requested)-}
func (p *Parser) Parse${self->util.onlyOneUserInput() ? '' : util.toFirstUpper(inp.target.id)}(${call contextParam}lexer *Lexer) error {
	return p.parse(${call contextArg}${inp.index}, ${parser.finalStates[inp.index]}, lexer)
}

${if self->needExplicitLookahead()-}
${call lookahead}
${end-}
${end-}
${call lalr}
${call gotoState}
${call applyRule}
${if self->go_parser.hasRecovering()-}
const errSymbol = ${syntax.error.index}

${call skipBrokenCode}
${call willShift}
${end-}
${if self->ignoredReportTokens()-}
${call reportIgnoredToken}
${end-}
${end}

${template go_parser.lookaheadNext}${end}
${template go_parser.callLookaheadNext(memoization)}lookaheadNext(&lexer, end, ${memoization?'nil /*empty stack*/':'stack'})${end}

${template newTemplates-}
{{define "onBeforeLexer"}}
type Dialect int

const (
	Javascript Dialect = iota
	Typescript
	TypescriptJsx
)
{{end}}

{{define "onAfterLexer"}}
func (l *Lexer) pushState(newState int) {
	l.Stack = append(l.Stack, l.State)
	l.State = newState
}

func (l *Lexer) popState() {
	if ln := len(l.Stack); ln > 0 {
		l.State = l.Stack[ln-1]
		l.Stack = l.Stack[:ln-1]
	} else {
		l.State = StateDiv
	}
}
{{end}}
${end}
