# ECMAScript 2016 Language Grammar (Standard ECMA-262, 7th Edition / June 2016)
# This grammar also covers:
#   JSX - a popular language extension for React.
#   TypeScript 2.8 (see typescriptlang.org)
language js(go);

lang = "js"
package = "github.com/inspirer/textmapper/parsers/js"
nonBacktracking = true
eventBased = true
eventFields = true
eventAST = true
writeBison = true
fileNode = "Module"
cancellable = true
recursiveLookaheads = true
optimizeTables = true
extraTypes = ["InsertedSemicolon"]
customImpl = ["fetchNext", "Parser", "ParserInit", "parse", "recoverFromError", "session", "lexerCopy", "streamNext"]
tokenStream = true

:: lexer

%s initial, div, template, templateDiv, templateExpr, templateExprDiv, jsxTypeArgs;
%x jsxTag, jsxClosingTag, jsxText;

# Accept end-of-input in all states.
<*> eoi: /{eoi}/

invalid_token:
error:

<initial, div, template, templateDiv, templateExpr, templateExprDiv, jsxTag, jsxTypeArgs, jsxClosingTag> {
  WhiteSpace: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)

  # LineTerminatorSequence
  WhiteSpace: /[\n\r\u2028\u2029]|\r\n/ (space)

  commentChars = /([^*]|\*+[^*\/])*\**/
  MultiLineComment:  /\/\*{commentChars}\*\//     (space)
  # Note: the following rule disables backtracking for incomplete multiline comments, which
  # would otherwise be reported as '/', '*', etc.
  invalid_token: /\/\*{commentChars}/
  SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/   (space)
}

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
PrivateIdentifier: /#{identifierStart}{identifierPart}*/
# Note: the following rule disables backtracking for incomplete identifiers.
invalid_token: /#?({identifierStart}{identifierPart}*)?{brokenEscapeSequence}/

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
'as':      /as/
'assert':  /assert/
'asserts': /asserts/
'async':   /async/
'from':    /from/
'get':     /get/
'let':     /let/
'of':      /of/
'set':     /set/
'static':  /static/
'target':  /target/
'satisfies': /satisfies/

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
'bigint': /bigint/
'undefined': /undefined/
'never': /never/
'object': /object/

# The following keywords have special meaning in certain contexts, but are
# valid identifiers:
'abstract':    /abstract/
'constructor': /constructor/
'declare':     /declare/
'global':      /global/
'is':          /is/
'module':      /module/
'namespace':   /namespace/
'override':    /override/
'require':     /require/
'type':        /type/
'accessor':    /accessor/

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
invalid_token: /\?\.[0-9]/   { l.rewind(l.tokenOffset+1); tok = token.QUEST }
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
'??=': /\?\?=/
'||=': /\|\|=/
'&&=': /&&=/

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

%input Module, TypeSnippet, ExpressionSnippet, NamespaceNameSnippet;

%inject MultiLineComment -> MultiLineComment;
%inject SingleLineComment -> SingleLineComment;
%inject invalid_token -> InvalidToken;
%inject NoSubstitutionTemplate -> NoSubstitutionTemplate;
%inject TemplateHead -> TemplateHead;
%inject TemplateMiddle -> TemplateMiddle;
%inject TemplateTail -> TemplateTail;

TypeSnippet :
    Type ;

ExpressionSnippet :
    Expression<+In, ~Yield, ~Await> ;

NamespaceNameSnippet -> TsNamespaceName :
  NamespaceName ;

%assert empty set(follow error & ~('}' | ')' | ',' | ';' | ']'));

%generate beforeSemi = set(precede ';');
%generate beforeMemberExpr = set(precede MemberExpression<+Await, +Yield>);

%flag In;
%flag Yield;
%flag Await;
%flag NoAsync = false;
%flag WithDefault = false;

%flag WithoutNew = false;
%flag WithoutAsserts = false;
%flag WithoutKeywords = false;
%flag WithoutPredefinedTypes = false;
%flag WithoutImplements = false;
%flag WithoutFrom = false;
%flag WithoutAs = false;
%flag WithoutSatisfies = false;

%lookahead flag NoLet = false;
%lookahead flag NoLetSq = false;
%lookahead flag NoObjLiteral = false;
%lookahead flag NoFuncClass = false;
%lookahead flag NoAs = false;
%lookahead flag NoSatisfies = false;
%lookahead flag StartWithLet = false;

SyntaxError -> SyntaxProblem :
    error ;

IdentifierName<WithoutNew, WithoutAsserts, WithoutKeywords, WithoutFrom, WithoutAs, WithoutSatisfies> :
    Identifier

# Keywords
  | [!WithoutNew] 'new'
  | [!WithoutAsserts] 'asserts'
  | [!WithoutKeywords] ('await'
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
    | 'null' | 'true' | 'false')

  # Soft keywords
  | [!WithoutAs] 'as' | [!WithoutFrom] 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async' | 'assert'

  # Typescript.
  | 'implements' | 'interface' | 'private' | 'protected' | 'public'
  | 'any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol' | 'bigint'
  | 'undefined' | 'never' | 'object' | 'abstract' | 'constructor' | 'declare'
  | 'is' | 'module' | 'namespace' | 'override' | 'require' | 'type' | 'global'
  | 'readonly' | 'keyof' | 'unique' | 'infer' | 'accessor'
  | [!WithoutSatisfies] 'satisfies'
;

IdentifierNameDecl<WithoutNew> -> NameIdent:
    IdentifierName ;

IdentifierNameRef<WithoutAsserts> -> ReferenceIdent :
    IdentifierName ;

ClassPrivateRef -> ReferenceIdent:
    PrivateIdentifier ;

# A.2 Expressions

IdentifierReference<Yield, Await, NoAsync, WithoutPredefinedTypes, WithDefault> -> ReferenceIdent :
# V8 runtime functions start with a percent sign.
# See http://stackoverflow.com/questions/11202824/what-is-in-javascript
    Identifier
  | '%' Identifier
  | [!Yield] 'yield'
  | [!Await] 'await'
  | [!NoLet] 'let'
  | [WithDefault] 'default'
  | [!NoAsync] 'async' (?= !StartOfArrowFunction)

  # Soft keywords
  | 'as' | 'assert' | 'asserts' | 'from' | 'get' | 'of' | 'set' | 'static' | 'target'

  # Typescript.
  | 'implements' | 'interface' | 'private' | 'protected' | 'public' | 'satisfies'
  | [!WithoutPredefinedTypes] ('any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol'
    | 'bigint' | 'undefined' | 'never' | 'object')
  | 'abstract' | 'constructor' | 'declare' | 'is' | 'module' | 'namespace' | 'override'
  | 'require' | 'type' | 'global' | 'accessor'
  | [!WithoutPredefinedTypes] ('keyof' | 'unique' | 'readonly' | 'infer')
;

BindingIdentifier<WithoutImplements> -> NameIdent :
    Identifier

  # These are allowed or not, depending on the context.
  | 'yield' | 'await'

  # Soft keywords
  | 'as' | 'assert' | 'asserts' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async'

  # Typescript.
  | [!WithoutImplements] 'implements'
  | 'interface' | 'private' | 'protected' | 'public'
  | 'any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol' | 'bigint'
  | 'undefined' | 'never' | 'object' | 'abstract' | 'constructor' | 'declare'
  | 'is' | 'module' | 'namespace' | 'override' | 'require' | 'type' | 'global'
  | 'readonly' | 'keyof' | 'unique' | 'infer' | 'satisfies' | 'accessor'
