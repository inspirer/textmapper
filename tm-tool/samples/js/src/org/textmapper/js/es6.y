%{
#include <stdio.h>
%}

%start Module

%right else
%token lookahead1
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
%token Lcurly
%token Lparen
%token Rparen
%token Lsquare
%token Rsquare
%token Dot
%token Semicolon
%token Comma
%token Less
%token Greater
%token LessEqual
%token GreaterEqual
%token EqualEqual
%token ExclamationEqual
%token EqualEqualEqual
%token ExclamationEqualEqual
%token Plus
%token Minus
%token Mult
%token Percent
%token PlusPlus
%token MinusMinus
%token LessLess
%token GreaterGreater
%token GreaterGreaterGreater
%token Ampersand
%token Or
%token Xor
%token Exclamation
%token Tilde
%token AmpersandAmpersand
%token OrOr
%token Questionmark
%token Colon
%token Equal
%token PlusEqual
%token MinusEqual
%token MultEqual
%token PercentEqual
%token LessLessEqual
%token GreaterGreaterEqual
%token GreaterGreaterGreaterEqual
%token AmpersandEqual
%token OrEqual
%token XorEqual
%token EqualGreater
%token NumericLiteral
%token StringLiteral
%token Rcurly
%token NoSubstitutionTemplate
%token TemplateHead
%token TemplateMiddle
%token TemplateTail
%token RegularExpressionLiteral
%token Slash
%token SlashEqual

%locations
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
  Lsquare Elisionopt Rsquare
| Lsquare ElementList Rsquare
| Lsquare ElementList Comma Elisionopt Rsquare
;

ArrayLiteral_Yield :
  Lsquare Elisionopt Rsquare
| Lsquare ElementList_Yield Rsquare
| Lsquare ElementList_Yield Comma Elisionopt Rsquare
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
  Lcurly Rcurly
| Lcurly PropertyDefinitionList Rcurly
| Lcurly PropertyDefinitionList Comma Rcurly
;

ObjectLiteral_Yield :
  Lcurly Rcurly
| Lcurly PropertyDefinitionList_Yield Rcurly
| Lcurly PropertyDefinitionList_Yield Comma Rcurly
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
  Lsquare AssignmentExpression_In Rsquare
;

ComputedPropertyName_Yield :
  Lsquare AssignmentExpression_In_Yield Rsquare
;

CoverInitializedName :
  IdentifierReference Initializer_In
;

CoverInitializedName_Yield :
  IdentifierReference_Yield Initializer_In_Yield
;

Initializer :
  Equal AssignmentExpression
;

Initializer_In :
  Equal AssignmentExpression_In
;

Initializer_In_Yield :
  Equal AssignmentExpression_In_Yield
;

Initializer_Yield :
  Equal AssignmentExpression_Yield
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
| MemberExpression Lsquare Expression_In Rsquare
| MemberExpression Dot IdentifierName
| MemberExpression TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoFuncClass :
  PrimaryExpression_NoFuncClass
| MemberExpression_NoFuncClass Lsquare Expression_In Rsquare
| MemberExpression_NoFuncClass Dot IdentifierName
| MemberExpression_NoFuncClass TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  PrimaryExpression_NoFuncClass_NoObjLiteral
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral Lsquare Expression_In Rsquare
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  PrimaryExpression_NoFuncClass_NoObjLiteral_Yield
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield Lsquare Expression_In_Yield Rsquare
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLet :
  PrimaryExpression_NoLet
| MemberExpression_NoLet Lsquare Expression_In Rsquare
| MemberExpression_NoLet Dot IdentifierName
| MemberExpression_NoLet TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLet_Yield :
  PrimaryExpression_NoLet_Yield
| MemberExpression_NoLet_Yield Lsquare Expression_In_Yield Rsquare
| MemberExpression_NoLet_Yield Dot IdentifierName
| MemberExpression_NoLet_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral :
  PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral Lsquare Expression_In Rsquare
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  PrimaryExpression_NoFuncClass_NoLet_NoObjLiteral_Yield
| MemberExpression_NoLetOnly_NoFuncClass_NoLetSq_NoObjLiteral_Yield Lsquare Expression_In_Yield Rsquare
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Dot IdentifierName
| MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLetOnly_NoLetSq :
  PrimaryExpression_NoLet
| MemberExpression_NoLetOnly_NoLetSq Lsquare Expression_In Rsquare
| MemberExpression_NoLetSq Dot IdentifierName
| MemberExpression_NoLetSq TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetOnly_NoLetSq_Yield :
  PrimaryExpression_NoLet_Yield
| MemberExpression_NoLetOnly_NoLetSq_Yield Lsquare Expression_In_Yield Rsquare
| MemberExpression_NoLetSq_Yield Dot IdentifierName
| MemberExpression_NoLetSq_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoLetSq :
  PrimaryExpression
| MemberExpression_NoLetOnly_NoLetSq Lsquare Expression_In Rsquare
| MemberExpression_NoLetSq Dot IdentifierName
| MemberExpression_NoLetSq TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_NoLetSq_Yield :
  PrimaryExpression_Yield
| MemberExpression_NoLetOnly_NoLetSq_Yield Lsquare Expression_In_Yield Rsquare
| MemberExpression_NoLetSq_Yield Dot IdentifierName
| MemberExpression_NoLetSq_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

MemberExpression_NoObjLiteral :
  PrimaryExpression_NoObjLiteral
| MemberExpression_NoObjLiteral Lsquare Expression_In Rsquare
| MemberExpression_NoObjLiteral Dot IdentifierName
| MemberExpression_NoObjLiteral TemplateLiteral
| SuperProperty
| MetaProperty
| new MemberExpression Arguments
;

