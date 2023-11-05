%{
%}

%start Module
%start TypeSnippet
%start ExpressionSnippet
%start NamespaceNameSnippet
%start StartOfArrowFunction // no-eoi
%start StartOfParametrizedCall // no-eoi
%start StartLParen // no-eoi
%start StartOfExtendsTypeRef // no-eoi
%start StartOfTypeImport // no-eoi
%start StartOfIs // no-eoi
%start StartOfFunctionType // no-eoi
%start StartOfMappedType // no-eoi
%start StartOfTupleElementName // no-eoi

%left RESOLVESHIFT
%left OROR
%left ANDAND
%left QUESTQUEST
%left OR
%left XOR
%left AND
%left ASSIGNASSIGN EXCLASSIGN ASSIGNASSIGNASSIGN EXCLASSIGNASSIGN
%left LT GT LTASSIGN GTASSIGN INSTANCEOF IN AS SATISFIES
%left LTLT GTGT GTGTGT
%left MINUS PLUS
%left MULT DIV REM
%right MULTMULT
%right ELSE
%left KEYOF TYPEOF UNIQUE READONLY INFER
%nonassoc IS
%token INVALID_TOKEN
%token ERROR
%token WHITESPACE
%token MULTILINECOMMENT
%token SINGLELINECOMMENT
%token IDENTIFIER
%token PRIVATEIDENTIFIER
%token AWAIT
%token BREAK
%token CASE
%token CATCH
%token CLASS
%token CONST
%token CONTINUE
%token DEBUGGER
%token DEFAULT
%token DELETE
%token DO
%token EXPORT
%token EXTENDS
%token FINALLY
%token FOR
%token FUNCTION
%token IF
%token IMPORT
%token NEW
%token RETURN
%token SUPER
%token SWITCH
%token THIS
%token THROW
%token TRY
%token VAR
%token VOID
%token WHILE
%token WITH
%token YIELD
%token ENUM
%token NULL
%token TRUE
%token FALSE
%token ASSERT
%token ASSERTS
%token ASYNC
%token FROM
%token GET
%token LET
%token OF
%token SET
%token STATIC
%token TARGET
%token IMPLEMENTS
%token INTERFACE
%token PRIVATE
%token PROTECTED
%token PUBLIC
%token ANY
%token UNKNOWN
%token BOOLEAN
%token NUMBER
%token STRING
%token SYMBOL
%token BIGINT
%token UNDEFINED
%token NEVER
%token OBJECT
%token ABSTRACT
%token CONSTRUCTOR
%token DECLARE
%token GLOBAL
%token MODULE
%token NAMESPACE
%token OVERRIDE
%token REQUIRE
%token TYPE
%token ACCESSOR
%token LBRACE
%token RBRACE
%token LPAREN
%token RPAREN
%token LBRACK
%token RBRACK
%token DOT
%token DOTDOTDOT
%token SEMICOLON
%token COMMA
%token ATSIGN
%token PLUSPLUS
%token MINUSMINUS
%token EXCL
%token TILDE
%token QUEST
%token QUESTDOT
%token COLON
%token ASSIGN
%token PLUSASSIGN
%token MINUSASSIGN
%token MULTASSIGN
%token DIVASSIGN
%token REMASSIGN
%token LTLTASSIGN
%token GTGTASSIGN
%token GTGTGTASSIGN
%token ANDASSIGN
%token ORASSIGN
%token XORASSIGN
%token ASSIGNGT
%token MULTMULTASSIGN
%token QUESTQUESTASSIGN
%token ORORASSIGN
%token ANDANDASSIGN
%token NUMERICLITERAL
%token STRINGLITERAL
%token NOSUBSTITUTIONTEMPLATE
%token TEMPLATEHEAD
%token TEMPLATEMIDDLE
%token TEMPLATETAIL
%token REGULAREXPRESSIONLITERAL
%token JSXSTRINGLITERAL
%token JSXIDENTIFIER
%token JSXTEXT

%%

TypeSnippet :
  Type
;

ExpressionSnippet :
  Expression_In
;

NamespaceNameSnippet :
  NamespaceName
;

SyntaxError :
  ERROR
;

IdentifierName :
  IDENTIFIER
| NEW
| ASSERTS
| AWAIT
| BREAK
| DO
| IN
| TYPEOF
| CASE
| ELSE
| INSTANCEOF
| VAR
| CATCH
| EXPORT
| VOID
| CLASS
| EXTENDS
| RETURN
| WHILE
| CONST
| FINALLY
| SUPER
| WITH
| CONTINUE
| FOR
| SWITCH
| YIELD
| DEBUGGER
| FUNCTION
| THIS
| DEFAULT
| IF
| THROW
| DELETE
| IMPORT
| TRY
| ENUM
| NULL
| TRUE
| FALSE
| AS
| FROM
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| ASSERT
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| ACCESSOR
| SATISFIES
;

IdentifierName_WithoutAsserts :
  IDENTIFIER
| NEW
| AWAIT
| BREAK
| DO
| IN
| TYPEOF
| CASE
| ELSE
| INSTANCEOF
| VAR
| CATCH
| EXPORT
| VOID
| CLASS
| EXTENDS
| RETURN
| WHILE
| CONST
| FINALLY
| SUPER
| WITH
| CONTINUE
| FOR
| SWITCH
| YIELD
| DEBUGGER
| FUNCTION
| THIS
| DEFAULT
| IF
| THROW
| DELETE
| IMPORT
| TRY
| ENUM
| NULL
| TRUE
| FALSE
| AS
| FROM
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| ASSERT
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| ACCESSOR
| SATISFIES
;

IdentifierName_WithoutFrom :
  IDENTIFIER
| NEW
| ASSERTS
| AWAIT
| BREAK
| DO
| IN
| TYPEOF
| CASE
| ELSE
| INSTANCEOF
| VAR
| CATCH
| EXPORT
| VOID
| CLASS
| EXTENDS
| RETURN
| WHILE
| CONST
| FINALLY
| SUPER
| WITH
| CONTINUE
| FOR
| SWITCH
| YIELD
| DEBUGGER
| FUNCTION
| THIS
| DEFAULT
| IF
| THROW
| DELETE
| IMPORT
| TRY
| ENUM
| NULL
| TRUE
| FALSE
| AS
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| ASSERT
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| ACCESSOR
| SATISFIES
;

IdentifierName_WithoutKeywords_WithoutAs_WithoutSatisfies :
  IDENTIFIER
| NEW
| ASSERTS
| FROM
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| ASSERT
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| ACCESSOR
;

IdentifierName_WithoutNew :
  IDENTIFIER
| ASSERTS
| AWAIT
| BREAK
| DO
| IN
| TYPEOF
| CASE
| ELSE
| INSTANCEOF
| VAR
| CATCH
| EXPORT
| VOID
| CLASS
| EXTENDS
| RETURN
| WHILE
| CONST
| FINALLY
| SUPER
| WITH
| CONTINUE
| FOR
| SWITCH
| YIELD
| DEBUGGER
| FUNCTION
| THIS
| DEFAULT
| IF
| THROW
| DELETE
| IMPORT
| TRY
| ENUM
| NULL
| TRUE
| FALSE
| AS
| FROM
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| ASSERT
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| ACCESSOR
| SATISFIES
;

IdentifierNameDecl :
  IdentifierName
;

IdentifierNameDecl_WithoutNew :
  IdentifierName_WithoutNew
;

IdentifierNameRef :
  IdentifierName
;

IdentifierNameRef_WithoutAsserts :
  IdentifierName_WithoutAsserts
;

ClassPrivateRef :
  PRIVATEIDENTIFIER
;

IdentifierReference :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| AWAIT
| LET
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Await :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| LET
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Await_NoAsync_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Await_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_NoAsync_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| AWAIT
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| AWAIT
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_WithDefault :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| AWAIT
| LET
| DEFAULT
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_WithoutPredefinedTypes :
  IDENTIFIER
| REM IDENTIFIER
| YIELD
| AWAIT
| LET
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
;

IdentifierReference_Yield :
  IDENTIFIER
| REM IDENTIFIER
| AWAIT
| LET
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Yield_Await :
  IDENTIFIER
| REM IDENTIFIER
| LET
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Yield_Await_NoAsync_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Yield_Await_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Yield_NoAsync_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| AWAIT
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

IdentifierReference_Yield_NoLet :
  IDENTIFIER
| REM IDENTIFIER
| AWAIT
| ASYNC lookahead_notStartOfArrowFunction
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| OF
| SET
| STATIC
| TARGET
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| SATISFIES
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| ACCESSOR
| KEYOF
| UNIQUE
| READONLY
| INFER
;

// lookahead: !StartOfArrowFunction
lookahead_notStartOfArrowFunction :
  %empty
;

BindingIdentifier :
  IDENTIFIER
| YIELD
| AWAIT
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| SATISFIES
| ACCESSOR
;

BindingIdentifier_WithoutImplements :
  IDENTIFIER
| YIELD
| AWAIT
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| ABSTRACT
| CONSTRUCTOR
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| SATISFIES
| ACCESSOR
;

LabelIdentifier :
  IDENTIFIER
| YIELD
| AWAIT
| AS
| ASSERT
| ASSERTS
| FROM
| GET
| LET
| OF
| SET
| STATIC
| TARGET
| ASYNC
| IMPLEMENTS
| INTERFACE
| PRIVATE
| PROTECTED
| PUBLIC
| ANY
| UNKNOWN
| BOOLEAN
| NUMBER
| STRING
| SYMBOL
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
| DECLARE
| IS
| MODULE
| NAMESPACE
| OVERRIDE
| REQUIRE
| TYPE
| GLOBAL
| READONLY
| KEYOF
| UNIQUE
| INFER
| SATISFIES
| ACCESSOR
;

PrimaryExpression :
  THIS
| IdentifierReference
| Literal
| ArrayLiteral
| ObjectLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral
| lookahead_notStartOfArrowFunction Parenthesized
| lookahead_notStartOfArrowFunction JSXElement
| lookahead_notStartOfArrowFunction JSXFragment
;

PrimaryExpression_Await :
  THIS
| IdentifierReference_Await
| Literal
| ArrayLiteral_Await
| ObjectLiteral_Await
| FunctionExpression
| ClassExpression_Await
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Await
| lookahead_notStartOfArrowFunction Parenthesized_Await
| lookahead_notStartOfArrowFunction JSXElement_Await
| lookahead_notStartOfArrowFunction JSXFragment_Await
;

PrimaryExpression_Await_NoAsync_NoLet :
  THIS
| IdentifierReference_Await_NoAsync_NoLet
| Literal
| ArrayLiteral_Await
| ObjectLiteral_Await
| FunctionExpression
| ClassExpression_Await
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Await
| lookahead_notStartOfArrowFunction Parenthesized_Await
| lookahead_notStartOfArrowFunction JSXElement_Await
| lookahead_notStartOfArrowFunction JSXFragment_Await
;

PrimaryExpression_Await_NoLet :
  THIS
| IdentifierReference_Await_NoLet
| Literal
| ArrayLiteral_Await
| ObjectLiteral_Await
| FunctionExpression
| ClassExpression_Await
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Await
| lookahead_notStartOfArrowFunction Parenthesized_Await
| lookahead_notStartOfArrowFunction JSXElement_Await
| lookahead_notStartOfArrowFunction JSXFragment_Await
;

PrimaryExpression_Await_NoLet_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference_Await_NoLet
| Literal
| ArrayLiteral_Await
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Await
| lookahead_notStartOfArrowFunction Parenthesized_Await
| lookahead_notStartOfArrowFunction JSXElement_Await
| lookahead_notStartOfArrowFunction JSXFragment_Await
;

PrimaryExpression_Await_NoObjLiteral :
  THIS
| IdentifierReference_Await
| Literal
| ArrayLiteral_Await
| FunctionExpression
| ClassExpression_Await
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Await
| lookahead_notStartOfArrowFunction Parenthesized_Await
| lookahead_notStartOfArrowFunction JSXElement_Await
| lookahead_notStartOfArrowFunction JSXFragment_Await
;

PrimaryExpression_Await_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference_Await
| Literal
| ArrayLiteral_Await
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Await
| lookahead_notStartOfArrowFunction Parenthesized_Await
| lookahead_notStartOfArrowFunction JSXElement_Await
| lookahead_notStartOfArrowFunction JSXFragment_Await
;

PrimaryExpression_NoAsync_NoLet :
  THIS
| IdentifierReference_NoAsync_NoLet
| Literal
| ArrayLiteral
| ObjectLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral
| lookahead_notStartOfArrowFunction Parenthesized
| lookahead_notStartOfArrowFunction JSXElement
| lookahead_notStartOfArrowFunction JSXFragment
;

PrimaryExpression_NoFuncClass :
  THIS
| IdentifierReference
| Literal
| ArrayLiteral
| ObjectLiteral
| REGULAREXPRESSIONLITERAL
| TemplateLiteral
| lookahead_notStartOfArrowFunction Parenthesized
| lookahead_notStartOfArrowFunction JSXElement
| lookahead_notStartOfArrowFunction JSXFragment
;

PrimaryExpression_NoLet :
  THIS
| IdentifierReference_NoLet
| Literal
| ArrayLiteral
| ObjectLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral
| lookahead_notStartOfArrowFunction Parenthesized
| lookahead_notStartOfArrowFunction JSXElement
| lookahead_notStartOfArrowFunction JSXFragment
;

PrimaryExpression_NoLet_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference_NoLet
| Literal
| ArrayLiteral
| REGULAREXPRESSIONLITERAL
| TemplateLiteral
| lookahead_notStartOfArrowFunction Parenthesized
| lookahead_notStartOfArrowFunction JSXElement
| lookahead_notStartOfArrowFunction JSXFragment
;

PrimaryExpression_NoObjLiteral :
  THIS
| IdentifierReference
| Literal
| ArrayLiteral
| FunctionExpression
| ClassExpression
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral
| lookahead_notStartOfArrowFunction Parenthesized
| lookahead_notStartOfArrowFunction JSXElement
| lookahead_notStartOfArrowFunction JSXFragment
;

PrimaryExpression_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference
| Literal
| ArrayLiteral
| REGULAREXPRESSIONLITERAL
| TemplateLiteral
| lookahead_notStartOfArrowFunction Parenthesized
| lookahead_notStartOfArrowFunction JSXElement
| lookahead_notStartOfArrowFunction JSXFragment
;

PrimaryExpression_Yield :
  THIS
| IdentifierReference_Yield
| Literal
| ArrayLiteral_Yield
| ObjectLiteral_Yield
| FunctionExpression
| ClassExpression_Yield
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield
| lookahead_notStartOfArrowFunction Parenthesized_Yield
| lookahead_notStartOfArrowFunction JSXElement_Yield
| lookahead_notStartOfArrowFunction JSXFragment_Yield
;

PrimaryExpression_Yield_Await :
  THIS
| IdentifierReference_Yield_Await
| Literal
| ArrayLiteral_Yield_Await
| ObjectLiteral_Yield_Await
| FunctionExpression
| ClassExpression_Yield_Await
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield_Await
| lookahead_notStartOfArrowFunction Parenthesized_Yield_Await
| lookahead_notStartOfArrowFunction JSXElement_Yield_Await
| lookahead_notStartOfArrowFunction JSXFragment_Yield_Await
;

PrimaryExpression_Yield_Await_NoAsync_NoLet :
  THIS
| IdentifierReference_Yield_Await_NoAsync_NoLet
| Literal
| ArrayLiteral_Yield_Await
| ObjectLiteral_Yield_Await
| FunctionExpression
| ClassExpression_Yield_Await
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield_Await
| lookahead_notStartOfArrowFunction Parenthesized_Yield_Await
| lookahead_notStartOfArrowFunction JSXElement_Yield_Await
| lookahead_notStartOfArrowFunction JSXFragment_Yield_Await
;

PrimaryExpression_Yield_Await_NoLet :
  THIS
| IdentifierReference_Yield_Await_NoLet
| Literal
| ArrayLiteral_Yield_Await
| ObjectLiteral_Yield_Await
| FunctionExpression
| ClassExpression_Yield_Await
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield_Await
| lookahead_notStartOfArrowFunction Parenthesized_Yield_Await
| lookahead_notStartOfArrowFunction JSXElement_Yield_Await
| lookahead_notStartOfArrowFunction JSXFragment_Yield_Await
;

PrimaryExpression_Yield_Await_NoLet_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference_Yield_Await_NoLet
| Literal
| ArrayLiteral_Yield_Await
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield_Await
| lookahead_notStartOfArrowFunction Parenthesized_Yield_Await
| lookahead_notStartOfArrowFunction JSXElement_Yield_Await
| lookahead_notStartOfArrowFunction JSXFragment_Yield_Await
;

PrimaryExpression_Yield_Await_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference_Yield_Await
| Literal
| ArrayLiteral_Yield_Await
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield_Await
| lookahead_notStartOfArrowFunction Parenthesized_Yield_Await
| lookahead_notStartOfArrowFunction JSXElement_Yield_Await
| lookahead_notStartOfArrowFunction JSXFragment_Yield_Await
;

PrimaryExpression_Yield_NoAsync_NoLet :
  THIS
| IdentifierReference_Yield_NoAsync_NoLet
| Literal
| ArrayLiteral_Yield
| ObjectLiteral_Yield
| FunctionExpression
| ClassExpression_Yield
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield
| lookahead_notStartOfArrowFunction Parenthesized_Yield
| lookahead_notStartOfArrowFunction JSXElement_Yield
| lookahead_notStartOfArrowFunction JSXFragment_Yield
;

PrimaryExpression_Yield_NoLet :
  THIS
| IdentifierReference_Yield_NoLet
| Literal
| ArrayLiteral_Yield
| ObjectLiteral_Yield
| FunctionExpression
| ClassExpression_Yield
| GeneratorExpression
| AsyncFunctionExpression
| AsyncGeneratorExpression
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield
| lookahead_notStartOfArrowFunction Parenthesized_Yield
| lookahead_notStartOfArrowFunction JSXElement_Yield
| lookahead_notStartOfArrowFunction JSXFragment_Yield
;

PrimaryExpression_Yield_NoLet_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference_Yield_NoLet
| Literal
| ArrayLiteral_Yield
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield
| lookahead_notStartOfArrowFunction Parenthesized_Yield
| lookahead_notStartOfArrowFunction JSXElement_Yield
| lookahead_notStartOfArrowFunction JSXFragment_Yield
;

PrimaryExpression_Yield_NoObjLiteral_NoFuncClass :
  THIS
| IdentifierReference_Yield
| Literal
| ArrayLiteral_Yield
| REGULAREXPRESSIONLITERAL
| TemplateLiteral_Yield
| lookahead_notStartOfArrowFunction Parenthesized_Yield
| lookahead_notStartOfArrowFunction JSXElement_Yield
| lookahead_notStartOfArrowFunction JSXFragment_Yield
;

Parenthesized :
  LPAREN Expression_In RPAREN
| LPAREN BrokenExpr RPAREN
;

Parenthesized_Await :
  LPAREN Expression_In_Await RPAREN
| LPAREN BrokenExpr RPAREN
;

Parenthesized_Yield :
  LPAREN Expression_In_Yield RPAREN
| LPAREN BrokenExpr RPAREN
;

Parenthesized_Yield_Await :
  LPAREN Expression_In_Yield_Await RPAREN
| LPAREN BrokenExpr RPAREN
;

Literal :
  NULL
| TRUE
| FALSE
| NUMERICLITERAL
| STRINGLITERAL
;

ArrayLiteral :
  LBRACK Elisionopt RBRACK
| LBRACK ElementList RBRACK
| LBRACK ElementList COMMA Elisionopt RBRACK
;

ArrayLiteral_Await :
  LBRACK Elisionopt RBRACK
| LBRACK ElementList_Await RBRACK
| LBRACK ElementList_Await COMMA Elisionopt RBRACK
;

ArrayLiteral_Yield :
  LBRACK Elisionopt RBRACK
| LBRACK ElementList_Yield RBRACK
| LBRACK ElementList_Yield COMMA Elisionopt RBRACK
;

ArrayLiteral_Yield_Await :
  LBRACK Elisionopt RBRACK
| LBRACK ElementList_Yield_Await RBRACK
| LBRACK ElementList_Yield_Await COMMA Elisionopt RBRACK
;

ElementList :
  Elisionopt AssignmentExpression_In
| Elisionopt SpreadElement
| ElementList COMMA Elisionopt AssignmentExpression_In
| ElementList COMMA Elisionopt SpreadElement
;

ElementList_Await :
  Elisionopt AssignmentExpression_In_Await
| Elisionopt SpreadElement_Await
| ElementList_Await COMMA Elisionopt AssignmentExpression_In_Await
| ElementList_Await COMMA Elisionopt SpreadElement_Await
;

ElementList_Yield :
  Elisionopt AssignmentExpression_In_Yield
| Elisionopt SpreadElement_Yield
| ElementList_Yield COMMA Elisionopt AssignmentExpression_In_Yield
| ElementList_Yield COMMA Elisionopt SpreadElement_Yield
;

ElementList_Yield_Await :
  Elisionopt AssignmentExpression_In_Yield_Await
| Elisionopt SpreadElement_Yield_Await
| ElementList_Yield_Await COMMA Elisionopt AssignmentExpression_In_Yield_Await
| ElementList_Yield_Await COMMA Elisionopt SpreadElement_Yield_Await
;

Elision :
  COMMA
| Elision COMMA
;

SpreadElement :
  DOTDOTDOT AssignmentExpression_In
;

SpreadElement_Await :
  DOTDOTDOT AssignmentExpression_In_Await
;

SpreadElement_Yield :
  DOTDOTDOT AssignmentExpression_In_Yield
;

SpreadElement_Yield_Await :
  DOTDOTDOT AssignmentExpression_In_Yield_Await
;

ObjectLiteral :
  LBRACE RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList COMMA RBRACE
;

ObjectLiteral_Await :
  LBRACE RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList_Await RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList_Await COMMA RBRACE
;

ObjectLiteral_Yield :
  LBRACE RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList_Yield RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList_Yield COMMA RBRACE
;

ObjectLiteral_Yield_Await :
  LBRACE RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList_Yield_Await RBRACE
| LBRACE /*.recoveryScope*/ PropertyDefinitionList_Yield_Await COMMA RBRACE
;

PropertyDefinitionList :
  PropertyDefinition
| PropertyDefinitionList COMMA PropertyDefinition
;

PropertyDefinitionList_Await :
  PropertyDefinition_Await
| PropertyDefinitionList_Await COMMA PropertyDefinition_Await
;

PropertyDefinitionList_Yield :
  PropertyDefinition_Yield
| PropertyDefinitionList_Yield COMMA PropertyDefinition_Yield
;

PropertyDefinitionList_Yield_Await :
  PropertyDefinition_Yield_Await
| PropertyDefinitionList_Yield_Await COMMA PropertyDefinition_Yield_Await
;

PropertyDefinition :
  IdentifierReference
| Modifiers PropertyName COLON AssignmentExpression_In
| PropertyName COLON AssignmentExpression_In
| Modifiers MethodDefinition
| MethodDefinition
| CoverInitializedName
| SyntaxError
| DOTDOTDOT AssignmentExpression_In
;

PropertyDefinition_Await :
  IdentifierReference_Await
| Modifiers PropertyName_Await COLON AssignmentExpression_In_Await
| PropertyName_Await COLON AssignmentExpression_In_Await
| Modifiers MethodDefinition_Await
| MethodDefinition_Await
| CoverInitializedName_Await
| SyntaxError
| DOTDOTDOT AssignmentExpression_In_Await
;

PropertyDefinition_Yield :
  IdentifierReference_Yield
| Modifiers PropertyName_Yield COLON AssignmentExpression_In_Yield
| PropertyName_Yield COLON AssignmentExpression_In_Yield
| Modifiers MethodDefinition_Yield
| MethodDefinition_Yield
| CoverInitializedName_Yield
| SyntaxError
| DOTDOTDOT AssignmentExpression_In_Yield
;

PropertyDefinition_Yield_Await :
  IdentifierReference_Yield_Await
| Modifiers PropertyName_Yield_Await COLON AssignmentExpression_In_Yield_Await
| PropertyName_Yield_Await COLON AssignmentExpression_In_Yield_Await
| Modifiers MethodDefinition_Yield_Await
| MethodDefinition_Yield_Await
| CoverInitializedName_Yield_Await
| SyntaxError
| DOTDOTDOT AssignmentExpression_In_Yield_Await
;

PropertyName :
  LiteralPropertyName
| ComputedPropertyName
;

PropertyName_Await :
  LiteralPropertyName
| ComputedPropertyName_Await
;

PropertyName_WithoutNew :
  LiteralPropertyName_WithoutNew
| ComputedPropertyName
;

PropertyName_Yield :
  LiteralPropertyName
| ComputedPropertyName_Yield
;

PropertyName_Yield_Await :
  LiteralPropertyName
| ComputedPropertyName_Yield_Await
;

LiteralPropertyName :
  IdentifierNameDecl
| PRIVATEIDENTIFIER
| STRINGLITERAL
| NUMERICLITERAL
;

LiteralPropertyName_WithoutNew :
  IdentifierNameDecl_WithoutNew
| PRIVATEIDENTIFIER
| STRINGLITERAL
| NUMERICLITERAL
;

ComputedPropertyName :
  LBRACK AssignmentExpression_In RBRACK
;

ComputedPropertyName_Await :
  LBRACK AssignmentExpression_In_Await RBRACK
;

ComputedPropertyName_Yield :
  LBRACK AssignmentExpression_In_Yield RBRACK
;

ComputedPropertyName_Yield_Await :
  LBRACK AssignmentExpression_In_Yield_Await RBRACK
;

CoverInitializedName :
  IdentifierReference Initializer_In
;

CoverInitializedName_Await :
  IdentifierReference_Await Initializer_In_Await
;

CoverInitializedName_Yield :
  IdentifierReference_Yield Initializer_In_Yield
;

CoverInitializedName_Yield_Await :
  IdentifierReference_Yield_Await Initializer_In_Yield_Await
;

Initializer :
  ASSIGN AssignmentExpression
| ASSIGN BrokenExpr
;

Initializer_Await :
  ASSIGN AssignmentExpression_Await
| ASSIGN BrokenExpr
;

Initializer_In :
  ASSIGN AssignmentExpression_In
| ASSIGN BrokenExpr
;

Initializer_In_Await :
  ASSIGN AssignmentExpression_In_Await
| ASSIGN BrokenExpr
;

Initializer_In_Yield :
  ASSIGN AssignmentExpression_In_Yield
| ASSIGN BrokenExpr
;

Initializer_In_Yield_Await :
  ASSIGN AssignmentExpression_In_Yield_Await
| ASSIGN BrokenExpr
;

Initializer_Yield :
  ASSIGN AssignmentExpression_Yield
| ASSIGN BrokenExpr
;

Initializer_Yield_Await :
  ASSIGN AssignmentExpression_Yield_Await
| ASSIGN BrokenExpr
;

TemplateLiteral :
  NOSUBSTITUTIONTEMPLATE
| TEMPLATEHEAD Expression_In TemplateSpans
;

TemplateLiteral_Await :
  NOSUBSTITUTIONTEMPLATE
| TEMPLATEHEAD Expression_In_Await TemplateSpans_Await
;

TemplateLiteral_Yield :
  NOSUBSTITUTIONTEMPLATE
| TEMPLATEHEAD Expression_In_Yield TemplateSpans_Yield
;

TemplateLiteral_Yield_Await :
  NOSUBSTITUTIONTEMPLATE
| TEMPLATEHEAD Expression_In_Yield_Await TemplateSpans_Yield_Await
;

TemplateSpans :
  TEMPLATETAIL
| TemplateMiddleList TEMPLATETAIL
;

TemplateSpans_Await :
  TEMPLATETAIL
| TemplateMiddleList_Await TEMPLATETAIL
;

TemplateSpans_Yield :
  TEMPLATETAIL
| TemplateMiddleList_Yield TEMPLATETAIL
;

TemplateSpans_Yield_Await :
  TEMPLATETAIL
| TemplateMiddleList_Yield_Await TEMPLATETAIL
;

TemplateMiddleList :
  TEMPLATEMIDDLE Expression_In
| TemplateMiddleList TEMPLATEMIDDLE Expression_In
;

TemplateMiddleList_Await :
  TEMPLATEMIDDLE Expression_In_Await
| TemplateMiddleList_Await TEMPLATEMIDDLE Expression_In_Await
;

TemplateMiddleList_Yield :
  TEMPLATEMIDDLE Expression_In_Yield
| TemplateMiddleList_Yield TEMPLATEMIDDLE Expression_In_Yield
;

TemplateMiddleList_Yield_Await :
  TEMPLATEMIDDLE Expression_In_Yield_Await
| TemplateMiddleList_Yield_Await TEMPLATEMIDDLE Expression_In_Yield_Await
;

TemplateLiteralType :
  NOSUBSTITUTIONTEMPLATE
