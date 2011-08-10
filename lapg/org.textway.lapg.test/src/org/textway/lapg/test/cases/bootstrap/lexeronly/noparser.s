# lapg syntax file

lang = "java"
prefix = "Noparser"
package = "org.textway.lapg.test.cases.bootstrap.lexeronly"
lexerInput = "char"

'{':	/\{/

_skip:	/'([^\n\\']|\\.)*'/
_skip:	/"([^\n\\"]|\\.)*"/
_skip:	/[^'"{}]+/

'}':	/\}/

