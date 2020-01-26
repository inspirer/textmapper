%{
%}

%start Module

%right else
%token WhiteSpace
%token LineTerminatorSequence
%token MultiLineComment
%token SingleLineComment
%token Identifier
%token break
%token case
%token catch
%token class
%token const
%token continue
%token debugger
%token default
%token delete
%token do
%token export
%token extends
%token finally
%token for
%token function
%token if
%token import
%token in
%token instanceof
%token new
%token return
%token super
%token switch
%token this
%token throw
%token try
%token typeof
%token var
%token void
%token while
%token with
%token yield
%token await
%token enum
%token null
%token true
%token false
%token as
%token from
%token get
%token let
%token of
%token set
%token static
%token target
%token Lbrace
%token Lparen
%token Rparen
%token Lbrack
%token Rbrack
%token Dot
%token Semicolon
%token Comma
%token Lt
%token Gt
%token LtAssign
%token GtAssign
%token AssignAssign
%token ExclAssign
%token AssignAssignAssign
%token ExclAssignAssign
%token Plus
%token Minus
%token Mult
%token Rem
%token PlusPlus
%token MinusMinus
%token LtLt
%token GtGt
%token GtGtGt
%token And
%token Or
%token Xor
%token Excl
%token Tilde
%token AndAnd
%token OrOr
%token Quest
%token Colon
%token Assign
%token PlusAssign
%token MinusAssign
%token MultAssign
%token RemAssign
%token LtLtAssign
%token GtGtAssign
%token GtGtGtAssign
%token AndAssign
%token OrAssign
%token XorAssign
%token AssignGt
%token NumericLiteral
%token StringLiteral
%token Rbrace
%token NoSubstitutionTemplate
%token TemplateHead
%token TemplateMiddle
%token TemplateTail
%token RegularExpressionLiteral
%token Div
%token DivAssign

%%

IdentifierName :
  Identifier
| break
| do
| in
| typeof
| case
| else
| instanceof
| var
| catch
| export
| new
| void
| class
| extends
| return
| while
| const
| finally
| super
| with
| continue
| for
| switch
| yield
| debugger
| function
| this
| default
| if
| throw
| delete
| import
| try
| enum
| await
| null
| true
| false
| as
| from
| get
| let
| of
| set
| static
| target
;

IdentifierReference :
  Identifier
| yield
| let
| as
| from
| get
| of
| set
| static
| target
;

IdentifierReference_NoLet :
  Identifier
| yield
| as
| from
| get
| of
| set
| static
| target
;

IdentifierReference_NoLet_Yield :
  Identifier
| as
| from
| get
| of
| set
| static
| target
;

IdentifierReference_Yield :
  Identifier
| let
| as
| from
| get
| of
| set
| static
| target
;

BindingIdentifier :
  Identifier
| yield
| as
| from
| get
| let
| of
| set
| static
| target
;

BindingIdentifier_Yield :
  Identifier
| as
| from
| get
| let
| of
| set
| static
| target
;

LabelIdentifier :
  Identifier
| yield
| as
| from
| get
| let
| of
| set
| static
| target
;

LabelIdentifier_Yield :
  Identifier
| as
| from
| get
| let
| of
| set
| static
| target
;

PrimaryExpression :
  this
| IdentifierReference
| Literal
| ArrayLiteral
| ObjectLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_NoFuncClass :
  this
| IdentifierReference
| Literal
| ArrayLiteral
| ObjectLiteral
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_NoFuncClass_NoLet :
  this
| IdentifierReference_NoLet
| Literal
| ArrayLiteral
| ObjectLiteral
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral :
  this
| IdentifierReference_NoLet
| Literal
| ArrayLiteral
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield :
  this
| IdentifierReference_NoLet_Yield
| Literal
| ArrayLiteral_Yield
| RegularExpressionLiteral
| TemplateLiteral_Yield
| CoverParenthesizedExpressionAndArrowParameterList_Yield
;

PrimaryExpression_NoFuncClass_NoObjLiteral :
  this
| IdentifierReference
| Literal
| ArrayLiteral
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_NoFuncClass_NoObjLiteral_Yield :
  this
| IdentifierReference_Yield
| Literal
| ArrayLiteral_Yield
| RegularExpressionLiteral
| TemplateLiteral_Yield
| CoverParenthesizedExpressionAndArrowParameterList_Yield
;

PrimaryExpression_NoLet :
  this
| IdentifierReference_NoLet
| Literal
| ArrayLiteral
| ObjectLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_NoLet_NoObjLiteral :
  this
| IdentifierReference_NoLet
| Literal
| ArrayLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_NoLet_Yield :
  this
| IdentifierReference_NoLet_Yield
| Literal
| ArrayLiteral_Yield
| ObjectLiteral_Yield
| FunctionExpression
| ClassExpression_Yield
| GeneratorExpression
| RegularExpressionLiteral
| TemplateLiteral_Yield
| CoverParenthesizedExpressionAndArrowParameterList_Yield
;

PrimaryExpression_NoObjLiteral :
  this
| IdentifierReference
| Literal
| ArrayLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| RegularExpressionLiteral
| TemplateLiteral
| CoverParenthesizedExpressionAndArrowParameterList
;

PrimaryExpression_Yield :
  this
| IdentifierReference_Yield
| Literal
| ArrayLiteral_Yield
| ObjectLiteral_Yield
| FunctionExpression
| ClassExpression_Yield
| GeneratorExpression
| RegularExpressionLiteral
| TemplateLiteral_Yield
| CoverParenthesizedExpressionAndArrowParameterList_Yield
;

CoverParenthesizedExpressionAndArrowParameterList :
  Lparen Expression_In Rparen
| Lparen Rparen
| Lparen Dot Dot Dot BindingIdentifier Rparen
| Lparen Expression_In Comma Dot Dot Dot BindingIdentifier Rparen
;

CoverParenthesizedExpressionAndArrowParameterList_Yield :
  Lparen Expression_In_Yield Rparen
| Lparen Rparen
| Lparen Dot Dot Dot BindingIdentifier_Yield Rparen
| Lparen Expression_In_Yield Comma Dot Dot Dot BindingIdentifier_Yield Rparen
;

Literal :
  null
| true
| false
| NumericLiteral
| StringLiteral
;

ArrayLiteral :
  Lbrack Elisionopt Rbrack
| Lbrack ElementList Rbrack
| Lbrack ElementList Comma Elisionopt Rbrack
;

ArrayLiteral_Yield :
  Lbrack Elisionopt Rbrack
| Lbrack ElementList_Yield Rbrack
| Lbrack ElementList_Yield Comma Elisionopt Rbrack
;

ElementList :
  Elisionopt AssignmentExpression_In
| Elisionopt SpreadElement
| ElementList Comma Elisionopt AssignmentExpression_In
| ElementList Comma Elisionopt SpreadElement
;

ElementList_Yield :
  Elisionopt AssignmentExpression_In_Yield
| Elisionopt SpreadElement_Yield
| ElementList_Yield Comma Elisionopt AssignmentExpression_In_Yield
| ElementList_Yield Comma Elisionopt SpreadElement_Yield
;

Elision :
  Comma
| Elision Comma
;

SpreadElement :
  Dot Dot Dot AssignmentExpression_In
;

SpreadElement_Yield :
  Dot Dot Dot AssignmentExpression_In_Yield
;

ObjectLiteral :
  Lbrace Rbrace
| Lbrace PropertyDefinitionList Rbrace
| Lbrace PropertyDefinitionList Comma Rbrace
;

ObjectLiteral_Yield :
  Lbrace Rbrace
| Lbrace PropertyDefinitionList_Yield Rbrace
| Lbrace PropertyDefinitionList_Yield Comma Rbrace
;

PropertyDefinitionList :
  PropertyDefinition
| PropertyDefinitionList Comma PropertyDefinition
;

PropertyDefinitionList_Yield :
  PropertyDefinition_Yield
| PropertyDefinitionList_Yield Comma PropertyDefinition_Yield
;

PropertyDefinition :
  IdentifierReference
| CoverInitializedName
| PropertyName Colon AssignmentExpression_In
| MethodDefinition
;

PropertyDefinition_Yield :
  IdentifierReference_Yield
| CoverInitializedName_Yield
| PropertyName_Yield Colon AssignmentExpression_In_Yield
| MethodDefinition_Yield
;

PropertyName :
  LiteralPropertyName
| ComputedPropertyName
;

PropertyName_Yield :
  LiteralPropertyName
| ComputedPropertyName_Yield
;

LiteralPropertyName :
  IdentifierName
| StringLiteral
| NumericLiteral
;

ComputedPropertyName :
  Lbrack AssignmentExpression_In Rbrack
;

ComputedPropertyName_Yield :
  Lbrack AssignmentExpression_In_Yield Rbrack
;

CoverInitializedName :
  IdentifierReference Initializer_In
;

CoverInitializedName_Yield :
  IdentifierReference_Yield Initializer_In_Yield
;

Initializer :
  Assign AssignmentExpression
;

Initializer_In :
  Assign AssignmentExpression_In
;

Initializer_In_Yield :
  Assign AssignmentExpression_In_Yield
;

Initializer_Yield :
  Assign AssignmentExpression_Yield
;

TemplateLiteral :
  NoSubstitutionTemplate
| TemplateHead Expression_In TemplateSpans
;

TemplateLiteral_Yield :
  NoSubstitutionTemplate
| TemplateHead Expression_In_Yield TemplateSpans_Yield
;

TemplateSpans :
  TemplateTail
| TemplateMiddleList TemplateTail
;

TemplateSpans_Yield :
  TemplateTail
| TemplateMiddleList_Yield TemplateTail
;

TemplateMiddleList :
  TemplateMiddle Expression_In
| TemplateMiddleList TemplateMiddle Expression_In
;

TemplateMiddleList_Yield :
  TemplateMiddle Expression_In_Yield
| TemplateMiddleList_Yield TemplateMiddle Expression_In_Yield
;

MemberExpression :
  PrimaryExpression
| MemberExpression Lbrack Expression_In Rbrack
| MemberExpression Dot IdentifierName
| MemberExpression TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoFuncClass :
  PrimaryExpression_NoFuncClass
| MemberExpression_NoFuncClass Lbrack Expression_In Rbrack
| MemberExpression_NoFuncClass Dot IdentifierName
| MemberExpression_NoFuncClass TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  PrimaryExpression_NoFuncClass_NoObjLiteral
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral Lbrack Expression_In Rbrack
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  PrimaryExpression_NoFuncClass_NoObjLiteral_Yield
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLet :
  PrimaryExpression_NoLet