| TEMPLATEHEAD Type TemplateSpansType
;

TemplateSpansType :
  TEMPLATETAIL
| TemplateMiddleListType TEMPLATETAIL
;

TemplateMiddleListType :
  TEMPLATEMIDDLE Type
| TemplateMiddleListType TEMPLATEMIDDLE Type
;

MemberExpression :
  PrimaryExpression
| MemberExpression LBRACK Expression_In RBRACK
| MemberExpression DOT IdentifierNameRef
| MemberExpression DOT ClassPrivateRef
| MemberExpression TemplateLiteral
| MemberExpression /*.noLineBreak*/ EXCL
| SuperProperty
| MetaProperty
| NEW MemberExpression Arguments
;

MemberExpression_Await :
  PrimaryExpression_Await
| MemberExpression_Await LBRACK Expression_In_Await RBRACK
| MemberExpression_Await DOT IdentifierNameRef
| MemberExpression_Await DOT ClassPrivateRef
| MemberExpression_Await TemplateLiteral_Await
| MemberExpression_Await /*.noLineBreak*/ EXCL
| SuperProperty_Await
| MetaProperty
| NEW MemberExpression_Await Arguments_Await
;

MemberExpression_Await_NoAsync_NoLet :
  PrimaryExpression_Await_NoAsync_NoLet
| MemberExpression_Await_NoLet LBRACK Expression_In_Await RBRACK
| MemberExpression_Await_NoLet DOT IdentifierNameRef
| MemberExpression_Await_NoLet DOT ClassPrivateRef
| MemberExpression_Await_NoLet TemplateLiteral_Await
| MemberExpression_Await_NoLet /*.noLineBreak*/ EXCL
| SuperProperty_Await
| MetaProperty
| NEW MemberExpression_Await Arguments_Await
;

MemberExpression_Await_NoLet :
  PrimaryExpression_Await_NoLet
| MemberExpression_Await_NoLet LBRACK Expression_In_Await RBRACK
| MemberExpression_Await_NoLet DOT IdentifierNameRef
| MemberExpression_Await_NoLet DOT ClassPrivateRef
| MemberExpression_Await_NoLet TemplateLiteral_Await
| MemberExpression_Await_NoLet /*.noLineBreak*/ EXCL
| SuperProperty_Await
| MetaProperty
| NEW MemberExpression_Await Arguments_Await
;

MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  PrimaryExpression_Await_NoObjLiteral_NoFuncClass
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In_Await RBRACK
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Await
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty_Await
| MetaProperty
| NEW MemberExpression_Await Arguments_Await
;

MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly :
  PrimaryExpression_Await_NoLet_NoObjLiteral_NoFuncClass
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In_Await RBRACK
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Await
| MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty_Await
| MetaProperty
| NEW MemberExpression_Await Arguments_Await
;

MemberExpression_Await_NoObjLiteral :
  PrimaryExpression_Await_NoObjLiteral
| MemberExpression_Await_NoObjLiteral LBRACK Expression_In_Await RBRACK
| MemberExpression_Await_NoObjLiteral DOT IdentifierNameRef
| MemberExpression_Await_NoObjLiteral DOT ClassPrivateRef
| MemberExpression_Await_NoObjLiteral TemplateLiteral_Await
| MemberExpression_Await_NoObjLiteral /*.noLineBreak*/ EXCL
| SuperProperty_Await
| MetaProperty
| NEW MemberExpression_Await Arguments_Await
;

MemberExpression_Await_StartWithLet :
  LET
| MemberExpression_Await_StartWithLet_NoLetOnly LBRACK Expression_In_Await RBRACK
| MemberExpression_Await_StartWithLet DOT IdentifierNameRef
| MemberExpression_Await_StartWithLet DOT ClassPrivateRef
| MemberExpression_Await_StartWithLet TemplateLiteral_Await
| MemberExpression_Await_StartWithLet /*.noLineBreak*/ EXCL
;

MemberExpression_Await_StartWithLet_NoLetOnly :
  MemberExpression_Await_StartWithLet_NoLetOnly LBRACK Expression_In_Await RBRACK
| MemberExpression_Await_StartWithLet DOT IdentifierNameRef
| MemberExpression_Await_StartWithLet DOT ClassPrivateRef
| MemberExpression_Await_StartWithLet TemplateLiteral_Await
| MemberExpression_Await_StartWithLet /*.noLineBreak*/ EXCL
;

MemberExpression_NoAsync_NoLet :
  PrimaryExpression_NoAsync_NoLet
| MemberExpression_NoLet LBRACK Expression_In RBRACK
| MemberExpression_NoLet DOT IdentifierNameRef
| MemberExpression_NoLet DOT ClassPrivateRef
| MemberExpression_NoLet TemplateLiteral
| MemberExpression_NoLet /*.noLineBreak*/ EXCL
| SuperProperty
| MetaProperty
| NEW MemberExpression Arguments
;

MemberExpression_NoFuncClass :
  PrimaryExpression_NoFuncClass
| MemberExpression_NoFuncClass LBRACK Expression_In RBRACK
| MemberExpression_NoFuncClass DOT IdentifierNameRef
| MemberExpression_NoFuncClass DOT ClassPrivateRef
| MemberExpression_NoFuncClass TemplateLiteral
| MemberExpression_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty
| MetaProperty
| NEW MemberExpression Arguments
;

MemberExpression_NoLet :
  PrimaryExpression_NoLet
| MemberExpression_NoLet LBRACK Expression_In RBRACK
| MemberExpression_NoLet DOT IdentifierNameRef
| MemberExpression_NoLet DOT ClassPrivateRef
| MemberExpression_NoLet TemplateLiteral
| MemberExpression_NoLet /*.noLineBreak*/ EXCL
| SuperProperty
| MetaProperty
| NEW MemberExpression Arguments
;

MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass :
  PrimaryExpression_NoObjLiteral_NoFuncClass
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In RBRACK
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty
| MetaProperty
| NEW MemberExpression Arguments
;

MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly :
  PrimaryExpression_NoLet_NoObjLiteral_NoFuncClass
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In RBRACK
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral
| MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty
| MetaProperty
| NEW MemberExpression Arguments
;

MemberExpression_NoObjLiteral :
  PrimaryExpression_NoObjLiteral
| MemberExpression_NoObjLiteral LBRACK Expression_In RBRACK
| MemberExpression_NoObjLiteral DOT IdentifierNameRef
| MemberExpression_NoObjLiteral DOT ClassPrivateRef
| MemberExpression_NoObjLiteral TemplateLiteral
| MemberExpression_NoObjLiteral /*.noLineBreak*/ EXCL
| SuperProperty
| MetaProperty
| NEW MemberExpression Arguments
;

MemberExpression_StartWithLet :
  LET
| MemberExpression_StartWithLet_NoLetOnly LBRACK Expression_In RBRACK
| MemberExpression_StartWithLet DOT IdentifierNameRef
| MemberExpression_StartWithLet DOT ClassPrivateRef
| MemberExpression_StartWithLet TemplateLiteral
| MemberExpression_StartWithLet /*.noLineBreak*/ EXCL
;

MemberExpression_StartWithLet_NoLetOnly :
  MemberExpression_StartWithLet_NoLetOnly LBRACK Expression_In RBRACK
| MemberExpression_StartWithLet DOT IdentifierNameRef
| MemberExpression_StartWithLet DOT ClassPrivateRef
| MemberExpression_StartWithLet TemplateLiteral
| MemberExpression_StartWithLet /*.noLineBreak*/ EXCL
;

MemberExpression_Yield :
  PrimaryExpression_Yield
| MemberExpression_Yield LBRACK Expression_In_Yield RBRACK
| MemberExpression_Yield DOT IdentifierNameRef
| MemberExpression_Yield DOT ClassPrivateRef
| MemberExpression_Yield TemplateLiteral_Yield
| MemberExpression_Yield /*.noLineBreak*/ EXCL
| SuperProperty_Yield
| MetaProperty
| NEW MemberExpression_Yield Arguments_Yield
;

MemberExpression_Yield_Await :
  PrimaryExpression_Yield_Await
| MemberExpression_Yield_Await LBRACK Expression_In_Yield_Await RBRACK
| MemberExpression_Yield_Await DOT IdentifierNameRef
| MemberExpression_Yield_Await DOT ClassPrivateRef
| MemberExpression_Yield_Await TemplateLiteral_Yield_Await
| MemberExpression_Yield_Await /*.noLineBreak*/ EXCL
| SuperProperty_Yield_Await
| MetaProperty
| NEW MemberExpression_Yield_Await Arguments_Yield_Await
;

MemberExpression_Yield_Await_NoAsync_NoLet :
  PrimaryExpression_Yield_Await_NoAsync_NoLet
| MemberExpression_Yield_Await_NoLet LBRACK Expression_In_Yield_Await RBRACK
| MemberExpression_Yield_Await_NoLet DOT IdentifierNameRef
| MemberExpression_Yield_Await_NoLet DOT ClassPrivateRef
| MemberExpression_Yield_Await_NoLet TemplateLiteral_Yield_Await
| MemberExpression_Yield_Await_NoLet /*.noLineBreak*/ EXCL
| SuperProperty_Yield_Await
| MetaProperty
| NEW MemberExpression_Yield_Await Arguments_Yield_Await
;

MemberExpression_Yield_Await_NoLet :
  PrimaryExpression_Yield_Await_NoLet
| MemberExpression_Yield_Await_NoLet LBRACK Expression_In_Yield_Await RBRACK
| MemberExpression_Yield_Await_NoLet DOT IdentifierNameRef
| MemberExpression_Yield_Await_NoLet DOT ClassPrivateRef
| MemberExpression_Yield_Await_NoLet TemplateLiteral_Yield_Await
| MemberExpression_Yield_Await_NoLet /*.noLineBreak*/ EXCL
| SuperProperty_Yield_Await
| MetaProperty
| NEW MemberExpression_Yield_Await Arguments_Yield_Await
;

MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  PrimaryExpression_Yield_Await_NoObjLiteral_NoFuncClass
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In_Yield_Await RBRACK
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield_Await
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty_Yield_Await
| MetaProperty
| NEW MemberExpression_Yield_Await Arguments_Yield_Await
;

MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly :
  PrimaryExpression_Yield_Await_NoLet_NoObjLiteral_NoFuncClass
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In_Yield_Await RBRACK
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield_Await
| MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty_Yield_Await
| MetaProperty
| NEW MemberExpression_Yield_Await Arguments_Yield_Await
;

MemberExpression_Yield_Await_StartWithLet :
  LET
| MemberExpression_Yield_Await_StartWithLet_NoLetOnly LBRACK Expression_In_Yield_Await RBRACK
| MemberExpression_Yield_Await_StartWithLet DOT IdentifierNameRef
| MemberExpression_Yield_Await_StartWithLet DOT ClassPrivateRef
| MemberExpression_Yield_Await_StartWithLet TemplateLiteral_Yield_Await
| MemberExpression_Yield_Await_StartWithLet /*.noLineBreak*/ EXCL
;

MemberExpression_Yield_Await_StartWithLet_NoLetOnly :
  MemberExpression_Yield_Await_StartWithLet_NoLetOnly LBRACK Expression_In_Yield_Await RBRACK
| MemberExpression_Yield_Await_StartWithLet DOT IdentifierNameRef
| MemberExpression_Yield_Await_StartWithLet DOT ClassPrivateRef
| MemberExpression_Yield_Await_StartWithLet TemplateLiteral_Yield_Await
| MemberExpression_Yield_Await_StartWithLet /*.noLineBreak*/ EXCL
;

MemberExpression_Yield_NoAsync_NoLet :
  PrimaryExpression_Yield_NoAsync_NoLet
| MemberExpression_Yield_NoLet LBRACK Expression_In_Yield RBRACK
| MemberExpression_Yield_NoLet DOT IdentifierNameRef
| MemberExpression_Yield_NoLet DOT ClassPrivateRef
| MemberExpression_Yield_NoLet TemplateLiteral_Yield
| MemberExpression_Yield_NoLet /*.noLineBreak*/ EXCL
| SuperProperty_Yield
| MetaProperty
| NEW MemberExpression_Yield Arguments_Yield
;

MemberExpression_Yield_NoLet :
  PrimaryExpression_Yield_NoLet
| MemberExpression_Yield_NoLet LBRACK Expression_In_Yield RBRACK
| MemberExpression_Yield_NoLet DOT IdentifierNameRef
| MemberExpression_Yield_NoLet DOT ClassPrivateRef
| MemberExpression_Yield_NoLet TemplateLiteral_Yield
| MemberExpression_Yield_NoLet /*.noLineBreak*/ EXCL
| SuperProperty_Yield
| MetaProperty
| NEW MemberExpression_Yield Arguments_Yield
;

MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  PrimaryExpression_Yield_NoObjLiteral_NoFuncClass
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In_Yield RBRACK
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty_Yield
| MetaProperty
| NEW MemberExpression_Yield Arguments_Yield
;

MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly :
  PrimaryExpression_Yield_NoLet_NoObjLiteral_NoFuncClass
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoLetOnly LBRACK Expression_In_Yield RBRACK
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield
| MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| SuperProperty_Yield
| MetaProperty
| NEW MemberExpression_Yield Arguments_Yield
;

MemberExpression_Yield_StartWithLet :
  LET
| MemberExpression_Yield_StartWithLet_NoLetOnly LBRACK Expression_In_Yield RBRACK
| MemberExpression_Yield_StartWithLet DOT IdentifierNameRef
| MemberExpression_Yield_StartWithLet DOT ClassPrivateRef
| MemberExpression_Yield_StartWithLet TemplateLiteral_Yield
| MemberExpression_Yield_StartWithLet /*.noLineBreak*/ EXCL
;

MemberExpression_Yield_StartWithLet_NoLetOnly :
  MemberExpression_Yield_StartWithLet_NoLetOnly LBRACK Expression_In_Yield RBRACK
| MemberExpression_Yield_StartWithLet DOT IdentifierNameRef
| MemberExpression_Yield_StartWithLet DOT ClassPrivateRef
| MemberExpression_Yield_StartWithLet TemplateLiteral_Yield
| MemberExpression_Yield_StartWithLet /*.noLineBreak*/ EXCL
;

SuperExpression :
  SUPER
;

SuperProperty :
  SuperExpression LBRACK Expression_In RBRACK
| SuperExpression DOT IdentifierNameRef
;

SuperProperty_Await :
  SuperExpression LBRACK Expression_In_Await RBRACK
| SuperExpression DOT IdentifierNameRef
;

SuperProperty_Yield :
  SuperExpression LBRACK Expression_In_Yield RBRACK
| SuperExpression DOT IdentifierNameRef
;

SuperProperty_Yield_Await :
  SuperExpression LBRACK Expression_In_Yield_Await RBRACK
| SuperExpression DOT IdentifierNameRef
;

MetaProperty :
  NewTarget
;

NewTarget :
  NEW DOT TARGET
;

NewExpression :
  MemberExpression lookahead_notStartOfParametrizedCall
| NEW NewExpression
;

NewExpression_Await :
  MemberExpression_Await lookahead_notStartOfParametrizedCall
| NEW NewExpression_Await
;

NewExpression_Await_NoAsync_NoLet :
  MemberExpression_Await_NoAsync_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression_Await
;

NewExpression_Await_NoLet :
  MemberExpression_Await_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression_Await
;

NewExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| NEW NewExpression_Await
;

NewExpression_Await_NoObjLiteral :
  MemberExpression_Await_NoObjLiteral lookahead_notStartOfParametrizedCall
| NEW NewExpression_Await
;

NewExpression_Await_StartWithLet :
  MemberExpression_Await_StartWithLet lookahead_notStartOfParametrizedCall
;

NewExpression_NoAsync_NoLet :
  MemberExpression_NoAsync_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression
;

NewExpression_NoFuncClass :
  MemberExpression_NoFuncClass lookahead_notStartOfParametrizedCall
| NEW NewExpression
;

NewExpression_NoLet :
  MemberExpression_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression
;

NewExpression_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| NEW NewExpression
;

NewExpression_NoObjLiteral :
  MemberExpression_NoObjLiteral lookahead_notStartOfParametrizedCall
| NEW NewExpression
;

NewExpression_StartWithLet :
  MemberExpression_StartWithLet lookahead_notStartOfParametrizedCall
;

NewExpression_Yield :
  MemberExpression_Yield lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield
;

NewExpression_Yield_Await :
  MemberExpression_Yield_Await lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield_Await
;

NewExpression_Yield_Await_NoAsync_NoLet :
  MemberExpression_Yield_Await_NoAsync_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield_Await
;

NewExpression_Yield_Await_NoLet :
  MemberExpression_Yield_Await_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield_Await
;

NewExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield_Await
;

NewExpression_Yield_Await_StartWithLet :
  MemberExpression_Yield_Await_StartWithLet lookahead_notStartOfParametrizedCall
;

NewExpression_Yield_NoAsync_NoLet :
  MemberExpression_Yield_NoAsync_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield
;

NewExpression_Yield_NoLet :
  MemberExpression_Yield_NoLet lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield
;

NewExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| NEW NewExpression_Yield
;

NewExpression_Yield_StartWithLet :
  MemberExpression_Yield_StartWithLet lookahead_notStartOfParametrizedCall
;

// lookahead: !StartOfParametrizedCall
lookahead_notStartOfParametrizedCall :
  %empty
;

CallExpression :
  MemberExpression Arguments
| SuperCall
| IMPORT Arguments
| CallExpression Arguments
| CallExpression LBRACK Expression_In RBRACK
| CallExpression DOT IdentifierNameRef
| CallExpression DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression /*.noLineBreak*/ EXCL
| CallExpression TemplateLiteral
;

CallExpression_Await :
  MemberExpression_Await Arguments_Await
| SuperCall_Await
| IMPORT Arguments_Await
| CallExpression_Await Arguments_Await
| CallExpression_Await LBRACK Expression_In_Await RBRACK
| CallExpression_Await DOT IdentifierNameRef
| CallExpression_Await DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Await /*.noLineBreak*/ EXCL
| CallExpression_Await TemplateLiteral_Await
;

CallExpression_Await_NoLet :
  MemberExpression_Await_NoLet Arguments_Await
| SuperCall_Await
| IMPORT Arguments_Await
| CallExpression_Await_NoLet Arguments_Await
| CallExpression_Await_NoLet LBRACK Expression_In_Await RBRACK
| CallExpression_Await_NoLet DOT IdentifierNameRef
| CallExpression_Await_NoLet DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Await_NoLet /*.noLineBreak*/ EXCL
| CallExpression_Await_NoLet TemplateLiteral_Await
;

CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Await
| SuperCall_Await
| IMPORT Arguments_Await
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Await
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In_Await RBRACK
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Await
;

CallExpression_Await_NoObjLiteral :
  MemberExpression_Await_NoObjLiteral Arguments_Await
| SuperCall_Await
| IMPORT Arguments_Await
| CallExpression_Await_NoObjLiteral Arguments_Await
| CallExpression_Await_NoObjLiteral LBRACK Expression_In_Await RBRACK
| CallExpression_Await_NoObjLiteral DOT IdentifierNameRef
| CallExpression_Await_NoObjLiteral DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Await_NoObjLiteral /*.noLineBreak*/ EXCL
| CallExpression_Await_NoObjLiteral TemplateLiteral_Await
;

CallExpression_Await_StartWithLet :
  MemberExpression_Await_StartWithLet Arguments_Await
| CallExpression_Await_StartWithLet Arguments_Await
| CallExpression_Await_StartWithLet LBRACK Expression_In_Await RBRACK
| CallExpression_Await_StartWithLet DOT IdentifierNameRef
| CallExpression_Await_StartWithLet DOT ClassPrivateRef
| CallExpression_Await_StartWithLet /*.noLineBreak*/ EXCL
| CallExpression_Await_StartWithLet TemplateLiteral_Await
;

CallExpression_NoFuncClass :
  MemberExpression_NoFuncClass Arguments
| SuperCall
| IMPORT Arguments
| CallExpression_NoFuncClass Arguments
| CallExpression_NoFuncClass LBRACK Expression_In RBRACK
| CallExpression_NoFuncClass DOT IdentifierNameRef
| CallExpression_NoFuncClass DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_NoFuncClass /*.noLineBreak*/ EXCL
| CallExpression_NoFuncClass TemplateLiteral
;

CallExpression_NoLet :
  MemberExpression_NoLet Arguments
| SuperCall
| IMPORT Arguments
| CallExpression_NoLet Arguments
| CallExpression_NoLet LBRACK Expression_In RBRACK
| CallExpression_NoLet DOT IdentifierNameRef
| CallExpression_NoLet DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_NoLet /*.noLineBreak*/ EXCL
| CallExpression_NoLet TemplateLiteral
;

CallExpression_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass Arguments
| SuperCall
| IMPORT Arguments
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass Arguments
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In RBRACK
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral
;

CallExpression_NoObjLiteral :
  MemberExpression_NoObjLiteral Arguments
| SuperCall
| IMPORT Arguments
| CallExpression_NoObjLiteral Arguments
| CallExpression_NoObjLiteral LBRACK Expression_In RBRACK
| CallExpression_NoObjLiteral DOT IdentifierNameRef
| CallExpression_NoObjLiteral DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_NoObjLiteral /*.noLineBreak*/ EXCL
| CallExpression_NoObjLiteral TemplateLiteral
;

CallExpression_StartWithLet :
  MemberExpression_StartWithLet Arguments
| CallExpression_StartWithLet Arguments
| CallExpression_StartWithLet LBRACK Expression_In RBRACK
| CallExpression_StartWithLet DOT IdentifierNameRef
| CallExpression_StartWithLet DOT ClassPrivateRef
| CallExpression_StartWithLet /*.noLineBreak*/ EXCL
| CallExpression_StartWithLet TemplateLiteral
;

CallExpression_Yield :
  MemberExpression_Yield Arguments_Yield
| SuperCall_Yield
| IMPORT Arguments_Yield
| CallExpression_Yield Arguments_Yield
| CallExpression_Yield LBRACK Expression_In_Yield RBRACK
| CallExpression_Yield DOT IdentifierNameRef
| CallExpression_Yield DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Yield /*.noLineBreak*/ EXCL
| CallExpression_Yield TemplateLiteral_Yield
;

CallExpression_Yield_Await :
  MemberExpression_Yield_Await Arguments_Yield_Await
| SuperCall_Yield_Await
| IMPORT Arguments_Yield_Await
| CallExpression_Yield_Await Arguments_Yield_Await
| CallExpression_Yield_Await LBRACK Expression_In_Yield_Await RBRACK
| CallExpression_Yield_Await DOT IdentifierNameRef
| CallExpression_Yield_Await DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Yield_Await /*.noLineBreak*/ EXCL
| CallExpression_Yield_Await TemplateLiteral_Yield_Await
;

CallExpression_Yield_Await_NoLet :
  MemberExpression_Yield_Await_NoLet Arguments_Yield_Await
| SuperCall_Yield_Await
| IMPORT Arguments_Yield_Await
| CallExpression_Yield_Await_NoLet Arguments_Yield_Await
| CallExpression_Yield_Await_NoLet LBRACK Expression_In_Yield_Await RBRACK
| CallExpression_Yield_Await_NoLet DOT IdentifierNameRef
| CallExpression_Yield_Await_NoLet DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Yield_Await_NoLet /*.noLineBreak*/ EXCL
| CallExpression_Yield_Await_NoLet TemplateLiteral_Yield_Await
;

CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Yield_Await
| SuperCall_Yield_Await
| IMPORT Arguments_Yield_Await
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Yield_Await
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In_Yield_Await RBRACK
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield_Await
;

CallExpression_Yield_Await_StartWithLet :
  MemberExpression_Yield_Await_StartWithLet Arguments_Yield_Await
| CallExpression_Yield_Await_StartWithLet Arguments_Yield_Await
| CallExpression_Yield_Await_StartWithLet LBRACK Expression_In_Yield_Await RBRACK
| CallExpression_Yield_Await_StartWithLet DOT IdentifierNameRef
| CallExpression_Yield_Await_StartWithLet DOT ClassPrivateRef
| CallExpression_Yield_Await_StartWithLet /*.noLineBreak*/ EXCL
| CallExpression_Yield_Await_StartWithLet TemplateLiteral_Yield_Await
;

CallExpression_Yield_NoLet :
  MemberExpression_Yield_NoLet Arguments_Yield
| SuperCall_Yield
| IMPORT Arguments_Yield
| CallExpression_Yield_NoLet Arguments_Yield
| CallExpression_Yield_NoLet LBRACK Expression_In_Yield RBRACK
| CallExpression_Yield_NoLet DOT IdentifierNameRef
| CallExpression_Yield_NoLet DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Yield_NoLet /*.noLineBreak*/ EXCL
| CallExpression_Yield_NoLet TemplateLiteral_Yield
;

CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Yield
| SuperCall_Yield
| IMPORT Arguments_Yield
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Yield
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In_Yield RBRACK
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| IMPORT DOT IdentifierNameRef
| IMPORT DOT ClassPrivateRef
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield
;

CallExpression_Yield_StartWithLet :
  MemberExpression_Yield_StartWithLet Arguments_Yield
| CallExpression_Yield_StartWithLet Arguments_Yield
| CallExpression_Yield_StartWithLet LBRACK Expression_In_Yield RBRACK
| CallExpression_Yield_StartWithLet DOT IdentifierNameRef
| CallExpression_Yield_StartWithLet DOT ClassPrivateRef
| CallExpression_Yield_StartWithLet /*.noLineBreak*/ EXCL
| CallExpression_Yield_StartWithLet TemplateLiteral_Yield
;

SuperCall :
  SuperExpression Arguments
;

SuperCall_Await :
  SuperExpression Arguments_Await
;

SuperCall_Yield :
  SuperExpression Arguments_Yield
;

SuperCall_Yield_Await :
  SuperExpression Arguments_Yield_Await
;

Arguments :
  lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList COMMA RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_notStartLParen
| LPAREN ArgumentList COMMA RPAREN
| LPAREN ArgumentList RPAREN
| LPAREN RPAREN
;

Arguments_Await :
  lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList_Await COMMA RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList_Await RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_notStartLParen
| LPAREN ArgumentList_Await COMMA RPAREN
| LPAREN ArgumentList_Await RPAREN
| LPAREN RPAREN
;

Arguments_Yield :
  lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList_Yield COMMA RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList_Yield RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_notStartLParen
| LPAREN ArgumentList_Yield COMMA RPAREN
| LPAREN ArgumentList_Yield RPAREN
| LPAREN RPAREN
;

Arguments_Yield_Await :
  lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList_Yield_Await COMMA RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN ArgumentList_Yield_Await RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_StartLParen LPAREN RPAREN
| lookahead_StartOfParametrizedCall TypeArguments lookahead_notStartLParen
| LPAREN ArgumentList_Yield_Await COMMA RPAREN
| LPAREN ArgumentList_Yield_Await RPAREN
| LPAREN RPAREN
;

// lookahead: StartLParen
lookahead_StartLParen :
  %empty
;

// lookahead: StartOfParametrizedCall
lookahead_StartOfParametrizedCall :
  %empty
;

// lookahead: !StartLParen
lookahead_notStartLParen :
  %empty
;

StartLParen :
  LPAREN
;

StartOfParametrizedCall :
  TypeArguments COMMA
| TypeArguments RPAREN
| TypeArguments SEMICOLON
| TypeArguments LPAREN
;

ArgumentList :
  AssignmentExpression_In
| SpreadElement
| BrokenExpr
| ArgumentList COMMA AssignmentExpression_In
| ArgumentList COMMA SpreadElement
| ArgumentList COMMA BrokenExpr
;

ArgumentList_Await :
  AssignmentExpression_In_Await
| SpreadElement_Await
| BrokenExpr
| ArgumentList_Await COMMA AssignmentExpression_In_Await
| ArgumentList_Await COMMA SpreadElement_Await
| ArgumentList_Await COMMA BrokenExpr
;

ArgumentList_Yield :
  AssignmentExpression_In_Yield
| SpreadElement_Yield
| BrokenExpr
| ArgumentList_Yield COMMA AssignmentExpression_In_Yield
| ArgumentList_Yield COMMA SpreadElement_Yield
| ArgumentList_Yield COMMA BrokenExpr
;

ArgumentList_Yield_Await :
  AssignmentExpression_In_Yield_Await
| SpreadElement_Yield_Await
| BrokenExpr
| ArgumentList_Yield_Await COMMA AssignmentExpression_In_Yield_Await
| ArgumentList_Yield_Await COMMA SpreadElement_Yield_Await
| ArgumentList_Yield_Await COMMA BrokenExpr
;

BrokenExpr :
  ERROR
;

OptionalLHS :
  MemberExpression
| CallExpression
| OptionalExpression
;

OptionalLHS_Await :
  MemberExpression_Await
| CallExpression_Await
| OptionalExpression_Await
;

