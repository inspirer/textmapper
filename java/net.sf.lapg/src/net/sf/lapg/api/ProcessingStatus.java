package net.sf.lapg.api;


public interface ProcessingStatus {

	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;
	public static final int KIND_INFO = 3;

	void report(int kind, String message, SourceElement ...anchors);

	void report(String message, Throwable th);

	void report(ParserConflict conflict);

	void debug(String info);

	boolean isDebugMode();

	boolean isAnalysisMode();
}
