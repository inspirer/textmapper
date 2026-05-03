language debug_conflicts(go);

:: lexer
'i': /if/
'e': /else/
'x': /x/
'+': /\+/

:: parser
%left '+';
%expect 1;

input : expr ;
expr : s | expr '+' expr ;

s : 'i' s
  | 'i' s 'e' s
  | 'x' ;

%%

Unresolved conflicts:
debug_conflicts.tm:16:5: input: 'i' s
shift/reduce conflict (next: 'e')
    s : 'i' s
        followed by: s: 'i' s 'e' _ s

Resolved by precedence:
debug_conflicts.tm:14:12: input: expr '+' expr
resolved as reduce conflict (next: '+')
    expr : expr '+' expr
        followed by: expr: expr '+' _ expr

debug_conflicts.tm:1:1: conflicts: 1 shift/reduce and 0 reduce/reduce