;

LabelIdentifier -> LabelIdent :
    Identifier

  # These are allowed or not, depending on the context.
  | 'yield' | 'await'

  # Soft keywords
  | 'as' | 'assert' | 'asserts' | 'from' | 'get' | 'let' | 'of' | 'set' | 'static' | 'target' | 'async'

  # Typescript.
  | 'implements' | 'interface' | 'private' | 'protected' | 'public'
  | 'any' | 'unknown' | 'boolean' | 'number' | 'string' | 'symbol' | 'bigint'
  | 'undefined' | 'never' | 'object' | 'declare' | 'is' | 'module' | 'namespace' | 'override'
  | 'require' | 'type' | 'global' | 'readonly' | 'keyof' | 'unique' | 'infer' | 'satisfies' | 'accessor'
;

PrimaryExpression<Yield, Await, NoAsync> -> Expr /* interface */:
    ('this' -> This)                                       -> ThisExpr
  | IdentifierReference                                    -> IdentExpr
  | Literal
  | ArrayLiteral
  | [!NoObjLiteral] ObjectLiteral
  | [!NoFuncClass] FunctionExpression
  | [!NoFuncClass] ClassExpression
  | [!NoFuncClass] GeneratorExpression
  | [!NoFuncClass] AsyncFunctionExpression
  | [!NoFuncClass] AsyncGeneratorExpression
  | RegularExpressionLiteral                               -> Regexp
  | TemplateLiteral
  | (?= !StartOfArrowFunction) Parenthesized
  | (?= !StartOfArrowFunction) JSXElement
  | (?= !StartOfArrowFunction) JSXFragment
;

Parenthesized<Yield, Await> -> Parenthesized:
    '(' Expression<+In> ')'
  | '(' BrokenExpr ')'
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
  | '[' ElementList ']'
  | '[' ElementList ',' Elisionopt ']'
;

ElementList<Yield, Await> :
    Elisionopt list+=AssignmentExpression<+In>
  | Elisionopt list+=SpreadElement
  | ElementList ',' Elisionopt list+=AssignmentExpression<+In>
  | ElementList ',' Elisionopt list+=SpreadElement
;

Elision :
    list+=(',' -> NoElement as Expr)
  | Elision list+=(',' -> NoElement as Expr)
;

SpreadElement<Yield, Await> -> Expr /* interface */:
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
    (IdentifierReference -> NameIdent)                    -> ShorthandProperty
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
  | (PrivateIdentifier -> NameIdent)
  | (StringLiteral -> Literal)
  | (NumericLiteral -> Literal)
;

ComputedPropertyName<Yield, Await> -> ComputedPropertyName :
    '[' AssignmentExpression<+In> ']' ;

CoverInitializedName<Yield, Await> :
    IdentifierReference Initializer<+In> ;

Initializer<In, Yield, Await> -> Initializer :
    '=' (AssignmentExpression | BrokenExpr)
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

TemplateLiteralType -> TsTemplateLiteralType :
    template+=NoSubstitutionTemplate
  | template+=TemplateHead substitution+=Type TemplateSpansType
;

TemplateSpansType :
    template+=TemplateTail
  | TemplateMiddleListType template+=TemplateTail
;

TemplateMiddleListType :
    template+=TemplateMiddle substitution+=Type
  | TemplateMiddleListType template+=TemplateMiddle substitution+=Type
;

MemberExpression<Yield, Await, NoAsync, flag NoLetOnly = false> -> Expr /* interface */:
    [!NoLetOnly && !StartWithLet] PrimaryExpression
  | [NoLetOnly && !StartWithLet] PrimaryExpression<+NoLet>
  | [StartWithLet && !NoLetOnly] ('let' -> ReferenceIdent) -> IdentExpr
  | [StartWithLet] expr=MemberExpression<+NoLetOnly, ~NoAsync> '[' index=Expression<+In> ']'            -> IndexAccess
  | [!StartWithLet] expr=MemberExpression<NoLetOnly: NoLetSq, ~NoAsync> '[' index=Expression<+In> ']'   -> IndexAccess
  | expr=MemberExpression<~NoAsync, ~NoLetOnly> '.' selector=IdentifierNameRef        -> PropertyAccess
  | expr=MemberExpression<~NoAsync, ~NoLetOnly> '.' selector=ClassPrivateRef          -> PropertyAccess
  | tag=MemberExpression<~NoAsync, ~NoLetOnly> literal=TemplateLiteral                -> TaggedTemplate
  | expr=MemberExpression<~NoAsync, ~NoLetOnly> .noLineBreak '!'                      -> TsNonNull
  | [!StartWithLet] SuperProperty
  | [!StartWithLet] MetaProperty
  | [!StartWithLet] 'new' expr=MemberExpression<~NoAsync, ~NoLetOnly> Arguments       -> NewExpr
;

SuperExpression -> Expr /* interface */:
    ('super' -> ReferenceIdent) -> SuperExpr
;

SuperProperty<Yield, Await> -> Expr /* interface */:
    expr=SuperExpression '[' index=Expression<+In> ']'          -> IndexAccess
  | expr=SuperExpression '.' selector=IdentifierNameRef         -> PropertyAccess
;

MetaProperty :
    NewTarget ;

NewTarget -> NewTarget :
    'new' '.' 'target' ;

NewExpression<Yield, Await, NoAsync> -> Expr /* interface */:
    MemberExpression  (?= !StartOfParametrizedCall)
  | [!StartWithLet] 'new' expr=NewExpression<~NoAsync>      -> NewExpr
;

CallExpression<Yield, Await> -> Expr /* interface */:
    expr=MemberExpression Arguments                             -> CallExpr
  | [!StartWithLet] SuperCall                                   -> CallExpr
  | [!StartWithLet] 'import' Arguments                          -> TsDynamicImport
  | expr=CallExpression Arguments                               -> CallExpr
  | expr=CallExpression '[' index=Expression<+In> ']'           -> IndexAccess
  | expr=CallExpression '.' selector=IdentifierNameRef          -> PropertyAccess
  | expr=CallExpression '.' selector=ClassPrivateRef            -> PropertyAccess
  | [!StartWithLet] 'import' '.' selector=IdentifierNameRef     -> PropertyAccess
  | [!StartWithLet] 'import' '.' selector=ClassPrivateRef       -> PropertyAccess
  | expr=CallExpression .noLineBreak '!'                        -> TsNonNull
  | tag=CallExpression literal=TemplateLiteral                  -> TaggedTemplate
;

SuperCall<Yield, Await> :
    expr=SuperExpression Arguments
;

Arguments<Yield, Await> -> Arguments :
    (?= StartOfParametrizedCall) TypeArguments (?= StartLParen) '(' (list=ArgumentList ','?)? ')'
  | (?= StartOfParametrizedCall) TypeArguments (?= !StartLParen)
  | '(' (list=ArgumentList ','?)? ')'
;

StartLParen:
    '(' ;

