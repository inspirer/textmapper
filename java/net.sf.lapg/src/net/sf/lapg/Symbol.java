package net.sf.lapg;

public class Symbol {

	public String name, type;
	public int index;
	public int opt;
	public int sibling;
	public int length, rpos;
	public boolean empty, term, good, employed, defed, temp, is_attr, has_attr;
	
	
	public Symbol(String name, String type, int index, int opt, int sibling) {
		this.name = name;
		this.type = type;
		this.index = index;
		this.opt = opt;
		this.sibling = sibling;
		this.rpos = this.length = 0;
		
		this.empty = this.term = this.good = this.employed = this.defed = this.temp = this.has_attr = this.is_attr = false;
	}
}