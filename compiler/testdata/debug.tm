language model1(go);

lang = "model1"

:: lexer

Identifier: /[a-zA-Z_]+/    (class)

# Keywords.
'a':      /a/
'b':      /b/
'c':      /c/

# Punctuation
'{': /\{/
'}': /\}/
'(': /\(/
')': /\)/
'[': /\[/
']': /\]/
'+': /\+/
'.': /\./
',': /,/
':': /:/
'-': /-/
'->': /->/
'!': /!/

UNO:

:: parser

%input Simple;
%flag Foo;


Simple -> File:
    ('-' | '+')+ 'a' expr<+Foo> '(' (('-' | '+') separator ',' ',')* ')' ;

%interface Expr;

expr<Foo> -> Expr:
    Identifier  -> Identifier
  | '(' expr<~Foo> ')' -> Bar
  | [Foo] '{' (Identifier separator ',')+ '}' -> Init
  | delayed<+Async>
  | '->' delayed
;

%flag Async = true;

delayed<Foo, Async> -> Delayed:
      '.' '.' expr
    | [Async && !Foo || Foo] '(' expr<~Foo> ')' '->' expr
    | '->' '(' PropagationWrap<+IfFirst> ')' delayed
;

%lookahead flag IfFirst;

PropagationWrap :
      Propagation
    | '!' Propagation
	| Greedy
;

%left '+' '-';
%right UNO;

Propagation :
      '(' PropagationWrap ')'
    | [IfFirst] (Propagation | 'b') 'c'
    | Propagation '+' Propagation
    | Propagation '-' Propagation
    | Mod<~IfFirst>
    | '-' Propagation %prec UNO
;

Mod :
      'b' 'b'
    | [IfFirst] 'a' 'a'
;

Greedy:
	  'b' (Mod | 'b' .greedy)+ ;

%%

state 0 (input Simple, lr0 -> shift)
	Simple: _ Simple$1 'a' expr_Foo '(' Simple$2opt ')'
	Simple$1: _ Simple$1 '-'
	Simple$1: _ Simple$1 '+'
	Simple$1: _ '-'
	Simple$1: _ '+'

state 1 (from 0 on '+', lr0 -> reduce)
	Simple$1: '+' _ { reduce to Simple$1 }

state 2 (from 0 on '-', lr0 -> reduce)
	Simple$1: '-' _ { reduce to Simple$1 }

state 3 (from 0 on Simple$1, lr0 -> shift)
	Simple: Simple$1 _ 'a' expr_Foo '(' Simple$2opt ')'
	Simple$1: Simple$1 _ '-'
	Simple$1: Simple$1 _ '+'

state 4 (from 3 on 'a', lr0 -> shift)
	Simple: Simple$1 'a' _ expr_Foo '(' Simple$2opt ')'
	expr_Foo: _ Identifier
	expr_Foo: _ '(' expr ')'
	expr_Foo: _ '{' Identifier_list_Comma_separated '}'
	expr_Foo: _ delayed_Foo_Async
	expr_Foo: _ '->' delayed_Foo_Async
	delayed_Foo_Async: _ '.' '.' expr_Foo
	delayed_Foo_Async: _ '(' expr ')' '->' expr_Foo
	delayed_Foo_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Foo_Async

state 5 (from 3 on '+', lr0 -> reduce)
	Simple$1: Simple$1 '+' _ { reduce to Simple$1 }

state 6 (from 3 on '-', lr0 -> reduce)
	Simple$1: Simple$1 '-' _ { reduce to Simple$1 }

state 7 (from 4 on Identifier, lr0 -> reduce)
	expr_Foo: Identifier _ { reduce to expr_Foo }

state 8 (from 4 on '{', lr0 -> shift)
	Identifier_list_Comma_separated: _ Identifier_list_Comma_separated ',' Identifier
	Identifier_list_Comma_separated: _ Identifier
	expr_Foo: '{' _ Identifier_list_Comma_separated '}'

