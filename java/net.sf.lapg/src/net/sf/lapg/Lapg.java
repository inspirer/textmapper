/*************************************************************
 * Copyright (c) 2002-2008 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 *************************************************************/
package net.sf.lapg;

import java.io.File;

import net.sf.lapg.gen.ConsoleGenerator;
import net.sf.lapg.gen.LapgOptions;

/**
 * @author inspirer
 * Main console entry point for Lapg engine.
 */
public class Lapg {

	public static final String VERSION = "1.4.0/java";
	public static final String BUILD = "2008";
	public static final String DEFAULT_FILE = "syntax";

	public static final String HELP_MESSAGE =
		"lapg - Lexical analyzer and parser generator\n"+
		"usage: lapg [OPTIONS]... [inputfile]\n"+
		"       lapg [-h|-v]\n"+
		"\n"+
		"Options:\n"+
		LapgOptions.HELP_OPTIONS+
		"\n"+
		"Operations:\n"+
		"  -h,  --help                    display this help\n"+
		"  -v,  --version                 version information\n"+
		"\n"+
		"Defaults:\n"+
		"  inputfile = "+DEFAULT_FILE+"\n";

	public static final String VERSION_MESSAGE =
		"lapg v" + VERSION + " build " + BUILD + "\n" +
		"Evgeny Gryaznov, 2002-08, egryaznov@gmail.com\n";


	public static void main(String[] args) {
		if (args.length >= 1 && args[0] != null) {
			if (args[0].equals("-h") || args[0].equals("--help")) {
				System.out.println(HELP_MESSAGE);
				return;
			}
			if (args[0].equals("-v") || args[0].equals("--version")) {
				System.out.println(VERSION_MESSAGE);
				return;
			}
		}

		if (args.length == 0 && !new File(DEFAULT_FILE).exists()) {
			System.err.println("lapg: file not found: " + DEFAULT_FILE);
			System.out.println(HELP_MESSAGE);
			System.exit(1);
			return;
		}

		LapgOptions options = LapgOptions.parseArguments(args);
		if (options == null) {
			System.err.println("Try 'lapg --help' for more information.");
			System.exit(1);
			return;
		}

		ConsoleGenerator cg = new ConsoleGenerator(options);
		if(!cg.compileGrammar()) {
			System.exit(1);
		}
	}
}