MemberExpression_Yield :
  PrimaryExpression_Yield
| MemberExpression_Yield Lsquare Expression_In_Yield Rsquare
| MemberExpression_Yield Dot IdentifierName
| MemberExpression_Yield TemplateLiteral_Yield
| SuperProperty_Yield
| MetaProperty
| new MemberExpression_Yield Arguments_Yield
;

SuperProperty :
  super Lsquare Expression_In Rsquare
| super Dot IdentifierName
;

SuperProperty_Yield :
  super Lsquare Expression_In_Yield Rsquare
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

NewExpression_NoLetSq :
  MemberExpression_NoLetSq
| new NewExpression
;

NewExpression_NoLetSq_Yield :
  MemberExpression_NoLetSq_Yield
| new NewExpression_Yield
;

NewExpression_NoObjLiteral :
  MemberExpression_NoObjLiteral
| new NewExpression
;

NewExpression_Yield :
  MemberExpression_Yield
| new NewExpression_Yield
;

CallExpression :
  MemberExpression Arguments
| SuperCall
| CallExpression Arguments
| CallExpression Lsquare Expression_In Rsquare
| CallExpression Dot IdentifierName
| CallExpression TemplateLiteral
;

CallExpression_NoFuncClass :
  MemberExpression_NoFuncClass Arguments
| SuperCall
| CallExpression_NoFuncClass Arguments
| CallExpression_NoFuncClass Lsquare Expression_In Rsquare
| CallExpression_NoFuncClass Dot IdentifierName
| CallExpression_NoFuncClass TemplateLiteral
;

CallExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral Arguments
| SuperCall
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral Arguments
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral Lsquare Expression_In Rsquare
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral Dot IdentifierName
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral TemplateLiteral
;

CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  MemberExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Arguments_Yield
| SuperCall_Yield
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Arguments_Yield
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Lsquare Expression_In_Yield Rsquare
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Dot IdentifierName
| CallExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield TemplateLiteral_Yield
;

CallExpression_NoLet :
  MemberExpression_NoLet Arguments
| SuperCall
| CallExpression_NoLet Arguments
| CallExpression_NoLet Lsquare Expression_In Rsquare
| CallExpression_NoLet Dot IdentifierName
| CallExpression_NoLet TemplateLiteral
;

CallExpression_NoLet_Yield :
  MemberExpression_NoLet_Yield Arguments_Yield
| SuperCall_Yield
| CallExpression_NoLet_Yield Arguments_Yield
| CallExpression_NoLet_Yield Lsquare Expression_In_Yield Rsquare
| CallExpression_NoLet_Yield Dot IdentifierName
| CallExpression_NoLet_Yield TemplateLiteral_Yield
;

CallExpression_NoLetSq :
  MemberExpression_NoLetSq Arguments
| SuperCall
| CallExpression_NoLetSq Arguments
| CallExpression_NoLetSq Lsquare Expression_In Rsquare
| CallExpression_NoLetSq Dot IdentifierName
| CallExpression_NoLetSq TemplateLiteral
;

CallExpression_NoLetSq_Yield :
  MemberExpression_NoLetSq_Yield Arguments_Yield
| SuperCall_Yield
| CallExpression_NoLetSq_Yield Arguments_Yield
| CallExpression_NoLetSq_Yield Lsquare Expression_In_Yield Rsquare
| CallExpression_NoLetSq_Yield Dot IdentifierName
| CallExpression_NoLetSq_Yield TemplateLiteral_Yield
;

CallExpression_NoObjLiteral :
  MemberExpression_NoObjLiteral Arguments
| SuperCall
| CallExpression_NoObjLiteral Arguments
| CallExpression_NoObjLiteral Lsquare Expression_In Rsquare
| CallExpression_NoObjLiteral Dot IdentifierName
| CallExpression_NoObjLiteral TemplateLiteral
;

CallExpression_Yield :
  MemberExpression_Yield Arguments_Yield
| SuperCall_Yield
| CallExpression_Yield Arguments_Yield
| CallExpression_Yield Lsquare Expression_In_Yield Rsquare
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

LeftHandSideExpression_NoLetSq :
  NewExpression_NoLetSq
| CallExpression_NoLetSq
;

LeftHandSideExpression_NoLetSq_Yield :
  NewExpression_NoLetSq_Yield
| CallExpression_NoLetSq_Yield
;

LeftHandSideExpression_NoObjLiteral :
  NewExpression_NoObjLiteral
| CallExpression_NoObjLiteral
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

PostfixExpression_NoLetSq :
  LeftHandSideExpression_NoLetSq
| LeftHandSideExpression_NoLetSq PlusPlus
| LeftHandSideExpression_NoLetSq MinusMinus
;

PostfixExpression_NoLetSq_Yield :
  LeftHandSideExpression_NoLetSq_Yield
| LeftHandSideExpression_NoLetSq_Yield PlusPlus
| LeftHandSideExpression_NoLetSq_Yield MinusMinus
;

PostfixExpression_NoObjLiteral :
  LeftHandSideExpression_NoObjLiteral
| LeftHandSideExpression_NoObjLiteral PlusPlus
| LeftHandSideExpression_NoObjLiteral MinusMinus
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
| Exclamation UnaryExpression
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
| Exclamation UnaryExpression
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
| Exclamation UnaryExpression
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
| Exclamation UnaryExpression_Yield
;

UnaryExpression_NoLetSq :
  PostfixExpression_NoLetSq
