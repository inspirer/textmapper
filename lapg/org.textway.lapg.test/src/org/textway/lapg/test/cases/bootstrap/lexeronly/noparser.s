# lapg syntax file

lang = "java"
prefix = "Noparser"
package = "org.textway.lapg.test.cases.bootstrap.lexeronly"

'{':	/\{/

_skip:	/'([^\n\\']|\\.)*'/
_skip:	/"([^\n\\"]|\\.)*"/
_skip:	/[^'"{}]+/

'}':	/\}/

