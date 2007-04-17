package net.sf.lapg;

import java.util.Iterator;
import java.util.Vector;

import net.sf.lapg.lalr.Grammar;
import net.sf.lapg.lalr.IError;
import net.sf.lapg.lalr.Rule;
import net.sf.lapg.lalr.Symbol;
import net.sf.lapg.lalr.Engine.Result;

public class GrammarGenerator {
	
	public static final int SYM_PLACE_RIGHT = 0;		// symbol from right part of rule, or define attr for sname (see sibling)	 
	public static final int SYM_PLACE_LEFT = 1;			// symbol from left part of rule 
	public static final int SYM_PLACE_LATTRIB = 2;		// create new symbol for L-attrib definition 

	public static final int PRIO_UNDEFINED = -1; 

	Vector<Rule> rules = new Vector<Rule>();
	Vector<Symbol> syms = new Vector<Symbol>();
	Vector<Integer> priorul = new Vector<Integer>();
	int nterms, input, eoi, errorn, situations = 0;
	
	IError err;
	int errors;
	
	private Symbol symbol(String name, int symPlace, String type, int sibling) {
		Symbol s, existing = null;

		// search's for a symbol in the table
		for (Iterator<Symbol> it = syms.iterator(); it.hasNext();) {
			s = it.next();
			if (!s.is_attr && name.equals(s.name)) {
				existing = s;
				break;
			}
		}

		// symbol have been found
		if (existing != null) {
			switch (symPlace) {
			case SYM_PLACE_RIGHT:
				if (sibling != -2) {
					if (existing.has_attr) {
						err.error(0, "L-attribute for symbol `" + existing.name
								+ "' already defined\n");
					} else {
						existing.has_attr = true;
						existing.sibling = sibling;
					}
				}
				return existing;
			case SYM_PLACE_LEFT:
				if (existing.term) {
					err.error(0,
							"error: terminal used as the left part of rule: "
									+ name + "\n");
					errors++;
				} else {
					existing.defed = true;
					if (type != null && existing.type == null)
						existing.type = type;
				}
				return existing;
			case SYM_PLACE_LATTRIB:
				name = "{}";
				break;
			}
		}

		// initialize structure
		s = new Symbol(name, type, syms.size(), -1, sibling);
		s.defed = (symPlace != 0);
		syms.add(s);

		if (symPlace == SYM_PLACE_RIGHT && sibling != -2)
			s.has_attr = true;

		if (symPlace == SYM_PLACE_LATTRIB) {
			assert existing != null;
			s.is_attr = true;
		}

		// check for *opt symbol
		int i = name.length() - 3;
		if (symPlace != SYM_PLACE_LATTRIB && i > 0 && name.endsWith("opt")) {

			if (symPlace == SYM_PLACE_LEFT) {
				err.error(0, "error: defined symbol with opt at end: `" + name
						+ "'\n");
				errors++;
			}

			String realName = name.substring(0, i);

			Symbol k = symbol(realName, SYM_PLACE_RIGHT, null, -2);
			k.opt = s.index;

			int[] one_rule = new int[] { s.index, k.index };
			rule(1, 0, null, one_rule, -1);
			rule(0, 0, null, one_rule, -1);
			s.defed = true;
		}
		return s;
	}

	private Symbol terminal(String name, String type) {
		
		assert syms.size() == nterms;

		if (name.equals("input")) {
			err.error(0, "error: wrong name for terminal: " + name + "\n");
			errors++;
			return terminal("$" + name, type);
		} else {
			Symbol s = symbol(name, SYM_PLACE_RIGHT, type, -2);
			s.term = true;
			nterms++;

			if (name.equals("error"))
				errorn = s.index;
			return s;
		}
	}

	private void set_input(int i) {
		assert input == -1 && i >= nterms && i < syms.size();
		input = i;
	}


	private void set_eoi(int i) {
		assert eoi == -1 && i < nterms && i >= 0;
		eoi = i;
	}

	private void rule(int length, int priority, String action, int[] array, int defline) {
		int i, left = array[0];

		// calculate priority if needed
		if( priority == PRIO_UNDEFINED ) {
			for( i = length; i > 0; i-- ) {
				if( syms.get(array[i]).term ) {
					priority = array[i];
					break;
				}
			}
		}

		if( length == 0 ) 
			syms.get(left).empty = true;

		int[] right = new int[length+1];
		for( i = 0; i < length; i++)
			right[i] = array[i+1];
		right[length] = -1-rules.size();
		Rule r = new Rule(left, defline, priority, action, right);
		rules.add(r);

		for( i = 0; i < length; i++ ) {
			Symbol s = syms.get(right[i]);
			if( s.is_attr ){
				s.length = i;
				s.rpos = situations;

			} else if( s.has_attr ) {
				if( i == 0 && right[i] != left // we allow left-recursive rules
				 || i > 0 && s.sibling == -1 && !syms.get(right[i-1]).is_attr
				 || i > 0 && s.sibling >= 0  && right[i-1] != s.sibling )
					err.error( 0, "L-attribute for symbol `"+s.name+"' is omitted\n" );
			}
		}

		situations += right.length;
	}
		
	void addprio(String id, int prio, boolean restofgroup) {
		if (prio > 0) {
			Symbol s = symbol(id, 0, null, -2);
			if (!restofgroup) {
				priorul.add(-prio);
			}
			priorul.add(s.index);
		}
	}

	public Grammar getGrammar() {
		return new Grammar(syms, rules, priorul, nterms, situations, input, eoi, errorn);
	}
}