| delete UnaryExpression
| void UnaryExpression
| typeof UnaryExpression
| PlusPlus UnaryExpression
| MinusMinus UnaryExpression
| Plus UnaryExpression
| Minus UnaryExpression
| Tilde UnaryExpression
| Exclamation UnaryExpression
;

UnaryExpression_NoLetSq_Yield :
  PostfixExpression_NoLetSq_Yield
| delete UnaryExpression_Yield
| void UnaryExpression_Yield
| typeof UnaryExpression_Yield
| PlusPlus UnaryExpression_Yield
| MinusMinus UnaryExpression_Yield
| Plus UnaryExpression_Yield
| Minus UnaryExpression_Yield
| Tilde UnaryExpression_Yield
| Exclamation UnaryExpression_Yield
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
| Exclamation UnaryExpression
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
| Exclamation UnaryExpression_Yield
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

MultiplicativeExpression_NoLetSq :
  UnaryExpression_NoLetSq
| MultiplicativeExpression_NoLetSq MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_NoLetSq_Yield :
  UnaryExpression_NoLetSq_Yield
| MultiplicativeExpression_NoLetSq_Yield MultiplicativeOperator UnaryExpression_Yield
;

MultiplicativeExpression_NoObjLiteral :
  UnaryExpression_NoObjLiteral
| MultiplicativeExpression_NoObjLiteral MultiplicativeOperator UnaryExpression
;

MultiplicativeExpression_Yield :
  UnaryExpression_Yield
| MultiplicativeExpression_Yield MultiplicativeOperator UnaryExpression_Yield
;

MultiplicativeOperator :
  Mult
| Slash
| Percent
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

AdditiveExpression_NoLetSq :
  MultiplicativeExpression_NoLetSq
| AdditiveExpression_NoLetSq Plus MultiplicativeExpression
| AdditiveExpression_NoLetSq Minus MultiplicativeExpression
;

AdditiveExpression_NoLetSq_Yield :
  MultiplicativeExpression_NoLetSq_Yield
| AdditiveExpression_NoLetSq_Yield Plus MultiplicativeExpression_Yield
| AdditiveExpression_NoLetSq_Yield Minus MultiplicativeExpression_Yield
;

AdditiveExpression_NoObjLiteral :
  MultiplicativeExpression_NoObjLiteral
| AdditiveExpression_NoObjLiteral Plus MultiplicativeExpression
| AdditiveExpression_NoObjLiteral Minus MultiplicativeExpression
;

AdditiveExpression_Yield :
  MultiplicativeExpression_Yield
| AdditiveExpression_Yield Plus MultiplicativeExpression_Yield
| AdditiveExpression_Yield Minus MultiplicativeExpression_Yield
;

ShiftExpression :
  AdditiveExpression
| ShiftExpression LessLess AdditiveExpression
| ShiftExpression GreaterGreater AdditiveExpression
| ShiftExpression GreaterGreaterGreater AdditiveExpression
;

ShiftExpression_NoFuncClass :
  AdditiveExpression_NoFuncClass
| ShiftExpression_NoFuncClass LessLess AdditiveExpression
| ShiftExpression_NoFuncClass GreaterGreater AdditiveExpression
| ShiftExpression_NoFuncClass GreaterGreaterGreater AdditiveExpression
;

ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral :
  AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral LessLess AdditiveExpression
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral GreaterGreater AdditiveExpression
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral GreaterGreaterGreater AdditiveExpression
;

ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  AdditiveExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield LessLess AdditiveExpression_Yield
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield GreaterGreater AdditiveExpression_Yield
| ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield GreaterGreaterGreater AdditiveExpression_Yield
;

ShiftExpression_NoLetSq :
  AdditiveExpression_NoLetSq
| ShiftExpression_NoLetSq LessLess AdditiveExpression
| ShiftExpression_NoLetSq GreaterGreater AdditiveExpression
| ShiftExpression_NoLetSq GreaterGreaterGreater AdditiveExpression
;

ShiftExpression_NoLetSq_Yield :
  AdditiveExpression_NoLetSq_Yield
| ShiftExpression_NoLetSq_Yield LessLess AdditiveExpression_Yield
| ShiftExpression_NoLetSq_Yield GreaterGreater AdditiveExpression_Yield
| ShiftExpression_NoLetSq_Yield GreaterGreaterGreater AdditiveExpression_Yield
;

ShiftExpression_NoObjLiteral :
  AdditiveExpression_NoObjLiteral
| ShiftExpression_NoObjLiteral LessLess AdditiveExpression
| ShiftExpression_NoObjLiteral GreaterGreater AdditiveExpression
| ShiftExpression_NoObjLiteral GreaterGreaterGreater AdditiveExpression
;

ShiftExpression_Yield :
  AdditiveExpression_Yield
| ShiftExpression_Yield LessLess AdditiveExpression_Yield
| ShiftExpression_Yield GreaterGreater AdditiveExpression_Yield
| ShiftExpression_Yield GreaterGreaterGreater AdditiveExpression_Yield
;

RelationalExpression :
  ShiftExpression
| RelationalExpression Less ShiftExpression
| RelationalExpression Greater ShiftExpression
| RelationalExpression LessEqual ShiftExpression
| RelationalExpression GreaterEqual ShiftExpression
| RelationalExpression instanceof ShiftExpression
;

RelationalExpression_In :
  ShiftExpression
| RelationalExpression_In Less ShiftExpression
| RelationalExpression_In Greater ShiftExpression
| RelationalExpression_In LessEqual ShiftExpression
| RelationalExpression_In GreaterEqual ShiftExpression
| RelationalExpression_In instanceof ShiftExpression
| RelationalExpression_In in ShiftExpression
;