| MemberExpression_NoLet Lbrack Expression_In Rbrack
| MemberExpression_NoLet Dot IdentifierName
| MemberExpression_NoLet TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLet_Yield :
  PrimaryExpression_NoLet_Yield
| MemberExpression_NoLet_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_NoLet_Yield Dot IdentifierName
| MemberExpression_NoLet_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLetOnly :
  PrimaryExpression_NoLet
| MemberExpression Lbrack Expression_In Rbrack
| MemberExpression Dot IdentifierName
| MemberExpression TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetOnly_NoFuncClass :
  PrimaryExpression_NoFuncClass_NoLet
| MemberExpression_NoFuncClass Lbrack Expression_In Rbrack
| MemberExpression_NoFuncClass Dot IdentifierName
| MemberExpression_NoFuncClass TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral :
  PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral Lbrack Expression_In Rbrack
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLetOnly_NoLet :
  PrimaryExpression_NoLet
| MemberExpression_NoLet Lbrack Expression_In Rbrack
| MemberExpression_NoLet Dot IdentifierName
| MemberExpression_NoLet TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetOnly_NoLet_Yield :
  PrimaryExpression_NoLet_Yield
| MemberExpression_NoLet_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_NoLet_Yield Dot IdentifierName
| MemberExpression_NoLet_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLetOnly_NoObjLiteral :
  PrimaryExpression_NoLet_NoObjLiteral
| MemberExpression_NoObjLiteral Lbrack Expression_In Rbrack
| MemberExpression_NoObjLiteral Dot IdentifierName
| MemberExpression_NoObjLiteral TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetOnly_StartWithLet :
  MemberExpression_NoLetOnly_StartWithLet Lbrack Expression_In Rbrack
| MemberExpression_StartWithLet Dot IdentifierName
| MemberExpression_StartWithLet TemplateLiteral
;

MemberExpression_NoLetOnly_StartWithLet_Yield :
  MemberExpression_NoLetOnly_StartWithLet_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_StartWithLet_Yield Dot IdentifierName
| MemberExpression_StartWithLet_Yield TemplateLiteral_Yield
;

MemberExpression_NoLetOnly_Yield :
  PrimaryExpression_NoLet_Yield
| MemberExpression_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_Yield Dot IdentifierName
| MemberExpression_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoObjLiteral :
  PrimaryExpression_NoObjLiteral
| MemberExpression_NoObjLiteral Lbrack Expression_In Rbrack
| MemberExpression_NoObjLiteral Dot IdentifierName
| MemberExpression_NoObjLiteral TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_StartWithLet :
  let
| MemberExpression_NoLetOnly_StartWithLet Lbrack Expression_In Rbrack
| MemberExpression_StartWithLet Dot IdentifierName
| MemberExpression_StartWithLet TemplateLiteral
;

MemberExpression_StartWithLet_Yield :
  let
| MemberExpression_NoLetOnly_StartWithLet_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_StartWithLet_Yield Dot IdentifierName
| MemberExpression_StartWithLet_Yield TemplateLiteral_Yield
;

MemberExpression_Yield :
  PrimaryExpression_Yield
| MemberExpression_Yield Lbrack Expression_In_Yield Rbrack
| MemberExpression_Yield Dot IdentifierName
| MemberExpression_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

SuperProperty :
  super Lbrack Expression_In Rbrack
| super Dot IdentifierName
;

SuperProperty_Yield :
  super Lbrack Expression_In_Yield Rbrack
| super Dot IdentifierName
;

MetaProperty :
  NewTarget
;

NewTarget :
  new Dot target
;

NewExpression :
  MemberExpression
| new NewExpression
;

NewExpression_NoFuncClass :
  MemberExpression_NoFuncClass
| new NewExpression
;

NewExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral
| new NewExpression
;

NewExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| new NewExpression_Yield
;

NewExpression_NoLet :
  MemberExpression_NoLet
| new NewExpression
;

NewExpression_NoLet_Yield :
  MemberExpression_NoLet_Yield
| new NewExpression_Yield
;

NewExpression_NoObjLiteral :
  MemberExpression_NoObjLiteral
| new NewExpression
;

NewExpression_StartWithLet :
  MemberExpression_StartWithLet
;

NewExpression_StartWithLet_Yield :
  MemberExpression_StartWithLet_Yield
;

NewExpression_Yield :
  MemberExpression_Yield
| new NewExpression_Yield
;

CallExpression :
  MemberExpression Arguments
| SuperCall
| CallExpression Arguments
| CallExpression Lbrack Expression_In Rbrack
| CallExpression Dot IdentifierName
| CallExpression TemplateLiteral
;

CallExpression_NoFuncClass :
  MemberExpression_NoFuncClass Arguments
| SuperCall
| CallExpression_NoFuncClass Arguments
| CallExpression_NoFuncClass Lbrack Expression_In Rbrack
| CallExpression_NoFuncClass Dot IdentifierName
| CallExpression_NoFuncClass TemplateLiteral
;

CallExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral Arguments
| SuperCall
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral Arguments
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral Lbrack Expression_In Rbrack
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral Dot IdentifierName
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
;

CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Arguments_Yield
| SuperCall_Yield
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Arguments_Yield
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Lbrack Expression_In_Yield Rbrack
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Dot IdentifierName
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
;

CallExpression_NoLet :
  MemberExpression_NoLet Arguments
| SuperCall
| CallExpression_NoLet Arguments
| CallExpression_NoLet Lbrack Expression_In Rbrack
| CallExpression_NoLet Dot IdentifierName
| CallExpression_NoLet TemplateLiteral
;

CallExpression_NoLet_Yield :
  MemberExpression_NoLet_Yield Arguments_Yield
| SuperCall_Yield
| CallExpression_NoLet_Yield Arguments_Yield
| CallExpression_NoLet_Yield Lbrack Expression_In_Yield Rbrack
| CallExpression_NoLet_Yield Dot IdentifierName
| CallExpression_NoLet_Yield TemplateLiteral_Yield
;

CallExpression_NoObjLiteral :
  MemberExpression_NoObjLiteral Arguments
| SuperCall
| CallExpression_NoObjLiteral Arguments
| CallExpression_NoObjLiteral Lbrack Expression_In Rbrack
| CallExpression_NoObjLiteral Dot IdentifierName
| CallExpression_NoObjLiteral TemplateLiteral
;

CallExpression_StartWithLet :
  MemberExpression_StartWithLet Arguments
| CallExpression_StartWithLet Arguments
| CallExpression_StartWithLet Lbrack Expression_In Rbrack
| CallExpression_StartWithLet Dot IdentifierName
| CallExpression_StartWithLet TemplateLiteral
;

CallExpression_StartWithLet_Yield :
  MemberExpression_StartWithLet_Yield Arguments_Yield
| CallExpression_StartWithLet_Yield Arguments_Yield
| CallExpression_StartWithLet_Yield Lbrack Expression_In_Yield Rbrack
| CallExpression_StartWithLet_Yield Dot IdentifierName
| CallExpression_StartWithLet_Yield TemplateLiteral_Yield
;

CallExpression_Yield :
  MemberExpression_Yield Arguments_Yield
| SuperCall_Yield
| CallExpression_Yield Arguments_Yield
| CallExpression_Yield Lbrack Expression_In_Yield Rbrack
| CallExpression_Yield Dot IdentifierName
| CallExpression_Yield TemplateLiteral_Yield
;

SuperCall :
  super Arguments
;

SuperCall_Yield :
  super Arguments_Yield
;

Arguments :
  Lparen Rparen
| Lparen ArgumentList Rparen
;

Arguments_Yield :
  Lparen Rparen
| Lparen ArgumentList_Yield Rparen
;

ArgumentList :
  AssignmentExpression_In
| Dot Dot Dot AssignmentExpression_In
| ArgumentList Comma AssignmentExpression_In
| ArgumentList Comma Dot Dot Dot AssignmentExpression_In
;

ArgumentList_Yield :
  AssignmentExpression_In_Yield
| Dot Dot Dot AssignmentExpression_In_Yield
| ArgumentList_Yield Comma AssignmentExpression_In_Yield
| ArgumentList_Yield Comma Dot Dot Dot AssignmentExpression_In_Yield
;

LeftHandSideExpression :
  NewExpression
| CallExpression
;

LeftHandSideExpression_NoFuncClass :
  NewExpression_NoFuncClass
| CallExpression_NoFuncClass
;

LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  NewExpression_NoFuncClass_NoLetSq_NoObjLiteral
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral
;

LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  NewExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
;

LeftHandSideExpression_NoLet :
  NewExpression_NoLet
| CallExpression_NoLet
;

LeftHandSideExpression_NoLet_Yield :
  NewExpression_NoLet_Yield
| CallExpression_NoLet_Yield
;

LeftHandSideExpression_NoObjLiteral :
  NewExpression_NoObjLiteral
| CallExpression_NoObjLiteral
;

LeftHandSideExpression_StartWithLet :
  NewExpression_StartWithLet
| CallExpression_StartWithLet
;

LeftHandSideExpression_StartWithLet_Yield :
  NewExpression_StartWithLet_Yield
| CallExpression_StartWithLet_Yield
;

LeftHandSideExpression_Yield :
  NewExpression_Yield
| CallExpression_Yield
;

PostfixExpression :
  LeftHandSideExpression
| LeftHandSideExpression PlusPlus
| LeftHandSideExpression MinusMinus
;

PostfixExpression_NoFuncClass :
  LeftHandSideExpression_NoFuncClass
| LeftHandSideExpression_NoFuncClass PlusPlus
| LeftHandSideExpression_NoFuncClass MinusMinus
;

PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral PlusPlus
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral MinusMinus
;

PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield PlusPlus
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield MinusMinus
;

PostfixExpression_NoLet :
  LeftHandSideExpression_NoLet
| LeftHandSideExpression_NoLet PlusPlus
| LeftHandSideExpression_NoLet MinusMinus
;

PostfixExpression_NoLet_Yield :
  LeftHandSideExpression_NoLet_Yield
| LeftHandSideExpression_NoLet_Yield PlusPlus
| LeftHandSideExpression_NoLet_Yield MinusMinus
;

PostfixExpression_NoObjLiteral :
  LeftHandSideExpression_NoObjLiteral
| LeftHandSideExpression_NoObjLiteral PlusPlus
| LeftHandSideExpression_NoObjLiteral MinusMinus
;

PostfixExpression_StartWithLet :
  LeftHandSideExpression_StartWithLet