OptionalLHS_Await_NoLet :
  MemberExpression_Await_NoLet
| CallExpression_Await_NoLet
| OptionalExpression_Await_NoLet
;

OptionalLHS_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass
;

OptionalLHS_Await_NoObjLiteral :
  MemberExpression_Await_NoObjLiteral
| CallExpression_Await_NoObjLiteral
| OptionalExpression_Await_NoObjLiteral
;

OptionalLHS_Await_StartWithLet :
  MemberExpression_Await_StartWithLet
| CallExpression_Await_StartWithLet
| OptionalExpression_Await_StartWithLet
;

OptionalLHS_NoFuncClass :
  MemberExpression_NoFuncClass
| CallExpression_NoFuncClass
| OptionalExpression_NoFuncClass
;

OptionalLHS_NoLet :
  MemberExpression_NoLet
| CallExpression_NoLet
| OptionalExpression_NoLet
;

OptionalLHS_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass
;

OptionalLHS_NoObjLiteral :
  MemberExpression_NoObjLiteral
| CallExpression_NoObjLiteral
| OptionalExpression_NoObjLiteral
;

OptionalLHS_StartWithLet :
  MemberExpression_StartWithLet
| CallExpression_StartWithLet
| OptionalExpression_StartWithLet
;

OptionalLHS_Yield :
  MemberExpression_Yield
| CallExpression_Yield
| OptionalExpression_Yield
;

OptionalLHS_Yield_Await :
  MemberExpression_Yield_Await
| CallExpression_Yield_Await
| OptionalExpression_Yield_Await
;

OptionalLHS_Yield_Await_NoLet :
  MemberExpression_Yield_Await_NoLet
| CallExpression_Yield_Await_NoLet
| OptionalExpression_Yield_Await_NoLet
;

OptionalLHS_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass
;

OptionalLHS_Yield_Await_StartWithLet :
  MemberExpression_Yield_Await_StartWithLet
| CallExpression_Yield_Await_StartWithLet
| OptionalExpression_Yield_Await_StartWithLet
;

OptionalLHS_Yield_NoLet :
  MemberExpression_Yield_NoLet
| CallExpression_Yield_NoLet
| OptionalExpression_Yield_NoLet
;

OptionalLHS_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  MemberExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass
;

OptionalLHS_Yield_StartWithLet :
  MemberExpression_Yield_StartWithLet
| CallExpression_Yield_StartWithLet
| OptionalExpression_Yield_StartWithLet
;

OptionalExpression :
  OptionalLHS QUESTDOT LBRACK Expression_In RBRACK
| OptionalLHS QUESTDOT IdentifierNameRef
| OptionalLHS QUESTDOT ClassPrivateRef
| OptionalLHS QUESTDOT Arguments
| OptionalLHS QUESTDOT TemplateLiteral
| OptionalExpression LBRACK Expression_In RBRACK
| OptionalExpression DOT IdentifierNameRef
| OptionalExpression DOT ClassPrivateRef
| OptionalExpression Arguments
| OptionalExpression /*.noLineBreak*/ EXCL
| OptionalExpression TemplateLiteral
;

OptionalExpression_Await :
  OptionalLHS_Await QUESTDOT LBRACK Expression_In_Await RBRACK
| OptionalLHS_Await QUESTDOT IdentifierNameRef
| OptionalLHS_Await QUESTDOT ClassPrivateRef
| OptionalLHS_Await QUESTDOT Arguments_Await
| OptionalLHS_Await QUESTDOT TemplateLiteral_Await
| OptionalExpression_Await LBRACK Expression_In_Await RBRACK
| OptionalExpression_Await DOT IdentifierNameRef
| OptionalExpression_Await DOT ClassPrivateRef
| OptionalExpression_Await Arguments_Await
| OptionalExpression_Await /*.noLineBreak*/ EXCL
| OptionalExpression_Await TemplateLiteral_Await
;

OptionalExpression_Await_NoLet :
  OptionalLHS_Await_NoLet QUESTDOT LBRACK Expression_In_Await RBRACK
| OptionalLHS_Await_NoLet QUESTDOT IdentifierNameRef
| OptionalLHS_Await_NoLet QUESTDOT ClassPrivateRef
| OptionalLHS_Await_NoLet QUESTDOT Arguments_Await
| OptionalLHS_Await_NoLet QUESTDOT TemplateLiteral_Await
| OptionalExpression_Await_NoLet LBRACK Expression_In_Await RBRACK
| OptionalExpression_Await_NoLet DOT IdentifierNameRef
| OptionalExpression_Await_NoLet DOT ClassPrivateRef
| OptionalExpression_Await_NoLet Arguments_Await
| OptionalExpression_Await_NoLet /*.noLineBreak*/ EXCL
| OptionalExpression_Await_NoLet TemplateLiteral_Await
;

OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  OptionalLHS_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT LBRACK Expression_In_Await RBRACK
| OptionalLHS_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT IdentifierNameRef
| OptionalLHS_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT ClassPrivateRef
| OptionalLHS_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT Arguments_Await
| OptionalLHS_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT TemplateLiteral_Await
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In_Await RBRACK
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Await
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Await
;

OptionalExpression_Await_NoObjLiteral :
  OptionalLHS_Await_NoObjLiteral QUESTDOT LBRACK Expression_In_Await RBRACK
| OptionalLHS_Await_NoObjLiteral QUESTDOT IdentifierNameRef
| OptionalLHS_Await_NoObjLiteral QUESTDOT ClassPrivateRef
| OptionalLHS_Await_NoObjLiteral QUESTDOT Arguments_Await
| OptionalLHS_Await_NoObjLiteral QUESTDOT TemplateLiteral_Await
| OptionalExpression_Await_NoObjLiteral LBRACK Expression_In_Await RBRACK
| OptionalExpression_Await_NoObjLiteral DOT IdentifierNameRef
| OptionalExpression_Await_NoObjLiteral DOT ClassPrivateRef
| OptionalExpression_Await_NoObjLiteral Arguments_Await
| OptionalExpression_Await_NoObjLiteral /*.noLineBreak*/ EXCL
| OptionalExpression_Await_NoObjLiteral TemplateLiteral_Await
;

OptionalExpression_Await_StartWithLet :
  OptionalLHS_Await_StartWithLet QUESTDOT LBRACK Expression_In_Await RBRACK
| OptionalLHS_Await_StartWithLet QUESTDOT IdentifierNameRef
| OptionalLHS_Await_StartWithLet QUESTDOT ClassPrivateRef
| OptionalLHS_Await_StartWithLet QUESTDOT Arguments_Await
| OptionalLHS_Await_StartWithLet QUESTDOT TemplateLiteral_Await
| OptionalExpression_Await_StartWithLet LBRACK Expression_In_Await RBRACK
| OptionalExpression_Await_StartWithLet DOT IdentifierNameRef
| OptionalExpression_Await_StartWithLet DOT ClassPrivateRef
| OptionalExpression_Await_StartWithLet Arguments_Await
| OptionalExpression_Await_StartWithLet /*.noLineBreak*/ EXCL
| OptionalExpression_Await_StartWithLet TemplateLiteral_Await
;

OptionalExpression_NoFuncClass :
  OptionalLHS_NoFuncClass QUESTDOT LBRACK Expression_In RBRACK
| OptionalLHS_NoFuncClass QUESTDOT IdentifierNameRef
| OptionalLHS_NoFuncClass QUESTDOT ClassPrivateRef
| OptionalLHS_NoFuncClass QUESTDOT Arguments
| OptionalLHS_NoFuncClass QUESTDOT TemplateLiteral
| OptionalExpression_NoFuncClass LBRACK Expression_In RBRACK
| OptionalExpression_NoFuncClass DOT IdentifierNameRef
| OptionalExpression_NoFuncClass DOT ClassPrivateRef
| OptionalExpression_NoFuncClass Arguments
| OptionalExpression_NoFuncClass /*.noLineBreak*/ EXCL
| OptionalExpression_NoFuncClass TemplateLiteral
;

OptionalExpression_NoLet :
  OptionalLHS_NoLet QUESTDOT LBRACK Expression_In RBRACK
| OptionalLHS_NoLet QUESTDOT IdentifierNameRef
| OptionalLHS_NoLet QUESTDOT ClassPrivateRef
| OptionalLHS_NoLet QUESTDOT Arguments
| OptionalLHS_NoLet QUESTDOT TemplateLiteral
| OptionalExpression_NoLet LBRACK Expression_In RBRACK
| OptionalExpression_NoLet DOT IdentifierNameRef
| OptionalExpression_NoLet DOT ClassPrivateRef
| OptionalExpression_NoLet Arguments
| OptionalExpression_NoLet /*.noLineBreak*/ EXCL
| OptionalExpression_NoLet TemplateLiteral
;

OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass :
  OptionalLHS_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT LBRACK Expression_In RBRACK
| OptionalLHS_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT IdentifierNameRef
| OptionalLHS_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT ClassPrivateRef
| OptionalLHS_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT Arguments
| OptionalLHS_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT TemplateLiteral
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In RBRACK
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass Arguments
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral
;

OptionalExpression_NoObjLiteral :
  OptionalLHS_NoObjLiteral QUESTDOT LBRACK Expression_In RBRACK
| OptionalLHS_NoObjLiteral QUESTDOT IdentifierNameRef
| OptionalLHS_NoObjLiteral QUESTDOT ClassPrivateRef
| OptionalLHS_NoObjLiteral QUESTDOT Arguments
| OptionalLHS_NoObjLiteral QUESTDOT TemplateLiteral
| OptionalExpression_NoObjLiteral LBRACK Expression_In RBRACK
| OptionalExpression_NoObjLiteral DOT IdentifierNameRef
| OptionalExpression_NoObjLiteral DOT ClassPrivateRef
| OptionalExpression_NoObjLiteral Arguments
| OptionalExpression_NoObjLiteral /*.noLineBreak*/ EXCL
| OptionalExpression_NoObjLiteral TemplateLiteral
;

OptionalExpression_StartWithLet :
  OptionalLHS_StartWithLet QUESTDOT LBRACK Expression_In RBRACK
| OptionalLHS_StartWithLet QUESTDOT IdentifierNameRef
| OptionalLHS_StartWithLet QUESTDOT ClassPrivateRef
| OptionalLHS_StartWithLet QUESTDOT Arguments
| OptionalLHS_StartWithLet QUESTDOT TemplateLiteral
| OptionalExpression_StartWithLet LBRACK Expression_In RBRACK
| OptionalExpression_StartWithLet DOT IdentifierNameRef
| OptionalExpression_StartWithLet DOT ClassPrivateRef
| OptionalExpression_StartWithLet Arguments
| OptionalExpression_StartWithLet /*.noLineBreak*/ EXCL
| OptionalExpression_StartWithLet TemplateLiteral
;

OptionalExpression_Yield :
  OptionalLHS_Yield QUESTDOT LBRACK Expression_In_Yield RBRACK
| OptionalLHS_Yield QUESTDOT IdentifierNameRef
| OptionalLHS_Yield QUESTDOT ClassPrivateRef
| OptionalLHS_Yield QUESTDOT Arguments_Yield
| OptionalLHS_Yield QUESTDOT TemplateLiteral_Yield
| OptionalExpression_Yield LBRACK Expression_In_Yield RBRACK
| OptionalExpression_Yield DOT IdentifierNameRef
| OptionalExpression_Yield DOT ClassPrivateRef
| OptionalExpression_Yield Arguments_Yield
| OptionalExpression_Yield /*.noLineBreak*/ EXCL
| OptionalExpression_Yield TemplateLiteral_Yield
;

OptionalExpression_Yield_Await :
  OptionalLHS_Yield_Await QUESTDOT LBRACK Expression_In_Yield_Await RBRACK
| OptionalLHS_Yield_Await QUESTDOT IdentifierNameRef
| OptionalLHS_Yield_Await QUESTDOT ClassPrivateRef
| OptionalLHS_Yield_Await QUESTDOT Arguments_Yield_Await
| OptionalLHS_Yield_Await QUESTDOT TemplateLiteral_Yield_Await
| OptionalExpression_Yield_Await LBRACK Expression_In_Yield_Await RBRACK
| OptionalExpression_Yield_Await DOT IdentifierNameRef
| OptionalExpression_Yield_Await DOT ClassPrivateRef
| OptionalExpression_Yield_Await Arguments_Yield_Await
| OptionalExpression_Yield_Await /*.noLineBreak*/ EXCL
| OptionalExpression_Yield_Await TemplateLiteral_Yield_Await
;

OptionalExpression_Yield_Await_NoLet :
  OptionalLHS_Yield_Await_NoLet QUESTDOT LBRACK Expression_In_Yield_Await RBRACK
| OptionalLHS_Yield_Await_NoLet QUESTDOT IdentifierNameRef
| OptionalLHS_Yield_Await_NoLet QUESTDOT ClassPrivateRef
| OptionalLHS_Yield_Await_NoLet QUESTDOT Arguments_Yield_Await
| OptionalLHS_Yield_Await_NoLet QUESTDOT TemplateLiteral_Yield_Await
| OptionalExpression_Yield_Await_NoLet LBRACK Expression_In_Yield_Await RBRACK
| OptionalExpression_Yield_Await_NoLet DOT IdentifierNameRef
| OptionalExpression_Yield_Await_NoLet DOT ClassPrivateRef
| OptionalExpression_Yield_Await_NoLet Arguments_Yield_Await
| OptionalExpression_Yield_Await_NoLet /*.noLineBreak*/ EXCL
| OptionalExpression_Yield_Await_NoLet TemplateLiteral_Yield_Await
;

OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  OptionalLHS_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT LBRACK Expression_In_Yield_Await RBRACK
| OptionalLHS_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT IdentifierNameRef
| OptionalLHS_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT ClassPrivateRef
| OptionalLHS_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT Arguments_Yield_Await
| OptionalLHS_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT TemplateLiteral_Yield_Await
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In_Yield_Await RBRACK
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Yield_Await
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield_Await
;

OptionalExpression_Yield_Await_StartWithLet :
  OptionalLHS_Yield_Await_StartWithLet QUESTDOT LBRACK Expression_In_Yield_Await RBRACK
| OptionalLHS_Yield_Await_StartWithLet QUESTDOT IdentifierNameRef
| OptionalLHS_Yield_Await_StartWithLet QUESTDOT ClassPrivateRef
| OptionalLHS_Yield_Await_StartWithLet QUESTDOT Arguments_Yield_Await
| OptionalLHS_Yield_Await_StartWithLet QUESTDOT TemplateLiteral_Yield_Await
| OptionalExpression_Yield_Await_StartWithLet LBRACK Expression_In_Yield_Await RBRACK
| OptionalExpression_Yield_Await_StartWithLet DOT IdentifierNameRef
| OptionalExpression_Yield_Await_StartWithLet DOT ClassPrivateRef
| OptionalExpression_Yield_Await_StartWithLet Arguments_Yield_Await
| OptionalExpression_Yield_Await_StartWithLet /*.noLineBreak*/ EXCL
| OptionalExpression_Yield_Await_StartWithLet TemplateLiteral_Yield_Await
;

OptionalExpression_Yield_NoLet :
  OptionalLHS_Yield_NoLet QUESTDOT LBRACK Expression_In_Yield RBRACK
| OptionalLHS_Yield_NoLet QUESTDOT IdentifierNameRef
| OptionalLHS_Yield_NoLet QUESTDOT ClassPrivateRef
| OptionalLHS_Yield_NoLet QUESTDOT Arguments_Yield
| OptionalLHS_Yield_NoLet QUESTDOT TemplateLiteral_Yield
| OptionalExpression_Yield_NoLet LBRACK Expression_In_Yield RBRACK
| OptionalExpression_Yield_NoLet DOT IdentifierNameRef
| OptionalExpression_Yield_NoLet DOT ClassPrivateRef
| OptionalExpression_Yield_NoLet Arguments_Yield
| OptionalExpression_Yield_NoLet /*.noLineBreak*/ EXCL
| OptionalExpression_Yield_NoLet TemplateLiteral_Yield
;

OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  OptionalLHS_Yield_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT LBRACK Expression_In_Yield RBRACK
| OptionalLHS_Yield_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT IdentifierNameRef
| OptionalLHS_Yield_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT ClassPrivateRef
| OptionalLHS_Yield_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT Arguments_Yield
| OptionalLHS_Yield_NoLetSq_NoObjLiteral_NoFuncClass QUESTDOT TemplateLiteral_Yield
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass LBRACK Expression_In_Yield RBRACK
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT IdentifierNameRef
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass DOT ClassPrivateRef
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass Arguments_Yield
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ EXCL
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass TemplateLiteral_Yield
;

OptionalExpression_Yield_StartWithLet :
  OptionalLHS_Yield_StartWithLet QUESTDOT LBRACK Expression_In_Yield RBRACK
| OptionalLHS_Yield_StartWithLet QUESTDOT IdentifierNameRef
| OptionalLHS_Yield_StartWithLet QUESTDOT ClassPrivateRef
| OptionalLHS_Yield_StartWithLet QUESTDOT Arguments_Yield
| OptionalLHS_Yield_StartWithLet QUESTDOT TemplateLiteral_Yield
| OptionalExpression_Yield_StartWithLet LBRACK Expression_In_Yield RBRACK
| OptionalExpression_Yield_StartWithLet DOT IdentifierNameRef
| OptionalExpression_Yield_StartWithLet DOT ClassPrivateRef
| OptionalExpression_Yield_StartWithLet Arguments_Yield
| OptionalExpression_Yield_StartWithLet /*.noLineBreak*/ EXCL
| OptionalExpression_Yield_StartWithLet TemplateLiteral_Yield
;

LeftHandSideExpression :
  NewExpression
| CallExpression lookahead_notStartOfParametrizedCall
| OptionalExpression lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Await :
  NewExpression_Await
| CallExpression_Await lookahead_notStartOfParametrizedCall
| OptionalExpression_Await lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Await_NoAsync_NoLet :
  NewExpression_Await_NoAsync_NoLet
| CallExpression_Await_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Await_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Await_NoLet :
  NewExpression_Await_NoLet
| CallExpression_Await_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Await_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  NewExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| OptionalExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Await_NoObjLiteral :
  NewExpression_Await_NoObjLiteral
| CallExpression_Await_NoObjLiteral lookahead_notStartOfParametrizedCall
| OptionalExpression_Await_NoObjLiteral lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Await_StartWithLet :
  NewExpression_Await_StartWithLet
| CallExpression_Await_StartWithLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Await_StartWithLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_NoAsync_NoLet :
  NewExpression_NoAsync_NoLet
| CallExpression_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_NoFuncClass :
  NewExpression_NoFuncClass
| CallExpression_NoFuncClass lookahead_notStartOfParametrizedCall
| OptionalExpression_NoFuncClass lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_NoLet :
  NewExpression_NoLet
| CallExpression_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_NoLetSq_NoObjLiteral_NoFuncClass :
  NewExpression_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| OptionalExpression_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_NoObjLiteral :
  NewExpression_NoObjLiteral
| CallExpression_NoObjLiteral lookahead_notStartOfParametrizedCall
| OptionalExpression_NoObjLiteral lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_StartWithLet :
  NewExpression_StartWithLet
| CallExpression_StartWithLet lookahead_notStartOfParametrizedCall
| OptionalExpression_StartWithLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield :
  NewExpression_Yield
| CallExpression_Yield lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_Await :
  NewExpression_Yield_Await
| CallExpression_Yield_Await lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_Await lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_Await_NoAsync_NoLet :
  NewExpression_Yield_Await_NoAsync_NoLet
| CallExpression_Yield_Await_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_Await_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_Await_NoLet :
  NewExpression_Yield_Await_NoLet
| CallExpression_Yield_Await_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_Await_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  NewExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_Await_StartWithLet :
  NewExpression_Yield_Await_StartWithLet
| CallExpression_Yield_Await_StartWithLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_Await_StartWithLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_NoAsync_NoLet :
  NewExpression_Yield_NoAsync_NoLet
| CallExpression_Yield_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_NoLet :
  NewExpression_Yield_NoLet
| CallExpression_Yield_NoLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_NoLet lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  NewExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass
| CallExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass lookahead_notStartOfParametrizedCall
;

LeftHandSideExpression_Yield_StartWithLet :
  NewExpression_Yield_StartWithLet
| CallExpression_Yield_StartWithLet lookahead_notStartOfParametrizedCall
| OptionalExpression_Yield_StartWithLet lookahead_notStartOfParametrizedCall
;

UpdateExpression :
  LeftHandSideExpression
| LeftHandSideExpression /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression
| MINUSMINUS UnaryExpression
;

UpdateExpression_Await :
  LeftHandSideExpression_Await
| LeftHandSideExpression_Await /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Await /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Await
| MINUSMINUS UnaryExpression_Await
;

UpdateExpression_Await_NoLet :
  LeftHandSideExpression_Await_NoLet
| LeftHandSideExpression_Await_NoLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Await_NoLet /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Await
| MINUSMINUS UnaryExpression_Await
;

UpdateExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  LeftHandSideExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass
| LeftHandSideExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Await
| MINUSMINUS UnaryExpression_Await
;

UpdateExpression_Await_NoObjLiteral :
  LeftHandSideExpression_Await_NoObjLiteral
| LeftHandSideExpression_Await_NoObjLiteral /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Await_NoObjLiteral /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Await
| MINUSMINUS UnaryExpression_Await
;

UpdateExpression_Await_StartWithLet :
  LeftHandSideExpression_Await_StartWithLet
| LeftHandSideExpression_Await_StartWithLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Await_StartWithLet /*.noLineBreak*/ MINUSMINUS
;

UpdateExpression_NoFuncClass :
  LeftHandSideExpression_NoFuncClass
| LeftHandSideExpression_NoFuncClass /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_NoFuncClass /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression
| MINUSMINUS UnaryExpression
;

UpdateExpression_NoLet :
  LeftHandSideExpression_NoLet
| LeftHandSideExpression_NoLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_NoLet /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression
| MINUSMINUS UnaryExpression
;

UpdateExpression_NoLetSq_NoObjLiteral_NoFuncClass :
  LeftHandSideExpression_NoLetSq_NoObjLiteral_NoFuncClass
| LeftHandSideExpression_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression
| MINUSMINUS UnaryExpression
;

UpdateExpression_NoObjLiteral :
  LeftHandSideExpression_NoObjLiteral
| LeftHandSideExpression_NoObjLiteral /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_NoObjLiteral /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression
| MINUSMINUS UnaryExpression
;

UpdateExpression_StartWithLet :
  LeftHandSideExpression_StartWithLet
| LeftHandSideExpression_StartWithLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_StartWithLet /*.noLineBreak*/ MINUSMINUS
;

UpdateExpression_Yield :
  LeftHandSideExpression_Yield
| LeftHandSideExpression_Yield /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Yield
| MINUSMINUS UnaryExpression_Yield
;

UpdateExpression_Yield_Await :
  LeftHandSideExpression_Yield_Await
| LeftHandSideExpression_Yield_Await /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield_Await /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Yield_Await
| MINUSMINUS UnaryExpression_Yield_Await
;

UpdateExpression_Yield_Await_NoLet :
  LeftHandSideExpression_Yield_Await_NoLet
| LeftHandSideExpression_Yield_Await_NoLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield_Await_NoLet /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Yield_Await
| MINUSMINUS UnaryExpression_Yield_Await
;

UpdateExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  LeftHandSideExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass
| LeftHandSideExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Yield_Await
| MINUSMINUS UnaryExpression_Yield_Await
;

UpdateExpression_Yield_Await_StartWithLet :
  LeftHandSideExpression_Yield_Await_StartWithLet
| LeftHandSideExpression_Yield_Await_StartWithLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield_Await_StartWithLet /*.noLineBreak*/ MINUSMINUS
;

UpdateExpression_Yield_NoLet :
  LeftHandSideExpression_Yield_NoLet
| LeftHandSideExpression_Yield_NoLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield_NoLet /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Yield
| MINUSMINUS UnaryExpression_Yield
;

UpdateExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  LeftHandSideExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass
| LeftHandSideExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass /*.noLineBreak*/ MINUSMINUS
| PLUSPLUS UnaryExpression_Yield
| MINUSMINUS UnaryExpression_Yield
;

UpdateExpression_Yield_StartWithLet :
  LeftHandSideExpression_Yield_StartWithLet
| LeftHandSideExpression_Yield_StartWithLet /*.noLineBreak*/ PLUSPLUS
| LeftHandSideExpression_Yield_StartWithLet /*.noLineBreak*/ MINUSMINUS
;

UnaryExpression :
  UpdateExpression
| DELETE UnaryExpression
| VOID UnaryExpression
| TYPEOF UnaryExpression
| PLUS UnaryExpression
| MINUS UnaryExpression
| TILDE UnaryExpression
| EXCL UnaryExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression
;

UnaryExpression_Await :
  UpdateExpression_Await
| DELETE UnaryExpression_Await
| VOID UnaryExpression_Await
| TYPEOF UnaryExpression_Await
| PLUS UnaryExpression_Await
| MINUS UnaryExpression_Await
| TILDE UnaryExpression_Await
| EXCL UnaryExpression_Await
| AwaitExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Await
;

UnaryExpression_Await_NoLet :
  UpdateExpression_Await_NoLet
| DELETE UnaryExpression_Await
| VOID UnaryExpression_Await
| TYPEOF UnaryExpression_Await
| PLUS UnaryExpression_Await
| MINUS UnaryExpression_Await
| TILDE UnaryExpression_Await
| EXCL UnaryExpression_Await
| AwaitExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Await
;

UnaryExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  UpdateExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass
| DELETE UnaryExpression_Await
| VOID UnaryExpression_Await
| TYPEOF UnaryExpression_Await
| PLUS UnaryExpression_Await
| MINUS UnaryExpression_Await
| TILDE UnaryExpression_Await
| EXCL UnaryExpression_Await
| AwaitExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Await
;

UnaryExpression_Await_NoObjLiteral :
  UpdateExpression_Await_NoObjLiteral
| DELETE UnaryExpression_Await
| VOID UnaryExpression_Await
| TYPEOF UnaryExpression_Await
| PLUS UnaryExpression_Await
| MINUS UnaryExpression_Await
| TILDE UnaryExpression_Await
| EXCL UnaryExpression_Await
| AwaitExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Await
;

UnaryExpression_Await_StartWithLet :
  UpdateExpression_Await_StartWithLet
;

UnaryExpression_NoFuncClass :
  UpdateExpression_NoFuncClass
| DELETE UnaryExpression
| VOID UnaryExpression
| TYPEOF UnaryExpression
| PLUS UnaryExpression
| MINUS UnaryExpression
| TILDE UnaryExpression
| EXCL UnaryExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression
;

UnaryExpression_NoLet :
  UpdateExpression_NoLet
| DELETE UnaryExpression
| VOID UnaryExpression
| TYPEOF UnaryExpression
| PLUS UnaryExpression
| MINUS UnaryExpression
| TILDE UnaryExpression
| EXCL UnaryExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression
;

UnaryExpression_NoLetSq_NoObjLiteral_NoFuncClass :
  UpdateExpression_NoLetSq_NoObjLiteral_NoFuncClass
| DELETE UnaryExpression
| VOID UnaryExpression
| TYPEOF UnaryExpression
| PLUS UnaryExpression
| MINUS UnaryExpression
| TILDE UnaryExpression
| EXCL UnaryExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression
;

UnaryExpression_NoObjLiteral :
  UpdateExpression_NoObjLiteral
| DELETE UnaryExpression
| VOID UnaryExpression
| TYPEOF UnaryExpression
| PLUS UnaryExpression
| MINUS UnaryExpression
| TILDE UnaryExpression
| EXCL UnaryExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression
;

UnaryExpression_StartWithLet :
  UpdateExpression_StartWithLet
;

UnaryExpression_Yield :
  UpdateExpression_Yield
| DELETE UnaryExpression_Yield
| VOID UnaryExpression_Yield
| TYPEOF UnaryExpression_Yield
| PLUS UnaryExpression_Yield
| MINUS UnaryExpression_Yield
| TILDE UnaryExpression_Yield
| EXCL UnaryExpression_Yield
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Yield
;

UnaryExpression_Yield_Await :
  UpdateExpression_Yield_Await
| DELETE UnaryExpression_Yield_Await
| VOID UnaryExpression_Yield_Await
| TYPEOF UnaryExpression_Yield_Await
| PLUS UnaryExpression_Yield_Await
| MINUS UnaryExpression_Yield_Await
| TILDE UnaryExpression_Yield_Await
| EXCL UnaryExpression_Yield_Await
| AwaitExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Yield_Await
;

UnaryExpression_Yield_Await_NoLet :
  UpdateExpression_Yield_Await_NoLet
