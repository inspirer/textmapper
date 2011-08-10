# lapg syntax file

lang = "java"
prefix = "SAction"
package = "org.textway.lapg.parser.action"
positions = "offset"
endpositions = ""
lexerInput = "char"


'{':	/\{/

_skip:	/'([^\n\\']|\\.)*'/
_skip:	/"([^\n\\"]|\\.)*"/
_skip:	/[^'"{}]+/

'}':	/\}/

