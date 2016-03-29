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
	private static final int[] tmAction = TemplatesLexer.unpack_int(263,
		"\ufffd\uffff\uffff\uffff\5\0\ufff5\uffff\uffed\uffff\1\0\3\0\4\0\uffff\uffff\0\0" +
		"\31\0\30\0\26\0\27\0\uffff\uffff\uffe5\uffff\20\0\25\0\23\0\24\0\uffff\uffff\11\0" +
		"\uffff\uffff\164\0\uffff\uffff\2\0\uffff\uffff\7\0\uffff\uffff\uffd7\uffff\71\0\73" +
		"\0\uffff\uffff\uffff\uffff\121\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\75\0\uffff\uffff\120\0\74\0\uffff\uffff\uffa1\uffff\uffff\uffff\uffff" +
		"\uffff\uff99\uffff\uffff\uffff\161\0\uffff\uffff\uffff\uffff\uff7b\uffff\106\0\105" +
		"\0\72\0\125\0\uff47\uffff\uff19\uffff\ufefd\uffff\ufee3\uffff\152\0\154\0\ufecd\uffff" +
		"\37\0\17\0\uffff\uffff\41\0\ufec5\uffff\uffff\uffff\uffff\uffff\6\0\ufebb\uffff\uffff" +
		"\uffff\ufe9d\uffff\ufe91\uffff\ufe5b\uffff\uffff\uffff\ufe53\uffff\uffff\uffff\ufe4b" +
		"\uffff\uffff\uffff\uffff\uffff\ufe43\uffff\ufe3b\uffff\124\0\123\0\ufe35\uffff\uffff" +
		"\uffff\156\0\ufe03\uffff\uffff\uffff\uffff\uffff\22\0\21\0\32\0\57\0\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\66\0\45\0\50\0\ufdfb" +
		"\uffff\uffff\uffff\166\0\uffff\uffff\ufdf5\uffff\16\0\uffff\uffff\153\0\ufded\uffff" +
		"\172\0\ufdcf\uffff\uffff\uffff\176\0\35\0\uffff\uffff\uffff\uffff\ufdc7\uffff\ufdc1" +
		"\uffff\14\0\ufdbb\uffff\uffff\uffff\116\0\117\0\115\0\uffff\uffff\111\0\uffff\uffff" +
		"\uffff\uffff\110\0\70\0\uffff\uffff\ufdb3\uffff\uffff\uffff\uffff\uffff\ufd7d\uffff" +
		"\ufd4f\uffff\ufd21\uffff\ufcf3\uffff\ufcc5\uffff\ufc97\uffff\ufc69\uffff\ufc3b\uffff" +
		"\ufc0d\uffff\141\0\ufbdf\uffff\ufbc1\uffff\ufba5\uffff\ufb89\uffff\ufb73\uffff\uffff" +
		"\uffff\155\0\uffff\uffff\uffff\uffff\uffff\uffff\42\0\12\0\uffff\uffff\76\0\uffff" +
		"\uffff\uffff\uffff\174\0\34\0\40\0\uffff\uffff\ufb5d\uffff\uffff\uffff\ufb53\uffff" +
		"\uffff\uffff\202\0\uffff\uffff\uffff\uffff\uffff\uffff\113\0\uffff\uffff\157\0\104" +
		"\0\ufb4d\uffff\uffff\uffff\ufb2f\uffff\uffff\uffff\65\0\uffff\uffff\uffff\uffff\13" +
		"\0\uffff\uffff\44\0\ufb11\uffff\uffff\uffff\uffff\uffff\204\0\60\0\112\0\uffff\uffff" +
		"\uffff\uffff\53\0\15\0\uffff\uffff\uffff\uffff\ufb09\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\151\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff\ufadb\uffff\uffff" +
		"\uffff\uffff\uffff\55\0\54\0\52\0\107\0\114\0\uffff\uffff\100\0\ufad3\uffff\102\0" +
		"\uffff\uffff\uffff\uffff\47\0\10\0\uffff\uffff\uffff\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\uffff\uffff\ufab5\uffff\56\0\uffff\uffff\101\0\103\0\46\0\61\0\uffff" +
		"\uffff\51\0\uffff\uffff\uffff\uffff\ufffe\uffff\ufffe\uffff");

	private static final short[] tmLalr = TemplatesLexer.unpack_short(1360,
		"\1\uffff\5\uffff\0\163\uffff\ufffe\13\uffff\37\uffff\34\165\uffff\ufffe\1\uffff\5" +
		"\uffff\0\162\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\5\uffff\0\160\uffff\ufffe" +
		"\57\uffff\66\uffff\30\67\36\67\44\67\45\67\46\67\47\67\50\67\51\67\52\67\55\67\56" +
		"\67\60\67\61\67\62\67\63\67\64\67\65\67\67\67\70\67\72\67\73\67\74\67\75\67\77\67" +
		"\uffff\ufffe\13\uffff\7\165\71\165\uffff\ufffe\7\uffff\10\uffff\11\uffff\20\uffff" +
		"\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff" +
		"\56\201\uffff\ufffe\55\uffff\61\uffff\70\uffff\30\122\36\122\44\122\45\122\46\122" +
		"\47\122\50\122\51\122\52\122\56\122\60\122\62\122\63\122\64\122\65\122\67\122\72" +
		"\122\73\122\74\122\75\122\76\122\77\122\uffff\ufffe\46\uffff\47\uffff\50\uffff\51" +
		"\uffff\52\uffff\72\uffff\73\uffff\74\uffff\75\uffff\30\137\36\137\44\137\45\137\56" +
		"\137\60\137\62\137\63\137\64\137\65\137\67\137\76\137\77\137\uffff\ufffe\30\uffff" +
		"\36\142\44\142\45\142\56\142\60\142\62\142\63\142\64\142\65\142\67\142\76\142\77" +
		"\142\uffff\ufffe\65\uffff\67\uffff\36\145\44\145\45\145\56\145\60\145\62\145\63\145" +
		"\64\145\76\145\77\145\uffff\ufffe\63\uffff\64\uffff\77\uffff\36\150\44\150\45\150" +
		"\56\150\60\150\62\150\76\150\uffff\ufffe\62\uffff\44\33\45\33\uffff\ufffe\57\uffff" +
		"\61\uffff\44\167\45\167\uffff\ufffe\7\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33" +
		"\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60\201\uffff" +
		"\ufffe\57\uffff\61\uffff\21\173\44\173\45\173\uffff\ufffe\57\uffff\30\67\36\67\44" +
		"\67\45\67\46\67\47\67\50\67\51\67\52\67\55\67\56\67\60\67\61\67\62\67\63\67\64\67" +
		"\65\67\67\67\70\67\72\67\73\67\74\67\75\67\76\67\77\67\uffff\ufffe\62\uffff\44\177" +
		"\45\177\uffff\ufffe\62\uffff\44\63\45\63\uffff\ufffe\62\uffff\44\62\45\62\uffff\ufffe" +
		"\62\uffff\44\36\45\36\uffff\ufffe\7\uffff\71\171\uffff\ufffe\57\uffff\66\uffff\71" +
		"\uffff\76\uffff\30\67\46\67\47\67\50\67\51\67\52\67\55\67\56\67\61\67\62\67\63\67" +
		"\64\67\65\67\67\67\70\67\72\67\73\67\74\67\75\67\77\67\uffff\ufffe\62\uffff\56\200" +
		"\60\200\uffff\ufffe\7\uffff\60\171\uffff\ufffe\57\uffff\61\uffff\66\167\uffff\ufffe" +
		"\7\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff" +
		"\47\uffff\53\uffff\55\uffff\57\uffff\60\201\uffff\ufffe\21\uffff\44\175\45\175\uffff" +
		"\ufffe\7\uffff\60\207\uffff\ufffe\1\uffff\5\203\uffff\ufffe\62\uffff\60\170\71\170" +
		"\uffff\ufffe\57\uffff\30\77\36\77\44\77\45\77\46\77\47\77\50\77\51\77\52\77\55\77" +
		"\56\77\60\77\61\77\62\77\63\77\64\77\65\77\67\77\70\77\72\77\73\77\74\77\75\77\76" +
		"\77\77\77\uffff\ufffe\46\131\47\131\50\uffff\51\uffff\52\uffff\72\131\73\131\74\131" +
		"\75\131\30\131\36\131\44\131\45\131\56\131\60\131\62\131\63\131\64\131\65\131\67" +
		"\131\76\131\77\131\uffff\ufffe\46\132\47\132\50\uffff\51\uffff\52\uffff\72\132\73" +
		"\132\74\132\75\132\30\132\36\132\44\132\45\132\56\132\60\132\62\132\63\132\64\132" +
		"\65\132\67\132\76\132\77\132\uffff\ufffe\46\126\47\126\50\126\51\126\52\126\72\126" +
		"\73\126\74\126\75\126\30\126\36\126\44\126\45\126\56\126\60\126\62\126\63\126\64" +
		"\126\65\126\67\126\76\126\77\126\uffff\ufffe\46\127\47\127\50\127\51\127\52\127\72" +
		"\127\73\127\74\127\75\127\30\127\36\127\44\127\45\127\56\127\60\127\62\127\63\127" +
		"\64\127\65\127\67\127\76\127\77\127\uffff\ufffe\46\130\47\130\50\130\51\130\52\130" +
		"\72\130\73\130\74\130\75\130\30\130\36\130\44\130\45\130\56\130\60\130\62\130\63" +
		"\130\64\130\65\130\67\130\76\130\77\130\uffff\ufffe\46\uffff\47\uffff\50\uffff\51" +
		"\uffff\52\uffff\72\135\73\135\74\135\75\135\30\135\36\135\44\135\45\135\56\135\60" +
		"\135\62\135\63\135\64\135\65\135\67\135\76\135\77\135\uffff\ufffe\46\uffff\47\uffff" +
		"\50\uffff\51\uffff\52\uffff\72\136\73\136\74\136\75\136\30\136\36\136\44\136\45\136" +
		"\56\136\60\136\62\136\63\136\64\136\65\136\67\136\76\136\77\136\uffff\ufffe\46\uffff" +
		"\47\uffff\50\uffff\51\uffff\52\uffff\72\133\73\133\74\133\75\133\30\133\36\133\44" +
		"\133\45\133\56\133\60\133\62\133\63\133\64\133\65\133\67\133\76\133\77\133\uffff" +
		"\ufffe\46\uffff\47\uffff\50\uffff\51\uffff\52\uffff\72\134\73\134\74\134\75\134\30" +
		"\134\36\134\44\134\45\134\56\134\60\134\62\134\63\134\64\134\65\134\67\134\76\134" +
		"\77\134\uffff\ufffe\61\uffff\30\140\36\140\44\140\45\140\56\140\60\140\62\140\63" +
		"\140\64\140\65\140\67\140\76\140\77\140\uffff\ufffe\30\uffff\36\143\44\143\45\143" +
		"\56\143\60\143\62\143\63\143\64\143\65\143\67\143\76\143\77\143\uffff\ufffe\30\uffff" +
		"\36\144\44\144\45\144\56\144\60\144\62\144\63\144\64\144\65\144\67\144\76\144\77" +
		"\144\uffff\ufffe\63\146\64\146\36\146\44\146\45\146\56\146\60\146\62\146\76\146\77" +
		"\146\uffff\ufffe\63\uffff\64\147\36\147\44\147\45\147\56\147\60\147\62\147\76\147" +
		"\77\147\uffff\ufffe\36\uffff\62\uffff\44\205\45\205\uffff\ufffe\62\uffff\60\206\uffff" +
		"\ufffe\7\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43" +
		"\uffff\47\uffff\53\uffff\55\uffff\57\uffff\60\201\uffff\ufffe\7\uffff\10\uffff\11" +
		"\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff\47\uffff\53\uffff\55" +
		"\uffff\57\uffff\60\201\uffff\ufffe\62\uffff\44\43\45\43\uffff\ufffe\54\uffff\57\uffff" +
		"\30\67\46\67\47\67\50\67\51\67\52\67\55\67\60\67\61\67\62\67\63\67\64\67\65\67\67" +
		"\67\70\67\72\67\73\67\74\67\75\67\77\67\uffff\ufffe\62\uffff\44\64\45\64\uffff\ufffe" +
		"\7\uffff\10\uffff\11\uffff\20\uffff\32\uffff\33\uffff\40\uffff\41\uffff\43\uffff" +
		"\47\uffff\53\uffff\55\uffff\57\uffff\60\201\uffff\ufffe\36\uffff\44\205\45\205\uffff" +
		"\ufffe");

	private static final short[] lapg_sym_goto = TemplatesLexer.unpack_short(123,
		"\0\2\22\37\54\71\111\116\221\305\372\377\u0101\u0103\u0107\u0109\u010e\u0142\u0148" +
		"\u014d\u0152\u0152\u0158\u015a\u015a\u015d\u015d\u0191\u01c5\u01c6\u01cb\u01cd\u01ce" +
		"\u0202\u0236\u023b\u026f\u027b\u0284\u028e\u02cc\u02d6\u02e0\u02ea\u031e\u031f\u0355" +
		"\u0359\u0399\u03a3\u03aa\u03c0\u03c3\u03c6\u03c7\u03cc\u03cd\u03ce\u03d2\u03dc\u03e6" +
		"\u03f0\u03fa\u03fe\u03ff\u03ff\u0404\u0405\u0406\u0408\u040a\u040c\u040e\u0410\u0412" +
		"\u0414\u0416\u041c\u0425\u0432\u043f\u0444\u0445\u044b\u044c\u044d\u045a\u045c\u0469" +
		"\u046a\u046c\u0479\u047e\u0480\u0485\u04b9\u04ed\u0521\u0523\u0526\u055a\u058e\u05c0" +
		"\u05e9\u0610\u0637\u065c\u0671\u0685\u068b\u068c\u0691\u0692\u0694\u0696\u0698\u0699" +
		"\u069a\u069b\u06a1\u06a2\u06a4\u06a5");

	private static final short[] lapg_sym_from = TemplatesLexer.unpack_short(1701,
		"\u0103\u0104\0\1\4\10\17\24\34\103\212\313\326\341\360\371\374\u0101\1\10\17\24\34" +
		"\103\313\326\341\360\371\374\u0101\1\10\17\24\34\103\313\326\341\360\371\374\u0101" +
		"\1\10\17\24\34\103\313\326\341\360\371\374\u0101\0\1\4\10\17\24\34\103\276\313\326" +
		"\341\360\371\374\u0101\103\326\341\371\u0101\16\26\32\40\41\43\44\45\46\47\51\54" +
		"\56\57\60\61\106\111\112\125\142\143\144\145\146\147\150\151\152\153\154\155\156" +
		"\157\160\161\162\163\164\165\171\172\201\204\210\211\221\223\224\231\264\270\277" +
		"\300\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32\41\44\46\51\54\56" +
		"\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164" +
		"\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321\332\343\345\346" +
		"\354\356\361\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152" +
		"\153\154\155\156\157\160\161\162\163\164\165\201\204\210\221\224\231\264\270\300" +
		"\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32\165\346\361\3\55\325\346" +
		"\32\165\346\361\165\346\16\32\165\346\361\16\32\41\44\46\51\54\56\57\60\61\111\112" +
		"\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210" +
		"\221\224\231\264\270\300\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32" +
		"\165\203\346\361\16\32\165\346\361\16\32\165\346\361\16\32\165\255\346\361\116\120" +
		"\73\246\247\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152" +
		"\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224\231\264\270\300\305" +
		"\307\310\312\315\321\332\343\345\346\354\356\361\16\32\41\44\46\51\54\56\57\60\61" +
		"\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201" +
		"\204\210\221\224\231\264\270\300\305\307\310\312\315\321\332\343\345\346\354\356" +
		"\361\30\16\32\165\346\361\271\372\3\16\32\41\44\46\51\54\56\57\60\61\111\112\142" +
		"\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221" +
		"\224\231\264\270\300\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32\41" +
		"\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160" +
		"\161\162\163\164\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321" +
		"\332\343\345\346\354\356\361\16\32\165\346\361\16\32\41\44\46\51\54\56\57\60\61\111" +
		"\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204" +
		"\210\221\224\231\264\270\300\305\307\310\312\315\321\332\343\345\346\354\356\361" +
		"\63\64\107\123\174\254\255\331\340\342\365\366\63\64\123\174\254\255\340\365\366" +
		"\72\233\234\235\236\237\240\241\242\243\16\32\41\44\46\51\54\56\57\60\61\72\111\112" +
		"\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210" +
		"\221\224\231\233\234\235\236\237\240\241\242\243\264\270\300\305\307\310\312\315" +
		"\321\332\343\345\346\354\356\361\72\233\234\235\236\237\240\241\242\243\72\233\234" +
		"\235\236\237\240\241\242\243\72\233\234\235\236\237\240\241\242\243\16\32\41\44\46" +
		"\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162" +
		"\163\164\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321\332\343" +
		"\345\346\354\356\361\333\16\32\41\44\46\51\54\56\57\60\61\65\111\112\142\145\146" +
		"\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\207\210\221\224" +
		"\231\264\270\300\305\307\310\312\315\321\332\343\345\346\354\356\361\131\134\227" +
		"\364\16\32\35\41\44\46\51\54\56\57\60\61\105\111\112\113\114\122\130\142\144\145" +
		"\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\175\201\204\210\221" +
		"\224\230\231\232\264\270\300\305\307\310\312\315\321\332\333\335\343\345\346\354" +
		"\356\361\135\177\256\263\274\306\334\336\367\370\65\105\113\122\175\232\245\100\115" +
		"\117\121\123\124\131\133\135\214\227\271\273\306\317\320\331\340\342\344\365\367" +
		"\75\250\251\75\250\251\74\35\130\261\272\302\74\65\130\215\272\302\72\233\234\235" +
		"\236\237\240\241\242\243\72\233\234\235\236\237\240\241\242\243\72\233\234\235\236" +
		"\237\240\241\242\243\72\233\234\235\236\237\240\241\242\243\130\252\272\302\75\16" +
		"\32\165\346\361\0\0\0\4\0\4\0\4\3\55\0\4\105\175\125\171\10\34\1\10\24\313\360\374" +
		"\63\64\123\174\254\255\340\365\366\1\10\17\24\34\103\313\326\341\360\371\374\u0101" +
		"\1\10\17\24\34\103\313\326\341\360\371\374\u0101\16\32\165\346\361\115\26\40\47\106" +
		"\144\156\203\113\1\10\17\24\34\103\313\326\341\360\371\374\u0101\103\371\1\10\17" +
		"\24\34\103\313\326\341\360\371\374\u0101\276\276\326\1\10\17\24\34\103\313\326\341" +
		"\360\371\374\u0101\16\32\165\346\361\271\372\103\326\341\371\u0101\16\32\41\44\46" +
		"\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162" +
		"\163\164\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321\332\343" +
		"\345\346\354\356\361\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145\146\147\150" +
		"\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224\231\264\270" +
		"\300\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32\41\44\46\51\54\56" +
		"\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161\162\163\164" +
		"\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321\332\343\345\346" +
		"\354\356\361\60\211\130\272\302\16\32\41\44\46\51\54\56\57\60\61\111\112\142\145" +
		"\146\147\150\151\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224" +
		"\231\264\270\300\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32\41\44" +
		"\46\51\54\56\57\60\61\111\112\142\145\146\147\150\151\152\153\154\155\157\160\161" +
		"\162\163\164\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321\332" +
		"\343\345\346\354\356\361\16\32\41\44\46\51\54\60\61\111\112\142\145\146\147\150\151" +
		"\152\153\154\155\157\160\161\162\163\164\165\201\204\210\221\224\231\264\270\300" +
		"\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32\41\44\46\51\54\60\61\111" +
		"\112\142\157\160\161\162\163\164\165\201\204\210\221\224\231\264\270\300\305\307" +
		"\310\312\315\321\332\343\345\346\354\356\361\16\32\41\44\46\51\54\60\61\111\112\142" +
		"\161\162\163\164\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321" +
		"\332\343\345\346\354\356\361\16\32\41\44\46\51\54\60\61\111\112\142\161\162\163\164" +
		"\165\201\204\210\221\224\231\264\270\300\305\307\310\312\315\321\332\343\345\346" +
		"\354\356\361\16\32\41\44\46\51\54\60\61\111\112\142\163\164\165\201\204\210\221\224" +
		"\231\264\270\300\305\307\310\312\315\321\332\343\345\346\354\356\361\16\32\44\46" +
		"\51\54\61\142\164\165\210\231\264\300\312\315\321\345\346\354\361\16\32\44\46\51" +
		"\54\61\142\165\210\231\264\300\312\315\321\345\346\354\361\60\111\201\305\307\356" +
		"\1\16\32\165\346\361\0\3\55\105\175\125\171\113\203\115\60\111\201\305\307\356\212" +
		"\271\372\211");

	private static final short[] lapg_sym_to = TemplatesLexer.unpack_short(1701,
		"\u0105\u0106\2\12\2\12\12\12\12\12\275\12\12\12\12\12\12\12\13\13\13\13\13\13\13" +
		"\13\13\13\13\13\13\14\14\14\14\14\14\14\14\14\14\14\14\14\15\15\15\15\15\15\15\15" +
		"\15\15\15\15\15\3\16\3\32\16\16\32\165\325\16\346\361\16\165\16\361\166\166\166\166" +
		"\166\35\104\35\104\114\116\35\120\35\104\35\35\114\114\130\35\104\114\114\213\35" +
		"\230\104\114\114\114\114\114\114\114\114\114\104\114\114\114\114\114\35\35\213\257" +
		"\114\114\35\272\114\302\114\35\35\114\330\35\333\114\114\35\35\35\114\114\35\35\35" +
		"\114\35\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36" +
		"\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36\36" +
		"\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\244\37\37\37" +
		"\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\37\40" +
		"\40\40\40\40\25\25\345\345\107\254\254\254\255\366\41\41\41\41\41\42\42\42\42\42" +
		"\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42" +
		"\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\42\43\43\43\264\43\43\44" +
		"\44\44\44\44\45\45\45\45\45\46\46\46\312\46\46\207\210\156\156\156\47\47\47\47\47" +
		"\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47" +
		"\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\47\50\50\50\50\50\50\50" +
		"\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50" +
		"\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\50\106\51\51\51\51\51\321\321" +
		"\26\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52" +
		"\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\52\53" +
		"\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53" +
		"\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\53\54\54\54" +
		"\54\54\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55" +
		"\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55\55" +
		"\136\136\176\136\136\136\136\352\136\363\136\136\137\137\137\137\137\137\137\137" +
		"\137\145\145\145\145\145\145\145\145\145\145\56\56\56\56\56\56\56\56\56\56\56\146" +
		"\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\146\146" +
		"\146\146\146\146\146\146\146\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\56\147" +
		"\147\147\147\147\147\147\147\147\147\150\150\150\150\150\150\150\150\150\150\151" +
		"\151\151\151\151\151\151\151\151\151\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57" +
		"\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57\57" +
		"\57\57\57\57\57\57\57\57\57\57\354\60\60\60\60\60\60\60\60\60\60\60\142\60\60\60" +
		"\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\60\270\60\60\60\60\60\60\60\60" +
		"\60\60\60\60\60\60\60\60\60\60\60\60\222\225\304\372\61\61\111\61\61\61\61\61\61" +
		"\61\61\61\171\61\61\201\111\211\111\61\231\61\61\61\61\61\61\61\61\61\61\61\61\61" +
		"\61\61\61\171\61\61\61\61\61\305\61\307\61\61\61\61\61\61\61\61\61\61\111\356\61" +
		"\61\61\61\61\61\226\262\314\316\324\335\355\357\375\376\143\172\172\172\172\172\172" +
		"\164\204\164\164\164\164\223\224\164\277\164\164\223\164\164\343\164\164\164\164" +
		"\164\164\161\161\161\162\162\162\157\112\216\315\216\216\160\144\217\300\217\217" +
		"\152\152\152\152\152\152\152\152\152\152\153\153\153\153\153\153\153\153\153\153" +
		"\154\154\154\154\154\154\154\154\154\154\155\155\155\155\155\155\155\155\155\155" +
		"\220\310\220\220\163\62\62\62\62\62\u0103\4\5\31\6\6\7\7\27\27\10\10\173\173\214" +
		"\214\33\110\17\34\103\341\371\u0101\140\141\212\260\311\313\360\373\374\20\20\102" +
		"\20\102\102\20\347\102\20\102\20\102\21\21\21\21\21\21\21\21\21\21\21\21\21\63\63" +
		"\63\63\63\205\105\113\122\175\232\245\265\202\22\22\22\22\22\22\22\22\22\22\22\22" +
		"\22\167\377\23\23\23\23\23\23\23\23\23\23\23\23\23\326\327\350\24\24\24\24\24\24" +
		"\24\24\24\24\24\24\24\64\64\64\64\64\322\322\170\351\362\170\u0102\65\65\65\65\65" +
		"\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65" +
		"\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\65\66\66\66\66\66\66\66" +
		"\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66" +
		"\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\66\67\67\67\67\67\67\67\67\67" +
		"\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67" +
		"\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\67\131\273\221\221\332\70\70\70\70" +
		"\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70" +
		"\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\70\71\71\71\71\71\71" +
		"\71\126\127\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71" +
		"\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\71\72\72\72\72\72\72\72" +
		"\72\72\72\72\72\233\234\235\236\237\240\241\242\243\72\72\72\72\72\72\72\72\72\72" +
		"\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\72\73\73\73\73\73\73\73\73" +
		"\73\73\73\73\246\247\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73\73" +
		"\73\73\73\73\73\73\73\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74" +
		"\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\74\75\75\75\75\75\75\75\75" +
		"\75\75\75\75\250\251\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75\75" +
		"\75\75\75\75\75\76\76\115\76\76\76\76\132\76\132\200\76\252\76\76\132\267\76\301" +
		"\303\76\76\320\76\132\132\337\76\76\76\353\364\76\76\76\132\76\77\77\77\77\77\77" +
		"\77\77\253\77\77\77\77\77\77\77\77\77\77\77\77\100\100\117\121\123\124\135\227\100" +
		"\271\306\317\331\340\342\344\365\100\367\100\133\133\133\133\133\133\u0104\101\101" +
		"\101\101\101\11\30\125\174\261\215\256\203\266\206\134\177\263\334\336\370\276\323" +
		"\u0100\274");

	private static final short[] tmRuleLen = TemplatesLexer.unpack_short(136,
		"\1\1\2\1\1\1\3\2\10\1\5\3\1\3\3\2\1\1\1\1\1\1\1\1\1\1\3\1\4\3\2\1\2\1\3\2\3\3\7\5" +
		"\1\13\7\1\2\2\4\3\5\11\2\2\2\3\1\1\3\1\1\1\1\1\4\3\6\10\6\10\4\1\1\6\3\3\5\3\5\1" +
		"\1\1\1\1\1\2\2\1\3\3\3\3\3\3\3\3\3\1\3\3\1\3\3\1\3\3\1\5\1\3\1\3\1\3\1\1\1\0\1\0" +
		"\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0");

	private static final short[] tmRuleSymbol = TemplatesLexer.unpack_short(136,
		"\102\103\103\104\104\104\105\105\106\107\110\111\112\112\113\114\114\115\115\116" +
		"\116\116\116\116\116\116\117\120\120\120\120\120\121\122\122\123\124\125\126\126" +
		"\126\127\127\130\130\130\131\132\133\133\133\133\134\135\135\136\136\136\136\136" +
		"\136\136\136\136\136\136\136\136\136\136\136\137\140\140\140\141\141\142\142\142" +
		"\143\143\144\144\144\145\145\145\145\145\145\145\145\145\145\146\146\146\147\147" +
		"\147\150\150\150\151\151\152\152\153\153\154\154\155\156\157\157\160\160\161\161" +
		"\162\162\163\163\164\164\165\165\166\166\167\167\170\170\171\171");

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
		int template_end = 75;
		int instructions = 76;
		int LsquareMinusRsquareRcurly = 77;
		int instruction = 78;
		int simple_instruction = 79;
		int sentence = 80;
		int comma_expr = 81;
		int qualified_id = 82;
		int template_for_expr = 83;
		int template_arguments = 84;
		int control_instruction = 85;
		int else_clause = 86;
		int switch_instruction = 87;
		int case_list = 88;
		int one_case = 89;
		int control_start = 90;
		int control_sentence = 91;
		int separator_expr = 92;
		int control_end = 93;
		int primary_expression = 94;
		int closure = 95;
		int complex_data = 96;
		int map_entries = 97;
		int map_separator = 98;
		int bcon = 99;
		int unary_expression = 100;
		int binary_op = 101;
		int instanceof_expression = 102;
		int equality_expression = 103;
		int conditional_op = 104;
		int conditional_expression = 105;
		int assignment_expression = 106;
		int expression = 107;
		int expression_list = 108;
		int body = 109;
		int syntax_problem = 110;
		int definitionsopt = 111;
		int cached_flagopt = 112;
		int parametersopt = 113;
		int parameter_listopt = 114;
		int template_argumentsopt = 115;
		int template_for_expropt = 116;
		int comma_expropt = 117;
		int expression_listopt = 118;
		int anyopt = 119;
		int separator_expropt = 120;
		int map_entriesopt = 121;
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
			case 8:  // query_def ::= '${' cached_flagopt Lquery qualified_id parametersopt '=' expression '}'
				{ tmLeft.value = new QueryNode(((String)tmStack[tmHead - 4].value), ((List<ParameterNode>)tmStack[tmHead - 3].value), templatePackage, ((ExpressionNode)tmStack[tmHead - 1].value), ((Boolean)tmStack[tmHead - 6].value) != null, source, tmLeft.offset, tmLeft.endoffset); checkFqn(((String)tmStack[tmHead - 4].value), tmLeft.offset, tmLeft.endoffset, tmStack[tmHead - 7].line); }
				break;
			case 9:  // cached_flag ::= Lcached
				{ tmLeft.value = Boolean.TRUE; }
				break;
			case 10:  // template_start ::= '${' Ltemplate qualified_id parametersopt '[-]}'
				{ tmLeft.value = new TemplateNode(((String)tmStack[tmHead - 2].value), ((List<ParameterNode>)tmStack[tmHead - 1].value), templatePackage, source, tmLeft.offset, tmLeft.endoffset); checkFqn(((String)tmStack[tmHead - 2].value), tmLeft.offset, tmLeft.endoffset, tmStack[tmHead - 4].line); }
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
			case 15:  // instructions ::= instructions instruction
				{ if (((Node)tmStack[tmHead].value) != null) ((ArrayList<Node>)tmStack[tmHead - 1].value).add(((Node)tmStack[tmHead].value)); }
				break;
			case 16:  // instructions ::= instruction
				{ tmLeft.value = new ArrayList<Node>(); if (((Node)tmStack[tmHead].value)!=null) ((ArrayList<Node>)tmLeft.value).add(((Node)tmStack[tmHead].value)); }
				break;
			case 17:  // '[-]}' ::= '-}'
				{ skipSpaces(tmStack[tmHead].offset+1); }
				break;
			case 22:  // instruction ::= escid
				{ tmLeft.value = createEscapedId(((String)tmStack[tmHead].value), tmLeft.offset, tmLeft.endoffset); }
				break;
			case 23:  // instruction ::= escint
				{ tmLeft.value = new IndexNode(null, new LiteralNode(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 24:  // instruction ::= escdollar
				{ tmLeft.value = new DollarNode(source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 25:  // instruction ::= any
				{ tmLeft.value = new TextNode(source, rawText(tmLeft.offset, tmLeft.endoffset), tmLeft.endoffset); }
				break;
			case 26:  // simple_instruction ::= '${' sentence '[-]}'
				{ tmLeft.value = ((Node)tmStack[tmHead - 1].value); }
				break;
			case 28:  // sentence ::= Lcall qualified_id template_argumentsopt template_for_expropt
				{ tmLeft.value = new CallTemplateNode(((String)tmStack[tmHead - 2].value), ((ArrayList)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead].value), templatePackage, true, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 29:  // sentence ::= Leval conditional_expression comma_expropt
				{ tmLeft.value = new EvalNode(((ExpressionNode)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 30:  // sentence ::= Lassert expression
				{ tmLeft.value = new AssertNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 31:  // sentence ::= syntax_problem
				{ tmLeft.value = null; }
				break;
			case 32:  // comma_expr ::= ',' conditional_expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 34:  // qualified_id ::= qualified_id '.' identifier
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + "." + ((String)tmStack[tmHead].value); }
				break;
			case 35:  // template_for_expr ::= Lfor expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 36:  // template_arguments ::= '(' expression_listopt ')'
				{ tmLeft.value = ((ArrayList)tmStack[tmHead - 1].value); }
				break;
			case 37:  // control_instruction ::= control_start instructions else_clause
				{ ((CompoundNode)tmStack[tmHead - 2].value).setInstructions(((ArrayList<Node>)tmStack[tmHead - 1].value)); applyElse(((CompoundNode)tmStack[tmHead - 2].value),((ElseIfNode)tmStack[tmHead].value), tmLeft.offset, tmLeft.endoffset, tmLeft.line); }
				break;
			case 38:  // else_clause ::= '${' Lelse Lif expression '[-]}' instructions else_clause
				{ tmLeft.value = new ElseIfNode(((ExpressionNode)tmStack[tmHead - 3].value), ((ArrayList<Node>)tmStack[tmHead - 1].value), ((ElseIfNode)tmStack[tmHead].value), source, tmStack[tmHead - 6].offset, tmStack[tmHead - 1].endoffset); }
				break;
			case 39:  // else_clause ::= '${' Lelse '[-]}' instructions control_end
				{ tmLeft.value = new ElseIfNode(null, ((ArrayList<Node>)tmStack[tmHead - 1].value), null, source, tmStack[tmHead - 4].offset, tmStack[tmHead - 1].endoffset); }
				break;
			case 40:  // else_clause ::= control_end
				{ tmLeft.value = null; }
				break;
			case 41:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list '${' Lelse '[-]}' instructions control_end
				{ tmLeft.value = new SwitchNode(((ExpressionNode)tmStack[tmHead - 8].value), ((ArrayList)tmStack[tmHead - 5].value), ((ArrayList<Node>)tmStack[tmHead - 1].value), source, tmLeft.offset,tmLeft.endoffset); checkIsSpace(tmStack[tmHead - 6].offset,tmStack[tmHead - 6].endoffset, tmStack[tmHead - 6].line); }
				break;
			case 42:  // switch_instruction ::= '${' Lswitch expression '[-]}' anyopt case_list control_end
				{ tmLeft.value = new SwitchNode(((ExpressionNode)tmStack[tmHead - 4].value), ((ArrayList)tmStack[tmHead - 1].value), ((ArrayList<Node>)null), source, tmLeft.offset,tmLeft.endoffset); checkIsSpace(tmStack[tmHead - 2].offset,tmStack[tmHead - 2].endoffset, tmStack[tmHead - 2].line); }
				break;
			case 43:  // case_list ::= one_case
				{ tmLeft.value = new ArrayList(); ((ArrayList)tmLeft.value).add(((CaseNode)tmStack[tmHead].value)); }
				break;
			case 44:  // case_list ::= case_list one_case
				{ ((ArrayList)tmStack[tmHead - 1].value).add(((CaseNode)tmStack[tmHead].value)); }
				break;
			case 45:  // case_list ::= case_list instruction
				{ CaseNode.add(((ArrayList)tmStack[tmHead - 1].value), ((Node)tmStack[tmHead].value)); }
				break;
			case 46:  // one_case ::= '${' Lcase expression '[-]}'
				{ tmLeft.value = new CaseNode(((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 47:  // control_start ::= '${' control_sentence '[-]}'
				{ tmLeft.value = ((CompoundNode)tmStack[tmHead - 1].value); }
				break;
			case 48:  // control_sentence ::= Lforeach identifier Lin expression separator_expropt
				{ tmLeft.value = new ForeachNode(((String)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), null, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 49:  // control_sentence ::= Lfor identifier Lin '[' conditional_expression ',' conditional_expression ']' separator_expropt
				{ tmLeft.value = new ForeachNode(((String)tmStack[tmHead - 7].value), ((ExpressionNode)tmStack[tmHead - 4].value), ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 50:  // control_sentence ::= Lif expression
				{ tmLeft.value = new IfNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 51:  // control_sentence ::= Lfile expression
				{ tmLeft.value = new FileNode(((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 52:  // separator_expr ::= Lseparator expression
				{ tmLeft.value = ((ExpressionNode)tmStack[tmHead].value); }
				break;
			case 55:  // primary_expression ::= identifier
				{ tmLeft.value = new SelectNode(null, ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 56:  // primary_expression ::= '(' expression ')'
				{ tmLeft.value = new ParenthesesNode(((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 57:  // primary_expression ::= icon
				{ tmLeft.value = new LiteralNode(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 58:  // primary_expression ::= bcon
				{ tmLeft.value = new LiteralNode(((Boolean)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 59:  // primary_expression ::= ccon
				{ tmLeft.value = new LiteralNode(((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 60:  // primary_expression ::= Lself
				{ tmLeft.value = new ThisNode(source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 61:  // primary_expression ::= Lnull
				{ tmLeft.value = new LiteralNode(null, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 62:  // primary_expression ::= identifier '(' expression_listopt ')'
				{ tmLeft.value = new MethodCallNode(null, ((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 63:  // primary_expression ::= primary_expression '.' identifier
				{ tmLeft.value = new SelectNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 64:  // primary_expression ::= primary_expression '.' identifier '(' expression_listopt ')'
				{ tmLeft.value = new MethodCallNode(((ExpressionNode)tmStack[tmHead - 5].value), ((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 65:  // primary_expression ::= primary_expression '.' identifier '(' identifier '|' expression ')'
				{ tmLeft.value = createCollectionProcessor(((ExpressionNode)tmStack[tmHead - 7].value), ((String)tmStack[tmHead - 5].value), ((String)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset, tmLeft.line); }
				break;
			case 66:  // primary_expression ::= primary_expression '->' qualified_id '(' expression_listopt ')'
				{ tmLeft.value = new CallTemplateNode(((String)tmStack[tmHead - 3].value), ((ArrayList)tmStack[tmHead - 1].value), ((ExpressionNode)tmStack[tmHead - 5].value), templatePackage, false, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 67:  // primary_expression ::= primary_expression '->' '(' expression ')' '(' expression_listopt ')'
				{ tmLeft.value = new CallTemplateNode(((ExpressionNode)tmStack[tmHead - 4].value),((ArrayList)tmStack[tmHead - 1].value),((ExpressionNode)tmStack[tmHead - 7].value),templatePackage, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 68:  // primary_expression ::= primary_expression '[' expression ']'
				{ tmLeft.value = new IndexNode(((ExpressionNode)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 71:  // closure ::= '{' cached_flagopt parameter_listopt '=>' expression '}'
				{ tmLeft.value = new ClosureNode(((Boolean)tmStack[tmHead - 4].value) != null, ((List<ParameterNode>)tmStack[tmHead - 3].value), ((ExpressionNode)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 72:  // complex_data ::= '[' expression_listopt ']'
				{ tmLeft.value = new ListNode(((ArrayList)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 73:  // complex_data ::= '[' map_entries ']'
				{ tmLeft.value = new ConcreteMapNode(((Map<String,ExpressionNode>)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 74:  // complex_data ::= Lnew qualified_id '(' map_entriesopt ')'
				{ tmLeft.value = new CreateClassNode(((String)tmStack[tmHead - 3].value), ((Map<String,ExpressionNode>)tmStack[tmHead - 1].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 75:  // map_entries ::= identifier map_separator conditional_expression
				{ tmLeft.value = new LinkedHashMap(); ((Map<String,ExpressionNode>)tmLeft.value).put(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 76:  // map_entries ::= map_entries ',' identifier map_separator conditional_expression
				{ ((Map<String,ExpressionNode>)tmStack[tmHead - 4].value).put(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 80:  // bcon ::= Ltrue
				{ tmLeft.value = Boolean.TRUE; }
				break;
			case 81:  // bcon ::= Lfalse
				{ tmLeft.value = Boolean.FALSE; }
				break;
			case 83:  // unary_expression ::= '!' unary_expression
				{ tmLeft.value = new UnaryExpression(UnaryExpression.NOT, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 84:  // unary_expression ::= '-' unary_expression
				{ tmLeft.value = new UnaryExpression(UnaryExpression.MINUS, ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 86:  // binary_op ::= binary_op '*' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.MULT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 87:  // binary_op ::= binary_op '/' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.DIV, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 88:  // binary_op ::= binary_op '%' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.REM, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 89:  // binary_op ::= binary_op '+' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.PLUS, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 90:  // binary_op ::= binary_op '-' binary_op
				{ tmLeft.value = new ArithmeticNode(ArithmeticNode.MINUS, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 91:  // binary_op ::= binary_op '<' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.LT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 92:  // binary_op ::= binary_op '>' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.GT, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 93:  // binary_op ::= binary_op '<=' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.LE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 94:  // binary_op ::= binary_op '>=' binary_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.GE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 96:  // instanceof_expression ::= instanceof_expression Lis qualified_id
				{ tmLeft.value = new InstanceOfNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 97:  // instanceof_expression ::= instanceof_expression Lis ccon
				{ tmLeft.value = new InstanceOfNode(((ExpressionNode)tmStack[tmHead - 2].value), ((String)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 99:  // equality_expression ::= equality_expression '==' instanceof_expression
				{ tmLeft.value = new ConditionalNode(ConditionalNode.EQ, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 100:  // equality_expression ::= equality_expression '!=' instanceof_expression
				{ tmLeft.value = new ConditionalNode(ConditionalNode.NE, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 102:  // conditional_op ::= conditional_op '&&' conditional_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.AND, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 103:  // conditional_op ::= conditional_op '||' conditional_op
				{ tmLeft.value = new ConditionalNode(ConditionalNode.OR, ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 105:  // conditional_expression ::= conditional_op '?' conditional_expression ':' conditional_expression
				{ tmLeft.value = new TriplexNode(((ExpressionNode)tmStack[tmHead - 4].value), ((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 107:  // assignment_expression ::= identifier '=' conditional_expression
				{ tmLeft.value = new AssignNode(((String)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 109:  // expression ::= expression ',' assignment_expression
				{ tmLeft.value = new CommaNode(((ExpressionNode)tmStack[tmHead - 2].value), ((ExpressionNode)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 110:  // expression_list ::= conditional_expression
				{ tmLeft.value = new ArrayList(); ((ArrayList)tmLeft.value).add(((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 111:  // expression_list ::= expression_list ',' conditional_expression
				{ ((ArrayList)tmStack[tmHead - 2].value).add(((ExpressionNode)tmStack[tmHead].value)); }
				break;
			case 112:  // body ::= instructions
				{
							tmLeft.value = new TemplateNode("inline", null, templatePackage, source, tmLeft.offset, tmLeft.endoffset);
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
		return (List<IBundleEntity>) parse(lexer, 0, 261);
	}

	public TemplateNode parseBody(TemplatesLexer lexer) throws IOException, ParseException {
		return (TemplateNode) parse(lexer, 1, 262);
	}
}