| LeftHandSideExpression_StartWithLet PlusPlus
| LeftHandSideExpression_StartWithLet MinusMinus
;

PostfixExpression_StartWithLet_Yield :
  LeftHandSideExpression_StartWithLet_Yield
| LeftHandSideExpression_StartWithLet_Yield PlusPlus
| LeftHandSideExpression_StartWithLet_Yield MinusMinus
;

PostfixExpression_Yield :
  LeftHandSideExpression_Yield
| LeftHandSideExpression_Yield PlusPlus
| LeftHandSideExpression_Yield MinusMinus
;

UnaryExpression :
  PostfixExpression
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Excl UnaryExpression
;

UnaryExpression_NoFuncClass :
  PostfixExpression_NoFuncClass
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Excl UnaryExpression
;

UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Excl UnaryExpression
;

UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  PostfixExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| delete UnaryExpression_Yield
| void UnaryExpression_Yield
| typeof UnaryExpression_Yield
| PlusPlus UnaryExpression_Yield
| MinusMinus UnaryExpression_Yield
| Plus UnaryExpression_Yield
| Minus UnaryExpression_Yield
| Tilde UnaryExpression_Yield
| Excl UnaryExpression_Yield
;

UnaryExpression_NoLet :
  PostfixExpression_NoLet
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Excl UnaryExpression
;

UnaryExpression_NoLet_Yield :
  PostfixExpression_NoLet_Yield
| delete UnaryExpression_Yield
| void UnaryExpression_Yield
| typeof UnaryExpression_Yield
| PlusPlus UnaryExpression_Yield
| MinusMinus UnaryExpression_Yield
| Plus UnaryExpression_Yield
| Minus UnaryExpression_Yield
| Tilde UnaryExpression_Yield
| Excl UnaryExpression_Yield
;

UnaryExpression_NoObjLiteral :
  PostfixExpression_NoObjLiteral
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Excl UnaryExpression
;

UnaryExpression_StartWithLet :
  PostfixExpression_StartWithLet
;

UnaryExpression_StartWithLet_Yield :
  PostfixExpression_StartWithLet_Yield
;

UnaryExpression_Yield :
  PostfixExpression_Yield
| delete UnaryExpression_Yield
| void UnaryExpression_Yield
| typeof UnaryExpression_Yield
| PlusPlus UnaryExpression_Yield
| MinusMinus UnaryExpression_Yield
| Plus UnaryExpression_Yield
| Minus UnaryExpression_Yield
| Tilde UnaryExpression_Yield
| Excl UnaryExpression_Yield
;

MultiplicativeExpression :
  UnaryExpression
| MultiplicativeExpression MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_NoFuncClass :
  UnaryExpression_NoFuncClass
| MultiplicativeExpression_NoFuncClass MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral
| MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  UnaryExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield MultiplicativeOperator UnaryExpression_Yield
;

MultiplicativeExpression_NoLet :
  UnaryExpression_NoLet
| MultiplicativeExpression_NoLet MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_NoLet_Yield :
  UnaryExpression_NoLet_Yield
| MultiplicativeExpression_NoLet_Yield MultiplicativeOperator UnaryExpression_Yield
;

MultiplicativeExpression_NoObjLiteral :
  UnaryExpression_NoObjLiteral
| MultiplicativeExpression_NoObjLiteral MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_StartWithLet :
  UnaryExpression_StartWithLet
| MultiplicativeExpression_StartWithLet MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_StartWithLet_Yield :
  UnaryExpression_StartWithLet_Yield
| MultiplicativeExpression_StartWithLet_Yield MultiplicativeOperator UnaryExpression_Yield
;

MultiplicativeExpression_Yield :
  UnaryExpression_Yield
| MultiplicativeExpression_Yield MultiplicativeOperator UnaryExpression_Yield
;

MultiplicativeOperator :
  Mult
| Div
| Rem
;

AdditiveExpression :
  MultiplicativeExpression
| AdditiveExpression Plus MultiplicativeExpression
| AdditiveExpression Minus MultiplicativeExpression
;

AdditiveExpression_NoFuncClass :
  MultiplicativeExpression_NoFuncClass
| AdditiveExpression_NoFuncClass Plus MultiplicativeExpression
| AdditiveExpression_NoFuncClass Minus MultiplicativeExpression
;

AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral
| AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral Plus MultiplicativeExpression
| AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral Minus MultiplicativeExpression
;

AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  MultiplicativeExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Plus MultiplicativeExpression_Yield
| AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Minus MultiplicativeExpression_Yield
;

AdditiveExpression_NoLet :
  MultiplicativeExpression_NoLet
| AdditiveExpression_NoLet Plus MultiplicativeExpression
| AdditiveExpression_NoLet Minus MultiplicativeExpression
;

AdditiveExpression_NoLet_Yield :
  MultiplicativeExpression_NoLet_Yield
| AdditiveExpression_NoLet_Yield Plus MultiplicativeExpression_Yield
| AdditiveExpression_NoLet_Yield Minus MultiplicativeExpression_Yield
;

AdditiveExpression_NoObjLiteral :
  MultiplicativeExpression_NoObjLiteral
| AdditiveExpression_NoObjLiteral Plus MultiplicativeExpression
| AdditiveExpression_NoObjLiteral Minus MultiplicativeExpression
;

AdditiveExpression_StartWithLet :
  MultiplicativeExpression_StartWithLet
| AdditiveExpression_StartWithLet Plus MultiplicativeExpression
| AdditiveExpression_StartWithLet Minus MultiplicativeExpression
;

AdditiveExpression_StartWithLet_Yield :
  MultiplicativeExpression_StartWithLet_Yield
| AdditiveExpression_StartWithLet_Yield Plus MultiplicativeExpression_Yield
| AdditiveExpression_StartWithLet_Yield Minus MultiplicativeExpression_Yield
;

AdditiveExpression_Yield :
  MultiplicativeExpression_Yield
| AdditiveExpression_Yield Plus MultiplicativeExpression_Yield
| AdditiveExpression_Yield Minus MultiplicativeExpression_Yield
;

ShiftExpression :
  AdditiveExpression
| ShiftExpression LtLt AdditiveExpression
| ShiftExpression GtGt AdditiveExpression
| ShiftExpression GtGtGt AdditiveExpression
;

ShiftExpression_NoFuncClass :
  AdditiveExpression_NoFuncClass
| ShiftExpression_NoFuncClass LtLt AdditiveExpression
| ShiftExpression_NoFuncClass GtGt AdditiveExpression
| ShiftExpression_NoFuncClass GtGtGt AdditiveExpression
;

ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral LtLt AdditiveExpression
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral GtGt AdditiveExpression
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral GtGtGt AdditiveExpression
;

ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield LtLt AdditiveExpression_Yield
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield GtGt AdditiveExpression_Yield
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield GtGtGt AdditiveExpression_Yield
;

ShiftExpression_NoLet :
  AdditiveExpression_NoLet
| ShiftExpression_NoLet LtLt AdditiveExpression
| ShiftExpression_NoLet GtGt AdditiveExpression
| ShiftExpression_NoLet GtGtGt AdditiveExpression
;

ShiftExpression_NoLet_Yield :
  AdditiveExpression_NoLet_Yield
| ShiftExpression_NoLet_Yield LtLt AdditiveExpression_Yield
| ShiftExpression_NoLet_Yield GtGt AdditiveExpression_Yield
| ShiftExpression_NoLet_Yield GtGtGt AdditiveExpression_Yield
;

ShiftExpression_NoObjLiteral :
  AdditiveExpression_NoObjLiteral
| ShiftExpression_NoObjLiteral LtLt AdditiveExpression
| ShiftExpression_NoObjLiteral GtGt AdditiveExpression
| ShiftExpression_NoObjLiteral GtGtGt AdditiveExpression
;

ShiftExpression_StartWithLet :
  AdditiveExpression_StartWithLet
| ShiftExpression_StartWithLet LtLt AdditiveExpression
| ShiftExpression_StartWithLet GtGt AdditiveExpression
| ShiftExpression_StartWithLet GtGtGt AdditiveExpression
;

ShiftExpression_StartWithLet_Yield :
  AdditiveExpression_StartWithLet_Yield
| ShiftExpression_StartWithLet_Yield LtLt AdditiveExpression_Yield
| ShiftExpression_StartWithLet_Yield GtGt AdditiveExpression_Yield
| ShiftExpression_StartWithLet_Yield GtGtGt AdditiveExpression_Yield
;

ShiftExpression_Yield :
  AdditiveExpression_Yield
| ShiftExpression_Yield LtLt AdditiveExpression_Yield
| ShiftExpression_Yield GtGt AdditiveExpression_Yield
| ShiftExpression_Yield GtGtGt AdditiveExpression_Yield
;

RelationalExpression :
  ShiftExpression
| RelationalExpression Lt ShiftExpression
| RelationalExpression Gt ShiftExpression
| RelationalExpression LtAssign ShiftExpression
| RelationalExpression GtAssign ShiftExpression
| RelationalExpression instanceof ShiftExpression
;

RelationalExpression_In :
  ShiftExpression
| RelationalExpression_In Lt ShiftExpression
| RelationalExpression_In Gt ShiftExpression
| RelationalExpression_In LtAssign ShiftExpression
| RelationalExpression_In GtAssign ShiftExpression
| RelationalExpression_In instanceof ShiftExpression
| RelationalExpression_In in ShiftExpression
;

RelationalExpression_In_NoFuncClass :
  ShiftExpression_NoFuncClass
| RelationalExpression_In_NoFuncClass Lt ShiftExpression
| RelationalExpression_In_NoFuncClass Gt ShiftExpression
| RelationalExpression_In_NoFuncClass LtAssign ShiftExpression
| RelationalExpression_In_NoFuncClass GtAssign ShiftExpression
| RelationalExpression_In_NoFuncClass instanceof ShiftExpression
| RelationalExpression_In_NoFuncClass in ShiftExpression
;

RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Lt ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Gt ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral LtAssign ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral GtAssign ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral instanceof ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral in ShiftExpression
;

RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Lt ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Gt ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield LtAssign ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield GtAssign ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield instanceof ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield in ShiftExpression_Yield
;

RelationalExpression_In_NoLet :
  ShiftExpression_NoLet
| RelationalExpression_In_NoLet Lt ShiftExpression
| RelationalExpression_In_NoLet Gt ShiftExpression
| RelationalExpression_In_NoLet LtAssign ShiftExpression
| RelationalExpression_In_NoLet GtAssign ShiftExpression
| RelationalExpression_In_NoLet instanceof ShiftExpression
| RelationalExpression_In_NoLet in ShiftExpression
;

