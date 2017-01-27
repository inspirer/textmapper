# Textmapper syntax overview

language simple(java);

:: lexer

whitespace: /[\r\n\t\f\x20]|\r\n/	(space)
end_of_line_comment: /\/\/[^\r\n]*/ (space)
TraditionalComment: /\/\*([^*]|\*+[^\/*])*\*+\// (space)

# Named Patterns

Letter = /[a-zA-Z_\p{L}]/
LetterOrDigit = /[a-zA-Z0-9_\p{L}]/

# Lexeme class - identifier

Identifier: /{Letter}{LetterOrDigit}*/  (class)

int_literal: /0|[1-9][0-9]*/

# keyword
class: /class/
null: /null/

# soft keyword
get: /get/		(soft)

# operators

'{': /\{/
'}': /\}/
'=': /=/

:: parser

%input ClassDeclaration;

ClassDeclaration :
	class name=Identifier '{' PropertyDeclaration* '}' ;

PropertyDeclaration :
	type=Identifier name=Identifier ('{' PropertyConstraint* '}')? ;

PropertyConstraint :
	get '=' Expression ;

Expression :
	  int_literal
	| null
;