state 9 (from 4 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	expr_Foo: '(' _ expr ')'
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	delayed_Foo_Async: '(' _ expr ')' '->' expr_Foo

state 10 (from 4 on '.', lr0 -> shift)
	delayed_Foo_Async: '.' _ '.' expr_Foo

state 11 (from 4 on '->', lr0 -> shift)
	expr_Foo: '->' _ delayed_Foo_Async
	delayed_Foo_Async: _ '.' '.' expr_Foo
	delayed_Foo_Async: _ '(' expr ')' '->' expr_Foo
	delayed_Foo_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Foo_Async
	delayed_Foo_Async: '->' _ '(' PropagationWrap_IfFirst ')' delayed_Foo_Async

state 12 (from 4 on expr_Foo, lr0 -> shift)
	Simple: Simple$1 'a' expr_Foo _ '(' Simple$2opt ')'

state 13 (from 4 on delayed_Foo_Async, lr0 -> reduce)
	expr_Foo: delayed_Foo_Async _ { reduce to expr_Foo }

state 14 (from 8 on Identifier, lr0 -> reduce)
	Identifier_list_Comma_separated: Identifier _ { reduce to Identifier_list_Comma_separated }

state 15 (from 8 on Identifier_list_Comma_separated, lr0 -> shift)
	Identifier_list_Comma_separated: Identifier_list_Comma_separated _ ',' Identifier
	expr_Foo: '{' Identifier_list_Comma_separated _ '}'

state 16 (from 9 on Identifier, lr0 -> reduce)
	expr: Identifier _ { reduce to expr }

state 17 (from 9 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: '(' _ expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: '(' _ expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async

state 18 (from 9 on '.', lr0 -> shift)
	delayed_Async: '.' _ '.' expr

state 19 (from 9 on '->', lr0 -> shift)
	expr: '->' _ delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	delayed_Async: '->' _ '(' PropagationWrap_IfFirst ')' delayed_Async

state 20 (from 9 on expr, lr0 -> shift)
	expr_Foo: '(' expr _ ')'
	delayed_Foo_Async: '(' expr _ ')' '->' expr_Foo

state 21 (from 9 on delayed_Async, lr0 -> reduce)
	expr: delayed_Async _ { reduce to expr }

state 22 (from 10 on '.', lr0 -> shift)
	expr_Foo: _ Identifier
	expr_Foo: _ '(' expr ')'
	expr_Foo: _ '{' Identifier_list_Comma_separated '}'
	expr_Foo: _ delayed_Foo_Async
	expr_Foo: _ '->' delayed_Foo_Async
	delayed_Foo_Async: _ '.' '.' expr_Foo
	delayed_Foo_Async: '.' '.' _ expr_Foo
	delayed_Foo_Async: _ '(' expr ')' '->' expr_Foo
	delayed_Foo_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Foo_Async

state 23 (from 11 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	delayed_Foo_Async: '(' _ expr ')' '->' expr_Foo
	delayed_Foo_Async: '->' '(' _ PropagationWrap_IfFirst ')' delayed_Foo_Async
	PropagationWrap_IfFirst: _ Propagation_IfFirst
	PropagationWrap_IfFirst: _ '!' Propagation
	PropagationWrap_IfFirst: _ Greedy
	Propagation_IfFirst: _ '(' PropagationWrap ')'
	Propagation_IfFirst: _ Propagation_IfFirst 'c'
	Propagation_IfFirst: _ 'b' 'c'
	Propagation_IfFirst: _ Propagation_IfFirst '+' Propagation
	Propagation_IfFirst: _ Propagation_IfFirst '-' Propagation
	Propagation_IfFirst: _ Mod
	Propagation_IfFirst: _ '-' Propagation
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 24 (from 11 on '->', lr0 -> shift)
	delayed_Foo_Async: '->' _ '(' PropagationWrap_IfFirst ')' delayed_Foo_Async

state 25 (from 11 on delayed_Foo_Async, lr0 -> reduce)
	expr_Foo: '->' delayed_Foo_Async _ { reduce to expr_Foo }

state 26 (from 12 on '(')
	Simple: Simple$1 'a' expr_Foo '(' _ Simple$2opt ')'
	Simple$2: _ Simple$2 ',' ',' '-'
	Simple$2: _ Simple$2 ',' ',' '+'
	Simple$2: _ '-'
	Simple$2: _ '+'
	Simple$2opt: _ Simple$2
	Simple$2opt: _ { reduce to Simple$2opt }

state 27 (from 15 on '}', lr0 -> reduce)
	expr_Foo: '{' Identifier_list_Comma_separated '}' _ { reduce to expr_Foo }

state 28 (from 15 on ',', lr0 -> shift)
	Identifier_list_Comma_separated: Identifier_list_Comma_separated ',' _ Identifier

state 29 (from 17 on expr, lr0 -> shift)
	expr: '(' expr _ ')'
	delayed_Async: '(' expr _ ')' '->' expr

state 30 (from 18 on '.', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: '.' '.' _ expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async

state 31 (from 19 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: '(' _ expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	delayed_Async: '->' '(' _ PropagationWrap_IfFirst ')' delayed_Async
	PropagationWrap_IfFirst: _ Propagation_IfFirst
	PropagationWrap_IfFirst: _ '!' Propagation
	PropagationWrap_IfFirst: _ Greedy
	Propagation_IfFirst: _ '(' PropagationWrap ')'
	Propagation_IfFirst: _ Propagation_IfFirst 'c'
	Propagation_IfFirst: _ 'b' 'c'
	Propagation_IfFirst: _ Propagation_IfFirst '+' Propagation
	Propagation_IfFirst: _ Propagation_IfFirst '-' Propagation
	Propagation_IfFirst: _ Mod
	Propagation_IfFirst: _ '-' Propagation
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 32 (from 19 on '->', lr0 -> shift)
	delayed_Async: '->' _ '(' PropagationWrap_IfFirst ')' delayed_Async

state 33 (from 19 on delayed_Async, lr0 -> reduce)
	expr: '->' delayed_Async _ { reduce to expr }

state 34 (from 20 on ')')
	expr_Foo: '(' expr ')' _ { reduce to expr_Foo }
	delayed_Foo_Async: '(' expr ')' _ '->' expr_Foo

state 35 (from 22 on expr_Foo, lr0 -> reduce)
	delayed_Foo_Async: '.' '.' expr_Foo _ { reduce to delayed_Foo_Async }

state 36 (from 23 on 'b', lr0 -> shift)
	Propagation_IfFirst: 'b' _ 'c'
	Mod: _ 'b' 'b'
	Mod: 'b' _ 'b'
	Greedy: 'b' _ Greedy$1
	Greedy$1: _ Greedy$1 Mod
	Greedy$1: _ Greedy$1 'b' .greedy
	Greedy$1: _ Mod
	Greedy$1: _ 'b' .greedy

state 37 (from 23 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: '(' _ expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: '(' _ expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	PropagationWrap: _ Propagation
	PropagationWrap: _ '!' Propagation
	PropagationWrap: _ Greedy
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Propagation_IfFirst: '(' _ PropagationWrap ')'
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 38 (from 23 on '-', lr0 -> shift)
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Propagation_IfFirst: '-' _ Propagation
	Mod: _ 'b' 'b'

state 39 (from 23 on '!', lr0 -> shift)
	PropagationWrap_IfFirst: '!' _ Propagation
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Mod: _ 'b' 'b'

state 40 (from 23 on expr, lr0 -> shift)
	delayed_Foo_Async: '(' expr _ ')' '->' expr_Foo

state 41 (from 23 on PropagationWrap_IfFirst, lr0 -> shift)
	delayed_Foo_Async: '->' '(' PropagationWrap_IfFirst _ ')' delayed_Foo_Async

state 42 (from 23 on Propagation_IfFirst)
	PropagationWrap_IfFirst: Propagation_IfFirst _ { reduce to PropagationWrap_IfFirst }
	Propagation_IfFirst: Propagation_IfFirst _ 'c'
	Propagation_IfFirst: Propagation_IfFirst _ '+' Propagation
	Propagation_IfFirst: Propagation_IfFirst _ '-' Propagation

state 43 (from 23 on Mod, lr0 -> reduce)
	Propagation_IfFirst: Mod _ { reduce to Propagation_IfFirst }

state 44 (from 23 on Greedy, lr0 -> reduce)
	PropagationWrap_IfFirst: Greedy _ { reduce to PropagationWrap_IfFirst }

state 45 (from 24 on '(', lr0 -> shift)
	delayed_Foo_Async: '->' '(' _ PropagationWrap_IfFirst ')' delayed_Foo_Async
	PropagationWrap_IfFirst: _ Propagation_IfFirst
	PropagationWrap_IfFirst: _ '!' Propagation
	PropagationWrap_IfFirst: _ Greedy
	Propagation_IfFirst: _ '(' PropagationWrap ')'
	Propagation_IfFirst: _ Propagation_IfFirst 'c'
	Propagation_IfFirst: _ 'b' 'c'
	Propagation_IfFirst: _ Propagation_IfFirst '+' Propagation
	Propagation_IfFirst: _ Propagation_IfFirst '-' Propagation
	Propagation_IfFirst: _ Mod
	Propagation_IfFirst: _ '-' Propagation
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 46 (from 26 on '+', lr0 -> reduce)
	Simple$2: '+' _ { reduce to Simple$2 }

state 47 (from 26 on '-', lr0 -> reduce)
	Simple$2: '-' _ { reduce to Simple$2 }

state 48 (from 26 on Simple$2)
	Simple$2: Simple$2 _ ',' ',' '-'
	Simple$2: Simple$2 _ ',' ',' '+'
	Simple$2opt: Simple$2 _ { reduce to Simple$2opt }

state 49 (from 26 on Simple$2opt, lr0 -> shift)
	Simple: Simple$1 'a' expr_Foo '(' Simple$2opt _ ')'

state 50 (from 28 on Identifier, lr0 -> reduce)
	Identifier_list_Comma_separated: Identifier_list_Comma_separated ',' Identifier _ { reduce to Identifier_list_Comma_separated }

state 51 (from 29 on ')')
	expr: '(' expr ')' _ { reduce to expr }
	delayed_Async: '(' expr ')' _ '->' expr

state 52 (from 30 on expr, lr0 -> reduce)
	delayed_Async: '.' '.' expr _ { reduce to delayed_Async }

state 53 (from 31 on expr, lr0 -> shift)
	delayed_Async: '(' expr _ ')' '->' expr

state 54 (from 31 on PropagationWrap_IfFirst, lr0 -> shift)
	delayed_Async: '->' '(' PropagationWrap_IfFirst _ ')' delayed_Async

state 55 (from 32 on '(', lr0 -> shift)
	delayed_Async: '->' '(' _ PropagationWrap_IfFirst ')' delayed_Async
	PropagationWrap_IfFirst: _ Propagation_IfFirst
	PropagationWrap_IfFirst: _ '!' Propagation
	PropagationWrap_IfFirst: _ Greedy
	Propagation_IfFirst: _ '(' PropagationWrap ')'
	Propagation_IfFirst: _ Propagation_IfFirst 'c'
	Propagation_IfFirst: _ 'b' 'c'
	Propagation_IfFirst: _ Propagation_IfFirst '+' Propagation
	Propagation_IfFirst: _ Propagation_IfFirst '-' Propagation
	Propagation_IfFirst: _ Mod
	Propagation_IfFirst: _ '-' Propagation
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 56 (from 34 on '->', lr0 -> shift)
	expr_Foo: _ Identifier
	expr_Foo: _ '(' expr ')'
	expr_Foo: _ '{' Identifier_list_Comma_separated '}'
	expr_Foo: _ delayed_Foo_Async
	expr_Foo: _ '->' delayed_Foo_Async
	delayed_Foo_Async: _ '.' '.' expr_Foo
	delayed_Foo_Async: _ '(' expr ')' '->' expr_Foo
	delayed_Foo_Async: '(' expr ')' '->' _ expr_Foo
	delayed_Foo_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Foo_Async

state 57 (from 36 on 'b', lr0 -> reduce)
	Greedy$1: 'b' .greedy _ { reduce to Greedy$1 }

	Dropped by .greedy:
	  Mod: 'b' _ 'b'
	  Mod: 'b' 'b' _

state 58 (from 36 on 'c', lr0 -> reduce)
	Propagation_IfFirst: 'b' 'c' _ { reduce to Propagation_IfFirst }

state 59 (from 36 on Mod, lr0 -> reduce)
	Greedy$1: Mod _ { reduce to Greedy$1 }

state 60 (from 36 on Greedy$1)
	Mod: _ 'b' 'b'
	Greedy: 'b' Greedy$1 _ { reduce to Greedy }
	Greedy$1: Greedy$1 _ Mod
	Greedy$1: Greedy$1 _ 'b' .greedy

state 61 (from 37 on 'b', lr0 -> shift)
	Mod: _ 'b' 'b'
	Mod: 'b' _ 'b'
	Greedy: 'b' _ Greedy$1
	Greedy$1: _ Greedy$1 Mod
	Greedy$1: _ Greedy$1 'b' .greedy
	Greedy$1: _ Mod
	Greedy$1: _ 'b' .greedy

state 62 (from 37 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: '(' _ expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: '(' _ expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	PropagationWrap: _ Propagation
	PropagationWrap: _ '!' Propagation
	PropagationWrap: _ Greedy
	Propagation: _ '(' PropagationWrap ')'
	Propagation: '(' _ PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 63 (from 37 on '-', lr0 -> shift)
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Propagation: '-' _ Propagation
	Mod: _ 'b' 'b'

state 64 (from 37 on '!', lr0 -> shift)
	PropagationWrap: '!' _ Propagation
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Mod: _ 'b' 'b'

state 65 (from 37 on PropagationWrap, lr0 -> shift)
	Propagation_IfFirst: '(' PropagationWrap _ ')'

state 66 (from 37 on Propagation)
	PropagationWrap: Propagation _ { reduce to PropagationWrap }
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation

state 67 (from 37 on Mod, lr0 -> reduce)
	Propagation: Mod _ { reduce to Propagation }

state 68 (from 37 on Greedy, lr0 -> reduce)
	PropagationWrap: Greedy _ { reduce to PropagationWrap }

state 69 (from 38 on 'b', lr0 -> shift)
	Mod: 'b' _ 'b'

state 70 (from 38 on '(', lr0 -> shift)
	PropagationWrap: _ Propagation
	PropagationWrap: _ '!' Propagation
	PropagationWrap: _ Greedy
	Propagation: _ '(' PropagationWrap ')'
	Propagation: '(' _ PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 71 (from 38 on Propagation)
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation
	Propagation_IfFirst: '-' Propagation _ { reduce to Propagation_IfFirst }

state 72 (from 39 on Propagation)
	PropagationWrap_IfFirst: '!' Propagation _ { reduce to PropagationWrap_IfFirst }
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation

state 73 (from 40 on ')', lr0 -> shift)
	delayed_Foo_Async: '(' expr ')' _ '->' expr_Foo

state 74 (from 41 on ')', lr0 -> shift)
	delayed_Foo_Async: _ '.' '.' expr_Foo
	delayed_Foo_Async: _ '(' expr ')' '->' expr_Foo
	delayed_Foo_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Foo_Async
	delayed_Foo_Async: '->' '(' PropagationWrap_IfFirst ')' _ delayed_Foo_Async

state 75 (from 42 on 'c', lr0 -> reduce)
	Propagation_IfFirst: Propagation_IfFirst 'c' _ { reduce to Propagation_IfFirst }

state 76 (from 42 on '+', lr0 -> shift)
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Propagation_IfFirst: Propagation_IfFirst '+' _ Propagation
	Mod: _ 'b' 'b'

state 77 (from 42 on '-', lr0 -> shift)
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Propagation_IfFirst: Propagation_IfFirst '-' _ Propagation
	Mod: _ 'b' 'b'

state 78 (from 45 on '(', lr0 -> shift)
	PropagationWrap: _ Propagation
	PropagationWrap: _ '!' Propagation
	PropagationWrap: _ Greedy
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Propagation_IfFirst: '(' _ PropagationWrap ')'
	Mod: _ 'b' 'b'
	Greedy: _ 'b' Greedy$1

state 79 (from 48 on ',', lr0 -> shift)
	Simple$2: Simple$2 ',' _ ',' '-'
	Simple$2: Simple$2 ',' _ ',' '+'

state 80 (from 49 on ')', lr0 -> reduce)
	Simple: Simple$1 'a' expr_Foo '(' Simple$2opt ')' _ { reduce to Simple }

state 81 (from 51 on '->', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: '(' expr ')' '->' _ expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async

state 82 (from 53 on ')', lr0 -> shift)
	delayed_Async: '(' expr ')' _ '->' expr

state 83 (from 54 on ')', lr0 -> shift)
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	delayed_Async: '->' '(' PropagationWrap_IfFirst ')' _ delayed_Async

state 84 (from 56 on expr_Foo, lr0 -> reduce)
	delayed_Foo_Async: '(' expr ')' '->' expr_Foo _ { reduce to delayed_Foo_Async }

state 85 (from 60 on 'b', lr0 -> reduce)
	Greedy$1: Greedy$1 'b' .greedy _ { reduce to Greedy$1 }

	Dropped by .greedy:
	  Mod: 'b' _ 'b'

state 86 (from 60 on Mod, lr0 -> reduce)
	Greedy$1: Greedy$1 Mod _ { reduce to Greedy$1 }

state 87 (from 62 on PropagationWrap, lr0 -> shift)
	Propagation: '(' PropagationWrap _ ')'

state 88 (from 63 on Propagation)
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation
	Propagation: '-' Propagation _ { reduce to Propagation }

state 89 (from 64 on Propagation)
	PropagationWrap: '!' Propagation _ { reduce to PropagationWrap }
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation

state 90 (from 65 on ')', lr0 -> reduce)
	Propagation_IfFirst: '(' PropagationWrap ')' _ { reduce to Propagation_IfFirst }

state 91 (from 66 on '+', lr0 -> shift)
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: Propagation '+' _ Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Mod: _ 'b' 'b'

state 92 (from 66 on '-', lr0 -> shift)
	Propagation: _ '(' PropagationWrap ')'
	Propagation: _ Propagation '+' Propagation
	Propagation: _ Propagation '-' Propagation
	Propagation: Propagation '-' _ Propagation
	Propagation: _ Mod
	Propagation: _ '-' Propagation
	Mod: _ 'b' 'b'

state 93 (from 69 on 'b', lr0 -> reduce)
	Mod: 'b' 'b' _ { reduce to Mod }

state 94 (from 74 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async
	delayed_Foo_Async: '(' _ expr ')' '->' expr_Foo

state 95 (from 74 on delayed_Foo_Async, lr0 -> reduce)
	delayed_Foo_Async: '->' '(' PropagationWrap_IfFirst ')' delayed_Foo_Async _ { reduce to delayed_Foo_Async }

state 96 (from 76 on Propagation)
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation
	Propagation_IfFirst: Propagation_IfFirst '+' Propagation _ { reduce to Propagation_IfFirst }

state 97 (from 77 on Propagation)
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation
	Propagation_IfFirst: Propagation_IfFirst '-' Propagation _ { reduce to Propagation_IfFirst }

state 98 (from 79 on ',', lr0 -> shift)
	Simple$2: Simple$2 ',' ',' _ '-'
	Simple$2: Simple$2 ',' ',' _ '+'

state 99 (from 81 on expr, lr0 -> reduce)
	delayed_Async: '(' expr ')' '->' expr _ { reduce to delayed_Async }

state 100 (from 83 on '(', lr0 -> shift)
	expr: _ Identifier
	expr: _ '(' expr ')'
	expr: _ delayed_Async
	expr: _ '->' delayed_Async
	delayed_Async: _ '.' '.' expr
	delayed_Async: _ '(' expr ')' '->' expr
	delayed_Async: '(' _ expr ')' '->' expr
	delayed_Async: _ '->' '(' PropagationWrap_IfFirst ')' delayed_Async

state 101 (from 83 on delayed_Async, lr0 -> reduce)
	delayed_Async: '->' '(' PropagationWrap_IfFirst ')' delayed_Async _ { reduce to delayed_Async }

state 102 (from 87 on ')', lr0 -> reduce)
	Propagation: '(' PropagationWrap ')' _ { reduce to Propagation }

state 103 (from 91 on Propagation)
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation '+' Propagation _ { reduce to Propagation }
	Propagation: Propagation _ '-' Propagation

state 104 (from 92 on Propagation)
	Propagation: Propagation _ '+' Propagation
	Propagation: Propagation _ '-' Propagation
	Propagation: Propagation '-' Propagation _ { reduce to Propagation }

state 105 (from 98 on '+', lr0 -> reduce)
	Simple$2: Simple$2 ',' ',' '+' _ { reduce to Simple$2 }

state 106 (from 98 on '-', lr0 -> reduce)
	Simple$2: Simple$2 ',' ',' '-' _ { reduce to Simple$2 }

state 107 (from 0 on Simple, lr0 -> shift)

state 108 (final Simple, from 107 on eoi, lr0 -> shift)

