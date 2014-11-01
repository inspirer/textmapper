package org.textmapper.tool.test.bootstrap.eoi;

import java.io.IOException;
import java.text.MessageFormat;
import org.textmapper.tool.test.bootstrap.eoi.EoiLexer.ErrorReporter;
import org.textmapper.tool.test.bootstrap.eoi.EoiLexer.LapgSymbol;
import org.textmapper.tool.test.bootstrap.eoi.EoiLexer.Tokens;

public class EoiParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public EoiParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}

	private static final boolean DEBUG_SYNTAX = false;
	private static final int[] tmAction = EoiLexer.unpack_int(17,
		"\uffff\uffff\1\0\ufffd\uffff\0\0\uffff\uffff\ufff7\uffff\uffff\uffff\uffff\uffff" +
		"\uffff\uffff\ufff1\uffff\5\0\uffff\uffff\2\0\uffff\uffff\4\0\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = EoiLexer.unpack_short(22,
		"\1\uffff\3\7\uffff\ufffe\4\uffff\3\6\uffff\ufffe\7\uffff\0\3\3\3\4\3\uffff\ufffe");

	private static final short[] lapg_sym_goto = EoiLexer.unpack_short(16,
		"\0\1\6\10\11\12\12\15\16\16\16\16\17\22\23\24");

	private static final short[] lapg_sym_from = EoiLexer.unpack_short(20,
		"\17\0\2\7\10\15\4\13\6\5\0\7\15\11\0\0\7\15\2\2");

	private static final short[] lapg_sym_to = EoiLexer.unpack_short(20,
		"\20\1\4\1\13\1\7\15\11\10\2\2\2\14\17\3\12\16\5\6");

	private static final short[] tmRuleLen = EoiLexer.unpack_short(8,
		"\1\1\4\3\5\3\1\0");

	private static final short[] tmRuleSymbol = EoiLexer.unpack_short(8,
		"\13\14\14\14\15\15\16\16");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"id",
		"':'",
		"';'",
		"','",
		"_skip",
		"'('",
		"')'",
		"_customEOI",
		"_retfromA",
		"_retfromB",
		"input",
		"expr",
		"list_of_id_and_2_elements_Comma_separated",
		"list_of_id_and_2_elements_Comma_separated_opt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int input = 11;
		static final int expr = 12;
		static final int list_of_id_and_2_elements_Comma_separated = 13;
		static final int list_of_id_and_2_elements_Comma_separated_opt = 14;
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
	protected LapgSymbol[] tmStack;
	protected LapgSymbol tmNext;
	protected EoiLexer tmLexer;

	public Object parse(EoiLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new LapgSymbol[1024];
		tmHead = 0;

		tmStack[0] = new LapgSymbol();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 16) {
			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				break;
			}
		}

		if (tmStack[tmHead].state != 16) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
			throw new ParseException();
		}
		return tmStack[tmHead - 1].value;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.current()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol tmLeft = new LapgSymbol();
		tmLeft.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		tmLeft.symbol = tmRuleSymbol[rule];
		tmLeft.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		LapgSymbol startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		tmLeft.line = startsym.line;
		tmLeft.offset = startsym.offset;
		tmLeft.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(tmLeft, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = tmLeft;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmLeft.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol tmLeft, int tmRule, int tmLength) {
	}
}