RelationalExpression_In_NoLet_Yield :
  ShiftExpression_NoLet_Yield
| RelationalExpression_In_NoLet_Yield Lt ShiftExpression_Yield
| RelationalExpression_In_NoLet_Yield Gt ShiftExpression_Yield
| RelationalExpression_In_NoLet_Yield LtAssign ShiftExpression_Yield
| RelationalExpression_In_NoLet_Yield GtAssign ShiftExpression_Yield
| RelationalExpression_In_NoLet_Yield instanceof ShiftExpression_Yield
| RelationalExpression_In_NoLet_Yield in ShiftExpression_Yield
;

RelationalExpression_In_NoObjLiteral :
  ShiftExpression_NoObjLiteral
| RelationalExpression_In_NoObjLiteral Lt ShiftExpression
| RelationalExpression_In_NoObjLiteral Gt ShiftExpression
| RelationalExpression_In_NoObjLiteral LtAssign ShiftExpression
| RelationalExpression_In_NoObjLiteral GtAssign ShiftExpression
| RelationalExpression_In_NoObjLiteral instanceof ShiftExpression
| RelationalExpression_In_NoObjLiteral in ShiftExpression
;

RelationalExpression_In_StartWithLet :
  ShiftExpression_StartWithLet
| RelationalExpression_In_StartWithLet Lt ShiftExpression
| RelationalExpression_In_StartWithLet Gt ShiftExpression
| RelationalExpression_In_StartWithLet LtAssign ShiftExpression
| RelationalExpression_In_StartWithLet GtAssign ShiftExpression
| RelationalExpression_In_StartWithLet instanceof ShiftExpression
| RelationalExpression_In_StartWithLet in ShiftExpression
;

RelationalExpression_In_StartWithLet_Yield :
  ShiftExpression_StartWithLet_Yield
| RelationalExpression_In_StartWithLet_Yield Lt ShiftExpression_Yield
| RelationalExpression_In_StartWithLet_Yield Gt ShiftExpression_Yield
| RelationalExpression_In_StartWithLet_Yield LtAssign ShiftExpression_Yield
| RelationalExpression_In_StartWithLet_Yield GtAssign ShiftExpression_Yield
| RelationalExpression_In_StartWithLet_Yield instanceof ShiftExpression_Yield
| RelationalExpression_In_StartWithLet_Yield in ShiftExpression_Yield
;

RelationalExpression_In_Yield :
  ShiftExpression_Yield
| RelationalExpression_In_Yield Lt ShiftExpression_Yield
| RelationalExpression_In_Yield Gt ShiftExpression_Yield
| RelationalExpression_In_Yield LtAssign ShiftExpression_Yield
| RelationalExpression_In_Yield GtAssign ShiftExpression_Yield
| RelationalExpression_In_Yield instanceof ShiftExpression_Yield
| RelationalExpression_In_Yield in ShiftExpression_Yield
;

RelationalExpression_NoLet :
  ShiftExpression_NoLet
| RelationalExpression_NoLet Lt ShiftExpression
| RelationalExpression_NoLet Gt ShiftExpression
| RelationalExpression_NoLet LtAssign ShiftExpression
| RelationalExpression_NoLet GtAssign ShiftExpression
| RelationalExpression_NoLet instanceof ShiftExpression
;

RelationalExpression_NoLet_Yield :
  ShiftExpression_NoLet_Yield
| RelationalExpression_NoLet_Yield Lt ShiftExpression_Yield
| RelationalExpression_NoLet_Yield Gt ShiftExpression_Yield
| RelationalExpression_NoLet_Yield LtAssign ShiftExpression_Yield
| RelationalExpression_NoLet_Yield GtAssign ShiftExpression_Yield
| RelationalExpression_NoLet_Yield instanceof ShiftExpression_Yield
;

RelationalExpression_NoObjLiteral :
  ShiftExpression_NoObjLiteral
| RelationalExpression_NoObjLiteral Lt ShiftExpression
| RelationalExpression_NoObjLiteral Gt ShiftExpression
| RelationalExpression_NoObjLiteral LtAssign ShiftExpression
| RelationalExpression_NoObjLiteral GtAssign ShiftExpression
| RelationalExpression_NoObjLiteral instanceof ShiftExpression
;

RelationalExpression_StartWithLet :
  ShiftExpression_StartWithLet
| RelationalExpression_StartWithLet Lt ShiftExpression
| RelationalExpression_StartWithLet Gt ShiftExpression
| RelationalExpression_StartWithLet LtAssign ShiftExpression
| RelationalExpression_StartWithLet GtAssign ShiftExpression
| RelationalExpression_StartWithLet instanceof ShiftExpression
;

RelationalExpression_StartWithLet_Yield :
  ShiftExpression_StartWithLet_Yield
| RelationalExpression_StartWithLet_Yield Lt ShiftExpression_Yield
| RelationalExpression_StartWithLet_Yield Gt ShiftExpression_Yield
| RelationalExpression_StartWithLet_Yield LtAssign ShiftExpression_Yield
| RelationalExpression_StartWithLet_Yield GtAssign ShiftExpression_Yield
| RelationalExpression_StartWithLet_Yield instanceof ShiftExpression_Yield
;

RelationalExpression_Yield :
  ShiftExpression_Yield
| RelationalExpression_Yield Lt ShiftExpression_Yield
| RelationalExpression_Yield Gt ShiftExpression_Yield
| RelationalExpression_Yield LtAssign ShiftExpression_Yield
| RelationalExpression_Yield GtAssign ShiftExpression_Yield
| RelationalExpression_Yield instanceof ShiftExpression_Yield
;

EqualityExpression :
  RelationalExpression
| EqualityExpression AssignAssign RelationalExpression
| EqualityExpression ExclAssign RelationalExpression
| EqualityExpression AssignAssignAssign RelationalExpression
| EqualityExpression ExclAssignAssign RelationalExpression
;

EqualityExpression_In :
  RelationalExpression_In
| EqualityExpression_In AssignAssign RelationalExpression_In
| EqualityExpression_In ExclAssign RelationalExpression_In
| EqualityExpression_In AssignAssignAssign RelationalExpression_In
| EqualityExpression_In ExclAssignAssign RelationalExpression_In
;

EqualityExpression_In_NoFuncClass :
  RelationalExpression_In_NoFuncClass
| EqualityExpression_In_NoFuncClass AssignAssign RelationalExpression_In
| EqualityExpression_In_NoFuncClass ExclAssign RelationalExpression_In
| EqualityExpression_In_NoFuncClass AssignAssignAssign RelationalExpression_In
| EqualityExpression_In_NoFuncClass ExclAssignAssign RelationalExpression_In
;

EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral AssignAssign RelationalExpression_In
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ExclAssign RelationalExpression_In
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral AssignAssignAssign RelationalExpression_In
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ExclAssignAssign RelationalExpression_In
;

EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield AssignAssign RelationalExpression_In_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ExclAssign RelationalExpression_In_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield AssignAssignAssign RelationalExpression_In_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ExclAssignAssign RelationalExpression_In_Yield
;

EqualityExpression_In_NoObjLiteral :
  RelationalExpression_In_NoObjLiteral
| EqualityExpression_In_NoObjLiteral AssignAssign RelationalExpression_In
| EqualityExpression_In_NoObjLiteral ExclAssign RelationalExpression_In
| EqualityExpression_In_NoObjLiteral AssignAssignAssign RelationalExpression_In
| EqualityExpression_In_NoObjLiteral ExclAssignAssign RelationalExpression_In
;

EqualityExpression_In_Yield :
  RelationalExpression_In_Yield
| EqualityExpression_In_Yield AssignAssign RelationalExpression_In_Yield
| EqualityExpression_In_Yield ExclAssign RelationalExpression_In_Yield
| EqualityExpression_In_Yield AssignAssignAssign RelationalExpression_In_Yield
| EqualityExpression_In_Yield ExclAssignAssign RelationalExpression_In_Yield
;

EqualityExpression_NoLet :
  RelationalExpression_NoLet
| EqualityExpression_NoLet AssignAssign RelationalExpression
| EqualityExpression_NoLet ExclAssign RelationalExpression
| EqualityExpression_NoLet AssignAssignAssign RelationalExpression
| EqualityExpression_NoLet ExclAssignAssign RelationalExpression
;

EqualityExpression_NoLet_Yield :
  RelationalExpression_NoLet_Yield
| EqualityExpression_NoLet_Yield AssignAssign RelationalExpression_Yield
| EqualityExpression_NoLet_Yield ExclAssign RelationalExpression_Yield
| EqualityExpression_NoLet_Yield AssignAssignAssign RelationalExpression_Yield
| EqualityExpression_NoLet_Yield ExclAssignAssign RelationalExpression_Yield
;

EqualityExpression_NoObjLiteral :
  RelationalExpression_NoObjLiteral
| EqualityExpression_NoObjLiteral AssignAssign RelationalExpression
| EqualityExpression_NoObjLiteral ExclAssign RelationalExpression
| EqualityExpression_NoObjLiteral AssignAssignAssign RelationalExpression
| EqualityExpression_NoObjLiteral ExclAssignAssign RelationalExpression
;

EqualityExpression_StartWithLet :
  RelationalExpression_StartWithLet
| EqualityExpression_StartWithLet AssignAssign RelationalExpression
| EqualityExpression_StartWithLet ExclAssign RelationalExpression
| EqualityExpression_StartWithLet AssignAssignAssign RelationalExpression
| EqualityExpression_StartWithLet ExclAssignAssign RelationalExpression
;

EqualityExpression_StartWithLet_Yield :
  RelationalExpression_StartWithLet_Yield
| EqualityExpression_StartWithLet_Yield AssignAssign RelationalExpression_Yield
| EqualityExpression_StartWithLet_Yield ExclAssign RelationalExpression_Yield
| EqualityExpression_StartWithLet_Yield AssignAssignAssign RelationalExpression_Yield
| EqualityExpression_StartWithLet_Yield ExclAssignAssign RelationalExpression_Yield
;

EqualityExpression_Yield :
  RelationalExpression_Yield
| EqualityExpression_Yield AssignAssign RelationalExpression_Yield
| EqualityExpression_Yield ExclAssign RelationalExpression_Yield
| EqualityExpression_Yield AssignAssignAssign RelationalExpression_Yield
| EqualityExpression_Yield ExclAssignAssign RelationalExpression_Yield
;

