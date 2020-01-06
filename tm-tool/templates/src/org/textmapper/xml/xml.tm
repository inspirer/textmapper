#  syntax: xml grammar

#  Copyright 2002-2020 Evgeny Gryaznov
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

language xml(java);

lang = "java" 
prefix = "Xml"
package = "org.textmapper.xml"
maxtoken = 2048
breaks = true
gentree = true
positions = "line,offset"
endpositions = "offset"
genCopyright = true

:: lexer

%x inTag;

any:	/[^<]+/

'<':	/</  { state = States.inTag; }

_skipcomment:   /<!--([^-]|-[^-]|--[^>])*-->/ (space)

<inTag> {
  identifier {String}:	/[a-zA-Z_][A-Za-z_0-9-]*/		{ $$ = tokenText(); }
  ccon {String}:	/"[^\n"]*"/							{ $$ = tokenText().substring(1, tokenSize()-1); }
  ccon {String}:	/'[^\n']*'/							{ $$ = tokenText().substring(1, tokenSize()-1); }

  '>':	    />/  { state = States.initial; }
  '=':		/=/
  ':':		/:/
  '/':		/\//

  _skip:      /[\t\r\n ]+/  (space)
}

:: parser

input {XmlNode} :
	xml_tags										{ $$ = new XmlNode("<root>", null, 1); ${left()}.setData($xml_tags); }
;

xml_tags {List<XmlElement>} :
	xml_tags xml_tag_or_space 						{ $xml_tags.add($xml_tag_or_space); }
	| xml_tag_or_space 								{ $$ = new ArrayList<XmlElement>(); ${left()}.add($xml_tag_or_space); }
;

xml_tag_or_space {XmlElement} :
	tag_start tag_end								{ checkTag($tag_start,$tag_end,${tag_end.offset},${tag_end.endoffset},${tag_end.line}); }
	| tag_start xml_tags tag_end					{ checkTag($tag_start,$tag_end,${tag_end.offset},${tag_end.endoffset},${tag_end.line}); $tag_start.setData($xml_tags); }
	| no_body_tag
	| any											{ $$ = getData(${left().offset},${left().endoffset}); }
;

tag_name {String} :
	identifier										{ $$ = $identifier; }
	| ns=identifier ':' identifier						{ $$ = $ns + ":" + $identifier; }
;

tag_start {XmlNode} :
	'<' tag_name attributesopt '>'		            { $$ = new XmlNode($tag_name, $attributesopt, ${first().line}); }
;

no_body_tag {XmlNode} :
	'<' tag_name attributesopt '/' '>'		        { $$ = new XmlNode($tag_name, $attributesopt, ${first().line}); }
;

tag_end {String} :
	'<' '/' tag_name '>'		                    { $$ = $tag_name; }
;

attributes {List<XmlAttribute>} :
	attributes attribute							{ $attributes.add($attribute); }
	| attribute 									{ $$ = new ArrayList<XmlAttribute>(); ${left()}.add($attribute); }
;

attribute {XmlAttribute} :
	identifier '=' ccon								{ $$ = new XmlAttribute($identifier,$ccon); }
;


##################################################################################

%%

${template java.imports-}
${call base-}
import java.util.ArrayList;
import java.util.List;
${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
${end}


${template java.classcode}
${call base-}

org.textmapper.xml.XmlTree.@TextSource source;

private XmlData getData(int start, int end) {
	return new XmlData(source.getContents(), start, end-start);
}

private void checkTag(XmlNode node, String endTag, int offset, int endoffset, int line) {
	if (!node.getTagName().equals(endTag))
		reporter.error("Tag " + node.getTagName() + " is closed with " + endTag, line, offset, endoffset);
}
${end}
