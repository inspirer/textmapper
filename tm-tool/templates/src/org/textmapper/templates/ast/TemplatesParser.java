/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.templates.ast;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.textmapper.templates.ast.TemplatesLexer.ErrorReporter;
import org.textmapper.templates.ast.TemplatesLexer.Span;
import org.textmapper.templates.ast.TemplatesLexer.Tokens;
import org.textmapper.templates.ast.TemplatesTree.TextSource;
import org.textmapper.templates.bundle.IBundleEntity;

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

	private ExpressionNode createMapCollect(ExpressionNode context, String instruction, String varName, ExpressionNode key, ExpressionNode value, TextSource source, int offset, int endoffset, int line) {
		if (!instruction.equals("collect")) {
			reporter.error("unknown collection processing instruction: " + instruction, line, offset, endoffset);
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
	private static final int[] tmAction = TemplatesLexer.unpack_int(275,
		"\ufffd\uffff\uffff\uffff\5\0\ufff5\uffff\uffed\uffff\1\0\3\0\4\0\uffff\uffff\0\0" +
		"\34\0\33\0\31\0\32\0\uffff\uffff\uffe5\uffff\23\0\30\0\26\0\27\0\uffff\uffff\11\0" +
		"\uffff\uffff\170\0\uffff\uffff\2\0\uffff\uffff\7\0\uffff\uffff\uffd7\uffff\74\0\76" +
		"\0\uffff\uffff\uffff\uffff\125\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\100\0\uffff\uffff\124\0\77\0\uffff\uffff\uff9f\uffff\uffff\uffff\uffff" +
		"\uffff\uff97\uffff\uffff\uffff\165\0\uffff\uffff\uffff\uffff\uff79\uffff\112\0\111" +
		"\0\75\0\131\0\uff45\uffff\uff17\uffff\ufefb\uffff\ufee1\uffff\156\0\160\0\ufecb\uffff" +
		"\42\0\22\0\uffff\uffff\44\0\ufec3\uffff\uffff\uffff\uffff\uffff\6\0\ufeb7\uffff\uffff" +
		"\uffff\ufe99\uffff\ufe8d\uffff\ufe57\uffff\uffff\uffff\ufe4f\uffff\uffff\uffff\ufe47" +
		"\uffff\uffff\uffff\uffff\uffff\ufe3f\uffff\ufe37\uffff\130\0\127\0\ufe31\uffff\uffff" +
		"\uffff\162\0\ufdff\uffff\uffff\uffff\uffff\uffff\25\0\24\0\35\0\62\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\71\0\50\0\53\0\ufdf7" +
		"\uffff\uffff\uffff\172\0\ufdf1\uffff\ufde9\uffff\21\0\uffff\uffff\157\0\ufddf\uffff" +
		"\200\0\ufdc1\uffff\uffff\uffff\204\0\40\0\uffff\uffff\uffff\uffff\ufdb9\uffff\ufdb3" +
		"\uffff\ufdad\uffff\ufda1\uffff\uffff\uffff\uffff\uffff\122\0\123\0\121\0\uffff\uffff" +
		"\115\0\uffff\uffff\uffff\uffff\114\0\73\0\uffff\uffff\ufd99\uffff\uffff\uffff\uffff" +
		"\uffff\ufd63\uffff\ufd35\uffff\ufd07\uffff\ufcd9\uffff\ufcab\uffff\ufc7d\uffff\ufc4f" +
		"\uffff\ufc21\uffff\ufbf3\uffff\145\0\ufbc5\uffff\ufba7\uffff\ufb8b\uffff\ufb6f\uffff" +
		"\ufb59\uffff\uffff\uffff\161\0\uffff\uffff\uffff\uffff\uffff\uffff\45\0\uffff\uffff" +
		"\174\0\uffff\uffff\ufb43\uffff\101\0\uffff\uffff\uffff\uffff\202\0\37\0\43\0\uffff" +
		"\uffff\ufb3d\uffff\uffff\uffff\ufb33\uffff\uffff\uffff\210\0\uffff\uffff\uffff\uffff" +
		"\15\0\uffff\uffff\117\0\uffff\uffff\163\0\110\0\ufb2d\uffff\uffff\uffff\ufb0f\uffff" +
		"\uffff\uffff\70\0\uffff\uffff\uffff\uffff\13\0\ufaf1\uffff\12\0\uffff\uffff\47\0" +
		"\ufae7\uffff\uffff\uffff\uffff\uffff\212\0\63\0\116\0\uffff\uffff\uffff\uffff\56" +
		"\0\ufadf\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufad3\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\155\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufaa5\uffff" +
		"\uffff\uffff\uffff\uffff\60\0\57\0\55\0\17\0\113\0\120\0\uffff\uffff\103\0\ufa9d" +
		"\uffff\106\0\uffff\uffff\uffff\uffff\52\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\10\0\ufa7f\uffff\61\0\uffff\uffff\104" +
		"\0\uffff\uffff\107\0\51\0\64\0\uffff\uffff\uffff\uffff\54\0\105\0\uffff\uffff\uffff" +
		"\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TemplatesLexer.unpack_short(1414,
		"\1\uffff\5\uffff\0\167\uffff\ufffe\13\uffff\37\uffff\34\171\uffff\ufffe\1\uffff\5" +
		"\uffff\0\166\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\5\uffff\0\164\uffff\ufffe" +
		"\57\uffff\66\uffff\30\72\36\72\44\72\45\72\46\72\47\72\50\72\51\72\52\72\55\72\56" +
		"\72\60\72\61\72\62\72\63\72\64\72\65\72\67\72\70\72\72\72\73\72\74\72\75\72\76\72" +
		"\77\72\uffff\ufffe\13\uffff\7\171\71\171\uffff\ufffe\7\uffff\10\uffff\11\uffff\20" +
		"\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57" +
		"\uffff\56\207\uffff\ufffe\55\uffff\61\uffff\70\uffff\30\126\36\126\44\126\45\126" +
		"\46\126\47\126\50\126\51\126\52\126\56\126\60\126\62\126\63\126\64\126\65\126\67" +
		"\126\72\126\73\126\74\126\75\126\76\126\77\126\uffff\ufffe\46\uffff\47\uffff\50\uffff" +
		"\51\uffff\52\uffff\72\uffff\73\uffff\74\uffff\75\uffff\30\143\36\143\44\143\45\143" +
		"\56\143\60\143\62\143\63\143\64\143\65\143\67\143\76\143\77\143\uffff\ufffe\30\uffff" +
		"\36\146\44\146\45\146\56\146\60\146\62\146\63\146\64\146\65\146\67\146\76\146\77" +
		"\146\uffff\ufffe\65\uffff\67\uffff\36\151\44\151\45\151\56\151\60\151\62\151\63\151" +
		"\64\151\76\151\77\151\uffff\ufffe\63\uffff\64\uffff\77\uffff\36\154\44\154\45\154" +
		"\56\154\60\154\62\154\76\154\uffff\ufffe\62\uffff\44\36\45\36\uffff\ufffe\57\uffff" +
		"\61\uffff\21\173\44\173\45\173\uffff\ufffe\7\uffff\10\uffff\11\uffff\20\uffff\32" +
		"\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60" +
		"\207\uffff\ufffe\57\uffff\61\uffff\21\201\44\201\45\201\uffff\ufffe\57\uffff\30\72" +
		"\36\72\44\72\45\72\46\72\47\72\50\72\51\72\52\72\55\72\56\72\60\72\61\72\62\72\63" +
		"\72\64\72\65\72\67\72\70\72\72\72\73\72\74\72\75\72\76\72\77\72\uffff\ufffe\62\uffff" +
		"\44\205\45\205\uffff\ufffe\62\uffff\44\66\45\66\uffff\ufffe\62\uffff\44\65\45\65" +
		"\uffff\ufffe\62\uffff\44\41\45\41\uffff\ufffe\7\uffff\71\177\uffff\ufffe\57\uffff" +
		"\66\uffff\71\uffff\76\uffff\30\72\46\72\47\72\50\72\51\72\52\72\55\72\56\72\61\72" +
		"\62\72\63\72\64\72\65\72\67\72\70\72\72\72\73\72\74\72\75\72\77\72\uffff\ufffe\62" +
		"\uffff\56\206\60\206\uffff\ufffe\7\uffff\60\177\uffff\ufffe\21\uffff\44\175\45\175" +
		"\uffff\ufffe\57\uffff\61\uffff\21\173\66\173\uffff\ufffe\7\uffff\10\uffff\11\uffff" +
		"\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff" +
		"\57\uffff\60\207\uffff\ufffe\21\uffff\44\203\45\203\uffff\ufffe\7\uffff\60\215\uffff" +
		"\ufffe\1\uffff\5\211\uffff\ufffe\60\14\62\14\71\14\7\44\61\44\uffff\ufffe\62\uffff" +
		"\60\176\71\176\uffff\ufffe\57\uffff\30\102\36\102\44\102\45\102\46\102\47\102\50" +
		"\102\51\102\52\102\55\102\56\102\60\102\61\102\62\102\63\102\64\102\65\102\67\102" +
		"\70\102\72\102\73\102\74\102\75\102\76\102\77\102\uffff\ufffe\46\135\47\135\50\uffff" +
		"\51\uffff\52\uffff\72\135\73\135\74\135\75\135\30\135\36\135\44\135\45\135\56\135" +
		"\60\135\62\135\63\135\64\135\65\135\67\135\76\135\77\135\uffff\ufffe\46\136\47\136" +
		"\50\uffff\51\uffff\52\uffff\72\136\73\136\74\136\75\136\30\136\36\136\44\136\45\136" +
		"\56\136\60\136\62\136\63\136\64\136\65\136\67\136\76\136\77\136\uffff\ufffe\46\132" +
		"\47\132\50\132\51\132\52\132\72\132\73\132\74\132\75\132\30\132\36\132\44\132\45" +
		"\132\56\132\60\132\62\132\63\132\64\132\65\132\67\132\76\132\77\132\uffff\ufffe\46" +
		"\133\47\133\50\133\51\133\52\133\72\133\73\133\74\133\75\133\30\133\36\133\44\133" +
		"\45\133\56\133\60\133\62\133\63\133\64\133\65\133\67\133\76\133\77\133\uffff\ufffe" +
		"\46\134\47\134\50\134\51\134\52\134\72\134\73\134\74\134\75\134\30\134\36\134\44" +
		"\134\45\134\56\134\60\134\62\134\63\134\64\134\65\134\67\134\76\134\77\134\uffff" +
		"\ufffe\46\uffff\47\uffff\50\uffff\51\uffff\52\uffff\72\141\73\141\74\141\75\141\30" +
		"\141\36\141\44\141\45\141\56\141\60\141\62\141\63\141\64\141\65\141\67\141\76\141" +
		"\77\141\uffff\ufffe\46\uffff\47\uffff\50\uffff\51\uffff\52\uffff\72\142\73\142\74" +
		"\142\75\142\30\142\36\142\44\142\45\142\56\142\60\142\62\142\63\142\64\142\65\142" +
		"\67\142\76\142\77\142\uffff\ufffe\46\uffff\47\uffff\50\uffff\51\uffff\52\uffff\72" +
		"\137\73\137\74\137\75\137\30\137\36\137\44\137\45\137\56\137\60\137\62\137\63\137" +
		"\64\137\65\137\67\137\76\137\77\137\uffff\ufffe\46\uffff\47\uffff\50\uffff\51\uffff" +
		"\52\uffff\72\140\73\140\74\140\75\140\30\140\36\140\44\140\45\140\56\140\60\140\62" +
		"\140\63\140\64\140\65\140\67\140\76\140\77\140\uffff\ufffe\61\uffff\30\144\36\144" +
		"\44\144\45\144\56\144\60\144\62\144\63\144\64\144\65\144\67\144\76\144\77\144\uffff" +
		"\ufffe\30\uffff\36\147\44\147\45\147\56\147\60\147\62\147\63\147\64\147\65\147\67" +
		"\147\76\147\77\147\uffff\ufffe\30\uffff\36\150\44\150\45\150\56\150\60\150\62\150" +
		"\63\150\64\150\65\150\67\150\76\150\77\150\uffff\ufffe\63\152\64\152\36\152\44\152" +
		"\45\152\56\152\60\152\62\152\76\152\77\152\uffff\ufffe\63\uffff\64\153\36\153\44" +
		"\153\45\153\56\153\60\153\62\153\76\153\77\153\uffff\ufffe\21\uffff\66\175\uffff" +
		"\ufffe\36\uffff\62\uffff\44\213\45\213\uffff\ufffe\62\uffff\60\214\uffff\ufffe\7" +
		"\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47" +
		"\uffff\53\uffff\55\uffff\57\uffff\60\207\uffff\ufffe\7\uffff\10\uffff\11\uffff\20" +
		"\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57" +
		"\uffff\60\207\uffff\ufffe\61\uffff\44\20\45\20\66\20\uffff\ufffe\62\uffff\44\46\45" +
		"\46\uffff\ufffe\60\16\62\16\71\16\7\44\61\44\uffff\ufffe\54\uffff\57\uffff\30\72" +
		"\46\72\47\72\50\72\51\72\52\72\55\72\60\72\61\72\62\72\63\72\64\72\65\72\67\72\70" +
		"\72\72\72\73\72\74\72\75\72\77\72\uffff\ufffe\62\uffff\44\67\45\67\uffff\ufffe\7" +
		"\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47" +
		"\uffff\53\uffff\55\uffff\57\uffff\60\207\uffff\ufffe\36\uffff\44\213\45\213\uffff" +
		"\ufffe");

	private static final short[] lapg_sym_goto = TemplatesLexer.unpack_short(125,
		"\0\2\22\37\54\71\111\116\225\312\u0100\u0105\u0107\u0109\u010d\u010f\u0114\u0149" +
		"\u0151\u0156\u015b\u015b\u0161\u0163\u0163\u0166\u0166\u019b\u01d0\u01d1\u01d6\u01d8" +
		"\u01d9\u020e\u0243\u0248\u027d\u0289\u0292\u029c\u02db\u02e5\u02ef\u02f9\u032e\u032f" +
		"\u0366\u036a\u03ab\u03b6\u03c0\u03d7\u03da\u03dd\u03de\u03e3\u03e4\u03e5\u03e9\u03f3" +
		"\u03fd\u0407\u0411\u0416\u0417\u0417\u041c\u041d\u041e\u0420\u0422\u0424\u0426\u0428" +
		"\u042a\u042c\u042e\u0430\u0436\u043f\u044c\u0459\u045e\u045f\u0469\u046a\u046b\u0478" +
		"\u047a\u0487\u0488\u048a\u0497\u049c\u049e\u04a3\u04d8\u050d\u0542\u0544\u0547\u057c" +
		"\u05b1\u05e4\u060e\u0636\u065e\u0684\u069a\u06af\u06b5\u06b6\u06bb\u06bc\u06be\u06c0" +
		"\u06c2\u06c4\u06c5\u06c6\u06c7\u06cd\u06ce\u06d0\u06d1");

	private static final short[] lapg_sym_from = TemplatesLexer.unpack_short(1745,
		"\u010f\u0110\0\1\4\10\17\24\34\103\212\317\334\350\370\u0101\u0105\u010b\1\10\17" +
		"\24\34\103\317\334\350\370\u0101\u0105\u010b\1\10\17\24\34\103\317\334\350\370\u0101" +
		"\u0105\u010b\1\10\17\24\34\103\317\334\350\370\u0101\u0105\u010b\0\1\4\10\17\24\34" +
		"\103\301\317\334\350\370\u0101\u0105\u010b\103\334\350\u0101\u010b\16\26\32\40\41" +
		"\43\44\45\46\47\51\54\56\57\60\61\106\111\112\125\142\143\144\145\146\147\150\151" +
		"\152\153\154\155\156\157\160\161\162\163\164\165\171\172\201\204\210\211\215\222" +
		"\224\225\232\261\267\273\302\304\311\313\314\316\327\337\341\351\352\354\355\364" +
		"\366\371\u0107\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152" +
		"\153\154\155\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311" +
		"\313\314\316\327\341\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\56\57" +
		"\60\61\111\112\142\145\146\147\150\151\152\153\154\155\156\157\160\161\162\163\164" +
		"\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355" +
		"\364\366\371\u0107\16\32\165\355\371\3\55\333\355\32\165\355\371\165\355\16\32\165" +
		"\355\371\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153" +
		"\154\155\157\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313" +
		"\314\316\327\341\351\352\354\355\364\366\371\u0107\16\32\165\174\203\264\355\371" +
		"\16\32\165\355\371\16\32\165\355\371\16\32\165\256\355\371\116\120\73\247\250\16" +
		"\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157" +
		"\160\161\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327" +
		"\341\351\352\354\355\364\366\371\u0107\16\32\41\44\46\51\54\56\57\60\61\111\112\142" +
		"\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\222" +
		"\225\232\267\273\304\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107\30" +
		"\16\32\165\355\371\274\u0103\3\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146" +
		"\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\222\225\232" +
		"\267\273\304\311\313\314\316\327\341\351\352\354\355\364\366\371\u0107\16\32\41\44" +
		"\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161" +
		"\162\163\164\165\201\204\210\222\225\232\267\273\304\311\313\314\316\327\341\351" +
		"\352\354\355\364\366\371\u0107\16\32\165\355\371\16\32\41\44\46\51\54\56\57\60\61" +
		"\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201" +
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
		"\u0111\u0112\2\12\2\12\12\12\12\12\300\12\12\12\12\12\12\12\13\13\13\13\13\13\13" +
		"\13\13\13\13\13\13\14\14\14\14\14\14\14\14\14\14\14\14\14\15\15\15\15\15\15\15\15" +
		"\15\15\15\15\15\3\16\3\32\16\16\32\165\333\16\355\371\16\165\16\371\166\166\166\166" +
		"\166\35\104\35\104\114\116\35\120\35\104\35\35\114\114\130\35\104\114\114\213\35" +
		"\231\104\114\114\114\114\114\114\114\114\114\104\114\114\114\114\114\35\35\213\260" +
		"\114\114\35\275\303\114\306\114\35\104\35\114\336\35\342\114\114\35\35\361\114\35" +
		"\114\35\35\35\114\35\35\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36" +
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

	private static final short[] tmRuleLen = TemplatesLexer.unpack_short(142,
		"\1\1\2\1\1\1\3\2\11\1\6\3\1\2\3\4\2\3\2\1\1\1\1\1\1\1\1\1\1\3\1\4\3\2\1\2\1\3\2\3" +
		"\3\7\5\1\13\7\1\2\2\4\3\5\11\2\2\2\3\1\1\3\1\1\1\1\1\4\3\6\10\12\6\10\4\1\1\6\3\3" +
		"\5\3\5\1\1\1\1\1\1\2\2\1\3\3\3\3\3\3\3\3\3\1\3\3\1\3\3\1\3\3\1\5\1\3\1\3\1\3\1\1" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0");

	private static final short[] tmRuleSymbol = TemplatesLexer.unpack_short(142,
		"\102\103\103\104\104\104\105\105\106\107\110\111\112\112\112\112\113\114\115\115" +
		"\116\116\117\117\117\117\117\117\117\120\121\121\121\121\121\122\123\123\124\125" +
		"\126\127\127\127\130\130\131\131\131\132\133\134\134\134\134\135\136\136\137\137" +
		"\137\137\137\137\137\137\137\137\137\137\137\137\137\137\137\140\141\141\141\142" +
		"\142\143\143\143\144\144\145\145\145\146\146\146\146\146\146\146\146\146\146\147" +
		"\147\147\150\150\150\151\151\151\152\152\153\153\154\154\155\155\156\157\160\160" +
		"\161\161\162\162\163\163\164\164\165\165\166\166\167\167\170\170\171\171\172\172" +
		"\173\173");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"any",
		"escdollar",
		"escid",
		"escint",
		"'${'",
		"'$/'",
		"identifier",
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

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int input = 66;
		static final int definitions = 67;
		static final int definition = 68;
		static final int template_def = 69;
		static final int query_def = 70;
		static final int cached_flag = 71;
		static final int template_start = 72;
		static final int parameters = 73;
		static final int parameter_list = 74;
		static final int context_type = 75;
		static final int template_end = 76;
		static final int instructions = 77;
		static final int LsquareMinusRsquareRcurly = 78;
		static final int instruction = 79;
		static final int simple_instruction = 80;
		static final int sentence = 81;
		static final int comma_expr = 82;
		static final int qualified_id = 83;
		static final int template_for_expr = 84;
		static final int template_arguments = 85;
		static final int control_instruction = 86;
		static final int else_clause = 87;
		static final int switch_instruction = 88;
		static final int case_list = 89;
		static final int one_case = 90;
		static final int control_start = 91;
		static final int control_sentence = 92;
		static final int separator_expr = 93;
		static final int control_end = 94;
		static final int primary_expression = 95;
		static final int closure = 96;
		static final int complex_data = 97;
		static final int map_entries = 98;
		static final int map_separator = 99;
		static final int bcon = 100;
		static final int unary_expression = 101;
		static final int binary_op = 102;
		static final int instanceof_expression = 103;
		static final int equality_expression = 104;
		static final int conditional_op = 105;
		static final int conditional_expression = 106;
		static final int assignment_expression = 107;
		static final int expression = 108;
		static final int expression_list = 109;
		static final int body = 110;
		static final int syntax_problem = 111;
		static final int definitionsopt = 112;
		static final int cached_flagopt = 113;
		static final int parametersopt = 114;
		static final int context_typeopt = 115;
		static final int parameter_listopt = 116;
		static final int template_argumentsopt = 117;
		static final int template_for_expropt = 118;
		static final int comma_expropt = 119;
		static final int expression_listopt = 120;
		static final int anyopt = 121;
		static final int separator_expropt = 122;
		static final int map_entriesopt = 123;
	}

	/**
	 * -3-n   Lookahead (state id)
	 * -2     Error
	 * -1     Shift
	 * 0..n   Reduce (rule index)
	 */
	protected static int tmAction(int state, int symbol) {
		int p;
		if (tmAction[state] < -2) {
			for (p = -tmAction[state] - 3; tmLalr[p] >= 0; p += 2) {
				if (tmLalr[p] == symbol) {
					break;
				}
			}
			return tmLalr[p + 1];
		}
		return tmAction[state];
	}

	protected static int tmGoto(int state, int symbol) {
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

	protected int tmHead;
	protected Span[] tmStack;
	protected Span tmNext;
	protected TemplatesLexer tmLexer;

	private Object parse(TemplatesLexer lexer, int initialState, int finalState) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;
		int tmShiftsAfterError = 4;

		tmStack[0] = new Span();
		tmStack[0].state = initialState;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != finalState) {
			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
				tmShiftsAfterError++;
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				if (restore()) {
					if (tmShiftsAfterError >= 4) {
						reporter.error(MessageFormat.format("syntax error before line {0}", tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
					}
					if (tmShiftsAfterError <= 1) {
						tmNext = tmLexer.next();
					}
					tmShiftsAfterError = 0;
					continue;
				}
				if (tmHead < 0) {
					tmHead = 0;
					tmStack[0] = new Span();
					tmStack[0].state = initialState;
				}
				break;
			}
		}

		if (tmStack[tmHead].state != finalState) {
			if (tmShiftsAfterError >= 4) {
				reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
			}
			throw new ParseException();
		}
		return tmStack[tmHead - 1].value;
	}

	protected boolean restore() {
		if (tmNext.symbol == 0) {
			return false;
		}
		while (tmHead >= 0 && tmGoto(tmStack[tmHead].state, 65) == -1) {
			dispose(tmStack[tmHead]);
			tmStack[tmHead] = null;
			tmHead--;
		}
		if (tmHead >= 0) {
			tmStack[++tmHead] = new Span();
			tmStack[tmHead].symbol = 65;
			tmStack[tmHead].value = null;
			tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, 65);
			tmStack[tmHead].line = tmNext.line;
			tmStack[tmHead].offset = tmNext.offset;
			tmStack[tmHead].endoffset = tmNext.endoffset;
			return true;
		}
		return false;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.tokenText()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		Span left = new Span();
		left.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		left.symbol = tmRuleSymbol[rule];
		left.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		Span startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		left.line = startsym.line;
		left.offset = startsym.offset;
		left.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(left, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			cleanup(tmStack[tmHead]);
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = left;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
		switch (ruleIndex) {
			case 1:  // definitions ::= definition
				{ tmLeft.value = new ArrayList(); if (((IBundleEntity)tmStack[tmHead].value) != null) ((List<IBundleEntity>)tmLeft.value).add(((IBundleEntity)tmStack[tmHead].value)); }
				break;
			case 2:  // definitions ::= definitions definition
				{ if (((IBundleEntity)tmStack[tmHead].value) != null) ((List<IBundleEntity>)tmStack[tmHead - 1].value).add(((IBundleEntity)tmStack[tmHead].value)); }
				break;
			case 5:  // definition ::= any
				{ tmLeft.value = null; }
				break;
			case 6:  // template_def ::= template_start instructions template_end
				{ ((TemplateNode)tmStack[tmHead - 2].value).setInstructions(((ArrayList<Node>)tmStack[tmHead - 1].value)); }
				break;
			case 8:  // query_def ::= '${' cached_flagopt Lquery qualified_id parametersopt context_typeopt '=' expression '}'
				{ tmLeft.value = new QueryNode(((String)tmStack[tmHead - 5].value), ((List<ParameterNode>)tmStack[tmHead - 4].value), ((String)tmStack[tmHead - 3].value), templatePackage, ((ExpressionNode)tmStack[tmHead - 1].value), ((Boolean)tmStack[tmHead - 7].value) != null, source, tmLeft.offset, tmLeft.endoffset); checkFqn(((String)tmStack[tmHead - 5].value), tmLeft.offset, tmLeft.endoffset, tmStack[tmHead - 8].line); }
				break;
			case 9:  // cached_flag ::= Lcached
				{ tmLeft.value = Boolean.TRUE; }
				break;
			case 10:  // template_start ::= '${' Ltemplate qualified_id parametersopt context_typeopt '[-]}'
				{ tmLeft.value = new TemplateNode(((String)tmStack[tmHead - 3].value), ((List<ParameterNode>)tmStack[tmHead - 2].value), ((String)tmStack[tmHead - 1].value), templatePackage, source, tmLeft.offset, tmLeft.endoffset); checkFqn(((String)tmStack[tmHead - 3].value), tmLeft.offset, tmLeft.endoffset, tmStack[tmHead - 5].line); }
				break;
			case 11:  // parameters ::= '(' parameter_listopt ')'
				{ tmLeft.value = ((List<ParameterNode>)tmStack[tmHead - 1].value); }
				break;
			case 12:  // parameter_list ::= identifier
				{ tmLeft.value = new ArrayList(); ((List<ParameterNode>)tmLeft.value).add(new ParameterNode(null, ((String)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmLeft.endoffset)); }
				break;
			case 13:  // parameter_list ::= qualified_id identifier
				{ tmLeft.value = new ArrayList(); ((List<ParameterNode>)tmLeft.value).add(new ParameterNode(((String)tmStack[tmHead - 1].value), ((String)tmStack[tmHead].value), source, tmStack[tmHead - 1].offset, tmLeft.endoffset)); }
				break;
			case 14:  // parameter_list ::= parameter_list ',' identifier
				{ ((List<ParameterNode>)tmStack[tmHead - 2].value).add(new ParameterNode(null, ((String)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmLeft.endoffset)); }
				break;
			case 15:  // parameter_list ::= parameter_list ',' qualified_id identifier
				{ ((List<ParameterNode>)tmStack[tmHead - 3].value).add(new ParameterNode(((String)tmStack[tmHead - 1].value), ((String)tmStack[tmHead].value), source, tmStack[tmHead - 1].offset, tmLeft.endoffset)); }
				break;
			case 16:  // context_type ::= Lfor qualified_id
				{ tmLeft.value = ((String)tmStack[tmHead].value); }
				break;
			case 18:  // instructions ::= instructions instruction
				{ if (((Node)tmStack[tmHead].value) != null) ((ArrayList<Node>)tmStack[tmHead - 1].value).add(((Node)tmStack[tmHead].value)); }
				break;
			case 19:  // instructions ::= instruction
				{ tmLeft.value = new ArrayList<Node>(); if (((Node)tmStack[tmHead].value)!=null) ((ArrayList<Node>)tmLeft.value).add(((Node)tmStack[tmHead].value)); }
				break;
			case 20:  // '[-]}' ::= '-}'
				{ skipSpaces(tmStack[tmHead].offset+1); }
				break;
			case 25:  // instruction ::= escid
				{ tmLeft.value = createEscapedId(((String)tmStack[tmHead].value), tmLeft.offset, tmLeft.endoffset); }
				break;
			case 26:  // instruction ::= escint
				{ tmLeft.value = new IndexNode(null, new LiteralNode(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 27:  // instruction ::= escdollar
				{ tmLeft.value = new DollarNode(source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 28:  // instruction ::= any
				{ tmLeft.value = new TextNode(source, rawText(tmLeft.offset, tmLeft.endoffset), tmLeft.endoffset); }
				break;
			case 29:  // simple_instruction ::= '${' sentence '[-]}'
				{ tmLeft.value = ((Node)tmStack[tmHead - 1].value); }
				break;
			case 31:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				{ tmLeft.value = new CallTemplateNode(((String)tmStack[tmHead - 2].value), ((ArrayList)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead].value), templatePackage, true, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 32:  // sentence ::= Leval conditional_expression comma_expropt
				{ tmLeft.value = new EvalNode(((ExpressionNode)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 33:  // sentence ::= Lassert expression
				{ tmLeft.value = new AssertNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 34:  // sentence ::= syntax_problem
				{ tmLeft.value = null; }
				break;
			case 35:  // comma_expr ::= ',' conditional_expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 37:  // qualified_id ::= qualified_id '.' identifier
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 38:  // template_for_expr ::= Lfor expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 39:  // template_arguments ::= '(' expression_listopt ')'
				{ tmLeft.value = ((ArrayList)tmStack[tmHead - 1].value); }
				break;
			case 40:  // control_instruction ::= control_start instructions else_clause
				{ ((CompoundNode)tmStack[tmHead - 2].value).setInstructions(((ArrayList<Node>)tmStack[tmHead - 1].value)); applyElse(((CompoundNode)tmStack[tmHead - 2].value),((ElseIfNode)tmStack[tmHead].value), tmLeft.offset, tmLeft.endoffset, tmLeft.line); }
				break;
			case 41:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				{ tmLeft.value = new ElseIfNode(((ExpressionNode)tmStack[tmHead - 3].value), ((ArrayList<Node>)tmStack[tmHead - 1].value), ((ElseIfNode)tmStack[tmHead].value), source, tmStack[tmHead - 6].offset, tmStack[tmHead - 1].endoffset); }
				break;
			case 42:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				{ tmLeft.value = new ElseIfNode(null, ((ArrayList<Node>)tmStack[tmHead - 1].value), null, source, tmStack[tmHead - 4].offset, tmStack[tmHead - 1].endoffset); }
				break;
			case 43:  // else_clause ::= control_end
				{ tmLeft.value = null; }
				break;
			case 44:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				{ tmLeft.value = new SwitchNode(((ExpressionNode)tmStack[tmHead - 8].value), ((ArrayList)tmStack[tmHead - 5].value), ((ArrayList<Node>)tmStack[tmHead - 1].value), source, tmLeft.offset,tmLeft.endoffset); checkIsSpace(tmStack[tmHead - 6].offset,tmStack[tmHead - 6].endoffset, tmStack[tmHead - 6].line); }
				break;
			case 45:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				{ tmLeft.value = new SwitchNode(((ExpressionNode)tmStack[tmHead - 4].value), ((ArrayList)tmStack[tmHead - 1].value), ((ArrayList<Node>)null), source, tmLeft.offset,tmLeft.endoffset); checkIsSpace(tmStack[tmHead - 2].offset,tmStack[tmHead - 2].endoffset, tmStack[tmHead - 2].line); }
				break;
			case 46:  // case_list ::= one_case
				{ tmLeft.value = new ArrayList(); ((ArrayList)tmLeft.value).add(((CaseNode)tmStack[tmHead].value)); }
				break;
			case 47:  // case_list ::= case_list one_case
				{ ((ArrayList)tmStack[tmHead - 1].value).add(((CaseNode)tmStack[tmHead].value)); }
				break;
			case 48:  // case_list ::= case_list instruction
				{ CaseNode.add(((ArrayList)tmStack[tmHead - 1].value), ((Node)tmStack[tmHead].value)); }
				break;
			case 49:  // one_case ::= '${' Lcase expression '[-]}'
				{ tmLeft.value = new CaseNode(((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 50:  // control_start ::= '${' control_sentence '[-]}'
				{ tmLeft.value = ((CompoundNode)tmStack[tmHead - 1].value); }
				break;
			case 51:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				{ tmLeft.value = new ForeachNode(((String)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), null, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 52:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				{ tmLeft.value = new ForeachNode(((String)tmStack[tmHead - 7].value), ((ExpressionNode)tmStack[tmHead - 4].value), ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 53:  // control_sentence ::= Lif expression
				{ tmLeft.value = new IfNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 54:  // control_sentence ::= Lfile expression
				{ tmLeft.value = new FileNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 55:  // separator_expr ::= Lseparator expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 58:  // primary_expression ::= identifier
				{ tmLeft.value = new SelectNode(null, ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 59:  // primary_expression ::= '(' expression ')'
				{ tmLeft.value = new ParenthesesNode(((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 60:  // primary_expression ::= icon
				{ tmLeft.value = new LiteralNode(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 61:  // primary_expression ::= bcon
				{ tmLeft.value = new LiteralNode(((Boolean)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 62:  // primary_expression ::= ccon
				{ tmLeft.value = new LiteralNode(((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 63:  // primary_expression ::= Lself
				{ tmLeft.value = new ThisNode(source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 64:  // primary_expression ::= Lnull
				{ tmLeft.value = new LiteralNode(null, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 65:  // primary_expression ::= identifier '(' expression_listopt ')'
				{ tmLeft.value = new MethodCallNode(null, ((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 66:  // primary_expression ::= primary_expression '.' identifier
				{ tmLeft.value = new SelectNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 67:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				{ tmLeft.value = new MethodCallNode(((ExpressionNode)tmStack[tmHead - 5].value), ((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 68:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				{ tmLeft.value = createCollectionProcessor(((ExpressionNode)tmStack[tmHead - 7].value), ((String)tmStack[tmHead - 5].value), ((String)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset, tmLeft.line); }
				break;
			case 69:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ':' expression ')'
				{ tmLeft.value = createMapCollect(((ExpressionNode)tmStack[tmHead - 9].value), ((String)tmStack[tmHead - 7].value), ((String)tmStack[tmHead - 5].value), ((ExpressionNode)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset, tmLeft.line); }
				break;
			case 70:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				{ tmLeft.value = new CallTemplateNode(((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead - 5].value), templatePackage, false, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 71:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				{ tmLeft.value = new CallTemplateNode(((ExpressionNode)tmStack[tmHead - 4].value),((ArrayList)tmStack[tmHead - 1].value),((ExpressionNode)tmStack[tmHead - 7].value),templatePackage, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 72:  // primary_expression ::= primary_expression '[' expression ']'
				{ tmLeft.value = new IndexNode(((ExpressionNode)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 75:  // closure ::= '{' cached_flagopt parameter_listopt '=>' expression '}'
				{ tmLeft.value = new ClosureNode(((Boolean)tmStack[tmHead - 4].value) != null, ((List<ParameterNode>)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 76:  // complex_data ::= '[' expression_listopt ']'
				{ tmLeft.value = new ListNode(((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 77:  // complex_data ::= '[' map_entries ']'
				{ tmLeft.value = new ConcreteMapNode(((Map<String,ExpressionNode>)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 78:  // complex_data ::= Lnew qualified_id '(' map_entriesopt ')'
				{ tmLeft.value = new CreateClassNode(((String)tmStack[tmHead - 3].value), ((Map<String,ExpressionNode>)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 79:  // map_entries ::= identifier map_separator conditional_expression
				{ tmLeft.value = new LinkedHashMap(); ((Map<String,ExpressionNode>)tmLeft.value).put(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 80:  // map_entries ::= map_entries ',' identifier map_separator conditional_expression
				{ ((Map<String,ExpressionNode>)tmStack[tmHead - 4].value).put(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 84:  // bcon ::= Ltrue
				{ tmLeft.value = Boolean.TRUE; }
				break;
			case 85:  // bcon ::= Lfalse
				{ tmLeft.value = Boolean.FALSE; }
				break;
			case 87:  // unary_expression ::= '!' unary_expression
				{ tmLeft.value = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 88:  // unary_expression ::= '-' unary_expression
				{ tmLeft.value = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 90:  // binary_op ::= binary_op '*' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 91:  // binary_op ::= binary_op '/' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 92:  // binary_op ::= binary_op '%' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 93:  // binary_op ::= binary_op '+' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 94:  // binary_op ::= binary_op '-' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 95:  // binary_op ::= binary_op '<' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 96:  // binary_op ::= binary_op '>' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 97:  // binary_op ::= binary_op '<=' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 98:  // binary_op ::= binary_op '>=' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 100:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				{ tmLeft.value = new InstanceOfNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 101:  // instanceof_expression ::= instanceof_expression Lis ccon
				{ tmLeft.value = new InstanceOfNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 103:  // equality_expression ::= equality_expression '==' instanceof_expression
				{ tmLeft.value = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 104:  // equality_expression ::= equality_expression '!=' instanceof_expression
				{ tmLeft.value = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 106:  // conditional_op ::= conditional_op '&&' conditional_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 107:  // conditional_op ::= conditional_op '||' conditional_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 109:  // conditional_expression ::= conditional_op '?' conditional_expression ':' conditional_expression
				{ tmLeft.value = new TriplexNode(((ExpressionNode)tmStack[tmHead - 4].value), ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 111:  // assignment_expression ::= identifier '=' conditional_expression
				{ tmLeft.value = new AssignNode(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 113:  // expression ::= expression ',' assignment_expression
				{ tmLeft.value = new CommaNode(((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 114:  // expression_list ::= conditional_expression
				{ tmLeft.value = new ArrayList(); ((ArrayList)tmLeft.value).add(((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 115:  // expression_list ::= expression_list ',' conditional_expression
				{ ((ArrayList)tmStack[tmHead - 2].value).add(((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 116:  // body ::= instructions
				{
							tmLeft.value = new TemplateNode("inline", null, null, templatePackage, source, tmLeft.offset, tmLeft.endoffset);
							((TemplateNode)tmLeft.value).setInstructions(((ArrayList<Node>)tmStack[tmHead].value));
						}
				break;
		}
	}

	/**
	 * disposes symbol dropped by error recovery mechanism
	 */
	protected void dispose(Span value) {
	}

	/**
	 * cleans node removed from the stack
	 */
	protected void cleanup(Span value) {
	}

	public List<IBundleEntity> parseInput(TemplatesLexer lexer) throws IOException, ParseException {
		return (List<IBundleEntity>) parse(lexer, 0, 273);
	}

	public TemplateNode parseBody(TemplatesLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1, 274);
	}
}