BitwiseANDExpression :
  EqualityExpression
| BitwiseANDExpression And EqualityExpression
;

BitwiseANDExpression_In :
  EqualityExpression_In
| BitwiseANDExpression_In And EqualityExpression_In
;

BitwiseANDExpression_In_NoFuncClass :
  EqualityExpression_In_NoFuncClass
| BitwiseANDExpression_In_NoFuncClass And EqualityExpression_In
;

BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral And EqualityExpression_In
;

BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield And EqualityExpression_In_Yield
;

BitwiseANDExpression_In_NoObjLiteral :
  EqualityExpression_In_NoObjLiteral
| BitwiseANDExpression_In_NoObjLiteral And EqualityExpression_In
;

BitwiseANDExpression_In_Yield :
  EqualityExpression_In_Yield
| BitwiseANDExpression_In_Yield And EqualityExpression_In_Yield
;

BitwiseANDExpression_NoLet :
  EqualityExpression_NoLet
| BitwiseANDExpression_NoLet And EqualityExpression
;

BitwiseANDExpression_NoLet_Yield :
  EqualityExpression_NoLet_Yield
| BitwiseANDExpression_NoLet_Yield And EqualityExpression_Yield
;

BitwiseANDExpression_NoObjLiteral :
  EqualityExpression_NoObjLiteral
| BitwiseANDExpression_NoObjLiteral And EqualityExpression
;

BitwiseANDExpression_StartWithLet :
  EqualityExpression_StartWithLet
| BitwiseANDExpression_StartWithLet And EqualityExpression
;

BitwiseANDExpression_StartWithLet_Yield :
  EqualityExpression_StartWithLet_Yield
| BitwiseANDExpression_StartWithLet_Yield And EqualityExpression_Yield
;

BitwiseANDExpression_Yield :
  EqualityExpression_Yield
| BitwiseANDExpression_Yield And EqualityExpression_Yield
;

BitwiseXORExpression :
  BitwiseANDExpression
| BitwiseXORExpression Xor BitwiseANDExpression
;

BitwiseXORExpression_In :
  BitwiseANDExpression_In
| BitwiseXORExpression_In Xor BitwiseANDExpression_In
;

BitwiseXORExpression_In_NoFuncClass :
  BitwiseANDExpression_In_NoFuncClass
| BitwiseXORExpression_In_NoFuncClass Xor BitwiseANDExpression_In
;

BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Xor BitwiseANDExpression_In
;

BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Xor BitwiseANDExpression_In_Yield
;

BitwiseXORExpression_In_NoObjLiteral :
  BitwiseANDExpression_In_NoObjLiteral
| BitwiseXORExpression_In_NoObjLiteral Xor BitwiseANDExpression_In
;

BitwiseXORExpression_In_Yield :
  BitwiseANDExpression_In_Yield
| BitwiseXORExpression_In_Yield Xor BitwiseANDExpression_In_Yield
;

BitwiseXORExpression_NoLet :
  BitwiseANDExpression_NoLet
| BitwiseXORExpression_NoLet Xor BitwiseANDExpression
;

BitwiseXORExpression_NoLet_Yield :
  BitwiseANDExpression_NoLet_Yield
| BitwiseXORExpression_NoLet_Yield Xor BitwiseANDExpression_Yield
;

BitwiseXORExpression_NoObjLiteral :
  BitwiseANDExpression_NoObjLiteral
| BitwiseXORExpression_NoObjLiteral Xor BitwiseANDExpression
;

BitwiseXORExpression_StartWithLet :
  BitwiseANDExpression_StartWithLet
| BitwiseXORExpression_StartWithLet Xor BitwiseANDExpression
;

BitwiseXORExpression_StartWithLet_Yield :
  BitwiseANDExpression_StartWithLet_Yield
| BitwiseXORExpression_StartWithLet_Yield Xor BitwiseANDExpression_Yield
;

BitwiseXORExpression_Yield :
  BitwiseANDExpression_Yield
| BitwiseXORExpression_Yield Xor BitwiseANDExpression_Yield
;

BitwiseORExpression :
  BitwiseXORExpression
| BitwiseORExpression Or BitwiseXORExpression
;

BitwiseORExpression_In :
  BitwiseXORExpression_In
| BitwiseORExpression_In Or BitwiseXORExpression_In
;

BitwiseORExpression_In_NoFuncClass :
  BitwiseXORExpression_In_NoFuncClass
| BitwiseORExpression_In_NoFuncClass Or BitwiseXORExpression_In
;

BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Or BitwiseXORExpression_In
;

BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  BitwiseXORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Or BitwiseXORExpression_In_Yield
;

BitwiseORExpression_In_NoObjLiteral :
  BitwiseXORExpression_In_NoObjLiteral
| BitwiseORExpression_In_NoObjLiteral Or BitwiseXORExpression_In
;

BitwiseORExpression_In_Yield :
  BitwiseXORExpression_In_Yield
| BitwiseORExpression_In_Yield Or BitwiseXORExpression_In_Yield
;

BitwiseORExpression_NoLet :
  BitwiseXORExpression_NoLet
| BitwiseORExpression_NoLet Or BitwiseXORExpression
;

BitwiseORExpression_NoLet_Yield :
  BitwiseXORExpression_NoLet_Yield
| BitwiseORExpression_NoLet_Yield Or BitwiseXORExpression_Yield
;

BitwiseORExpression_NoObjLiteral :
  BitwiseXORExpression_NoObjLiteral
| BitwiseORExpression_NoObjLiteral Or BitwiseXORExpression
;

BitwiseORExpression_StartWithLet :
  BitwiseXORExpression_StartWithLet
| BitwiseORExpression_StartWithLet Or BitwiseXORExpression
;

BitwiseORExpression_StartWithLet_Yield :
  BitwiseXORExpression_StartWithLet_Yield
| BitwiseORExpression_StartWithLet_Yield Or BitwiseXORExpression_Yield
;

BitwiseORExpression_Yield :
  BitwiseXORExpression_Yield
| BitwiseORExpression_Yield Or BitwiseXORExpression_Yield
;

LogicalANDExpression :
  BitwiseORExpression
| LogicalANDExpression AndAnd BitwiseORExpression
;

LogicalANDExpression_In :
  BitwiseORExpression_In
| LogicalANDExpression_In AndAnd BitwiseORExpression_In
;

LogicalANDExpression_In_NoFuncClass :
  BitwiseORExpression_In_NoFuncClass
| LogicalANDExpression_In_NoFuncClass AndAnd BitwiseORExpression_In
;

LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral AndAnd BitwiseORExpression_In
;

LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield AndAnd BitwiseORExpression_In_Yield
;

LogicalANDExpression_In_NoObjLiteral :
  BitwiseORExpression_In_NoObjLiteral
| LogicalANDExpression_In_NoObjLiteral AndAnd BitwiseORExpression_In
;

LogicalANDExpression_In_Yield :
  BitwiseORExpression_In_Yield
| LogicalANDExpression_In_Yield AndAnd BitwiseORExpression_In_Yield
;

LogicalANDExpression_NoLet :
  BitwiseORExpression_NoLet
| LogicalANDExpression_NoLet AndAnd BitwiseORExpression
;

LogicalANDExpression_NoLet_Yield :
  BitwiseORExpression_NoLet_Yield
| LogicalANDExpression_NoLet_Yield AndAnd BitwiseORExpression_Yield
;

LogicalANDExpression_NoObjLiteral :
  BitwiseORExpression_NoObjLiteral
| LogicalANDExpression_NoObjLiteral AndAnd BitwiseORExpression
;

LogicalANDExpression_StartWithLet :
  BitwiseORExpression_StartWithLet
| LogicalANDExpression_StartWithLet AndAnd BitwiseORExpression
;

LogicalANDExpression_StartWithLet_Yield :
  BitwiseORExpression_StartWithLet_Yield
| LogicalANDExpression_StartWithLet_Yield AndAnd BitwiseORExpression_Yield
;

LogicalANDExpression_Yield :
  BitwiseORExpression_Yield
| LogicalANDExpression_Yield AndAnd BitwiseORExpression_Yield
;

LogicalORExpression :
  LogicalANDExpression
| LogicalORExpression OrOr LogicalANDExpression
;

LogicalORExpression_In :
  LogicalANDExpression_In
| LogicalORExpression_In OrOr LogicalANDExpression_In
;

LogicalORExpression_In_NoFuncClass :
  LogicalANDExpression_In_NoFuncClass
| LogicalORExpression_In_NoFuncClass OrOr LogicalANDExpression_In
;

LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral OrOr LogicalANDExpression_In
;

LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield OrOr LogicalANDExpression_In_Yield
;

LogicalORExpression_In_NoObjLiteral :
  LogicalANDExpression_In_NoObjLiteral
| LogicalORExpression_In_NoObjLiteral OrOr LogicalANDExpression_In
;

LogicalORExpression_In_Yield :
  LogicalANDExpression_In_Yield
| LogicalORExpression_In_Yield OrOr LogicalANDExpression_In_Yield
;

LogicalORExpression_NoLet :
  LogicalANDExpression_NoLet
| LogicalORExpression_NoLet OrOr LogicalANDExpression
;

LogicalORExpression_NoLet_Yield :
  LogicalANDExpression_NoLet_Yield
| LogicalORExpression_NoLet_Yield OrOr LogicalANDExpression_Yield
;

LogicalORExpression_NoObjLiteral :
  LogicalANDExpression_NoObjLiteral
| LogicalORExpression_NoObjLiteral OrOr LogicalANDExpression
;

LogicalORExpression_StartWithLet :
  LogicalANDExpression_StartWithLet
| LogicalORExpression_StartWithLet OrOr LogicalANDExpression
;

LogicalORExpression_StartWithLet_Yield :
  LogicalANDExpression_StartWithLet_Yield
| LogicalORExpression_StartWithLet_Yield OrOr LogicalANDExpression_Yield
;

LogicalORExpression_Yield :
  LogicalANDExpression_Yield
| LogicalORExpression_Yield OrOr LogicalANDExpression_Yield
;

ConditionalExpression :
  LogicalORExpression
| LogicalORExpression Quest AssignmentExpression_In Colon AssignmentExpression
;

ConditionalExpression_In :
  LogicalORExpression_In
| LogicalORExpression_In Quest AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_NoFuncClass :
  LogicalORExpression_In_NoFuncClass
| LogicalORExpression_In_NoFuncClass Quest AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Quest AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Quest AssignmentExpression_In_Yield Colon AssignmentExpression_In_Yield
;

