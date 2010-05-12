package net.sf.lapg.gen;

import net.sf.lapg.api.ParserConflict;
import net.sf.lapg.api.ProcessingStatus;
import net.sf.lapg.api.Rule;
import net.sf.lapg.api.SourceElement;

public class ProcessingStatusAdapter implements ProcessingStatus {

	private final INotifier notifier;
	private final int debuglev;

	public ProcessingStatusAdapter(INotifier notifier, int debuglev) {
		this.notifier = notifier;
		this.debuglev = debuglev;
	}

	public void report(int kind, String message, SourceElement ...anchors) {
		SourceElement anchor = anchors != null && anchors.length > 0 ? anchors[0] : null;
		switch(kind) {
		case KIND_FATAL:
		case KIND_ERROR:
			if(anchor != null && anchor.getResourceName() != null) {
				notifier.error(anchor.getResourceName() + "," + anchor.getLine() + ": ");
			}
			notifier.error(message + "\n");
			break;
		case KIND_WARN:
			if(anchor != null && anchor.getResourceName() != null) {
				notifier.warn(anchor.getResourceName() + "," + anchor.getLine() + ": ");
			}
			notifier.warn(message + "\n");
			break;
		}
	}

	public void report(ParserConflict conflict) {
		Rule rule = conflict.getRules()[0];
		if(conflict.getKind() == ParserConflict.FIXED) {
			if(isAnalysisMode()) {
				report(KIND_WARN, conflict.getText(), rule);
			}
		} else {
			report(KIND_ERROR, conflict.getText(), rule);
		}
	}

	public void debug(String info) {
		notifier.debug(info);
	}

	public boolean isDebugMode() {
		return debuglev >= 2;
	}

	public boolean isAnalysisMode() {
		return debuglev >= 1;
	}

	public void error(SourceElement element, String error) {
		notifier.error(error);
	}
}