StartOfParametrizedCall:
    TypeArguments (',' | ')' | ';' | '(') ;


ArgumentList<Yield, Await> :
    AssignmentExpression<+In>
  | SpreadElement
  | BrokenExpr
  | ArgumentList ',' AssignmentExpression<+In>
  | ArgumentList ',' SpreadElement
  | ArgumentList ',' BrokenExpr
;

BrokenExpr -> SyntaxProblem as Expr:
    error ;

OptionalLHS<Yield, Await> :
    MemberExpression  | CallExpression | OptionalExpression ;

OptionalExpression<Yield, Await> -> Expr /* interface */:
    expr=OptionalLHS '?.' '[' index=Expression<+In> ']'      -> OptionalIndexAccess
  | expr=OptionalLHS '?.' selector=IdentifierNameRef         -> OptionalPropertyAccess
  | expr=OptionalLHS '?.' selector=ClassPrivateRef           -> OptionalPropertyAccess
  | expr=OptionalLHS '?.' Arguments                          -> OptionalCallExpr
  | tag=OptionalLHS '?.' literal=TemplateLiteral             -> OptionalTaggedTemplate
  | expr=OptionalExpression '[' index=Expression<+In> ']'    -> IndexAccess
  | expr=OptionalExpression '.' selector=IdentifierNameRef   -> PropertyAccess
  | expr=OptionalExpression '.' selector=ClassPrivateRef     -> PropertyAccess
  | expr=OptionalExpression Arguments                        -> CallExpr
  | expr=OptionalExpression .noLineBreak '!'                 -> TsNonNull
  | tag=OptionalExpression literal=TemplateLiteral           -> TaggedTemplate
;

LeftHandSideExpression<Yield, Await, NoAsync> -> Expr /* interface */:
    NewExpression
  | CallExpression (?= !StartOfParametrizedCall)
  | OptionalExpression (?= !StartOfParametrizedCall)
;

UpdateExpression<Yield, Await> -> Expr /* interface */:
    LeftHandSideExpression
  | LeftHandSideExpression .noLineBreak '++'          -> PostInc
  | LeftHandSideExpression .noLineBreak '--'          -> PostDec
  | [!StartWithLet] '++' UnaryExpression              -> PreInc
  | [!StartWithLet] '--' UnaryExpression              -> PreDec
;

UnaryExpression<Yield, Await> -> Expr /* interface */:
    UpdateExpression
  | [!StartWithLet] 'delete' UnaryExpression          -> UnaryExpr
  | [!StartWithLet] 'void' UnaryExpression            -> UnaryExpr
  | [!StartWithLet] 'typeof' UnaryExpression          -> UnaryExpr
  | [!StartWithLet] '+' UnaryExpression               -> UnaryExpr
  | [!StartWithLet] '-' UnaryExpression               -> UnaryExpr
  | [!StartWithLet] '~' UnaryExpression               -> UnaryExpr
  | [!StartWithLet] '!' UnaryExpression               -> NotExpr
  | [!StartWithLet && Await] AwaitExpression
  | [!StartWithLet] (?= !StartOfArrowFunction) '<' Type '>' UnaryExpression -> TsCastExpr
;

%left resolveShift;

%left '||';
%left '&&';
%left '??';
%left '|';
%left '^';
%left '&';
%left '==' '!=' '===' '!==';
%left '<' '>' '<=' '>=' 'instanceof' 'in' 'as' 'satisfies';
%left '<<' '>>' '>>>';
%left '-' '+';
%left '*' '/' '%';
%right '**';

BinaryExpression<In, Yield, Await> -> Expr /* interface */:
    UnaryExpression
  | left=BinaryExpression '+' right=BinaryExpression                -> AdditiveExpr
  | left=BinaryExpression '-' right=BinaryExpression                -> AdditiveExpr
  | left=BinaryExpression '<<' right=BinaryExpression               -> ShiftExpr
  | left=BinaryExpression '>>' right=BinaryExpression               -> ShiftExpr
  | left=BinaryExpression '>>>' right=BinaryExpression              -> ShiftExpr
  | left=BinaryExpression '*' right=BinaryExpression                -> MultiplicativeExpr
  | left=BinaryExpression '/' right=BinaryExpression                -> MultiplicativeExpr
  | left=BinaryExpression '%' right=BinaryExpression                -> MultiplicativeExpr
  | left=UpdateExpression '**' right=BinaryExpression               -> ExponentiationExpr
  | left=BinaryExpression '<' right=BinaryExpression                -> RelationalExpr
  | left=BinaryExpression '>' right=BinaryExpression                -> RelationalExpr
  | left=BinaryExpression '<=' right=BinaryExpression               -> RelationalExpr
  | left=BinaryExpression '>=' right=BinaryExpression               -> RelationalExpr
  | left=BinaryExpression 'instanceof' right=BinaryExpression       -> InstanceOfExpr
  | [In] left=BinaryExpression 'in' right=BinaryExpression          -> InExpr
  | [!NoAs] left=BinaryExpression .noLineBreak 'as' Type<+NoQuest>  -> TsAsExpr
  | [!NoAs] left=BinaryExpression .noLineBreak 'as' ('const' -> TsConst) -> TsAsConstExpr   # TS 3.4
  | [!NoSatisfies] left=BinaryExpression 'satisfies' Type<+NoQuest> -> TsSatisfiesExpr
  | left=BinaryExpression '==' right=BinaryExpression               -> EqualityExpr
  | left=BinaryExpression '!=' right=BinaryExpression               -> EqualityExpr
  | left=BinaryExpression '===' right=BinaryExpression              -> EqualityExpr
  | left=BinaryExpression '!==' right=BinaryExpression              -> EqualityExpr
  | left=BinaryExpression '&' right=BinaryExpression                -> BitwiseAND
  | left=BinaryExpression '^' right=BinaryExpression                -> BitwiseXOR
  | left=BinaryExpression '|' right=BinaryExpression                -> BitwiseOR
  | left=BinaryExpression '&&' right=BinaryExpression               -> LogicalAND
  | left=BinaryExpression '||' right=BinaryExpression               -> LogicalOR
  | left=BinaryExpression '??' right=BinaryExpression               -> CoalesceExpr
;

ConditionalExpression<In, Yield, Await> -> Expr /* interface */:
    BinaryExpression
  | cond=BinaryExpression '?' then=AssignmentExpression<+In> ':' else=AssignmentExpression
        -> ConditionalExpr
;

AssignmentExpression<In, Yield, Await> -> Expr /* interface */:
    ConditionalExpression
  | [Yield && !StartWithLet] YieldExpression
  | [!StartWithLet] ArrowFunction
  | [!StartWithLet] AsyncArrowFunction
  | left=LeftHandSideExpression '=' right=AssignmentExpression                -> AssignmentExpr
  | left=LeftHandSideExpression AssignmentOperator right=AssignmentExpression -> AssignmentExpr
;

AssignmentOperator -> AssignmentOperator :
    '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|=' | '**=' | '??=' | '||=' | '&&=' ;

CommaExpression<In, Yield, Await> -> CommaExpr :
    left=Expression ',' right=AssignmentExpression ;

%interface Expr;

Expression<In, Yield, Await> -> Expr /* interface */:
    AssignmentExpression
  | CommaExpression
;

# A.3 Statements

