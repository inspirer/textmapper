%{
%}

%start Test
%start Decl1

%token INVALID_TOKEN
%token WHITESPACE
%token SINGLELINECOMMENT
%token IDENTIFIER
%token INTEGERCONSTANT
%token LASTINT
%token TEST
%token DECL1
%token DECL2
%token LBRACE
%token RBRACE
%token LPAREN
%token RPAREN
%token LBRACK
%token RBRACK
%token DOT
%token COMMA
%token COLON
%token MINUS
%token MINUSGT
%token SHARPATID
%token ZFOO
%token BACKTRACKINGTOKEN
%token ERROR
%token MULTILINECOMMENT

%%

Declaration_list :
  Declaration_list Declaration
| Declaration
;

Test :
  Declaration_list
;

Declaration :
  Decl1
| Decl2
| LBRACE MINUS MINUS Declaration_list RBRACE
| LBRACE MINUS MINUS RBRACE
| LBRACE MINUS Declaration_list RBRACE
| LBRACE MINUS RBRACE
| LBRACE Declaration_list RBRACE
| LBRACE RBRACE
| LASTINT
| INTEGERCONSTANT LBRACK RBRACK
			{
        switch  {{nn0, _ := rhs[0].value.(int)}}nn0 {
        case 7:
          p.listener(Int7, rhs[0].sym.offset, rhs[2].sym.endoffset)
        case 9:
          p.listener(Int9, rhs[0].sym.offset, rhs[2].sym.endoffset)
        }
      }
| INTEGERCONSTANT
			{
        switch  {{nn0, _ := rhs[0].value.(int)}}nn0 {
        case 7:
          p.listener(Int7, rhs[0].sym.offset, rhs[0].sym.endoffset)
        case 9:
          p.listener(Int9, rhs[0].sym.offset, rhs[0].sym.endoffset)
        }
      }
| TEST LBRACE setof_not_LparenLpareneoiSpaceOrSpaceAposDotAposRparenSpaceOrSpaceAposRbraceAposRparen_optlist RBRACE
| TEST LPAREN empty1 RPAREN
| TEST INTEGERCONSTANT
;

setof_not_LparenLpareneoiSpaceOrSpaceAposDotAposRparenSpaceOrSpaceAposRbraceAposRparen :
  INVALID_TOKEN
| WHITESPACE
| SINGLELINECOMMENT
| IDENTIFIER
| INTEGERCONSTANT
| LASTINT
| TEST
| DECL1
| DECL2
| LBRACE
| LPAREN
| RPAREN
| LBRACK
| RBRACK
| COMMA
| COLON
| MINUS
| MINUSGT
| SHARPATID
| ZFOO
| BACKTRACKINGTOKEN
| ERROR
| MULTILINECOMMENT
;

setof_not_LparenLpareneoiSpaceOrSpaceAposDotAposRparenSpaceOrSpaceAposRbraceAposRparen_optlist :
  %empty
| setof_not_LparenLpareneoiSpaceOrSpaceAposDotAposRparenSpaceOrSpaceAposRbraceAposRparen_optlist setof_not_LparenLpareneoiSpaceOrSpaceAposDotAposRparenSpaceOrSpaceAposRbraceAposRparen
;

empty1 :
  %empty
;

QualifiedName :
  IDENTIFIER
| QualifiedName DOT IDENTIFIER
;

Decl1 :
  DECL1 LPAREN QualifiedName RPAREN
;

Decl2 :
  DECL2
;

%%

