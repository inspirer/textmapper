#  syntax: xml grammar
#
#  Lapg (Lexer and Parser Generator)
#  Copyright 2002-2010 Evgeny Gryaznov
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

lang = "java" 
prefix = "Xml"
package = "org.textway.xml"
maxtoken = 2048
breaks = "on"
gentree = "on"
positions = "line,offset"
endpositions = "offset"

# Vocabulary

[0]

any:	/[^<]+/

'<':	/</         { group = 1; break; }

_skipcomment:   /<!--([^-]|-[^-]|--[^>])*-->/		{ return false; }

[1]

identifier(String):	/[a-zA-Z_][A-Za-z_0-9-]*/		{ $lexem = current(); break; }
ccon(String):	/"[^\n"]*"/							{ $lexem = token.toString().substring(1, token.length()-1); break; }
ccon(String):	/'[^\n']*'/

'>':	    />/          { group = 0; break; }
'=':		/=/
':':		/:/
'/':		/\//

_skip:      /[\t\r\n ]+/    { return false; }

# Grammar

input (XmlNode) ::=
	xml_tags										{ $$ = new XmlNode("<root>", null, 1); $input.setData($xml_tags); }
;

xml_tags (List<XmlElement>) ::=
	xml_tags xml_tag_or_space 						{ $xml_tags#0.add($xml_tag_or_space); }
	| xml_tag_or_space 								{ $$ = new ArrayList<XmlElement>(); $xml_tags.add($xml_tag_or_space); }
;

xml_tag_or_space (XmlElement) ::=
	tag_start tag_end								{ checkTag($tag_start,$tag_end,${tag_end.offset},${tag_end.endoffset},${tag_end.line}); }
	| tag_start xml_tags tag_end					{ checkTag($tag_start,$tag_end,${tag_end.offset},${tag_end.endoffset},${tag_end.line}); $tag_start.setData($xml_tags); }
	| no_body_tag
	| any											{ $$ = getData(${self[0].offset},${self[0].endoffset}); }
;

tag_name (String) ::=
	identifier										{ $$ = $identifier; }
	| identifier ':' identifier						{ $$ = $identifier#0 + ":" + $identifier#1; }
;

tag_start (XmlNode) ::=
	'<' tag_name attributesopt '>'		            { $$ = new XmlNode($tag_name, $attributesopt, ${self[0].line}); }
;

no_body_tag (XmlNode) ::=
	'<' tag_name attributesopt '/' '>'		        { $$ = new XmlNode($tag_name, $attributesopt, ${self[0].line}); }
;

tag_end (String) ::=
	'<' '/' tag_name '>'		                    { $$ = $tag_name; }
;

attributes (List<XmlAttribute>) ::=
	attributes attribute							{ $attributes#0.add($attribute); }
	| attribute 									{ $$ = new ArrayList<XmlAttribute>(); $attributes.add($attribute); }
;

attribute (XmlAttribute) ::=
	identifier '=' ccon								{ $$ = new XmlAttribute($identifier,$ccon); }
;


##################################################################################

%%

${template java.imports-}
${call base-}
import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;
${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
${end}


${template java.classcode}
${call base-}

org.textway.xml.XmlTree.@TextSource source;

private XmlData getData(int start, int end) {
	return new XmlData(source.getContents(), start, end-start);
}

private void checkTag(XmlNode node, String endTag, int offset, int endoffset, int line) {
	if (!node.getTagName().equals(endTag))
		reporter.error(offset, endoffset, line, "Tag " + node.getTagName() + " is closed with " + endTag);
}
${end}
