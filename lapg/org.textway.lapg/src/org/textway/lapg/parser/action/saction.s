# lapg syntax file

lang = "java"
prefix = "SAction"
package = "org.textway.lapg.parser.action"

'{':	/\{/

_skip:	/'([^\n\\']|\\.)*'/
_skip:	/"([^\n\\"]|\\.)*"/
_skip:	/[^'"{}]+/

'}':	/\}/