ConditionalExpression_In_NoObjLiteral :
  LogicalORExpression_In_NoObjLiteral
| LogicalORExpression_In_NoObjLiteral Quest AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_Yield :
  LogicalORExpression_In_Yield
| LogicalORExpression_In_Yield Quest AssignmentExpression_In_Yield Colon AssignmentExpression_In_Yield
;

ConditionalExpression_NoLet :
  LogicalORExpression_NoLet
| LogicalORExpression_NoLet Quest AssignmentExpression_In Colon AssignmentExpression
;

ConditionalExpression_NoLet_Yield :
  LogicalORExpression_NoLet_Yield
| LogicalORExpression_NoLet_Yield Quest AssignmentExpression_In_Yield Colon AssignmentExpression_Yield
;

ConditionalExpression_NoObjLiteral :
  LogicalORExpression_NoObjLiteral
| LogicalORExpression_NoObjLiteral Quest AssignmentExpression_In Colon AssignmentExpression
;

ConditionalExpression_StartWithLet :
  LogicalORExpression_StartWithLet
| LogicalORExpression_StartWithLet Quest AssignmentExpression_In Colon AssignmentExpression
;

ConditionalExpression_StartWithLet_Yield :
  LogicalORExpression_StartWithLet_Yield
| LogicalORExpression_StartWithLet_Yield Quest AssignmentExpression_In_Yield Colon AssignmentExpression_Yield
;

ConditionalExpression_Yield :
  LogicalORExpression_Yield
| LogicalORExpression_Yield Quest AssignmentExpression_In_Yield Colon AssignmentExpression_Yield
;

AssignmentExpression :
  ConditionalExpression
| ArrowFunction
| LeftHandSideExpression Assign AssignmentExpression
| LeftHandSideExpression AssignmentOperator AssignmentExpression
;

AssignmentExpression_In :
  ConditionalExpression_In
| ArrowFunction_In
| LeftHandSideExpression Assign AssignmentExpression_In
| LeftHandSideExpression AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoFuncClass :
  ConditionalExpression_In_NoFuncClass
| ArrowFunction_In
| LeftHandSideExpression_NoFuncClass Assign AssignmentExpression_In
| LeftHandSideExpression_NoFuncClass AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| ArrowFunction_In
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral Assign AssignmentExpression_In
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| YieldExpression_In
| ArrowFunction_In_Yield
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Assign AssignmentExpression_In_Yield
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield AssignmentOperator AssignmentExpression_In_Yield
;

AssignmentExpression_In_NoObjLiteral :
  ConditionalExpression_In_NoObjLiteral
| ArrowFunction_In
| LeftHandSideExpression_NoObjLiteral Assign AssignmentExpression_In
| LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_Yield :
  ConditionalExpression_In_Yield
| YieldExpression_In
| ArrowFunction_In_Yield
| LeftHandSideExpression_Yield Assign AssignmentExpression_In_Yield
| LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_In_Yield
;

AssignmentExpression_NoLet :
  ConditionalExpression_NoLet
| ArrowFunction
| LeftHandSideExpression_NoLet Assign AssignmentExpression
| LeftHandSideExpression_NoLet AssignmentOperator AssignmentExpression
;

AssignmentExpression_NoLet_Yield :
  ConditionalExpression_NoLet_Yield
| YieldExpression
| ArrowFunction_Yield
| LeftHandSideExpression_NoLet_Yield Assign AssignmentExpression_Yield
| LeftHandSideExpression_NoLet_Yield AssignmentOperator AssignmentExpression_Yield
;

AssignmentExpression_NoObjLiteral :
  ConditionalExpression_NoObjLiteral
| ArrowFunction
| LeftHandSideExpression_NoObjLiteral Assign AssignmentExpression
| LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression
;

AssignmentExpression_StartWithLet :
  ConditionalExpression_StartWithLet
| LeftHandSideExpression_StartWithLet Assign AssignmentExpression
| LeftHandSideExpression_StartWithLet AssignmentOperator AssignmentExpression
;

AssignmentExpression_StartWithLet_Yield :
  ConditionalExpression_StartWithLet_Yield
| LeftHandSideExpression_StartWithLet_Yield Assign AssignmentExpression_Yield
| LeftHandSideExpression_StartWithLet_Yield AssignmentOperator AssignmentExpression_Yield
;

AssignmentExpression_Yield :
  ConditionalExpression_Yield
| YieldExpression
| ArrowFunction_Yield
| LeftHandSideExpression_Yield Assign AssignmentExpression_Yield
| LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_Yield
;

AssignmentOperator :
  MultAssign
| DivAssign
| RemAssign
| PlusAssign
| MinusAssign
| LtLtAssign
| GtGtAssign
| GtGtGtAssign
| AndAssign
| XorAssign
| OrAssign
;

Expression_In :
  AssignmentExpression_In
| Expression_In Comma AssignmentExpression_In
;

Expression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| Expression_In_NoFuncClass_NoLetSq_NoObjLiteral Comma AssignmentExpression_In
;

Expression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| Expression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Comma AssignmentExpression_In_Yield
;

Expression_In_Yield :
  AssignmentExpression_In_Yield
| Expression_In_Yield Comma AssignmentExpression_In_Yield
;

Expression_NoLet :
  AssignmentExpression_NoLet
| Expression_NoLet Comma AssignmentExpression
;

Expression_NoLet_Yield :
  AssignmentExpression_NoLet_Yield
| Expression_NoLet_Yield Comma AssignmentExpression_Yield
;

Expression_StartWithLet :
  AssignmentExpression_StartWithLet
| Expression_StartWithLet Comma AssignmentExpression
;

Expression_StartWithLet_Yield :
  AssignmentExpression_StartWithLet_Yield
| Expression_StartWithLet_Yield Comma AssignmentExpression_Yield
;

Statement :
  BlockStatement
| VariableStatement
| EmptyStatement
| ExpressionStatement
| IfStatement
| BreakableStatement
| ContinueStatement
| BreakStatement
| WithStatement
| LabelledStatement
| ThrowStatement
| TryStatement
| DebuggerStatement
;

Statement_Return :
  BlockStatement_Return
| VariableStatement
| EmptyStatement
| ExpressionStatement
| IfStatement_Return
| BreakableStatement_Return
| ContinueStatement
| BreakStatement
| ReturnStatement
| WithStatement_Return
| LabelledStatement_Return
| ThrowStatement
| TryStatement_Return
| DebuggerStatement
;

Statement_Return_Yield :
  BlockStatement_Return_Yield
| VariableStatement_Yield
| EmptyStatement
| ExpressionStatement_Yield
| IfStatement_Return_Yield
| BreakableStatement_Return_Yield
| ContinueStatement_Yield
| BreakStatement_Yield
| ReturnStatement_Yield
| WithStatement_Return_Yield
| LabelledStatement_Return_Yield
| ThrowStatement_Yield
| TryStatement_Return_Yield
| DebuggerStatement
;

Declaration :
  HoistableDeclaration
| ClassDeclaration
| LexicalDeclaration_In
;

Declaration_Yield :
  HoistableDeclaration_Yield
| ClassDeclaration_Yield
| LexicalDeclaration_In_Yield
;

HoistableDeclaration :
  FunctionDeclaration
| GeneratorDeclaration
;

HoistableDeclaration_Default :
  FunctionDeclaration_Default
| GeneratorDeclaration_Default
;

HoistableDeclaration_Yield :
  FunctionDeclaration_Yield
| GeneratorDeclaration_Yield
;

BreakableStatement :
  IterationStatement
| SwitchStatement
;

BreakableStatement_Return :
  IterationStatement_Return
| SwitchStatement_Return
;

BreakableStatement_Return_Yield :
  IterationStatement_Return_Yield
| SwitchStatement_Return_Yield
;

BlockStatement :
  Block
;

BlockStatement_Return :
  Block_Return
;

BlockStatement_Return_Yield :
  Block_Return_Yield
;

Block :
  Lbrace StatementList Rbrace
| Lbrace Rbrace
;

Block_Return :
  Lbrace StatementList_Return Rbrace
| Lbrace Rbrace
;

Block_Return_Yield :
  Lbrace StatementList_Return_Yield Rbrace
| Lbrace Rbrace
;

StatementList :
  StatementListItem
| StatementList StatementListItem
;

StatementList_Return :
  StatementListItem_Return
| StatementList_Return StatementListItem_Return
;

StatementList_Return_Yield :
  StatementListItem_Return_Yield
| StatementList_Return_Yield StatementListItem_Return_Yield
;

StatementListItem :
  Statement
| Declaration
;

StatementListItem_Return :
  Statement_Return
| Declaration
;

StatementListItem_Return_Yield :
  Statement_Return_Yield
| Declaration_Yield
;

LexicalDeclaration :
  LetOrConst BindingList Semicolon
;

LexicalDeclaration_In :
  LetOrConst BindingList_In Semicolon
;

LexicalDeclaration_In_Yield :
  LetOrConst BindingList_In_Yield Semicolon
;

LexicalDeclaration_Yield :
  LetOrConst BindingList_Yield Semicolon
;

LetOrConst :
  let
| const
;

BindingList :
  LexicalBinding
| BindingList Comma LexicalBinding
;

BindingList_In :
  LexicalBinding_In
| BindingList_In Comma LexicalBinding_In
;

BindingList_In_Yield :
  LexicalBinding_In_Yield
| BindingList_In_Yield Comma LexicalBinding_In_Yield
;

BindingList_Yield :
  LexicalBinding_Yield
| BindingList_Yield Comma LexicalBinding_Yield
;

LexicalBinding :
  BindingIdentifier Initializeropt
| BindingPattern Initializer
;

LexicalBinding_In :
  BindingIdentifier Initializeropt_In
| BindingPattern Initializer_In
;

LexicalBinding_In_Yield :
  BindingIdentifier_Yield Initializeropt_In_Yield
| BindingPattern_Yield Initializer_In_Yield
;

LexicalBinding_Yield :
  BindingIdentifier_Yield Initializeropt_Yield
| BindingPattern_Yield Initializer_Yield
;

VariableStatement :
  var VariableDeclarationList_In Semicolon
;

VariableStatement_Yield :
  var VariableDeclarationList_In_Yield Semicolon
;

VariableDeclarationList :
  VariableDeclaration
| VariableDeclarationList Comma VariableDeclaration
;

VariableDeclarationList_In :
  VariableDeclaration_In
| VariableDeclarationList_In Comma VariableDeclaration_In
;

VariableDeclarationList_In_Yield :
  VariableDeclaration_In_Yield
