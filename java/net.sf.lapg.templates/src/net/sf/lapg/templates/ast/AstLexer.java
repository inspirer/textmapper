package net.sf.lapg.templates.ast;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

public class AstLexer {

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

	final private InputStream stream;
	final private ErrorReporter reporter;
	final private String encoding;

	final private byte[] token = new byte[2048];
	private int len;
		
	final private byte[] data = new byte[512];
	private int datalen, l, chr;

	private int group = 0;
		
	private int lapg_current_line = 1, lapg_current_offset = 0;

	public interface Lexems {
		public static final int eoi = 0;
		public static final int any = 1;
		public static final int escdollar = 2;
		public static final int escid = 3;
		public static final int escint = 4;
		public static final int N24LBRACE = 5;
		public static final int N24DIV = 6;
		public static final int identifier = 7;
		public static final int icon = 8;
		public static final int ccon = 9;
		public static final int Lcall = 10;
		public static final int Lcase = 11;
		public static final int Lend = 12;
		public static final int Lelse = 13;
		public static final int Leval = 14;
		public static final int Lfalse = 15;
		public static final int Lfor = 16;
		public static final int Lfile = 17;
		public static final int Lforeach = 18;
		public static final int Lgrep = 19;
		public static final int Lif = 20;
		public static final int Lin = 21;
		public static final int Lmap = 22;
		public static final int Lnot = 23;
		public static final int Lnull = 24;
		public static final int Lswitch = 25;
		public static final int Ltemplate = 26;
		public static final int Ltrue = 27;
		public static final int Lthis = 28;
		public static final int Lassert = 29;
		public static final int RBRACE = 30;
		public static final int MINUSRBRACE = 31;
		public static final int PLUS = 32;
		public static final int MINUS = 33;
		public static final int MULT = 34;
		public static final int DIV = 35;
		public static final int PERC = 36;
		public static final int EXCL = 37;
		public static final int LBRACKET = 38;
		public static final int RBRACKET = 39;
		public static final int LROUNDBRACKET = 40;
		public static final int RROUNDBRACKET = 41;
		public static final int DOT = 42;
		public static final int COMMA = 43;
		public static final int AMPAMP = 44;
		public static final int OROR = 45;
		public static final int EQEQ = 46;
		public static final int EXCLEQ = 47;
		public static final int MINUSGREATER = 48;
		public static final int LESSEQ = 49;
		public static final int GREATEREQ = 50;
		public static final int LESS = 51;
		public static final int GREATER = 52;
		public static final int COLON = 53;
		public static final int QUESTMARK = 54;
		public static final int N60 = 55;
		public static final int N24 = 56;
		public static final int _skip = 57;
	}

	public AstLexer(InputStream stream, ErrorReporter reporter, String encoding) throws IOException {
		this.stream = stream;
		this.reporter = reporter;
		this.encoding = encoding;
		this.datalen = stream.read(data);
		this.l = 0;
		chr = l < datalen ? data[l++] : 0;
	}

	private final short[] lapg_char2no = new short[] {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 1, 1, 4, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
		21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 1, 32, 33, 34, 35,
		1, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
		51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 1, 65,
		66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81,
		82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	};
	