| DELETE UnaryExpression_Yield_Await
| VOID UnaryExpression_Yield_Await
| TYPEOF UnaryExpression_Yield_Await
| PLUS UnaryExpression_Yield_Await
| MINUS UnaryExpression_Yield_Await
| TILDE UnaryExpression_Yield_Await
| EXCL UnaryExpression_Yield_Await
| AwaitExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Yield_Await
;

UnaryExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass :
  UpdateExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass
| DELETE UnaryExpression_Yield_Await
| VOID UnaryExpression_Yield_Await
| TYPEOF UnaryExpression_Yield_Await
| PLUS UnaryExpression_Yield_Await
| MINUS UnaryExpression_Yield_Await
| TILDE UnaryExpression_Yield_Await
| EXCL UnaryExpression_Yield_Await
| AwaitExpression
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Yield_Await
;

UnaryExpression_Yield_Await_StartWithLet :
  UpdateExpression_Yield_Await_StartWithLet
;

UnaryExpression_Yield_NoLet :
  UpdateExpression_Yield_NoLet
| DELETE UnaryExpression_Yield
| VOID UnaryExpression_Yield
| TYPEOF UnaryExpression_Yield
| PLUS UnaryExpression_Yield
| MINUS UnaryExpression_Yield
| TILDE UnaryExpression_Yield
| EXCL UnaryExpression_Yield
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Yield
;

UnaryExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass :
  UpdateExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass
| DELETE UnaryExpression_Yield
| VOID UnaryExpression_Yield
| TYPEOF UnaryExpression_Yield
| PLUS UnaryExpression_Yield
| MINUS UnaryExpression_Yield
| TILDE UnaryExpression_Yield
| EXCL UnaryExpression_Yield
| lookahead_notStartOfArrowFunction LT Type GT UnaryExpression_Yield
;

UnaryExpression_Yield_StartWithLet :
  UpdateExpression_Yield_StartWithLet
;

BinaryExpression :
  UnaryExpression
| BinaryExpression PLUS BinaryExpression
| BinaryExpression MINUS BinaryExpression
| BinaryExpression LTLT BinaryExpression
| BinaryExpression GTGT BinaryExpression
| BinaryExpression GTGTGT BinaryExpression
| BinaryExpression MULT BinaryExpression
| BinaryExpression DIV BinaryExpression
| BinaryExpression REM BinaryExpression
| UpdateExpression MULTMULT BinaryExpression
| BinaryExpression LT BinaryExpression
| BinaryExpression GT BinaryExpression
| BinaryExpression LTASSIGN BinaryExpression
| BinaryExpression GTASSIGN BinaryExpression
| BinaryExpression INSTANCEOF BinaryExpression
| BinaryExpression /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression /*.noLineBreak*/ AS CONST
| BinaryExpression SATISFIES Type_NoQuest
| BinaryExpression ASSIGNASSIGN BinaryExpression
| BinaryExpression EXCLASSIGN BinaryExpression
| BinaryExpression ASSIGNASSIGNASSIGN BinaryExpression
| BinaryExpression EXCLASSIGNASSIGN BinaryExpression
| BinaryExpression AND BinaryExpression
| BinaryExpression XOR BinaryExpression
| BinaryExpression OR BinaryExpression
| BinaryExpression ANDAND BinaryExpression
| BinaryExpression OROR BinaryExpression
| BinaryExpression QUESTQUEST BinaryExpression
;

BinaryExpression_Await :
  UnaryExpression_Await
| BinaryExpression_Await PLUS BinaryExpression_Await
| BinaryExpression_Await MINUS BinaryExpression_Await
| BinaryExpression_Await LTLT BinaryExpression_Await
| BinaryExpression_Await GTGT BinaryExpression_Await
| BinaryExpression_Await GTGTGT BinaryExpression_Await
| BinaryExpression_Await MULT BinaryExpression_Await
| BinaryExpression_Await DIV BinaryExpression_Await
| BinaryExpression_Await REM BinaryExpression_Await
| UpdateExpression_Await MULTMULT BinaryExpression_Await
| BinaryExpression_Await LT BinaryExpression_Await
| BinaryExpression_Await GT BinaryExpression_Await
| BinaryExpression_Await LTASSIGN BinaryExpression_Await
| BinaryExpression_Await GTASSIGN BinaryExpression_Await
| BinaryExpression_Await INSTANCEOF BinaryExpression_Await
| BinaryExpression_Await /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_Await /*.noLineBreak*/ AS CONST
| BinaryExpression_Await SATISFIES Type_NoQuest
| BinaryExpression_Await ASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await EXCLASSIGN BinaryExpression_Await
| BinaryExpression_Await ASSIGNASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await EXCLASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await AND BinaryExpression_Await
| BinaryExpression_Await XOR BinaryExpression_Await
| BinaryExpression_Await OR BinaryExpression_Await
| BinaryExpression_Await ANDAND BinaryExpression_Await
| BinaryExpression_Await OROR BinaryExpression_Await
| BinaryExpression_Await QUESTQUEST BinaryExpression_Await
;

BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet :
  UnaryExpression_Await_StartWithLet
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet PLUS BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet MINUS BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet LTLT BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet GTGT BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet GTGTGT BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet MULT BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet DIV BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet REM BinaryExpression_Await
| UpdateExpression_Await_StartWithLet MULTMULT BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet LT BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet GT BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet LTASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet GTASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet INSTANCEOF BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet EXCLASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet EXCLASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet AND BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet XOR BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet OR BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet ANDAND BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet OROR BinaryExpression_Await
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet QUESTQUEST BinaryExpression_Await
;

BinaryExpression_Await_NoLet :
  UnaryExpression_Await_NoLet
| BinaryExpression_Await_NoLet PLUS BinaryExpression_Await
| BinaryExpression_Await_NoLet MINUS BinaryExpression_Await
| BinaryExpression_Await_NoLet LTLT BinaryExpression_Await
| BinaryExpression_Await_NoLet GTGT BinaryExpression_Await
| BinaryExpression_Await_NoLet GTGTGT BinaryExpression_Await
| BinaryExpression_Await_NoLet MULT BinaryExpression_Await
| BinaryExpression_Await_NoLet DIV BinaryExpression_Await
| BinaryExpression_Await_NoLet REM BinaryExpression_Await
| UpdateExpression_Await_NoLet MULTMULT BinaryExpression_Await
| BinaryExpression_Await_NoLet LT BinaryExpression_Await
| BinaryExpression_Await_NoLet GT BinaryExpression_Await
| BinaryExpression_Await_NoLet LTASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoLet GTASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoLet INSTANCEOF BinaryExpression_Await
| BinaryExpression_Await_NoLet /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_Await_NoLet /*.noLineBreak*/ AS CONST
| BinaryExpression_Await_NoLet SATISFIES Type_NoQuest
| BinaryExpression_Await_NoLet ASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoLet EXCLASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoLet ASSIGNASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoLet EXCLASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoLet AND BinaryExpression_Await
| BinaryExpression_Await_NoLet XOR BinaryExpression_Await
| BinaryExpression_Await_NoLet OR BinaryExpression_Await
| BinaryExpression_Await_NoLet ANDAND BinaryExpression_Await
| BinaryExpression_Await_NoLet OROR BinaryExpression_Await
| BinaryExpression_Await_NoLet QUESTQUEST BinaryExpression_Await
;

BinaryExpression_Await_NoObjLiteral :
  UnaryExpression_Await_NoObjLiteral
| BinaryExpression_Await_NoObjLiteral PLUS BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral MINUS BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral LTLT BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral GTGT BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral GTGTGT BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral MULT BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral DIV BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral REM BinaryExpression_Await
| UpdateExpression_Await_NoObjLiteral MULTMULT BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral LT BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral GT BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral LTASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral GTASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral INSTANCEOF BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_Await_NoObjLiteral /*.noLineBreak*/ AS CONST
| BinaryExpression_Await_NoObjLiteral SATISFIES Type_NoQuest
| BinaryExpression_Await_NoObjLiteral ASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral EXCLASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral ASSIGNASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral EXCLASSIGNASSIGN BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral AND BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral XOR BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral OR BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral ANDAND BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral OROR BinaryExpression_Await
| BinaryExpression_Await_NoObjLiteral QUESTQUEST BinaryExpression_Await
;

BinaryExpression_In :
  UnaryExpression
| BinaryExpression_In PLUS BinaryExpression_In
| BinaryExpression_In MINUS BinaryExpression_In
| BinaryExpression_In LTLT BinaryExpression_In
| BinaryExpression_In GTGT BinaryExpression_In
| BinaryExpression_In GTGTGT BinaryExpression_In
| BinaryExpression_In MULT BinaryExpression_In
| BinaryExpression_In DIV BinaryExpression_In
| BinaryExpression_In REM BinaryExpression_In
| UpdateExpression MULTMULT BinaryExpression_In
| BinaryExpression_In LT BinaryExpression_In
| BinaryExpression_In GT BinaryExpression_In
| BinaryExpression_In LTASSIGN BinaryExpression_In
| BinaryExpression_In GTASSIGN BinaryExpression_In
| BinaryExpression_In INSTANCEOF BinaryExpression_In
| BinaryExpression_In IN BinaryExpression_In
| BinaryExpression_In /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_In /*.noLineBreak*/ AS CONST
| BinaryExpression_In SATISFIES Type_NoQuest
| BinaryExpression_In ASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In EXCLASSIGN BinaryExpression_In
| BinaryExpression_In ASSIGNASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In EXCLASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In AND BinaryExpression_In
| BinaryExpression_In XOR BinaryExpression_In
| BinaryExpression_In OR BinaryExpression_In
| BinaryExpression_In ANDAND BinaryExpression_In
| BinaryExpression_In OROR BinaryExpression_In
| BinaryExpression_In QUESTQUEST BinaryExpression_In
;

BinaryExpression_In_Await :
  UnaryExpression_Await
| BinaryExpression_In_Await PLUS BinaryExpression_In_Await
| BinaryExpression_In_Await MINUS BinaryExpression_In_Await
| BinaryExpression_In_Await LTLT BinaryExpression_In_Await
| BinaryExpression_In_Await GTGT BinaryExpression_In_Await
| BinaryExpression_In_Await GTGTGT BinaryExpression_In_Await
| BinaryExpression_In_Await MULT BinaryExpression_In_Await
| BinaryExpression_In_Await DIV BinaryExpression_In_Await
| BinaryExpression_In_Await REM BinaryExpression_In_Await
| UpdateExpression_Await MULTMULT BinaryExpression_In_Await
| BinaryExpression_In_Await LT BinaryExpression_In_Await
| BinaryExpression_In_Await GT BinaryExpression_In_Await
| BinaryExpression_In_Await LTASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await GTASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await INSTANCEOF BinaryExpression_In_Await
| BinaryExpression_In_Await IN BinaryExpression_In_Await
| BinaryExpression_In_Await /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_In_Await /*.noLineBreak*/ AS CONST
| BinaryExpression_In_Await SATISFIES Type_NoQuest
| BinaryExpression_In_Await ASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await EXCLASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await ASSIGNASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await EXCLASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await AND BinaryExpression_In_Await
| BinaryExpression_In_Await XOR BinaryExpression_In_Await
| BinaryExpression_In_Await OR BinaryExpression_In_Await
| BinaryExpression_In_Await ANDAND BinaryExpression_In_Await
| BinaryExpression_In_Await OROR BinaryExpression_In_Await
| BinaryExpression_In_Await QUESTQUEST BinaryExpression_In_Await
;

BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  UnaryExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies PLUS BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MINUS BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTLT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGTGT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MULT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies DIV BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies REM BinaryExpression_In_Await
| UpdateExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass MULTMULT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies INSTANCEOF BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies IN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies AND BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies XOR BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OR BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ANDAND BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OROR BinaryExpression_In_Await
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUESTQUEST BinaryExpression_In_Await
;

BinaryExpression_In_Await_NoObjLiteral :
  UnaryExpression_Await_NoObjLiteral
| BinaryExpression_In_Await_NoObjLiteral PLUS BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral MINUS BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral LTLT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral GTGT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral GTGTGT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral MULT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral DIV BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral REM BinaryExpression_In_Await
| UpdateExpression_Await_NoObjLiteral MULTMULT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral LT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral GT BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral LTASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral GTASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral INSTANCEOF BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral IN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_In_Await_NoObjLiteral /*.noLineBreak*/ AS CONST
| BinaryExpression_In_Await_NoObjLiteral SATISFIES Type_NoQuest
| BinaryExpression_In_Await_NoObjLiteral ASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral EXCLASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral ASSIGNASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral EXCLASSIGNASSIGN BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral AND BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral XOR BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral OR BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral ANDAND BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral OROR BinaryExpression_In_Await
| BinaryExpression_In_Await_NoObjLiteral QUESTQUEST BinaryExpression_In_Await
;

BinaryExpression_In_NoFuncClass :
  UnaryExpression_NoFuncClass
| BinaryExpression_In_NoFuncClass PLUS BinaryExpression_In
| BinaryExpression_In_NoFuncClass MINUS BinaryExpression_In
| BinaryExpression_In_NoFuncClass LTLT BinaryExpression_In
| BinaryExpression_In_NoFuncClass GTGT BinaryExpression_In
| BinaryExpression_In_NoFuncClass GTGTGT BinaryExpression_In
| BinaryExpression_In_NoFuncClass MULT BinaryExpression_In
| BinaryExpression_In_NoFuncClass DIV BinaryExpression_In
| BinaryExpression_In_NoFuncClass REM BinaryExpression_In
| UpdateExpression_NoFuncClass MULTMULT BinaryExpression_In
| BinaryExpression_In_NoFuncClass LT BinaryExpression_In
| BinaryExpression_In_NoFuncClass GT BinaryExpression_In
| BinaryExpression_In_NoFuncClass LTASSIGN BinaryExpression_In
| BinaryExpression_In_NoFuncClass GTASSIGN BinaryExpression_In
| BinaryExpression_In_NoFuncClass INSTANCEOF BinaryExpression_In
| BinaryExpression_In_NoFuncClass IN BinaryExpression_In
| BinaryExpression_In_NoFuncClass /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_In_NoFuncClass /*.noLineBreak*/ AS CONST
| BinaryExpression_In_NoFuncClass SATISFIES Type_NoQuest
| BinaryExpression_In_NoFuncClass ASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoFuncClass EXCLASSIGN BinaryExpression_In
| BinaryExpression_In_NoFuncClass ASSIGNASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoFuncClass EXCLASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoFuncClass AND BinaryExpression_In
| BinaryExpression_In_NoFuncClass XOR BinaryExpression_In
| BinaryExpression_In_NoFuncClass OR BinaryExpression_In
| BinaryExpression_In_NoFuncClass ANDAND BinaryExpression_In
| BinaryExpression_In_NoFuncClass OROR BinaryExpression_In
| BinaryExpression_In_NoFuncClass QUESTQUEST BinaryExpression_In
;

BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  UnaryExpression_NoLetSq_NoObjLiteral_NoFuncClass
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies PLUS BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MINUS BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTLT BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGT BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGTGT BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MULT BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies DIV BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies REM BinaryExpression_In
| UpdateExpression_NoLetSq_NoObjLiteral_NoFuncClass MULTMULT BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LT BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GT BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTASSIGN BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTASSIGN BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies INSTANCEOF BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies IN BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGN BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies AND BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies XOR BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OR BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ANDAND BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OROR BinaryExpression_In
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUESTQUEST BinaryExpression_In
;

BinaryExpression_In_NoObjLiteral :
  UnaryExpression_NoObjLiteral
| BinaryExpression_In_NoObjLiteral PLUS BinaryExpression_In
| BinaryExpression_In_NoObjLiteral MINUS BinaryExpression_In
| BinaryExpression_In_NoObjLiteral LTLT BinaryExpression_In
| BinaryExpression_In_NoObjLiteral GTGT BinaryExpression_In
| BinaryExpression_In_NoObjLiteral GTGTGT BinaryExpression_In
| BinaryExpression_In_NoObjLiteral MULT BinaryExpression_In
| BinaryExpression_In_NoObjLiteral DIV BinaryExpression_In
| BinaryExpression_In_NoObjLiteral REM BinaryExpression_In
| UpdateExpression_NoObjLiteral MULTMULT BinaryExpression_In
| BinaryExpression_In_NoObjLiteral LT BinaryExpression_In
| BinaryExpression_In_NoObjLiteral GT BinaryExpression_In
| BinaryExpression_In_NoObjLiteral LTASSIGN BinaryExpression_In
| BinaryExpression_In_NoObjLiteral GTASSIGN BinaryExpression_In
| BinaryExpression_In_NoObjLiteral INSTANCEOF BinaryExpression_In
| BinaryExpression_In_NoObjLiteral IN BinaryExpression_In
| BinaryExpression_In_NoObjLiteral /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_In_NoObjLiteral /*.noLineBreak*/ AS CONST
| BinaryExpression_In_NoObjLiteral SATISFIES Type_NoQuest
| BinaryExpression_In_NoObjLiteral ASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoObjLiteral EXCLASSIGN BinaryExpression_In
| BinaryExpression_In_NoObjLiteral ASSIGNASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoObjLiteral EXCLASSIGNASSIGN BinaryExpression_In
| BinaryExpression_In_NoObjLiteral AND BinaryExpression_In
| BinaryExpression_In_NoObjLiteral XOR BinaryExpression_In
| BinaryExpression_In_NoObjLiteral OR BinaryExpression_In
| BinaryExpression_In_NoObjLiteral ANDAND BinaryExpression_In
| BinaryExpression_In_NoObjLiteral OROR BinaryExpression_In
| BinaryExpression_In_NoObjLiteral QUESTQUEST BinaryExpression_In
;

BinaryExpression_In_Yield :
  UnaryExpression_Yield
| BinaryExpression_In_Yield PLUS BinaryExpression_In_Yield
| BinaryExpression_In_Yield MINUS BinaryExpression_In_Yield
| BinaryExpression_In_Yield LTLT BinaryExpression_In_Yield
| BinaryExpression_In_Yield GTGT BinaryExpression_In_Yield
| BinaryExpression_In_Yield GTGTGT BinaryExpression_In_Yield
| BinaryExpression_In_Yield MULT BinaryExpression_In_Yield
| BinaryExpression_In_Yield DIV BinaryExpression_In_Yield
| BinaryExpression_In_Yield REM BinaryExpression_In_Yield
| UpdateExpression_Yield MULTMULT BinaryExpression_In_Yield
| BinaryExpression_In_Yield LT BinaryExpression_In_Yield
| BinaryExpression_In_Yield GT BinaryExpression_In_Yield
| BinaryExpression_In_Yield LTASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield GTASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield INSTANCEOF BinaryExpression_In_Yield
| BinaryExpression_In_Yield IN BinaryExpression_In_Yield
| BinaryExpression_In_Yield /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_In_Yield /*.noLineBreak*/ AS CONST
| BinaryExpression_In_Yield SATISFIES Type_NoQuest
| BinaryExpression_In_Yield ASSIGNASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield EXCLASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield ASSIGNASSIGNASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield EXCLASSIGNASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield AND BinaryExpression_In_Yield
| BinaryExpression_In_Yield XOR BinaryExpression_In_Yield
| BinaryExpression_In_Yield OR BinaryExpression_In_Yield
| BinaryExpression_In_Yield ANDAND BinaryExpression_In_Yield
| BinaryExpression_In_Yield OROR BinaryExpression_In_Yield
| BinaryExpression_In_Yield QUESTQUEST BinaryExpression_In_Yield
;

BinaryExpression_In_Yield_Await :
  UnaryExpression_Yield_Await
| BinaryExpression_In_Yield_Await PLUS BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await MINUS BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await LTLT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await GTGT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await GTGTGT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await MULT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await DIV BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await REM BinaryExpression_In_Yield_Await
| UpdateExpression_Yield_Await MULTMULT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await LT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await GT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await LTASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await GTASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await INSTANCEOF BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await IN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_In_Yield_Await /*.noLineBreak*/ AS CONST
| BinaryExpression_In_Yield_Await SATISFIES Type_NoQuest
| BinaryExpression_In_Yield_Await ASSIGNASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await EXCLASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await ASSIGNASSIGNASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await EXCLASSIGNASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await AND BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await XOR BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await OR BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await ANDAND BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await OROR BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await QUESTQUEST BinaryExpression_In_Yield_Await
;

BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  UnaryExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies PLUS BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MINUS BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTLT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGTGT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MULT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies DIV BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies REM BinaryExpression_In_Yield_Await
| UpdateExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass MULTMULT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GT BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies INSTANCEOF BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies IN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGNASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGNASSIGN BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies AND BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies XOR BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OR BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ANDAND BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OROR BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUESTQUEST BinaryExpression_In_Yield_Await
;

BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  UnaryExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies PLUS BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MINUS BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTLT BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGT BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTGTGT BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies MULT BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies DIV BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies REM BinaryExpression_In_Yield
| UpdateExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass MULTMULT BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LT BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GT BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies LTASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies GTASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies INSTANCEOF BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies IN BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ASSIGNASSIGNASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies EXCLASSIGNASSIGN BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies AND BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies XOR BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OR BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies ANDAND BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies OROR BinaryExpression_In_Yield
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUESTQUEST BinaryExpression_In_Yield
;

BinaryExpression_NoAs_NoSatisfies_StartWithLet :
  UnaryExpression_StartWithLet
| BinaryExpression_NoAs_NoSatisfies_StartWithLet PLUS BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet MINUS BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet LTLT BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet GTGT BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet GTGTGT BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet MULT BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet DIV BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet REM BinaryExpression
| UpdateExpression_StartWithLet MULTMULT BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet LT BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet GT BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet LTASSIGN BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet GTASSIGN BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet INSTANCEOF BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGN BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet EXCLASSIGN BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGNASSIGN BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet EXCLASSIGNASSIGN BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet AND BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet XOR BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet OR BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet ANDAND BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet OROR BinaryExpression
| BinaryExpression_NoAs_NoSatisfies_StartWithLet QUESTQUEST BinaryExpression
;

BinaryExpression_NoLet :
  UnaryExpression_NoLet
| BinaryExpression_NoLet PLUS BinaryExpression
| BinaryExpression_NoLet MINUS BinaryExpression
| BinaryExpression_NoLet LTLT BinaryExpression
| BinaryExpression_NoLet GTGT BinaryExpression
| BinaryExpression_NoLet GTGTGT BinaryExpression
| BinaryExpression_NoLet MULT BinaryExpression
| BinaryExpression_NoLet DIV BinaryExpression
| BinaryExpression_NoLet REM BinaryExpression
| UpdateExpression_NoLet MULTMULT BinaryExpression
| BinaryExpression_NoLet LT BinaryExpression
| BinaryExpression_NoLet GT BinaryExpression
| BinaryExpression_NoLet LTASSIGN BinaryExpression
| BinaryExpression_NoLet GTASSIGN BinaryExpression
| BinaryExpression_NoLet INSTANCEOF BinaryExpression
| BinaryExpression_NoLet /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_NoLet /*.noLineBreak*/ AS CONST
| BinaryExpression_NoLet SATISFIES Type_NoQuest
| BinaryExpression_NoLet ASSIGNASSIGN BinaryExpression
| BinaryExpression_NoLet EXCLASSIGN BinaryExpression
| BinaryExpression_NoLet ASSIGNASSIGNASSIGN BinaryExpression
| BinaryExpression_NoLet EXCLASSIGNASSIGN BinaryExpression
| BinaryExpression_NoLet AND BinaryExpression
| BinaryExpression_NoLet XOR BinaryExpression
| BinaryExpression_NoLet OR BinaryExpression
| BinaryExpression_NoLet ANDAND BinaryExpression
| BinaryExpression_NoLet OROR BinaryExpression
| BinaryExpression_NoLet QUESTQUEST BinaryExpression
;

BinaryExpression_NoObjLiteral :
  UnaryExpression_NoObjLiteral
| BinaryExpression_NoObjLiteral PLUS BinaryExpression
| BinaryExpression_NoObjLiteral MINUS BinaryExpression
| BinaryExpression_NoObjLiteral LTLT BinaryExpression
| BinaryExpression_NoObjLiteral GTGT BinaryExpression
| BinaryExpression_NoObjLiteral GTGTGT BinaryExpression
| BinaryExpression_NoObjLiteral MULT BinaryExpression
| BinaryExpression_NoObjLiteral DIV BinaryExpression
| BinaryExpression_NoObjLiteral REM BinaryExpression
| UpdateExpression_NoObjLiteral MULTMULT BinaryExpression
| BinaryExpression_NoObjLiteral LT BinaryExpression
| BinaryExpression_NoObjLiteral GT BinaryExpression
| BinaryExpression_NoObjLiteral LTASSIGN BinaryExpression
| BinaryExpression_NoObjLiteral GTASSIGN BinaryExpression
| BinaryExpression_NoObjLiteral INSTANCEOF BinaryExpression
| BinaryExpression_NoObjLiteral /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_NoObjLiteral /*.noLineBreak*/ AS CONST
| BinaryExpression_NoObjLiteral SATISFIES Type_NoQuest
| BinaryExpression_NoObjLiteral ASSIGNASSIGN BinaryExpression
| BinaryExpression_NoObjLiteral EXCLASSIGN BinaryExpression
| BinaryExpression_NoObjLiteral ASSIGNASSIGNASSIGN BinaryExpression
| BinaryExpression_NoObjLiteral EXCLASSIGNASSIGN BinaryExpression
| BinaryExpression_NoObjLiteral AND BinaryExpression
| BinaryExpression_NoObjLiteral XOR BinaryExpression
| BinaryExpression_NoObjLiteral OR BinaryExpression
| BinaryExpression_NoObjLiteral ANDAND BinaryExpression
| BinaryExpression_NoObjLiteral OROR BinaryExpression
| BinaryExpression_NoObjLiteral QUESTQUEST BinaryExpression
;

BinaryExpression_Yield :
  UnaryExpression_Yield
| BinaryExpression_Yield PLUS BinaryExpression_Yield
| BinaryExpression_Yield MINUS BinaryExpression_Yield
| BinaryExpression_Yield LTLT BinaryExpression_Yield
| BinaryExpression_Yield GTGT BinaryExpression_Yield
| BinaryExpression_Yield GTGTGT BinaryExpression_Yield
| BinaryExpression_Yield MULT BinaryExpression_Yield
| BinaryExpression_Yield DIV BinaryExpression_Yield
| BinaryExpression_Yield REM BinaryExpression_Yield
| UpdateExpression_Yield MULTMULT BinaryExpression_Yield
| BinaryExpression_Yield LT BinaryExpression_Yield
| BinaryExpression_Yield GT BinaryExpression_Yield
| BinaryExpression_Yield LTASSIGN BinaryExpression_Yield
| BinaryExpression_Yield GTASSIGN BinaryExpression_Yield
| BinaryExpression_Yield INSTANCEOF BinaryExpression_Yield
| BinaryExpression_Yield /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_Yield /*.noLineBreak*/ AS CONST
| BinaryExpression_Yield SATISFIES Type_NoQuest
| BinaryExpression_Yield ASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield EXCLASSIGN BinaryExpression_Yield
| BinaryExpression_Yield ASSIGNASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield EXCLASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield AND BinaryExpression_Yield
| BinaryExpression_Yield XOR BinaryExpression_Yield
| BinaryExpression_Yield OR BinaryExpression_Yield
| BinaryExpression_Yield ANDAND BinaryExpression_Yield
| BinaryExpression_Yield OROR BinaryExpression_Yield
| BinaryExpression_Yield QUESTQUEST BinaryExpression_Yield
;

BinaryExpression_Yield_Await :
  UnaryExpression_Yield_Await
| BinaryExpression_Yield_Await PLUS BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await MINUS BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await LTLT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await GTGT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await GTGTGT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await MULT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await DIV BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await REM BinaryExpression_Yield_Await
| UpdateExpression_Yield_Await MULTMULT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await LT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await GT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await LTASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await GTASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await INSTANCEOF BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_Yield_Await /*.noLineBreak*/ AS CONST
| BinaryExpression_Yield_Await SATISFIES Type_NoQuest
| BinaryExpression_Yield_Await ASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await EXCLASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await ASSIGNASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await EXCLASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await AND BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await XOR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await OR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await ANDAND BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await OROR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await QUESTQUEST BinaryExpression_Yield_Await
;

BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet :
  UnaryExpression_Yield_Await_StartWithLet
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet PLUS BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet MINUS BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet LTLT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet GTGT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet GTGTGT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet MULT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet DIV BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet REM BinaryExpression_Yield_Await
| UpdateExpression_Yield_Await_StartWithLet MULTMULT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet LT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet GT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet LTASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet GTASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet INSTANCEOF BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet EXCLASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet EXCLASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet AND BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet XOR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet OR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet ANDAND BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet OROR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet QUESTQUEST BinaryExpression_Yield_Await
;