%interface Stmt, Decl;

Statement<Yield, Await> -> Stmt /* interface */:
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

Declaration<Yield, Await> -> Decl /* interface */:
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

HoistableDeclaration -> Decl /* interface */:
    FunctionDeclaration
  | GeneratorDeclaration
  | AsyncFunctionDeclaration
  | AsyncGeneratorDeclaration
;

BreakableStatement<Yield, Await> -> Stmt /* interface */:
    IterationStatement
  | SwitchStatement
;

BlockStatement<Yield, Await> :
    Block ;

Block<Yield, Await> -> Block :
    '{' .recoveryScope list=StatementList? list+=(SyntaxError -> StmtListItem)? '}' ;

StatementList<Yield, Await> :
    StatementListItem
  | StatementList StatementListItem
;

%interface StmtListItem, BindingPattern, PropertyPattern, ElementPattern, CaseClause;

StatementListItem<Yield, Await> -> StmtListItem /* interface */:
    Statement
  | Declaration
  | error ';'                                         -> SyntaxProblem
;

LexicalDeclaration<In, Yield, Await> -> LexicalDecl :
    LetOrConst BindingList ';' ;

LetOrConst -> LetOrConst :
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

VariableStatement<Yield, Await> -> VarStmt :
    'var' VariableDeclarationList<+In> ';'
;

VariableDeclarationList<In, Yield, Await> :
    VariableDeclaration
  | VariableDeclarationList ',' VariableDeclaration
;

VariableDeclaration<In, Yield, Await> -> VarDecl :
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

ElementElision :
    list+=(',' -> NoElement as ElementPattern)
  | Elision list+=(',' -> NoElement as ElementPattern)
;

ArrayBindingPattern<Yield, Await> -> ArrayPattern :
    '[' ElementElisionopt BindingRestElementopt ']'
  | '[' ElementPatternList ']'
  | '[' ElementPatternList ',' ElementElisionopt BindingRestElementopt ']'
;

ElementPatternList<Yield, Await> :
    BindingElisionElement
  | ElementPatternList ',' BindingElisionElement
;

BindingElisionElement<Yield, Await> :
    Elision? list+=ElementPattern ;

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
    (IdentifierReference -> NameIdent) Initializeropt<+In>
;

BindingRestElement -> BindingRestElement :
    '...' BindingIdentifier
;

EmptyStatement -> EmptyStmt :
    ';' .emptyStatement ;

ExpressionStatement<Yield, Await> -> ExprStmt :
    Expression<+In, +NoFuncClass, +NoAs, +NoSatisfies, +NoObjLiteral, +NoLetSq> ';' ;

%right 'else';

IfStatement<Yield, Await> -> IfStmt :
    'if' '(' Expression<+In> ')' then=Statement 'else' else=Statement
  | 'if' '(' Expression<+In> ')' then=Statement %prec 'else'
;

%interface IterationStmt;

IterationStatement<Yield, Await> -> IterationStmt /* interface */:
    'do' Statement 'while' '(' Expression<+In> ')' ';' .doWhile       -> DoWhileStmt
  | 'while' '(' Expression<+In> ')' Statement                         -> WhileStmt
  | 'for' '(' var=Expression<~In,+NoLet>? ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStmt
  | 'for' '(' var=Expression<~In,+StartWithLet, +NoAs, +NoSatisfies> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStmt
  | 'for' '(' ('var' -> Var) VariableDeclarationList<~In> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStmtWithVar
  | 'for' '(' LetOrConst BindingList<~In> ';' .forSC ForCondition
          ';' .forSC ForFinalExpression ')' Statement                 -> ForStmtWithVar
  | 'for' '(' var=LeftHandSideExpression<+NoLet>
          'in' object=Expression<+In> ')' Statement                   -> ForInStmt
  | 'for' '(' var=LeftHandSideExpression<+StartWithLet>
          'in' object=Expression<+In> ')' Statement                   -> ForInStmt
  | 'for' '(' ('var' -> Var) ForBinding
          'in' object=Expression<+In> ')' Statement                   -> ForInStmtWithVar
  | 'for' '(' ForDeclaration
          'in' object=Expression<+In> ')' Statement                   -> ForInStmtWithVar
  | 'for' ([Await] 'await' -> Await)? '(' var=LeftHandSideExpression<+NoLet, +NoAsync>
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStmt
  | 'for' ([Await] 'await' -> Await)? '(' var=((('async' -> ReferenceIdent) -> IdentExpr) -> Expr /* interface */) (?= !StartOfArrowFunction)
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStmt
  | 'for' ([Await] 'await' -> Await)? '(' ('var' -> Var) ForBinding
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStmtWithVar
  | 'for' ([Await] 'await' -> Await)?  '(' ForDeclaration
          'of' iterable=AssignmentExpression<+In> ')' Statement       -> ForOfStmtWithVar
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

ForFinalExpression<Yield, Await> -> ForFinalExpr :
    Expressionopt<+In> ;

ContinueStatement -> ContinueStmt :
    'continue' ';'
  | 'continue' .noLineBreak LabelIdentifier ';'
;

BreakStatement -> BreakStmt :
    'break' ';'
  | 'break' .noLineBreak LabelIdentifier ';'
;

ReturnStatement<Yield, Await> -> ReturnStmt :
    'return' ';'
  | 'return' .noLineBreak Expression<+In> ';'
;

WithStatement<Yield, Await> -> WithStmt :
    'with' '(' Expression<+In> ')' Statement
;

SwitchStatement<Yield, Await> -> SwitchStmt :
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
    'case' (Expression<+In> -> Cond) ':' StatementList?     -> Case
  | 'default' ':' StatementList?                            -> Default
;

LabelledStatement<Yield, Await> -> LabelledStmt :
    LabelIdentifier ':' LabelledItem ;

LabelledItem<Yield, Await> :
    Statement
  | FunctionDeclaration
;

ThrowStatement<Yield, Await> -> ThrowStmt :
    'throw' .noLineBreak Expression<+In> ';'
;

TryStatement<Yield, Await> -> TryStmt :
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
    BindingIdentifier TypeAnnotation?
  | BindingPattern TypeAnnotation?
;

DebuggerStatement -> DebuggerStmt :
    'debugger' ';'
;

# A.4 Functions and Classes

%interface ClassElement, MethodDefinition;

FunctionDeclaration -> Func :
    'function' BindingIdentifier? FormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await> ;

FunctionExpression -> FuncExpr :
    'function' BindingIdentifier? FormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await> ;

UniqueFormalParameters<Yield, Await> :
    FormalParameters ;

FunctionBody<Yield, Await>:
    '{' .recoveryScope list=StatementList? list+=(SyntaxError -> StmtListItem)? '}' -> Body
  | ';'
;

ArrowFunction<In> -> ArrowFunc :
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
  | SyntaxError
;

StartOfArrowFunction:
    BindingIdentifier '=>'
  | TypeParameters? ParameterList<~Yield, ~Await> TypeAnnotation? '=>'
;

AsyncArrowFunction<In> -> AsyncArrowFunc :
    'async' .afterAsync .noLineBreak (?= StartOfArrowFunction) ArrowParameters .noLineBreak '=>' AsyncConciseBody ;

