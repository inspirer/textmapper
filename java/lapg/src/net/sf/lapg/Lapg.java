package net.sf.lapg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.sf.lapg.gen.SourceBuilder;


public class Lapg {
	
	private static final String VERSION = "1.4.0";
	private static final String BUILD = "2007";
	
	private static final String help_message =
		"lapg - Lexical analyzer and parser generator\n"+
		"Evgeny Gryaznov, 2002-07, inspirer@inbox.ru\n"+
		"\n"+
		"usage: lapg [OPTIONS]... [inputfile [outputfile]]\n"+
		"\n"+
		"Generator:\n"+
		"  -d,  --debug                   debug info\n"+
		"  -e,  --extended-debug          extended debug info\n"+
		"\n"+
		"Operations:\n"+
		"  -h,  --help                    display this help\n"+
		"  -v,  --version                 output version information\n"+
		"  -tf, --template-from-file      generate template for file parsing\n"+
		"  -ts, --template-from-string    generate template for string parsing\n"+
		"  -vb, --verbose                 print output script\n"+
		"  -l name, --lang=name           language (for tf/ts/vb), supported: c++, cs, js, java\n"+
		"\n"+
		"Defaults:\n"+
		"  inputfile = syntax\n"+
		"  outputfile = parse.cpp/cs/java/js...\n";
	
	private static boolean check( String s, String v1, String v2) {
		return s != null && ( s.equals(v1) || s.equals(v2) );
	}

	public static void main(String[] args) {
		int debug = 0, action = 0;
		String input = "syntax", lang = null, output = null;

		for( int i = 0, e = 0; i < args.length; i++ )

			if( check( args[i], "-d", "--debug" ) ) {
				debug = 1;

			} else if( check( args[i], "-e", "--extended-debug" ) ) {
				debug = 2;

			} else if( check( args[i], "-h","--help" ) ) {
				System.out.println( help_message );
				return;

			} else if( check( args[i], "-v","--version" ) ) {
				System.out.print( 
					"lapg v" + VERSION + " build " + BUILD + "\n" +
					"Evgeny Gryaznov, 2002-07, inspirer@inbox.ru\n" );
				return;

			} else if( check( args[i], "-tf","--template-from-file" ) ) {
				action = 1;

			} else if( check( args[i], "-ts","--template-from-string" ) ) {
				action = 2;

			} else if( check( args[i], "-vb","--verbose" ) ) {
				action = 3;

			} else if( args[i].equals("-l") || args[i].startsWith("--lang=") ) {
//				String lng;
//				int e;
//
//				if( args[i].startsWith("--") )
//					lng = args[i].substring(7);
//				else
//					lng = args[++i];
//
//				for( e = 0; langs[e].lang; e++ )
//					if( !strcmp( langs[e].lang, lng ) )
//						break;
//
//				if( langs[e].lang ) {
//					lang = e;
//				} else {
//					System.err.println( "lapg: unknown language: " + lng );
//				}

			} else if( args[i].startsWith("-") && args[i].length() > 1 ) {
				System.err.println( "lapg: invalid option " + args[i]);
				System.err.println( "Try 'lapg --help' for more information.");
				System.exit(1);
				return;

			} else switch( e++ ){
				case 0: input = args[i]; break;
				case 1: output = args[i]; break;
			}

		SourceBuilder p = new SourceBuilder(lang, debug);

		switch( action ) {

			// run
			case 0:

				// open file
				InputStream stream;
				String sourceName; 
				if( input != null && !input.startsWith("-") ) {
					try {
						stream = new FileInputStream( input );
						sourceName = input;
					} catch( FileNotFoundException ex) {
						System.err.println( "lapg: file not found: " + input);
						System.exit(1);
						return;
					}
				} else {
					stream = System.in;
					sourceName = "<stdin>";
				}

				// save options
//				g.sourcename = (input != null && !input.equals("-")) ? input : "<stdin>";
//				g.targetname = output;

				//new File("errors").delete();
				//new File("states").delete();
				p.process(sourceName, stream);
				break;

			// --template-from-file
			case 1:
//				g.TemplateFromFile();
				break;

			// --template-from-string
			case 2:
//				g.TemplateFromString();
				break;

			// --verbose
			case 3:
//				g.printScript();
				break;
		}
	}
}