BinaryExpression_Yield_Await_NoLet :
  UnaryExpression_Yield_Await_NoLet
| BinaryExpression_Yield_Await_NoLet PLUS BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet MINUS BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet LTLT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet GTGT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet GTGTGT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet MULT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet DIV BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet REM BinaryExpression_Yield_Await
| UpdateExpression_Yield_Await_NoLet MULTMULT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet LT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet GT BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet LTASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet GTASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet INSTANCEOF BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_Yield_Await_NoLet /*.noLineBreak*/ AS CONST
| BinaryExpression_Yield_Await_NoLet SATISFIES Type_NoQuest
| BinaryExpression_Yield_Await_NoLet ASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet EXCLASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet ASSIGNASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet EXCLASSIGNASSIGN BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet AND BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet XOR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet OR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet ANDAND BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet OROR BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await_NoLet QUESTQUEST BinaryExpression_Yield_Await
;

BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet :
  UnaryExpression_Yield_StartWithLet
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet PLUS BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet MINUS BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet LTLT BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet GTGT BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet GTGTGT BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet MULT BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet DIV BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet REM BinaryExpression_Yield
| UpdateExpression_Yield_StartWithLet MULTMULT BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet LT BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet GT BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet LTASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet GTASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet INSTANCEOF BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet EXCLASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet ASSIGNASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet EXCLASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet AND BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet XOR BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet OR BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet ANDAND BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet OROR BinaryExpression_Yield
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet QUESTQUEST BinaryExpression_Yield
;

BinaryExpression_Yield_NoLet :
  UnaryExpression_Yield_NoLet
| BinaryExpression_Yield_NoLet PLUS BinaryExpression_Yield
| BinaryExpression_Yield_NoLet MINUS BinaryExpression_Yield
| BinaryExpression_Yield_NoLet LTLT BinaryExpression_Yield
| BinaryExpression_Yield_NoLet GTGT BinaryExpression_Yield
| BinaryExpression_Yield_NoLet GTGTGT BinaryExpression_Yield
| BinaryExpression_Yield_NoLet MULT BinaryExpression_Yield
| BinaryExpression_Yield_NoLet DIV BinaryExpression_Yield
| BinaryExpression_Yield_NoLet REM BinaryExpression_Yield
| UpdateExpression_Yield_NoLet MULTMULT BinaryExpression_Yield
| BinaryExpression_Yield_NoLet LT BinaryExpression_Yield
| BinaryExpression_Yield_NoLet GT BinaryExpression_Yield
| BinaryExpression_Yield_NoLet LTASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoLet GTASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoLet INSTANCEOF BinaryExpression_Yield
| BinaryExpression_Yield_NoLet /*.noLineBreak*/ AS Type_NoQuest
| BinaryExpression_Yield_NoLet /*.noLineBreak*/ AS CONST
| BinaryExpression_Yield_NoLet SATISFIES Type_NoQuest
| BinaryExpression_Yield_NoLet ASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoLet EXCLASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoLet ASSIGNASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoLet EXCLASSIGNASSIGN BinaryExpression_Yield
| BinaryExpression_Yield_NoLet AND BinaryExpression_Yield
| BinaryExpression_Yield_NoLet XOR BinaryExpression_Yield
| BinaryExpression_Yield_NoLet OR BinaryExpression_Yield
| BinaryExpression_Yield_NoLet ANDAND BinaryExpression_Yield
| BinaryExpression_Yield_NoLet OROR BinaryExpression_Yield
| BinaryExpression_Yield_NoLet QUESTQUEST BinaryExpression_Yield
;

ConditionalExpression :
  BinaryExpression
| BinaryExpression QUEST AssignmentExpression_In COLON AssignmentExpression
;

ConditionalExpression_Await :
  BinaryExpression_Await
| BinaryExpression_Await QUEST AssignmentExpression_In_Await COLON AssignmentExpression_Await
;

ConditionalExpression_Await_NoAs_NoSatisfies_StartWithLet :
  BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet
| BinaryExpression_Await_NoAs_NoSatisfies_StartWithLet QUEST AssignmentExpression_In_Await COLON AssignmentExpression_Await
;

ConditionalExpression_Await_NoLet :
  BinaryExpression_Await_NoLet
| BinaryExpression_Await_NoLet QUEST AssignmentExpression_In_Await COLON AssignmentExpression_Await
;

ConditionalExpression_Await_NoObjLiteral :
  BinaryExpression_Await_NoObjLiteral
| BinaryExpression_Await_NoObjLiteral QUEST AssignmentExpression_In_Await COLON AssignmentExpression_Await
;

ConditionalExpression_In :
  BinaryExpression_In
| BinaryExpression_In QUEST AssignmentExpression_In COLON AssignmentExpression_In
;

ConditionalExpression_In_Await :
  BinaryExpression_In_Await
| BinaryExpression_In_Await QUEST AssignmentExpression_In_Await COLON AssignmentExpression_In_Await
;

ConditionalExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| BinaryExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUEST AssignmentExpression_In_Await COLON AssignmentExpression_In_Await
;

ConditionalExpression_In_Await_NoObjLiteral :
  BinaryExpression_In_Await_NoObjLiteral
| BinaryExpression_In_Await_NoObjLiteral QUEST AssignmentExpression_In_Await COLON AssignmentExpression_In_Await
;

ConditionalExpression_In_NoFuncClass :
  BinaryExpression_In_NoFuncClass
| BinaryExpression_In_NoFuncClass QUEST AssignmentExpression_In COLON AssignmentExpression_In
;

ConditionalExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| BinaryExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUEST AssignmentExpression_In COLON AssignmentExpression_In
;

ConditionalExpression_In_NoObjLiteral :
  BinaryExpression_In_NoObjLiteral
| BinaryExpression_In_NoObjLiteral QUEST AssignmentExpression_In COLON AssignmentExpression_In
;

ConditionalExpression_In_Yield :
  BinaryExpression_In_Yield
| BinaryExpression_In_Yield QUEST AssignmentExpression_In_Yield COLON AssignmentExpression_In_Yield
;

ConditionalExpression_In_Yield_Await :
  BinaryExpression_In_Yield_Await
| BinaryExpression_In_Yield_Await QUEST AssignmentExpression_In_Yield_Await COLON AssignmentExpression_In_Yield_Await
;

ConditionalExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| BinaryExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUEST AssignmentExpression_In_Yield_Await COLON AssignmentExpression_In_Yield_Await
;

ConditionalExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| BinaryExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies QUEST AssignmentExpression_In_Yield COLON AssignmentExpression_In_Yield
;

ConditionalExpression_NoAs_NoSatisfies_StartWithLet :
  BinaryExpression_NoAs_NoSatisfies_StartWithLet
| BinaryExpression_NoAs_NoSatisfies_StartWithLet QUEST AssignmentExpression_In COLON AssignmentExpression
;

ConditionalExpression_NoLet :
  BinaryExpression_NoLet
| BinaryExpression_NoLet QUEST AssignmentExpression_In COLON AssignmentExpression
;

ConditionalExpression_NoObjLiteral :
  BinaryExpression_NoObjLiteral
| BinaryExpression_NoObjLiteral QUEST AssignmentExpression_In COLON AssignmentExpression
;

ConditionalExpression_Yield :
  BinaryExpression_Yield
| BinaryExpression_Yield QUEST AssignmentExpression_In_Yield COLON AssignmentExpression_Yield
;

ConditionalExpression_Yield_Await :
  BinaryExpression_Yield_Await
| BinaryExpression_Yield_Await QUEST AssignmentExpression_In_Yield_Await COLON AssignmentExpression_Yield_Await
;

ConditionalExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet :
  BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet
| BinaryExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet QUEST AssignmentExpression_In_Yield_Await COLON AssignmentExpression_Yield_Await
;

ConditionalExpression_Yield_Await_NoLet :
  BinaryExpression_Yield_Await_NoLet
| BinaryExpression_Yield_Await_NoLet QUEST AssignmentExpression_In_Yield_Await COLON AssignmentExpression_Yield_Await
;

ConditionalExpression_Yield_NoAs_NoSatisfies_StartWithLet :
  BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet
| BinaryExpression_Yield_NoAs_NoSatisfies_StartWithLet QUEST AssignmentExpression_In_Yield COLON AssignmentExpression_Yield
;

ConditionalExpression_Yield_NoLet :
  BinaryExpression_Yield_NoLet
| BinaryExpression_Yield_NoLet QUEST AssignmentExpression_In_Yield COLON AssignmentExpression_Yield
;

AssignmentExpression :
  ConditionalExpression
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression ASSIGN AssignmentExpression
| LeftHandSideExpression AssignmentOperator AssignmentExpression
;

AssignmentExpression_Await :
  ConditionalExpression_Await
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_Await ASSIGN AssignmentExpression_Await
| LeftHandSideExpression_Await AssignmentOperator AssignmentExpression_Await
;

AssignmentExpression_Await_NoAs_NoSatisfies_StartWithLet :
  ConditionalExpression_Await_NoAs_NoSatisfies_StartWithLet
| LeftHandSideExpression_Await_StartWithLet ASSIGN AssignmentExpression_Await
| LeftHandSideExpression_Await_StartWithLet AssignmentOperator AssignmentExpression_Await
;

AssignmentExpression_Await_NoLet :
  ConditionalExpression_Await_NoLet
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_Await_NoLet ASSIGN AssignmentExpression_Await
| LeftHandSideExpression_Await_NoLet AssignmentOperator AssignmentExpression_Await
;

AssignmentExpression_Await_NoObjLiteral :
  ConditionalExpression_Await_NoObjLiteral
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_Await_NoObjLiteral ASSIGN AssignmentExpression_Await
| LeftHandSideExpression_Await_NoObjLiteral AssignmentOperator AssignmentExpression_Await
;

AssignmentExpression_In :
  ConditionalExpression_In
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression ASSIGN AssignmentExpression_In
| LeftHandSideExpression AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_Await :
  ConditionalExpression_In_Await
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_Await ASSIGN AssignmentExpression_In_Await
| LeftHandSideExpression_Await AssignmentOperator AssignmentExpression_In_Await
;

AssignmentExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  ConditionalExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass ASSIGN AssignmentExpression_In_Await
| LeftHandSideExpression_Await_NoLetSq_NoObjLiteral_NoFuncClass AssignmentOperator AssignmentExpression_In_Await
;

AssignmentExpression_In_Await_NoObjLiteral :
  ConditionalExpression_In_Await_NoObjLiteral
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_Await_NoObjLiteral ASSIGN AssignmentExpression_In_Await
| LeftHandSideExpression_Await_NoObjLiteral AssignmentOperator AssignmentExpression_In_Await
;

AssignmentExpression_In_NoFuncClass :
  ConditionalExpression_In_NoFuncClass
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_NoFuncClass ASSIGN AssignmentExpression_In
| LeftHandSideExpression_NoFuncClass AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  ConditionalExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_NoLetSq_NoObjLiteral_NoFuncClass ASSIGN AssignmentExpression_In
| LeftHandSideExpression_NoLetSq_NoObjLiteral_NoFuncClass AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_NoObjLiteral :
  ConditionalExpression_In_NoObjLiteral
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_NoObjLiteral ASSIGN AssignmentExpression_In
| LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression_In
;

AssignmentExpression_In_Yield :
  ConditionalExpression_In_Yield
| YieldExpression_In
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_Yield ASSIGN AssignmentExpression_In_Yield
| LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_In_Yield
;

AssignmentExpression_In_Yield_Await :
  ConditionalExpression_In_Yield_Await
| YieldExpression_In_Await
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_Yield_Await ASSIGN AssignmentExpression_In_Yield_Await
| LeftHandSideExpression_Yield_Await AssignmentOperator AssignmentExpression_In_Yield_Await
;

AssignmentExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  ConditionalExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| YieldExpression_In_Await
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass ASSIGN AssignmentExpression_In_Yield_Await
| LeftHandSideExpression_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass AssignmentOperator AssignmentExpression_In_Yield_Await
;

AssignmentExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  ConditionalExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| YieldExpression_In
| ArrowFunction_In
| AsyncArrowFunction_In
| LeftHandSideExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass ASSIGN AssignmentExpression_In_Yield
| LeftHandSideExpression_Yield_NoLetSq_NoObjLiteral_NoFuncClass AssignmentOperator AssignmentExpression_In_Yield
;

AssignmentExpression_NoAs_NoSatisfies_StartWithLet :
  ConditionalExpression_NoAs_NoSatisfies_StartWithLet
| LeftHandSideExpression_StartWithLet ASSIGN AssignmentExpression
| LeftHandSideExpression_StartWithLet AssignmentOperator AssignmentExpression
;

AssignmentExpression_NoLet :
  ConditionalExpression_NoLet
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_NoLet ASSIGN AssignmentExpression
| LeftHandSideExpression_NoLet AssignmentOperator AssignmentExpression
;

AssignmentExpression_NoObjLiteral :
  ConditionalExpression_NoObjLiteral
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_NoObjLiteral ASSIGN AssignmentExpression
| LeftHandSideExpression_NoObjLiteral AssignmentOperator AssignmentExpression
;

AssignmentExpression_Yield :
  ConditionalExpression_Yield
| YieldExpression
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_Yield ASSIGN AssignmentExpression_Yield
| LeftHandSideExpression_Yield AssignmentOperator AssignmentExpression_Yield
;

AssignmentExpression_Yield_Await :
  ConditionalExpression_Yield_Await
| YieldExpression_Await
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_Yield_Await ASSIGN AssignmentExpression_Yield_Await
| LeftHandSideExpression_Yield_Await AssignmentOperator AssignmentExpression_Yield_Await
;

AssignmentExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet :
  ConditionalExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet
| LeftHandSideExpression_Yield_Await_StartWithLet ASSIGN AssignmentExpression_Yield_Await
| LeftHandSideExpression_Yield_Await_StartWithLet AssignmentOperator AssignmentExpression_Yield_Await
;

AssignmentExpression_Yield_Await_NoLet :
  ConditionalExpression_Yield_Await_NoLet
| YieldExpression_Await
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_Yield_Await_NoLet ASSIGN AssignmentExpression_Yield_Await
| LeftHandSideExpression_Yield_Await_NoLet AssignmentOperator AssignmentExpression_Yield_Await
;

AssignmentExpression_Yield_NoAs_NoSatisfies_StartWithLet :
  ConditionalExpression_Yield_NoAs_NoSatisfies_StartWithLet
| LeftHandSideExpression_Yield_StartWithLet ASSIGN AssignmentExpression_Yield
| LeftHandSideExpression_Yield_StartWithLet AssignmentOperator AssignmentExpression_Yield
;

AssignmentExpression_Yield_NoLet :
  ConditionalExpression_Yield_NoLet
| YieldExpression
| ArrowFunction
| AsyncArrowFunction
| LeftHandSideExpression_Yield_NoLet ASSIGN AssignmentExpression_Yield
| LeftHandSideExpression_Yield_NoLet AssignmentOperator AssignmentExpression_Yield
;

AssignmentOperator :
  MULTASSIGN
| DIVASSIGN
| REMASSIGN
| PLUSASSIGN
| MINUSASSIGN
| LTLTASSIGN
| GTGTASSIGN
| GTGTGTASSIGN
| ANDASSIGN
| XORASSIGN
| ORASSIGN
| MULTMULTASSIGN
| QUESTQUESTASSIGN
| ORORASSIGN
| ANDANDASSIGN
;

CommaExpression_Await_NoAs_NoSatisfies_StartWithLet :
  Expression_Await_NoAs_NoSatisfies_StartWithLet COMMA AssignmentExpression_Await
;

CommaExpression_Await_NoLet :
  Expression_Await_NoLet COMMA AssignmentExpression_Await
;

CommaExpression_In :
  Expression_In COMMA AssignmentExpression_In
;

CommaExpression_In_Await :
  Expression_In_Await COMMA AssignmentExpression_In_Await
;

CommaExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  Expression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies COMMA AssignmentExpression_In_Await
;

CommaExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  Expression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies COMMA AssignmentExpression_In
;

CommaExpression_In_Yield :
  Expression_In_Yield COMMA AssignmentExpression_In_Yield
;

CommaExpression_In_Yield_Await :
  Expression_In_Yield_Await COMMA AssignmentExpression_In_Yield_Await
;

CommaExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  Expression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies COMMA AssignmentExpression_In_Yield_Await
;

CommaExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  Expression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies COMMA AssignmentExpression_In_Yield
;

CommaExpression_NoAs_NoSatisfies_StartWithLet :
  Expression_NoAs_NoSatisfies_StartWithLet COMMA AssignmentExpression
;

CommaExpression_NoLet :
  Expression_NoLet COMMA AssignmentExpression
;

CommaExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet :
  Expression_Yield_Await_NoAs_NoSatisfies_StartWithLet COMMA AssignmentExpression_Yield_Await
;

CommaExpression_Yield_Await_NoLet :
  Expression_Yield_Await_NoLet COMMA AssignmentExpression_Yield_Await
;

CommaExpression_Yield_NoAs_NoSatisfies_StartWithLet :
  Expression_Yield_NoAs_NoSatisfies_StartWithLet COMMA AssignmentExpression_Yield
;

CommaExpression_Yield_NoLet :
  Expression_Yield_NoLet COMMA AssignmentExpression_Yield
;

Expression_Await_NoAs_NoSatisfies_StartWithLet :
  AssignmentExpression_Await_NoAs_NoSatisfies_StartWithLet
| CommaExpression_Await_NoAs_NoSatisfies_StartWithLet
;

Expression_Await_NoLet :
  AssignmentExpression_Await_NoLet
| CommaExpression_Await_NoLet
;

Expression_In :
  AssignmentExpression_In
| CommaExpression_In
;

Expression_In_Await :
  AssignmentExpression_In_Await
| CommaExpression_In_Await
;

Expression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  AssignmentExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| CommaExpression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
;

Expression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  AssignmentExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| CommaExpression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
;

Expression_In_Yield :
  AssignmentExpression_In_Yield
| CommaExpression_In_Yield
;

Expression_In_Yield_Await :
  AssignmentExpression_In_Yield_Await
| CommaExpression_In_Yield_Await
;

Expression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  AssignmentExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| CommaExpression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
;

Expression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies :
  AssignmentExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
| CommaExpression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies
;

Expression_NoAs_NoSatisfies_StartWithLet :
  AssignmentExpression_NoAs_NoSatisfies_StartWithLet
| CommaExpression_NoAs_NoSatisfies_StartWithLet
;

Expression_NoLet :
  AssignmentExpression_NoLet
| CommaExpression_NoLet
;

Expression_Yield_Await_NoAs_NoSatisfies_StartWithLet :
  AssignmentExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet
| CommaExpression_Yield_Await_NoAs_NoSatisfies_StartWithLet
;

Expression_Yield_Await_NoLet :
  AssignmentExpression_Yield_Await_NoLet
| CommaExpression_Yield_Await_NoLet
;

Expression_Yield_NoAs_NoSatisfies_StartWithLet :
  AssignmentExpression_Yield_NoAs_NoSatisfies_StartWithLet
| CommaExpression_Yield_NoAs_NoSatisfies_StartWithLet
;

Expression_Yield_NoLet :
  AssignmentExpression_Yield_NoLet
| CommaExpression_Yield_NoLet
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
| ReturnStatement
| WithStatement
| LabelledStatement
| ThrowStatement
| TryStatement
| DebuggerStatement
;

Statement_Await :
  BlockStatement_Await
| VariableStatement_Await
| EmptyStatement
| ExpressionStatement_Await
| IfStatement_Await
| BreakableStatement_Await
| ContinueStatement
| BreakStatement
| ReturnStatement_Await
| WithStatement_Await
| LabelledStatement_Await
| ThrowStatement_Await
| TryStatement_Await
| DebuggerStatement
;

Statement_Yield :
  BlockStatement_Yield
| VariableStatement_Yield
| EmptyStatement
| ExpressionStatement_Yield
| IfStatement_Yield
| BreakableStatement_Yield
| ContinueStatement
| BreakStatement
| ReturnStatement_Yield
| WithStatement_Yield
| LabelledStatement_Yield
| ThrowStatement_Yield
| TryStatement_Yield
| DebuggerStatement
;

Statement_Yield_Await :
  BlockStatement_Yield_Await
| VariableStatement_Yield_Await
| EmptyStatement
| ExpressionStatement_Yield_Await
| IfStatement_Yield_Await
| BreakableStatement_Yield_Await
| ContinueStatement
| BreakStatement
| ReturnStatement_Yield_Await
| WithStatement_Yield_Await
| LabelledStatement_Yield_Await
| ThrowStatement_Yield_Await
| TryStatement_Yield_Await
| DebuggerStatement
;

Declaration :
  HoistableDeclaration
| ClassDeclaration
| LexicalDeclaration_In
| TypeAliasDeclaration
| NamespaceDeclaration
| InterfaceDeclaration
| EnumDeclaration
| ImportAliasDeclaration
| AmbientDeclaration
;

Declaration_Await :
  HoistableDeclaration
| ClassDeclaration_Await
| LexicalDeclaration_In_Await
| TypeAliasDeclaration
| NamespaceDeclaration
| InterfaceDeclaration
| EnumDeclaration
| ImportAliasDeclaration
| AmbientDeclaration
;

Declaration_Yield :
  HoistableDeclaration
| ClassDeclaration_Yield
| LexicalDeclaration_In_Yield
| TypeAliasDeclaration
| NamespaceDeclaration
| InterfaceDeclaration
| EnumDeclaration
| ImportAliasDeclaration
| AmbientDeclaration
;

Declaration_Yield_Await :
  HoistableDeclaration
| ClassDeclaration_Yield_Await
| LexicalDeclaration_In_Yield_Await
| TypeAliasDeclaration
| NamespaceDeclaration
| InterfaceDeclaration
| EnumDeclaration
| ImportAliasDeclaration
| AmbientDeclaration
;

HoistableDeclaration :
  FunctionDeclaration
| GeneratorDeclaration
| AsyncFunctionDeclaration
| AsyncGeneratorDeclaration
;

BreakableStatement :
  IterationStatement
| SwitchStatement
;

BreakableStatement_Await :
  IterationStatement_Await
| SwitchStatement_Await
;

BreakableStatement_Yield :
  IterationStatement_Yield
| SwitchStatement_Yield
;

BreakableStatement_Yield_Await :
  IterationStatement_Yield_Await
| SwitchStatement_Yield_Await
;

BlockStatement :
  Block
;

BlockStatement_Await :
  Block_Await
;

BlockStatement_Yield :
  Block_Yield
;

BlockStatement_Yield_Await :
  Block_Yield_Await
;

Block :
  LBRACE /*.recoveryScope*/ StatementList SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

Block_Await :
  LBRACE /*.recoveryScope*/ StatementList_Await SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList_Await RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

Block_Yield :
  LBRACE /*.recoveryScope*/ StatementList_Yield SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList_Yield RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

Block_Yield_Await :
  LBRACE /*.recoveryScope*/ StatementList_Yield_Await SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList_Yield_Await RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

StatementList :
  StatementListItem
| StatementList StatementListItem
;

StatementList_Await :
  StatementListItem_Await
| StatementList_Await StatementListItem_Await
;

StatementList_Yield :
  StatementListItem_Yield
| StatementList_Yield StatementListItem_Yield
;

StatementList_Yield_Await :
  StatementListItem_Yield_Await
| StatementList_Yield_Await StatementListItem_Yield_Await
;

StatementListItem :
  Statement
| Declaration
| ERROR SEMICOLON
;

StatementListItem_Await :
  Statement_Await
| Declaration_Await
| ERROR SEMICOLON
;

StatementListItem_Yield :
  Statement_Yield
| Declaration_Yield
| ERROR SEMICOLON
;

StatementListItem_Yield_Await :
  Statement_Yield_Await
| Declaration_Yield_Await
| ERROR SEMICOLON
;

LexicalDeclaration_In :
  LetOrConst BindingList_In SEMICOLON
;

LexicalDeclaration_In_Await :
  LetOrConst BindingList_In_Await SEMICOLON
;

LexicalDeclaration_In_Yield :
  LetOrConst BindingList_In_Yield SEMICOLON
;

LexicalDeclaration_In_Yield_Await :
  LetOrConst BindingList_In_Yield_Await SEMICOLON
;

LetOrConst :
  LET
| CONST
;

BindingList :
  LexicalBinding
| BindingList COMMA LexicalBinding
;

BindingList_Await :
  LexicalBinding_Await
| BindingList_Await COMMA LexicalBinding_Await
;

BindingList_In :
  LexicalBinding_In
| BindingList_In COMMA LexicalBinding_In
;

BindingList_In_Await :
  LexicalBinding_In_Await
| BindingList_In_Await COMMA LexicalBinding_In_Await
;

BindingList_In_Yield :
  LexicalBinding_In_Yield
| BindingList_In_Yield COMMA LexicalBinding_In_Yield
;

BindingList_In_Yield_Await :
  LexicalBinding_In_Yield_Await
| BindingList_In_Yield_Await COMMA LexicalBinding_In_Yield_Await
;

BindingList_Yield :
  LexicalBinding_Yield
| BindingList_Yield COMMA LexicalBinding_Yield
;

BindingList_Yield_Await :
  LexicalBinding_Yield_Await
| BindingList_Yield_Await COMMA LexicalBinding_Yield_Await
;

ExclToken :
  EXCL
;

LexicalBinding :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt
| BindingIdentifier TypeAnnotationopt Initializeropt
| BindingPattern ExclToken TypeAnnotationopt Initializer
| BindingPattern TypeAnnotationopt Initializer
;

LexicalBinding_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_Await
| BindingPattern_Await ExclToken TypeAnnotationopt Initializer_Await
| BindingPattern_Await TypeAnnotationopt Initializer_Await
;

LexicalBinding_In :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In
| BindingIdentifier TypeAnnotationopt Initializeropt_In
| BindingPattern ExclToken TypeAnnotationopt Initializer_In
| BindingPattern TypeAnnotationopt Initializer_In
;

LexicalBinding_In_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_In_Await
| BindingPattern_Await ExclToken TypeAnnotationopt Initializer_In_Await
| BindingPattern_Await TypeAnnotationopt Initializer_In_Await
;

LexicalBinding_In_Yield :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In_Yield
| BindingIdentifier TypeAnnotationopt Initializeropt_In_Yield
| BindingPattern_Yield ExclToken TypeAnnotationopt Initializer_In_Yield
| BindingPattern_Yield TypeAnnotationopt Initializer_In_Yield
;

LexicalBinding_In_Yield_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In_Yield_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_In_Yield_Await
| BindingPattern_Yield_Await ExclToken TypeAnnotationopt Initializer_In_Yield_Await
| BindingPattern_Yield_Await TypeAnnotationopt Initializer_In_Yield_Await
;

LexicalBinding_Yield :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_Yield
| BindingIdentifier TypeAnnotationopt Initializeropt_Yield
| BindingPattern_Yield ExclToken TypeAnnotationopt Initializer_Yield
| BindingPattern_Yield TypeAnnotationopt Initializer_Yield
;

LexicalBinding_Yield_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_Yield_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_Yield_Await
| BindingPattern_Yield_Await ExclToken TypeAnnotationopt Initializer_Yield_Await
| BindingPattern_Yield_Await TypeAnnotationopt Initializer_Yield_Await
;

VariableStatement :
  VAR VariableDeclarationList_In SEMICOLON
;

VariableStatement_Await :
  VAR VariableDeclarationList_In_Await SEMICOLON
;

VariableStatement_Yield :
  VAR VariableDeclarationList_In_Yield SEMICOLON
;

VariableStatement_Yield_Await :
  VAR VariableDeclarationList_In_Yield_Await SEMICOLON
;

VariableDeclarationList :
  VariableDeclaration
| VariableDeclarationList COMMA VariableDeclaration
;

VariableDeclarationList_Await :
  VariableDeclaration_Await
| VariableDeclarationList_Await COMMA VariableDeclaration_Await
;

VariableDeclarationList_In :
  VariableDeclaration_In
| VariableDeclarationList_In COMMA VariableDeclaration_In
;

VariableDeclarationList_In_Await :
  VariableDeclaration_In_Await
| VariableDeclarationList_In_Await COMMA VariableDeclaration_In_Await
;

VariableDeclarationList_In_Yield :
  VariableDeclaration_In_Yield
| VariableDeclarationList_In_Yield COMMA VariableDeclaration_In_Yield
;

VariableDeclarationList_In_Yield_Await :
  VariableDeclaration_In_Yield_Await
| VariableDeclarationList_In_Yield_Await COMMA VariableDeclaration_In_Yield_Await
;

VariableDeclarationList_Yield :
  VariableDeclaration_Yield
| VariableDeclarationList_Yield COMMA VariableDeclaration_Yield
;

