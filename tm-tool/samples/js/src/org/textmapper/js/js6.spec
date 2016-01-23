# Standard ECMA-262, 6th Edition / June 2015
# ECMAScript 2015 Language Specification

# A.1	Lexical Grammar

#  [+aaa] if aaa is set
#  [~aaa] if aaa is not set

SourceCharacter ::
	any Unicode code unit

InputElementDiv ::
	WhiteSpace
	LineTerminator
	Comment
	CommonToken
	DivPunctuator
	RightBracePunctuator


InputElementRegExp ::
	WhiteSpace
	LineTerminator
	Comment
	CommonToken
	RightBracePunctuator
	RegularExpressionLiteral

InputElementRegExpOrTemplateTail ::
	WhiteSpace
	LineTerminator
	Comment
	CommonToken
	RegularExpressionLiteral
	TemplateSubstitutionTail

InputElementTemplateTail ::
	WhiteSpace
	LineTerminator
	Comment
	CommonToken
	DivPunctuator
	TemplateSubstitutionTail

WhiteSpace ::
	<TAB>
	<VT>
	<FF>
	<SP>
	<NBSP>
	<ZWNBSP>
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

#Token ::
#	IdentifierName
#	Punctuator
#	NumericLiteral
#	StringLiteral
#
#Identifier ::
#	IdentifierName but not ReservedWord

CommonToken ::
	IdentifierName
	Punctuator
	NumericLiteral
	StringLiteral
	Template

IdentifierName ::
	IdentifierStart
	IdentifierName IdentifierPart

IdentifierStart ::
	UnicodeIDStart
	$
	_
	\ UnicodeEscapeSequence

IdentifierPart ::
	UnicodeIDContinue
	$
	_
	\ UnicodeEscapeSequence
	<ZWNJ>
	<ZWJ>

UnicodeIDStart ::
	any Unicode code point with the Unicode property “ID_Start” or “Other_ID_Start”

UnicodeIDContinue ::
	any Unicode code point with the Unicode property “ID_Continue”, “Other_ID_Continue”, or “Other_ID_Start”

ReservedWord ::
	Keyword
	FutureReservedWord
	NullLiteral
	BooleanLiteral

Keyword :: one of
	break 		do 			in 			typeof
	case 		else 		instanceof	var
	catch		export		new			void
	class		extends		return		while
	const		finally		super		with
	continue	for			switch		yield
	debugger	function	this
	default		if			throw
	delete		import		try
# TODO: smart keywords? (used in PropertyAssignment)
	get set

FutureReservedWord :: one of
	enum await
#	or in strict mode code one of
#	implements	package		protected
#	interface	private		public

Punctuator :: one of
	{	}	(	)	[	]
	.	;	,	<	>	<=
	>=	==	!=	===	!==
	+	-	*	%	++	--
	<<	>>	>>>	&	|	^
	!	~	&&	||	?	:
	=	+=	-=	*=	%=	<<=
	>>=	>>>= &=	|=	^=	=>

DivPunctuator :: one of
	/   /=

RightBracePunctuator :: one of
	}

NullLiteral ::
	null

BooleanLiteral ::
	true
	false

NumericLiteral ::
	DecimalLiteral
	BinaryIntegerLiteral
	OctalIntegerLiteral
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

BinaryIntegerLiteral ::
	0 b BinaryDigits
	0 B BinaryDigits

BinaryDigits ::
	BinaryDigit
	BinaryDigits BinaryDigit

BinaryDigit :: one of
	0 1

OctalIntegerLiteral ::
	0 o OctalDigits
	0 O OctalDigits

OctalDigits ::
	OctalDigit
	OctalDigits OctalDigit

OctalDigit ::
	0 1 2 3 4 5 6 7

HexIntegerLiteral ::
	0 x HexDigits
	0 X HexDigits

HexDigits ::
	HexDigit
	HexDigits HexDigit

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
	u { HexDigits }

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

Template ::
	NoSubstitutionTemplate
	TemplateHead

