package net.sf.lapg.lalr;

import net.sf.lapg.ParserConflict;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.Symbol;

public class LalrConflict implements ParserConflict {

	private final int kind;
	private final String kindtext;
	private final Input input;

	private final Symbol[] symbols;
	private final Rule[] rules;

	public LalrConflict(int kind, String kindtext, Input input, Symbol[] symbols, Rule[] rules) {
		this.kind = kind;
		this.input = input;
		this.kindtext = kindtext;
		this.symbols = symbols;
		this.rules = rules;
	}

	public int getKind() {
		return kind;
	}

	public String getKindAsText() {
		return kindtext;
	}

	public Input getInput() {
		return input;
	}

	public Rule[] getRules() {
		return rules;
	}

	public Symbol[] getSymbols() {
		return symbols;
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		sb.append("input: ");
		sb.append(input.getText());
		sb.append("\n");
		sb.append(getKindAsText());
		sb.append(" conflict (next: ");
		boolean first = true;
		for(Symbol s : getSymbols()) {
			if(!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(s.getName());
		}
		sb.append(")\n");
		for(Rule r : getRules()) {
			sb.append("    ");
			sb.append(r.toString());
			sb.append('\n');
		}
		return sb.toString();
	}

	public static class InputImpl implements Input {

		private final int state;
		private final Symbol[] symbols;

		public InputImpl(int state, Symbol[] symbols) {
			this.state = state;
			this.symbols = symbols;
		}

		public int getState() {
			return state;
		}

		public Symbol[] getSymbols() {
			return symbols;
		}

		public String getText() {
			StringBuilder sb = new StringBuilder();
			for(Symbol s : symbols) {
				if(sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(s.getName());
			}
			return sb.toString();
		}
	}
}