# AsyncArrowHead :
#      'async' .noLineBreak ArrowFormalParameters<~Yield, +Await> ;

AsyncConciseBody<In> :
    AssignmentExpression<~Yield, +Await, +NoObjLiteral>           -> ConciseBody
  | AsyncFunctionBody
;

MethodDefinition<Yield, Await> -> MethodDefinition /* interface */:
    PropertyName ('?'|'!')? UniqueFormalParameters<~Yield, ~Await> FunctionBody<~Yield, ~Await> -> Method
  | GeneratorMethod
  | AsyncMethod
  | AsyncGeneratorMethod
  | 'get' PropertyName '(' ')' TypeAnnotationopt FunctionBody<~Yield, ~Await>            -> Getter
  | 'set' PropertyName '(' PropertySetParameterList ')' FunctionBody<~Yield, ~Await>     -> Setter
;

PropertySetParameterList :
    Parameter<~Yield, ~Await> ;

GeneratorMethod<Yield, Await> -> GeneratorMethod :
    '*' PropertyName ('?'|'!')? UniqueFormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorDeclaration -> Generator :
    'function' '*' BindingIdentifier? FormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorExpression -> GeneratorExpr :
    'function' '*' BindingIdentifier/* no yield*/? FormalParameters<+Yield, ~Await> GeneratorBody ;

GeneratorBody :
    FunctionBody<+Yield, ~Await> ;

YieldExpression<In, Await> -> Yield :
    'yield'
  | 'yield' .afterYield .noLineBreak AssignmentExpression<+Yield>
  | 'yield' .afterYield .noLineBreak '*' AssignmentExpression<+Yield>
;

AsyncMethod<Yield, Await> -> AsyncMethod :
    'async' .afterAsync .noLineBreak PropertyName ('?'|'!')? UniqueFormalParameters<~Yield, +Await> AsyncFunctionBody ;

AsyncFunctionDeclaration -> AsyncFunc :
    'async' .afterAsync .noLineBreak 'function' BindingIdentifier? FormalParameters<~Yield, +Await> AsyncFunctionBody ;

AsyncFunctionExpression -> AsyncFuncExpr :
    'async' .afterAsync .noLineBreak 'function' BindingIdentifier/* no await*/? FormalParameters<~Yield, +Await> AsyncFunctionBody ;

AsyncFunctionBody :
    FunctionBody<~Yield, +Await> ;

AsyncGeneratorMethod<Yield, Await> -> AsyncGeneratorMethod :
    'async' .afterAsync .noLineBreak '*' PropertyName ('?'|'!')? UniqueFormalParameters<+Yield, +Await> AsyncGeneratorBody ;

AsyncGeneratorDeclaration -> AsyncGeneratorDeclaration :
    'async' .afterAsync .noLineBreak 'function' '*' BindingIdentifier? FormalParameters<+Yield, +Await> AsyncGeneratorBody ;

AsyncGeneratorExpression -> AsyncGeneratorExpression :
    'async' .afterAsync .noLineBreak 'function' '*' BindingIdentifier? FormalParameters<+Yield, +Await> AsyncGeneratorBody ;

AsyncGeneratorBody :
    FunctionBody<+Yield, +Await> ;

AwaitExpression -> AwaitExpr :
    'await' UnaryExpression<~Yield, +Await> ;

ClassDeclaration<Yield, Await> -> Decl /* interface */:
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

%flag WithDeclare = false;

Modifier<WithDeclare> -> Modifier /* interface */:
    AccessibilityModifier
  | Decorator<~Await, ~Yield>
  | 'static'                 -> Static
  | 'abstract'               -> TsAbstract
  | 'override'               -> TsOverride
  | 'readonly'               -> TsReadonly
  | 'accessor'               -> Accessor
  | [WithDeclare] 'declare'  -> Declare
;

Modifiers<WithDeclare>:
    Modifier
  | Modifiers Modifier
;

ClassElement<Yield, Await> -> ClassElement /* interface */:
    Modifiers<+WithDeclare>? MethodDefinition   -> MemberMethod
  | Modifiers<+WithDeclare>? FieldDefinition    -> MemberVar
  | IndexSignature<+WithDeclare> ';'            -> TsIndexMemberDecl
  | ClassStaticBlock
  | ';'                                         -> EmptyDecl
  | SyntaxError ';'
;

FieldDefinition<Yield, Await>:
    PropertyName ('?'|'!')? TypeAnnotationopt Initializeropt<+In> ';' ;

ClassStaticBlock -> StaticBlock:
    'static' ClassStaticBlockBody ;

ClassStaticBlockBody -> Body:
    '{' .recoveryScope list=StatementList<~Yield, +Await>? list+=(SyntaxError -> StmtListItem)? '}' ;

# A.5 Scripts and Modules

%interface ModuleItem, NamedImport, ExportElement;

Module -> Module:
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

ImportDeclaration -> ImportDecl :
    'import' (?= !StartOfTypeImport) ImportClause FromClause (.noLineBreak AssertClause)? ';'
  | 'import' (?= StartOfTypeImport) ('type' -> TsTypeOnly)  ImportClause FromClause (.noLineBreak AssertClause)? ';'
  | 'import' ModuleSpecifier (.noLineBreak AssertClause)? ';'
;

AssertClause -> AssertClause:
    'assert' '{' ((AssertEntry separator ',')+ ','?)? '}'
;

AssertEntry -> AssertEntry:
    AssertionKey ':' StringLiteral ;

AssertionKey -> AssertionKey:
    IdentifierName
  | StringLiteral
;

StartOfTypeImport:
      'type' ('*' | '{' | IdentifierName<+WithoutFrom>) ;

ImportRequireDeclaration -> TsImportRequireDecl:
    ('export' -> TsExport)? 'import' (?= !StartOfTypeImport) BindingIdentifier '=' 'require' '(' ModuleSpecifier ')' ';' ;

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
    (IdentifierNameRef -> NameIdent)                  -> ImportSpec
  | IdentifierNameRef 'as' ImportedBinding            -> ImportSpec
  | error                                             -> SyntaxProblem
;

ModuleSpecifier -> ModuleSpec :
    StringLiteral ;

ImportedBinding :
    BindingIdentifier ;

ExportDeclaration -> ModuleItem /* interface */:
    'export' ('type' -> TsTypeOnly)? '*' ('as' ImportedBinding)? FromClause (.noLineBreak AssertClause)? ';' -> ExportDecl
  | 'export' ('type' -> TsTypeOnly)? ExportClause FromClause (.noLineBreak AssertClause)? ';'                -> ExportDecl
  | 'export' ('type' -> TsTypeOnly)? ExportClause ';'                           -> ExportDecl
  | 'export' VariableStatement<~Yield, ~Await>                                  -> ExportDecl
  | Modifiers? 'export' Declaration<~Yield, ~Await>                             -> ExportDecl
  | 'export' 'default' HoistableDeclaration                                        -> ExportDefault
  | Modifiers? 'export' 'default' ClassDeclaration<~Yield, ~Await>                 -> ExportDefault
  | 'export' 'default' AssignmentExpression<+In, ~Yield, ~Await, +NoFuncClass> ';' -> ExportDefault
  | 'export' '=' AssignmentExpression<+In, ~Yield, ~Await, +NoFuncClass> ';'     -> TsExportAssignment
  | 'export' 'as' 'namespace' BindingIdentifier ';'   -> TsNamespaceExportDecl