VariableDeclarationList_Yield_Await :
  VariableDeclaration_Yield_Await
| VariableDeclarationList_Yield_Await COMMA VariableDeclaration_Yield_Await
;

VariableDeclaration :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt
| BindingIdentifier TypeAnnotationopt Initializeropt
| BindingPattern ExclToken TypeAnnotationopt Initializer
| BindingPattern TypeAnnotationopt Initializer
;

VariableDeclaration_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_Await
| BindingPattern_Await ExclToken TypeAnnotationopt Initializer_Await
| BindingPattern_Await TypeAnnotationopt Initializer_Await
;

VariableDeclaration_In :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In
| BindingIdentifier TypeAnnotationopt Initializeropt_In
| BindingPattern ExclToken TypeAnnotationopt Initializer_In
| BindingPattern TypeAnnotationopt Initializer_In
;

VariableDeclaration_In_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_In_Await
| BindingPattern_Await ExclToken TypeAnnotationopt Initializer_In_Await
| BindingPattern_Await TypeAnnotationopt Initializer_In_Await
;

VariableDeclaration_In_Yield :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In_Yield
| BindingIdentifier TypeAnnotationopt Initializeropt_In_Yield
| BindingPattern_Yield ExclToken TypeAnnotationopt Initializer_In_Yield
| BindingPattern_Yield TypeAnnotationopt Initializer_In_Yield
;

VariableDeclaration_In_Yield_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_In_Yield_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_In_Yield_Await
| BindingPattern_Yield_Await ExclToken TypeAnnotationopt Initializer_In_Yield_Await
| BindingPattern_Yield_Await TypeAnnotationopt Initializer_In_Yield_Await
;

VariableDeclaration_Yield :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_Yield
| BindingIdentifier TypeAnnotationopt Initializeropt_Yield
| BindingPattern_Yield ExclToken TypeAnnotationopt Initializer_Yield
| BindingPattern_Yield TypeAnnotationopt Initializer_Yield
;

VariableDeclaration_Yield_Await :
  BindingIdentifier ExclToken TypeAnnotationopt Initializeropt_Yield_Await
| BindingIdentifier TypeAnnotationopt Initializeropt_Yield_Await
| BindingPattern_Yield_Await ExclToken TypeAnnotationopt Initializer_Yield_Await
| BindingPattern_Yield_Await TypeAnnotationopt Initializer_Yield_Await
;

BindingPattern :
  ObjectBindingPattern
| ArrayBindingPattern
;

BindingPattern_Await :
  ObjectBindingPattern_Await
| ArrayBindingPattern_Await
;

BindingPattern_Yield :
  ObjectBindingPattern_Yield
| ArrayBindingPattern_Yield
;

BindingPattern_Yield_Await :
  ObjectBindingPattern_Yield_Await
| ArrayBindingPattern_Yield_Await
;

ObjectBindingPattern :
  LBRACE /*.recoveryScope*/ BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_list_Comma_separated COMMA BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_list_Comma_separated RBRACE
;

ObjectBindingPattern_Await :
  LBRACE /*.recoveryScope*/ BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_Await_list_Comma_separated COMMA BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_Await_list_Comma_separated RBRACE
;

ObjectBindingPattern_Yield :
  LBRACE /*.recoveryScope*/ BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_Yield_list_Comma_separated COMMA BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_Yield_list_Comma_separated RBRACE
;

ObjectBindingPattern_Yield_Await :
  LBRACE /*.recoveryScope*/ BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_Yield_Await_list_Comma_separated COMMA BindingRestElementopt RBRACE
| LBRACE /*.recoveryScope*/ PropertyPattern_Yield_Await_list_Comma_separated RBRACE
;

PropertyPattern_Await_list_Comma_separated :
  PropertyPattern_Await_list_Comma_separated COMMA PropertyPattern_Await
| PropertyPattern_Await
;

PropertyPattern_Yield_Await_list_Comma_separated :
  PropertyPattern_Yield_Await_list_Comma_separated COMMA PropertyPattern_Yield_Await
| PropertyPattern_Yield_Await
;

PropertyPattern_Yield_list_Comma_separated :
  PropertyPattern_Yield_list_Comma_separated COMMA PropertyPattern_Yield
| PropertyPattern_Yield
;

PropertyPattern_list_Comma_separated :
  PropertyPattern_list_Comma_separated COMMA PropertyPattern
| PropertyPattern
;

ElementElision :
  COMMA
| Elision COMMA
;

ArrayBindingPattern :
  LBRACK ElementElisionopt BindingRestElementopt RBRACK
| LBRACK ElementPatternList RBRACK
| LBRACK ElementPatternList COMMA ElementElisionopt BindingRestElementopt RBRACK
;

ArrayBindingPattern_Await :
  LBRACK ElementElisionopt BindingRestElementopt RBRACK
| LBRACK ElementPatternList_Await RBRACK
| LBRACK ElementPatternList_Await COMMA ElementElisionopt BindingRestElementopt RBRACK
;

ArrayBindingPattern_Yield :
  LBRACK ElementElisionopt BindingRestElementopt RBRACK
| LBRACK ElementPatternList_Yield RBRACK
| LBRACK ElementPatternList_Yield COMMA ElementElisionopt BindingRestElementopt RBRACK
;

ArrayBindingPattern_Yield_Await :
  LBRACK ElementElisionopt BindingRestElementopt RBRACK
| LBRACK ElementPatternList_Yield_Await RBRACK
| LBRACK ElementPatternList_Yield_Await COMMA ElementElisionopt BindingRestElementopt RBRACK
;

ElementPatternList :
  BindingElisionElement
| ElementPatternList COMMA BindingElisionElement
;

ElementPatternList_Await :
  BindingElisionElement_Await
| ElementPatternList_Await COMMA BindingElisionElement_Await
;

ElementPatternList_Yield :
  BindingElisionElement_Yield
| ElementPatternList_Yield COMMA BindingElisionElement_Yield
;

ElementPatternList_Yield_Await :
  BindingElisionElement_Yield_Await
| ElementPatternList_Yield_Await COMMA BindingElisionElement_Yield_Await
;

BindingElisionElement :
  Elision ElementPattern
| ElementPattern
;

BindingElisionElement_Await :
  Elision ElementPattern_Await
| ElementPattern_Await
;

BindingElisionElement_Yield :
  Elision ElementPattern_Yield
| ElementPattern_Yield
;

BindingElisionElement_Yield_Await :
  Elision ElementPattern_Yield_Await
| ElementPattern_Yield_Await
;

PropertyPattern :
  SingleNameBinding
| PropertyName COLON ElementPattern
| SyntaxError
;

PropertyPattern_Await :
  SingleNameBinding_Await
| PropertyName_Await COLON ElementPattern_Await
| SyntaxError
;

PropertyPattern_Yield :
  SingleNameBinding_Yield
| PropertyName_Yield COLON ElementPattern_Yield
| SyntaxError
;

PropertyPattern_Yield_Await :
  SingleNameBinding_Yield_Await
| PropertyName_Yield_Await COLON ElementPattern_Yield_Await
| SyntaxError
;

ElementPattern :
  SingleNameBinding
| BindingPattern Initializeropt_In
| SyntaxError
;

ElementPattern_Await :
  SingleNameBinding_Await
| BindingPattern_Await Initializeropt_In_Await
| SyntaxError
;

ElementPattern_Yield :
  SingleNameBinding_Yield
| BindingPattern_Yield Initializeropt_In_Yield
| SyntaxError
;

ElementPattern_Yield_Await :
  SingleNameBinding_Yield_Await
| BindingPattern_Yield_Await Initializeropt_In_Yield_Await
| SyntaxError
;

SingleNameBinding :
  IdentifierReference Initializeropt_In
;

SingleNameBinding_Await :
  IdentifierReference_Await Initializeropt_In_Await
;

SingleNameBinding_Yield :
  IdentifierReference_Yield Initializeropt_In_Yield
;

SingleNameBinding_Yield_Await :
  IdentifierReference_Yield_Await Initializeropt_In_Yield_Await
;

BindingRestElement :
  DOTDOTDOT BindingIdentifier
;

EmptyStatement :
  SEMICOLON /*.emptyStatement*/
;

ExpressionStatement :
  Expression_In_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies SEMICOLON
;

ExpressionStatement_Await :
  Expression_In_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies SEMICOLON
;

ExpressionStatement_Yield :
  Expression_In_Yield_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies SEMICOLON
;

ExpressionStatement_Yield_Await :
  Expression_In_Yield_Await_NoLetSq_NoObjLiteral_NoFuncClass_NoAs_NoSatisfies SEMICOLON
;

IfStatement :
  IF LPAREN Expression_In RPAREN Statement ELSE Statement
| IF LPAREN Expression_In RPAREN Statement %prec ELSE
;

IfStatement_Await :
  IF LPAREN Expression_In_Await RPAREN Statement_Await ELSE Statement_Await
| IF LPAREN Expression_In_Await RPAREN Statement_Await %prec ELSE
;

IfStatement_Yield :
  IF LPAREN Expression_In_Yield RPAREN Statement_Yield ELSE Statement_Yield
| IF LPAREN Expression_In_Yield RPAREN Statement_Yield %prec ELSE
;

IfStatement_Yield_Await :
  IF LPAREN Expression_In_Yield_Await RPAREN Statement_Yield_Await ELSE Statement_Yield_Await
| IF LPAREN Expression_In_Yield_Await RPAREN Statement_Yield_Await %prec ELSE
;

IterationStatement :
  DO Statement WHILE LPAREN Expression_In RPAREN SEMICOLON /*.doWhile*/
| WHILE LPAREN Expression_In RPAREN Statement
| FOR LPAREN Expression_NoLet SEMICOLON /*.forSC*/ ForCondition SEMICOLON /*.forSC*/ ForFinalExpression RPAREN Statement
| FOR LPAREN SEMICOLON /*.forSC*/ ForCondition SEMICOLON /*.forSC*/ ForFinalExpression RPAREN Statement
| FOR LPAREN Expression_NoAs_NoSatisfies_StartWithLet SEMICOLON /*.forSC*/ ForCondition SEMICOLON /*.forSC*/ ForFinalExpression RPAREN Statement
| FOR LPAREN VAR VariableDeclarationList SEMICOLON /*.forSC*/ ForCondition SEMICOLON /*.forSC*/ ForFinalExpression RPAREN Statement
| FOR LPAREN LetOrConst BindingList SEMICOLON /*.forSC*/ ForCondition SEMICOLON /*.forSC*/ ForFinalExpression RPAREN Statement
| FOR LPAREN LeftHandSideExpression_NoLet IN Expression_In RPAREN Statement
| FOR LPAREN LeftHandSideExpression_StartWithLet IN Expression_In RPAREN Statement
| FOR LPAREN VAR ForBinding IN Expression_In RPAREN Statement
| FOR LPAREN ForDeclaration IN Expression_In RPAREN Statement
| FOR LPAREN LeftHandSideExpression_NoAsync_NoLet OF AssignmentExpression_In RPAREN Statement
| FOR LPAREN ASYNC lookahead_notStartOfArrowFunction OF AssignmentExpression_In RPAREN Statement
| FOR LPAREN VAR ForBinding OF AssignmentExpression_In RPAREN Statement
| FOR LPAREN ForDeclaration OF AssignmentExpression_In RPAREN Statement
;

IterationStatement_Await :
  DO Statement_Await WHILE LPAREN Expression_In_Await RPAREN SEMICOLON /*.doWhile*/
| WHILE LPAREN Expression_In_Await RPAREN Statement_Await
| FOR LPAREN Expression_Await_NoLet SEMICOLON /*.forSC*/ ForCondition_Await SEMICOLON /*.forSC*/ ForFinalExpression_Await RPAREN Statement_Await
| FOR LPAREN SEMICOLON /*.forSC*/ ForCondition_Await SEMICOLON /*.forSC*/ ForFinalExpression_Await RPAREN Statement_Await
| FOR LPAREN Expression_Await_NoAs_NoSatisfies_StartWithLet SEMICOLON /*.forSC*/ ForCondition_Await SEMICOLON /*.forSC*/ ForFinalExpression_Await RPAREN Statement_Await
| FOR LPAREN VAR VariableDeclarationList_Await SEMICOLON /*.forSC*/ ForCondition_Await SEMICOLON /*.forSC*/ ForFinalExpression_Await RPAREN Statement_Await
| FOR LPAREN LetOrConst BindingList_Await SEMICOLON /*.forSC*/ ForCondition_Await SEMICOLON /*.forSC*/ ForFinalExpression_Await RPAREN Statement_Await
| FOR LPAREN LeftHandSideExpression_Await_NoLet IN Expression_In_Await RPAREN Statement_Await
| FOR LPAREN LeftHandSideExpression_Await_StartWithLet IN Expression_In_Await RPAREN Statement_Await
| FOR LPAREN VAR ForBinding_Await IN Expression_In_Await RPAREN Statement_Await
| FOR LPAREN ForDeclaration_Await IN Expression_In_Await RPAREN Statement_Await
| FOR AWAIT LPAREN LeftHandSideExpression_Await_NoAsync_NoLet OF AssignmentExpression_In_Await RPAREN Statement_Await
| FOR LPAREN LeftHandSideExpression_Await_NoAsync_NoLet OF AssignmentExpression_In_Await RPAREN Statement_Await
| FOR AWAIT LPAREN ASYNC lookahead_notStartOfArrowFunction OF AssignmentExpression_In_Await RPAREN Statement_Await
| FOR LPAREN ASYNC lookahead_notStartOfArrowFunction OF AssignmentExpression_In_Await RPAREN Statement_Await
| FOR AWAIT LPAREN VAR ForBinding_Await OF AssignmentExpression_In_Await RPAREN Statement_Await
| FOR LPAREN VAR ForBinding_Await OF AssignmentExpression_In_Await RPAREN Statement_Await
| FOR AWAIT LPAREN ForDeclaration_Await OF AssignmentExpression_In_Await RPAREN Statement_Await
| FOR LPAREN ForDeclaration_Await OF AssignmentExpression_In_Await RPAREN Statement_Await
;

IterationStatement_Yield :
  DO Statement_Yield WHILE LPAREN Expression_In_Yield RPAREN SEMICOLON /*.doWhile*/
| WHILE LPAREN Expression_In_Yield RPAREN Statement_Yield
| FOR LPAREN Expression_Yield_NoLet SEMICOLON /*.forSC*/ ForCondition_Yield SEMICOLON /*.forSC*/ ForFinalExpression_Yield RPAREN Statement_Yield
| FOR LPAREN SEMICOLON /*.forSC*/ ForCondition_Yield SEMICOLON /*.forSC*/ ForFinalExpression_Yield RPAREN Statement_Yield
| FOR LPAREN Expression_Yield_NoAs_NoSatisfies_StartWithLet SEMICOLON /*.forSC*/ ForCondition_Yield SEMICOLON /*.forSC*/ ForFinalExpression_Yield RPAREN Statement_Yield
| FOR LPAREN VAR VariableDeclarationList_Yield SEMICOLON /*.forSC*/ ForCondition_Yield SEMICOLON /*.forSC*/ ForFinalExpression_Yield RPAREN Statement_Yield
| FOR LPAREN LetOrConst BindingList_Yield SEMICOLON /*.forSC*/ ForCondition_Yield SEMICOLON /*.forSC*/ ForFinalExpression_Yield RPAREN Statement_Yield
| FOR LPAREN LeftHandSideExpression_Yield_NoLet IN Expression_In_Yield RPAREN Statement_Yield
| FOR LPAREN LeftHandSideExpression_Yield_StartWithLet IN Expression_In_Yield RPAREN Statement_Yield
| FOR LPAREN VAR ForBinding_Yield IN Expression_In_Yield RPAREN Statement_Yield
| FOR LPAREN ForDeclaration_Yield IN Expression_In_Yield RPAREN Statement_Yield
| FOR LPAREN LeftHandSideExpression_Yield_NoAsync_NoLet OF AssignmentExpression_In_Yield RPAREN Statement_Yield
| FOR LPAREN ASYNC lookahead_notStartOfArrowFunction OF AssignmentExpression_In_Yield RPAREN Statement_Yield
| FOR LPAREN VAR ForBinding_Yield OF AssignmentExpression_In_Yield RPAREN Statement_Yield
| FOR LPAREN ForDeclaration_Yield OF AssignmentExpression_In_Yield RPAREN Statement_Yield
;

IterationStatement_Yield_Await :
  DO Statement_Yield_Await WHILE LPAREN Expression_In_Yield_Await RPAREN SEMICOLON /*.doWhile*/
| WHILE LPAREN Expression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN Expression_Yield_Await_NoLet SEMICOLON /*.forSC*/ ForCondition_Yield_Await SEMICOLON /*.forSC*/ ForFinalExpression_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN SEMICOLON /*.forSC*/ ForCondition_Yield_Await SEMICOLON /*.forSC*/ ForFinalExpression_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN Expression_Yield_Await_NoAs_NoSatisfies_StartWithLet SEMICOLON /*.forSC*/ ForCondition_Yield_Await SEMICOLON /*.forSC*/ ForFinalExpression_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN VAR VariableDeclarationList_Yield_Await SEMICOLON /*.forSC*/ ForCondition_Yield_Await SEMICOLON /*.forSC*/ ForFinalExpression_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN LetOrConst BindingList_Yield_Await SEMICOLON /*.forSC*/ ForCondition_Yield_Await SEMICOLON /*.forSC*/ ForFinalExpression_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN LeftHandSideExpression_Yield_Await_NoLet IN Expression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN LeftHandSideExpression_Yield_Await_StartWithLet IN Expression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN VAR ForBinding_Yield_Await IN Expression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN ForDeclaration_Yield_Await IN Expression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR AWAIT LPAREN LeftHandSideExpression_Yield_Await_NoAsync_NoLet OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN LeftHandSideExpression_Yield_Await_NoAsync_NoLet OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR AWAIT LPAREN ASYNC lookahead_notStartOfArrowFunction OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN ASYNC lookahead_notStartOfArrowFunction OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR AWAIT LPAREN VAR ForBinding_Yield_Await OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN VAR ForBinding_Yield_Await OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR AWAIT LPAREN ForDeclaration_Yield_Await OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
| FOR LPAREN ForDeclaration_Yield_Await OF AssignmentExpression_In_Yield_Await RPAREN Statement_Yield_Await
;

ForDeclaration :
  LetOrConst ForBinding
;

ForDeclaration_Await :
  LetOrConst ForBinding_Await
;

ForDeclaration_Yield :
  LetOrConst ForBinding_Yield
;

ForDeclaration_Yield_Await :
  LetOrConst ForBinding_Yield_Await
;

ForBinding :
  BindingIdentifier
| BindingPattern
;

ForBinding_Await :
  BindingIdentifier
| BindingPattern_Await
;

ForBinding_Yield :
  BindingIdentifier
| BindingPattern_Yield
;

ForBinding_Yield_Await :
  BindingIdentifier
| BindingPattern_Yield_Await
;

ForCondition :
  Expressionopt_In
;

ForCondition_Await :
  Expressionopt_In_Await
;

ForCondition_Yield :
  Expressionopt_In_Yield
;

ForCondition_Yield_Await :
  Expressionopt_In_Yield_Await
;

ForFinalExpression :
  Expressionopt_In
;

ForFinalExpression_Await :
  Expressionopt_In_Await
;

ForFinalExpression_Yield :
  Expressionopt_In_Yield
;

ForFinalExpression_Yield_Await :
  Expressionopt_In_Yield_Await
;

ContinueStatement :
  CONTINUE SEMICOLON
| CONTINUE /*.noLineBreak*/ LabelIdentifier SEMICOLON
;

BreakStatement :
  BREAK SEMICOLON
| BREAK /*.noLineBreak*/ LabelIdentifier SEMICOLON
;

ReturnStatement :
  RETURN SEMICOLON
| RETURN /*.noLineBreak*/ Expression_In SEMICOLON
;

ReturnStatement_Await :
  RETURN SEMICOLON
| RETURN /*.noLineBreak*/ Expression_In_Await SEMICOLON
;

ReturnStatement_Yield :
  RETURN SEMICOLON
| RETURN /*.noLineBreak*/ Expression_In_Yield SEMICOLON
;

ReturnStatement_Yield_Await :
  RETURN SEMICOLON
| RETURN /*.noLineBreak*/ Expression_In_Yield_Await SEMICOLON
;

WithStatement :
  WITH LPAREN Expression_In RPAREN Statement
;

WithStatement_Await :
  WITH LPAREN Expression_In_Await RPAREN Statement_Await
;

WithStatement_Yield :
  WITH LPAREN Expression_In_Yield RPAREN Statement_Yield
;

WithStatement_Yield_Await :
  WITH LPAREN Expression_In_Yield_Await RPAREN Statement_Yield_Await
;

SwitchStatement :
  SWITCH LPAREN Expression_In RPAREN CaseBlock
;

SwitchStatement_Await :
  SWITCH LPAREN Expression_In_Await RPAREN CaseBlock_Await
;

SwitchStatement_Yield :
  SWITCH LPAREN Expression_In_Yield RPAREN CaseBlock_Yield
;

SwitchStatement_Yield_Await :
  SWITCH LPAREN Expression_In_Yield_Await RPAREN CaseBlock_Yield_Await
;

CaseBlock :
  LBRACE /*.recoveryScope*/ CaseClausesopt RBRACE
;

CaseBlock_Await :
  LBRACE /*.recoveryScope*/ CaseClausesopt_Await RBRACE
;

CaseBlock_Yield :
  LBRACE /*.recoveryScope*/ CaseClausesopt_Yield RBRACE
;

CaseBlock_Yield_Await :
  LBRACE /*.recoveryScope*/ CaseClausesopt_Yield_Await RBRACE
;

CaseClauses :
  CaseClause
| CaseClauses CaseClause
;

CaseClauses_Await :
  CaseClause_Await
| CaseClauses_Await CaseClause_Await
;

CaseClauses_Yield :
  CaseClause_Yield
| CaseClauses_Yield CaseClause_Yield
;

CaseClauses_Yield_Await :
  CaseClause_Yield_Await
| CaseClauses_Yield_Await CaseClause_Yield_Await
;

CaseClause :
  CASE Expression_In COLON StatementList
| CASE Expression_In COLON
| DEFAULT COLON StatementList
| DEFAULT COLON
;

CaseClause_Await :
  CASE Expression_In_Await COLON StatementList_Await
| CASE Expression_In_Await COLON
| DEFAULT COLON StatementList_Await
| DEFAULT COLON
;

CaseClause_Yield :
  CASE Expression_In_Yield COLON StatementList_Yield
| CASE Expression_In_Yield COLON
| DEFAULT COLON StatementList_Yield
| DEFAULT COLON
;

CaseClause_Yield_Await :
  CASE Expression_In_Yield_Await COLON StatementList_Yield_Await
| CASE Expression_In_Yield_Await COLON
| DEFAULT COLON StatementList_Yield_Await
| DEFAULT COLON
;

LabelledStatement :
  LabelIdentifier COLON LabelledItem
;

LabelledStatement_Await :
  LabelIdentifier COLON LabelledItem_Await
;

LabelledStatement_Yield :
  LabelIdentifier COLON LabelledItem_Yield
;

LabelledStatement_Yield_Await :
  LabelIdentifier COLON LabelledItem_Yield_Await
;

LabelledItem :
  Statement
| FunctionDeclaration
;

LabelledItem_Await :
  Statement_Await
| FunctionDeclaration
;

LabelledItem_Yield :
  Statement_Yield
| FunctionDeclaration
;

LabelledItem_Yield_Await :
  Statement_Yield_Await
| FunctionDeclaration
;

ThrowStatement :
  THROW /*.noLineBreak*/ Expression_In SEMICOLON
;

ThrowStatement_Await :
  THROW /*.noLineBreak*/ Expression_In_Await SEMICOLON
;

ThrowStatement_Yield :
  THROW /*.noLineBreak*/ Expression_In_Yield SEMICOLON
;

ThrowStatement_Yield_Await :
  THROW /*.noLineBreak*/ Expression_In_Yield_Await SEMICOLON
;

TryStatement :
  TRY Block Catch
| TRY Block Catch Finally
| TRY Block Finally
;

TryStatement_Await :
  TRY Block_Await Catch_Await
| TRY Block_Await Catch_Await Finally_Await
| TRY Block_Await Finally_Await
;

TryStatement_Yield :
  TRY Block_Yield Catch_Yield
| TRY Block_Yield Catch_Yield Finally_Yield
| TRY Block_Yield Finally_Yield
;

TryStatement_Yield_Await :
  TRY Block_Yield_Await Catch_Yield_Await
| TRY Block_Yield_Await Catch_Yield_Await Finally_Yield_Await
| TRY Block_Yield_Await Finally_Yield_Await
;

Catch :
  CATCH LPAREN CatchParameter RPAREN Block
| CATCH Block
;

Catch_Await :
  CATCH LPAREN CatchParameter_Await RPAREN Block_Await
| CATCH Block_Await
;

Catch_Yield :
  CATCH LPAREN CatchParameter_Yield RPAREN Block_Yield
| CATCH Block_Yield
;

Catch_Yield_Await :
  CATCH LPAREN CatchParameter_Yield_Await RPAREN Block_Yield_Await
| CATCH Block_Yield_Await
;

Finally :
  FINALLY Block
;

Finally_Await :
  FINALLY Block_Await
;

Finally_Yield :
  FINALLY Block_Yield
;

Finally_Yield_Await :
  FINALLY Block_Yield_Await
;

CatchParameter :
  BindingIdentifier TypeAnnotation
| BindingIdentifier
| BindingPattern TypeAnnotation
| BindingPattern
;

CatchParameter_Await :
  BindingIdentifier TypeAnnotation
| BindingIdentifier
| BindingPattern_Await TypeAnnotation
| BindingPattern_Await
;

CatchParameter_Yield :
  BindingIdentifier TypeAnnotation
| BindingIdentifier
| BindingPattern_Yield TypeAnnotation
| BindingPattern_Yield
;

CatchParameter_Yield_Await :
  BindingIdentifier TypeAnnotation
| BindingIdentifier
| BindingPattern_Yield_Await TypeAnnotation
| BindingPattern_Yield_Await
;

DebuggerStatement :
  DEBUGGER SEMICOLON
;

FunctionDeclaration :
  FUNCTION BindingIdentifier FormalParameters FunctionBody
| FUNCTION FormalParameters FunctionBody
;

FunctionExpression :
  FUNCTION BindingIdentifier FormalParameters FunctionBody
| FUNCTION FormalParameters FunctionBody
;

UniqueFormalParameters :
  FormalParameters
;

UniqueFormalParameters_Await :
  FormalParameters_Await
;

UniqueFormalParameters_Yield :
  FormalParameters_Yield
;

UniqueFormalParameters_Yield_Await :
  FormalParameters_Yield_Await
;

FunctionBody :
  LBRACE /*.recoveryScope*/ StatementList SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
| SEMICOLON
;

FunctionBody_Await :
  LBRACE /*.recoveryScope*/ StatementList_Await SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList_Await RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
| SEMICOLON
;

FunctionBody_Yield :
  LBRACE /*.recoveryScope*/ StatementList_Yield SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList_Yield RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
| SEMICOLON
;

FunctionBody_Yield_Await :
  LBRACE /*.recoveryScope*/ StatementList_Yield_Await SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList_Yield_Await RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
| SEMICOLON
;

ArrowFunction :
  BindingIdentifier /*.noLineBreak*/ ASSIGNGT ConciseBody
| lookahead_StartOfArrowFunction FormalParameters /*.noLineBreak*/ ASSIGNGT ConciseBody
;

ArrowFunction_In :
  BindingIdentifier /*.noLineBreak*/ ASSIGNGT ConciseBody_In
| lookahead_StartOfArrowFunction FormalParameters /*.noLineBreak*/ ASSIGNGT ConciseBody_In
;

// lookahead: StartOfArrowFunction
lookahead_StartOfArrowFunction :
  %empty
;

ArrowParameters :
  BindingIdentifier
| FormalParameters
;

ConciseBody :
  AssignmentExpression_NoObjLiteral
| FunctionBody
| SyntaxError
;

ConciseBody_In :
  AssignmentExpression_In_NoObjLiteral
| FunctionBody
| SyntaxError
;

StartOfArrowFunction :
  BindingIdentifier ASSIGNGT
| TypeParameters ParameterList TypeAnnotation ASSIGNGT
| TypeParameters ParameterList ASSIGNGT
| ParameterList TypeAnnotation ASSIGNGT
| ParameterList ASSIGNGT
;

AsyncArrowFunction :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ lookahead_StartOfArrowFunction ArrowParameters /*.noLineBreak*/ ASSIGNGT AsyncConciseBody
;

AsyncArrowFunction_In :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ lookahead_StartOfArrowFunction ArrowParameters /*.noLineBreak*/ ASSIGNGT AsyncConciseBody_In
;

AsyncConciseBody :
  AssignmentExpression_Await_NoObjLiteral