| VariableDeclarationList_In_Yield Comma VariableDeclaration_In_Yield
;

VariableDeclarationList_Yield :
  VariableDeclaration_Yield
| VariableDeclarationList_Yield Comma VariableDeclaration_Yield
;

VariableDeclaration :
  BindingIdentifier Initializeropt
| BindingPattern Initializer
;

VariableDeclaration_In :
  BindingIdentifier Initializeropt_In
| BindingPattern Initializer_In
;

VariableDeclaration_In_Yield :
  BindingIdentifier_Yield Initializeropt_In_Yield
| BindingPattern_Yield Initializer_In_Yield
;

VariableDeclaration_Yield :
  BindingIdentifier_Yield Initializeropt_Yield
| BindingPattern_Yield Initializer_Yield
;

BindingPattern :
  ObjectBindingPattern
| ArrayBindingPattern
;

BindingPattern_Yield :
  ObjectBindingPattern_Yield
| ArrayBindingPattern_Yield
;

ObjectBindingPattern :
  Lbrace Rbrace
| Lbrace BindingPropertyList Rbrace
| Lbrace BindingPropertyList Comma Rbrace
;

ObjectBindingPattern_Yield :
  Lbrace Rbrace
| Lbrace BindingPropertyList_Yield Rbrace
| Lbrace BindingPropertyList_Yield Comma Rbrace
;

ArrayBindingPattern :
  Lbrack Elisionopt BindingRestElementopt Rbrack
| Lbrack BindingElementList Rbrack
| Lbrack BindingElementList Comma Elisionopt BindingRestElementopt Rbrack
;

ArrayBindingPattern_Yield :
  Lbrack Elisionopt BindingRestElementopt_Yield Rbrack
| Lbrack BindingElementList_Yield Rbrack
| Lbrack BindingElementList_Yield Comma Elisionopt BindingRestElementopt_Yield Rbrack
;

BindingPropertyList :
  BindingProperty
| BindingPropertyList Comma BindingProperty
;

BindingPropertyList_Yield :
  BindingProperty_Yield
| BindingPropertyList_Yield Comma BindingProperty_Yield
;

BindingElementList :
  BindingElisionElement
| BindingElementList Comma BindingElisionElement
;

BindingElementList_Yield :
  BindingElisionElement_Yield
| BindingElementList_Yield Comma BindingElisionElement_Yield
;

BindingElisionElement :
  Elisionopt BindingElement
;

BindingElisionElement_Yield :
  Elisionopt BindingElement_Yield
;

BindingProperty :
  SingleNameBinding
| PropertyName Colon BindingElement
;

BindingProperty_Yield :
  SingleNameBinding_Yield
| PropertyName_Yield Colon BindingElement_Yield
;

BindingElement :
  SingleNameBinding
| BindingPattern Initializeropt_In
;

BindingElement_Yield :
  SingleNameBinding_Yield
| BindingPattern_Yield Initializeropt_In_Yield
;

SingleNameBinding :
  BindingIdentifier Initializeropt_In
;

SingleNameBinding_Yield :
  BindingIdentifier_Yield Initializeropt_In_Yield
;

BindingRestElement :
  Dot Dot Dot BindingIdentifier
;

BindingRestElement_Yield :
  Dot Dot Dot BindingIdentifier_Yield
;

EmptyStatement :
  Semicolon
;

ExpressionStatement :
  Expression_In_NoFuncClass_NoLetSq_NoObjLiteral Semicolon
;

ExpressionStatement_Yield :
  Expression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Semicolon
;

IfStatement :
  if Lparen Expression_In Rparen Statement else Statement
| if Lparen Expression_In Rparen Statement %prec else
;

IfStatement_Return :
  if Lparen Expression_In Rparen Statement_Return else Statement_Return
| if Lparen Expression_In Rparen Statement_Return %prec else
;

IfStatement_Return_Yield :
  if Lparen Expression_In_Yield Rparen Statement_Return_Yield else Statement_Return_Yield
| if Lparen Expression_In_Yield Rparen Statement_Return_Yield %prec else
;

IterationStatement :
  do Statement while Lparen Expression_In Rparen Semicolon
| while Lparen Expression_In Rparen Statement
| for Lparen Expressionopt_NoLet Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement
| for Lparen Expression_StartWithLet Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement
| for Lparen var VariableDeclarationList Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement
| for Lparen LexicalDeclaration Expressionopt_In Semicolon Expressionopt_In Rparen Statement
| for Lparen LeftHandSideExpression_NoLet in Expression_In Rparen Statement
| for Lparen LeftHandSideExpression_StartWithLet in Expression_In Rparen Statement
| for Lparen var ForBinding in Expression_In Rparen Statement
| for Lparen ForDeclaration in Expression_In Rparen Statement
| for Lparen LeftHandSideExpression_NoLet of AssignmentExpression_In Rparen Statement
| for Lparen var ForBinding of AssignmentExpression_In Rparen Statement
| for Lparen ForDeclaration of AssignmentExpression_In Rparen Statement
;

IterationStatement_Return :
  do Statement_Return while Lparen Expression_In Rparen Semicolon
| while Lparen Expression_In Rparen Statement_Return
| for Lparen Expressionopt_NoLet Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement_Return
| for Lparen Expression_StartWithLet Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement_Return
| for Lparen var VariableDeclarationList Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement_Return
| for Lparen LexicalDeclaration Expressionopt_In Semicolon Expressionopt_In Rparen Statement_Return
| for Lparen LeftHandSideExpression_NoLet in Expression_In Rparen Statement_Return
| for Lparen LeftHandSideExpression_StartWithLet in Expression_In Rparen Statement_Return
| for Lparen var ForBinding in Expression_In Rparen Statement_Return
| for Lparen ForDeclaration in Expression_In Rparen Statement_Return
| for Lparen LeftHandSideExpression_NoLet of AssignmentExpression_In Rparen Statement_Return
| for Lparen var ForBinding of AssignmentExpression_In Rparen Statement_Return
| for Lparen ForDeclaration of AssignmentExpression_In Rparen Statement_Return
;

IterationStatement_Return_Yield :
  do Statement_Return_Yield while Lparen Expression_In_Yield Rparen Semicolon
| while Lparen Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen Expressionopt_NoLet_Yield Semicolon Expressionopt_In_Yield Semicolon Expressionopt_In_Yield Rparen Statement_Return_Yield
| for Lparen Expression_StartWithLet_Yield Semicolon Expressionopt_In_Yield Semicolon Expressionopt_In_Yield Rparen Statement_Return_Yield
| for Lparen var VariableDeclarationList_Yield Semicolon Expressionopt_In_Yield Semicolon Expressionopt_In_Yield Rparen Statement_Return_Yield
| for Lparen LexicalDeclaration_Yield Expressionopt_In_Yield Semicolon Expressionopt_In_Yield Rparen Statement_Return_Yield
| for Lparen LeftHandSideExpression_NoLet_Yield in Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen LeftHandSideExpression_StartWithLet_Yield in Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen var ForBinding_Yield in Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen ForDeclaration_Yield in Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen LeftHandSideExpression_NoLet_Yield of AssignmentExpression_In_Yield Rparen Statement_Return_Yield
| for Lparen var ForBinding_Yield of AssignmentExpression_In_Yield Rparen Statement_Return_Yield
| for Lparen ForDeclaration_Yield of AssignmentExpression_In_Yield Rparen Statement_Return_Yield
;

ForDeclaration :
  LetOrConst ForBinding
;

ForDeclaration_Yield :
  LetOrConst ForBinding_Yield
;

ForBinding :
  BindingIdentifier
| BindingPattern
;

ForBinding_Yield :
  BindingIdentifier_Yield
| BindingPattern_Yield
;

ContinueStatement :
  continue Semicolon
| continue LabelIdentifier Semicolon
;

ContinueStatement_Yield :
  continue Semicolon
| continue LabelIdentifier_Yield Semicolon
;

BreakStatement :
  break Semicolon
| break LabelIdentifier Semicolon
;

BreakStatement_Yield :
  break Semicolon
| break LabelIdentifier_Yield Semicolon
;

ReturnStatement :
  return Semicolon
| return Expression_In Semicolon
;

ReturnStatement_Yield :
  return Semicolon
| return Expression_In_Yield Semicolon
;

WithStatement :
  with Lparen Expression_In Rparen Statement
;

WithStatement_Return :
  with Lparen Expression_In Rparen Statement_Return
;

WithStatement_Return_Yield :
  with Lparen Expression_In_Yield Rparen Statement_Return_Yield
;

SwitchStatement :
  switch Lparen Expression_In Rparen CaseBlock
;

SwitchStatement_Return :
  switch Lparen Expression_In Rparen CaseBlock_Return
;

SwitchStatement_Return_Yield :
  switch Lparen Expression_In_Yield Rparen CaseBlock_Return_Yield
;

CaseBlock :
  Lbrace CaseClausesopt Rbrace
| Lbrace CaseClausesopt DefaultClause CaseClausesopt Rbrace
;

CaseBlock_Return :
  Lbrace CaseClausesopt_Return Rbrace
| Lbrace CaseClausesopt_Return DefaultClause_Return CaseClausesopt_Return Rbrace
;

CaseBlock_Return_Yield :
  Lbrace CaseClausesopt_Return_Yield Rbrace
| Lbrace CaseClausesopt_Return_Yield DefaultClause_Return_Yield CaseClausesopt_Return_Yield Rbrace
;

CaseClauses :
  CaseClause
| CaseClauses CaseClause
;

CaseClauses_Return :
  CaseClause_Return
| CaseClauses_Return CaseClause_Return
;

CaseClauses_Return_Yield :
  CaseClause_Return_Yield
| CaseClauses_Return_Yield CaseClause_Return_Yield
;

CaseClause :
  case Expression_In Colon StatementList
| case Expression_In Colon
;

CaseClause_Return :
  case Expression_In Colon StatementList_Return
| case Expression_In Colon
;

CaseClause_Return_Yield :
  case Expression_In_Yield Colon StatementList_Return_Yield
| case Expression_In_Yield Colon
;

DefaultClause :
  default Colon StatementList
| default Colon
;

DefaultClause_Return :
  default Colon StatementList_Return
| default Colon
;

DefaultClause_Return_Yield :
  default Colon StatementList_Return_Yield
| default Colon
;

LabelledStatement :
  Identifier Colon LabelledItem
| yield Colon LabelledItem
;

LabelledStatement_Return :
  Identifier Colon LabelledItem_Return
| yield Colon LabelledItem_Return
;