RelationalExpression_In_NoFuncClass :
  ShiftExpression_NoFuncClass
| RelationalExpression_In_NoFuncClass Less ShiftExpression
| RelationalExpression_In_NoFuncClass Greater ShiftExpression
| RelationalExpression_In_NoFuncClass LessEqual ShiftExpression
| RelationalExpression_In_NoFuncClass GreaterEqual ShiftExpression
| RelationalExpression_In_NoFuncClass instanceof ShiftExpression
| RelationalExpression_In_NoFuncClass in ShiftExpression
;

RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Less ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Greater ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral LessEqual ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral GreaterEqual ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral instanceof ShiftExpression
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral in ShiftExpression
;

RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  ShiftExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Less ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Greater ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield LessEqual ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield GreaterEqual ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield instanceof ShiftExpression_Yield
| RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield in ShiftExpression_Yield
;

RelationalExpression_In_NoLetSq :
  ShiftExpression_NoLetSq
| RelationalExpression_In_NoLetSq Less ShiftExpression
| RelationalExpression_In_NoLetSq Greater ShiftExpression
| RelationalExpression_In_NoLetSq LessEqual ShiftExpression
| RelationalExpression_In_NoLetSq GreaterEqual ShiftExpression
| RelationalExpression_In_NoLetSq instanceof ShiftExpression
| RelationalExpression_In_NoLetSq in ShiftExpression
;

RelationalExpression_In_NoLetSq_Yield :
  ShiftExpression_NoLetSq_Yield
| RelationalExpression_In_NoLetSq_Yield Less ShiftExpression_Yield
| RelationalExpression_In_NoLetSq_Yield Greater ShiftExpression_Yield
| RelationalExpression_In_NoLetSq_Yield LessEqual ShiftExpression_Yield
| RelationalExpression_In_NoLetSq_Yield GreaterEqual ShiftExpression_Yield
| RelationalExpression_In_NoLetSq_Yield instanceof ShiftExpression_Yield
| RelationalExpression_In_NoLetSq_Yield in ShiftExpression_Yield
;

RelationalExpression_In_NoObjLiteral :
  ShiftExpression_NoObjLiteral
| RelationalExpression_In_NoObjLiteral Less ShiftExpression
| RelationalExpression_In_NoObjLiteral Greater ShiftExpression
| RelationalExpression_In_NoObjLiteral LessEqual ShiftExpression
| RelationalExpression_In_NoObjLiteral GreaterEqual ShiftExpression
| RelationalExpression_In_NoObjLiteral instanceof ShiftExpression
| RelationalExpression_In_NoObjLiteral in ShiftExpression
;

RelationalExpression_In_Yield :
  ShiftExpression_Yield
| RelationalExpression_In_Yield Less ShiftExpression_Yield
| RelationalExpression_In_Yield Greater ShiftExpression_Yield
| RelationalExpression_In_Yield LessEqual ShiftExpression_Yield
| RelationalExpression_In_Yield GreaterEqual ShiftExpression_Yield
| RelationalExpression_In_Yield instanceof ShiftExpression_Yield
| RelationalExpression_In_Yield in ShiftExpression_Yield
;

RelationalExpression_NoLetSq :
  ShiftExpression_NoLetSq
| RelationalExpression_NoLetSq Less ShiftExpression
| RelationalExpression_NoLetSq Greater ShiftExpression
| RelationalExpression_NoLetSq LessEqual ShiftExpression
| RelationalExpression_NoLetSq GreaterEqual ShiftExpression
| RelationalExpression_NoLetSq instanceof ShiftExpression
;

RelationalExpression_NoLetSq_Yield :
  ShiftExpression_NoLetSq_Yield
| RelationalExpression_NoLetSq_Yield Less ShiftExpression_Yield
| RelationalExpression_NoLetSq_Yield Greater ShiftExpression_Yield
| RelationalExpression_NoLetSq_Yield LessEqual ShiftExpression_Yield
| RelationalExpression_NoLetSq_Yield GreaterEqual ShiftExpression_Yield
| RelationalExpression_NoLetSq_Yield instanceof ShiftExpression_Yield
;

RelationalExpression_NoObjLiteral :
  ShiftExpression_NoObjLiteral
| RelationalExpression_NoObjLiteral Less ShiftExpression
| RelationalExpression_NoObjLiteral Greater ShiftExpression
| RelationalExpression_NoObjLiteral LessEqual ShiftExpression
| RelationalExpression_NoObjLiteral GreaterEqual ShiftExpression
| RelationalExpression_NoObjLiteral instanceof ShiftExpression
;

RelationalExpression_Yield :
  ShiftExpression_Yield
| RelationalExpression_Yield Less ShiftExpression_Yield
| RelationalExpression_Yield Greater ShiftExpression_Yield
| RelationalExpression_Yield LessEqual ShiftExpression_Yield
| RelationalExpression_Yield GreaterEqual ShiftExpression_Yield
| RelationalExpression_Yield instanceof ShiftExpression_Yield
;

EqualityExpression :
  RelationalExpression
| EqualityExpression EqualEqual RelationalExpression
| EqualityExpression ExclamationEqual RelationalExpression
| EqualityExpression EqualEqualEqual RelationalExpression
| EqualityExpression ExclamationEqualEqual RelationalExpression
;

EqualityExpression_In :
  RelationalExpression_In
| EqualityExpression_In EqualEqual RelationalExpression_In
| EqualityExpression_In ExclamationEqual RelationalExpression_In
| EqualityExpression_In EqualEqualEqual RelationalExpression_In
| EqualityExpression_In ExclamationEqualEqual RelationalExpression_In
;

