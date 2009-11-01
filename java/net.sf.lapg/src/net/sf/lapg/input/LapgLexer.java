package net.sf.lapg.input;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class LapgLexer {

	public static class LapgPlace {
		public int line, offset;

		public LapgPlace( int line, int offset ) {
			this.line = line;
			this.offset = offset;
		}
	};

	public static class LapgSymbol {
		public Object sym;
		public int  lexem, state;
		public LapgPlace pos;
		public LapgPlace endpos;
	};
	
	public interface ErrorReporter {
		void error(LapgPlace start, LapgPlace end, String s);
	};

	final private Reader stream;
	final private ErrorReporter reporter;

	final private char[] token = new char[4096];
	private int len;
		
	final private char[] data = new char[2048];
	private int datalen, l;
	private char chr;

	private int group = 0;
		
	private int lapg_current_line = 1, lapg_current_offset = 0;

	public interface Lexems {
		public static final int eoi = 0;
		public static final int identifier = 1;
		public static final int regexp = 2;
		public static final int scon = 3;
		public static final int type = 4;
		public static final int icon = 5;
		public static final int PERC = 6;
		public static final int _skip = 7;
		public static final int COLONCOLONEQ = 8;
		public static final int OR = 9;
		public static final int SEMICOLON = 10;
		public static final int DOT = 11;
		public static final int COLON = 12;
		public static final int LBRACKET = 13;
		public static final int RBRACKET = 14;
		public static final int LESSLESS = 15;
		public static final int LBRACE = 16;
		public static final int iLBRACE = 17;
		public static final int RBRACE = 18;
	}

	
	private int deep = 0;
	private int templatesStart = -1;
	
	int getTemplatesStart() {
		return templatesStart;
	}

	public LapgLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.stream = stream;
		this.reporter = reporter;
		this.datalen = stream.read(data);
		this.l = 0;
		chr = l < datalen ? data[l++] : 0;
	}

    private static final short lapg_char2no[] = {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 25, 5, 1, 1, 25, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		25, 1, 6, 11, 1, 10, 1, 2, 7, 8, 1, 1, 1, 9, 16, 3,
		30, 30, 30, 30, 30, 30, 30, 30, 24, 24, 12, 15, 19, 13, 1, 26,
		1, 28, 28, 28, 28, 28, 28, 23, 23, 23, 23, 23, 23, 23, 23, 23,
		23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 17, 4, 18, 1, 23,
		1, 29, 29, 28, 28, 28, 29, 23, 23, 23, 23, 23, 23, 23, 27, 23,
		23, 23, 27, 23, 27, 23, 27, 23, 21, 23, 23, 20, 14, 22, 1, 1,
	};

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, -1, 2, 3, -1, 4, 5, 6, -1, 7, -1, 8, 9, -1, 10, 11, 12, 13, 14, 15, 16, 17, -1, 17, 18, 19, -1, 17, 17, 17, 18, },
		{ -1, 20, 21, 20, 20, 20, 22, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 23, 20, 24, 20, 20, 20, 20, 20, 20, 20, 20, },
		{ -1, 25, -1, 25, 25, -1, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, },
		{ -1, 3, 3, 26, 27, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, },
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 28, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, },
		{ -1, 5, 5, 5, -1, -1, 29, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, },
		{ -1, 30, 30, 30, 30, -1, 30, 30, -1, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, },
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 18, -1, -1, -1, -1, -1, 18, },
		{ -9, 8, 8, 8, 8, -9, 8, 8, 8, 8, 31, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, },
		{ -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, 32, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, -14, },
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, },
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, },
		{ -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, -13, },
		{ -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, -15, },
		{ -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, -16, },
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 33, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, },
		{ -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, -18, },
		{ -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 17, -3, 17, 17, -3, -3, 17, 17, 17, 17, },
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, 18, -7, -7, -7, -7, -7, 18, },
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 19, -9, -9, -9, -9, -9, },
		{ -9, 20, -9, 20, 20, 20, -9, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, -9, 20, -9, 20, 20, 20, 20, 20, 20, 20, 20, },
		{ -1, 21, 34, 21, 35, -1, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, },
		{ -1, 22, 22, 22, 36, -1, 37, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, },
		{ -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, -19, },
		{ -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, -20, },
		{ -1, 25, 38, 25, 25, -1, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, 25, },
		{ -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, },
		{ -1, 3, 3, 3, 3, -1, 3, 3, 3, 3, 31, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, },
		{ -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, 39, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, },
		{ -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, },
		{ -1, 30, 30, 30, 30, -1, 30, 30, 40, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, },
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, },
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 41, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, },
		{ -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, -17, },
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, },
		{ -1, -1, 21, -1, 21, -1, 21, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 42, -1, -1, -1, -1, 21, 21, -1, 21, 43, },
		{ -1, -1, 22, -1, 22, -1, 22, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 44, -1, -1, -1, -1, 22, 22, -1, 22, 45, },
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, },
		{ -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, },
		{ -2, 39, 39, 39, 39, -2, 39, 39, 39, 39, 31, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, 39, },
		{ -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, },
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 46, -1, -1, -1, 46, 46, 46, },
		{ -1, 21, 34, 21, 35, -1, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 47, },
		{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 48, -1, -1, -1, 48, 48, 48, },
		{ -1, 22, 22, 22, 36, -1, 37, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 49, },
		{ -1, 21, 34, 21, 35, -1, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 46, 21, 21, 21, 46, 46, 46, },
		{ -1, 21, 34, 21, 35, -1, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, },
		{ -1, 22, 22, 22, 36, -1, 37, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 48, 22, 22, 22, 48, 48, 48, },
		{ -1, 22, 22, 22, 36, -1, 37, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, },

	};

	public String current() {
		return new String(token,0,len);
	}

	private static int mapCharacter(int chr) {
		if(chr >= 0 && chr < 128) {
			return lapg_char2no[chr];
		}
		return 1;
	}

	public LapgSymbol next() throws IOException, UnsupportedEncodingException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {			
			lapg_n.pos = new LapgPlace( lapg_current_line, lapg_current_offset );
			for( len = 0, state = group; state >= 0; ) {
				if( len < 4095 ) token[len++] = chr;
				state = lapg_lexem[state][mapCharacter(chr)];
				if( state >= -1 && chr != 0 ) { 
					lapg_current_offset++;
					if( chr == '\n' ) {
						lapg_current_line++;
					}
					if( l >= datalen ) {
						this.datalen = stream.read(data);
						l = 0;
					}
					chr = l < datalen ? data[l++] : 0;
				}
			}
			lapg_n.endpos = new LapgPlace( lapg_current_line, lapg_current_offset );

			if( state == -1 ) {
				if( chr == 0 ) {
					reporter.error(lapg_n.pos, lapg_n.endpos, "Unexpected end of file reached");
					break;
				}
				reporter.error(lapg_n.pos, lapg_n.endpos, MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", lapg_n.pos.line, current()));
				lapg_n.lexem = -1;
				continue;
			}

			len--;
			lapg_n.lexem = -state-2;
			lapg_n.sym = null;

		} while(lapg_n.lexem == -1 || !createToken(lapg_n));
		return lapg_n;
	}

	private boolean createToken(LapgSymbol lapg_n) throws UnsupportedEncodingException {
		switch( lapg_n.lexem ) {
			case 1:
				 lapg_n.sym = new String(token,0,len); break; 
			case 2:
				 lapg_n.sym = new String(token,1,len-2); break; 
			case 3:
				 lapg_n.sym = new String(token,1,len-2); break; 
			case 4:
				 lapg_n.sym = new String(token,1,len-2); break; 
			case 5:
				 lapg_n.sym = Integer.parseInt(new String(token,0,len)); break; 
			case 0:
				 templatesStart = lapg_n.endpos.offset; break; 
			case 7:
				 return false; 
			case 16:
				 deep = 1; group = 1; break; 
			case 17:
				 deep++; break; 
			case 18:
				 if( --deep == 0 ) group = 0; break; 
		}
		return true;
	} 
}
