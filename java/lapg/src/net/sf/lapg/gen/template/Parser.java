// Parser.java

package net.sf.lapg.gen.template;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class Parser {
	
	private static final boolean DEBUG_SYNTAX = true;
	
	int killEnds = -1;
	TemplateContext context = new TemplateContext(null);
	byte[] buff;
	int l;
	
	private String lexem(int start, int end) {
		if( killEnds == start ) {
			while( start < end && (buff[start] == '\t' || buff[start] == ' ') )
				start++;
	
			if( start < end && buff[start] == '\r' )
				start++;
	
			if( start < end && buff[start] == '\n' )
				start++;
		}
		try {
			return new String(buff, start, end-start, "utf-8");
		} catch(UnsupportedEncodingException ex) {
			return "";
		}
	}
	
	private void killEndsForBrace(int offset) {
		killEnds = offset+1;
	}
	
	void error( String s ) {
		System.err.println(s);
	}
	
	public boolean parse(String s) {
		l = 0;
		try {
			buff = s.getBytes("utf-8");
		} catch( UnsupportedEncodingException ex ) {
			return false;
		}
		return parse();
	}
	
	public String getResult() {
		return context.toString();
	}
	
	public static void main(String[] args) {
		Parser p = new Parser();
		p.parse(
			"Okey, here is template\n"+
			"\n"+
			"${foreach a in list-}\n"+
			"inside foreach\n"+
			"${if aaa}inside if${aaa}${end}Æ\n"+
			"after if\n"+
			"${end-}\n"+
			"after end\n" );
	
		System.out.println("----\n"+p.getResult());
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
		   5,   1,   1,   6,   7,   1,   1,   1,   1,   1,   1,   1,   1,   8,   1,   1,
		   9,  10,  11,  12,  13,  14,  15,  16,  17,  18,   1,   1,   1,   1,   1,   1,
		   1,  19,  20,  21,  22,  23,  24,  25,  26,  27,  28,  29,  30,  31,  32,  33,
		  34,  35,  36,  37,  38,  39,  40,  41,  42,  43,  44,   1,   1,   1,   1,  45,
		   1,  46,  47,  48,  49,  50,  51,  52,  53,  54,  55,  56,  57,  58,  59,  60,
		  61,  62,  63,  64,  65,  66,  67,  68,  69,  70,  71,  72,   1,  73,   1,   1,
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
		{  -2,   2,   2,   2,   2,   2,   2,   3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2, },
		{  -1,  -1,   4,   4,   4,   4,   5,  -1,   6,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   8,   9,   7,   7,  10,   7,   7,   7,   7,  11,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -1,  12, },
		{  -3,   2,   2,   2,   2,   2,   2,  -3,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2,   2, },
		{  -1,  -1,  -1,  -1,  -1,  -1,  -1,  13,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  15,  -1, },
		{ -14, -14,   4,   4,   4,   4, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, },
		{ -14,   5,   5, -14,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5,   5, },
		{ -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  16,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  17,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  18,   7,   7,   7,   7,   7,   7,   7,  19,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  20,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3,  -3, },
		{  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  -4,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  14,  -4,  -4, },
		{  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5,  -5, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  21,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  22,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,  -9,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -9,  -9, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7, -10, -10, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  23,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,  -7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -7,  -7, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  24,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7, -11, -11, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  25,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  26,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,  -6,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  27,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -6,  -6, },
		{  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,  -8,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,   7,  -8,  -8, },
	};

	private static final int[] lapg_action = new int[] {
		  -1,   8,   7,  -1,  -3,   2,   5,   6,  -1,  10,  -1, -13, -19, -25,   1,  -1,
		  -1,  14,  -1,   3,  -1,  -1,  -1,  11,  -1,  16,   9,  12, -31,  13,  -1,  17,
		  -1,  -2,
	};

	private static final short[] lapg_lalr = new short[] {
		   1,  -1,   2,  -1,   3,  -1,   0,   0,  -1,  -2,   9,  -1,   4,  15,  -1,  -2,
		  11,  -1,  10,   4,  -1,  -2,  11,  -1,  10,   4,  -1,  -2,  11,  -1,  10,   4,
		  -1,  -2,
	};

	private static final short[] lapg_sym_goto = new short[] {
		   0,   1,   5,   9,  13,  18,  19,  21,  23,  24,  25,  28,  31,  31,  32,  34,
		  38,  41,  45,  49,  51,  55,  56,  58,  59,
	};

	private static final short[] lapg_sym_from = new short[] {
		  32,   0,   4,   8,  15,   0,   4,   8,  15,   0,   4,   8,  15,   3,  10,  18,
		  22,  24,  22,   3,  22,   3,  22,  16,  11,  20,  21,  30,  12,  13,  28,   0,
		   0,   8,   0,   4,   8,  15,  12,  13,  28,   0,   4,   8,  15,   0,   4,   8,
		  15,   3,  22,   0,   4,   8,  15,  15,   3,  22,  11,
	};

	private static final short[] lapg_sym_to = new short[] {
		  33,   1,   1,   1,   1,   2,   2,   2,   2,   3,   3,   3,  22,   9,  16,  25,
		   9,  29,  28,  10,  10,  11,  11,  24,  17,  26,  27,  31,  19,  19,  19,  32,
		   4,  15,   5,  14,   5,  14,  20,  21,  30,   6,   6,   6,   6,   7,   7,   7,
		   7,  12,  12,   8,   8,   8,   8,  23,  13,  13,  18,
	};

	private static final short[] lapg_rlen = new short[] {
		   1,   2,   1,   1,   0,   1,   1,   1,   1,   4,   1,   3,   4,   4,   1,   0,
		   3,   4,
	};

	private static final short[] lapg_rlex = new short[] {
		  13,  14,  14,  16,  16,  15,  15,  15,  15,  18,  19,  17,  20,  22,  23,  23,
		  22,  21,
	};

	private static final String[] lapg_syms = new String[] {
		"eoi",
		"any",
		"escid",
		"'${'",
		"identifier",
		"Lend",
		"Lforeach",
		"Lif",
		"Lin",
		"Lnot",
		"'}'",
		"'-'",
		"_skip",
		"input",
		"instructions",
		"instruction",
		"kill_ends",
		"control_instruction",
		"simple_instruction",
		"out_sentence",
		"control_start_instruction",
		"control_end_instruction",
		"control_sentence",
		"Lnotopt",
	};

	public enum Tokens {
		eoi,
		any,
		escid,
		N24LBRACE,
		identifier,
		Lend,
		Lforeach,
		Lif,
		Lin,
		Lnot,
		RBRACE,
		MINUS,
		_skip,
		input,
		instructions,
		instruction,
		kill_ends,
		control_instruction,
		simple_instruction,
		out_sentence,
		control_start_instruction,
		control_end_instruction,
		control_sentence,
		Lnotopt,
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
					 lapg_n.sym = new String(token,1,lapg_size-1); break; 
				case 3:
					 group = 1; break; 
				case 4:
					 lapg_n.sym = new String(token,0,lapg_size); break; 
				case 10:
					 group = 0; break; 
				case 12:
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
						case 3:
							 lapg_gg.sym = Boolean.TRUE; 
							break;
						case 4:
							 lapg_gg.sym = Boolean.FALSE; 
							break;
						case 7:
							 context.acceptVar(((String)lapg_m[lapg_head-0].sym)); 
							break;
						case 8:
							 context.accept(lexem(lapg_m[lapg_head-0].pos.offset,lapg_m[lapg_head-0].endpos.offset)); 
							break;
						case 9:
							 if( ((Boolean)lapg_m[lapg_head-1].sym) ) killEndsForBrace(lapg_m[lapg_head-0].pos.offset); 
							break;
						case 10:
							 context.acceptVar(((String)lapg_m[lapg_head-0].sym)); 
							break;
						case 12:
							 if( ((Boolean)lapg_m[lapg_head-1].sym) ) killEndsForBrace(lapg_m[lapg_head-0].pos.offset); 
							break;
						case 17:
							 if( ((Boolean)lapg_m[lapg_head-1].sym) ) killEndsForBrace(lapg_m[lapg_head-0].pos.offset); 
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

		if( lapg_m[lapg_head].state != 34-1 ) {
			error( MessageFormat.format( "syntax error before line {0}", lapg_n.pos.line ) );
			return false;
		};
		return true;
	}
}
