package net.sf.lapg.templates;

import java.io.PrintStream;

public interface ITemplate {
	String getName();
	String apply(Object context, PrintStream errors);
}
