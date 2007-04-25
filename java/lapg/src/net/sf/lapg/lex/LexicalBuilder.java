package net.sf.lapg.lex;

public class LexicalBuilder {

	private static final int BITS = 32;
	private static final int MAX_LEXEMS = 0x100000;
	private static final int MAX_ENTRIES = 1024;
	private static final int MAX_DEEP = 128;
	private static final int SIZE_SYM = (((256)+BITS-1)/BITS);
	private static final int HASH_SIZE = 1023;

}
