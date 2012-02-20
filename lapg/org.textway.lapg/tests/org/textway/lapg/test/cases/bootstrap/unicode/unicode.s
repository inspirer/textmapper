# lapg syntax file

lang = "java"
prefix = "UnicodeTest"
package = "org.textway.lapg.test.cases.bootstrap.unicode"

identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/   { $lexem = current(); break; }
icon(Integer):  /-?[0-9]+/                     { $lexem = Integer.parseInt(current()); break; }


schar = /[\w\p{Ll}]/
string(String): /"({schar})+"/			   { $lexem = current(); break; }
_skip:          /[\n\t\r ]+/                   { return false; }

# grammar
