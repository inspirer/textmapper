package net.sf.lapg.api;

import net.sf.lapg.ParserConflict;

public interface ProcessingStatus {

	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;
	public static final int KIND_ANALYSIS = 3;

	void report(int kind, String message, SourceElement ...anchors);

	void report(ParserConflict conflict);

	void error(String error);

	void warn(String warning);

	void debug(String info);

	boolean isDebugMode();

	boolean isAnalysisMode();
}
