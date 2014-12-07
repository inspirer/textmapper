# Standard ECMA-262, 5.1 Edition, June 2011
# ECMAScript Language Specification
#
# http://www.ecma-international.org/publications/standards/Ecma-262.htm

# A.1	Lexical Grammar

SourceCharacter ::
	any Unicode code unit

InputElementDiv ::
	WhiteSpace
	LineTerminator
	Comment
	Token
	DivPunctuator

InputElementRegExp ::
	WhiteSpace
	LineTerminator
	Comment
	Token
	RegularExpressionLiteral

WhiteSpace ::
	<TAB>
	<VT>
	<FF>
	<SP>
	<NBSP>
	<BOM>
	<USP>

LineTerminator ::
	<LF>
	<CR>
	<LS>
	<PS>

LineTerminatorSequence ::
	<LF>
	<CR> [lookahead != <LF>]
	<LS>
	<PS>
	<CR> <LF>

Comment ::
	MultiLineComment
	SingleLineComment

MultiLineComment ::
	/ * MultiLineCommentCharsopt * /

MultiLineCommentChars ::
	MultiLineNotAsteriskChar MultiLineCommentCharsopt
	* PostAsteriskCommentCharsopt

PostAsteriskCommentChars ::
	MultiLineNotForwardSlashOrAsteriskChar MultiLineCommentCharsopt
	* PostAsteriskCommentCharsopt

MultiLineNotAsteriskChar ::
	SourceCharacter but not *

MultiLineNotForwardSlashOrAsteriskChar ::
	SourceCharacter but not / or *

SingleLineComment ::
	/ / SingleLineCommentCharsopt

SingleLineCommentChars ::
	SingleLineCommentChar SingleLineCommentCharsopt

SingleLineCommentChar ::
	SourceCharacter but not LineTerminator

Token ::
	IdentifierName
	Punctuator
	NumericLiteral
	StringLiteral

Identifier ::
	IdentifierName but not ReservedWord

IdentifierName ::
	IdentifierStart
	IdentifierName IdentifierPart

IdentifierStart ::
	UnicodeLetter
	$
	_
	\ UnicodeEscapeSequence

IdentifierPart ::
	IdentifierStart
	UnicodeCombiningMark
	UnicodeDigit
	UnicodeConnectorPunctuation
	<ZWNJ>
	<ZWJ>

UnicodeLetter ::
	any character in the Unicode categories "Uppercase letter (Lu)", "Lowercase letter (Ll)", "Titlecase letter (Lt)", "Modifier letter (Lm)", "Other letter (Lo)", or "Letter number (Nl)"

UnicodeCombiningMark ::
	any character in the Unicode categories "Non-spacing mark (Mn)" or "Combining spacing mark (Mc)"

UnicodeDigit ::
	any character in the Unicode category "Decimal number (Nd)"

UnicodeConnectorPunctuation ::
	any character in the Unicode category "Connector punctuation (Pc)"

ReservedWord ::
	Keyword
	FutureReservedWord
	NullLiteral
	BooleanLiteral

Keyword :: one of
	break		do			instanceof	typeof
	case		else		new			var
	catch		finally		return		void 
	continue	for			switch		while
	debugger	function	this		with
	default		if			throw
	delete		in			try
# TODO: smart keywords? (used in PropertyAssignment) 
	get set

FutureReservedWord :: one of
	class		enum		extends		super
	const		export		import
#	or in strict mode code one of
#	implements	let			private		public
#	interface	package		protected	static
#	yield

Punctuator :: one of
	{	}	(	)	[	]
	.	;	,	<	>	<=
	>=	==	!=	===	!==	
	+	-	*	%	++	--
	<<	>>	>>>	&	|	^
	!	~	&&	||	?	:
	=	+=	-=	*=	%=	<<=
	>>=	>>>= &=	|=	^=

DivPunctuator :: one of
	/   /=

Literal ::
	NullLiteral
	BooleanLiteral
	NumericLiteral
	StringLiteral
	RegularExpressionLiteral

NullLiteral ::
	null

BooleanLiteral ::
	true
	false

NumericLiteral ::
	DecimalLiteral
	HexIntegerLiteral

