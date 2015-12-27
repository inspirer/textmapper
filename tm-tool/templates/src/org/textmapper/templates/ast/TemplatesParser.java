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
	private static final int[] tmAction = TemplatesLexer.unpack_int(268,
		"\ufffd\uffff\uffff\uffff\5\0\ufff5\uffff\uffed\uffff\1\0\3\0\4\0\uffff\uffff\0\0" +
		"\32\0\31\0\27\0\30\0\uffff\uffff\uffe5\uffff\21\0\26\0\24\0\25\0\uffff\uffff\11\0" +
		"\uffff\uffff\165\0\uffff\uffff\2\0\uffff\uffff\7\0\uffff\uffff\uffd7\uffff\72\0\74" +
		"\0\uffff\uffff\uffff\uffff\122\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\76\0\uffff\uffff\121\0\75\0\uffff\uffff\uffa1\uffff\uffff\uffff\uffff" +
		"\uffff\uff99\uffff\uffff\uffff\162\0\uffff\uffff\uffff\uffff\uff7b\uffff\107\0\106" +
		"\0\73\0\126\0\uff47\uffff\uff19\uffff\ufefd\uffff\ufee3\uffff\153\0\155\0\ufecd\uffff" +
		"\40\0\20\0\uffff\uffff\42\0\ufec5\uffff\uffff\uffff\uffff\uffff\6\0\ufeb9\uffff\uffff" +
		"\uffff\ufe9b\uffff\ufe8f\uffff\ufe59\uffff\uffff\uffff\ufe51\uffff\uffff\uffff\ufe49" +
		"\uffff\uffff\uffff\uffff\uffff\ufe41\uffff\ufe39\uffff\125\0\124\0\ufe33\uffff\uffff" +
		"\uffff\157\0\ufe01\uffff\uffff\uffff\uffff\uffff\23\0\22\0\33\0\60\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\67\0\46\0\51\0\ufdf9" +
		"\uffff\uffff\uffff\167\0\ufdf3\uffff\ufdeb\uffff\17\0\uffff\uffff\154\0\ufde1\uffff" +
		"\175\0\ufdc3\uffff\uffff\uffff\201\0\36\0\uffff\uffff\uffff\uffff\ufdbb\uffff\ufdb5" +
		"\uffff\14\0\ufdaf\uffff\uffff\uffff\117\0\120\0\116\0\uffff\uffff\112\0\uffff\uffff" +
		"\uffff\uffff\111\0\71\0\uffff\uffff\ufda7\uffff\uffff\uffff\uffff\uffff\ufd71\uffff" +
		"\ufd43\uffff\ufd15\uffff\ufce7\uffff\ufcb9\uffff\ufc8b\uffff\ufc5d\uffff\ufc2f\uffff" +
		"\ufc01\uffff\142\0\ufbd3\uffff\ufbb5\uffff\ufb99\uffff\ufb7d\uffff\ufb67\uffff\uffff" +
		"\uffff\156\0\uffff\uffff\uffff\uffff\uffff\uffff\43\0\uffff\uffff\171\0\uffff\uffff" +
		"\ufb51\uffff\77\0\uffff\uffff\uffff\uffff\177\0\35\0\41\0\uffff\uffff\ufb4b\uffff" +
		"\uffff\uffff\ufb41\uffff\uffff\uffff\205\0\uffff\uffff\uffff\uffff\uffff\uffff\114" +
		"\0\uffff\uffff\160\0\105\0\ufb3b\uffff\uffff\uffff\ufb1d\uffff\uffff\uffff\66\0\uffff" +
		"\uffff\uffff\uffff\13\0\ufaff\uffff\12\0\uffff\uffff\45\0\ufaf5\uffff\uffff\uffff" +
		"\uffff\uffff\207\0\61\0\113\0\uffff\uffff\uffff\uffff\54\0\15\0\uffff\uffff\uffff" +
		"\uffff\ufaed\uffff\uffff\uffff\uffff\uffff\uffff\uffff\152\0\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\ufabf\uffff\uffff\uffff\uffff\uffff\56\0\55\0\53\0\110\0" +
		"\115\0\uffff\uffff\101\0\ufab7\uffff\103\0\uffff\uffff\uffff\uffff\50\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\10\0\ufa99" +
		"\uffff\57\0\uffff\uffff\102\0\104\0\47\0\62\0\uffff\uffff\52\0\uffff\uffff\uffff" +
		"\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TemplatesLexer.unpack_short(1388,
		"\1\uffff\5\uffff\0\164\uffff\ufffe\13\uffff\37\uffff\34\166\uffff\ufffe\1\uffff\5" +
		"\uffff\0\163\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\5\uffff\0\161\uffff\ufffe" +
		"\57\uffff\66\uffff\30\70\36\70\44\70\45\70\46\70\47\70\50\70\51\70\52\70\55\70\56" +
		"\70\60\70\61\70\62\70\63\70\64\70\65\70\67\70\70\70\72\70\73\70\74\70\75\70\77\70" +
		"\uffff\ufffe\13\uffff\7\166\71\166\uffff\ufffe\7\uffff\10\uffff\11\uffff\20\uffff" +
		"\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff" +
		"\56\204\uffff\ufffe\55\uffff\61\uffff\70\uffff\30\123\36\123\44\123\45\123\46\123" +
		"\47\123\50\123\51\123\52\123\56\123\60\123\62\123\63\123\64\123\65\123\67\123\72" +
		"\123\73\123\74\123\75\123\76\123\77\123\uffff\ufffe\46\uffff\47\uffff\50\uffff\51" +
		"\uffff\52\uffff\72\uffff\73\uffff\74\uffff\75\uffff\30\140\36\140\44\140\45\140\56" +
		"\140\60\140\62\140\63\140\64\140\65\140\67\140\76\140\77\140\uffff\ufffe\30\uffff" +
		"\36\143\44\143\45\143\56\143\60\143\62\143\63\143\64\143\65\143\67\143\76\143\77" +
		"\143\uffff\ufffe\65\uffff\67\uffff\36\146\44\146\45\146\56\146\60\146\62\146\63\146" +
		"\64\146\76\146\77\146\uffff\ufffe\63\uffff\64\uffff\77\uffff\36\151\44\151\45\151" +
		"\56\151\60\151\62\151\76\151\uffff\ufffe\62\uffff\44\34\45\34\uffff\ufffe\57\uffff" +
		"\61\uffff\21\170\44\170\45\170\uffff\ufffe\7\uffff\10\uffff\11\uffff\20\uffff\32" +
		"\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60" +
		"\204\uffff\ufffe\57\uffff\61\uffff\21\176\44\176\45\176\uffff\ufffe\57\uffff\30\70" +
		"\36\70\44\70\45\70\46\70\47\70\50\70\51\70\52\70\55\70\56\70\60\70\61\70\62\70\63" +
		"\70\64\70\65\70\67\70\70\70\72\70\73\70\74\70\75\70\76\70\77\70\uffff\ufffe\62\uffff" +
		"\44\202\45\202\uffff\ufffe\62\uffff\44\64\45\64\uffff\ufffe\62\uffff\44\63\45\63" +
		"\uffff\ufffe\62\uffff\44\37\45\37\uffff\ufffe\7\uffff\71\174\uffff\ufffe\57\uffff" +
		"\66\uffff\71\uffff\76\uffff\30\70\46\70\47\70\50\70\51\70\52\70\55\70\56\70\61\70" +
		"\62\70\63\70\64\70\65\70\67\70\70\70\72\70\73\70\74\70\75\70\77\70\uffff\ufffe\62" +
		"\uffff\56\203\60\203\uffff\ufffe\7\uffff\60\174\uffff\ufffe\21\uffff\44\172\45\172" +
		"\uffff\ufffe\57\uffff\61\uffff\21\170\66\170\uffff\ufffe\7\uffff\10\uffff\11\uffff" +
		"\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff" +
		"\57\uffff\60\204\uffff\ufffe\21\uffff\44\200\45\200\uffff\ufffe\7\uffff\60\212\uffff" +
		"\ufffe\1\uffff\5\206\uffff\ufffe\62\uffff\60\173\71\173\uffff\ufffe\57\uffff\30\100" +
		"\36\100\44\100\45\100\46\100\47\100\50\100\51\100\52\100\55\100\56\100\60\100\61" +
		"\100\62\100\63\100\64\100\65\100\67\100\70\100\72\100\73\100\74\100\75\100\76\100" +
		"\77\100\uffff\ufffe\46\132\47\132\50\uffff\51\uffff\52\uffff\72\132\73\132\74\132" +
		"\75\132\30\132\36\132\44\132\45\132\56\132\60\132\62\132\63\132\64\132\65\132\67" +
		"\132\76\132\77\132\uffff\ufffe\46\133\47\133\50\uffff\51\uffff\52\uffff\72\133\73" +
		"\133\74\133\75\133\30\133\36\133\44\133\45\133\56\133\60\133\62\133\63\133\64\133" +
		"\65\133\67\133\76\133\77\133\uffff\ufffe\46\127\47\127\50\127\51\127\52\127\72\127" +
		"\73\127\74\127\75\127\30\127\36\127\44\127\45\127\56\127\60\127\62\127\63\127\64" +
		"\127\65\127\67\127\76\127\77\127\uffff\ufffe\46\130\47\130\50\130\51\130\52\130\72" +
		"\130\73\130\74\130\75\130\30\130\36\130\44\130\45\130\56\130\60\130\62\130\63\130" +
		"\64\130\65\130\67\130\76\130\77\130\uffff\ufffe\46\131\47\131\50\131\51\131\52\131" +
		"\72\131\73\131\74\131\75\131\30\131\36\131\44\131\45\131\56\131\60\131\62\131\63" +
		"\131\64\131\65\131\67\131\76\131\77\131\uffff\ufffe\46\uffff\47\uffff\50\uffff\51" +
		"\uffff\52\uffff\72\136\73\136\74\136\75\136\30\136\36\136\44\136\45\136\56\136\60" +
		"\136\62\136\63\136\64\136\65\136\67\136\76\136\77\136\uffff\ufffe\46\uffff\47\uffff" +
		"\50\uffff\51\uffff\52\uffff\72\137\73\137\74\137\75\137\30\137\36\137\44\137\45\137" +
		"\56\137\60\137\62\137\63\137\64\137\65\137\67\137\76\137\77\137\uffff\ufffe\46\uffff" +
		"\47\uffff\50\uffff\51\uffff\52\uffff\72\134\73\134\74\134\75\134\30\134\36\134\44" +
		"\134\45\134\56\134\60\134\62\134\63\134\64\134\65\134\67\134\76\134\77\134\uffff" +
		"\ufffe\46\uffff\47\uffff\50\uffff\51\uffff\52\uffff\72\135\73\135\74\135\75\135\30" +
		"\135\36\135\44\135\45\135\56\135\60\135\62\135\63\135\64\135\65\135\67\135\76\135" +
		"\77\135\uffff\ufffe\61\uffff\30\141\36\141\44\141\45\141\56\141\60\141\62\141\63" +
		"\141\64\141\65\141\67\141\76\141\77\141\uffff\ufffe\30\uffff\36\144\44\144\45\144" +
		"\56\144\60\144\62\144\63\144\64\144\65\144\67\144\76\144\77\144\uffff\ufffe\30\uffff" +
		"\36\145\44\145\45\145\56\145\60\145\62\145\63\145\64\145\65\145\67\145\76\145\77" +
		"\145\uffff\ufffe\63\147\64\147\36\147\44\147\45\147\56\147\60\147\62\147\76\147\77" +
		"\147\uffff\ufffe\63\uffff\64\150\36\150\44\150\45\150\56\150\60\150\62\150\76\150" +
		"\77\150\uffff\ufffe\21\uffff\66\172\uffff\ufffe\36\uffff\62\uffff\44\210\45\210\uffff" +
		"\ufffe\62\uffff\60\211\uffff\ufffe\7\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33" +
		"\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60\204\uffff" +
		"\ufffe\7\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43" +
		"\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60\204\uffff\ufffe\61\uffff\44\16\45\16" +
		"\66\16\uffff\ufffe\62\uffff\44\44\45\44\uffff\ufffe\54\uffff\57\uffff\30\70\46\70" +
		"\47\70\50\70\51\70\52\70\55\70\60\70\61\70\62\70\63\70\64\70\65\70\67\70\70\70\72" +
		"\70\73\70\74\70\75\70\77\70\uffff\ufffe\62\uffff\44\65\45\65\uffff\ufffe\7\uffff" +
		"\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff" +
		"\53\uffff\55\uffff\57\uffff\60\204\uffff\ufffe\36\uffff\44\210\45\210\uffff\ufffe");

	private static final short[] lapg_sym_goto = TemplatesLexer.unpack_short(125,
		"\0\2\22\37\54\71\111\116\222\306\373\u0100\u0102\u0104\u0108\u010a\u010f\u0143\u014b" +
		"\u0150\u0155\u0155\u015b\u015d\u015d\u0160\u0160\u0194\u01c8\u01c9\u01ce\u01d0\u01d1" +
		"\u0205\u0239\u023e\u0272\u027e\u0287\u0291\u02cf\u02d9\u02e3\u02ed\u0321\u0322\u0358" +
		"\u035c\u039c\u03a6\u03ae\u03c4\u03c7\u03ca\u03cb\u03d0\u03d1\u03d2\u03d6\u03e0\u03ea" +
		"\u03f4\u03fe\u0402\u0403\u0403\u0408\u0409\u040a\u040c\u040e\u0410\u0412\u0414\u0416" +
		"\u0418\u041a\u041c\u0422\u042b\u0438\u0445\u044a\u044b\u0452\u0453\u0454\u0461\u0463" +
		"\u0470\u0471\u0473\u0480\u0485\u0487\u048c\u04c0\u04f4\u0528\u052a\u052d\u0561\u0595" +
		"\u05c7\u05f0\u0617\u063e\u0663\u0678\u068c\u0692\u0693\u0698\u0699\u069b\u069d\u069f" +
		"\u06a1\u06a2\u06a3\u06a4\u06aa\u06ab\u06ad\u06ae");

	private static final short[] lapg_sym_from = TemplatesLexer.unpack_short(1710,
		"\u0108\u0109\0\1\4\10\17\24\34\103\212\315\332\345\364\375\u0101\u0106\1\10\17\24" +
		"\34\103\315\332\345\364\375\u0101\u0106\1\10\17\24\34\103\315\332\345\364\375\u0101" +
		"\u0106\1\10\17\24\34\103\315\332\345\364\375\u0101\u0106\0\1\4\10\17\24\34\103\300" +
		"\315\332\345\364\375\u0101\u0106\103\332\345\375\u0106\16\26\32\40\41\43\44\45\46" +
		"\47\51\54\56\57\60\61\106\111\112\125\142\143\144\145\146\147\150\151\152\153\154" +
		"\155\156\157\160\161\162\163\164\165\171\172\201\204\210\211\221\223\224\231\260" +
		"\266\272\301\302\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\41\44" +
		"\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161" +
		"\162\163\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336\346" +
		"\347\351\352\360\362\365\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147" +
		"\150\151\152\153\154\155\156\157\160\161\162\163\164\165\201\204\210\221\224\231" +
		"\266\272\302\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\165\352\365" +
		"\3\55\331\352\32\165\352\365\165\352\16\32\165\352\365\16\32\41\44\46\51\54\56\57" +
		"\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165" +
		"\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336\346\347\351\352\360" +
		"\362\365\16\32\165\174\203\263\352\365\16\32\165\352\365\16\32\165\352\365\16\32" +
		"\165\255\352\365\116\120\73\246\247\16\32\41\44\46\51\54\56\57\60\61\111\112\142" +
		"\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221" +
		"\224\231\266\272\302\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\41" +
		"\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160" +
		"\161\162\163\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336" +
		"\346\347\351\352\360\362\365\30\16\32\165\352\365\273\377\3\16\32\41\44\46\51\54" +
		"\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163" +
		"\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336\346\347\351" +
		"\352\360\362\365\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151" +
		"\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224\231\266\272\302" +
		"\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\165\352\365\16\32\41\44" +
		"\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161" +
		"\162\163\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336\346" +
		"\347\351\352\360\362\365\63\64\107\123\254\255\262\335\344\367\371\372\63\64\123" +
		"\254\255\262\344\371\372\72\233\234\235\236\237\240\241\242\243\16\32\41\44\46\51" +
		"\54\56\57\60\61\72\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162" +
		"\163\164\165\201\204\210\221\224\231\233\234\235\236\237\240\241\242\243\266\272" +
		"\302\307\311\312\314\325\336\346\347\351\352\360\362\365\72\233\234\235\236\237\240" +
		"\241\242\243\72\233\234\235\236\237\240\241\242\243\72\233\234\235\236\237\240\241" +
		"\242\243\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153" +
		"\154\155\157\160\161\162\163\164\165\201\204\210\221\224\231\266\272\302\307\311" +
		"\312\314\325\336\346\347\351\352\360\362\365\337\16\32\41\44\46\51\54\56\57\60\61" +
		"\65\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201" +
		"\204\207\210\221\224\231\266\272\302\307\311\312\314\325\336\346\347\351\352\360" +
		"\362\365\131\134\227\370\16\32\35\41\44\46\51\54\56\57\60\61\105\111\112\113\114" +
		"\122\130\142\144\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165" +
		"\175\201\204\210\221\224\230\231\232\266\272\302\307\311\312\314\325\336\337\341" +
		"\346\347\351\352\360\362\365\135\177\256\265\276\310\340\342\373\374\65\105\113\122" +
		"\175\232\245\317\100\115\117\121\123\124\131\133\135\214\227\273\275\310\323\324" +
		"\335\344\350\367\371\373\75\250\251\75\250\251\74\35\130\274\304\321\74\65\130\215" +
		"\274\304\72\233\234\235\236\237\240\241\242\243\72\233\234\235\236\237\240\241\242" +
		"\243\72\233\234\235\236\237\240\241\242\243\72\233\234\235\236\237\240\241\242\243" +
		"\130\252\274\304\75\16\32\165\352\365\0\0\0\4\0\4\0\4\3\55\0\4\105\175\125\171\174" +
		"\263\10\34\1\10\24\315\364\u0101\63\64\123\254\255\262\344\371\372\1\10\17\24\34" +
		"\103\315\332\345\364\375\u0101\u0106\1\10\17\24\34\103\315\332\345\364\375\u0101" +
		"\u0106\16\32\165\352\365\115\26\40\47\106\144\156\260\203\113\1\10\17\24\34\103\315" +
		"\332\345\364\375\u0101\u0106\103\375\1\10\17\24\34\103\315\332\345\364\375\u0101" +
		"\u0106\300\300\332\1\10\17\24\34\103\315\332\345\364\375\u0101\u0106\16\32\165\352" +
		"\365\273\377\103\332\345\375\u0106\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145" +
		"\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224" +
		"\231\266\272\302\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\41\44" +
		"\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161" +
		"\162\163\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336\346" +
		"\347\351\352\360\362\365\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147" +
		"\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224\231\266" +
		"\272\302\307\311\312\314\325\336\346\347\351\352\360\362\365\60\211\130\274\304\16" +
		"\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157" +
		"\160\161\162\163\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325" +
		"\336\346\347\351\352\360\362\365\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145" +
		"\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224" +
		"\231\266\272\302\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\41\44" +
		"\46\51\54\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163" +
		"\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336\346\347\351" +
		"\352\360\362\365\16\32\41\44\46\51\54\60\61\111\112\142\157\160\161\162\163\164\165" +
		"\201\204\210\221\224\231\266\272\302\307\311\312\314\325\336\346\347\351\352\360" +
		"\362\365\16\32\41\44\46\51\54\60\61\111\112\142\161\162\163\164\165\201\204\210\221" +
		"\224\231\266\272\302\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\41" +
		"\44\46\51\54\60\61\111\112\142\161\162\163\164\165\201\204\210\221\224\231\266\272" +
		"\302\307\311\312\314\325\336\346\347\351\352\360\362\365\16\32\41\44\46\51\54\60" +
		"\61\111\112\142\163\164\165\201\204\210\221\224\231\266\272\302\307\311\312\314\325" +
		"\336\346\347\351\352\360\362\365\16\32\44\46\51\54\61\142\164\165\210\231\266\302" +
		"\314\325\346\351\352\360\365\16\32\44\46\51\54\61\142\165\210\231\266\302\314\325" +
		"\346\351\352\360\365\60\111\201\307\311\362\1\16\32\165\352\365\0\3\55\105\175\174" +
		"\263\125\171\113\203\115\60\111\201\307\311\362\212\273\377\211");

	private static final short[] lapg_sym_to = TemplatesLexer.unpack_short(1710,
		"\u010a\u010b\2\12\2\12\12\12\12\12\277\12\12\12\12\12\12\12\13\13\13\13\13\13\13" +
		"\13\13\13\13\13\13\14\14\14\14\14\14\14\14\14\14\14\14\14\15\15\15\15\15\15\15\15" +
		"\15\15\15\15\15\3\16\3\32\16\16\32\165\331\16\352\365\16\165\16\365\166\166\166\166" +
		"\166\35\104\35\104\114\116\35\120\35\104\35\35\114\114\130\35\104\114\114\213\35" +
		"\230\104\114\114\114\114\114\114\114\114\114\104\114\114\114\114\114\35\35\213\257" +
		"\114\114\35\274\114\304\114\35\104\35\114\334\35\337\114\114\35\35\114\35\114\35" +
		"\35\35\114\35\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36" +
		"\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36" +
		"\36\36\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\244\37" +
		"\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37" +
		"\37\40\40\40\40\40\25\25\351\351\107\254\254\254\255\372\41\41\41\41\41\42\42\42" +
		"\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42" +
		"\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\43\43\43\260\266" +
		"\260\43\43\44\44\44\44\44\45\45\45\45\45\46\46\46\314\46\46\207\210\156\156\156\47" +
		"\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47" +
		"\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\50\50\50" +
		"\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50" +
		"\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\106\51\51\51\51" +
		"\51\325\325\26\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52" +
		"\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52" +
		"\52\52\52\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53" +
		"\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53" +
		"\53\54\54\54\54\54\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55" +
		"\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55" +
		"\55\55\55\55\136\136\176\136\136\136\136\356\136\376\136\136\137\137\137\137\137" +
		"\137\137\137\137\145\145\145\145\145\145\145\145\145\145\56\56\56\56\56\56\56\56" +
		"\56\56\56\146\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56" +
		"\56\56\146\146\146\146\146\146\146\146\146\56\56\56\56\56\56\56\56\56\56\56\56\56" +
		"\56\56\56\147\147\147\147\147\147\147\147\147\147\150\150\150\150\150\150\150\150" +
		"\150\150\151\151\151\151\151\151\151\151\151\151\57\57\57\57\57\57\57\57\57\57\57" +
		"\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57" +
		"\57\57\57\57\57\57\57\57\57\57\57\57\57\57\360\60\60\60\60\60\60\60\60\60\60\60\142" +
		"\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\272\60\60\60\60\60" +
		"\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\222\225\306\377\61\61\111\61\61\61" +
		"\61\61\61\61\61\61\171\61\61\201\111\211\111\61\231\61\61\61\61\61\61\61\61\61\61" +
		"\61\61\61\61\61\61\171\61\61\61\61\61\307\61\311\61\61\61\61\61\61\61\61\61\111\362" +
		"\61\61\61\61\61\61\61\226\264\316\322\330\341\361\363\u0102\u0103\143\172\172\172" +
		"\172\172\172\172\164\204\164\164\164\164\223\224\164\301\164\164\223\164\164\347" +
		"\164\164\164\164\164\164\161\161\161\162\162\162\157\112\216\216\216\346\160\144" +
		"\217\302\217\217\152\152\152\152\152\152\152\152\152\152\153\153\153\153\153\153" +
		"\153\153\153\153\154\154\154\154\154\154\154\154\154\154\155\155\155\155\155\155" +
		"\155\155\155\155\220\312\220\220\163\62\62\62\62\62\u0108\4\5\31\6\6\7\7\27\27\10" +
		"\10\173\173\214\214\261\261\33\110\17\34\103\345\375\u0106\140\141\212\313\315\320" +
		"\364\u0100\u0101\20\20\102\20\102\102\20\353\102\20\102\20\102\21\21\21\21\21\21" +
		"\21\21\21\21\21\21\21\63\63\63\63\63\205\105\113\122\175\232\245\317\267\202\22\22" +
		"\22\22\22\22\22\22\22\22\22\22\22\167\u0104\23\23\23\23\23\23\23\23\23\23\23\23\23" +
		"\332\333\354\24\24\24\24\24\24\24\24\24\24\24\24\24\64\64\64\64\64\326\326\170\355" +
		"\366\170\u0107\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65" +
		"\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65" +
		"\65\65\65\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66" +
		"\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66" +
		"\66\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67" +
		"\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\131" +
		"\275\221\221\336\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70" +
		"\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70" +
		"\70\70\70\71\71\71\71\71\71\71\126\127\71\71\71\71\71\71\71\71\71\71\71\71\71\71" +
		"\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71" +
		"\71\71\72\72\72\72\72\72\72\72\72\72\72\72\233\234\235\236\237\240\241\242\243\72" +
		"\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72" +
		"\72\73\73\73\73\73\73\73\73\73\73\73\73\246\247\73\73\73\73\73\73\73\73\73\73\73" +
		"\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\74\74\74\74\74\74\74\74\74\74\74" +
		"\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74" +
		"\74\75\75\75\75\75\75\75\75\75\75\75\75\250\251\75\75\75\75\75\75\75\75\75\75\75" +
		"\75\75\75\75\75\75\75\75\75\75\75\75\75\75\76\76\115\76\76\76\76\132\76\132\200\76" +
		"\252\76\76\132\271\76\303\305\76\76\324\76\132\132\343\76\76\357\76\370\76\76\76" +
		"\132\76\77\77\77\77\77\77\77\77\253\77\77\77\77\77\77\77\77\77\77\77\77\100\100\117" +
		"\121\123\124\135\227\100\273\310\323\335\344\350\367\371\100\373\100\133\133\133" +
		"\133\133\133\u0109\101\101\101\101\101\11\30\125\174\263\262\321\215\256\203\270" +
		"\206\134\177\265\340\342\374\300\327\u0105\276");

	private static final short[] tmRuleLen = TemplatesLexer.unpack_short(139,
		"\1\1\2\1\1\1\3\2\11\1\6\3\1\3\2\3\2\1\1\1\1\1\1\1\1\1\1\3\1\4\3\2\1\2\1\3\2\3\3\7" +
		"\5\1\13\7\1\2\2\4\3\5\11\2\2\2\3\1\1\3\1\1\1\1\1\4\3\6\10\6\10\4\1\1\6\3\3\5\3\5" +
		"\1\1\1\1\1\1\2\2\1\3\3\3\3\3\3\3\3\3\1\3\3\1\3\3\1\3\3\1\5\1\3\1\3\1\3\1\1\1\0\1" +
		"\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0");

	private static final short[] tmRuleSymbol = TemplatesLexer.unpack_short(139,
		"\102\103\103\104\104\104\105\105\106\107\110\111\112\112\113\114\115\115\116\116" +
		"\117\117\117\117\117\117\117\120\121\121\121\121\121\122\123\123\124\125\126\127" +
		"\127\127\130\130\131\131\131\132\133\134\134\134\134\135\136\136\137\137\137\137" +
		"\137\137\137\137\137\137\137\137\137\137\137\137\140\141\141\141\142\142\143\143" +
		"\143\144\144\145\145\145\146\146\146\146\146\146\146\146\146\146\147\147\147\150" +
		"\150\150\151\151\151\152\152\153\153\154\154\155\155\156\157\160\160\161\161\162" +
		"\162\163\163\164\164\165\165\166\166\167\167\170\170\171\171\172\172\173\173");

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
		int input = 66;
		int definitions = 67;
		int definition = 68;
		int template_def = 69;
		int query_def = 70;
		int cached_flag = 71;
		int template_start = 72;
		int parameters = 73;
		int parameter_list = 74;
		int context_type = 75;
		int template_end = 76;
		int instructions = 77;
		int LsquareMinusRsquareRcurly = 78;
		int instruction = 79;
		int simple_instruction = 80;
		int sentence = 81;
		int comma_expr = 82;
		int qualified_id = 83;
		int template_for_expr = 84;
		int template_arguments = 85;
		int control_instruction = 86;
		int else_clause = 87;
		int switch_instruction = 88;
		int case_list = 89;
		int one_case = 90;
		int control_start = 91;
		int control_sentence = 92;
		int separator_expr = 93;
		int control_end = 94;
		int primary_expression = 95;
		int closure = 96;
		int complex_data = 97;
		int map_entries = 98;
		int map_separator = 99;
		int bcon = 100;
		int unary_expression = 101;
		int binary_op = 102;
		int instanceof_expression = 103;
		int equality_expression = 104;
		int conditional_op = 105;
		int conditional_expression = 106;
		int assignment_expression = 107;
		int expression = 108;
		int expression_list = 109;
		int body = 110;
		int syntax_problem = 111;
		int definitionsopt = 112;
		int cached_flagopt = 113;
		int parametersopt = 114;
		int context_typeopt = 115;
		int parameter_listopt = 116;
		int template_argumentsopt = 117;
		int template_for_expropt = 118;
		int comma_expropt = 119;
		int expression_listopt = 120;
		int anyopt = 121;
		int separator_expropt = 122;
		int map_entriesopt = 123;
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
				{ tmLeft.value = new ArrayList(); ((List<ParameterNode>)tmLeft.value).add(new ParameterNode(((String)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmLeft.endoffset)); }
				break;
			case 13:  // parameter_list ::= parameter_list ',' identifier
				{ ((List<ParameterNode>)tmStack[tmHead - 2].value).add(new ParameterNode(((String)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmLeft.endoffset)); }
				break;
			case 14:  // context_type ::= Lfor qualified_id
				{ tmLeft.value = ((String)tmStack[tmHead].value); }
				break;
			case 16:  // instructions ::= instructions instruction
				{ if (((Node)tmStack[tmHead].value) != null) ((ArrayList<Node>)tmStack[tmHead - 1].value).add(((Node)tmStack[tmHead].value)); }
				break;
			case 17:  // instructions ::= instruction
				{ tmLeft.value = new ArrayList<Node>(); if (((Node)tmStack[tmHead].value)!=null) ((ArrayList<Node>)tmLeft.value).add(((Node)tmStack[tmHead].value)); }
				break;
			case 18:  // '[-]}' ::= '-}'
				{ skipSpaces(tmStack[tmHead].offset+1); }
				break;
			case 23:  // instruction ::= escid
				{ tmLeft.value = createEscapedId(((String)tmStack[tmHead].value), tmLeft.offset, tmLeft.endoffset); }
				break;
			case 24:  // instruction ::= escint
				{ tmLeft.value = new IndexNode(null, new LiteralNode(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 25:  // instruction ::= escdollar
				{ tmLeft.value = new DollarNode(source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 26:  // instruction ::= any
				{ tmLeft.value = new TextNode(source, rawText(tmLeft.offset, tmLeft.endoffset), tmLeft.endoffset); }
				break;
			case 27:  // simple_instruction ::= '${' sentence '[-]}'
				{ tmLeft.value = ((Node)tmStack[tmHead - 1].value); }
				break;
			case 29:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				{ tmLeft.value = new CallTemplateNode(((String)tmStack[tmHead - 2].value), ((ArrayList)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead].value), templatePackage, true, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 30:  // sentence ::= Leval conditional_expression comma_expropt
				{ tmLeft.value = new EvalNode(((ExpressionNode)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 31:  // sentence ::= Lassert expression
				{ tmLeft.value = new AssertNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 32:  // sentence ::= syntax_problem
				{ tmLeft.value = null; }
				break;
			case 33:  // comma_expr ::= ',' conditional_expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 35:  // qualified_id ::= qualified_id '.' identifier
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 36:  // template_for_expr ::= Lfor expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 37:  // template_arguments ::= '(' expression_listopt ')'
				{ tmLeft.value = ((ArrayList)tmStack[tmHead - 1].value); }
				break;
			case 38:  // control_instruction ::= control_start instructions else_clause
				{ ((CompoundNode)tmStack[tmHead - 2].value).setInstructions(((ArrayList<Node>)tmStack[tmHead - 1].value)); applyElse(((CompoundNode)tmStack[tmHead - 2].value),((ElseIfNode)tmStack[tmHead].value), tmLeft.offset, tmLeft.endoffset, tmLeft.line); }
				break;
			case 39:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				{ tmLeft.value = new ElseIfNode(((ExpressionNode)tmStack[tmHead - 3].value), ((ArrayList<Node>)tmStack[tmHead - 1].value), ((ElseIfNode)tmStack[tmHead].value), source, tmStack[tmHead - 6].offset, tmStack[tmHead - 1].endoffset); }
				break;
			case 40:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				{ tmLeft.value = new ElseIfNode(null, ((ArrayList<Node>)tmStack[tmHead - 1].value), null, source, tmStack[tmHead - 4].offset, tmStack[tmHead - 1].endoffset); }
				break;
			case 41:  // else_clause ::= control_end
				{ tmLeft.value = null; }
				break;
			case 42:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				{ tmLeft.value = new SwitchNode(((ExpressionNode)tmStack[tmHead - 8].value), ((ArrayList)tmStack[tmHead - 5].value), ((ArrayList<Node>)tmStack[tmHead - 1].value), source, tmLeft.offset,tmLeft.endoffset); checkIsSpace(tmStack[tmHead - 6].offset,tmStack[tmHead - 6].endoffset, tmStack[tmHead - 6].line); }
				break;
			case 43:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				{ tmLeft.value = new SwitchNode(((ExpressionNode)tmStack[tmHead - 4].value), ((ArrayList)tmStack[tmHead - 1].value), ((ArrayList<Node>)null), source, tmLeft.offset,tmLeft.endoffset); checkIsSpace(tmStack[tmHead - 2].offset,tmStack[tmHead - 2].endoffset, tmStack[tmHead - 2].line); }
				break;
			case 44:  // case_list ::= one_case
				{ tmLeft.value = new ArrayList(); ((ArrayList)tmLeft.value).add(((CaseNode)tmStack[tmHead].value)); }
				break;
			case 45:  // case_list ::= case_list one_case
				{ ((ArrayList)tmStack[tmHead - 1].value).add(((CaseNode)tmStack[tmHead].value)); }
				break;
			case 46:  // case_list ::= case_list instruction
				{ CaseNode.add(((ArrayList)tmStack[tmHead - 1].value), ((Node)tmStack[tmHead].value)); }
				break;
			case 47:  // one_case ::= '${' Lcase expression '[-]}'
				{ tmLeft.value = new CaseNode(((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 48:  // control_start ::= '${' control_sentence '[-]}'
				{ tmLeft.value = ((CompoundNode)tmStack[tmHead - 1].value); }
				break;
			case 49:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				{ tmLeft.value = new ForeachNode(((String)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), null, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 50:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				{ tmLeft.value = new ForeachNode(((String)tmStack[tmHead - 7].value), ((ExpressionNode)tmStack[tmHead - 4].value), ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 51:  // control_sentence ::= Lif expression
				{ tmLeft.value = new IfNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 52:  // control_sentence ::= Lfile expression
				{ tmLeft.value = new FileNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 53:  // separator_expr ::= Lseparator expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 56:  // primary_expression ::= identifier
				{ tmLeft.value = new SelectNode(null, ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 57:  // primary_expression ::= '(' expression ')'
				{ tmLeft.value = new ParenthesesNode(((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 58:  // primary_expression ::= icon
				{ tmLeft.value = new LiteralNode(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 59:  // primary_expression ::= bcon
				{ tmLeft.value = new LiteralNode(((Boolean)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 60:  // primary_expression ::= ccon
				{ tmLeft.value = new LiteralNode(((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 61:  // primary_expression ::= Lself
				{ tmLeft.value = new ThisNode(source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 62:  // primary_expression ::= Lnull
				{ tmLeft.value = new LiteralNode(null, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 63:  // primary_expression ::= identifier '(' expression_listopt ')'
				{ tmLeft.value = new MethodCallNode(null, ((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 64:  // primary_expression ::= primary_expression '.' identifier
				{ tmLeft.value = new SelectNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 65:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				{ tmLeft.value = new MethodCallNode(((ExpressionNode)tmStack[tmHead - 5].value), ((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 66:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				{ tmLeft.value = createCollectionProcessor(((ExpressionNode)tmStack[tmHead - 7].value), ((String)tmStack[tmHead - 5].value), ((String)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset, tmLeft.line); }
				break;
			case 67:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				{ tmLeft.value = new CallTemplateNode(((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead - 5].value), templatePackage, false, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 68:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				{ tmLeft.value = new CallTemplateNode(((ExpressionNode)tmStack[tmHead - 4].value),((ArrayList)tmStack[tmHead - 1].value),((ExpressionNode)tmStack[tmHead - 7].value),templatePackage, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 69:  // primary_expression ::= primary_expression '[' expression ']'
				{ tmLeft.value = new IndexNode(((ExpressionNode)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 72:  // closure ::= '{' cached_flagopt parameter_listopt '=>' expression '}'
				{ tmLeft.value = new ClosureNode(((Boolean)tmStack[tmHead - 4].value) != null, ((List<ParameterNode>)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 73:  // complex_data ::= '[' expression_listopt ']'
				{ tmLeft.value = new ListNode(((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 74:  // complex_data ::= '[' map_entries ']'
				{ tmLeft.value = new ConcreteMapNode(((Map<String,ExpressionNode>)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 75:  // complex_data ::= Lnew qualified_id '(' map_entriesopt ')'
				{ tmLeft.value = new CreateClassNode(((String)tmStack[tmHead - 3].value), ((Map<String,ExpressionNode>)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 76:  // map_entries ::= identifier map_separator conditional_expression
				{ tmLeft.value = new LinkedHashMap(); ((Map<String,ExpressionNode>)tmLeft.value).put(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 77:  // map_entries ::= map_entries ',' identifier map_separator conditional_expression
				{ ((Map<String,ExpressionNode>)tmStack[tmHead - 4].value).put(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 81:  // bcon ::= Ltrue
				{ tmLeft.value = Boolean.TRUE; }
				break;
			case 82:  // bcon ::= Lfalse
				{ tmLeft.value = Boolean.FALSE; }
				break;
			case 84:  // unary_expression ::= '!' unary_expression
				{ tmLeft.value = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 85:  // unary_expression ::= '-' unary_expression
				{ tmLeft.value = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 87:  // binary_op ::= binary_op '*' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 88:  // binary_op ::= binary_op '/' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 89:  // binary_op ::= binary_op '%' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 90:  // binary_op ::= binary_op '+' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 91:  // binary_op ::= binary_op '-' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 92:  // binary_op ::= binary_op '<' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 93:  // binary_op ::= binary_op '>' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 94:  // binary_op ::= binary_op '<=' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 95:  // binary_op ::= binary_op '>=' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 97:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				{ tmLeft.value = new InstanceOfNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 98:  // instanceof_expression ::= instanceof_expression Lis ccon
				{ tmLeft.value = new InstanceOfNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 100:  // equality_expression ::= equality_expression '==' instanceof_expression
				{ tmLeft.value = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 101:  // equality_expression ::= equality_expression '!=' instanceof_expression
				{ tmLeft.value = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 103:  // conditional_op ::= conditional_op '&&' conditional_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 104:  // conditional_op ::= conditional_op '||' conditional_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 106:  // conditional_expression ::= conditional_op '?' conditional_expression ':' conditional_expression
				{ tmLeft.value = new TriplexNode(((ExpressionNode)tmStack[tmHead - 4].value), ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 108:  // assignment_expression ::= identifier '=' conditional_expression
				{ tmLeft.value = new AssignNode(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 110:  // expression ::= expression ',' assignment_expression
				{ tmLeft.value = new CommaNode(((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 111:  // expression_list ::= conditional_expression
				{ tmLeft.value = new ArrayList(); ((ArrayList)tmLeft.value).add(((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 112:  // expression_list ::= expression_list ',' conditional_expression
				{ ((ArrayList)tmStack[tmHead - 2].value).add(((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 113:  // body ::= instructions
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
		return (List<IBundleEntity>) parse(lexer, 0, 266);
	}

	public TemplateNode parseBody(TemplatesLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1, 267);
	}
}