EqualityExpression_In_NoFuncClass :
  RelationalExpression_In_NoFuncClass
| EqualityExpression_In_NoFuncClass EqualEqual RelationalExpression_In
| EqualityExpression_In_NoFuncClass ExclamationEqual RelationalExpression_In
| EqualityExpression_In_NoFuncClass EqualEqualEqual RelationalExpression_In
| EqualityExpression_In_NoFuncClass ExclamationEqualEqual RelationalExpression_In
;

EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral EqualEqual RelationalExpression_In
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ExclamationEqual RelationalExpression_In
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral EqualEqualEqual RelationalExpression_In
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral ExclamationEqualEqual RelationalExpression_In
;

EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  RelationalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield EqualEqual RelationalExpression_In_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ExclamationEqual RelationalExpression_In_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield EqualEqualEqual RelationalExpression_In_Yield
| EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield ExclamationEqualEqual RelationalExpression_In_Yield
;

EqualityExpression_In_NoObjLiteral :
  RelationalExpression_In_NoObjLiteral
| EqualityExpression_In_NoObjLiteral EqualEqual RelationalExpression_In
| EqualityExpression_In_NoObjLiteral ExclamationEqual RelationalExpression_In
| EqualityExpression_In_NoObjLiteral EqualEqualEqual RelationalExpression_In
| EqualityExpression_In_NoObjLiteral ExclamationEqualEqual RelationalExpression_In
;

EqualityExpression_In_Yield :
  RelationalExpression_In_Yield
| EqualityExpression_In_Yield EqualEqual RelationalExpression_In_Yield
| EqualityExpression_In_Yield ExclamationEqual RelationalExpression_In_Yield
| EqualityExpression_In_Yield EqualEqualEqual RelationalExpression_In_Yield
| EqualityExpression_In_Yield ExclamationEqualEqual RelationalExpression_In_Yield
;

EqualityExpression_NoLetSq :
  RelationalExpression_NoLetSq
| EqualityExpression_NoLetSq EqualEqual RelationalExpression
| EqualityExpression_NoLetSq ExclamationEqual RelationalExpression
| EqualityExpression_NoLetSq EqualEqualEqual RelationalExpression
| EqualityExpression_NoLetSq ExclamationEqualEqual RelationalExpression
;

EqualityExpression_NoLetSq_Yield :
  RelationalExpression_NoLetSq_Yield
| EqualityExpression_NoLetSq_Yield EqualEqual RelationalExpression_Yield
| EqualityExpression_NoLetSq_Yield ExclamationEqual RelationalExpression_Yield
| EqualityExpression_NoLetSq_Yield EqualEqualEqual RelationalExpression_Yield
| EqualityExpression_NoLetSq_Yield ExclamationEqualEqual RelationalExpression_Yield
;

EqualityExpression_NoObjLiteral :
  RelationalExpression_NoObjLiteral
| EqualityExpression_NoObjLiteral EqualEqual RelationalExpression
| EqualityExpression_NoObjLiteral ExclamationEqual RelationalExpression
| EqualityExpression_NoObjLiteral EqualEqualEqual RelationalExpression
| EqualityExpression_NoObjLiteral ExclamationEqualEqual RelationalExpression
;

EqualityExpression_Yield :
  RelationalExpression_Yield
| EqualityExpression_Yield EqualEqual RelationalExpression_Yield
| EqualityExpression_Yield ExclamationEqual RelationalExpression_Yield
| EqualityExpression_Yield EqualEqualEqual RelationalExpression_Yield
| EqualityExpression_Yield ExclamationEqualEqual RelationalExpression_Yield
;

BitwiseANDExpression :
  EqualityExpression
| BitwiseANDExpression Ampersand EqualityExpression
;

BitwiseANDExpression_In :
  EqualityExpression_In
| BitwiseANDExpression_In Ampersand EqualityExpression_In
;

BitwiseANDExpression_In_NoFuncClass :
  EqualityExpression_In_NoFuncClass
| BitwiseANDExpression_In_NoFuncClass Ampersand EqualityExpression_In
;

BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Ampersand EqualityExpression_In
;

BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  EqualityExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| BitwiseANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Ampersand EqualityExpression_In_Yield
;

BitwiseANDExpression_In_NoObjLiteral :
  EqualityExpression_In_NoObjLiteral
| BitwiseANDExpression_In_NoObjLiteral Ampersand EqualityExpression_In
;

BitwiseANDExpression_In_Yield :
  EqualityExpression_In_Yield
| BitwiseANDExpression_In_Yield Ampersand EqualityExpression_In_Yield
;

BitwiseANDExpression_NoLetSq :
  EqualityExpression_NoLetSq
| BitwiseANDExpression_NoLetSq Ampersand EqualityExpression
;

BitwiseANDExpression_NoLetSq_Yield :
  EqualityExpression_NoLetSq_Yield
| BitwiseANDExpression_NoLetSq_Yield Ampersand EqualityExpression_Yield
;

BitwiseANDExpression_NoObjLiteral :
  EqualityExpression_NoObjLiteral
| BitwiseANDExpression_NoObjLiteral Ampersand EqualityExpression
;

BitwiseANDExpression_Yield :
  EqualityExpression_Yield
| BitwiseANDExpression_Yield Ampersand EqualityExpression_Yield
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

BitwiseXORExpression_NoLetSq :
  BitwiseANDExpression_NoLetSq
| BitwiseXORExpression_NoLetSq Xor BitwiseANDExpression
;