DecimalLiteral ::
	DecimalIntegerLiteral . DecimalDigitsopt ExponentPartopt
	. DecimalDigits ExponentPartopt
	DecimalIntegerLiteral ExponentPartopt

DecimalIntegerLiteral ::
	0
	NonZeroDigit DecimalDigitsopt

DecimalDigits ::
	DecimalDigit
	DecimalDigits DecimalDigit

DecimalDigit :: one of
	0 1 2 3 4 5 6 7 8 9

NonZeroDigit :: one of
	1 2 3 4 5 6 7 8 9

ExponentPart ::
	ExponentIndicator SignedInteger

ExponentIndicator :: one of
	e E

SignedInteger ::
	DecimalDigits
	+ DecimalDigits
	- DecimalDigits

HexIntegerLiteral ::
	0 x HexDigit
	0 X HexDigit
	HexIntegerLiteral HexDigit

HexDigit :: one of
	0 1 2 3 4 5 6 7 8 9 a b c d e f A B C D E F

StringLiteral ::
	" DoubleStringCharactersopt "
	' SingleStringCharactersopt '

DoubleStringCharacters ::
	DoubleStringCharacter DoubleStringCharactersopt

SingleStringCharacters ::
	SingleStringCharacter SingleStringCharactersopt

DoubleStringCharacter ::
	SourceCharacter but not " or \ or LineTerminator
	\ EscapeSequence
	LineContinuation

SingleStringCharacter ::
	SourceCharacter but not ' or \ or LineTerminator
	\ EscapeSequence
	LineContinuation

LineContinuation ::
	\ LineTerminatorSequence

EscapeSequence ::
	CharacterEscapeSequence
	0 [lookahead != DecimalDigit]
	HexEscapeSequence
	UnicodeEscapeSequence

CharacterEscapeSequence ::
	SingleEscapeCharacter
	NonEscapeCharacter

SingleEscapeCharacter :: one of
	' " \ b f n r t v

NonEscapeCharacter ::
	SourceCharacter but not EscapeCharacter or LineTerminator

EscapeCharacter ::
	SingleEscapeCharacter
	DecimalDigit
	x
	u

HexEscapeSequence ::
	x HexDigit HexDigit

UnicodeEscapeSequence ::
	u HexDigit HexDigit HexDigit HexDigit

RegularExpressionLiteral ::
	/ RegularExpressionBody / RegularExpressionFlags

RegularExpressionBody ::
	RegularExpressionFirstChar RegularExpressionChars

RegularExpressionChars ::
	[empty]
	RegularExpressionChars RegularExpressionChar