| AsyncFunctionBody
;

AsyncConciseBody_In :
  AssignmentExpression_In_Await_NoObjLiteral
| AsyncFunctionBody
;

MethodDefinition :
  PropertyName QUEST UniqueFormalParameters FunctionBody
| PropertyName EXCL UniqueFormalParameters FunctionBody
| PropertyName UniqueFormalParameters FunctionBody
| GeneratorMethod
| AsyncMethod
| AsyncGeneratorMethod
| GET PropertyName LPAREN RPAREN TypeAnnotationopt FunctionBody
| SET PropertyName LPAREN PropertySetParameterList RPAREN FunctionBody
;

MethodDefinition_Await :
  PropertyName_Await QUEST UniqueFormalParameters FunctionBody
| PropertyName_Await EXCL UniqueFormalParameters FunctionBody
| PropertyName_Await UniqueFormalParameters FunctionBody
| GeneratorMethod_Await
| AsyncMethod_Await
| AsyncGeneratorMethod_Await
| GET PropertyName_Await LPAREN RPAREN TypeAnnotationopt FunctionBody
| SET PropertyName_Await LPAREN PropertySetParameterList RPAREN FunctionBody
;

MethodDefinition_Yield :
  PropertyName_Yield QUEST UniqueFormalParameters FunctionBody
| PropertyName_Yield EXCL UniqueFormalParameters FunctionBody
| PropertyName_Yield UniqueFormalParameters FunctionBody
| GeneratorMethod_Yield
| AsyncMethod_Yield
| AsyncGeneratorMethod_Yield
| GET PropertyName_Yield LPAREN RPAREN TypeAnnotationopt FunctionBody
| SET PropertyName_Yield LPAREN PropertySetParameterList RPAREN FunctionBody
;

MethodDefinition_Yield_Await :
  PropertyName_Yield_Await QUEST UniqueFormalParameters FunctionBody
| PropertyName_Yield_Await EXCL UniqueFormalParameters FunctionBody
| PropertyName_Yield_Await UniqueFormalParameters FunctionBody
| GeneratorMethod_Yield_Await
| AsyncMethod_Yield_Await
| AsyncGeneratorMethod_Yield_Await
| GET PropertyName_Yield_Await LPAREN RPAREN TypeAnnotationopt FunctionBody
| SET PropertyName_Yield_Await LPAREN PropertySetParameterList RPAREN FunctionBody
;

PropertySetParameterList :
  Parameter
;

GeneratorMethod :
  MULT PropertyName QUEST UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName EXCL UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName UniqueFormalParameters_Yield GeneratorBody
;

GeneratorMethod_Await :
  MULT PropertyName_Await QUEST UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName_Await EXCL UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName_Await UniqueFormalParameters_Yield GeneratorBody
;

GeneratorMethod_Yield :
  MULT PropertyName_Yield QUEST UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName_Yield EXCL UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName_Yield UniqueFormalParameters_Yield GeneratorBody
;

GeneratorMethod_Yield_Await :
  MULT PropertyName_Yield_Await QUEST UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName_Yield_Await EXCL UniqueFormalParameters_Yield GeneratorBody
| MULT PropertyName_Yield_Await UniqueFormalParameters_Yield GeneratorBody
;

GeneratorDeclaration :
  FUNCTION MULT BindingIdentifier FormalParameters_Yield GeneratorBody
| FUNCTION MULT FormalParameters_Yield GeneratorBody
;

GeneratorExpression :
  FUNCTION MULT BindingIdentifier FormalParameters_Yield GeneratorBody
| FUNCTION MULT FormalParameters_Yield GeneratorBody
;

GeneratorBody :
  FunctionBody_Yield
;

YieldExpression :
  YIELD
| YIELD /*.afterYield*/ /*.noLineBreak*/ AssignmentExpression_Yield
| YIELD /*.afterYield*/ /*.noLineBreak*/ MULT AssignmentExpression_Yield
;

YieldExpression_Await :
  YIELD
| YIELD /*.afterYield*/ /*.noLineBreak*/ AssignmentExpression_Yield_Await
| YIELD /*.afterYield*/ /*.noLineBreak*/ MULT AssignmentExpression_Yield_Await
;

YieldExpression_In :
  YIELD
| YIELD /*.afterYield*/ /*.noLineBreak*/ AssignmentExpression_In_Yield
| YIELD /*.afterYield*/ /*.noLineBreak*/ MULT AssignmentExpression_In_Yield
;

YieldExpression_In_Await :
  YIELD
| YIELD /*.afterYield*/ /*.noLineBreak*/ AssignmentExpression_In_Yield_Await
| YIELD /*.afterYield*/ /*.noLineBreak*/ MULT AssignmentExpression_In_Yield_Await
;

AsyncMethod :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName QUEST UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName EXCL UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName UniqueFormalParameters_Await AsyncFunctionBody
;

AsyncMethod_Await :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Await QUEST UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Await EXCL UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Await UniqueFormalParameters_Await AsyncFunctionBody
;

AsyncMethod_Yield :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Yield QUEST UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Yield EXCL UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Yield UniqueFormalParameters_Await AsyncFunctionBody
;

AsyncMethod_Yield_Await :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Yield_Await QUEST UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Yield_Await EXCL UniqueFormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ PropertyName_Yield_Await UniqueFormalParameters_Await AsyncFunctionBody
;

AsyncFunctionDeclaration :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION BindingIdentifier FormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION FormalParameters_Await AsyncFunctionBody
;

AsyncFunctionExpression :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION BindingIdentifier FormalParameters_Await AsyncFunctionBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION FormalParameters_Await AsyncFunctionBody
;

AsyncFunctionBody :
  FunctionBody_Await
;

AsyncGeneratorMethod :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName QUEST UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName EXCL UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName UniqueFormalParameters_Yield_Await AsyncGeneratorBody
;

AsyncGeneratorMethod_Await :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Await QUEST UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Await EXCL UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Await UniqueFormalParameters_Yield_Await AsyncGeneratorBody
;

AsyncGeneratorMethod_Yield :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Yield QUEST UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Yield EXCL UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Yield UniqueFormalParameters_Yield_Await AsyncGeneratorBody
;

AsyncGeneratorMethod_Yield_Await :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Yield_Await QUEST UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Yield_Await EXCL UniqueFormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ MULT PropertyName_Yield_Await UniqueFormalParameters_Yield_Await AsyncGeneratorBody
;

AsyncGeneratorDeclaration :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION MULT BindingIdentifier FormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION MULT FormalParameters_Yield_Await AsyncGeneratorBody
;

AsyncGeneratorExpression :
  ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION MULT BindingIdentifier FormalParameters_Yield_Await AsyncGeneratorBody
| ASYNC /*.afterAsync*/ /*.noLineBreak*/ FUNCTION MULT FormalParameters_Yield_Await AsyncGeneratorBody
;

AsyncGeneratorBody :
  FunctionBody_Yield_Await
;

AwaitExpression :
  AWAIT UnaryExpression_Await
;

ClassDeclaration :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail
| Modifiers CLASS TypeParametersopt ClassTail
| CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail
| CLASS TypeParametersopt ClassTail
;

ClassDeclaration_Await :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail_Await
| Modifiers CLASS TypeParametersopt ClassTail_Await
| CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail_Await
| CLASS TypeParametersopt ClassTail_Await
;

ClassDeclaration_Yield :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail_Yield
| Modifiers CLASS TypeParametersopt ClassTail_Yield
| CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail_Yield
| CLASS TypeParametersopt ClassTail_Yield
;

ClassDeclaration_Yield_Await :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail_Yield_Await
| Modifiers CLASS TypeParametersopt ClassTail_Yield_Await
| CLASS BindingIdentifier_WithoutImplements TypeParametersopt ClassTail_Yield_Await
| CLASS TypeParametersopt ClassTail_Yield_Await
;

ClassExpression :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail
| Modifiers CLASS BindingIdentifier_WithoutImplements ClassTail
| Modifiers CLASS TypeParameters ClassTail
| Modifiers CLASS ClassTail
| CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail
| CLASS BindingIdentifier_WithoutImplements ClassTail
| CLASS TypeParameters ClassTail
| CLASS ClassTail
;

ClassExpression_Await :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail_Await
| Modifiers CLASS BindingIdentifier_WithoutImplements ClassTail_Await
| Modifiers CLASS TypeParameters ClassTail_Await
| Modifiers CLASS ClassTail_Await
| CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail_Await
| CLASS BindingIdentifier_WithoutImplements ClassTail_Await
| CLASS TypeParameters ClassTail_Await
| CLASS ClassTail_Await
;

ClassExpression_Yield :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail_Yield
| Modifiers CLASS BindingIdentifier_WithoutImplements ClassTail_Yield
| Modifiers CLASS TypeParameters ClassTail_Yield
| Modifiers CLASS ClassTail_Yield
| CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail_Yield
| CLASS BindingIdentifier_WithoutImplements ClassTail_Yield
| CLASS TypeParameters ClassTail_Yield
| CLASS ClassTail_Yield
;

ClassExpression_Yield_Await :
  Modifiers CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail_Yield_Await
| Modifiers CLASS BindingIdentifier_WithoutImplements ClassTail_Yield_Await
| Modifiers CLASS TypeParameters ClassTail_Yield_Await
| Modifiers CLASS ClassTail_Yield_Await
| CLASS BindingIdentifier_WithoutImplements TypeParameters ClassTail_Yield_Await
| CLASS BindingIdentifier_WithoutImplements ClassTail_Yield_Await
| CLASS TypeParameters ClassTail_Yield_Await
| CLASS ClassTail_Yield_Await
;

ClassTail :
  ClassHeritage ClassBody
;

ClassTail_Await :
  ClassHeritage_Await ClassBody_Await
;

ClassTail_Yield :
  ClassHeritage_Yield ClassBody_Yield
;

ClassTail_Yield_Await :
  ClassHeritage_Yield_Await ClassBody_Yield_Await
;

ClassHeritage :
  ClassExtendsClause ImplementsClause
| ClassExtendsClause
| ImplementsClause
| %empty
;

ClassHeritage_Await :
  ClassExtendsClause_Await ImplementsClause
| ClassExtendsClause_Await
| ImplementsClause
| %empty
;

ClassHeritage_Yield :
  ClassExtendsClause_Yield ImplementsClause
| ClassExtendsClause_Yield
| ImplementsClause
| %empty
;

ClassHeritage_Yield_Await :
  ClassExtendsClause_Yield_Await ImplementsClause
| ClassExtendsClause_Yield_Await
| ImplementsClause
| %empty
;

StartOfExtendsTypeRef :
  TypeReference IMPLEMENTS
| TypeReference LBRACE
;

ClassExtendsClause :
  EXTENDS lookahead_StartOfExtendsTypeRef TypeReference
| EXTENDS lookahead_notStartOfExtendsTypeRef LeftHandSideExpression
;

ClassExtendsClause_Await :
  EXTENDS lookahead_StartOfExtendsTypeRef TypeReference
| EXTENDS lookahead_notStartOfExtendsTypeRef LeftHandSideExpression_Await
;

ClassExtendsClause_Yield :
  EXTENDS lookahead_StartOfExtendsTypeRef TypeReference
| EXTENDS lookahead_notStartOfExtendsTypeRef LeftHandSideExpression_Yield
;

ClassExtendsClause_Yield_Await :
  EXTENDS lookahead_StartOfExtendsTypeRef TypeReference
| EXTENDS lookahead_notStartOfExtendsTypeRef LeftHandSideExpression_Yield_Await
;

// lookahead: StartOfExtendsTypeRef
lookahead_StartOfExtendsTypeRef :
  %empty
;

// lookahead: !StartOfExtendsTypeRef
lookahead_notStartOfExtendsTypeRef :
  %empty
;

ImplementsClause :
  IMPLEMENTS TypeReference_list_Comma_separated
;

TypeReference_list_Comma_separated :
  TypeReference_list_Comma_separated COMMA TypeReference
| TypeReference
;

ClassBody :
  LBRACE /*.recoveryScope*/ ClassElementList RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

ClassBody_Await :
  LBRACE /*.recoveryScope*/ ClassElementList_Await RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

ClassBody_Yield :
  LBRACE /*.recoveryScope*/ ClassElementList_Yield RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

ClassBody_Yield_Await :
  LBRACE /*.recoveryScope*/ ClassElementList_Yield_Await RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

ClassElementList :
  ClassElement
| ClassElementList ClassElement
;

ClassElementList_Await :
  ClassElement_Await
| ClassElementList_Await ClassElement_Await
;

ClassElementList_Yield :
  ClassElement_Yield
| ClassElementList_Yield ClassElement_Yield
;

ClassElementList_Yield_Await :
  ClassElement_Yield_Await
| ClassElementList_Yield_Await ClassElement_Yield_Await
;

Modifier :
  AccessibilityModifier
| Decorator
| STATIC
| ABSTRACT
| OVERRIDE
| READONLY
| ACCESSOR
;

Modifier_WithDeclare :
  AccessibilityModifier
| Decorator
| STATIC
| ABSTRACT
| OVERRIDE
| READONLY
| ACCESSOR
| DECLARE
;

Modifiers :
  Modifier
| Modifiers Modifier
;

Modifiers_WithDeclare :
  Modifier_WithDeclare
| Modifiers_WithDeclare Modifier_WithDeclare
;

ClassElement :
  Modifiers_WithDeclare MethodDefinition
| MethodDefinition
| Modifiers_WithDeclare FieldDefinition
| FieldDefinition
| IndexSignature_WithDeclare SEMICOLON
| ClassStaticBlock
| SEMICOLON
| SyntaxError SEMICOLON
;

ClassElement_Await :
  Modifiers_WithDeclare MethodDefinition_Await
| MethodDefinition_Await
| Modifiers_WithDeclare FieldDefinition_Await
| FieldDefinition_Await
| IndexSignature_WithDeclare SEMICOLON
| ClassStaticBlock
| SEMICOLON
| SyntaxError SEMICOLON
;

ClassElement_Yield :
  Modifiers_WithDeclare MethodDefinition_Yield
| MethodDefinition_Yield
| Modifiers_WithDeclare FieldDefinition_Yield
| FieldDefinition_Yield
| IndexSignature_WithDeclare SEMICOLON
| ClassStaticBlock
| SEMICOLON
| SyntaxError SEMICOLON
;

ClassElement_Yield_Await :
  Modifiers_WithDeclare MethodDefinition_Yield_Await
| MethodDefinition_Yield_Await
| Modifiers_WithDeclare FieldDefinition_Yield_Await
| FieldDefinition_Yield_Await
| IndexSignature_WithDeclare SEMICOLON
| ClassStaticBlock
| SEMICOLON
| SyntaxError SEMICOLON
;

FieldDefinition :
  PropertyName QUEST TypeAnnotationopt Initializeropt_In SEMICOLON
| PropertyName EXCL TypeAnnotationopt Initializeropt_In SEMICOLON
| PropertyName TypeAnnotationopt Initializeropt_In SEMICOLON
;

FieldDefinition_Await :
  PropertyName_Await QUEST TypeAnnotationopt Initializeropt_In_Await SEMICOLON
| PropertyName_Await EXCL TypeAnnotationopt Initializeropt_In_Await SEMICOLON
| PropertyName_Await TypeAnnotationopt Initializeropt_In_Await SEMICOLON
;

FieldDefinition_Yield :
  PropertyName_Yield QUEST TypeAnnotationopt Initializeropt_In_Yield SEMICOLON
| PropertyName_Yield EXCL TypeAnnotationopt Initializeropt_In_Yield SEMICOLON
| PropertyName_Yield TypeAnnotationopt Initializeropt_In_Yield SEMICOLON
;

FieldDefinition_Yield_Await :
  PropertyName_Yield_Await QUEST TypeAnnotationopt Initializeropt_In_Yield_Await SEMICOLON
| PropertyName_Yield_Await EXCL TypeAnnotationopt Initializeropt_In_Yield_Await SEMICOLON
| PropertyName_Yield_Await TypeAnnotationopt Initializeropt_In_Yield_Await SEMICOLON
;

ClassStaticBlock :
  STATIC ClassStaticBlockBody
;

ClassStaticBlockBody :
  LBRACE /*.recoveryScope*/ StatementList_Await SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ StatementList_Await RBRACE
| LBRACE /*.recoveryScope*/ SyntaxError RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
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
| ImportRequireDeclaration
| ExportDeclaration
| StatementListItem
;

ImportDeclaration :
  IMPORT lookahead_notStartOfTypeImport ImportClause FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| IMPORT lookahead_notStartOfTypeImport ImportClause FromClause SEMICOLON
| IMPORT lookahead_StartOfTypeImport TYPE ImportClause FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| IMPORT lookahead_StartOfTypeImport TYPE ImportClause FromClause SEMICOLON
| IMPORT ModuleSpecifier /*.noLineBreak*/ AssertClause SEMICOLON
| IMPORT ModuleSpecifier SEMICOLON
;

// lookahead: StartOfTypeImport
lookahead_StartOfTypeImport :
  %empty
;

// lookahead: !StartOfTypeImport
lookahead_notStartOfTypeImport :
  %empty
;

AssertClause :
  ASSERT LBRACE AssertEntry_list_Comma_separated COMMA RBRACE
| ASSERT LBRACE AssertEntry_list_Comma_separated RBRACE
| ASSERT LBRACE RBRACE
;

AssertEntry_list_Comma_separated :
  AssertEntry_list_Comma_separated COMMA AssertEntry
| AssertEntry
;

AssertEntry :
  AssertionKey COLON STRINGLITERAL
;

AssertionKey :
  IdentifierName
| STRINGLITERAL
;

StartOfTypeImport :
  TYPE MULT
| TYPE LBRACE
| TYPE IdentifierName_WithoutFrom
;

ImportRequireDeclaration :
  EXPORT IMPORT lookahead_notStartOfTypeImport BindingIdentifier ASSIGN REQUIRE LPAREN ModuleSpecifier RPAREN SEMICOLON
| IMPORT lookahead_notStartOfTypeImport BindingIdentifier ASSIGN REQUIRE LPAREN ModuleSpecifier RPAREN SEMICOLON
;

ImportClause :
  ImportedDefaultBinding
| NameSpaceImport
| NamedImports
| ImportedDefaultBinding COMMA NameSpaceImport
| ImportedDefaultBinding COMMA NamedImports
;

ImportedDefaultBinding :
  ImportedBinding
;

NameSpaceImport :
  MULT AS ImportedBinding
;

FromClause :
  FROM ModuleSpecifier
;

NamedImport_list_Comma_separated :
  NamedImport_list_Comma_separated COMMA NamedImport
| NamedImport
;

NamedImports :
  LBRACE RBRACE
| LBRACE NamedImport_list_Comma_separated COMMA RBRACE
| LBRACE NamedImport_list_Comma_separated RBRACE
;

NamedImport :
  IdentifierNameRef
| IdentifierNameRef AS ImportedBinding
| ERROR
;

ModuleSpecifier :
  STRINGLITERAL
;

ImportedBinding :
  BindingIdentifier
;

ExportDeclaration :
  EXPORT TYPE MULT AS ImportedBinding FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| EXPORT TYPE MULT AS ImportedBinding FromClause SEMICOLON
| EXPORT TYPE MULT FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| EXPORT TYPE MULT FromClause SEMICOLON
| EXPORT MULT AS ImportedBinding FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| EXPORT MULT AS ImportedBinding FromClause SEMICOLON
| EXPORT MULT FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| EXPORT MULT FromClause SEMICOLON
| EXPORT TYPE ExportClause FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| EXPORT TYPE ExportClause FromClause SEMICOLON
| EXPORT ExportClause FromClause /*.noLineBreak*/ AssertClause SEMICOLON
| EXPORT ExportClause FromClause SEMICOLON
| EXPORT TYPE ExportClause SEMICOLON
| EXPORT ExportClause SEMICOLON
| EXPORT VariableStatement
| Modifiers EXPORT Declaration
| EXPORT Declaration
| EXPORT DEFAULT HoistableDeclaration
| Modifiers EXPORT DEFAULT ClassDeclaration
| EXPORT DEFAULT ClassDeclaration
| EXPORT DEFAULT AssignmentExpression_In_NoFuncClass SEMICOLON
| EXPORT ASSIGN AssignmentExpression_In_NoFuncClass SEMICOLON
| EXPORT AS NAMESPACE BindingIdentifier SEMICOLON
;

ExportClause :
  LBRACE RBRACE
| LBRACE ExportElement_list_Comma_separated COMMA RBRACE
| LBRACE ExportElement_list_Comma_separated RBRACE
;

ExportElement_list_Comma_separated :
  ExportElement_list_Comma_separated COMMA ExportElement
| ExportElement
;

ExportElement :
  IdentifierNameRef
| IdentifierNameRef AS IdentifierNameDecl
| ERROR
;

Decorator :
  ATSIGN DecoratorMemberExpression
| ATSIGN DecoratorCallExpression
;

DecoratorMemberExpression :
  IdentifierReference
| DecoratorMemberExpression DOT IdentifierName
;

DecoratorCallExpression :
  DecoratorMemberExpression Arguments
;

JSXChild_Await_optlist :
  JSXChild_Await_optlist JSXChild_Await
| %empty
;

JSXChild_Yield_Await_optlist :
  JSXChild_Yield_Await_optlist JSXChild_Yield_Await
| %empty
;

JSXChild_Yield_optlist :
  JSXChild_Yield_optlist JSXChild_Yield
| %empty
;

JSXChild_optlist :
  JSXChild_optlist JSXChild
| %empty
;

JSXElement :
  JSXSelfClosingElement
| JSXOpeningElement JSXChild_optlist JSXClosingElement
;

JSXElement_Await :
  JSXSelfClosingElement_Await
| JSXOpeningElement_Await JSXChild_Await_optlist JSXClosingElement
;

JSXElement_Yield :
  JSXSelfClosingElement_Yield
| JSXOpeningElement_Yield JSXChild_Yield_optlist JSXClosingElement
;

JSXElement_Yield_Await :
  JSXSelfClosingElement_Yield_Await
| JSXOpeningElement_Yield_Await JSXChild_Yield_Await_optlist JSXClosingElement
;

JSXAttribute_Await_optlist :
  JSXAttribute_Await_optlist JSXAttribute_Await
| %empty
;

JSXAttribute_Yield_Await_optlist :
  JSXAttribute_Yield_Await_optlist JSXAttribute_Yield_Await
| %empty
;

JSXAttribute_Yield_optlist :
  JSXAttribute_Yield_optlist JSXAttribute_Yield
| %empty
;

JSXAttribute_optlist :
  JSXAttribute_optlist JSXAttribute
| %empty
;

JSXSelfClosingElement :
  LT JSXElementName TypeArguments JSXAttribute_optlist DIV GT
| LT JSXElementName JSXAttribute_optlist DIV GT
;

JSXSelfClosingElement_Await :
  LT JSXElementName TypeArguments JSXAttribute_Await_optlist DIV GT
| LT JSXElementName JSXAttribute_Await_optlist DIV GT
;

JSXSelfClosingElement_Yield :
  LT JSXElementName TypeArguments JSXAttribute_Yield_optlist DIV GT
| LT JSXElementName JSXAttribute_Yield_optlist DIV GT
;

JSXSelfClosingElement_Yield_Await :
  LT JSXElementName TypeArguments JSXAttribute_Yield_Await_optlist DIV GT
| LT JSXElementName JSXAttribute_Yield_Await_optlist DIV GT
;

JSXOpeningElement :
  LT JSXElementName TypeArguments JSXAttribute_optlist GT
| LT JSXElementName JSXAttribute_optlist GT
;

JSXOpeningElement_Await :
  LT JSXElementName TypeArguments JSXAttribute_Await_optlist GT
| LT JSXElementName JSXAttribute_Await_optlist GT
;

JSXOpeningElement_Yield :
  LT JSXElementName TypeArguments JSXAttribute_Yield_optlist GT
| LT JSXElementName JSXAttribute_Yield_optlist GT
;

JSXOpeningElement_Yield_Await :
  LT JSXElementName TypeArguments JSXAttribute_Yield_Await_optlist GT
| LT JSXElementName JSXAttribute_Yield_Await_optlist GT
;

JSXClosingElement :
  LT DIV JSXElementName GT
;

JSXFragment :
  LT GT JSXChild_optlist LT DIV GT
;

JSXFragment_Await :
  LT GT JSXChild_Await_optlist LT DIV GT
;

JSXFragment_Yield :
  LT GT JSXChild_Yield_optlist LT DIV GT
;

JSXFragment_Yield_Await :
  LT GT JSXChild_Yield_Await_optlist LT DIV GT
;

JSXElementName :
  JSXIDENTIFIER
| JSXIDENTIFIER COLON JSXIDENTIFIER
| JSXMemberExpression
;

JSXMemberExpression :
  JSXIDENTIFIER DOT JSXIDENTIFIER
| JSXMemberExpression DOT JSXIDENTIFIER
;

JSXAttribute :
  JSXAttributeName ASSIGN JSXAttributeValue
| JSXAttributeName
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpression_In RBRACE
;

JSXAttribute_Await :
  JSXAttributeName ASSIGN JSXAttributeValue_Await
| JSXAttributeName
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpression_In_Await RBRACE
;

JSXAttribute_Yield :
  JSXAttributeName ASSIGN JSXAttributeValue_Yield
| JSXAttributeName
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpression_In_Yield RBRACE
;

JSXAttribute_Yield_Await :
  JSXAttributeName ASSIGN JSXAttributeValue_Yield_Await
| JSXAttributeName
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpression_In_Yield_Await RBRACE
;

JSXAttributeName :
  JSXIDENTIFIER
| JSXIDENTIFIER COLON JSXIDENTIFIER
;

JSXAttributeValue :
  JSXSTRINGLITERAL
| LBRACE /*.recoveryScope*/ AssignmentExpression_In RBRACE
| JSXElement
| JSXFragment
;

JSXAttributeValue_Await :
  JSXSTRINGLITERAL
| LBRACE /*.recoveryScope*/ AssignmentExpression_In_Await RBRACE
| JSXElement_Await
| JSXFragment_Await
;

JSXAttributeValue_Yield :
  JSXSTRINGLITERAL
| LBRACE /*.recoveryScope*/ AssignmentExpression_In_Yield RBRACE
| JSXElement_Yield
| JSXFragment_Yield
;

JSXAttributeValue_Yield_Await :
  JSXSTRINGLITERAL
| LBRACE /*.recoveryScope*/ AssignmentExpression_In_Yield_Await RBRACE
| JSXElement_Yield_Await
| JSXFragment_Yield_Await
;

JSXChild :
  JSXTEXT
| JSXElement
| JSXFragment
| LBRACE /*.recoveryScope*/ AssignmentExpressionopt_In RBRACE
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpressionopt_In RBRACE
;

JSXChild_Await :
  JSXTEXT
| JSXElement_Await
| JSXFragment_Await
| LBRACE /*.recoveryScope*/ AssignmentExpressionopt_In_Await RBRACE
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpressionopt_In_Await RBRACE
;

JSXChild_Yield :
  JSXTEXT
| JSXElement_Yield
| JSXFragment_Yield
| LBRACE /*.recoveryScope*/ AssignmentExpressionopt_In_Yield RBRACE
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpressionopt_In_Yield RBRACE
;

JSXChild_Yield_Await :
  JSXTEXT
| JSXElement_Yield_Await
| JSXFragment_Yield_Await
| LBRACE /*.recoveryScope*/ AssignmentExpressionopt_In_Yield_Await RBRACE
| LBRACE /*.recoveryScope*/ DOTDOTDOT AssignmentExpressionopt_In_Yield_Await RBRACE
;

Type :
  UnionOrIntersectionOrPrimaryType %prec RESOLVESHIFT
| UnionOrIntersectionOrPrimaryType EXTENDS Type_NoQuest QUEST Type COLON Type
| FunctionType
| ConstructorType
| AssertsType
| TypePredicate
;

Type_NoQuest :
  UnionOrIntersectionOrPrimaryType_NoQuest %prec RESOLVESHIFT
| FunctionType_NoQuest
| ConstructorType_NoQuest
| TypePredicate_NoQuest
;

TypePredicate :
  IdentifierNameRef_WithoutAsserts IS Type
| ASSERTS lookahead_StartOfIs IS Type_NoQuest
;

TypePredicate_NoQuest :
  IdentifierNameRef_WithoutAsserts IS Type_NoQuest
| ASSERTS lookahead_StartOfIs IS Type_NoQuest
;

// lookahead: StartOfIs
lookahead_StartOfIs :
  %empty
;

AssertsType :
  ASSERTS /*.noLineBreak*/ lookahead_notStartOfIs THIS IS Type
| ASSERTS /*.noLineBreak*/ lookahead_notStartOfIs THIS
| ASSERTS /*.noLineBreak*/ lookahead_notStartOfIs IdentifierName_WithoutKeywords_WithoutAs_WithoutSatisfies IS Type
| ASSERTS /*.noLineBreak*/ lookahead_notStartOfIs IdentifierName_WithoutKeywords_WithoutAs_WithoutSatisfies
;

