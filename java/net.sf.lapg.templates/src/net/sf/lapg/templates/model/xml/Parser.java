// Parser.java

package net.sf.lapg.templates.model.xml;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Parser {
	
	public Parser() {
	}
	
	private static final boolean DEBUG_SYNTAX = false;
	
	private List<?> result;
	
	byte[] buff;
	int l;
	
	private XmlData getData(int start, int end) {
		return new XmlData(buff, start, end-start);
	}
	
	void error( String s ) {
		System.err.println(s);
	}
	
	public List<?> parse(String s) {
		l = 0;
		result = null;
		try {
			buff = s.getBytes("utf-8");
		} catch( UnsupportedEncodingException ex ) {
			return null;
		}
		parse();
		return result;
	}
	
	private void checkTag(XmlNode node, String endTag, int line) {
		if( !node.getTagName().equals(endTag) )
			error("Tag " + node.getTagName() + " is closed with " + endTag + " at line " + line);
	}

	public class lapg_place {
		public int line, offset;

		public lapg_place( int line, int offset ) {
			this.line = line;
			this.offset = offset;
		}
	};

	public class lapg_symbol {
		public Object sym;
		public int  lexem, state;
		public lapg_place pos;
		public lapg_place endpos;
	};

	private static final short[] lapg_char2no = new short[] {
		   0,   1,   1,   1,   1,   1,   1,   1,   1,   2,   3,   1,   1,   4,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   5,   6,   7,   1,   1,   1,   1,   8,   1,   1,   1,   1,   1,   9,   1,  10,
		  11,  12,  13,  14,  15,  16,  17,  18,  19,  20,  21,   1,  22,  23,  24,   1,
		   1,  25,  26,  27,  28,  29,  30,  31,  32,  33,  34,  35,  36,  37,  38,  39,
		  40,  41,  42,  43,  44,  45,  46,  47,  48,  49,  50,   1,   1,   1,   1,  51,
		   1,  52,  53,  54,  55,  56,  57,  58,  59,  60,  61,  62,  63,  64,  65,  66,
		  67,  68,  69,  70,  71,  72,  73,  74,  75,  76,  77,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
		   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,   1,
	};

	private static final short[][] lapg_lexem = new short[][] {
		{  -2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2, },
		{  -1,  -1,   4,   4,   4,   4,  -1,   5,   6,  -1,   7,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   8,  -1,   9,  10,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11, },
		{  -3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,  -3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2, },
		{  -4,  -4,  -4,  -4,  -4,  -4,  12,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4, },
		{ -12, -12,   4,   4,   4,   4, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{  -1,   5,   5,  -1,   5,   5,   5,  13,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5, },
		{  -1,   6,   6,  -1,   6,   6,   6,   6,  14,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6,   6, },
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9, },
		{  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  11,  -6,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  -6,  -6,  -6,  -6,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11,  11, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  15,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7, },
		{  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  16,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1, },
		{  -1,  16,  16,  16,  16,  16,  16,  16,  16,  17,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16, },
		{  -1,  16,  16,  16,  16,  16,  16,  16,  16,  18,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16, },
		{  -1,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  19,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16,  16, },
		{  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5, },
	};

	private static final int[] lapg_action = new int[] {
		  -1,   6,  -1,  -3,   2,  -1,   5, -11, -21,   1,  -1,  -1,   3,  -1,  -1,  -1,
		 -29,  15,  -1,   4,   8,  -1,  11,  -1,  14,  -1,  16,  12,  13,  -1,  -2,
	};

	private static final short[] lapg_lalr = new short[] {
		   1,  -1,   2,  -1,   0,   0,  -1,  -2,   8,  -1,   4,   7,   6,   7,   9,   7,
		  -1,  -2,   4,  -1,   6,  10,   9,  10,  -1,  -2,   4,  -1,   6,   9,   9,   9,
		  -1,  -2,
	};

	private static final short[] lapg_sym_goto = new short[] {
		   0,   1,   5,   9,   9,  15,  16,  19,  20,  21,  23,  23,  24,  26,  30,  34,
		  36,  40,  43,  44,  45,  47,
	};

	private static final short[] lapg_sym_from = new short[] {
		  29,   0,   3,   5,  11,   0,   3,   5,  11,   2,   8,  10,  13,  16,  18,  21,
		  15,  23,  25,  14,   7,  10,  15,   0,   0,   5,   0,   3,   5,  11,   0,   3,
		   5,  11,   5,  11,   0,   3,   5,  11,   2,  10,  18,   8,   8,   8,  16,
	};

	private static final short[] lapg_sym_to = new short[] {
		  30,   1,   1,   1,   1,   2,   2,  10,  10,   7,  14,   7,  20,  14,   7,  26,
		  22,  27,  28,  21,  13,  18,  23,  29,   3,  11,   4,   9,   4,   9,   5,   5,
		   5,   5,  12,  19,   6,   6,   6,   6,   8,   8,  25,  15,  16,  17,  24,
	};

	private static final short[] lapg_rlen = new short[] {
		   1,   2,   1,   2,   3,   1,   1,   1,   3,   1,   0,   4,   5,   4,   2,   1,
		   3,
	};

	private static final short[] lapg_rlex = new short[] {
		  11,  12,  12,  13,  13,  13,  13,  17,  17,  18,  18,  14,  16,  15,  19,  19,
		  20,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"any",
		"'<'",
		"_skipcomment",
		"identifier",
		"ccon",
		"'>'",
		"'='",
		"':'",
		"'/'",
		"_skip",
		"input",
		"xml_tags",
		"xml_tag_or_space",
		"tag_start",
		"tag_end",
		"no_body_tag",
		"tag_name",
		"attributesopt",
		"attributes",
		"attribute",
	};

	public enum Tokens {
		eoi,
		any,
		LESS,
		_skipcomment,
		identifier,
		ccon,
		GREATER,
		EQ,
		COLON,
		DIV,
		_skip,
		input,
		xml_tags,
		xml_tag_or_space,
		tag_start,
		tag_end,
		no_body_tag,
		tag_name,
		attributesopt,
		attributes,
		attribute,
	}

	private static int lapg_next( int state, int symbol ) {
		int p;
		if( lapg_action[state] < -2 ) {
			for( p = - lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2 )
				if( lapg_lalr[p] == symbol ) break;
			return lapg_lalr[p+1];
		}
		return lapg_action[state];
	}

	private static int lapg_state_sym( int state, int symbol ) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol+1]-1;
		int i, e;

		while( min <= max ) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if( i == state )
				return lapg_sym_to[e];
			else if( i < state )
				min = e + 1;
			else
				max = e - 1;
		}
		return -1;
	}

	public boolean parse() {

		byte[]        token = new byte[1024];
		int           lapg_head = 0, group = 0, lapg_i, lapg_size, chr;
		lapg_symbol[] lapg_m = new lapg_symbol[1024];
		lapg_symbol   lapg_n;
		int           lapg_current_line = 1, lapg_current_offset = 0;

		lapg_m[0] = new lapg_symbol();
		lapg_m[0].state = 0;
		chr = l < buff.length ? buff[l++] : 0;

		do {
			lapg_n = new lapg_symbol();
			lapg_n.pos = new lapg_place( lapg_current_line, lapg_current_offset );
			for( lapg_size = 0, lapg_i = group; lapg_i >= 0; ) {
				if( lapg_size < 1024-1 ) token[lapg_size++] = (byte)chr;
				lapg_i = lapg_lexem[lapg_i][lapg_char2no[(chr+256)%256]];
				if( lapg_i >= -1 && chr != 0 ) { 
					lapg_current_offset++;
					if( chr == '\n' ) lapg_current_line++;
					chr = l < buff.length ? buff[l++] : 0;
				}
			}
			lapg_n.endpos = new lapg_place( lapg_current_line, lapg_current_offset );

			if( lapg_i == -1 ) {
				if( chr == 0 ) {
					error( "Unexpected end of file reached");
					break;
				}
				error( MessageFormat.format( "invalid lexem at line {0}: `{1}`, skipped", lapg_n.pos.line, new String(token,0,lapg_size) ) );
				lapg_n.lexem = -1;
				continue;
			}

			lapg_size--;
			lapg_n.lexem = -lapg_i-2;
			lapg_n.sym = null;

			switch( lapg_n.lexem ) {
				case 2:
					 group = 1; break; 
				case 3:
					 continue;
				case 4:
					 lapg_n.sym = new String(token,0,lapg_size); break; 
				case 5:
					 lapg_n.sym = new String(token,1,lapg_size-2); break; 
				case 6:
					 group = 0; break; 
				case 10:
					 continue;
			}


			do {
				lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

				if( lapg_i >= 0 ) {
					lapg_symbol lapg_gg = new lapg_symbol();
					lapg_gg.sym = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].sym:null;
					lapg_gg.lexem = lapg_rlex[lapg_i];
					lapg_gg.state = 0;
					if( DEBUG_SYNTAX )
						System.out.println( "reduce to " + lapg_syms[lapg_rlex[lapg_i]] );
					lapg_gg.pos = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head+1-lapg_rlen[lapg_i]].pos:lapg_n.pos;
					lapg_gg.endpos = (lapg_rlen[lapg_i]!=0)?lapg_m[lapg_head].endpos:lapg_n.pos;
					switch( lapg_i ) {
						case 0:
							 result = ((List<Object>)lapg_m[lapg_head-0].sym); 
							break;
						case 1:
							 ((List<Object>)lapg_gg.sym).add(lapg_m[lapg_head-0].sym); 
							break;
						case 2:
							 lapg_gg.sym = new ArrayList<Object>(); ((List<Object>)lapg_gg.sym).add(lapg_m[lapg_head-0].sym); 
							break;
						case 3:
							 checkTag(((XmlNode)lapg_m[lapg_head-1].sym),((String)lapg_m[lapg_head-0].sym),lapg_m[lapg_head-0].pos.line); 
							break;
						case 4:
							 checkTag(((XmlNode)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head-0].sym),lapg_m[lapg_head-0].pos.line); ((XmlNode)lapg_m[lapg_head-2].sym).data = ((List<Object>)lapg_m[lapg_head-1].sym); 
							break;
						case 6:
							 lapg_gg.sym = getData(lapg_m[lapg_head-0].pos.offset,lapg_m[lapg_head-0].endpos.offset); 
							break;
						case 7:
							 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
							break;
						case 8:
							 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + ":" + ((String)lapg_m[lapg_head-0].sym); 
							break;
						case 11:
							 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-2].sym), ((List<XmlAttribute>)lapg_m[lapg_head-1].sym), lapg_m[lapg_head-3].pos.line); 
							break;
						case 12:
							 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-3].sym), ((List<XmlAttribute>)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-4].pos.line); 
							break;
						case 13:
							 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
							break;
						case 14:
							 ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head-0].sym)); 
							break;
						case 15:
							 lapg_gg.sym = new ArrayList<XmlAttribute>(); ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head-0].sym)); 
							break;
						case 16:
							 lapg_gg.sym = new XmlAttribute(((String)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head-0].sym)); 
							break;
					}
					for( int e = lapg_rlen[lapg_i]; e > 0; e-- ) 
						lapg_m[lapg_head--] = null;
					lapg_m[++lapg_head] = lapg_gg;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_gg.lexem );
				} else if( lapg_i == -1 ) {
					lapg_m[++lapg_head] = lapg_n;
					lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
					if( DEBUG_SYNTAX )
						System.out.println( MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], new String(token,0,lapg_size) ) );
				}

			} while( lapg_i >= 0 && lapg_m[lapg_head].state != -1 );

			if( (lapg_i == -2 || lapg_m[lapg_head].state == -1) && lapg_n.lexem != 0 ) {
				break;
			}

		} while( lapg_n.lexem != 0 );

		if( lapg_m[lapg_head].state != 31-1 ) {
			error( MessageFormat.format( "syntax error before line {0}", lapg_n.pos.line ) );
			return false;
		};
		return true;
	}
}