BitwiseXORExpression_NoLetSq_Yield :
  BitwiseANDExpression_NoLetSq_Yield
| BitwiseXORExpression_NoLetSq_Yield Xor BitwiseANDExpression_Yield
;

BitwiseXORExpression_NoObjLiteral :
  BitwiseANDExpression_NoObjLiteral
| BitwiseXORExpression_NoObjLiteral Xor BitwiseANDExpression
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

BitwiseORExpression_NoLetSq :
  BitwiseXORExpression_NoLetSq
| BitwiseORExpression_NoLetSq Or BitwiseXORExpression
;

BitwiseORExpression_NoLetSq_Yield :
  BitwiseXORExpression_NoLetSq_Yield
| BitwiseORExpression_NoLetSq_Yield Or BitwiseXORExpression_Yield
;

BitwiseORExpression_NoObjLiteral :
  BitwiseXORExpression_NoObjLiteral
| BitwiseORExpression_NoObjLiteral Or BitwiseXORExpression
;

BitwiseORExpression_Yield :
  BitwiseXORExpression_Yield
| BitwiseORExpression_Yield Or BitwiseXORExpression_Yield
;

LogicalANDExpression :
  BitwiseORExpression
| LogicalANDExpression AmpersandAmpersand BitwiseORExpression
;

LogicalANDExpression_In :
  BitwiseORExpression_In
| LogicalANDExpression_In AmpersandAmpersand BitwiseORExpression_In
;

LogicalANDExpression_In_NoFuncClass :
  BitwiseORExpression_In_NoFuncClass
| LogicalANDExpression_In_NoFuncClass AmpersandAmpersand BitwiseORExpression_In
;

LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral AmpersandAmpersand BitwiseORExpression_In
;

LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  BitwiseORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| LogicalANDExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield AmpersandAmpersand BitwiseORExpression_In_Yield
;

LogicalANDExpression_In_NoObjLiteral :
  BitwiseORExpression_In_NoObjLiteral
| LogicalANDExpression_In_NoObjLiteral AmpersandAmpersand BitwiseORExpression_In
;

LogicalANDExpression_In_Yield :
  BitwiseORExpression_In_Yield
| LogicalANDExpression_In_Yield AmpersandAmpersand BitwiseORExpression_In_Yield
;

LogicalANDExpression_NoLetSq :
  BitwiseORExpression_NoLetSq
| LogicalANDExpression_NoLetSq AmpersandAmpersand BitwiseORExpression
;

LogicalANDExpression_NoLetSq_Yield :
  BitwiseORExpression_NoLetSq_Yield
| LogicalANDExpression_NoLetSq_Yield AmpersandAmpersand BitwiseORExpression_Yield
;

LogicalANDExpression_NoObjLiteral :
  BitwiseORExpression_NoObjLiteral
| LogicalANDExpression_NoObjLiteral AmpersandAmpersand BitwiseORExpression
;

LogicalANDExpression_Yield :
  BitwiseORExpression_Yield
| LogicalANDExpression_Yield AmpersandAmpersand BitwiseORExpression_Yield
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

LogicalORExpression_NoLetSq :
  LogicalANDExpression_NoLetSq
| LogicalORExpression_NoLetSq OrOr LogicalANDExpression
;

LogicalORExpression_NoLetSq_Yield :
  LogicalANDExpression_NoLetSq_Yield
| LogicalORExpression_NoLetSq_Yield OrOr LogicalANDExpression_Yield
;

LogicalORExpression_NoObjLiteral :
  LogicalANDExpression_NoObjLiteral
| LogicalORExpression_NoObjLiteral OrOr LogicalANDExpression
;

LogicalORExpression_Yield :
  LogicalANDExpression_Yield
| LogicalORExpression_Yield OrOr LogicalANDExpression_Yield
;

ConditionalExpression :
  LogicalORExpression
| LogicalORExpression Questionmark AssignmentExpression_In Colon AssignmentExpression
;

ConditionalExpression_In :
  LogicalORExpression_In
| LogicalORExpression_In Questionmark AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_NoFuncClass :
  LogicalORExpression_In_NoFuncClass
| LogicalORExpression_In_NoFuncClass Questionmark AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral Questionmark AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| LogicalORExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield Questionmark AssignmentExpression_In_Yield Colon AssignmentExpression_In_Yield
;

ConditionalExpression_In_NoObjLiteral :
  LogicalORExpression_In_NoObjLiteral
| LogicalORExpression_In_NoObjLiteral Questionmark AssignmentExpression_In Colon AssignmentExpression_In
;

ConditionalExpression_In_Yield :
  LogicalORExpression_In_Yield
| LogicalORExpression_In_Yield Questionmark AssignmentExpression_In_Yield Colon AssignmentExpression_In_Yield
;

ConditionalExpression_NoLetSq :
  LogicalORExpression_NoLetSq
| LogicalORExpression_NoLetSq Questionmark AssignmentExpression_In Colon AssignmentExpression
;

ConditionalExpression_NoLetSq_Yield :
  LogicalORExpression_NoLetSq_Yield
| LogicalORExpression_NoLetSq_Yield Questionmark AssignmentExpression_In_Yield Colon AssignmentExpression_Yield
;

ConditionalExpression_NoObjLiteral :
  LogicalORExpression_NoObjLiteral
| LogicalORExpression_NoObjLiteral Questionmark AssignmentExpression_In Colon AssignmentExpression
;

ConditionalExpression_Yield :
  LogicalORExpression_Yield
| LogicalORExpression_Yield Questionmark AssignmentExpression_In_Yield Colon AssignmentExpression_Yield
;

