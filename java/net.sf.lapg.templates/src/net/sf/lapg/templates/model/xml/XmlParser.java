package net.sf.lapg.templates.model.xml;

import java.io.IOException;
import net.sf.lapg.templates.model.xml.XmlLexer.Lexems;
import net.sf.lapg.templates.model.xml.XmlLexer.LapgSymbol;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


public class XmlParser implements XmlLexer.ErrorReporter {
	
	public XmlParser() {
	}
	
	private static final boolean DEBUG_SYNTAX = false;
	
	private List<XmlElement> result;
	
	byte[] buff;
	
	private XmlData getData(int start, int end) {
		return new XmlData(buff, start, end-start);
	}
	
	public void error(String s) {
		System.err.println(s);
	}
	
	public List<XmlElement> parse(String s) {
		result = null;
		try {
			buff = s.getBytes("utf-8");
			XmlLexer lexer = new XmlLexer(new ByteArrayInputStream(buff), this, "utf-8");
			parse(lexer);
		} catch( IOException ex ) {
		}
		return result;
	}
	
	private void checkTag(XmlNode node, String endTag, int line) {
		if( !node.getTagName().equals(endTag) )
			error("Tag " + node.getTagName() + " is closed with " + endTag + " at line " + line);
	}
    private static final int lapg_action[] = {
		-1, 6, -1, -3, 2, -1, 5, -11, -21, 1, -1, -1, 3, -1, -1, -1,
		-29, 15, -1, 4, 8, -1, 11, -1, 14, -1, 16, 12, 13, -1, -2,
	};

    private static final short lapg_lalr[] = {
		1, -1, 2, -1, 0, 0, -1, -2, 8, -1, 4, 7, 6, 7, 9, 7,
		-1, -2, 4, -1, 6, 10, 9, 10, -1, -2, 4, -1, 6, 9, 9, 9,
		-1, -2,
	};

    private static final short lapg_sym_goto[] = {
		0, 1, 5, 9, 9, 15, 16, 19, 20, 21, 23, 23, 24, 26, 30, 34,
		36, 40, 43, 44, 45, 47,
	};

    private static final short lapg_sym_from[] = {
		29, 0, 3, 5, 11, 0, 3, 5, 11, 2, 8, 10, 13, 16, 18, 21,
		15, 23, 25, 14, 7, 10, 15, 0, 0, 5, 0, 3, 5, 11, 0, 3,
		5, 11, 5, 11, 0, 3, 5, 11, 2, 10, 18, 8, 8, 8, 16,
	};

    private static final short lapg_sym_to[] = {
		30, 1, 1, 1, 1, 2, 2, 10, 10, 7, 14, 7, 20, 14, 7, 26,
		22, 27, 28, 21, 13, 18, 23, 29, 3, 11, 4, 9, 4, 9, 5, 5,
		5, 5, 12, 19, 6, 6, 6, 6, 8, 8, 25, 15, 16, 17, 24,
	};

    private static final short lapg_rlen[] = {
		1, 2, 1, 2, 3, 1, 1, 1, 3, 1, 0, 4, 5, 4, 2, 1,
		3,
	};

    private static final short lapg_rlex[] = {
		11, 12, 12, 13, 13, 13, 13, 17, 17, 18, 18, 14, 16, 15, 19, 19,
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

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 11;
		public static final int xml_tags = 12;
		public static final int xml_tag_or_space = 13;
		public static final int tag_start = 14;
		public static final int tag_end = 15;
		public static final int no_body_tag = 16;
		public static final int tag_name = 17;
		public static final int attributesopt = 18;
		public static final int attributes = 19;
		public static final int attribute = 20;
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

	private int lapg_head;
	private LapgSymbol[] lapg_m;
	private LapgSymbol lapg_n;

	public boolean parse(XmlLexer lexer) throws IOException {

		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lexer.next(); 

		while( lapg_m[lapg_head].state != 30 ) {
			int lapg_i = lapg_next( lapg_m[lapg_head].state, lapg_n.lexem );

			if( lapg_i >= 0 ) {
				reduce(lapg_i);
			} else if( lapg_i == -1 ) {
				shift(lexer);
			}

			if( lapg_i == -2 || lapg_m[lapg_head].state == -1 ) {
				break;
			}
		}

		if( lapg_m[lapg_head].state != 30 ) {
			error( MessageFormat.format( "syntax error before line {0}", lapg_n.pos.line ) );
			return false;
		};
		return true;
	}

	private void shift(XmlLexer lexer) throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym( lapg_m[lapg_head-1].state, lapg_n.lexem );
		if( DEBUG_SYNTAX ) {
			System.out.println( MessageFormat.format( "shift: {0} ({1})", lapg_syms[lapg_n.lexem], lexer.current() ) );
		}
		if( lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0 ) {
			lapg_n = lexer.next();
		}
	}

