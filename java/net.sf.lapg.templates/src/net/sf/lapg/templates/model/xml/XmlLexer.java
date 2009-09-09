package net.sf.lapg.templates.model.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class XmlLexer {

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
		void error(String s);
	};

	final private Reader stream;
	final private ErrorReporter reporter;

	final private char[] token = new char[2048];
	private int len;
		
	final private char[] data = new char[2048];
	private int datalen, l;
	private char chr;

	private int group = 0;
		
	private int lapg_current_line = 1, lapg_current_offset = 0;

	public interface Lexems {
		public static final int eoi = 0;
		public static final int any = 1;
		public static final int LESS = 2;
		public static final int _skipcomment = 3;
		public static final int identifier = 4;
		public static final int ccon = 5;
		public static final int GREATER = 6;
		public static final int EQ = 7;
		public static final int COLON = 8;
		public static final int DIV = 9;
		public static final int _skip = 10;
	}

	

	public XmlLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.stream = stream;
		this.reporter = reporter;
		this.datalen = stream.read(data);
		this.l = 0;
		chr = l < datalen ? data[l++] : 0;
	}

    private static final short lapg_char2no[] = {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 14, 13, 1, 1, 14, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		14, 3, 6, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 4, 1, 10,
		12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 9, 1, 2, 8, 5, 1,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 1, 1, 1, 1, 11,
		1, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
		11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 1, 1, 1, 1, 1,
	};

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, 2, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, },
		{ -1, -1, -1, -1, -1, 4, 5, 6, 7, 8, 9, 10, -1, 11, 11, },
		{ -3, 2, -3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, },
		{ -4, -4, -4, 12, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, },
		{ -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, },
		{ -1, 5, 5, 5, 5, 5, 13, 5, 5, 5, 5, 5, 5, -1, 5, },
		{ -1, 6, 6, 6, 6, 6, 6, 14, 6, 6, 6, 6, 6, -1, 6, },
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, },
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, },
		{ -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, },
		{ -6, -6, -6, -6, 10, -6, -6, -6, -6, -6, -6, 10, 10, -6, -6, },
		{ -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, 11, 11, },
		{ -1, -1, -1, -1, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, },
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, },
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, },
		{ -1, -1, -1, -1, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, },
		{ -1, 16, 16, 16, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, },
		{ -1, 16, 16, 16, 18, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, },
		{ -1, 16, 16, 16, 16, 19, 16, 16, 16, 16, 16, 16, 16, 16, 16, },
		{ -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, },

	};

	public String current() {
		return new String(token,0,len);
	}

	private static int mapCharacter(int chr) {
		return lapg_char2no[(chr+256)%256];
	}

	public LapgSymbol next() throws IOException, UnsupportedEncodingException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {			
			lapg_n.pos = new LapgPlace( lapg_current_line, lapg_current_offset );
			for( len = 0, state = group; state >= 0; ) {
				if( len < 2047 ) token[len++] = chr;
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
					reporter.error( "Unexpected end of file reached");
					break;
				}
				reporter.error( MessageFormat.format( "invalid lexem at line {0}: `{1}`, skipped", lapg_n.pos.line, current() ) );
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
			case 2:
				 group = 1; break; 
			case 3:
				 return false; 
			case 4:
				 lapg_n.sym = new String(token,0,len); break; 
			case 5:
				 lapg_n.sym = new String(token,1,len-2); break; 
			case 6:
				 group = 0; break; 
			case 10:
				 return false; 
		}
		return true;
	} 
}