	private int lapg_lexem[][] = unpackFromString(122,96,
		"-2,2:8,3,2:86,-1:2,4:4,5,-1:2,6,7,8,9,10,11,12,13,14,15,16,17,18:10,19,20,2" +
		"1,22,23,24:26,25,-1,26,24,27,28,24,29,24,30,31,32,24,33,24:3,34,35,24:4,36," +
		"37,24:6,-1,38,39,-3,2:8,-3,2:86,-1:9,40,-1:10,41,42:10,-1:5,43:26,-1:3,43,-" +
		"1,43:26,44,-1:2,-59:2,4:4,-59:90,-39:33,45,-39:62,-58:96,-38:96,-1:11,46,-1" +
		":85,9:2,-1,9:8,47,9:50,48,9:32,-42:96,-43:96,-36:96,-34:96,-45:96,-35:34,49" +
		",-35:60,50,-44:96,-37:96,-10:21,18:10,-10:65,-55:96,-53:33,51,-53:62,-1:33," +
		"52,-1:62,-54:33,53,-54:62,-56:96,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:26,-9" +
		":3,-40:96,-41:96,-57:96,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:18,54,24:7,-9:" +
		"24,24:10,-9:5,24:26,-9:3,24,-9,55,24:25,-9:24,24:10,-9:5,24:26,-9:3,24,-9,2" +
		"4:11,56,24,57,24:7,58,24:4,-9:24,24:10,-9:5,24:26,-9:3,24,-9,59,24:7,60,24:" +
		"5,61,24:11,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:17,62,24:8,-9:24,24:10,-9:5" +
		",24:26,-9:3,24,-9,24:5,63,24:7,64,24:12,-9:24,24:10,-9:5,24:26,-9:3,24,-9,6" +
		"5,24:25,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:14,66,24:5,67,24:5,-9:24,24:10" +
		",-9:5,24:26,-9:3,24,-9,24:22,68,24:3,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:4" +
		",69,24:2,70,24:9,71,24:8,-9:3,-1:94,72,-1,-32:96,-4:96,-8:96,-6:21,42:10,-6" +
		":65,-5:8,73,-5:12,43:10,-5:5,43:26,-5:3,43,-5,43:26,-5:3,-7:96,-49:96,-46:9" +
		"6,-11:96,-1:7,9,-1:4,9,-1:8,74:8,-1:6,9,-1:27,9,-1:3,9:2,-1:3,9,-1:7,9,-1:3" +
		",9,-1,9,-1,9,-1,75,-1:5,-50:96,-33:96,-51:96,-48:96,-52:96,-9:21,24:10,-9:5" +
		",24:26,-9:3,24,-9,24:18,76,24:7,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:11,77," +
		"24:6,78,24:7,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:18,79,24:7,-9:24,24:10,-9" +
		":5,24:26,-9:3,24,-9,24:3,80,24:22,-9:24,24:10,-9:5,24:26,-9:3,24,-9,81,24:2" +
		"5,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:11,82,24:14,-9:24,24:10,-9:5,24:26,-" +
		"9:3,24,-9,24:11,83,24:14,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:17,84,24:8,-9" +
		":24,24:10,-9:5,24:26,-9:3,24,-9,24:4,85,24:21,-9:3,-22:21,24:10,-22:5,24:26" +
		",-22:3,24,-22,24:26,-22:3,-23:21,24:10,-23:5,24:26,-23:3,24,-23,24:26,-23:3" +
		",-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:15,86,24:10,-9:24,24:10,-9:5,24:26,-9" +
		":3,24,-9,24:19,87,24:6,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:11,88,24:14,-9:" +
		"24,24:10,-9:5,24:26,-9:3,24,-9,24:8,89,24:17,-9:24,24:10,-9:5,24:26,-9:3,24" +
		",-9,24:12,90,24:13,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:8,91,24:17,-9:24,24" +
		":10,-9:5,24:26,-9:3,24,-9,24:20,92,24:5,-9:3,-47:96,-1:21,93:10,-1:66,9:2,-" +
		"1,9:8,47,9:8,94:8,9:34,48,9:32,-1:21,95:10,-1:5,95:6,-1:25,95:6,-1:23,-9:21" +
		",24:10,-9:5,24:26,-9:3,24,-9,24:4,96,24:21,-9:24,24:10,-9:5,24:26,-9:3,24,-" +
		"9,24:11,97,24:14,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:4,98,24:21,-9:24,24:1" +
		"0,-9:5,24:26,-9:3,24,-9,24:4,99,24:21,-9:3,-14:21,24:10,-14:5,24:26,-14:3,2" +
		"4,-14,24:26,-14:3,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:11,100,24:14,-9:24,2" +
		"4:10,-9:5,24:26,-9:3,24,-9,24:18,101,24:7,-9:24,24:10,-9:5,24:26,-9:3,24,-9" +
		",24:4,102,24:21,-9:3,-18:21,24:10,-18:5,24:26,-18:3,24,-18,24:4,103,24:21,-" +
		"18:3,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:15,104,24:10,-9:3,-24:21,24:10,-2" +
		"4:5,24:26,-24:3,24,-24,24:26,-24:3,-25:21,24:10,-25:5,24:26,-25:3,24,-25,24" +
		":26,-25:3,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:11,105,24:14,-9:24,24:10,-9:" +
		"5,24:26,-9:3,24,-9,24:19,106,24:6,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:15,1" +
		"07,24:10,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:18,108,24:7,-9:24,24:10,-9:5," +
		"24:26,-9:3,24,-9,24:4,109,24:21,-9:3,-5:21,93:10,-5:65,-1,9:2,-1,9:8,47,9:5" +
		"0,48,9:32,-1,9:2,-1,9:8,47,9:8,95:10,9:5,95:6,9:21,48,9:3,95:6,9:23,-9:21,2" +
		"4:10,-9:5,24:26,-9:3,24,-9,24:17,110,24:8,-9:3,-12:21,24:10,-12:5,24:26,-12" +
		":3,24,-12,24:26,-12:3,-13:21,24:10,-13:5,24:26,-13:3,24,-13,24:26,-13:3,-15" +
		":21,24:10,-15:5,24:26,-15:3,24,-15,24:26,-15:3,-16:21,24:10,-16:5,24:26,-16" +
		":3,24,-16,24:26,-16:3,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:4,111,24:21,-9:3" +
		",-19:21,24:10,-19:5,24:26,-19:3,24,-19,24:26,-19:3,-9:21,24:10,-9:5,24:26,-" +
		"9:3,24,-9,112,24:25,-9:3,-21:21,24:10,-21:5,24:26,-21:3,24,-21,24:26,-21:3," +
		"-26:21,24:10,-26:5,24:26,-26:3,24,-26,24:26,-26:3,-9:21,24:10,-9:5,24:26,-9" +
		":3,24,-9,24:2,113,24:23,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:11,114,24:14,-" +
		"9:3,-30:21,24:10,-30:5,24:26,-30:3,24,-30,24:26,-30:3,-29:21,24:10,-29:5,24" +
		":26,-29:3,24,-29,24:26,-29:3,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:19,115,24" +
		":6,-9:3,-17:21,24:10,-17:5,24:26,-17:3,24,-17,24:26,-17:3,-9:21,24:10,-9:5," +
		"24:26,-9:3,24,-9,24:2,116,24:23,-9:24,24:10,-9:5,24:26,-9:3,24,-9,24:7,117," +
		"24:18,-9:24,24:10,-9:5,24:26,-9:3,24,-9,118,24:25,-9:3,-31:21,24:10,-31:5,2" +
		"4:26,-31:3,24,-31,24:26,-31:3,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:7,119,24" +
		":18,-9:3,-27:21,24:10,-27:5,24:26,-27:3,24,-27,24:26,-27:3,-9:21,24:10,-9:5" +
		",24:26,-9:3,24,-9,24:19,120,24:6,-9:3,-20:21,24:10,-20:5,24:26,-20:3,24,-20" +
		",24:26,-20:3,-9:21,24:10,-9:5,24:26,-9:3,24,-9,24:4,121,24:21,-9:3,-28:21,2" +
		"4:10,-28:5,24:26,-28:3,24,-28,24:26,-28:3");
			
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;
		
