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

space: /[\t\x0b\x0c\x20\xa0\ufeff\p{Zs}]/ (space)

# Note: LineTerminator: /[\n\r\u2028\u2029]/
LineTerminatorSequence: /[\n\r\u2028\u2029]|\r\n/ (space)

commentChars = /([^*]|\*+[^*\/])*([^*]|\*+[^*\/]?)/
MultiLineComment: /\/\*{commentChars}?\*\// (space)
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
# TODO what about 'let' ?
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

# TODO
#TemplateCharacter ::
#	$ [lookahead != { ]
#	\ EscapeSequence
#	LineContinuation
#	LineTerminatorSequence
#	SourceCharacter but not ` or \ or $ or LineTerminator

tplChar = /\$/

[initial, div]

'}': /\}/

NoSubstitutionTemplate: /`{tplChar}*`/
TemplateHead: /`{tplChar}*\$\{/

[template, template_div]

TemplateMiddle: /\}{tplChar}*\$\{/
TemplateTail: /\}{tplChar}*`/

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

%input Program;

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

Literal ::=
	  'null'
	| 'true'
	| 'false'
	| NumericLiteral
	| StringLiteral
;

# FIXME
Program ::=
	  Literal IdentifierName ;