// lookahead: !StartOfIs
lookahead_notStartOfIs :
  %empty
;

StartOfIs :
  IS
;

TypeParameter_list_Comma_separated :
  TypeParameter_list_Comma_separated COMMA TypeParameter
| TypeParameter
;

TypeParameters :
  LT TypeParameter_list_Comma_separated COMMA GT
| LT TypeParameter_list_Comma_separated GT
;

TypeParameter :
  BindingIdentifier Constraint ASSIGN Type
| BindingIdentifier Constraint
| BindingIdentifier ASSIGN Type
| BindingIdentifier
;

Constraint :
  EXTENDS Type
;

TypeArguments :
  LT Type_list_Comma_separated GT
;

Type_list_Comma_separated :
  Type_list_Comma_separated COMMA Type
| Type
;

UnionOrIntersectionOrPrimaryType :
  UnionOrIntersectionOrPrimaryType OR IntersectionOrPrimaryType
| OR IntersectionOrPrimaryType
| IntersectionOrPrimaryType %prec RESOLVESHIFT
;

UnionOrIntersectionOrPrimaryType_NoQuest :
  UnionOrIntersectionOrPrimaryType_NoQuest OR IntersectionOrPrimaryType_NoQuest
| OR IntersectionOrPrimaryType_NoQuest
| IntersectionOrPrimaryType_NoQuest %prec RESOLVESHIFT
;

IntersectionOrPrimaryType :
  IntersectionOrPrimaryType AND TypeOperator
| AND TypeOperator
| TypeOperator
;

IntersectionOrPrimaryType_NoQuest :
  IntersectionOrPrimaryType_NoQuest AND TypeOperator_NoQuest
| AND TypeOperator_NoQuest
| TypeOperator_NoQuest
;

TypeOperator :
  PrimaryType
| KEYOF TypeOperator
| UNIQUE TypeOperator
| READONLY TypeOperator
| INFER IdentifierName
;

TypeOperator_NoQuest :
  PrimaryType_NoQuest
| KEYOF TypeOperator_NoQuest
| UNIQUE TypeOperator_NoQuest
| READONLY TypeOperator_NoQuest
| INFER IdentifierName
;

PrimaryType :
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
| THIS
| PrimaryType /*.noLineBreak*/ EXCL
| PrimaryType /*.noLineBreak*/ QUEST
;

PrimaryType_NoQuest :
  ParenthesizedType
| PredefinedType
| TypeReference
| ObjectType
| MappedType
| ArrayType_NoQuest
| IndexedAccessType_NoQuest
| LiteralType
| TupleType
| TypeQuery
| ImportType
| THIS
| PrimaryType_NoQuest /*.noLineBreak*/ EXCL
;

ParenthesizedType :
  LPAREN lookahead_notStartOfFunctionType Type RPAREN
;

// lookahead: !StartOfFunctionType
lookahead_notStartOfFunctionType :
  %empty
;

LiteralType :
  STRINGLITERAL
| MINUS NUMERICLITERAL
| NUMERICLITERAL
| NULL
| TRUE
| FALSE
| TemplateLiteralType
;

PredefinedType :
  ANY
| UNKNOWN
| NUMBER
| BOOLEAN
| STRING
| SYMBOL
| VOID
| BIGINT
| UNDEFINED
| NEVER
| OBJECT
;

TypeReference :
  TypeName /*.noLineBreak*/ TypeArguments %prec RESOLVESHIFT
| TypeName /*.noLineBreak*/ %prec RESOLVESHIFT
;

TypeName :
  IdentifierReference_WithoutPredefinedTypes
| NamespaceName DOT IdentifierReference_WithDefault
;

NamespaceName :
  IdentifierReference
| NamespaceName DOT IdentifierReference_WithDefault
;

ObjectType :
  LBRACE /*.recoveryScope*/ lookahead_notStartOfMappedType TypeBody RBRACE
| LBRACE /*.recoveryScope*/ lookahead_notStartOfMappedType RBRACE
;

// lookahead: !StartOfMappedType
lookahead_notStartOfMappedType :
  %empty
;

TypeBody :
  TypeMemberList
| TypeMemberList COMMA
| TypeMemberList SEMICOLON
;

TypeMemberList :
  TypeMember
| TypeMemberList SEMICOLON TypeMember
| TypeMemberList COMMA TypeMember
;

TypeMember :
  PropertySignature
| MethodSignature
| CallSignature
| ConstructSignature
| IndexSignature
| AccessorSignature
| SyntaxError
;

ArrayType :
  PrimaryType /*.noLineBreak*/ LBRACK RBRACK
;

ArrayType_NoQuest :
  PrimaryType_NoQuest /*.noLineBreak*/ LBRACK RBRACK
;

IndexedAccessType :
  PrimaryType /*.noLineBreak*/ LBRACK Type RBRACK
;

IndexedAccessType_NoQuest :
  PrimaryType_NoQuest /*.noLineBreak*/ LBRACK Type_NoQuest RBRACK
;

StartOfMappedType :
  PLUS READONLY
| MINUS READONLY
| READONLY LBRACK IdentifierName IN
| LBRACK IdentifierName IN
;

MappedType :
  LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType PLUS READONLY LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType MINUS READONLY LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType READONLY LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type AS Type RBRACK TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK PLUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK MINUS QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK QUEST TypeAnnotation RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation SEMICOLON RBRACE
| LBRACE /*.recoveryScope*/ lookahead_StartOfMappedType LBRACK BindingIdentifier IN Type RBRACK TypeAnnotation RBRACE
;

// lookahead: StartOfMappedType
lookahead_StartOfMappedType :
  %empty
;

TupleElementType_list_Comma_separated :
  TupleElementType_list_Comma_separated COMMA TupleElementType
| TupleElementType
;

TupleType :
  LBRACK TupleElementType_list_Comma_separated COMMA RBRACK
| LBRACK TupleElementType_list_Comma_separated RBRACK
| LBRACK COMMA RBRACK
| LBRACK RBRACK
;

TupleElementType :
  lookahead_notStartOfTupleElementName Type
| lookahead_StartOfTupleElementName IdentifierName QUEST COLON Type
| lookahead_StartOfTupleElementName IdentifierName COLON Type
| DOTDOTDOT lookahead_notStartOfTupleElementName Type
| DOTDOTDOT lookahead_StartOfTupleElementName IdentifierName QUEST COLON Type
| DOTDOTDOT lookahead_StartOfTupleElementName IdentifierName COLON Type
;

// lookahead: StartOfTupleElementName
lookahead_StartOfTupleElementName :
  %empty
;

// lookahead: !StartOfTupleElementName
lookahead_notStartOfTupleElementName :
  %empty
;

StartOfTupleElementName :
  IdentifierName QUEST COLON
| IdentifierName COLON
;

StartOfFunctionType :
  Modifiers BindingIdentifier COLON
| Modifiers BindingIdentifier COMMA
| Modifiers BindingIdentifier QUEST
| Modifiers BindingIdentifier ASSIGN
| Modifiers BindingIdentifier RPAREN ASSIGNGT
| BindingIdentifier COLON
| BindingIdentifier COMMA
| BindingIdentifier QUEST
| BindingIdentifier ASSIGN
| BindingIdentifier RPAREN ASSIGNGT
| Modifiers BindingPattern COLON
| Modifiers BindingPattern COMMA
| Modifiers BindingPattern QUEST
| Modifiers BindingPattern ASSIGN
| Modifiers BindingPattern RPAREN ASSIGNGT
| BindingPattern COLON
| BindingPattern COMMA
| BindingPattern QUEST
| BindingPattern ASSIGN
| BindingPattern RPAREN ASSIGNGT
| DOTDOTDOT
| THIS COLON
| RPAREN
;

FunctionType :
  TypeParameters FunctionTypeParameterList ASSIGNGT Type
| FunctionTypeParameterList ASSIGNGT Type
;

FunctionType_NoQuest :
  TypeParameters FunctionTypeParameterList ASSIGNGT Type_NoQuest
| FunctionTypeParameterList ASSIGNGT Type_NoQuest
;

ConstructorType :
  ABSTRACT NEW TypeParameters ParameterList ASSIGNGT Type
| ABSTRACT NEW ParameterList ASSIGNGT Type
| NEW TypeParameters ParameterList ASSIGNGT Type
| NEW ParameterList ASSIGNGT Type
;

ConstructorType_NoQuest :
  ABSTRACT NEW TypeParameters ParameterList ASSIGNGT Type_NoQuest
| ABSTRACT NEW ParameterList ASSIGNGT Type_NoQuest
| NEW TypeParameters ParameterList ASSIGNGT Type_NoQuest
| NEW ParameterList ASSIGNGT Type_NoQuest
;

TypeQuery :
  TYPEOF TypeQueryExpression
;

ImportType :
  ImportTypeStart /*.noLineBreak*/ TypeArguments %prec RESOLVESHIFT
| ImportTypeStart /*.noLineBreak*/ %prec RESOLVESHIFT
;

ImportTypeStart :
  TYPEOF IMPORT LPAREN Type RPAREN
| IMPORT LPAREN Type RPAREN
| ImportTypeStart DOT IdentifierNameRef
;

TypeQueryExpression :
  IdentifierNameRef
| TypeQueryExpression DOT IdentifierNameRef
;

PropertySignature :
  Modifiers PropertyName_WithoutNew QUEST TypeAnnotation
| Modifiers PropertyName_WithoutNew QUEST
| Modifiers PropertyName_WithoutNew TypeAnnotation
| Modifiers PropertyName_WithoutNew
| PropertyName_WithoutNew QUEST TypeAnnotation
| PropertyName_WithoutNew QUEST
| PropertyName_WithoutNew TypeAnnotation
| PropertyName_WithoutNew
;

TypeAnnotation :
  COLON Type
;

FormalParameters :
  TypeParameters ParameterList TypeAnnotation
| TypeParameters ParameterList
| ParameterList TypeAnnotation
| ParameterList
;

FormalParameters_Await :
  TypeParameters ParameterList_Await TypeAnnotation
| TypeParameters ParameterList_Await
| ParameterList_Await TypeAnnotation
| ParameterList_Await
;

FormalParameters_Yield :
  TypeParameters ParameterList_Yield TypeAnnotation
| TypeParameters ParameterList_Yield
| ParameterList_Yield TypeAnnotation
| ParameterList_Yield
;

FormalParameters_Yield_Await :
  TypeParameters ParameterList_Yield_Await TypeAnnotation
| TypeParameters ParameterList_Yield_Await
| ParameterList_Yield_Await TypeAnnotation
| ParameterList_Yield_Await
;

CallSignature :
  TypeParameters ParameterList TypeAnnotation
| TypeParameters ParameterList
| ParameterList TypeAnnotation
| ParameterList
;

ParameterList :
  LPAREN Parameter_list_Comma_separated COMMA RPAREN
| LPAREN Parameter_list_Comma_separated RPAREN
| LPAREN COMMA RPAREN
| LPAREN RPAREN
;

ParameterList_Await :
  LPAREN Parameter_Await_list_Comma_separated COMMA RPAREN
| LPAREN Parameter_Await_list_Comma_separated RPAREN
| LPAREN COMMA RPAREN
| LPAREN RPAREN
;

ParameterList_Yield :
  LPAREN Parameter_Yield_list_Comma_separated COMMA RPAREN
| LPAREN Parameter_Yield_list_Comma_separated RPAREN
| LPAREN COMMA RPAREN
| LPAREN RPAREN
;

ParameterList_Yield_Await :
  LPAREN Parameter_Yield_Await_list_Comma_separated COMMA RPAREN
| LPAREN Parameter_Yield_Await_list_Comma_separated RPAREN
| LPAREN COMMA RPAREN
| LPAREN RPAREN
;

Parameter_Await_list_Comma_separated :
  Parameter_Await_list_Comma_separated COMMA Parameter_Await
| Parameter_Await
;

Parameter_Yield_Await_list_Comma_separated :
  Parameter_Yield_Await_list_Comma_separated COMMA Parameter_Yield_Await
| Parameter_Yield_Await
;

Parameter_Yield_list_Comma_separated :
  Parameter_Yield_list_Comma_separated COMMA Parameter_Yield
| Parameter_Yield
;

Parameter_list_Comma_separated :
  Parameter_list_Comma_separated COMMA Parameter
| Parameter
;

FunctionTypeParameterList :
  LPAREN lookahead_StartOfFunctionType Parameter_list_Comma_separated COMMA RPAREN
| LPAREN lookahead_StartOfFunctionType Parameter_list_Comma_separated RPAREN
| LPAREN lookahead_StartOfFunctionType COMMA RPAREN
| LPAREN lookahead_StartOfFunctionType RPAREN
;

// lookahead: StartOfFunctionType
lookahead_StartOfFunctionType :
  %empty
;

Parameter :
  Modifiers BindingIdentifier QUEST TypeAnnotation
| Modifiers BindingIdentifier QUEST
| Modifiers BindingIdentifier TypeAnnotation
| Modifiers BindingIdentifier
| BindingIdentifier QUEST TypeAnnotation
| BindingIdentifier QUEST
| BindingIdentifier TypeAnnotation
| BindingIdentifier
| Modifiers BindingPattern QUEST TypeAnnotation
| Modifiers BindingPattern QUEST
| Modifiers BindingPattern TypeAnnotation
| Modifiers BindingPattern
| BindingPattern QUEST TypeAnnotation
| BindingPattern QUEST
| BindingPattern TypeAnnotation
| BindingPattern
| Modifiers BindingIdentifier TypeAnnotation Initializer_In
| Modifiers BindingIdentifier Initializer_In
| BindingIdentifier TypeAnnotation Initializer_In
| BindingIdentifier Initializer_In
| Modifiers BindingPattern TypeAnnotation Initializer_In
| Modifiers BindingPattern Initializer_In
| BindingPattern TypeAnnotation Initializer_In
| BindingPattern Initializer_In
| DOTDOTDOT BindingIdentifier TypeAnnotation
| DOTDOTDOT BindingIdentifier
| DOTDOTDOT BindingPattern TypeAnnotation
| DOTDOTDOT BindingPattern
| THIS TypeAnnotation
| SyntaxError
;

Parameter_Await :
  Modifiers BindingIdentifier QUEST TypeAnnotation
| Modifiers BindingIdentifier QUEST
| Modifiers BindingIdentifier TypeAnnotation
| Modifiers BindingIdentifier
| BindingIdentifier QUEST TypeAnnotation
| BindingIdentifier QUEST
| BindingIdentifier TypeAnnotation
| BindingIdentifier
| Modifiers BindingPattern_Await QUEST TypeAnnotation
| Modifiers BindingPattern_Await QUEST
| Modifiers BindingPattern_Await TypeAnnotation
| Modifiers BindingPattern_Await
| BindingPattern_Await QUEST TypeAnnotation
| BindingPattern_Await QUEST
| BindingPattern_Await TypeAnnotation
| BindingPattern_Await
| Modifiers BindingIdentifier TypeAnnotation Initializer_In_Await
| Modifiers BindingIdentifier Initializer_In_Await
| BindingIdentifier TypeAnnotation Initializer_In_Await
| BindingIdentifier Initializer_In_Await
| Modifiers BindingPattern_Await TypeAnnotation Initializer_In_Await
| Modifiers BindingPattern_Await Initializer_In_Await
| BindingPattern_Await TypeAnnotation Initializer_In_Await
| BindingPattern_Await Initializer_In_Await
| DOTDOTDOT BindingIdentifier TypeAnnotation
| DOTDOTDOT BindingIdentifier
| DOTDOTDOT BindingPattern_Await TypeAnnotation
| DOTDOTDOT BindingPattern_Await
| THIS TypeAnnotation
| SyntaxError
;

Parameter_Yield :
  Modifiers BindingIdentifier QUEST TypeAnnotation
| Modifiers BindingIdentifier QUEST
| Modifiers BindingIdentifier TypeAnnotation
| Modifiers BindingIdentifier
| BindingIdentifier QUEST TypeAnnotation
| BindingIdentifier QUEST
| BindingIdentifier TypeAnnotation
| BindingIdentifier
| Modifiers BindingPattern_Yield QUEST TypeAnnotation
| Modifiers BindingPattern_Yield QUEST
| Modifiers BindingPattern_Yield TypeAnnotation
| Modifiers BindingPattern_Yield
| BindingPattern_Yield QUEST TypeAnnotation
| BindingPattern_Yield QUEST
| BindingPattern_Yield TypeAnnotation
| BindingPattern_Yield
| Modifiers BindingIdentifier TypeAnnotation Initializer_In_Yield
| Modifiers BindingIdentifier Initializer_In_Yield
| BindingIdentifier TypeAnnotation Initializer_In_Yield
| BindingIdentifier Initializer_In_Yield
| Modifiers BindingPattern_Yield TypeAnnotation Initializer_In_Yield
| Modifiers BindingPattern_Yield Initializer_In_Yield
| BindingPattern_Yield TypeAnnotation Initializer_In_Yield
| BindingPattern_Yield Initializer_In_Yield
| DOTDOTDOT BindingIdentifier TypeAnnotation
| DOTDOTDOT BindingIdentifier
| DOTDOTDOT BindingPattern_Yield TypeAnnotation
| DOTDOTDOT BindingPattern_Yield
| THIS TypeAnnotation
| SyntaxError
;

Parameter_Yield_Await :
  Modifiers BindingIdentifier QUEST TypeAnnotation
| Modifiers BindingIdentifier QUEST
| Modifiers BindingIdentifier TypeAnnotation
| Modifiers BindingIdentifier
| BindingIdentifier QUEST TypeAnnotation
| BindingIdentifier QUEST
| BindingIdentifier TypeAnnotation
| BindingIdentifier
| Modifiers BindingPattern_Yield_Await QUEST TypeAnnotation
| Modifiers BindingPattern_Yield_Await QUEST
| Modifiers BindingPattern_Yield_Await TypeAnnotation
| Modifiers BindingPattern_Yield_Await
| BindingPattern_Yield_Await QUEST TypeAnnotation
| BindingPattern_Yield_Await QUEST
| BindingPattern_Yield_Await TypeAnnotation
| BindingPattern_Yield_Await
| Modifiers BindingIdentifier TypeAnnotation Initializer_In_Yield_Await
| Modifiers BindingIdentifier Initializer_In_Yield_Await
| BindingIdentifier TypeAnnotation Initializer_In_Yield_Await
| BindingIdentifier Initializer_In_Yield_Await
| Modifiers BindingPattern_Yield_Await TypeAnnotation Initializer_In_Yield_Await
| Modifiers BindingPattern_Yield_Await Initializer_In_Yield_Await
| BindingPattern_Yield_Await TypeAnnotation Initializer_In_Yield_Await
| BindingPattern_Yield_Await Initializer_In_Yield_Await
| DOTDOTDOT BindingIdentifier TypeAnnotation
| DOTDOTDOT BindingIdentifier
| DOTDOTDOT BindingPattern_Yield_Await TypeAnnotation
| DOTDOTDOT BindingPattern_Yield_Await
| THIS TypeAnnotation
| SyntaxError
;

AccessibilityModifier :
  PUBLIC
| PRIVATE
| PROTECTED
;

ConstructSignature :
  Modifiers NEW TypeParameters ParameterList TypeAnnotation
| Modifiers NEW TypeParameters ParameterList
| Modifiers NEW ParameterList TypeAnnotation
| Modifiers NEW ParameterList
| NEW TypeParameters ParameterList TypeAnnotation
| NEW TypeParameters ParameterList
| NEW ParameterList TypeAnnotation
| NEW ParameterList
;

IndexSignature :
  Modifiers LBRACK IdentifierName COLON Type RBRACK TypeAnnotation
| LBRACK IdentifierName COLON Type RBRACK TypeAnnotation
;

IndexSignature_WithDeclare :
  Modifiers_WithDeclare LBRACK IdentifierName COLON Type RBRACK TypeAnnotation
| LBRACK IdentifierName COLON Type RBRACK TypeAnnotation
;

MethodSignature :
  Modifiers PropertyName_WithoutNew QUEST FormalParameters
| Modifiers PropertyName_WithoutNew FormalParameters
| PropertyName_WithoutNew QUEST FormalParameters
| PropertyName_WithoutNew FormalParameters
| Modifiers NEW QUEST FormalParameters
| NEW QUEST FormalParameters
;

AccessorSignature :
  Modifiers GET PropertyName LPAREN RPAREN TypeAnnotationopt
| GET PropertyName LPAREN RPAREN TypeAnnotationopt
| Modifiers SET PropertyName LPAREN PropertySetParameterList RPAREN
| SET PropertyName LPAREN PropertySetParameterList RPAREN
;

TypeAliasDeclaration :
  TYPE BindingIdentifier TypeParameters ASSIGN Type SEMICOLON
| TYPE BindingIdentifier ASSIGN Type SEMICOLON
;

InterfaceDeclaration :
  INTERFACE BindingIdentifier TypeParametersopt InterfaceExtendsClause ObjectType
| INTERFACE BindingIdentifier TypeParametersopt ObjectType
;

InterfaceExtendsClause :
  EXTENDS TypeReference_list_Comma_separated
;

EnumDeclaration :
  CONST ENUM BindingIdentifier EnumBody
| ENUM BindingIdentifier EnumBody
;

EnumBody :
  LBRACE /*.recoveryScope*/ EnumMember_list_Comma_separated COMMA RBRACE
| LBRACE /*.recoveryScope*/ EnumMember_list_Comma_separated RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

EnumMember_list_Comma_separated :
  EnumMember_list_Comma_separated COMMA EnumMember
| EnumMember
;

EnumMember :
  PropertyName
| PropertyName ASSIGN AssignmentExpression_In
| PropertyName ASSIGN BrokenExpr
;

NamespaceDeclaration :
  NAMESPACE IdentifierPath NamespaceBody
| MODULE IdentifierPath NamespaceBody
;

IdentifierPath :
  BindingIdentifier
| IdentifierPath DOT BindingIdentifier
;

NamespaceBody :
  LBRACE /*.recoveryScope*/ ModuleItemList RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

ImportAliasDeclaration :
  IMPORT lookahead_notStartOfTypeImport BindingIdentifier ASSIGN EntityName SEMICOLON
;

EntityName :
  NamespaceName
;

AmbientDeclaration :
  DECLARE AmbientVariableDeclaration
| DECLARE AmbientFunctionDeclaration
| DECLARE AmbientClassDeclaration
| DECLARE AmbientInterfaceDeclaration
| DECLARE AmbientEnumDeclaration
| DECLARE AmbientNamespaceDeclaration
| DECLARE AmbientModuleDeclaration
| DECLARE AmbientGlobalDeclaration
| DECLARE TypeAliasDeclaration
;

AmbientVariableDeclaration :
  VAR AmbientBindingList SEMICOLON
| LET AmbientBindingList SEMICOLON
| CONST AmbientBindingList SEMICOLON
;

AmbientBindingList :
  AmbientBinding
| AmbientBindingList COMMA AmbientBinding
;

AmbientBinding :
  BindingIdentifier TypeAnnotation Initializer_In
| BindingIdentifier TypeAnnotation
| BindingIdentifier Initializer_In
| BindingIdentifier
;

AmbientFunctionDeclaration :
  FUNCTION BindingIdentifier FormalParameters SEMICOLON
;

AmbientClassDeclaration :
  Modifiers CLASS BindingIdentifier TypeParametersopt ClassTail
| CLASS BindingIdentifier TypeParametersopt ClassTail
;

AmbientInterfaceDeclaration :
  Modifiers INTERFACE BindingIdentifier TypeParametersopt InterfaceExtendsClause ObjectType
| Modifiers INTERFACE BindingIdentifier TypeParametersopt ObjectType
| INTERFACE BindingIdentifier TypeParametersopt InterfaceExtendsClause ObjectType
| INTERFACE BindingIdentifier TypeParametersopt ObjectType
;

AmbientEnumDeclaration :
  CONST ENUM BindingIdentifier EnumBody
| ENUM BindingIdentifier EnumBody
;

AmbientNamespaceDeclaration :
  NAMESPACE IdentifierPath AmbientNamespaceBody
;

AmbientGlobalDeclaration :
  GLOBAL LBRACE /*.recoveryScope*/ ModuleBodyopt RBRACE
| GLOBAL SEMICOLON
;

AmbientNamespaceBody :
  LBRACE /*.recoveryScope*/ AmbientNamespaceElement_list RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

AmbientNamespaceElement_list :
  AmbientNamespaceElement_list AmbientNamespaceElement
| AmbientNamespaceElement
;

AmbientNamespaceElement :
  EXPORT AmbientVariableDeclaration
| AmbientVariableDeclaration
| EXPORT AmbientFunctionDeclaration
| AmbientFunctionDeclaration
| EXPORT AmbientClassDeclaration
| AmbientClassDeclaration
| EXPORT AmbientInterfaceDeclaration
| AmbientInterfaceDeclaration
| EXPORT AmbientEnumDeclaration
| AmbientEnumDeclaration
| EXPORT AmbientNamespaceDeclaration
| AmbientNamespaceDeclaration
| EXPORT AmbientModuleDeclaration
| AmbientModuleDeclaration
| EXPORT ImportAliasDeclaration
| ImportAliasDeclaration
| EXPORT TypeAliasDeclaration
| TypeAliasDeclaration
| EXPORT ExportClause SEMICOLON
;

AmbientModuleDeclaration :
  MODULE STRINGLITERAL AmbientModuleBody
| MODULE STRINGLITERAL SEMICOLON
| MODULE IdentifierPath AmbientModuleBody
| MODULE IdentifierPath SEMICOLON
;

AmbientModuleBody :
  LBRACE /*.recoveryScope*/ AmbientModuleElement_list RBRACE
| LBRACE /*.recoveryScope*/ RBRACE
;

AmbientModuleElement_list :
  AmbientModuleElement_list AmbientModuleElement
| AmbientModuleElement
;

AmbientModuleElement :
  ImportDeclaration
| EXPORT AmbientVariableDeclaration
| AmbientVariableDeclaration
| EXPORT AmbientFunctionDeclaration
| AmbientFunctionDeclaration
| EXPORT AmbientClassDeclaration
| AmbientClassDeclaration
| EXPORT AmbientInterfaceDeclaration
| AmbientInterfaceDeclaration
| EXPORT AmbientEnumDeclaration
| AmbientEnumDeclaration
| EXPORT AmbientNamespaceDeclaration
| AmbientNamespaceDeclaration
| EXPORT AmbientModuleDeclaration
| AmbientModuleDeclaration
| EXPORT ImportAliasDeclaration
| ImportAliasDeclaration
| EXPORT TypeAliasDeclaration
| TypeAliasDeclaration
| EXPORT ExportClause SEMICOLON
| EXPORT DEFAULT AssignmentExpression_In_NoFuncClass SEMICOLON
| EXPORT ASSIGN AssignmentExpression_In_NoFuncClass SEMICOLON
;

Elisionopt :
  Elision
| %empty
;

TypeAnnotationopt :
  TypeAnnotation
| %empty
;

Initializeropt :
  Initializer
| %empty
;

Initializeropt_Await :
  Initializer_Await
| %empty
;

Initializeropt_In :
  Initializer_In
| %empty
;

Initializeropt_In_Await :
  Initializer_In_Await
| %empty
;

Initializeropt_In_Yield :
  Initializer_In_Yield
| %empty
;

Initializeropt_In_Yield_Await :
  Initializer_In_Yield_Await
| %empty
;

Initializeropt_Yield :
  Initializer_Yield
| %empty
;

Initializeropt_Yield_Await :
  Initializer_Yield_Await
| %empty
;

BindingRestElementopt :
  BindingRestElement
| %empty
;

ElementElisionopt :
  ElementElision
| %empty
;

Expressionopt_In :
  Expression_In
| %empty
;

Expressionopt_In_Await :
  Expression_In_Await
| %empty
;

Expressionopt_In_Yield :
  Expression_In_Yield
| %empty
;

Expressionopt_In_Yield_Await :
  Expression_In_Yield_Await
| %empty
;

CaseClausesopt :
  CaseClauses
| %empty
;

CaseClausesopt_Await :
  CaseClauses_Await
| %empty
;

CaseClausesopt_Yield :
  CaseClauses_Yield
| %empty
;

CaseClausesopt_Yield_Await :
  CaseClauses_Yield_Await
| %empty
;

TypeParametersopt :
  TypeParameters
| %empty
;

ModuleBodyopt :
  ModuleBody
| %empty
;

AssignmentExpressionopt_In :
  AssignmentExpression_In
| %empty
;

AssignmentExpressionopt_In_Await :
  AssignmentExpression_In_Await
| %empty
;

AssignmentExpressionopt_In_Yield :
  AssignmentExpression_In_Yield
| %empty
;

AssignmentExpressionopt_In_Yield_Await :
  AssignmentExpression_In_Yield_Await
| %empty
;

%%