		int commaIndex;
		String workString;
		
		int res[][] = new int[size1][size2];
		for (int i = 0; i < size1; i++) {
			for (int j = 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex == -1) ? st : st.substring(0, commaIndex);
				st = st.substring(commaIndex + 1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j] = Integer.parseInt(workString);
					continue;
				}
				lengthString = workString.substring(colonIndex + 1);
				sequenceLength = Integer.parseInt(lengthString);
				workString = workString.substring(0, colonIndex);
				sequenceInteger = Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}

	public String current() throws UnsupportedEncodingException {
		return new String(token,0,len,encoding);
	}

	public LapgSymbol next() throws IOException, UnsupportedEncodingException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {			
			lapg_n.pos = new LapgPlace( lapg_current_line, lapg_current_offset );
			for( len = 0, state = group; state >= 0; ) {
				if( len < 2047 ) token[len++] = (byte)chr;
				state = lapg_lexem[state][lapg_char2no[(chr+256)%256]];
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

		} while(!createToken(lapg_n));
		return lapg_n;
	}

	private boolean createToken(LapgSymbol lapg_n) throws UnsupportedEncodingException {
		switch( lapg_n.lexem ) {
			case 3:
				 lapg_n.sym = new String(token,1,len-1); break; 
			case 4:
				 lapg_n.sym = Integer.parseInt(new String(token,1,len-1)); break; 
			case 5:
				 group = 1; break; 
			case 7:
				 lapg_n.sym = new String(token,0,len); break; 
			case 8:
				 lapg_n.sym = Integer.parseInt(new String(token,0,len)); break; 
			case 9:
				 lapg_n.sym = new String(token,1,len-2); break; 
			case 30:
				 group = 0; break; 
			case 31:
				 group = 0; break; 
			case 57:
				 return false; 
		}
		return true;
	} 
}