	@SuppressWarnings("unchecked")
	private void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]].sym:null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if( DEBUG_SYNTAX ) {
			System.out.println( "reduce to " + lapg_syms[lapg_rlex[rule]] );
		}
		lapg_gg.pos = (lapg_rlen[rule]!=0)?lapg_m[lapg_head+1-lapg_rlen[rule]].pos:lapg_n.pos;
		lapg_gg.endpos = (lapg_rlen[rule]!=0)?lapg_m[lapg_head].endpos:lapg_n.pos;
		switch( rule ) {
			case 0:  // input ::= xml_tags
				 result = ((List<XmlElement>)lapg_m[lapg_head-0].sym); 
				break;
			case 1:  // xml_tags ::= xml_tags xml_tag_or_space
				 ((List<XmlElement>)lapg_gg.sym).add(((XmlElement)lapg_m[lapg_head-0].sym)); 
				break;
			case 2:  // xml_tags ::= xml_tag_or_space
				 lapg_gg.sym = new ArrayList<XmlElement>(); ((List<XmlElement>)lapg_gg.sym).add(((XmlElement)lapg_m[lapg_head-0].sym)); 
				break;
			case 3:  // xml_tag_or_space ::= tag_start tag_end
				 checkTag(((XmlNode)lapg_m[lapg_head-1].sym),((String)lapg_m[lapg_head-0].sym),lapg_m[lapg_head-0].pos.line); 
				break;
			case 4:  // xml_tag_or_space ::= tag_start xml_tags tag_end
				 checkTag(((XmlNode)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head-0].sym),lapg_m[lapg_head-0].pos.line); ((XmlNode)lapg_m[lapg_head-2].sym).setData(((List<XmlElement>)lapg_m[lapg_head-1].sym)); 
				break;
			case 6:  // xml_tag_or_space ::= any
				 lapg_gg.sym = getData(lapg_m[lapg_head-0].pos.offset,lapg_m[lapg_head-0].endpos.offset); 
				break;
			case 7:  // tag_name ::= identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 8:  // tag_name ::= identifier ':' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + ":" + ((String)lapg_m[lapg_head-0].sym); 
				break;
			case 11:  // tag_start ::= '<' tag_name attributesopt '>'
				 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-2].sym), ((List<XmlAttribute>)lapg_m[lapg_head-1].sym), lapg_m[lapg_head-3].pos.line); 
				break;
			case 12:  // no_body_tag ::= '<' tag_name attributesopt '/' '>'
				 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-3].sym), ((List<XmlAttribute>)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-4].pos.line); 
				break;
			case 13:  // tag_end ::= '<' '/' tag_name '>'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 14:  // attributes ::= attributes attribute
				 ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head-0].sym)); 
				break;
			case 15:  // attributes ::= attribute
				 lapg_gg.sym = new ArrayList<XmlAttribute>(); ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head-0].sym)); 
				break;
			case 16:  // attribute ::= identifier '=' ccon
				 lapg_gg.sym = new XmlAttribute(((String)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head-0].sym)); 
				break;
		}
		for( int e = lapg_rlen[rule]; e > 0; e-- ) { 
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
	}
}