;

ExportClause -> ExportClause :
    '{' '}'
  | '{' (ExportElement separator ',')+ ','? '}'
;

ExportElement -> ExportElement /* interface */:
    (IdentifierNameRef -> NameIdent)                  -> ExportSpec
  | IdentifierNameRef 'as' IdentifierNameDecl         -> ExportSpec
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
  | DecoratorMemberExpression '.' (IdentifierName -> ReferenceIdent)
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

JSXFragment<Yield, Await> -> JSXFragment :
    '<' '>' JSXChild* '<' '/' '>' ;

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
    JSXAttributeName ('=' JSXAttributeValue)?               -> JSXNormalAttribute
  | '{' .recoveryScope '...' AssignmentExpression<+In> '}'  -> JSXSpreadAttribute
;

JSXAttributeName -> JSXAttributeName :
    jsxIdentifier
  | jsxIdentifier ':' jsxIdentifier
;

JSXAttributeValue<Yield, Await> -> JSXAttributeValue /* interface */:
    jsxStringLiteral                                  -> JSXLiteral
  | '{' .recoveryScope AssignmentExpression<+In> '}'  -> JSXExpr
  | JSXElement
  | JSXFragment
;

JSXChild<Yield, Await> -> JSXChild /* interface */:
    jsxText                                           -> JSXText
  | JSXElement
  | JSXFragment
  | '{' .recoveryScope AssignmentExpressionopt<+In> '}'              -> JSXExpr
  | '{' .recoveryScope '...' AssignmentExpressionopt<+In> '}'        -> JSXSpreadExpr
;

# Typescript

# Note: yield and await do not apply to type contexts.

# A.1 Types

%interface TsType, TsTypeMember;

%flag NoQuest = false;

Type<NoQuest> -> TsType /* interface */:
    UnionOrIntersectionOrPrimaryType %prec resolveShift
  | [!NoQuest] check=UnionOrIntersectionOrPrimaryType 'extends' ext=Type<+NoQuest> '?' truet=Type ':' falset=Type  -> TsConditional
  | FunctionType
  | ConstructorType
  | [!NoQuest] AssertsType
  | TypePredicate
;

TypePredicate<NoQuest> -> TsTypePredicate:
    paramref=IdentifierNameRef<+WithoutAsserts> 'is' Type -> TsTypePredicate
  | paramref=('asserts' -> ReferenceIdent) (?= StartOfIs) 'is' Type<+NoQuest> -> TsTypePredicate
;

# 3.7
AssertsType<NoQuest> -> TsAssertsType:
    'asserts' .noLineBreak (?= !StartOfIs) ('this' -> This | IdentifierName<+WithoutKeywords, +WithoutAs, +WithoutSatisfies> -> ReferenceIdent) ('is' Type)? ;

StartOfIs:
      'is' ;

TypeParameters -> TsTypeParameters :
    '<' (TypeParameter separator ',')+ ','? '>' ;

TypeParameter -> TsTypeParameter :
    BindingIdentifier Constraint? ('=' Type)?;

Constraint -> TsTypeConstraint :
    'extends' Type ;

TypeArguments -> TsTypeArguments :
    '<' (Type separator ',')+ '>' ;

UnionOrIntersectionOrPrimaryType<NoQuest> -> TsType /* interface */:
    inner+=UnionOrIntersectionOrPrimaryType? '|' inner+=IntersectionOrPrimaryType -> TsUnionType
  | IntersectionOrPrimaryType %prec resolveShift
;

IntersectionOrPrimaryType<NoQuest> -> TsType /* interface */:
    inner+=IntersectionOrPrimaryType? '&' inner+=TypeOperator -> TsIntersectionType
  | TypeOperator
;

TypeOperator<NoQuest> -> TsType /* interface */:
    PrimaryType
  | 'keyof' TypeOperator      -> TsKeyOfType
  | 'unique' TypeOperator     -> TsUniqueType
  | 'readonly' TypeOperator   -> TsReadonlyType
  | 'infer' (IdentifierName -> NameIdent) -> TsTypeVar
;

PrimaryType<NoQuest> -> TsType /* interface */:
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
  | 'this'                                        -> TsThisType
  | PrimaryType .noLineBreak '!'                  -> TsNonNullableType
  | [!NoQuest] PrimaryType .noLineBreak '?'     -> TsNullableType
;

ParenthesizedType -> TsParenthesizedType :
    '(' (?= !StartOfFunctionType) Type ')' ;

# 2.0
LiteralType -> TsLiteralType :
    StringLiteral
  | '-'? NumericLiteral
  | 'null'
  | 'true'
  | 'false'
  | TemplateLiteralType
;

PredefinedType -> TsPredefinedType :
    'any'
  | 'unknown'
  | 'number'
  | 'boolean'
  | 'string'
  | 'symbol'
  | 'void'
  | 'bigint'
  | 'undefined'
  | 'never'
  | 'object'
;

TypeReference -> TsTypeReference :
    TypeName .noLineBreak TypeArguments? %prec resolveShift ;

TypeName -> TsTypeName :
    ref+=IdentifierReference<+WithoutPredefinedTypes, ~Yield, ~Await>
  | (NamespaceName -> TsNamespaceName) '.' ref+=IdentifierReference<~Yield, ~Await, +WithDefault>
;

NamespaceName:
    ref+=IdentifierReference<~Yield, ~Await>
  | NamespaceName '.' ref+=IdentifierReference<~Yield, ~Await, +WithDefault>
;

ObjectType -> TsObjectType :
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

TypeMember -> TsTypeMember /* interface */:
    PropertySignature
  | MethodSignature
  | CallSignature
  | ConstructSignature
  | IndexSignature
  | AccessorSignature
  | SyntaxError
;

ArrayType<NoQuest> -> TsArrayType :
    PrimaryType .noLineBreak '[' ']' ;

# 2.1
IndexedAccessType<NoQuest> -> TsIndexedAccessType :
    left=PrimaryType .noLineBreak '[' index=Type ']' ;

# 2.1
StartOfMappedType :
    ('+' | '-') 'readonly'
  | 'readonly'? '[' IdentifierName 'in'
;

# 2.1
MappedType -> TsMappedType :
    '{' .recoveryScope (?= StartOfMappedType) (('+'|'-')? 'readonly')?
          '[' BindingIdentifier 'in' inType=Type ('as' asType=Type)? ']'
          (('+'|'-')? '?')? TypeAnnotation ';'? '}' ;

TupleType -> TsTupleType :
    '[' (TupleElementType separator ',')+? ','? ']' ;

%interface TupleMember;

TupleElementType -> TupleMember:
    (?= !StartOfTupleElementName) Type
  | (?= StartOfTupleElementName) IdentifierName '?'? ':' Type -> NamedTupleMember
  | '...' (?= !StartOfTupleElementName) Type  -> TsRestType
  | '...' (?= StartOfTupleElementName) IdentifierName '?'? ':' (Type -> TsRestType as TsType) -> NamedTupleMember
;

StartOfTupleElementName:
      IdentifierName '?'? ':' ;

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

