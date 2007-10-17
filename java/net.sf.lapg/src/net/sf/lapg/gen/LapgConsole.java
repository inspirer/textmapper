package net.sf.lapg.gen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.sf.lapg.Lapg;

public class LapgConsole {

	private static final String HELP_MESSAGE =
		"lapg - Lexical analyzer and parser generator\n"+
		"usage: lapg [OPTIONS]... [inputfile [outputfile]]\n"+
		"\n"+
		"LALR Generator:\n"+
		"  -d,  --debug                   debug info\n"+
		"  -e,  --extended-debug          extended debug info\n"+
		"\n"+
		"Operations:\n"+
		"  -h,  --help                    display this help\n"+
		"  -v,  --version                 version information\n"+
		"  -tf, --template-from-file      template for file parsing\n"+
		"  -ts, --template-from-string    template for string parsing\n"+
		"  -vb, --verbose                 output generator template\n"+
		"  -l name, --lang=name           language (for tf/ts/vb), supported: c, c++, cs, js, java\n"+
		"  -g file, --gen=filename        use generator template\n"+
		"\n"+
		"Defaults:\n"+
		"  inputfile = syntax\n"+
		"  outputfile = parse.cpp/cs/java/js...\n";

	private static final int ACT_COMPILE = 0;
	private static final int ACT_HELP = 1;
	private static final int ACT_VERSION = 2;
	private static final int ACT_TEMPLATE_FILE = 3;
	private static final int ACT_TEMPLATE_STRING = 4;
	private static final int ACT_VERBOSE = 5;

	private static final int DEBUG_AMBIG = 1;
	private static final int DEBUG_TABLES = 2;

	private int action;
	private int debug;
	private String input, output, template;
	private TargetLanguage lang;

	public LapgConsole() {
		debug = 0;
		action = ACT_COMPILE;
		input = "syntax";
		lang = null;
		output = null;
		template = null;
	}

	private static boolean check( String s, String v1, String v2) {
		return s != null && ( s.equals(v1) || s.equals(v2) );
	}

	public boolean parseArguments( String[] args ) {
		for( int i = 0, e = 0; i < args.length; i++ ) {

			if( check( args[i], "-d", "--debug" ) ) {
				debug = DEBUG_AMBIG;

			} else if( check( args[i], "-e", "--extended-debug" ) ) {
				debug = DEBUG_TABLES;

			} else if( check( args[i], "-h","--help" ) ) {
				action = ACT_HELP;
				return true;

			} else if( check( args[i], "-v","--version" ) ) {
				action = ACT_VERSION;
				return true;

			} else if( check( args[i], "-tf","--template-from-file" ) ) {
				action = ACT_TEMPLATE_FILE;

			} else if( check( args[i], "-ts","--template-from-string" ) ) {
				action = ACT_TEMPLATE_STRING;

			} else if( check( args[i], "-vb","--verbose" ) ) {
				action = ACT_VERBOSE;

			} else if( (args[i].equals("-l") && i+1 < args.length) || args[i].startsWith("--lang=") ) {
				String lng;

				if( args[i].startsWith("--") ) {
					lng = args[i].substring(7);
				} else {
					lng = args[++i];
				}

				lang = SourceBuilder.getLanguage(lng);
				if( lang == null ) {
					System.err.println( "lapg: unknown language: " + lng );
					return false;
				}

			} else if( (args[i].equals("-g") && i+1 < args.length) || args[i].startsWith("--gen=") ) {
				if( args[i].startsWith("--") ) {
					template = args[i].substring(6);
				} else {
					template = args[++i];
				}

			} else if( args[i].startsWith("-") && args[i].length() > 1 ) {
				System.err.println( "lapg: invalid option " + args[i]);
				System.err.println( "Try 'lapg --help' for more information.");
				return false;

			} else {
				switch( e++ ){
					case 0: input = args[i]; break;
					case 1: output = args[i]; break;
				}
			}
		}

		return true;
	}

	private void compileGrammar() {
		SourceBuilder p = new SourceBuilder(lang, debug);

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

		new File(ErrorImpl.OUT_ERRORS).delete();
		new File(ErrorImpl.OUT_TABLES).delete();
		p.process(sourceName, stream, output);
	}

	private void showHelp() {
		System.out.print(HELP_MESSAGE);
	}

	private void showVersion() {
		System.out.print(
				"lapg v" + Lapg.VERSION + " build " + Lapg.BUILD + "\n" +
				"Evgeny Gryaznov, 2002-07, inspirer@inbox.ru\n" );
	}

	private void showFileTemplate() {
		//g.TemplateFromFile();
	}

	private void showStringTemplate() {
		//g.TemplateFromString();
	}

	private void showScript() {
		//g.printScript()
	}

	public void perform() {
		switch( action ) {
		case ACT_COMPILE:
			compileGrammar();
			break;
		case ACT_HELP:
			showHelp();
			break;
		case ACT_VERSION:
			showVersion();
			break;
		case ACT_TEMPLATE_FILE:
			showFileTemplate();
			break;
		case ACT_TEMPLATE_STRING:
			showStringTemplate();
			break;
		case ACT_VERBOSE:
			showScript();
			break;
		}
	}
}
