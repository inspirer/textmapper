package net.sf.lapg.gen;

import net.sf.lapg.api.ProcessingStatus;

public class ProcessingStatusAdapter implements ProcessingStatus {

	private final INotifier notifier;
	private final int debuglev;

	public ProcessingStatusAdapter(INotifier notifier, int debuglev) {
		this.notifier = notifier;
		this.debuglev = debuglev;
	}

	public void debug(String info) {
		notifier.debug(info);
	}

	public void warn(String warning) {
		notifier.warn(warning);
	}

	public void error(String error) {
		notifier.error(error);
	}

	public boolean isDebugMode() {
		return debuglev >= 2;
	}

	public boolean isAnalysisMode() {
		return debuglev >= 1;
	}
}
