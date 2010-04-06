package net.sf.lapg.api;

public interface ProcessingStatus {

	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	//void report(int kind, String message);

	void error(String error);

	void error(SourceElement element, String error);

	void warn(String warning);

	void debug(String info);

	boolean isDebugMode();

	boolean isAnalysisMode();
}
