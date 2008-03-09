package net.sf.lapg;

import net.sf.lapg.gen.LapgConsole;


/**
 *  Main entry point for Lapg engine.
 */
public class Lapg {

	public static final String VERSION = "1.4.0/java";
	public static final String BUILD = "2008";

	public static void main(String[] args) {
		LapgConsole con = new LapgConsole();
		if( !con.parseArguments(args) ) {
			System.exit(1);
			return;
		}

		con.perform();
	}
}
