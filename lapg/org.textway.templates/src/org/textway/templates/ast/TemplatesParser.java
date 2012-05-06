/**
 * Copyright 2002-2012 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.templates.ast;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.textway.templates.ast.TemplatesLexer.ErrorReporter;
import org.textway.templates.ast.TemplatesLexer.LapgSymbol;
import org.textway.templates.ast.TemplatesLexer.Lexems;
import org.textway.templates.ast.TemplatesTree.TextSource;
import org.textway.templates.bundle.IBundleEntity;

public class TemplatesParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public TemplatesParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	String templatePackage;

	private int killEnds = -1;

	private int rawText(int start, final int end) {
		char[] buff = source.getContents();
		if( killEnds == start ) {
			while( start < end && (buff[start] == '\t' || buff[start] == ' ') )
				start++;

			if( start < end && buff[start] == '\r' )
				start++;

			if( start < end && buff[start] == '\n' )
				start++;
		}
		return start;
	}

	private void checkIsSpace(int start, int end, int line) {
		String val = source.getText(rawText(start,end),end).trim();
		if( val.length() > 0 )
			reporter.error(start, end, line, "Unknown text ignored: `"+val+"`");
	}

	private void applyElse(CompoundNode node, ElseIfNode elseNode, int offset, int endoffset, int line) {
		if (elseNode == null ) {
			return;
		}
		if (node instanceof IfNode) {
			((IfNode)node).applyElse(elseNode);
		} else {
			reporter.error(offset, endoffset, line, "Unknown else node, instructions skipped");
		}
	}

	private ExpressionNode createMapCollect(ExpressionNode context, String instruction, String varName, ExpressionNode key, ExpressionNode value, TextSource source, int offset, int endoffset, int line) {
		if(!instruction.equals("collect")) {
			reporter.error(offset, endoffset, line, "unknown collection processing instruction: " + instruction);
			return new ErrorNode(source, offset, endoffset);
		}
		return new CollectMapNode(context, varName, key, value, source, offset, endoffset);
	}

	private ExpressionNode createCollectionProcessor(ExpressionNode context, String instruction, String varName, ExpressionNode foreachExpr, TextSource source, int offset, int endoffset, int line) {
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
		if(kind == 0) {
			reporter.error(offset, endoffset, line, "unknown collection processing instruction: " + instruction);
			return new ErrorNode(source, offset, endoffset);
		}
		return new CollectionProcessorNode(context, kind, varName, foreachExpr, source, offset, endoffset);
	}

	private Node createEscapedId(String escid, int offset, int endoffset) {
		int sharp = escid.indexOf('#');
		if( sharp >= 0 ) {
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
		if( templateName.indexOf('.') >= 0 && templatePackage != null) {
			reporter.error(offset, endoffset, line, "template name should be simple identifier");
		}
	}
	private static final int[] lapg_action = TemplatesLexer.unpack_int(275,
		"\ufffd\uffff\uffff\uffff\7\0\ufff5\uffff\uffed\uffff\3\0\5\0\6\0\uffff\uffff\2\0" +
		"\46\0\45\0\43\0\44\0\uffff\uffff\uffe5\uffff\35\0\42\0\40\0\41\0\uffff\uffff\21\0" +
		"\uffff\uffff\13\0\uffff\uffff\4\0\uffff\uffff\11\0\uffff\uffff\uffd7\uffff\122\0" +
		"\124\0\uffff\uffff\uffff\uffff\155\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\126\0\uffff\uffff\154\0\125\0\uffff\uffff\uff9f\uffff\uffff\uffff\uffff" +
		"\uffff\uff97\uffff\uffff\uffff\215\0\uffff\uffff\uffff\uffff\uff79\uffff\140\0\137" +
		"\0\123\0\161\0\uff45\uffff\uff17\uffff\ufefb\uffff\ufee1\uffff\206\0\210\0\ufecb" +
		"\uffff\62\0\34\0\uffff\uffff\64\0\ufec3\uffff\uffff\uffff\uffff\uffff\10\0\ufeb7" +
		"\uffff\uffff\uffff\ufe99\uffff\ufe8d\uffff\ufe57\uffff\uffff\uffff\ufe4f\uffff\uffff" +
		"\uffff\ufe47\uffff\uffff\uffff\uffff\uffff\ufe3f\uffff\ufe37\uffff\160\0\157\0\ufe31" +
		"\uffff\uffff\uffff\212\0\ufdff\uffff\uffff\uffff\uffff\uffff\37\0\36\0\47\0\106\0" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\117\0\72" +
		"\0\75\0\ufdf7\uffff\uffff\uffff\15\0\ufdf1\uffff\ufde9\uffff\33\0\uffff\uffff\207" +
		"\0\ufddf\uffff\52\0\ufdc1\uffff\uffff\uffff\57\0\60\0\uffff\uffff\uffff\uffff\ufdb9" +
		"\uffff\ufdb3\uffff\ufdad\uffff\ufda1\uffff\uffff\uffff\uffff\uffff\152\0\153\0\151" +
		"\0\uffff\uffff\143\0\uffff\uffff\uffff\uffff\142\0\121\0\uffff\uffff\ufd99\uffff" +
		"\uffff\uffff\uffff\uffff\ufd63\uffff\ufd35\uffff\ufd07\uffff\ufcd9\uffff\ufcab\uffff" +
		"\ufc7d\uffff\ufc4f\uffff\ufc21\uffff\ufbf3\uffff\175\0\ufbc5\uffff\ufba7\uffff\ufb8b" +
		"\uffff\ufb6f\uffff\ufb59\uffff\uffff\uffff\211\0\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\65\0\uffff\uffff\17\0\uffff\uffff\ufb43\uffff\127\0\uffff\uffff\uffff\uffff\54\0" +
		"\55\0\63\0\uffff\uffff\ufb3d\uffff\uffff\uffff\ufb33\uffff\uffff\uffff\77\0\uffff" +
		"\uffff\uffff\uffff\27\0\uffff\uffff\147\0\uffff\uffff\213\0\136\0\ufb2d\uffff\uffff" +
		"\uffff\ufb0f\uffff\uffff\uffff\116\0\uffff\uffff\uffff\uffff\25\0\ufaf1\uffff\22" +
		"\0\uffff\uffff\71\0\ufae7\uffff\uffff\uffff\uffff\uffff\110\0\111\0\146\0\uffff\uffff" +
		"\uffff\uffff\102\0\ufadf\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufad3\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\205\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\ufaa5\uffff\uffff\uffff\uffff\uffff\104\0\103\0\100\0\31\0\141\0\150\0\uffff\uffff" +
		"\131\0\ufa9d\uffff\134\0\uffff\uffff\uffff\uffff\74\0\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\20\0\ufa7f\uffff\105\0\uffff" +
		"\uffff\132\0\uffff\uffff\135\0\73\0\112\0\uffff\uffff\uffff\uffff\101\0\133\0\uffff" +
		"\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = TemplatesLexer.unpack_short(1414,
		"\2\uffff\6\uffff\0\0\uffff\ufffe\13\uffff\37\uffff\34\12\uffff\ufffe\2\uffff\6\uffff" +
		"\0\1\uffff\ufffe\2\uffff\3\uffff\4\uffff\5\uffff\6\uffff\0\214\uffff\ufffe\57\uffff" +
		"\66\uffff\30\120\36\120\44\120\45\120\46\120\47\120\50\120\51\120\52\120\55\120\56" +
		"\120\60\120\61\120\62\120\63\120\64\120\65\120\67\120\70\120\72\120\73\120\74\120" +
		"\75\120\76\120\77\120\uffff\ufffe\13\uffff\1\12\71\12\uffff\ufffe\1\uffff\10\uffff" +
		"\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff" +
		"\55\uffff\57\uffff\56\67\uffff\ufffe\55\uffff\61\uffff\70\uffff\30\156\36\156\44" +
		"\156\45\156\46\156\47\156\50\156\51\156\52\156\56\156\60\156\62\156\63\156\64\156" +
		"\65\156\67\156\72\156\73\156\74\156\75\156\76\156\77\156\uffff\ufffe\46\uffff\47" +
		"\uffff\50\uffff\51\uffff\52\uffff\72\uffff\73\uffff\74\uffff\75\uffff\30\173\36\173" +
		"\44\173\45\173\56\173\60\173\62\173\63\173\64\173\65\173\67\173\76\173\77\173\uffff" +
		"\ufffe\30\uffff\36\176\44\176\45\176\56\176\60\176\62\176\63\176\64\176\65\176\67" +
		"\176\76\176\77\176\uffff\ufffe\65\uffff\67\uffff\36\201\44\201\45\201\56\201\60\201" +
		"\62\201\63\201\64\201\76\201\77\201\uffff\ufffe\63\uffff\64\uffff\77\uffff\36\204" +
		"\44\204\45\204\56\204\60\204\62\204\76\204\uffff\ufffe\62\uffff\44\50\45\50\uffff" +
		"\ufffe\57\uffff\61\uffff\21\14\44\14\45\14\uffff\ufffe\1\uffff\10\uffff\11\uffff" +
		"\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff" +
		"\57\uffff\60\67\uffff\ufffe\57\uffff\61\uffff\21\51\44\51\45\51\uffff\ufffe\57\uffff" +
		"\30\120\36\120\44\120\45\120\46\120\47\120\50\120\51\120\52\120\55\120\56\120\60" +
		"\120\61\120\62\120\63\120\64\120\65\120\67\120\70\120\72\120\73\120\74\120\75\120" +
		"\76\120\77\120\uffff\ufffe\62\uffff\44\56\45\56\uffff\ufffe\62\uffff\44\114\45\114" +
		"\uffff\ufffe\62\uffff\44\113\45\113\uffff\ufffe\62\uffff\44\61\45\61\uffff\ufffe" +
		"\1\uffff\71\23\uffff\ufffe\57\uffff\66\uffff\71\uffff\76\uffff\30\120\46\120\47\120" +
		"\50\120\51\120\52\120\55\120\56\120\61\120\62\120\63\120\64\120\65\120\67\120\70" +
		"\120\72\120\73\120\74\120\75\120\77\120\uffff\ufffe\62\uffff\56\70\60\70\uffff\ufffe" +
		"\1\uffff\60\23\uffff\ufffe\21\uffff\44\16\45\16\uffff\ufffe\57\uffff\61\uffff\21" +
		"\14\66\14\uffff\ufffe\1\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff" +
		"\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60\67\uffff\ufffe\21\uffff" +
		"\44\53\45\53\uffff\ufffe\1\uffff\60\144\uffff\ufffe\2\uffff\6\76\uffff\ufffe\60\26" +
		"\62\26\71\26\1\64\61\64\uffff\ufffe\62\uffff\60\24\71\24\uffff\ufffe\57\uffff\30" +
		"\130\36\130\44\130\45\130\46\130\47\130\50\130\51\130\52\130\55\130\56\130\60\130" +
		"\61\130\62\130\63\130\64\130\65\130\67\130\70\130\72\130\73\130\74\130\75\130\76" +
		"\130\77\130\uffff\ufffe\46\165\47\165\50\uffff\51\uffff\52\uffff\72\165\73\165\74" +
		"\165\75\165\30\165\36\165\44\165\45\165\56\165\60\165\62\165\63\165\64\165\65\165" +
		"\67\165\76\165\77\165\uffff\ufffe\46\166\47\166\50\uffff\51\uffff\52\uffff\72\166" +
		"\73\166\74\166\75\166\30\166\36\166\44\166\45\166\56\166\60\166\62\166\63\166\64" +
		"\166\65\166\67\166\76\166\77\166\uffff\ufffe\46\162\47\162\50\162\51\162\52\162\72" +
		"\162\73\162\74\162\75\162\30\162\36\162\44\162\45\162\56\162\60\162\62\162\63\162" +
		"\64\162\65\162\67\162\76\162\77\162\uffff\ufffe\46\163\47\163\50\163\51\163\52\163" +
		"\72\163\73\163\74\163\75\163\30\163\36\163\44\163\45\163\56\163\60\163\62\163\63" +
		"\163\64\163\65\163\67\163\76\163\77\163\uffff\ufffe\46\164\47\164\50\164\51\164\52" +
		"\164\72\164\73\164\74\164\75\164\30\164\36\164\44\164\45\164\56\164\60\164\62\164" +
		"\63\164\64\164\65\164\67\164\76\164\77\164\uffff\ufffe\46\uffff\47\uffff\50\uffff" +
		"\51\uffff\52\uffff\72\171\73\171\74\171\75\171\30\171\36\171\44\171\45\171\56\171" +
		"\60\171\62\171\63\171\64\171\65\171\67\171\76\171\77\171\uffff\ufffe\46\uffff\47" +
		"\uffff\50\uffff\51\uffff\52\uffff\72\172\73\172\74\172\75\172\30\172\36\172\44\172" +
		"\45\172\56\172\60\172\62\172\63\172\64\172\65\172\67\172\76\172\77\172\uffff\ufffe" +
		"\46\uffff\47\uffff\50\uffff\51\uffff\52\uffff\72\167\73\167\74\167\75\167\30\167" +
		"\36\167\44\167\45\167\56\167\60\167\62\167\63\167\64\167\65\167\67\167\76\167\77" +
		"\167\uffff\ufffe\46\uffff\47\uffff\50\uffff\51\uffff\52\uffff\72\170\73\170\74\170" +
		"\75\170\30\170\36\170\44\170\45\170\56\170\60\170\62\170\63\170\64\170\65\170\67" +
		"\170\76\170\77\170\uffff\ufffe\61\uffff\30\174\36\174\44\174\45\174\56\174\60\174" +
		"\62\174\63\174\64\174\65\174\67\174\76\174\77\174\uffff\ufffe\30\uffff\36\177\44" +
		"\177\45\177\56\177\60\177\62\177\63\177\64\177\65\177\67\177\76\177\77\177\uffff" +
		"\ufffe\30\uffff\36\200\44\200\45\200\56\200\60\200\62\200\63\200\64\200\65\200\67" +
		"\200\76\200\77\200\uffff\ufffe\63\202\64\202\36\202\44\202\45\202\56\202\60\202\62" +
		"\202\76\202\77\202\uffff\ufffe\63\uffff\64\203\36\203\44\203\45\203\56\203\60\203" +
		"\62\203\76\203\77\203\uffff\ufffe\21\uffff\66\16\uffff\ufffe\36\uffff\62\uffff\44" +
		"\107\45\107\uffff\ufffe\62\uffff\60\145\uffff\ufffe\1\uffff\10\uffff\11\uffff\20" +
		"\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57" +
		"\uffff\60\67\uffff\ufffe\1\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40" +
		"\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60\67\uffff\ufffe\61" +
		"\uffff\44\32\45\32\66\32\uffff\ufffe\62\uffff\44\66\45\66\uffff\ufffe\60\30\62\30" +
		"\71\30\1\64\61\64\uffff\ufffe\54\uffff\57\uffff\30\120\46\120\47\120\50\120\51\120" +
		"\52\120\55\120\60\120\61\120\62\120\63\120\64\120\65\120\67\120\70\120\72\120\73" +
		"\120\74\120\75\120\77\120\uffff\ufffe\62\uffff\44\115\45\115\uffff\ufffe\1\uffff" +
		"\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff" +
		"\53\uffff\55\uffff\57\uffff\60\67\uffff\ufffe\36\uffff\44\107\45\107\uffff\ufffe");

	private static final short[] lapg_sym_goto = TemplatesLexer.unpack_short(125,
		"\0\2\111\131\146\163\200\220\225\312\u0100\u0105\u0107\u0109\u010d\u010f\u0114\u0149" +
		"\u0151\u0156\u015b\u015b\u0161\u0163\u0163\u0166\u0166\u019b\u01d0\u01d1\u01d6\u01d8" +
		"\u01d9\u020e\u0243\u0248\u027d\u0289\u0292\u029c\u02db\u02e5\u02ef\u02f9\u032e\u032f" +
		"\u0366\u036a\u03ab\u03b6\u03c0\u03d7\u03da\u03dd\u03de\u03e3\u03e4\u03e5\u03e9\u03f3" +
		"\u03fd\u0407\u0411\u0416\u0417\u0417\u041c\u041d\u041e\u0420\u0422\u0424\u0426\u0428" +
		"\u042a\u042c\u042e\u0430\u0436\u043f\u044c\u0459\u045e\u045f\u0469\u046a\u046b\u0478" +
		"\u047a\u0487\u0488\u048a\u0497\u049c\u049e\u04a3\u04d8\u050d\u0542\u0544\u0547\u057c" +
		"\u05b1\u05e4\u060e\u0636\u065e\u0684\u069a\u06af\u06b5\u06b6\u06bb\u06bc\u06be\u06c0" +
		"\u06c2\u06c4\u06c5\u06c6\u06c7\u06cd\u06ce\u06d0\u06d1");

	private static final short[] lapg_sym_from = TemplatesLexer.unpack_short(1745,
		"\u010f\u0110\16\26\32\40\41\43\44\45\46\47\51\54\56\57\60\61\106\111\112\125\142" +
		"\143\144\145\146\147\150\151\152\153\154\155\156\157\160\161\162\163\164\165\171" +
		"\172\201\204\210\211\215\222\224\225\232\261\267\273\302\304\311\313\314\316\327" +
		"\337\341\351\352\354\355\364\366\371\u0107\0\1\4\10\17\24\34\103\212\317\334\350" +
		"\370\u0101\u0105\u010b\1\10\17\24\34\103\317\334\350\370\u0101\u0105\u010b\1\10\17" +
		"\24\34\103\317\334\350\370\u0101\u0105\u010b\1\10\17\24\34\103\317\334\350\370\u0101" +
		"\u0105\u010b\0\1\4\10\17\24\34\103\301\317\334\350\370\u0101\u0105\u010b\103\334" +
		"\350\u0101\u010b\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151" +
		"\152\153\154\155\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304" +
		"\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\56" +
		"\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\156\157\160\161\162\163" +
		"\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354" +
		"\355\364\366\371\u0107\16\32\165\355\371\3\55\333\355\32\165\355\371\165\355\16\32" +
		"\165\355\371\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152" +
		"\153\154\155\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311" +
		"\313\314\316\327\341\351\352\354\355\364\366\371\u0107\16\32\165\174\203\264\355" +
		"\371\16\32\165\355\371\16\32\165\355\371\16\32\165\256\355\371\116\120\73\247\250" +
		"\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155" +
		"\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316" +
		"\327\341\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\56\57\60\61\111\112" +
		"\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210" +
		"\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107" +
		"\30\16\32\165\355\371\274\u0103\3\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145" +
		"\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\222\225" +
		"\232\267\273\304\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107\16\32" +
		"\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160" +
		"\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341" +
		"\351\352\354\355\364\366\371\u0107\16\32\165\355\371\16\32\41\44\46\51\54\56\57\60" +
		"\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201" +
		"\204\210\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355\364\366" +
		"\371\u0107\63\64\107\123\255\256\263\340\347\373\375\376\63\64\123\255\256\263\347" +
		"\375\376\72\234\235\236\237\240\241\242\243\244\16\32\41\44\46\51\54\56\57\60\61" +
		"\72\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201" +
		"\204\210\222\225\232\234\235\236\237\240\241\242\243\244\267\273\304\311\313\314" +
		"\316\327\341\351\352\354\355\364\366\371\u0107\72\234\235\236\237\240\241\242\243" +
		"\244\72\234\235\236\237\240\241\242\243\244\72\234\235\236\237\240\241\242\243\244" +
		"\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155" +
		"\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316" +
		"\327\341\351\352\354\355\364\366\371\u0107\342\16\32\41\44\46\51\54\56\57\60\61\65" +
		"\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201" +
		"\204\207\210\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355\364" +
		"\366\371\u0107\131\134\230\374\16\32\35\41\44\46\51\54\56\57\60\61\105\111\112\113" +
		"\114\122\130\142\144\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164" +
		"\165\175\201\204\210\222\225\231\232\233\267\273\304\311\313\314\316\327\341\342" +
		"\344\351\352\354\355\364\366\371\u0107\135\177\257\266\277\312\343\345\377\u0100" +
		"\u010c\65\105\113\122\175\215\233\246\321\337\100\115\117\121\123\124\131\133\135" +
		"\214\230\274\276\312\325\326\340\347\353\373\375\377\u010c\75\251\252\75\251\252" +
		"\74\35\130\275\306\323\74\65\130\216\275\306\72\234\235\236\237\240\241\242\243\244" +
		"\72\234\235\236\237\240\241\242\243\244\72\234\235\236\237\240\241\242\243\244\72" +
		"\234\235\236\237\240\241\242\243\244\130\253\275\306\377\75\16\32\165\355\371\0\0" +
		"\0\4\0\4\0\4\3\55\0\4\105\175\125\171\174\264\10\34\1\10\24\317\370\u0105\63\64\123" +
		"\255\256\263\347\375\376\1\10\17\24\34\103\317\334\350\370\u0101\u0105\u010b\1\10" +
		"\17\24\34\103\317\334\350\370\u0101\u0105\u010b\16\32\165\355\371\115\26\40\47\106" +
		"\125\144\156\171\261\302\203\113\1\10\17\24\34\103\317\334\350\370\u0101\u0105\u010b" +
		"\103\u0101\1\10\17\24\34\103\317\334\350\370\u0101\u0105\u010b\301\301\334\1\10\17" +
		"\24\34\103\317\334\350\370\u0101\u0105\u010b\16\32\165\355\371\274\u0103\103\334" +
		"\350\u0101\u010b\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151" +
		"\152\153\154\155\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304" +
		"\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\56" +
		"\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164" +
		"\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355" +
		"\364\366\371\u0107\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151" +
		"\152\153\154\155\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304" +
		"\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107\60\211\130\275\306\16" +
		"\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157" +
		"\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327" +
		"\341\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\56\57\60\61\111\112\142" +
		"\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\222" +
		"\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107\16" +
		"\32\41\44\46\51\54\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160" +
		"\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341" +
		"\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\60\61\111\112\142\157\160" +
		"\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341" +
		"\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\60\61\111\112\142\161\162" +
		"\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341\351\352" +
		"\354\355\364\366\371\u0107\16\32\41\44\46\51\54\60\61\111\112\142\161\162\163\164" +
		"\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355" +
		"\364\366\371\u0107\16\32\41\44\46\51\54\60\61\111\112\142\163\164\165\201\204\210" +
		"\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107" +
		"\16\32\44\46\51\54\61\142\164\165\210\232\267\304\316\327\351\354\355\364\371\u0107" +
		"\16\32\44\46\51\54\61\142\165\210\232\267\304\316\327\351\354\355\364\371\u0107\60" +
		"\111\201\311\313\366\1\16\32\165\355\371\0\3\55\105\175\174\264\125\171\113\203\115" +
		"\60\111\201\311\313\366\212\274\u0103\211");

	private static final short[] lapg_sym_to = TemplatesLexer.unpack_short(1745,
		"\u0111\u0112\35\104\35\104\114\116\35\120\35\104\35\35\114\114\130\35\104\114\114" +
		"\213\35\231\104\114\114\114\114\114\114\114\114\114\104\114\114\114\114\114\35\35" +
		"\213\260\114\114\35\275\303\114\306\114\35\104\35\114\336\35\342\114\114\35\35\361" +
		"\114\35\114\35\35\35\114\35\35\2\12\2\12\12\12\12\12\300\12\12\12\12\12\12\12\13" +
		"\13\13\13\13\13\13\13\13\13\13\13\13\14\14\14\14\14\14\14\14\14\14\14\14\14\15\15" +
		"\15\15\15\15\15\15\15\15\15\15\15\3\16\3\32\16\16\32\165\333\16\355\371\16\165\16" +
		"\371\166\166\166\166\166\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36" +
		"\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36" +
		"\36\36\36\36\36\36\36\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37" +
		"\37\37\37\245\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37" +
		"\37\37\37\37\37\37\37\40\40\40\40\40\25\25\354\354\107\255\255\255\256\376\41\41" +
		"\41\41\41\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42" +
		"\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42" +
		"\42\42\43\43\43\261\267\261\43\43\44\44\44\44\44\45\45\45\45\45\46\46\46\316\46\46" +
		"\207\210\156\156\156\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47" +
		"\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47" +
		"\47\47\47\47\47\47\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50" +
		"\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50" +
		"\50\50\50\50\50\106\51\51\51\51\51\327\327\26\52\52\52\52\52\52\52\52\52\52\52\52" +
		"\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52" +
		"\52\52\52\52\52\52\52\52\52\52\52\52\52\52\53\53\53\53\53\53\53\53\53\53\53\53\53" +
		"\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53" +
		"\53\53\53\53\53\53\53\53\53\53\53\53\53\54\54\54\54\54\55\55\55\55\55\55\55\55\55" +
		"\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55" +
		"\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\136\136\176\136\136\136\136\362" +
		"\136\u0102\136\136\137\137\137\137\137\137\137\137\137\145\145\145\145\145\145\145" +
		"\145\145\145\56\56\56\56\56\56\56\56\56\56\56\146\56\56\56\56\56\56\56\56\56\56\56" +
		"\56\56\56\56\56\56\56\56\56\56\56\56\56\56\146\146\146\146\146\146\146\146\146\56" +
		"\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\147\147\147\147\147\147\147\147" +
		"\147\147\150\150\150\150\150\150\150\150\150\150\151\151\151\151\151\151\151\151" +
		"\151\151\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57" +
		"\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57" +
		"\57\57\364\60\60\60\60\60\60\60\60\60\60\60\142\60\60\60\60\60\60\60\60\60\60\60" +
		"\60\60\60\60\60\60\60\60\60\60\273\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60" +
		"\60\60\60\60\60\223\226\310\u0103\61\61\111\61\61\61\61\61\61\61\61\61\171\61\61" +
		"\201\111\211\111\61\232\61\61\61\61\61\61\61\61\61\61\61\61\61\61\61\61\171\61\61" +
		"\61\61\61\311\61\313\61\61\61\61\61\61\61\61\61\111\366\61\61\61\61\61\61\61\61\227" +
		"\265\320\324\332\344\365\367\u0106\u0108\u010e\143\172\172\172\172\172\172\172\172" +
		"\172\164\204\164\164\164\164\224\225\164\302\164\164\224\164\164\352\164\164\164" +
		"\164\164\164\164\161\161\161\162\162\162\157\112\217\217\217\351\160\144\220\304" +
		"\220\220\152\152\152\152\152\152\152\152\152\152\153\153\153\153\153\153\153\153" +
		"\153\153\154\154\154\154\154\154\154\154\154\154\155\155\155\155\155\155\155\155" +
		"\155\155\221\314\221\221\u0107\163\62\62\62\62\62\u010f\4\5\31\6\6\7\7\27\27\10\10" +
		"\173\173\214\214\262\262\33\110\17\34\103\350\u0101\u010b\140\141\212\315\317\322" +
		"\370\u0104\u0105\20\20\102\20\102\102\20\356\102\20\102\20\102\21\21\21\21\21\21" +
		"\21\21\21\21\21\21\21\63\63\63\63\63\205\105\113\122\175\215\233\246\215\321\337" +
		"\270\202\22\22\22\22\22\22\22\22\22\22\22\22\22\167\u0109\23\23\23\23\23\23\23\23" +
		"\23\23\23\23\23\334\335\357\24\24\24\24\24\24\24\24\24\24\24\24\24\64\64\64\64\64" +
		"\330\330\170\360\372\170\u010d\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65" +
		"\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65" +
		"\65\65\65\65\65\65\65\65\65\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66" +
		"\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66" +
		"\66\66\66\66\66\66\66\66\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67" +
		"\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67" +
		"\67\67\67\67\67\67\67\131\276\222\222\341\70\70\70\70\70\70\70\70\70\70\70\70\70" +
		"\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70" +
		"\70\70\70\70\70\70\70\70\70\70\70\70\70\71\71\71\71\71\71\71\126\127\71\71\71\71" +
		"\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71" +
		"\71\71\71\71\71\71\71\71\71\71\71\71\71\72\72\72\72\72\72\72\72\72\72\72\72\234\235" +
		"\236\237\240\241\242\243\244\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72" +
		"\72\72\72\72\72\72\72\72\72\72\72\72\73\73\73\73\73\73\73\73\73\73\73\73\247\250" +
		"\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73" +
		"\73\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74" +
		"\74\74\74\74\74\74\74\74\74\74\74\74\74\74\75\75\75\75\75\75\75\75\75\75\75\75\251" +
		"\252\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75" +
		"\76\76\115\76\76\76\76\132\76\132\200\76\253\76\76\132\272\76\305\307\76\76\326\76" +
		"\132\132\346\76\76\363\76\374\76\76\76\132\76\76\77\77\77\77\77\77\77\77\254\77\77" +
		"\77\77\77\77\77\77\77\77\77\77\77\100\100\117\121\123\124\135\230\100\274\312\325" +
		"\340\347\353\373\375\100\377\100\u010c\133\133\133\133\133\133\u0110\101\101\101" +
		"\101\101\11\30\125\174\264\263\323\216\257\203\271\206\134\177\266\343\345\u0100" +
		"\301\331\u010a\277");

	private static final short[] lapg_rlen = TemplatesLexer.unpack_short(142,
		"\0\1\1\1\2\1\1\1\3\2\0\1\0\1\0\1\11\1\6\0\1\3\1\2\3\4\2\3\2\1\1\1\1\1\1\1\1\1\1\3" +
		"\1\0\1\0\1\4\0\1\3\2\1\2\1\3\2\0\1\3\3\7\5\1\0\1\7\13\1\2\2\4\3\0\1\5\11\2\2\2\3" +
		"\1\1\3\1\1\1\1\1\4\3\6\10\12\6\10\4\1\1\6\3\3\0\1\5\3\5\1\1\1\1\1\1\2\2\1\3\3\3\3" +
		"\3\3\3\3\3\1\3\3\1\3\3\1\3\3\1\5\1\3\1\3\1\3\1\1");

	private static final short[] lapg_rlex = TemplatesLexer.unpack_short(142,
		"\160\160\102\103\103\104\104\104\105\105\161\161\162\162\163\163\106\107\110\164" +
		"\164\111\112\112\112\112\113\114\115\115\116\116\117\117\117\117\117\117\117\120" +
		"\121\165\165\166\166\121\167\167\121\121\121\122\123\123\124\170\170\125\126\127" +
		"\127\127\171\171\130\130\131\131\131\132\133\172\172\134\134\134\134\135\136\136" +
		"\137\137\137\137\137\137\137\137\137\137\137\137\137\137\137\137\137\140\141\141" +
		"\173\173\141\142\142\143\143\143\144\144\145\145\145\146\146\146\146\146\146\146" +
		"\146\146\146\147\147\147\150\150\150\151\151\151\152\152\153\153\154\154\155\155" +
		"\156\157");

	protected static final String[] lapg_syms = new String[] {
		"eoi",
		"identifier",
		"any",
		"escdollar",
		"escid",
		"escint",
		"'${'",
		"'$/'",
		"icon",
		"ccon",
		"Lcall",
		"Lcached",
		"Lcase",
		"Lend",
		"Lelse",
		"Leval",
		"Lfalse",
		"Lfor",
		"Lfile",
		"Lforeach",
		"Lgrep",
		"Lif",
		"Lin",
		"Limport",
		"Lis",
		"Lmap",
		"Lnew",
		"Lnull",
		"Lquery",
		"Lswitch",
		"Lseparator",
		"Ltemplate",
		"Ltrue",
		"Lself",
		"Lassert",
		"'{'",
		"'}'",
		"'-}'",
		"'+'",
		"'-'",
		"'*'",
		"'/'",
		"'%'",
		"'!'",
		"'|'",
		"'['",
		"']'",
		"'('",
		"')'",
		"'.'",
		"','",
		"'&&'",
		"'||'",
		"'=='",
		"'='",
		"'!='",
		"'->'",
		"'=>'",
		"'<='",
		"'>='",
		"'<'",
		"'>'",
		"':'",
		"'?'",
		"_skip",
		"error",
		"input",
		"definitions",
		"definition",
		"template_def",
		"query_def",
		"cached_flag",
		"template_start",
		"parameters",
		"parameter_list",
		"context_type",
		"template_end",
		"instructions",
		"'[-]}'",
		"instruction",
		"simple_instruction",
		"sentence",
		"comma_expr",
		"qualified_id",
		"template_for_expr",
		"template_arguments",
		"control_instruction",
		"else_clause",
		"switch_instruction",
		"case_list",
		"one_case",
		"control_start",
		"control_sentence",
		"separator_expr",
		"control_end",
		"primary_expression",
		"closure",
		"complex_data",
		"map_entries",
		"map_separator",
		"bcon",
		"unary_expression",
		"binary_op",
		"instanceof_expression",
		"equality_expression",
		"conditional_op",
		"conditional_expression",
		"assignment_expression",
		"expression",
		"expression_list",
		"body",
		"syntax_problem",
		"definitionsopt",
		"cached_flagopt",
		"parametersopt",
		"context_typeopt",
		"parameter_listopt",
		"template_argumentsopt",
		"template_for_expropt",
		"comma_expropt",
		"expression_listopt",
		"anyopt",
		"separator_expropt",
		"map_entriesopt",
	};

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 66;
		public static final int definitions = 67;
		public static final int definition = 68;
		public static final int template_def = 69;
		public static final int query_def = 70;
		public static final int cached_flag = 71;
		public static final int template_start = 72;
		public static final int parameters = 73;
		public static final int parameter_list = 74;
		public static final int context_type = 75;
		public static final int template_end = 76;
		public static final int instructions = 77;
		public static final int LSQUAREMINUSRSQUARERCURLY = 78;
		public static final int instruction = 79;
		public static final int simple_instruction = 80;
		public static final int sentence = 81;
		public static final int comma_expr = 82;
		public static final int qualified_id = 83;
		public static final int template_for_expr = 84;
		public static final int template_arguments = 85;
		public static final int control_instruction = 86;
		public static final int else_clause = 87;
		public static final int switch_instruction = 88;
		public static final int case_list = 89;
		public static final int one_case = 90;
		public static final int control_start = 91;
		public static final int control_sentence = 92;
		public static final int separator_expr = 93;
		public static final int control_end = 94;
		public static final int primary_expression = 95;
		public static final int closure = 96;
		public static final int complex_data = 97;
		public static final int map_entries = 98;
		public static final int map_separator = 99;
		public static final int bcon = 100;
		public static final int unary_expression = 101;
		public static final int binary_op = 102;
		public static final int instanceof_expression = 103;
		public static final int equality_expression = 104;
		public static final int conditional_op = 105;
		public static final int conditional_expression = 106;
		public static final int assignment_expression = 107;
		public static final int expression = 108;
		public static final int expression_list = 109;
		public static final int body = 110;
		public static final int syntax_problem = 111;
		public static final int definitionsopt = 112;
		public static final int cached_flagopt = 113;
		public static final int parametersopt = 114;
		public static final int context_typeopt = 115;
		public static final int parameter_listopt = 116;
		public static final int template_argumentsopt = 117;
		public static final int template_for_expropt = 118;
		public static final int comma_expropt = 119;
		public static final int expression_listopt = 120;
		public static final int anyopt = 121;
		public static final int separator_expropt = 122;
		public static final int map_entriesopt = 123;
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final int lapg_state_sym(int state, int symbol) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol + 1] - 1;
		int i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if (i == state) {
				return lapg_sym_to[e];
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
			}
		}
		return -1;
	}

	protected int lapg_head;
	protected LapgSymbol[] lapg_m;
	protected LapgSymbol lapg_n;
	protected TemplatesLexer lapg_lexer;

	private Object parse(TemplatesLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;
		int lapg_symbols_ok = 4;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = initialState;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != finalState) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
				lapg_symbols_ok++;
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				if (restore()) {
					if (lapg_symbols_ok >= 4) {
						reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
								MessageFormat.format("syntax error before line {0}", lapg_lexer.getTokenLine()));
					}
					if (lapg_symbols_ok <= 1) {
						lapg_n = lapg_lexer.next();
					}
					lapg_symbols_ok = 0;
					continue;
				}
				if (lapg_head < 0) {
					lapg_head = 0;
					lapg_m[0] = new LapgSymbol();
					lapg_m[0].state = initialState;
				}
				break;
			}
		}

		if (lapg_m[lapg_head].state != finalState) {
			if (lapg_symbols_ok >= 4) {
				reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			}
			throw new ParseException();
		}
		return lapg_m[lapg_head - 1].sym;
	}

	protected boolean restore() {
		if (lapg_n.lexem == 0) {
			return false;
		}
		while (lapg_head >= 0 && lapg_state_sym(lapg_m[lapg_head].state, 65) == -1) {
			dispose(lapg_m[lapg_head]);
			lapg_m[lapg_head] = null;
			lapg_head--;
		}
		if (lapg_head >= 0) {
			lapg_m[++lapg_head] = new LapgSymbol();
			lapg_m[lapg_head].lexem = 65;
			lapg_m[lapg_head].sym = null;
			lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, 65);
			lapg_m[lapg_head].line = lapg_n.line;
			lapg_m[lapg_head].offset = lapg_n.offset;
			lapg_m[lapg_head].endoffset = lapg_n.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			cleanup(lapg_m[lapg_head]);
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 3:  // definitions ::= definition
				 lapg_gg.sym = new ArrayList(); if (((IBundleEntity)lapg_m[lapg_head].sym) != null) ((List<IBundleEntity>)lapg_gg.sym).add(((IBundleEntity)lapg_m[lapg_head].sym)); 
				break;
			case 4:  // definitions ::= definitions definition
				 if (((IBundleEntity)lapg_m[lapg_head].sym) != null) ((List<IBundleEntity>)lapg_gg.sym).add(((IBundleEntity)lapg_m[lapg_head].sym)); 
				break;
			case 7:  // definition ::= any
				 lapg_gg.sym = null; 
				break;
			case 8:  // template_def ::= template_start instructions template_end
				 ((TemplateNode)lapg_m[lapg_head - 2].sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head - 1].sym)); 
				break;
			case 16:  // query_def ::= '${' cached_flagopt Lquery qualified_id parametersopt context_typeopt '=' expression '}'
				 lapg_gg.sym = new QueryNode(((String)lapg_m[lapg_head - 5].sym), ((List<ParameterNode>)lapg_m[lapg_head - 4].sym), ((String)lapg_m[lapg_head - 3].sym), templatePackage, ((ExpressionNode)lapg_m[lapg_head - 1].sym), ((Boolean)lapg_m[lapg_head - 7].sym) != null, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head - 5].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head - 8].line); 
				break;
			case 17:  // cached_flag ::= Lcached
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 18:  // template_start ::= '${' Ltemplate qualified_id parametersopt context_typeopt '[-]}'
				 lapg_gg.sym = new TemplateNode(((String)lapg_m[lapg_head - 3].sym), ((List<ParameterNode>)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head - 1].sym), templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); checkFqn(((String)lapg_m[lapg_head - 3].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_m[lapg_head - 5].line); 
				break;
			case 21:  // parameters ::= '(' parameter_listopt ')'
				 lapg_gg.sym = ((List<ParameterNode>)lapg_m[lapg_head - 1].sym); 
				break;
			case 22:  // parameter_list ::= identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset)); 
				break;
			case 23:  // parameter_list ::= qualified_id identifier
				 lapg_gg.sym = new ArrayList(); ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 1].offset, lapg_gg.endoffset)); 
				break;
			case 24:  // parameter_list ::= parameter_list ',' identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head].offset, lapg_gg.endoffset)); 
				break;
			case 25:  // parameter_list ::= parameter_list ',' qualified_id identifier
				 ((List<ParameterNode>)lapg_gg.sym).add(new ParameterNode(((String)lapg_m[lapg_head - 1].sym), ((String)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 1].offset, lapg_gg.endoffset)); 
				break;
			case 26:  // context_type ::= Lfor qualified_id
				 lapg_gg.sym = ((String)lapg_m[lapg_head].sym); 
				break;
			case 28:  // instructions ::= instructions instruction
				 if (((Node)lapg_m[lapg_head].sym) != null) ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head].sym)); 
				break;
			case 29:  // instructions ::= instruction
				 lapg_gg.sym = new ArrayList<Node>(); if (((Node)lapg_m[lapg_head].sym)!=null) ((ArrayList<Node>)lapg_gg.sym).add(((Node)lapg_m[lapg_head].sym)); 
				break;
			case 30:  // '[-]}' ::= '-}'
				 skipSpaces(lapg_m[lapg_head].offset+1); 
				break;
			case 35:  // instruction ::= escid
				 lapg_gg.sym = createEscapedId(((String)lapg_m[lapg_head].sym), lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 36:  // instruction ::= escint
				 lapg_gg.sym = new IndexNode(null, new LiteralNode(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 37:  // instruction ::= escdollar
				 lapg_gg.sym = new DollarNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 38:  // instruction ::= any
				 lapg_gg.sym = new TextNode(source, rawText(lapg_gg.offset, lapg_gg.endoffset), lapg_gg.endoffset); 
				break;
			case 39:  // simple_instruction ::= '${' sentence '[-]}'
				 lapg_gg.sym = ((Node)lapg_m[lapg_head - 1].sym); 
				break;
			case 45:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head - 2].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), ((ExpressionNode)lapg_m[lapg_head].sym), templatePackage, true, source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 48:  // sentence ::= Leval conditional_expression comma_expropt
				 lapg_gg.sym = new EvalNode(((ExpressionNode)lapg_m[lapg_head - 1].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 49:  // sentence ::= Lassert expression
				 lapg_gg.sym = new AssertNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 50:  // sentence ::= syntax_problem
				 lapg_gg.sym = null; 
				break;
			case 51:  // comma_expr ::= ',' conditional_expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 53:  // qualified_id ::= qualified_id '.' identifier
				 lapg_gg.sym = ((String)lapg_gg.sym) + "." + ((String)lapg_m[lapg_head].sym); 
				break;
			case 54:  // template_for_expr ::= Lfor expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 57:  // template_arguments ::= '(' expression_listopt ')'
				 lapg_gg.sym = ((ArrayList)lapg_m[lapg_head - 1].sym); 
				break;
			case 58:  // control_instruction ::= control_start instructions else_clause
				 ((CompoundNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head - 1].sym)); applyElse(((CompoundNode)lapg_m[lapg_head - 2].sym),((ElseIfNode)lapg_m[lapg_head].sym), lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 59:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				 lapg_gg.sym = new ElseIfNode(((ExpressionNode)lapg_m[lapg_head - 3].sym), ((ArrayList<Node>)lapg_m[lapg_head - 1].sym), ((ElseIfNode)lapg_m[lapg_head].sym), source, lapg_m[lapg_head - 6].offset, lapg_m[lapg_head - 1].endoffset); 
				break;
			case 60:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new ElseIfNode(null, ((ArrayList<Node>)lapg_m[lapg_head - 1].sym), null, source, lapg_m[lapg_head - 4].offset, lapg_m[lapg_head - 1].endoffset); 
				break;
			case 61:  // else_clause ::= control_end
				 lapg_gg.sym = null; 
				break;
			case 64:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head - 4].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), null, source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head - 2].offset,lapg_m[lapg_head - 2].endoffset, lapg_m[lapg_head - 2].line); 
				break;
			case 65:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				 lapg_gg.sym = new SwitchNode(((ExpressionNode)lapg_m[lapg_head - 8].sym), ((ArrayList)lapg_m[lapg_head - 5].sym), ((ArrayList<Node>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset,lapg_gg.endoffset); checkIsSpace(lapg_m[lapg_head - 6].offset,lapg_m[lapg_head - 6].endoffset, lapg_m[lapg_head - 6].line); 
				break;
			case 66:  // case_list ::= one_case
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head].sym)); 
				break;
			case 67:  // case_list ::= case_list one_case
				 ((ArrayList)lapg_gg.sym).add(((CaseNode)lapg_m[lapg_head].sym)); 
				break;
			case 68:  // case_list ::= case_list instruction
				 CaseNode.add(((ArrayList)lapg_gg.sym), ((Node)lapg_m[lapg_head].sym)); 
				break;
			case 69:  // one_case ::= '${' Lcase expression '[-]}'
				 lapg_gg.sym = new CaseNode(((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset,lapg_gg.endoffset); 
				break;
			case 70:  // control_start ::= '${' control_sentence '[-]}'
				 lapg_gg.sym = ((CompoundNode)lapg_m[lapg_head - 1].sym); 
				break;
			case 73:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), null, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 74:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				 lapg_gg.sym = new ForeachNode(((String)lapg_m[lapg_head - 7].sym), ((ExpressionNode)lapg_m[lapg_head - 4].sym), ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 75:  // control_sentence ::= Lif expression
				 lapg_gg.sym = new IfNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 76:  // control_sentence ::= Lfile expression
				 lapg_gg.sym = new FileNode(((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 77:  // separator_expr ::= Lseparator expression
				 lapg_gg.sym = ((ExpressionNode)lapg_m[lapg_head].sym); 
				break;
			case 80:  // primary_expression ::= identifier
				 lapg_gg.sym = new SelectNode(null, ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 81:  // primary_expression ::= '(' expression ')'
				 lapg_gg.sym = new ParenthesesNode(((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 82:  // primary_expression ::= icon
				 lapg_gg.sym = new LiteralNode(((Integer)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 83:  // primary_expression ::= bcon
				 lapg_gg.sym = new LiteralNode(((Boolean)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 84:  // primary_expression ::= ccon
				 lapg_gg.sym = new LiteralNode(((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 85:  // primary_expression ::= Lself
				 lapg_gg.sym = new ThisNode(source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 86:  // primary_expression ::= Lnull
				 lapg_gg.sym = new LiteralNode(null, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 87:  // primary_expression ::= identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(null, ((String)lapg_m[lapg_head - 3].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 88:  // primary_expression ::= primary_expression '.' identifier
				 lapg_gg.sym = new SelectNode(((ExpressionNode)lapg_m[lapg_head - 2].sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 89:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				 lapg_gg.sym = new MethodCallNode(((ExpressionNode)lapg_m[lapg_head - 5].sym), ((String)lapg_m[lapg_head - 3].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 90:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				 lapg_gg.sym = createCollectionProcessor(((ExpressionNode)lapg_m[lapg_head - 7].sym), ((String)lapg_m[lapg_head - 5].sym), ((String)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 91:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ':' expression ')'
				 lapg_gg.sym = createMapCollect(((ExpressionNode)lapg_m[lapg_head - 9].sym), ((String)lapg_m[lapg_head - 7].sym), ((String)lapg_m[lapg_head - 5].sym), ((ExpressionNode)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset, lapg_gg.line); 
				break;
			case 92:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((String)lapg_m[lapg_head - 3].sym), ((ArrayList)lapg_m[lapg_head - 1].sym), ((ExpressionNode)lapg_m[lapg_head - 5].sym), templatePackage, false, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 93:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				 lapg_gg.sym = new CallTemplateNode(((ExpressionNode)lapg_m[lapg_head - 4].sym),((ArrayList)lapg_m[lapg_head - 1].sym),((ExpressionNode)lapg_m[lapg_head - 7].sym),templatePackage, source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 94:  // primary_expression ::= primary_expression '[' expression ']'
				 lapg_gg.sym = new IndexNode(((ExpressionNode)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 97:  // closure ::= '{' cached_flagopt parameter_listopt '=>' expression '}'
				 lapg_gg.sym = new ClosureNode(((Boolean)lapg_m[lapg_head - 4].sym) != null, ((List<ParameterNode>)lapg_m[lapg_head - 3].sym), ((ExpressionNode)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 98:  // complex_data ::= '[' expression_listopt ']'
				 lapg_gg.sym = new ListNode(((ArrayList)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 99:  // complex_data ::= '[' map_entries ']'
				 lapg_gg.sym = new ConcreteMapNode(((Map<String,ExpressionNode>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 102:  // complex_data ::= Lnew qualified_id '(' map_entriesopt ')'
				 lapg_gg.sym = new CreateClassNode(((String)lapg_m[lapg_head - 3].sym), ((Map<String,ExpressionNode>)lapg_m[lapg_head - 1].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 103:  // map_entries ::= identifier map_separator conditional_expression
				 lapg_gg.sym = new LinkedHashMap(); ((Map<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 104:  // map_entries ::= map_entries ',' identifier map_separator conditional_expression
				 ((Map<String,ExpressionNode>)lapg_gg.sym).put(((String)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 108:  // bcon ::= Ltrue
				 lapg_gg.sym = Boolean.TRUE; 
				break;
			case 109:  // bcon ::= Lfalse
				 lapg_gg.sym = Boolean.FALSE; 
				break;
			case 111:  // unary_expression ::= '!' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 112:  // unary_expression ::= '-' unary_expression
				 lapg_gg.sym = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 114:  // binary_op ::= binary_op '*' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 115:  // binary_op ::= binary_op '/' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 116:  // binary_op ::= binary_op '%' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 117:  // binary_op ::= binary_op '+' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 118:  // binary_op ::= binary_op '-' binary_op
				 lapg_gg.sym = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 119:  // binary_op ::= binary_op '<' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 120:  // binary_op ::= binary_op '>' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 121:  // binary_op ::= binary_op '<=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 122:  // binary_op ::= binary_op '>=' binary_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 124:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 125:  // instanceof_expression ::= instanceof_expression Lis ccon
				 lapg_gg.sym = new InstanceOfNode(((ExpressionNode)lapg_gg.sym), ((String)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 127:  // equality_expression ::= equality_expression '==' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 128:  // equality_expression ::= equality_expression '!=' instanceof_expression
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)lapg_gg.sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 130:  // conditional_op ::= conditional_op '&&' conditional_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 131:  // conditional_op ::= conditional_op '||' conditional_op
				 lapg_gg.sym = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 133:  // conditional_expression ::= conditional_op '?' conditional_expression ':' conditional_expression
				 lapg_gg.sym = new TriplexNode(((ExpressionNode)lapg_m[lapg_head - 4].sym), ((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 135:  // assignment_expression ::= identifier '=' conditional_expression
				 lapg_gg.sym = new AssignNode(((String)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 137:  // expression ::= expression ',' assignment_expression
				 lapg_gg.sym = new CommaNode(((ExpressionNode)lapg_m[lapg_head - 2].sym), ((ExpressionNode)lapg_m[lapg_head].sym), source, lapg_gg.offset, lapg_gg.endoffset); 
				break;
			case 138:  // expression_list ::= conditional_expression
				 lapg_gg.sym = new ArrayList(); ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 139:  // expression_list ::= expression_list ',' conditional_expression
				 ((ArrayList)lapg_gg.sym).add(((ExpressionNode)lapg_m[lapg_head].sym)); 
				break;
			case 140:  // body ::= instructions
				
							lapg_gg.sym = new TemplateNode("inline", null, null, templatePackage, source, lapg_gg.offset, lapg_gg.endoffset);
							((TemplateNode)lapg_gg.sym).setInstructions(((ArrayList<Node>)lapg_m[lapg_head].sym));
						
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(LapgSymbol sym) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(LapgSymbol sym) {
	}

	public List<IBundleEntity> parseInput(TemplatesLexer lexer) throws IOException, ParseException {
		return (List<IBundleEntity>) parse(lexer, 0, 273);
	}

	public TemplateNode parseBody(TemplatesLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1, 274);
	}
}