AssignmentExpression :
  ConditionalExpression
| ArrowFunction
| LeftHandSideExpression Equal AssignmentExpression
| LeftHandSideExpression AssignmentOperator AssignmentExpression
;

AssignmentExpression_In :
  ConditionalExpression_In
| ArrowFunction_In
| LeftHandSideExpression Equal AssignmentExpression_In
| LeftHandSideExpression AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoFuncClass :
  ConditionalExpression_In_NoFuncClass
| ArrowFunction_In
| LeftHandSideExpression_NoFuncClass Equal AssignmentExpression_In
| LeftHandSideExpression_NoFuncClass AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral :
  ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral
| ArrowFunction_In
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral Equal AssignmentExpression_In
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield :
  ConditionalExpression_In_NoFuncClass_NoLetSq_NoObjLiteral_Yield
| YieldExpression_In
| ArrowFunction_In_Yield
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield Equal AssignmentExpression_In_Yield
| LeftHandSideExpression_NoFuncClass_NoLetSq_NoObjLiteral_Yield AssignmentOperator AssignmentExpression_In_Yield
;

AssignmentExpression_In_NoObjLiteral :
  ConditionalExpression_In_NoObjLiteral
| ArrowFunction_In
| LeftHandSideExpression_NoObjLiteral Equal AssignmentExpression_In
| LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_Yield :
  ConditionalExpression_In_Yield
| YieldExpression_In
| ArrowFunction_In_Yield
| LeftHandSideExpression_Yield Equal AssignmentExpression_In_Yield
| LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_In_Yield
;

AssignmentExpression_NoLetSq :
  ConditionalExpression_NoLetSq
| ArrowFunction
| LeftHandSideExpression_NoLetSq Equal AssignmentExpression
| LeftHandSideExpression_NoLetSq AssignmentOperator AssignmentExpression
;

AssignmentExpression_NoLetSq_Yield :
  ConditionalExpression_NoLetSq_Yield
| YieldExpression
| ArrowFunction_Yield
| LeftHandSideExpression_NoLetSq_Yield Equal AssignmentExpression_Yield
| LeftHandSideExpression_NoLetSq_Yield AssignmentOperator AssignmentExpression_Yield
;

AssignmentExpression_NoObjLiteral :
  ConditionalExpression_NoObjLiteral
| ArrowFunction
| LeftHandSideExpression_NoObjLiteral Equal AssignmentExpression
| LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression
;

AssignmentExpression_Yield :
  ConditionalExpression_Yield
| YieldExpression
| ArrowFunction_Yield
| LeftHandSideExpression_Yield Equal AssignmentExpression_Yield
| LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_Yield
;

AssignmentOperator :
  MultEqual
| SlashEqual
| PercentEqual
| PlusEqual
| MinusEqual
| LessLessEqual
| GreaterGreaterEqual
| GreaterGreaterGreaterEqual
| AmpersandEqual
| XorEqual
| OrEqual
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

Expression_NoLetSq :
  AssignmentExpression_NoLetSq
| Expression_NoLetSq Comma AssignmentExpression
;

Expression_NoLetSq_Yield :
  AssignmentExpression_NoLetSq_Yield
| Expression_NoLetSq_Yield Comma AssignmentExpression_Yield
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
  Lcurly StatementList Rcurly
| Lcurly Rcurly
;

Block_Return :
  Lcurly StatementList_Return Rcurly
| Lcurly Rcurly
;

Block_Return_Yield :
  Lcurly StatementList_Return_Yield Rcurly
| Lcurly Rcurly
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
  Lcurly Rcurly
| Lcurly BindingPropertyList Rcurly
| Lcurly BindingPropertyList Comma Rcurly
;

ObjectBindingPattern_Yield :
  Lcurly Rcurly
| Lcurly BindingPropertyList_Yield Rcurly
| Lcurly BindingPropertyList_Yield Comma Rcurly
;

ArrayBindingPattern :
  Lsquare Elisionopt BindingRestElementopt Rsquare
| Lsquare BindingElementList Rsquare
| Lsquare BindingElementList Comma Elisionopt BindingRestElementopt Rsquare
;

ArrayBindingPattern_Yield :
  Lsquare Elisionopt BindingRestElementopt_Yield Rsquare
| Lsquare BindingElementList_Yield Rsquare
| Lsquare BindingElementList_Yield Comma Elisionopt BindingRestElementopt_Yield Rsquare
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
| for Lparen Expressionopt_NoLetSq Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement
| for Lparen var VariableDeclarationList Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement
| for Lparen LexicalDeclaration Expressionopt_In Semicolon Expressionopt_In Rparen Statement
| for Lparen LeftHandSideExpression_NoLetSq in Expression_In Rparen Statement
| for Lparen var ForBinding in Expression_In Rparen Statement
| for Lparen ForDeclaration in Expression_In Rparen Statement
| for Lparen lookahead1 LeftHandSideExpression_NoLet of AssignmentExpression_In Rparen Statement
| for Lparen var ForBinding of AssignmentExpression_In Rparen Statement
| for Lparen ForDeclaration of AssignmentExpression_In Rparen Statement
;

IterationStatement_Return :
  do Statement_Return while Lparen Expression_In Rparen Semicolon
