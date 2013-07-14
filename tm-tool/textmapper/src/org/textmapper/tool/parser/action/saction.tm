#  syntax: lalr1 generator source grammar

#  Copyright 2002-2013 Evgeny Gryaznov
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

language saction(java);

lang = "java"
prefix = "SAction"
package = "org.textmapper.tool.parser.action"
positions = "offset"
endpositions = ""
lexerInput = "custom"
genCopyright = true

:: lexer

'{':	/\{/

_skip:	/'([^\n\\']|\\.)*'/		(space)
_skip:	/"([^\n\\"]|\\.)*"/		(space)
_skip:	/[^'"{}]+/		(space)

'}':	/\}/

:: parser

%input javaaction no-eoi;

javaaction ::=
	'{' command_tokensopt '}' ;

command_tokens ::=
	command_tokens command_token | command_token ;

command_token ::=
	'{' command_tokensopt '}'
;
