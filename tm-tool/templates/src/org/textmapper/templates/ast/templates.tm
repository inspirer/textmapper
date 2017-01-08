#  syntax: lapg templates source grammar

#  Copyright 2002-2016 Evgeny Gryaznov
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

language templates(java);

lang = "java" 
prefix = "Templates"
package = "org.textmapper.templates.ast"
breaks = true
gentree = true
positions = "line,offset"
endpositions = "offset"
genCopyright = true
genCleanup = true

:: lexer

[initial]

any:	/[^$]+/

escdollar:		/$$/
escid{String}:	/$[a-zA-Z_][A-Za-z_0-9]*(#[0-9]+)?/	{ $$ = tokenText().substring(1, tokenSize()); }
escint{Integer}:/$[0-9]+/							{ $$ = Integer.parseInt(tokenText().substring(1, tokenSize())); }

'${':	/$\{/ => query		{ deep = 1;}
'$/':   /$\//

[query]

identifier{String}:	/[a-zA-Z$_][A-Za-z$_0-9]*/ (class) { $$ = tokenText(); }

icon{Integer}:	/[0-9]+/							{ $$ = Integer.parseInt(tokenText()); }
ccon{String}:	/'([^\n\\']|\\(['"?\\abfnrtv]|x[0-9a-fA-F]+|[0-7]([0-7][0-7]?)?))*'/	{ $$ = unescape(tokenText(), 1, tokenSize()-1); }

Lcall:		/call/
Lcached:	/cached/
Lcase:		/case/
Lend:		/end/
Lelse:		/else/
Leval:		/eval/
Lfalse:		/false/
Lfor:		/for/
Lfile:		/file/
Lforeach:	/foreach/
Lgrep:		/grep/
Lif:		/if/
Lin:		/in/
Limport:	/import/
Lis:		/is/
Lmap:		/map/
Lnew:		/new/
Lnull:		/null/
Lquery:		/query/
Lswitch:	/switch/
Lseparator:	/separator/
Ltemplate:  /template/
Ltrue:		/true/
Lself:		/self/
Lassert:	/assert/

'{':		/\{/		{ deep++; }
'}':		/\}/		{ if (--deep == 0) { state = 0; } }
'-}':		/-\}/ => initial
'+':		/+/
'-':		/-/
'*':		/\*/
'/':		/\//
'%':		/%/
'!':		/!/
'|':		/\|/
'[':		/\[/
']':		/\]/
'(':		/\(/
')':		/\)/
'.':		/\./
',':		/,/
'&&':		/&&/
'||':		/\|\|/
'==':		/==/
'=':		/=/
'!=':		/!=/
'->':		/->/
'=>':		/=>/
'<=':		/<=/
'>=':		/>=/
'<':		/</
'>':		/>/
':':		/:/
'?':		/?/

_skip:      /[\t\r\n ]+/	(space)

error:

:: parser

input {List<org.textmapper.templates.bundle.@IBundleEntity>} ::=
	definitionsopt
;

definitions {List<org.textmapper.templates.bundle.@IBundleEntity>} ::=
	  definition									{ $$ = new ArrayList(); if ($definition != null) ${left()}.add($definition); }
	| definitions definition						{ if ($definition != null) $definitions.add($definition); }
;

definition {org.textmapper.templates.bundle.@IBundleEntity} ::=
	  template_def
	| query_def
	| any											{ $$ = null; }
;

template_def {TemplateNode} ::=
	template_start instructions template_end		{ $template_start.setInstructions($instructions); }
	| template_start template_end
;

query_def {QueryNode} ::=
	'${' cached_flagopt Lquery qualified_id parametersopt '=' expression '}'
                                                    { $$ = new QueryNode($qualified_id, $parametersopt, templatePackage, $expression, $cached_flagopt != null, source, ${left().offset}, ${left().endoffset}); checkFqn($qualified_id, ${left().offset}, ${left().endoffset}, ${first().line}); }
;

cached_flag {Boolean} ::=
	Lcached											{ $$ = Boolean.TRUE; }
;

template_start {TemplateNode} ::=
	'${' Ltemplate qualified_id parametersopt '[-]}'
                                                    { $$ = new TemplateNode($qualified_id, $parametersopt, templatePackage, source, ${left().offset}, ${left().endoffset}); checkFqn($qualified_id, ${left().offset}, ${left().endoffset}, ${first().line}); }
; 

parameters {List<ParameterNode>} ::=
	'(' parameter_listopt ')' 						{ $$ = $1; }
;

parameter_list {List<ParameterNode>} ::=
	  identifier                       				{ $$ = new ArrayList(); ${left()}.add(new ParameterNode($identifier, source, ${identifier.offset}, ${left().endoffset})); }
	| parameter_list ',' identifier                 { $parameter_list.add(new ParameterNode($identifier, source, ${identifier.offset}, ${left().endoffset})); }
;

template_end ::=
	'${' Lend '}' ;

instructions {ArrayList<Node>} ::=
	instructions instruction						{ if ($instruction != null) $instructions.add($instruction); }
	| instruction 									{ $$ = new ArrayList<Node>(); if ($instruction!=null) ${left()}.add($instruction); }
;

'[-]}' ::=
	'-}'											{ skipSpaces(${first().offset}+1); }
	| '}'
;

instruction {Node} ::=
	  control_instruction
	| switch_instruction
	| simple_instruction
	| escid											{ $$ = createEscapedId($escid, ${left().offset}, ${left().endoffset}); }
	| escint										{ $$ = new IndexNode(null, new LiteralNode($escint, source, ${left().offset}, ${left().endoffset}), source, ${left().offset}, ${left().endoffset}); }
	| escdollar										{ $$ = new DollarNode(source, ${left().offset}, ${left().endoffset}); }
	| any											{ $$ = new TextNode(source, rawText(${left().offset}, ${left().endoffset}), ${left().endoffset}); }
;

simple_instruction {Node} ::=
	'${' sentence '[-]}' 							{ $$ = $1; } 
;

sentence {Node} ::=
	  expression
	| Lcall qualified_id template_argumentsopt template_for_expropt
													{ $$ = new CallTemplateNode($qualified_id, $template_argumentsopt, $template_for_expropt, templatePackage, true, source, ${left().offset}, ${left().endoffset}); }
	| Leval conditional_expression comma_expropt	{ $$ = new EvalNode($conditional_expression, $comma_expropt, source, ${left().offset}, ${left().endoffset}); }
	| Lassert expression							{ $$ = new AssertNode($expression, source, ${left().offset}, ${left().endoffset}); }
	| syntax_problem								{ $$ = null; }
;

comma_expr {ExpressionNode} ::=
	',' conditional_expression						{ $$ = $conditional_expression; }
;

qualified_id {String} ::=
	identifier
	| qualified_id '.' identifier					{ $$ = $qualified_id + "." + $identifier; }
;

template_for_expr {ExpressionNode} ::=
	Lfor expression									{ $$ = $1; }
;

template_arguments {ArrayList} ::=
	'(' expression_listopt ')'						{ $$ = $1; } 
;

control_instruction {CompoundNode} ::=
	control_start instructions else_clause 			{ $control_start.setInstructions($instructions); applyElse($control_start,$else_clause, ${left().offset}, ${left().endoffset}, ${left().line}); }
;

else_clause {ElseIfNode} ::=
	  '${' Lelse Lif expression '[-]}' instructions else_clause
	  												{ $$ = new ElseIfNode($expression, $instructions, $else_clause, source, ${first().offset}, ${instructions.endoffset}); }
	| '${' Lelse '[-]}' instructions control_end
													{ $$ = new ElseIfNode(null, $instructions, null, source, ${first().offset}, ${instructions.endoffset}); }
	| control_end
													{ $$ = null; }
;   

switch_instruction {CompoundNode} ::=
	'${' Lswitch expression '[-]}' anyopt
           case_list ('${' Lelse '[-]}' instructions)? control_end
													{ $$ = new SwitchNode($expression, $case_list, $instructions, source, ${left().offset},${left().endoffset}); checkIsSpace(${anyopt.offset},${anyopt.endoffset}, ${anyopt.line}); }
;

case_list {ArrayList} ::=
	one_case										{ $$ = new ArrayList(); ${left()}.add($one_case); }
	| case_list one_case                            { $case_list.add($one_case); }
	| case_list instruction                         { CaseNode.add($case_list, $instruction); }
;

one_case {CaseNode} ::=
	'${' Lcase expression '[-]}' 					{ $$ = new CaseNode($expression, source, ${left().offset}, ${left().endoffset}); }
;

control_start {CompoundNode} ::=
	'${' control_sentence '[-]}' 					{ $$ = $1; } ;

control_sentence {CompoundNode} ::=
	  Lforeach identifier Lin expression separator_expropt
                                                    { $$ = new ForeachNode($identifier, $expression, null, $separator_expropt, source, ${left().offset}, ${left().endoffset}); }
	| Lfor identifier Lin '[' start=conditional_expression ',' end=conditional_expression ']' separator_expropt
													{ $$ = new ForeachNode($identifier, $start, $end, $separator_expropt, source, ${left().offset}, ${left().endoffset}); }
	| Lif expression								{ $$ = new IfNode($expression, source, ${left().offset}, ${left().endoffset}); }
	| Lfile expression								{ $$ = new FileNode($expression, source, ${left().offset}, ${left().endoffset}); }
;

separator_expr {ExpressionNode} ::=
    Lseparator expression                           { $$ = $1; }
;

control_end ::=
	'${' Lend '[-]}'
	| '$/'
;

primary_expression {ExpressionNode} ::=
  	  identifier									{ $$ = new SelectNode(null, $identifier, source, ${left().offset}, ${left().endoffset}); }
    | '(' expression ')'							{ $$ = new ParenthesesNode($1, source, ${left().offset}, ${left().endoffset}); }
	| icon 											{ $$ = new LiteralNode($0, source, ${left().offset}, ${left().endoffset}); }
	| bcon                                          { $$ = new LiteralNode($0, source, ${left().offset}, ${left().endoffset}); }
	| ccon 											{ $$ = new LiteralNode($0, source, ${left().offset}, ${left().endoffset}); }
  	| Lself											{ $$ = new ThisNode(source, ${left().offset}, ${left().endoffset}); }
  	| Lnull											{ $$ = new LiteralNode(null, source, ${left().offset}, ${left().endoffset}); }
    | identifier '(' expression_listopt ')'         { $$ = new MethodCallNode(null, $identifier, $expression_listopt, source, ${left().offset}, ${left().endoffset}); }
    | primary_expression '.' identifier				{ $$ = new SelectNode($primary_expression, $identifier, source, ${left().offset}, ${left().endoffset}); }
    | primary_expression '.' identifier '(' expression_listopt ')'   
    												{ $$ = new MethodCallNode($primary_expression, $identifier, $expression_listopt, source, ${left().offset}, ${left().endoffset}); }
    | primary_expression '.' identifier '(' var=identifier '|' expression ')'
    												{ $$ = createCollectionProcessor($primary_expression, $identifier, $var, $expression, source, ${left().offset}, ${left().endoffset}, ${left().line}); }
    | primary_expression '->' qualified_id '(' expression_listopt ')'
    												{ $$ = new CallTemplateNode($qualified_id, $expression_listopt, $primary_expression, templatePackage, false, source, ${left().offset}, ${left().endoffset}); }
    | primary_expression '->' '(' expression ')' '(' expression_listopt ')'  
    												{ $$ = new CallTemplateNode($expression,$expression_listopt,$primary_expression,templatePackage, source, ${left().offset}, ${left().endoffset}); }
    | primary_expression '[' expression ']'			{ $$ = new IndexNode($primary_expression, $expression, source, ${left().offset}, ${left().endoffset}); }
    | complex_data
    | closure
;

closure ::=
	  '{' cached_flagopt parameter_listopt '=>' expression '}'
                                                    { $$ = new ClosureNode($cached_flagopt != null, $parameter_listopt, $expression, source, ${left().offset}, ${left().endoffset}); }
;

complex_data {ExpressionNode} ::=
	'[' expression_listopt ']'						{ $$ = new ListNode($expression_listopt, source, ${left().offset}, ${left().endoffset}); }
    | '[' map_entries ']'							{ $$ = new ConcreteMapNode($map_entries, source, ${left().offset}, ${left().endoffset}); }
    | Lnew qualified_id '(' map_entriesopt ')'		{ $$ = new CreateClassNode($qualified_id, $map_entriesopt, source, ${left().offset}, ${left().endoffset}); }
 ;

map_entries {java.util.@Map<String,ExpressionNode>} ::=
	identifier map_separator conditional_expression
													{ $$ = new java.util.@LinkedHashMap(); ${left()}.put($identifier, $conditional_expression); }
	| map_entries ',' identifier map_separator conditional_expression
													{ $map_entries.put($identifier, $conditional_expression); }
;

map_separator ::=
	':' | '=' | '=>' ;

bcon {Boolean} ::=
	Ltrue 											{ $$ = Boolean.TRUE; }
	| Lfalse										{ $$ = Boolean.FALSE; }
;

unary_expression {ExpressionNode} ::=
	primary_expression
	| '!' unary_expression							{ $$ = new UnaryExpression(UnaryExpression.NOT, $unary_expression, source, ${left().offset}, ${left().endoffset}); }
	| '-' unary_expression							{ $$ = new UnaryExpression(UnaryExpression.MINUS, $unary_expression, source, ${left().offset}, ${left().endoffset}); }
;

%left '||';
%left '&&';
%left '>' '<' '<=' '>=';
%left '-' '+';
%left '*' '/' '%';

binary_op {ExpressionNode} ::=
	unary_expression
	| left=binary_op '*' right=binary_op						{ $$ = new ArithmeticNode(ArithmeticNode.MULT, $left, $right, source, ${left().offset}, ${left().endoffset}); }
	| left=binary_op '/' right=binary_op						{ $$ = new ArithmeticNode(ArithmeticNode.DIV, $left, $right, source, ${left().offset}, ${left().endoffset}); }
	| left=binary_op '%' right=binary_op						{ $$ = new ArithmeticNode(ArithmeticNode.REM, $left, $right, source, ${left().offset}, ${left().endoffset}); }
	| left=binary_op '+' right=binary_op						{ $$ = new ArithmeticNode(ArithmeticNode.PLUS, $left, $right, source, ${left().offset}, ${left().endoffset}); }
	| left=binary_op '-' right=binary_op						{ $$ = new ArithmeticNode(ArithmeticNode.MINUS, $left, $right, source, ${left().offset}, ${left().endoffset}); }
    | left=binary_op '<' right=binary_op						{ $$ = new ConditionalNode(ConditionalNode.LT, $left, $right, source, ${left().offset}, ${left().endoffset}); }
    | left=binary_op '>' right=binary_op						{ $$ = new ConditionalNode(ConditionalNode.GT, $left, $right, source, ${left().offset}, ${left().endoffset}); }
    | left=binary_op '<=' right=binary_op 						{ $$ = new ConditionalNode(ConditionalNode.LE, $left, $right, source, ${left().offset}, ${left().endoffset}); }
    | left=binary_op '>=' right=binary_op 						{ $$ = new ConditionalNode(ConditionalNode.GE, $left, $right, source, ${left().offset}, ${left().endoffset}); }
;

instanceof_expression {ExpressionNode} ::=
	  binary_op
	| instanceof_expression Lis qualified_id		{ $$ = new InstanceOfNode($instanceof_expression, $qualified_id, source, ${left().offset}, ${left().endoffset}); }
	| instanceof_expression Lis ccon				{ $$ = new InstanceOfNode($instanceof_expression, $ccon, source, ${left().offset}, ${left().endoffset}); }
;

equality_expression {ExpressionNode} ::=
      instanceof_expression
    | equality_expression '==' instanceof_expression { $$ = new ConditionalNode(ConditionalNode.EQ, $equality_expression, $instanceof_expression, source, ${left().offset}, ${left().endoffset}); }
    | equality_expression '!=' instanceof_expression { $$ = new ConditionalNode(ConditionalNode.NE, $equality_expression, $instanceof_expression, source, ${left().offset}, ${left().endoffset}); }
;

conditional_op {ExpressionNode} ::=
      equality_expression
    | left=conditional_op '&&' right=conditional_op			{ $$ = new ConditionalNode(ConditionalNode.AND, $left, $right, source, ${left().offset}, ${left().endoffset}); }
    | left=conditional_op '||' right=conditional_op			{ $$ = new ConditionalNode(ConditionalNode.OR, $left, $right, source, ${left().offset}, ${left().endoffset}); }
;

conditional_expression {ExpressionNode} ::=
    conditional_op
  | conditional_op '?' then=conditional_expression ':' else=conditional_expression
  													{ $$ = new TriplexNode($conditional_op, $then, $else, source, ${left().offset}, ${left().endoffset}); }
;

assignment_expression {ExpressionNode} ::=
	conditional_expression
  | identifier '=' conditional_expression			{ $$ = new AssignNode($identifier, $conditional_expression, source, ${left().offset}, ${left().endoffset}); }
;

expression {ExpressionNode} ::=
	assignment_expression
  | expression ',' assignment_expression			{ $$ = new CommaNode($expression, $assignment_expression, source, ${left().offset}, ${left().endoffset}); }
;

expression_list {ArrayList} ::=
	conditional_expression							{ $$ = new ArrayList(); ${left()}.add($conditional_expression); }
	| expression_list ',' conditional_expression	{ $expression_list.add($conditional_expression); }
;

body {TemplateNode} ::=
	instructions
						{
							$$ = new TemplateNode("inline", null, templatePackage, source, ${left().offset}, ${left().endoffset});
							${left()}.setInstructions($instructions);
						}
;

syntax_problem ::=
	error ;

%input input, body;

##################################################################################
%%
${template java.imports}
import java.util.ArrayList;
import java.util.List;
${end}

${template java_tree.parseParameters-}
${call base}, String templatePackage${end}

${template java_tree.createParser-}
${call base-}
parser.source = source;
parser.templatePackage = templatePackage;
${end}

${template java_lexer.lexercode}
private int deep = 0;

private String unescape(String s, int start, int end) {
	StringBuilder sb = new StringBuilder();
	end = Math.min(end, s.length());
	for(int i = start; i < end; i++) {
		char c = s.charAt(i);
		if(c == '\\') {
			if(++i == end) {
				break;
			}
			c = s.charAt(i);
			if(c == 'u' || c == 'x') {
				// FIXME process unicode
			} else if(c == 'n') {
				sb.append('\n');
			} else if(c == 'r') {
				sb.append('\r');
			} else if(c == 't') {
				sb.append('\t');
			} else {
				sb.append(c);
			}
		} else {
			sb.append(c);
		}
	}
	return sb.toString();
}
${end}

${template java.classcode}
${call base-}
org.textmapper.templates.ast.TemplatesTree.@TextSource source;
String templatePackage;

private int killEnds = -1;

private int rawText(int start, final int end) {
	CharSequence buffer = source.getContents();
	if (killEnds == start) {
		while (start < end && (buffer.charAt(start) == '\t' || buffer.charAt(start) == ' '))
			start++;

		if (start < end && buffer.charAt(start) == '\r')
			start++;

		if (start < end && buffer.charAt(start) == '\n')
			start++;
	}
	return start;
}

private void checkIsSpace(int start, int end, int line) {
	String val = source.getText(rawText(start, end), end).trim();
	if (val.length() > 0) {
		reporter.error("Unknown text ignored: `" + val + "`", line, start, end);
	}
}

private void applyElse(CompoundNode node, ElseIfNode elseNode, int offset, int endoffset, int line) {
	if (elseNode == null) {
		return;
	}
	if (node instanceof IfNode) {
		((IfNode)node).applyElse(elseNode);
	} else {
		reporter.error("Unknown else node, instructions skipped", line, offset, endoffset);
	}
}

private ExpressionNode createCollectionProcessor(ExpressionNode context, String instruction, String varName, ExpressionNode foreachExpr, org.textmapper.templates.ast.TemplatesTree.@TextSource source, int offset, int endoffset, int line) {
	char first = instruction.charAt(0);
	int kind = 0;
	switch(first) {
	case 'c':
		if(instruction.equals("collect")) {
			kind = CollectionProcessorNode.COLLECT;
		} else if(instruction.equals("collectUnique")) {
			kind = CollectionProcessorNode.COLLECTUNIQUE;
		}
		break;
	case 'r':
		if(instruction.equals("reject")) {
			kind = CollectionProcessorNode.REJECT;
		}
		break;
	case 'm':
		if(instruction.equals("max")) {
			kind = CollectionProcessorNode.MAX;
		}
		break;
	case 's':
		if(instruction.equals("select")) {
			kind = CollectionProcessorNode.SELECT;
		} else if(instruction.equals("sort")) {
			kind = CollectionProcessorNode.SORT;
		}
		break;
	case 'f':
		if(instruction.equals("forAll")) {
			kind = CollectionProcessorNode.FORALL;
		}
		break;
	case 'e':
		if(instruction.equals("exists")) {
			kind = CollectionProcessorNode.EXISTS;
		}
		break;
	case 'g':
		if(instruction.equals("groupBy")) {
			kind = CollectionProcessorNode.GROUPBY;
		}
		break;
	}
	if (kind == 0) {
		reporter.error("unknown collection processing instruction: " + instruction, line, offset, endoffset);
		return new ErrorNode(source, offset, endoffset);
	}
	return new CollectionProcessorNode(context, kind, varName, foreachExpr, source, offset, endoffset);
}

private Node createEscapedId(String escid, int offset, int endoffset) {
	int sharp = escid.indexOf('#');
	if (sharp >= 0) {
		Integer index = new Integer(escid.substring(sharp+1));
		escid = escid.substring(0, sharp);
		return new IndexNode(new SelectNode(null,escid,source,offset,endoffset), new LiteralNode(index,source,offset,endoffset),source,offset,endoffset);
	
	} else {
		return new SelectNode(null,escid,source,offset,endoffset);
	}
}

private void skipSpaces(int offset) {
	killEnds = offset+1;
}

private void checkFqn(String templateName, int offset, int endoffset, int line) {
	if (templateName.indexOf('.') >= 0 && templatePackage != null) {
		reporter.error("template name should be simple identifier", line, offset, endoffset);
	}
}
${end}