| while Lparen Expression_In Rparen Statement_Return
| for Lparen Expressionopt_NoLetSq Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement_Return
| for Lparen var VariableDeclarationList Semicolon Expressionopt_In Semicolon Expressionopt_In Rparen Statement_Return
| for Lparen LexicalDeclaration Expressionopt_In Semicolon Expressionopt_In Rparen Statement_Return
| for Lparen LeftHandSideExpression_NoLetSq in Expression_In Rparen Statement_Return
| for Lparen var ForBinding in Expression_In Rparen Statement_Return
| for Lparen ForDeclaration in Expression_In Rparen Statement_Return
| for Lparen lookahead1 LeftHandSideExpression_NoLet of AssignmentExpression_In Rparen Statement_Return
| for Lparen var ForBinding of AssignmentExpression_In Rparen Statement_Return
| for Lparen ForDeclaration of AssignmentExpression_In Rparen Statement_Return
;

IterationStatement_Return_Yield :
  do Statement_Return_Yield while Lparen Expression_In_Yield Rparen Semicolon
| while Lparen Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen Expressionopt_NoLetSq_Yield Semicolon Expressionopt_In_Yield Semicolon Expressionopt_In_Yield Rparen Statement_Return_Yield
| for Lparen var VariableDeclarationList_Yield Semicolon Expressionopt_In_Yield Semicolon Expressionopt_In_Yield Rparen Statement_Return_Yield
| for Lparen LexicalDeclaration_Yield Expressionopt_In_Yield Semicolon Expressionopt_In_Yield Rparen Statement_Return_Yield
| for Lparen LeftHandSideExpression_NoLetSq_Yield in Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen var ForBinding_Yield in Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen ForDeclaration_Yield in Expression_In_Yield Rparen Statement_Return_Yield
| for Lparen lookahead1 LeftHandSideExpression_NoLet_Yield of AssignmentExpression_In_Yield Rparen Statement_Return_Yield
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
  Lcurly CaseClausesopt Rcurly
| Lcurly CaseClausesopt DefaultClause CaseClausesopt Rcurly
;

CaseBlock_Return :
  Lcurly CaseClausesopt_Return Rcurly
| Lcurly CaseClausesopt_Return DefaultClause_Return CaseClausesopt_Return Rcurly
;

CaseBlock_Return_Yield :
  Lcurly CaseClausesopt_Return_Yield Rcurly
| Lcurly CaseClausesopt_Return_Yield DefaultClause_Return_Yield CaseClausesopt_Return_Yield Rcurly
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
  function BindingIdentifier Lparen FormalParameters Rparen Lcurly FunctionBody Rcurly
;

FunctionDeclaration_Default :
  function BindingIdentifier Lparen FormalParameters Rparen Lcurly FunctionBody Rcurly
| function Lparen FormalParameters Rparen Lcurly FunctionBody Rcurly
;

FunctionDeclaration_Yield :
  function BindingIdentifier_Yield Lparen FormalParameters Rparen Lcurly FunctionBody Rcurly
;

FunctionExpression :
  function BindingIdentifier Lparen FormalParameters Rparen Lcurly FunctionBody Rcurly
| function Lparen FormalParameters Rparen Lcurly FunctionBody Rcurly
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
  ArrowParameters EqualGreater ConciseBody
;

ArrowFunction_In :
  ArrowParameters EqualGreater ConciseBody_In
;

ArrowFunction_In_Yield :
  ArrowParameters_Yield EqualGreater ConciseBody_In
;

ArrowFunction_Yield :
  ArrowParameters_Yield EqualGreater ConciseBody
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
| Lcurly FunctionBody Rcurly
;

ConciseBody_In :
  AssignmentExpression_In_NoObjLiteral
| Lcurly FunctionBody Rcurly
;

MethodDefinition :
  PropertyName Lparen StrictFormalParameters Rparen Lcurly FunctionBody Rcurly
| GeneratorMethod
| get PropertyName Lparen Rparen Lcurly FunctionBody Rcurly
| set PropertyName Lparen PropertySetParameterList Rparen Lcurly FunctionBody Rcurly
;

MethodDefinition_Yield :
  PropertyName_Yield Lparen StrictFormalParameters_Yield Rparen Lcurly FunctionBody_Yield Rcurly
| GeneratorMethod_Yield
| get PropertyName_Yield Lparen Rparen Lcurly FunctionBody_Yield Rcurly
| set PropertyName_Yield Lparen PropertySetParameterList Rparen Lcurly FunctionBody_Yield Rcurly
;

PropertySetParameterList :
  FormalParameter
;

GeneratorMethod :
  Mult PropertyName Lparen StrictFormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
;

GeneratorMethod_Yield :
  Mult PropertyName_Yield Lparen StrictFormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
;

GeneratorDeclaration :
  function Mult BindingIdentifier Lparen FormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
;

GeneratorDeclaration_Default :
  function Mult BindingIdentifier Lparen FormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
| function Mult Lparen FormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
;

GeneratorDeclaration_Yield :
  function Mult BindingIdentifier_Yield Lparen FormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
;

GeneratorExpression :
  function Mult BindingIdentifier_Yield Lparen FormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
| function Mult Lparen FormalParameters_Yield Rparen Lcurly GeneratorBody Rcurly
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
  ClassHeritage Lcurly ClassBodyopt Rcurly
| Lcurly ClassBodyopt Rcurly
;

ClassTail_Yield :
  ClassHeritage_Yield Lcurly ClassBodyopt_Yield Rcurly
| Lcurly ClassBodyopt_Yield Rcurly
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
  Lcurly Rcurly
| Lcurly ImportsList Rcurly
| Lcurly ImportsList Comma Rcurly
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
  Lcurly Rcurly
| Lcurly ExportsList Rcurly
| Lcurly ExportsList Comma Rcurly
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

Expressionopt_NoLetSq :
  %empty
| Expression_NoLetSq
;

Expressionopt_NoLetSq_Yield :
  %empty
| Expression_NoLetSq_Yield
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