LabelledStatement_Return_Yield :
  Identifier Colon LabelledItem_Return_Yield
;

LabelledItem :
  Statement
| FunctionDeclaration
;

LabelledItem_Return :
  Statement_Return
| FunctionDeclaration
;

LabelledItem_Return_Yield :
  Statement_Return_Yield
| FunctionDeclaration_Yield
;

ThrowStatement :
  throw Expression_In Semicolon
;

ThrowStatement_Yield :
  throw Expression_In_Yield Semicolon
;

TryStatement :
  try Block Catch
| try Block Finally
| try Block Catch Finally
;

TryStatement_Return :
  try Block_Return Catch_Return
| try Block_Return Finally_Return
| try Block_Return Catch_Return Finally_Return
;

TryStatement_Return_Yield :
  try Block_Return_Yield Catch_Return_Yield
| try Block_Return_Yield Finally_Return_Yield
| try Block_Return_Yield Catch_Return_Yield Finally_Return_Yield
;

Catch :
  catch Lparen CatchParameter Rparen Block
;

Catch_Return :
  catch Lparen CatchParameter Rparen Block_Return
;

Catch_Return_Yield :
  catch Lparen CatchParameter_Yield Rparen Block_Return_Yield
;

Finally :
  finally Block
;

Finally_Return :
  finally Block_Return
;

Finally_Return_Yield :
  finally Block_Return_Yield
;

CatchParameter :
  BindingIdentifier
| BindingPattern
;

CatchParameter_Yield :
  BindingIdentifier_Yield
| BindingPattern_Yield
;

DebuggerStatement :
  debugger Semicolon
;

FunctionDeclaration :
  function BindingIdentifier Lparen FormalParameters Rparen Lbrace FunctionBody Rbrace
;

FunctionDeclaration_Default :
  function BindingIdentifier Lparen FormalParameters Rparen Lbrace FunctionBody Rbrace
| function Lparen FormalParameters Rparen Lbrace FunctionBody Rbrace
;

FunctionDeclaration_Yield :
  function BindingIdentifier_Yield Lparen FormalParameters Rparen Lbrace FunctionBody Rbrace
;

FunctionExpression :
  function BindingIdentifier Lparen FormalParameters Rparen Lbrace FunctionBody Rbrace
| function Lparen FormalParameters Rparen Lbrace FunctionBody Rbrace
;

StrictFormalParameters :
  FormalParameters
;

StrictFormalParameters_Yield :
  FormalParameters_Yield
;

FormalParameters :
  %empty
| FormalParameterList
;

FormalParameters_Yield :
  %empty
| FormalParameterList_Yield
;

FormalParameterList :
  FunctionRestParameter
| FormalsList
| FormalsList Comma FunctionRestParameter
;

FormalParameterList_Yield :
  FunctionRestParameter_Yield
| FormalsList_Yield
| FormalsList_Yield Comma FunctionRestParameter_Yield
;

FormalsList :
  FormalParameter
| FormalsList Comma FormalParameter
;

FormalsList_Yield :
  FormalParameter_Yield
| FormalsList_Yield Comma FormalParameter_Yield
;

FunctionRestParameter :
  BindingRestElement
;

FunctionRestParameter_Yield :
  BindingRestElement_Yield
;

FormalParameter :
  BindingElement
;

FormalParameter_Yield :
  BindingElement_Yield
;

FunctionBody :
  %empty
| StatementList_Return
;

FunctionBody_Yield :
  %empty
| StatementList_Return_Yield
;

ArrowFunction :
  ArrowParameters AssignGt ConciseBody
;

ArrowFunction_In :
  ArrowParameters AssignGt ConciseBody_In
;

ArrowFunction_In_Yield :
  ArrowParameters_Yield AssignGt ConciseBody_In
;

ArrowFunction_Yield :
  ArrowParameters_Yield AssignGt ConciseBody
;

ArrowParameters :
  BindingIdentifier
| CoverParenthesizedExpressionAndArrowParameterList
;

ArrowParameters_Yield :
  BindingIdentifier_Yield
| CoverParenthesizedExpressionAndArrowParameterList_Yield
;

ConciseBody :
  AssignmentExpression_NoObjLiteral
| Lbrace FunctionBody Rbrace
;

ConciseBody_In :
  AssignmentExpression_In_NoObjLiteral
| Lbrace FunctionBody Rbrace
;

MethodDefinition :
  PropertyName Lparen StrictFormalParameters Rparen Lbrace FunctionBody Rbrace
| GeneratorMethod
| get PropertyName Lparen Rparen Lbrace FunctionBody Rbrace
| set PropertyName Lparen PropertySetParameterList Rparen Lbrace FunctionBody Rbrace
;

MethodDefinition_Yield :
  PropertyName_Yield Lparen StrictFormalParameters_Yield Rparen Lbrace FunctionBody_Yield Rbrace
| GeneratorMethod_Yield
| get PropertyName_Yield Lparen Rparen Lbrace FunctionBody_Yield Rbrace
| set PropertyName_Yield Lparen PropertySetParameterList Rparen Lbrace FunctionBody_Yield Rbrace
;

PropertySetParameterList :
  FormalParameter
;

GeneratorMethod :
  Mult PropertyName Lparen StrictFormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
;

GeneratorMethod_Yield :
  Mult PropertyName_Yield Lparen StrictFormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
;

GeneratorDeclaration :
  function Mult BindingIdentifier Lparen FormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
;

GeneratorDeclaration_Default :
  function Mult BindingIdentifier Lparen FormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
| function Mult Lparen FormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
;

GeneratorDeclaration_Yield :
  function Mult BindingIdentifier_Yield Lparen FormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
;

GeneratorExpression :
  function Mult BindingIdentifier_Yield Lparen FormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
| function Mult Lparen FormalParameters_Yield Rparen Lbrace GeneratorBody Rbrace
;

GeneratorBody :
  FunctionBody_Yield
;

YieldExpression :
  yield
| yield AssignmentExpression_Yield
| yield Mult AssignmentExpression_Yield
;

YieldExpression_In :
  yield
| yield AssignmentExpression_In_Yield
| yield Mult AssignmentExpression_In_Yield
;

ClassDeclaration :
  class BindingIdentifier ClassTail
;

ClassDeclaration_Default :
  class BindingIdentifier ClassTail
| class ClassTail
;

ClassDeclaration_Yield :
  class BindingIdentifier_Yield ClassTail_Yield
;

ClassExpression :
  class BindingIdentifier ClassTail
| class ClassTail
;

ClassExpression_Yield :
  class BindingIdentifier_Yield ClassTail_Yield
| class ClassTail_Yield
;

ClassTail :
  ClassHeritage Lbrace ClassBodyopt Rbrace
| Lbrace ClassBodyopt Rbrace
;

ClassTail_Yield :
  ClassHeritage_Yield Lbrace ClassBodyopt_Yield Rbrace
| Lbrace ClassBodyopt_Yield Rbrace
;

ClassHeritage :
  extends LeftHandSideExpression
;

ClassHeritage_Yield :
  extends LeftHandSideExpression_Yield
;

ClassBody :
  ClassElementList
;

ClassBody_Yield :
  ClassElementList_Yield
;

ClassElementList :
  ClassElement
| ClassElementList ClassElement
;

ClassElementList_Yield :
  ClassElement_Yield
| ClassElementList_Yield ClassElement_Yield
;

ClassElement :
  MethodDefinition
| static MethodDefinition
| Semicolon
;

ClassElement_Yield :
  MethodDefinition_Yield
| static MethodDefinition_Yield
| Semicolon
;

Module :
  ModuleBodyopt
;

ModuleBody :
  ModuleItemList
;

ModuleItemList :
  ModuleItem
| ModuleItemList ModuleItem
;

ModuleItem :
  ImportDeclaration
| ExportDeclaration
| StatementListItem
;

ImportDeclaration :
  import ImportClause FromClause Semicolon
| import ModuleSpecifier Semicolon
;

ImportClause :
  ImportedDefaultBinding
| NameSpaceImport
| NamedImports
| ImportedDefaultBinding Comma NameSpaceImport
| ImportedDefaultBinding Comma NamedImports
;

ImportedDefaultBinding :
  ImportedBinding
;

NameSpaceImport :
  Mult as ImportedBinding
;

NamedImports :
  Lbrace Rbrace
| Lbrace ImportsList Rbrace
| Lbrace ImportsList Comma Rbrace
;

FromClause :
  from ModuleSpecifier
;

ImportsList :
  ImportSpecifier
| ImportsList Comma ImportSpecifier
;

ImportSpecifier :
  ImportedBinding
| IdentifierName as ImportedBinding
;

ModuleSpecifier :
  StringLiteral
;

ImportedBinding :
  BindingIdentifier
;

ExportDeclaration :
  export Mult FromClause Semicolon
| export ExportClause FromClause Semicolon
| export ExportClause Semicolon
| export VariableStatement
| export Declaration
| export default HoistableDeclaration_Default
| export default ClassDeclaration_Default
| export default AssignmentExpression_In_NoFuncClass Semicolon
;

ExportClause :
  Lbrace Rbrace
| Lbrace ExportsList Rbrace
| Lbrace ExportsList Comma Rbrace
;

ExportsList :
  ExportSpecifier
| ExportsList Comma ExportSpecifier
;

ExportSpecifier :
  IdentifierName
| IdentifierName as IdentifierName
;

Elisionopt :
  %empty
| Elision
;

Initializeropt :
  %empty
| Initializer
;

Initializeropt_In :
  %empty
| Initializer_In
;

Initializeropt_In_Yield :
  %empty
| Initializer_In_Yield
;

Initializeropt_Yield :
  %empty
| Initializer_Yield
;

BindingRestElementopt :
  %empty
| BindingRestElement
;

BindingRestElementopt_Yield :
  %empty
| BindingRestElement_Yield
;

Expressionopt_In :
  %empty
| Expression_In
;

Expressionopt_In_Yield :
  %empty
| Expression_In_Yield
;

Expressionopt_NoLet :
  %empty
| Expression_NoLet
;

Expressionopt_NoLet_Yield :
  %empty
| Expression_NoLet_Yield
;

CaseClausesopt :
  %empty
| CaseClauses
;

CaseClausesopt_Return :
  %empty
| CaseClauses_Return
;

CaseClausesopt_Return_Yield :
  %empty
| CaseClauses_Return_Yield
;

ClassBodyopt :
  %empty
| ClassBody
;

ClassBodyopt_Yield :
  %empty
| ClassBody_Yield
;

ModuleBodyopt :
  %empty
| ModuleBody
;

%%