NoSubstitutionTemplate ::
	` TemplateCharactersopt `

TemplateHead ::
	` TemplateCharactersopt $ {

TemplateSubstitutionTail ::
	TemplateMiddle
	TemplateTail

TemplateMiddle ::
	} TemplateCharactersopt ${

TemplateTail ::
	} TemplateCharactersopt `

TemplateCharacters ::
	TemplateCharacter TemplateCharactersopt

TemplateCharacter ::
	$ [lookahead != { ]
	\ EscapeSequence
	LineContinuation
	LineTerminatorSequence
	SourceCharacter but not ` or \ or $ or LineTerminator

# A.2 Expressions

IdentifierReference[Yield] :
	Identifier
	[~Yield] yield

BindingIdentifier[Yield] :
	Identifier
	[~Yield] yield

LabelIdentifier[Yield] :
	Identifier
	[~Yield] yield

Identifier :
	IdentifierName but not ReservedWord

PrimaryExpression[Yield] :
	this
	IdentifierReference[?Yield]
    Literal
    ArrayLiteral[?Yield]
    ObjectLiteral[?Yield]
    FunctionExpression
    ClassExpression[?Yield]
    GeneratorExpression
    RegularExpressionLiteral
    TemplateLiteral[?Yield]
    CoverParenthesizedExpressionAndArrowParameterList[?Yield]

CoverParenthesizedExpressionAndArrowParameterList[Yield] :
	( Expression[In, ?Yield] )
	( )
	( . . . BindingIdentifier[?Yield] )
	( Expression[In, ?Yield] , . . . BindingIdentifier[?Yield] )

# When processing the production
# PrimaryExpression[Yield] : CoverParenthesizedExpressionAndArrowParameterList[?Yield]
# the interpretation of CoverParenthesizedExpressionAndArrowParameterList is refined
# using the following grammar:

ParenthesizedExpression[Yield] :
  ( Expression[In, ?Yield] )

Literal ::
	NullLiteral
	BooleanLiteral
	NumericLiteral
	StringLiteral

ArrayLiteral[Yield] :
	[ Elisionopt ]
	[ ElementList[?Yield] ]
	[ ElementList[?Yield] , Elisionopt ]

ElementList[Yield] :
	Elisionopt AssignmentExpression[In, ?Yield]
	Elisionopt SpreadElement[?Yield]
	ElementList[?Yield] , Elisionopt AssignmentExpression[In, ?Yield]
	ElementList[?Yield] , Elisionopt SpreadElement[?Yield]

Elision :
	,
	Elision ,

SpreadElement[Yield] :
	. . . AssignmentExpression[In, ?Yield]

ObjectLiteral[Yield] :
	{ }
	{ PropertyDefinitionList[?Yield] }
	{ PropertyDefinitionList[?Yield] , }

PropertyDefinitionList[Yield] :
	PropertyDefinition[?Yield]
	PropertyDefinitionList[?Yield] , PropertyDefinition[?Yield]

PropertyDefinition[Yield] :
	IdentifierReference[?Yield]
	CoverInitializedName[?Yield]
	PropertyName[?Yield] : AssignmentExpression[In, ?Yield]
	MethodDefinition[?Yield]

PropertyName[Yield] :
	LiteralPropertyName
	ComputedPropertyName[?Yield]

LiteralPropertyName :
	IdentifierName
	StringLiteral
	NumericLiteral

ComputedPropertyName[Yield] :
	[ AssignmentExpression[In, ?Yield] ]

CoverInitializedName[Yield] :
	IdentifierReference[?Yield] Initializer[In, ?Yield]

Initializer[In, Yield] :
	= AssignmentExpression[?In, ?Yield]

TemplateLiteral[Yield] :
	NoSubstitutionTemplate
	TemplateHead Expression[In, ?Yield] TemplateSpans[?Yield]

TemplateSpans[Yield] :
	TemplateTail
	TemplateMiddleList[?Yield] TemplateTail

TemplateMiddleList[Yield] :
	TemplateMiddle Expression[In, ?Yield]
	TemplateMiddleList[?Yield] TemplateMiddle Expression[In, ?Yield]

MemberExpression[Yield] :
	PrimaryExpression[?Yield]
	MemberExpression[?Yield] [ Expression[In, ?Yield] ]
	MemberExpression[?Yield] . IdentifierName
	MemberExpression[?Yield] TemplateLiteral[?Yield]
	SuperProperty[?Yield]
	MetaProperty
	new MemberExpression[?Yield] Arguments[?Yield]

SuperProperty[Yield] :
	super [ Expression[In, ?Yield] ]
	super . IdentifierName

MetaProperty :
	NewTarget

NewTarget :
	new . target

NewExpression[Yield] :
	MemberExpression[?Yield]
	new NewExpression[?Yield]

CallExpression[Yield] :
	MemberExpression[?Yield] Arguments[?Yield]
	SuperCall[?Yield]
	CallExpression[?Yield] Arguments[?Yield]
	CallExpression[?Yield] [ Expression[In, ?Yield] ]
	CallExpression[?Yield] . IdentifierName
	CallExpression[?Yield] TemplateLiteral[?Yield]

SuperCall[Yield] :
	super Arguments[?Yield]

Arguments[Yield] :
	( )
	( ArgumentList[?Yield] )

ArgumentList[Yield] :
	AssignmentExpression[In, ?Yield]
	. . . AssignmentExpression[In, ?Yield]
	ArgumentList[?Yield] , AssignmentExpression[In, ?Yield]
	ArgumentList[?Yield] , . . . AssignmentExpression[In, ?Yield]

LeftHandSideExpression[Yield] :
	NewExpression[?Yield]
	CallExpression[?Yield]

PostfixExpression[Yield] :
	LeftHandSideExpression[?Yield]
	LeftHandSideExpression[?Yield] [no LineTerminator here] ++
	LeftHandSideExpression[?Yield] [no LineTerminator here] --

UnaryExpression[Yield] :
	PostfixExpression[?Yield]
	delete UnaryExpression[?Yield]
	void UnaryExpression[?Yield]
	typeof UnaryExpression[?Yield]
	++ UnaryExpression[?Yield]
	-- UnaryExpression[?Yield]
	+ UnaryExpression[?Yield]
	- UnaryExpression[?Yield]
	~ UnaryExpression[?Yield]
	! UnaryExpression[?Yield]

MultiplicativeExpression[Yield] :
	UnaryExpression[?Yield]
	MultiplicativeExpression[?Yield] MultiplicativeOperator UnaryExpression[?Yield]

MultiplicativeOperator : one of
	* / %

AdditiveExpression[Yield] :
	MultiplicativeExpression[?Yield]
	AdditiveExpression[?Yield] + MultiplicativeExpression[?Yield]
	AdditiveExpression[?Yield] - MultiplicativeExpression[?Yield]

ShiftExpression[Yield] : See 12.8
	AdditiveExpression[?Yield]
	ShiftExpression[?Yield] << AdditiveExpression[?Yield]
	ShiftExpression[?Yield] >> AdditiveExpression[?Yield]
	ShiftExpression[?Yield] >>> AdditiveExpression[?Yield]

RelationalExpression[In, Yield] :
	ShiftExpression[?Yield]
	RelationalExpression[?In, ?Yield] < ShiftExpression[?Yield]
	RelationalExpression[?In, ?Yield] > ShiftExpression[?Yield]
	RelationalExpression[?In, ?Yield] <= ShiftExpression[? Yield]
	RelationalExpression[?In, ?Yield] >= ShiftExpression[?Yield]
	RelationalExpression[?In, ?Yield] instanceof ShiftExpression[?Yield]
	[+In] RelationalExpression[In, ?Yield] in ShiftExpression[?Yield]

EqualityExpression[In, Yield] :
	RelationalExpression[?In, ?Yield]
	EqualityExpression[?In, ?Yield] == RelationalExpression[?In, ?Yield]
	EqualityExpression[?In, ?Yield] != RelationalExpression[?In, ?Yield]
	EqualityExpression[?In, ?Yield] === RelationalExpression[?In, ?Yield]
	EqualityExpression[?In, ?Yield] !== RelationalExpression[?In, ?Yield]

BitwiseANDExpression[In, Yield] :
	EqualityExpression[?In, ?Yield]
	BitwiseANDExpression[?In, ?Yield] & EqualityExpression[?In, ?Yield]

BitwiseXORExpression[In, Yield] :
	BitwiseANDExpression[?In, ?Yield]
	BitwiseXORExpression[?In, ?Yield] ^ BitwiseANDExpression[?In, ?Yield]

BitwiseORExpression[In, Yield] :
	BitwiseXORExpression[?In, ?Yield]
	BitwiseORExpression[?In, ?Yield] | BitwiseXORExpression[?In, ?Yield]

LogicalANDExpression[In, Yield] :
	BitwiseORExpression[?In, ?Yield]
	LogicalANDExpression[?In, ?Yield] && BitwiseORExpression[?In, ?Yield]

LogicalORExpression[In, Yield] :
	LogicalANDExpression[?In, ?Yield]
	LogicalORExpression[?In, ?Yield] || LogicalANDExpression[?In, ?Yield]

ConditionalExpression[In, Yield] :
	LogicalORExpression[?In, ?Yield]
	LogicalORExpression[?In, ?Yield] ? AssignmentExpression[In, ?Yield] : AssignmentExpression[?In, ?Yield]

AssignmentExpression[In, Yield] :
	ConditionalExpression[?In, ?Yield]
	[+Yield] YieldExpression[?In]
	ArrowFunction[?In, ?Yield]
	LeftHandSideExpression[?Yield] = AssignmentExpression[?In, ?Yield]
	LeftHandSideExpression[?Yield] AssignmentOperator AssignmentExpression[?In, ?Yield]

AssignmentOperator : one of
	*= /= %= += -= <<= >>= >>>= &= ^= |=

Expression[In, Yield] :
	AssignmentExpression[?In, ?Yield]
	Expression[?In, ?Yield] , AssignmentExpression[?In, ?Yield]

# A.3 Statements

Statement[Yield, Return] :
	BlockStatement[?Yield, ?Return]
	VariableStatement[?Yield]
	EmptyStatement
	ExpressionStatement[?Yield]
	IfStatement[?Yield, ?Return]
	BreakableStatement[?Yield, ?Return]
	ContinueStatement[?Yield]
	BreakStatement[?Yield]
	[+Return] ReturnStatement[?Yield]
	WithStatement[?Yield, ?Return]
	LabelledStatement[?Yield, ?Return]
	ThrowStatement[?Yield]
	TryStatement[?Yield, ?Return]
	DebuggerStatement

Declaration[Yield] :
	HoistableDeclaration[?Yield]
	ClassDeclaration[?Yield]
	LexicalDeclaration[In, ?Yield]

HoistableDeclaration[Yield, Default] :
	FunctionDeclaration[?Yield,?Default]
	GeneratorDeclaration[?Yield, ?Default]

BreakableStatement[Yield, Return] :
	IterationStatement[?Yield, ?Return]
	SwitchStatement[?Yield, ?Return]

BlockStatement[Yield, Return] :
	Block[?Yield, ?Return]

Block[Yield, Return] :
	{ StatementList[?Yield, ?Return]opt }

StatementList[Yield, Return] :
	StatementListItem[?Yield, ?Return]
	StatementList[?Yield, ?Return] StatementListItem[?Yield, ?Return]

StatementListItem[Yield, Return] :
	Statement[?Yield, ?Return]
	Declaration[?Yield]

LexicalDeclaration[In, Yield] :
	LetOrConst BindingList[?In, ?Yield] ;

LetOrConst :
	let
	const

BindingList[In, Yield] :
	LexicalBinding[?In, ?Yield]
	BindingList[?In, ?Yield] , LexicalBinding[?In, ?Yield]

LexicalBinding[In, Yield] :
	BindingIdentifier[?Yield] Initializer[?In, ?Yield]opt
	BindingPattern[?Yield] Initializer[?In, ?Yield]

VariableStatement[Yield] :
	var VariableDeclarationList[In, ?Yield] ;

VariableDeclarationList[In, Yield] :
	VariableDeclaration[?In, ?Yield]
	VariableDeclarationList[?In, ?Yield] , VariableDeclaration[?In, ?Yield]

VariableDeclaration[In, Yield] :
	BindingIdentifier[?Yield] Initializer[?In, ?Yield]opt
	BindingPattern[?Yield] Initializer[?In, ?Yield]

BindingPattern[Yield] :
	ObjectBindingPattern[?Yield]
	ArrayBindingPattern[?Yield]

ObjectBindingPattern[Yield] :
	{ }
	{ BindingPropertyList[?Yield] }
	{ BindingPropertyList[?Yield] , }

ArrayBindingPattern[Yield] :
	[ Elisionopt BindingRestElement[?Yield]opt ]
	[ BindingElementList[?Yield] ]
	[ BindingElementList[?Yield] , Elisionopt BindingRestElement[?Yield]opt ]

BindingPropertyList[Yield] :
	BindingProperty[?Yield]
	BindingPropertyList[?Yield] , BindingProperty[?Yield]

BindingElementList[Yield] :
	BindingElisionElement[?Yield]
	BindingElementList[?Yield] , BindingElisionElement[?Yield]

BindingElisionElement[Yield] :
	Elisionopt BindingElement[?Yield]

BindingProperty[Yield] :
	SingleNameBinding[?Yield]
	PropertyName[?Yield] : BindingElement[?Yield]

BindingElement[Yield] :
	SingleNameBinding[?Yield]
	BindingPattern[?Yield] Initializer[In, ?Yield]opt

SingleNameBinding[Yield] :
	BindingIdentifier[?Yield] Initializer[In, ?Yield]opt

BindingRestElement[Yield] :
	. . . BindingIdentifier[?Yield]

EmptyStatement :
	;

ExpressionStatement[Yield] :
	[lookahead != {'{', function, class, let '[' }] Expression[In, ?Yield] ;

IfStatement[Yield, Return] :
	if ( Expression[In, ?Yield] ) Statement[?Yield, ?Return] else Statement[?Yield, ?Return]
	if ( Expression[In, ?Yield] ) Statement[?Yield, ?Return]

IterationStatement[Yield, Return] :
	do Statement[?Yield, ?Return] while ( Expression[In, ?Yield] ) ;
	while ( Expression[In, ?Yield] ) Statement[?Yield, ?Return]
	for ( [lookahead != {let [ }] Expression[?Yield]opt ; Expression[In, ?Yield]opt ; Expression[In, ?Yield]opt ) Statement[?Yield, ?Return]
	for ( var VariableDeclarationList[?Yield]; Expression[In, ?Yield]opt ; Expression[In, ?Yield]opt ) Statement[?Yield, ?Return]
	for ( LexicalDeclaration[?Yield] Expression[In, ?Yield]opt ; Expression[In, ?Yield]opt ) Statement[?Yield, ?Return]
	for ( [lookahead != {let [ }] LeftHandSideExpression[?Yield] in Expression[In, ?Yield] ) Statement[?Yield, ?Return]
	for ( var ForBinding[?Yield] in Expression[In, ?Yield] ) Statement[?Yield, ?Return]
	for ( ForDeclaration[?Yield] in Expression[In, ?Yield] ) Statement[?Yield, ?Return]
	for ( [lookahead != let] LeftHandSideExpression[?Yield] of AssignmentExpression[In, ?Yield] ) Statement[?Yield, ?Return]
    for ( var ForBinding[?Yield] of AssignmentExpression[In, ?Yield] ) Statement[?Yield, ?Return]
    for ( ForDeclaration[?Yield] of AssignmentExpression[In, ?Yield] ) Statement[?Yield, ?Return]

ForDeclaration[Yield] :
	LetOrConst ForBinding[?Yield]

ForBinding[Yield] :
	BindingIdentifier[?Yield]
	BindingPattern[?Yield]

ContinueStatement[Yield] :
	continue ;
	continue [no LineTerminator here] LabelIdentifier[?Yield] ;

BreakStatement[Yield] :
	break ;
	break [no LineTerminator here] LabelIdentifier[?Yield] ;

ReturnStatement[Yield] :
	return ;
	return [no LineTerminator here] Expression[In, ?Yield] ;

WithStatement[Yield, Return] :
	with ( Expression[In, ?Yield] ) Statement[?Yield, ?Return]

SwitchStatement[Yield, Return] :
	switch ( Expression[In, ?Yield] ) CaseBlock[?Yield, ?Return]

CaseBlock[Yield, Return] :
	{ CaseClauses[?Yield, ?Return]opt }
	{ CaseClauses[?Yield, ?Return]opt DefaultClause[?Yield, ?Return] CaseClauses[?Yield, ?Return]opt }

CaseClauses[Yield, Return] :
	CaseClause[?Yield, ?Return]
	CaseClauses[?Yield, ?Return] CaseClause[?Yield, ?Return]

CaseClause[Yield, Return] :
	case Expression[In, ?Yield] : StatementList[?Yield, ?Return]opt

DefaultClause[Yield, Return] :
	default : StatementList[?Yield, ?Return]opt

LabelledStatement[Yield, Return] :
	LabelIdentifier[?Yield] : LabelledItem[?Yield, ?Return]

LabelledItem[Yield, Return] :
	Statement[?Yield, ?Return]
	FunctionDeclaration[?Yield]

ThrowStatement[Yield] :
	throw [no LineTerminator here] Expression[In, ?Yield] ;

TryStatement[Yield, Return] :
	try Block[?Yield, ?Return] Catch[?Yield, ?Return]
	try Block[?Yield, ?Return] Finally[?Yield, ?Return]
	try Block[?Yield, ?Return] Catch[?Yield, ?Return] Finally[?Yield, ?Return]

Catch[Yield, Return] :
	catch ( CatchParameter[?Yield] ) Block[?Yield, ?Return]

Finally[Yield, Return] :
	finally Block[?Yield, ?Return]

CatchParameter[Yield] :
	BindingIdentifier[?Yield]
	BindingPattern[?Yield]

DebuggerStatement :
	debugger ;

# A.4 Functions and Classes

FunctionDeclaration[Yield, Default] :
	function BindingIdentifier[?Yield] ( FormalParameters ) { FunctionBody }
	[+Default] function ( FormalParameters ) { FunctionBody }

FunctionExpression :
	function BindingIdentifieropt ( FormalParameters ) { FunctionBody }

StrictFormalParameters[Yield] :
	FormalParameters[?Yield]

FormalParameters[Yield] :
	[empty]
	FormalParameterList[?Yield]

FormalParameterList[Yield] :
	FunctionRestParameter[?Yield]
	FormalsList[?Yield]
	FormalsList[?Yield] , FunctionRestParameter[?Yield]

FormalsList[Yield] :
	FormalParameter[?Yield]
	FormalsList[?Yield] , FormalParameter[?Yield]

FunctionRestParameter[Yield] :
	BindingRestElement[?Yield]

FormalParameter[Yield] :
	BindingElement[?Yield]

FunctionBody[Yield] :
	FunctionStatementList[?Yield]

FunctionStatementList[Yield] :
	StatementList[?Yield, Return]opt

ArrowFunction[In, Yield] :
	ArrowParameters[?Yield] [no LineTerminator here] => ConciseBody[?In]

ArrowParameters[Yield] :
	BindingIdentifier[?Yield]
	CoverParenthesizedExpressionAndArrowParameterList[?Yield]

ConciseBody[In] :
	[lookahead != {] AssignmentExpression[?In]
	{ FunctionBody }

# When the production
#  ArrowParameters[Yield] : CoverParenthesizedExpressionAndArrowParameterList[?Yield]
#  is recognized the following grammar is used to refine the interpretation of
#  CoverParenthesizedExpressionAndArrowParameterList:

ArrowFormalParameters[Yield] :
	( StrictFormalParameters[?Yield] )

MethodDefinition[Yield] :
	PropertyName[?Yield] ( StrictFormalParameters ) { FunctionBody }
	GeneratorMethod[?Yield]
	get PropertyName[?Yield] ( ) { FunctionBody }
	set PropertyName[?Yield] ( PropertySetParameterList ) { FunctionBody }

PropertySetParameterList :
	FormalParameter

GeneratorMethod[Yield] :
	* PropertyName[?Yield] (StrictFormalParameters[Yield] ) { GeneratorBody }

GeneratorDeclaration[Yield, Default] :
	function * BindingIdentifier[?Yield] ( FormalParameters[Yield] ) { GeneratorBody }
	[+Default] function * ( FormalParameters[Yield] ) { GeneratorBody }

GeneratorExpression :
	function * BindingIdentifier[Yield]opt ( FormalParameters[Yield] ) { GeneratorBody }

GeneratorBody :
	FunctionBody[Yield]

YieldExpression[In] :
	yield
	yield [no LineTerminator here] AssignmentExpression[?In, Yield]
	yield [no LineTerminator here] * AssignmentExpression[?In, Yield]

ClassDeclaration[Yield, Default] :
	class BindingIdentifier[?Yield] ClassTail[?Yield]
	[+Default] class ClassTail[?Yield]

ClassExpression[Yield] :
	class BindingIdentifier[?Yield]opt ClassTail[?Yield]

ClassTail[Yield] :
	ClassHeritage[?Yield]opt { ClassBody[?Yield]opt }

ClassHeritage[Yield] :
	extends LeftHandSideExpression[?Yield]

ClassBody[Yield] :
	ClassElementList[?Yield]

ClassElementList[Yield] :
	ClassElement[?Yield]
	ClassElementList[?Yield] ClassElement[?Yield]

ClassElement[Yield] :
	MethodDefinition[?Yield]
	static MethodDefinition[?Yield]
	;

# A.5 Scripts and Modules

Script :
	ScriptBodyopt

ScriptBody :
	StatementList

Module :
	ModuleBodyopt

ModuleBody :
	ModuleItemList

ModuleItemList :
	ModuleItem
	ModuleItemList ModuleItem

ModuleItem :
	ImportDeclaration
	ExportDeclaration
	StatementListItem

ImportDeclaration :
	import ImportClause FromClause ;
	import ModuleSpecifier ;

ImportClause :
	ImportedDefaultBinding
	NameSpaceImport
	NamedImports
	ImportedDefaultBinding , NameSpaceImport
	ImportedDefaultBinding , NamedImports

ImportedDefaultBinding :
	ImportedBinding

NameSpaceImport :
	* as ImportedBinding

NamedImports :
	{ }
	{ ImportsList }
	{ ImportsList , }

FromClause :
	from ModuleSpecifier

ImportsList :
	ImportSpecifier
	ImportsList , ImportSpecifier

ImportSpecifier :
	ImportedBinding
	IdentifierName as ImportedBinding

ModuleSpecifier :
	StringLiteral

ImportedBinding :
	BindingIdentifier

ExportDeclaration :
	export * FromClause ;
	export ExportClause FromClause ;
	export ExportClause ;
	export VariableStatement
	export Declaration
	export default HoistableDeclaration[Default]
	export default ClassDeclaration[Default]
	export default [lookahead != { function, class }] AssignmentExpression[In] ;

ExportClause :
	{ }
	{ ExportsList }
	{ ExportsList , }

ExportsList :
	ExportSpecifier
	ExportsList , ExportSpecifier

ExportSpecifier :
	IdentifierName
	IdentifierName as IdentifierName

# A.6 Number Conversions (skipped)

# A.7 Universal Resource Identifier Character Classes (skipped)

# A.8 Regular Expressions

Pattern[U] ::
	Disjunction[?U]

Disjunction[U] ::
	Alternative[?U]
	Alternative[?U] | Disjunction[?U]

Alternative[U] ::
	[empty]
	Alternative[?U] Term[?U]

Term[U] ::
	Assertion[?U]
	Atom[?U]
	Atom[?U] Quantifier

Assertion[U] ::
	^
	$
	\ b
	\ B
	( ? = Disjunction[?U] )
	( ? ! Disjunction[?U] )

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

Atom[U] ::
	PatternCharacter
	.
	\ AtomEscape[?U]
	CharacterClass[?U]
	( Disjunction[?U] )
	( ? : Disjunction[?U] )

SyntaxCharacter ::
	^ $ \ . * + ? ( ) [ ] { } |

PatternCharacter ::
	SourceCharacter but not SyntaxCharacter

AtomEscape[U] ::
	DecimalEscape
	CharacterEscape[?U]
	CharacterClassEscape

CharacterEscape[U] ::
	ControlEscape
	c ControlLetter
	HexEscapeSequence
	RegExpUnicodeEscapeSequence[?U]
	IdentityEscape[?U]

ControlEscape :: one of
	f n r t v

ControlLetter :: one of
	a b c d e f g h i j k l m n o p q r s t u v w x y z
	A B C D E F G H I J K L M N O P Q R S T U V W X Y Z

RegExpUnicodeEscapeSequence[U] ::
	[+U] u LeadSurrogate \u TrailSurrogate
	[+U] u LeadSurrogate
	[+U] u TrailSurrogate
	[+U] u NonSurrogate
	[~U] u Hex4Digits
	[+U] u { HexDigits }

# Each \u TrailSurrogate for which the choice of associated u LeadSurrogate is ambiguous shall be
# associated with the nearest possible u LeadSurrogate that would otherwise have no corresponding
# \u TrailSurrogate.

LeadSurrogate ::
	Hex4Digits [match only if the SV of Hex4Digits is in the inclusive range 0xD800 to 0xDBFF]

TrailSurrogate ::
	Hex4Digits [match only if the SV of Hex4Digits is in the inclusive range 0xDC00 to 0xDFFF]

NonSurrogate ::
	Hex4Digits [match only if the SV of Hex4Digits is not in the inclusive range 0xD800 to 0xDFFF]

IdentityEscape[U] ::
	[+U] SyntaxCharacter
	[+U] /
	[~U] SourceCharacter but not UnicodeIDContinue

DecimalEscape ::
	DecimalIntegerLiteral [lookahead != DecimalDigit]

CharacterClassEscape :: one of
	d D s S w W

CharacterClass[U] ::
	[ [lookahead != {^}] ClassRanges[?U] ]
	[ ^ ClassRanges[?U] ]

ClassRanges[U] ::
	[empty]
	NonemptyClassRanges[?U]

NonemptyClassRanges[U] ::
	ClassAtom[?U]
	ClassAtom[?U] NonemptyClassRangesNoDash[?U]
	ClassAtom[?U] - ClassAtom[?U] ClassRanges[?U]

NonemptyClassRangesNoDash[U] ::
	ClassAtom[?U]
	ClassAtomNoDash[?U] NonemptyClassRangesNoDash[?U]
	ClassAtomNoDash[?U] - ClassAtom[?U] ClassRanges[?U]

ClassAtom[U] ::
	-
	ClassAtomNoDash[?U]

ClassAtomNoDash[U] ::
	SourceCharacter but not \ or ] or -
	\ ClassEscape[?U]

ClassEscape[U] ::
	DecimalEscape
	b
	[+U] -
	CharacterEscape[?U]
	CharacterClassEscape