RegularExpressionFirstChar ::
	RegularExpressionNonTerminator but not * or \ or / or [
	RegularExpressionBackslashSequence
	RegularExpressionClass

RegularExpressionChar ::
	RegularExpressionNonTerminator but not \ or / or [
	RegularExpressionBackslashSequence
	RegularExpressionClass

RegularExpressionBackslashSequence ::
	\ RegularExpressionNonTerminator

RegularExpressionNonTerminator ::
	SourceCharacter but not LineTerminator

RegularExpressionClass ::
	[ RegularExpressionClassChars ]

RegularExpressionClassChars ::
	[empty]
	RegularExpressionClassChars RegularExpressionClassChar

RegularExpressionClassChar ::
	RegularExpressionNonTerminator but not ] or \
	RegularExpressionBackslashSequence

RegularExpressionFlags ::
	[empty]
	RegularExpressionFlags IdentifierPart

# A.2 Number Conversions (skipped)

# A.3 Expressions

PrimaryExpression :
	this
	Identifier
	Literal
	ArrayLiteral
	ObjectLiteral
	( Expression )

ArrayLiteral :
	[ Elisionopt ]
	[ ElementList ]
	[ ElementList , Elisionopt ]

ElementList :
	Elisionopt AssignmentExpression
	ElementList , Elisionopt AssignmentExpression

Elision :
	,
	Elision ,

ObjectLiteral :
	{ }
	{ PropertyNameAndValueList }
	{ PropertyNameAndValueList , }

PropertyNameAndValueList :
	PropertyAssignment
	PropertyNameAndValueList , PropertyAssignment

PropertyAssignment :
	PropertyName : AssignmentExpression
	get PropertyName ( ) { FunctionBody }
	set PropertyName ( PropertySetParameterList ) { FunctionBody }

PropertyName :
	IdentifierName
	StringLiteral
	NumericLiteral

PropertySetParameterList :
	Identifier

MemberExpression :
	PrimaryExpression
	FunctionExpression
	MemberExpression [ Expression ]
	MemberExpression . IdentifierName
	new MemberExpression Arguments

NewExpression :
	MemberExpression
	new NewExpression

CallExpression :
	MemberExpression Arguments
	CallExpression Arguments
	CallExpression [ Expression ]
	CallExpression . IdentifierName

Arguments :
	( )
	( ArgumentList )

ArgumentList :
	AssignmentExpression
	ArgumentList , AssignmentExpression

LeftHandSideExpression :
	NewExpression
	CallExpression

PostfixExpression :
	LeftHandSideExpression
	LeftHandSideExpression [no LineTerminator here] ++
	LeftHandSideExpression [no LineTerminator here] --

UnaryExpression :
	PostfixExpression
	delete UnaryExpression
	void UnaryExpression
	typeof UnaryExpression
	++ UnaryExpression
	-- UnaryExpression
	+ UnaryExpression
	- UnaryExpression
	~ UnaryExpression
	! UnaryExpression

MultiplicativeExpression :
	UnaryExpression
	MultiplicativeExpression * UnaryExpression
	MultiplicativeExpression / UnaryExpression
	MultiplicativeExpression % UnaryExpression

AdditiveExpression :
	MultiplicativeExpression
	AdditiveExpression + MultiplicativeExpression
	AdditiveExpression - MultiplicativeExpression

ShiftExpression :
	AdditiveExpression
	ShiftExpression << AdditiveExpression
	ShiftExpression >> AdditiveExpression
	ShiftExpression >>> AdditiveExpression

RelationalExpression :
	ShiftExpression
	RelationalExpression < ShiftExpression
	RelationalExpression > ShiftExpression
	RelationalExpression <= ShiftExpression
	RelationalExpression >= ShiftExpression
	RelationalExpression instanceof ShiftExpression
	RelationalExpression in ShiftExpression

RelationalExpressionNoIn :
	ShiftExpression
	RelationalExpressionNoIn < ShiftExpression
	RelationalExpressionNoIn > ShiftExpression
	RelationalExpressionNoIn <= ShiftExpression
	RelationalExpressionNoIn >= ShiftExpression
	RelationalExpressionNoIn instanceof ShiftExpression

EqualityExpression :
	RelationalExpression
	EqualityExpression == RelationalExpression
	EqualityExpression != RelationalExpression
	EqualityExpression === RelationalExpression
	EqualityExpression !== RelationalExpression

EqualityExpressionNoIn :
	RelationalExpressionNoIn
	EqualityExpressionNoIn == RelationalExpressionNoIn
	EqualityExpressionNoIn != RelationalExpressionNoIn
	EqualityExpressionNoIn === RelationalExpressionNoIn
	EqualityExpressionNoIn !== RelationalExpressionNoIn

BitwiseANDExpression :
	EqualityExpression
	BitwiseANDExpression & EqualityExpression

BitwiseANDExpressionNoIn :
	EqualityExpressionNoIn
	BitwiseANDExpressionNoIn & EqualityExpressionNoIn

BitwiseXORExpression :
	BitwiseANDExpression
	BitwiseXORExpression ^ BitwiseANDExpression

BitwiseXORExpressionNoIn :
	BitwiseANDExpressionNoIn
	BitwiseXORExpressionNoIn ^ BitwiseANDExpressionNoIn

BitwiseORExpression :
	BitwiseXORExpression
	BitwiseORExpression | BitwiseXORExpression

BitwiseORExpressionNoIn :
	BitwiseXORExpressionNoIn
	BitwiseORExpressionNoIn | BitwiseXORExpressionNoIn

LogicalANDExpression :
	BitwiseORExpression
	LogicalANDExpression && BitwiseORExpression

LogicalANDExpressionNoIn :
	BitwiseORExpressionNoIn
	LogicalANDExpressionNoIn && BitwiseORExpressionNoIn

LogicalORExpression :
	LogicalANDExpression
	LogicalORExpression || LogicalANDExpression

LogicalORExpressionNoIn :
	LogicalANDExpressionNoIn
	LogicalORExpressionNoIn || LogicalANDExpressionNoIn

ConditionalExpression :
	LogicalORExpression
	LogicalORExpression ? AssignmentExpression : AssignmentExpression

ConditionalExpressionNoIn :
	LogicalORExpressionNoIn
	LogicalORExpressionNoIn ? AssignmentExpressionNoIn : AssignmentExpressionNoIn

AssignmentExpression :
	ConditionalExpression
	LeftHandSideExpression = AssignmentExpression
	LeftHandSideExpression AssignmentOperator AssignmentExpression

AssignmentExpressionNoIn :
	ConditionalExpressionNoIn
	LeftHandSideExpression = AssignmentExpressionNoIn
	LeftHandSideExpression AssignmentOperator AssignmentExpressionNoIn

AssignmentOperator : one of
	*=  /=  %=  +=  -=  <<=  >>=  >>>=  &=  ^=  |=

Expression :
	AssignmentExpression
	Expression , AssignmentExpression

ExpressionNoIn :
	AssignmentExpressionNoIn
	ExpressionNoIn , AssignmentExpressionNoIn

# A.4 Statements

Statement :
	Block
	VariableStatement
	EmptyStatement
	ExpressionStatement
	IfStatement
	IterationStatement
	ContinueStatement
	BreakStatement
	ReturnStatement
	WithStatement
	LabelledStatement
	SwitchStatement
	ThrowStatement
	TryStatement
	DebuggerStatement

Block :
	{ StatementListopt }

StatementList :
	Statement
	StatementList Statement

VariableStatement :
	var VariableDeclarationList ;

VariableDeclarationList :
	VariableDeclaration
	VariableDeclarationList , VariableDeclaration

VariableDeclarationListNoIn :
	VariableDeclarationNoIn
	VariableDeclarationListNoIn , VariableDeclarationNoIn

VariableDeclaration :
	Identifier Initialiseropt

VariableDeclarationNoIn :
	Identifier InitialiserNoInopt

Initialiser :
	= AssignmentExpression

InitialiserNoIn :
	= AssignmentExpressionNoIn

EmptyStatement :
	;

ExpressionStatement :
	[lookahead != { function] Expression ;

IfStatement :
	if ( Expression ) Statement else Statement
	if ( Expression ) Statement

IterationStatement :
	do Statement while ( Expression ) ;
	while ( Expression ) Statement
	for ( ExpressionNoInopt ; Expressionopt ; Expressionopt ) Statement
	for ( var VariableDeclarationListNoIn ; Expressionopt ; Expressionopt ) Statement
	for ( LeftHandSideExpression in Expression ) Statement
	for ( var VariableDeclarationNoIn in Expression ) Statement

ContinueStatement :
	continue ;
	continue [no LineTerminator here] Identifier ;

BreakStatement :
	break ;
	break [no LineTerminator here] Identifier ;

ReturnStatement :
	return ;
	return [no LineTerminator here] Expression ;

WithStatement :
	with ( Expression ) Statement

SwitchStatement :
	switch ( Expression ) CaseBlock

CaseBlock :
	{ CaseClausesopt }
	{ CaseClausesopt DefaultClause CaseClausesopt }

CaseClauses :
	CaseClause
	CaseClauses CaseClause

CaseClause :
	case Expression : StatementListopt

DefaultClause :
	default : StatementListopt

LabelledStatement :
	Identifier : Statement

ThrowStatement :
	throw [no LineTerminator here] Expression ;

TryStatement :
	try Block Catch
	try Block Finally
	try Block Catch Finally

Catch :
	catch ( Identifier ) Block

Finally :
	finally Block

DebuggerStatement :
	debugger ;

# A.5 Functions and Programs

FunctionDeclaration :
	function Identifier ( FormalParameterListopt ) { FunctionBody }

FunctionExpression :
	function Identifieropt ( FormalParameterListopt ) { FunctionBody }

FormalParameterList :
	Identifier
	FormalParameterList , Identifier

FunctionBody :
	SourceElementsopt

Program :
	SourceElementsopt

SourceElements :
	SourceElement
	SourceElements SourceElement

SourceElement :
	Statement
	FunctionDeclaration

# A.6 Universal Resource Identifier Character Classes (skipped)

# A.7 Regular Expressions

Pattern ::
	Disjunction

Disjunction ::
	Alternative
	Alternative | Disjunction

Alternative ::
	[empty]
	Alternative Term

Term ::
	Assertion
	Atom
	Atom Quantifier

Assertion ::
	^
	$
	\ b
	\ B
	( ? = Disjunction )
	( ? ! Disjunction )

Quantifier ::
	QuantifierPrefix
	QuantifierPrefix ?

QuantifierPrefix ::
	*
	+
	?
	{ DecimalDigits }
	{ DecimalDigits , }
	{ DecimalDigits , DecimalDigits }

Atom ::
	PatternCharacter
	.
	\ AtomEscape
	CharacterClass
	( Disjunction )
	( ? : Disjunction )

PatternCharacter ::
	SourceCharacter but not one of ^ $ \ . * + ? ( ) [ ] { } |

AtomEscape ::
	DecimalEscape
	CharacterEscape
	CharacterClassEscape

CharacterEscape ::
	ControlEscape
	c ControlLetter
	HexEscapeSequence
	UnicodeEscapeSequence
	IdentityEscape

ControlEscape ::
	f n r t v

ControlLetter :: one of
	a b c d e f g h i j k l m n o p q r s t u v w x y z
	A B C D E F G H I J K L M N O P Q R S T U V W X Y Z

IdentityEscape ::
	SourceCharacter but not IdentifierPart
	<ZWJ>
	<ZWNJ>

DecimalEscape ::
	DecimalIntegerLiteral [lookahead != DecimalDigit]

CharacterClassEscape :: one of
	d D s S w W

CharacterClass ::
	[ [lookahead != ^] ClassRanges ]
	[ ^ ClassRanges ]

ClassRanges ::
	[empty]
	NonemptyClassRanges

NonemptyClassRanges ::
	ClassAtom
	ClassAtom NonemptyClassRangesNoDash
	ClassAtom – ClassAtom ClassRanges

NonemptyClassRangesNoDash ::
	ClassAtom
	ClassAtomNoDash NonemptyClassRangesNoDash
	ClassAtomNoDash – ClassAtom ClassRanges

ClassAtom ::
	-
	ClassAtomNoDash

ClassAtomNoDash ::
	SourceCharacter but not \ or ] or -
	\ ClassEscape

ClassEscape ::
	DecimalEscape
	b
	CharacterEscape
	CharacterClassEscape

# A.8 JSON

# A.8.1 JSON Lexical Grammar

JSONWhiteSpace ::
	<TAB>
	<CR>
	<LF>
	<SP>

JSONString ::
	" JSONStringCharactersopt "

JSONStringCharacters ::
	JSONStringCharacter JSONStringCharactersopt

JSONStringCharacter ::
	SourceCharacter but not one of " or \ or U+0000 through U+001F
	\ JSONEscapeSequence

JSONEscapeSequence ::
	JSONEscapeCharacter
	UnicodeEscapeSequence

JSONEscapeCharacter :: one of
	" / \ b f n r t

JSONNumber ::
	-opt DecimalIntegerLiteral JSONFractionopt ExponentPartopt

JSONFraction ::
	. DecimalDigits

JSONNullLiteral ::
	NullLiteral

JSONBooleanLiteral ::
	BooleanLiteral

# A.8.2 JSON Syntactic Grammar

JSONText :
	JSONValue

JSONValue :
	JSONNullLiteral
	JSONBooleanLiteral
	JSONObject
	JSONArray
	JSONString
	JSONNumber

JSONObject :
	{ }
	{ JSONMemberList }

JSONMember :
	JSONString : JSONValue

JSONMemberList :
	JSONMember
	JSONMemberList , JSONMember

JSONArray :
	[ ]
	[ JSONElementList ]

JSONElementList :
	JSONValue
	JSONElementList , JSONValue