FunctionType<NoQuest> -> TsFuncType :
    TypeParameters? FunctionTypeParameterList<~Yield, ~Await> '=>' Type ;

ConstructorType<NoQuest> -> TsConstructorType :
    ('abstract' -> TsAbstract)? 'new' TypeParameters? ParameterList<~Yield, ~Await> '=>' Type ;

%left 'keyof' 'typeof' 'unique' 'readonly' 'infer';
%nonassoc 'is';

TypeQuery -> TsTypeQuery :
    'typeof' TypeQueryExpression ;

# 2.9
ImportType -> TsImportType:
     ImportTypeStart .noLineBreak TypeArguments? %prec resolveShift ;

ImportTypeStart -> TsImportTypeStart :
    ('typeof' -> TsTypeOf)? 'import' '(' Type ')'
  | ImportTypeStart '.' IdentifierNameRef -> TsImportTypeStart
;

TypeQueryExpression :
    IdentifierNameRef
  | TypeQueryExpression '.' IdentifierNameRef
;

PropertySignature -> TsPropertySignature :
    Modifiers? PropertyName<+WithoutNew, ~Yield, ~Await> '?'? TypeAnnotation? ;

TypeAnnotation -> TsTypeAnnotation :
    ':' Type ;

FormalParameters<Yield, Await> :
    TypeParameters? ParameterList TypeAnnotation? ;

CallSignature -> TsCallSignature :
    TypeParameters? ParameterList<~Yield, ~Await> TypeAnnotation? ;

ParameterList<Yield, Await> -> Parameters :
    '(' (Parameter separator ',')+? ','? ')' ;

FunctionTypeParameterList<Yield, Await> -> Parameters :
    '(' (?= StartOfFunctionType) (Parameter separator ',')+? ','? ')' ;

%interface Parameter;

Parameter<Yield, Await> -> Parameter :
    Modifiers? BindingIdentifier ('?'->TsOptional)? TypeAnnotation? -> DefaultParameter
  | Modifiers? BindingPattern ('?'->TsOptional)? TypeAnnotation?    -> DefaultParameter
  | Modifiers? BindingIdentifier TypeAnnotation? Initializer<+In>   -> DefaultParameter
  | Modifiers? BindingPattern TypeAnnotation? Initializer<+In>      -> DefaultParameter
  | '...' (BindingIdentifier | BindingPattern) TypeAnnotation?      -> RestParameter
  | 'this' TypeAnnotation                                           -> TsThisParameter
  | SyntaxError
;

AccessibilityModifier -> TsAccessibilityModifier :
    'public'
  | 'private'
  | 'protected'
;

BindingIdentifierOrPattern<Yield, Await> :
    BindingIdentifier
  | BindingPattern
;

ConstructSignature -> TsConstructSignature :
    Modifiers? 'new' TypeParameters? ParameterList<~Yield, ~Await> TypeAnnotation? ;

# Note: using IdentifierName instead of BindingIdentifier to avoid r/r
# conflicts with ComputedPropertyName.
IndexSignature<WithDeclare> -> TsIndexSignature :
    Modifiers? '[' (IdentifierName -> NameIdent) ':' Type ']' TypeAnnotation ;

MethodSignature -> TsMethodSignature :
    Modifiers? PropertyName<+WithoutNew, ~Yield, ~Await> '?'? FormalParameters<~Yield, ~Await>
  | Modifiers? ((('new'-> NameIdent)->LiteralPropertyName)->PropertyName) '?' FormalParameters<~Yield, ~Await>
  ;

AccessorSignature -> TsTypeMember:
    Modifiers? 'get' PropertyName<~Yield, ~Await> '(' ')' TypeAnnotationopt          -> Getter
  | Modifiers? 'set' PropertyName<~Yield, ~Await> '(' PropertySetParameterList ')'   -> Setter
;

TypeAliasDeclaration -> TsTypeAliasDecl :
    'type' BindingIdentifier TypeParameters? '=' Type ';' ;

# A.5 Interfaces

InterfaceDeclaration -> TsInterface:
    'interface' BindingIdentifier TypeParametersopt InterfaceExtendsClause? ObjectType ;

InterfaceExtendsClause -> TsInterfaceExtends:
    'extends' (TypeReference separator ',')+ ;

# A.7 Enums

EnumDeclaration -> TsEnum:
    ('const' -> TsConst)? 'enum' BindingIdentifier EnumBody ;

EnumBody -> TsEnumBody:
    '{' .recoveryScope ((EnumMember separator ',')+ ','?)? '}' ;

EnumMember -> TsEnumMember:
    PropertyName<~Yield, ~Await>
  | PropertyName<~Yield, ~Await> '=' (AssignmentExpression<+In, ~Yield, ~Await> | BrokenExpr)
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

ImportAliasDeclaration -> TsImportAliasDecl:
    'import' (?= !StartOfTypeImport) BindingIdentifier '=' EntityName ';' ;

EntityName:
    NamespaceName ;

# A.10 Ambients

%interface TsAmbientElement, TsAmbientClassElement;

AmbientDeclaration -> TsAmbientElement /* interface */:
    'declare' AmbientVariableDeclaration        -> TsAmbientVar
  | 'declare' AmbientFunctionDeclaration        -> TsAmbientFunc
  | 'declare' AmbientClassDeclaration           -> TsAmbientClass
  | 'declare' AmbientInterfaceDeclaration       -> TsAmbientInterface
  | 'declare' AmbientEnumDeclaration            -> TsAmbientEnum
  | 'declare' AmbientNamespaceDeclaration       -> TsAmbientNamespace
  | 'declare' AmbientModuleDeclaration          -> TsAmbientModule
  | 'declare' AmbientGlobalDeclaration          -> TsAmbientGlobal
  | 'declare' TypeAliasDeclaration              -> TsAmbientTypeAlias
;

AmbientVariableDeclaration:
    ('var' -> Var) AmbientBindingList ';'
  | ('let' -> LetOrConst) AmbientBindingList ';'
  | ('const' -> LetOrConst) AmbientBindingList ';'
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
    Modifiers? 'class' BindingIdentifier TypeParametersopt ClassTail<~Yield, ~Await> ;

AmbientInterfaceDeclaration:
    Modifiers? 'interface' BindingIdentifier TypeParametersopt InterfaceExtendsClause? ObjectType ;

AmbientEnumDeclaration:
    ('const' -> TsConst)? 'enum' BindingIdentifier EnumBody ;

AmbientNamespaceDeclaration:
    'namespace' IdentifierPath AmbientNamespaceBody ;

AmbientGlobalDeclaration:
    'global' ('{' .recoveryScope ModuleBodyopt '}' | ';') ;

AmbientNamespaceBody:
    '{' .recoveryScope AmbientNamespaceElement+? '}' ;

AmbientNamespaceElement -> TsAmbientElement /* interface */:
    'export'? AmbientVariableDeclaration    -> TsAmbientVar
  | 'export'? AmbientFunctionDeclaration    -> TsAmbientFunc
  | 'export'? AmbientClassDeclaration       -> TsAmbientClass
  | 'export'? AmbientInterfaceDeclaration   -> TsAmbientInterface
  | 'export'? AmbientEnumDeclaration        -> TsAmbientEnum
  | 'export'? AmbientNamespaceDeclaration   -> TsAmbientNamespace
  | 'export'? AmbientModuleDeclaration      -> TsAmbientModule
  | 'export'? ImportAliasDeclaration        -> TsAmbientImportAlias
  | 'export'? TypeAliasDeclaration          -> TsAmbientTypeAlias
  | 'export' ExportClause ';'               -> TsAmbientExportDecl
;

AmbientModuleDeclaration:
    'module' (StringLiteral -> Literal | IdentifierPath) (AmbientModuleBody | ';') ;

AmbientModuleBody:
    '{' .recoveryScope AmbientModuleElement+? '}' ;

AmbientModuleElement -> TsAmbientElement /* interface */:
  ImportDeclaration
  | 'export'? AmbientVariableDeclaration                                           -> TsAmbientVar
  | 'export'? AmbientFunctionDeclaration                                           -> TsAmbientFunc
  | 'export'? AmbientClassDeclaration                                              -> TsAmbientClass
  | 'export'? AmbientInterfaceDeclaration                                          -> TsAmbientInterface
  | 'export'? AmbientEnumDeclaration                                               -> TsAmbientEnum
  | 'export'? AmbientNamespaceDeclaration                                          -> TsAmbientNamespace
  | 'export'? AmbientModuleDeclaration                                             -> TsAmbientModule
  | 'export'? ImportAliasDeclaration                                               -> TsAmbientImportAlias
  | 'export'? TypeAliasDeclaration                                                 -> TsAmbientTypeAlias
  | 'export' ExportClause ';'                                                      -> TsAmbientExportDecl
  | 'export' 'default' AssignmentExpression<+In, ~Yield, ~Await, +NoFuncClass> ';' -> ExportDefault
  | 'export' '=' AssignmentExpression<+In, ~Yield, ~Await, +NoFuncClass> ';'       -> TsExportAssignment
;

%%

{{define "onBeforeLexer"}}
type Dialect int

const (
	Javascript Dialect = iota
	Typescript
	TypescriptJsx
)
{{end}}

{{define "stateVars"}}
	Dialect Dialect
	token   token.Type // last token
	Stack   []int // stack of JSX states, non-empty for StateJsx*
{{end}}

{{define "initStateVars"}}
	l.Dialect = Javascript
	l.token = token.UNAVAILABLE
	l.Stack = nil
{{end}}

{{define "onBeforeNext"}}
	prevLine := l.tokenLine
{{end}}

{{define "onAfterNext"}}

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
		switch tok {
		case token.NEW, token.DELETE, token.VOID, token.TYPEOF, token.INSTANCEOF,
      token.IN, token.DO, token.RETURN, token.CASE, token.THROW, token.ELSE:

			l.State &^= 1
		case token.TEMPLATEHEAD:
			l.State |= 1
			l.pushState(StateTemplate)
		case token.TEMPLATEMIDDLE:
			l.State = StateTemplate
		case token.TEMPLATETAIL:
			l.popState()
		case token.RPAREN, token.RBRACK:
			// TODO support if (...) /aaaa/;
			l.State |= 1
		case token.PLUSPLUS, token.MINUSMINUS:
			if prevLine != l.tokenLine {
				// This is a pre-increment/decrement, so we expect a regular expression.
				l.State &^= 1
			}
			// Otherwise: if we were expecting a regular expression literal before this
			// token, this is a pre-increment/decrement, otherwise, this is a post. We
			// can just propagate the previous value of the lowest bit of the state.
		case token.LT:
			if l.State&1 == 0 {
				// Start a new JSX tag.
				switch l.Dialect {
				case TypescriptJsx:
					copy := l.Copy()
					copy.Dialect = Typescript
					if copy.Next() == token.IDENTIFIER {
						var isArrowFunc bool
						switch copy.Next() {
						case token.EXTENDS:
							fourth := copy.Next()
							isArrowFunc = fourth != token.ASSIGN && fourth != token.GT
						case token.COMMA, token.ASSIGN:
							isArrowFunc = true
						}
						if !isArrowFunc {
							l.State |= 1
							l.pushState(StateJsxTag)
						}
					} else {
					  l.State |= 1
					  l.pushState(StateJsxTag)
          }
				case Typescript:
					// JSX is not allowed in pure TypeScript.
				default:
					l.State |= 1
					l.pushState(StateJsxTag)
				}
			} else {
				l.State &^= 1
			}
		case token.LBRACE:
			l.State &^= 1
			if l.State >= StateTemplate {
				l.pushState(StateTemplateExpr)
			}
		case token.RBRACE:
			l.State &^= 1
			if l.State >= StateTemplate {
				l.popState()
			}
		case token.SINGLELINECOMMENT, token.MULTILINECOMMENT:
			break
		case token.EXCL:
			if l.Dialect != Javascript {
				switch l.token {
				case token.RPAREN, token.RBRACK, token.RBRACE, token.PRIVATEIDENTIFIER,
          token.IDENTIFIER, token.NOSUBSTITUTIONTEMPLATE, token.THIS,
          token.NULL, token.TRUE, token.FALSE, token.STRINGLITERAL,
          token.NUMERICLITERAL:

					// TS non-null assertion.
					l.State |= 1
				case token.NEW, token.DELETE, token.VOID, token.TYPEOF,
          token.INSTANCEOF, token.IN, token.DO, token.RETURN, token.CASE,
          token.THROW, token.ELSE:

					l.State &^= 1
				default:
					if l.token >= keywordStart && l.token < keywordEnd {
						// TS non-null assertion.
						l.State |= 1
					} else {
						// Unary expression.
						l.State &^= 1
					}
				}
			} else {
				l.State &^= 1
			}
		default:
			if tok >= punctuationStart && tok < punctuationEnd {
				l.State &^= 1
			} else {
				l.State |= 1
			}
		}
	} else {
		// Handling JSX states.
		switch tok {
		case token.DIV:
			if l.State == StateJsxTag && l.token == token.LT {
				l.State = StateJsxClosingTag
				if len(l.Stack) > 0 {
					l.Stack = l.Stack[:len(l.Stack)-1]
				}
			}
		case token.GT:
			if l.State == StateJsxTypeArgs || l.State == StateJsxClosingTag || l.token == token.DIV {
				l.popState()
			} else {
				l.State = StateJsxText
			}
		case token.LBRACE:
			if l.State != StateJsxTypeArgs {
				l.pushState(StateTemplateExpr)
			}
		case token.LT:
			if l.Dialect == TypescriptJsx && l.State != StateJsxText && l.token != token.ASSIGN {
				// Type arguments.
				l.pushState(StateJsxTypeArgs)
			} else {
				// Start a new JSX tag.
				l.pushState(StateJsxTag)
			}
		}
	}
	l.token = tok
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

{{define "streamStateVars" -}}
	delayed      symbol // by semicolon insertion
	recoveryMode bool   // forces use of simplified semicolon insertion rules during error recovery

	lastToken token.Type
	lastEnd   int
	lastLine  int
{{end}}

{{define "initStreamStateVars" -}}
	s.delayed.symbol = noToken
	s.recoveryMode = false
	s.lastToken = token.UNAVAILABLE
{{end}}

{{define "onAfterStream" -}}
func (s *TokenStream) SetDialect(d Dialect) {
	s.lexer.Dialect = d
}
{{end}}
