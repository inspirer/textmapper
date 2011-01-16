package org.textway.lapg.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IResourceDelta;

public class FileUtil {

	public static String getStreamContents(InputStream stream, String charset) throws IOException {
		StringBuilder contents = new StringBuilder();
		char[] buf = new char[4096];
		InputStreamReader reader = null;

		try {
			reader = charset == null ? new InputStreamReader(stream) : new InputStreamReader(stream, charset);

			int read;
			while ((read = reader.read(buf)) > 0) {
				contents.append(buf, 0, read);
			}
			return contents.toString();
		} finally {
			try {
				if(reader != null) {
					reader.close();
				} else {
					stream.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public static boolean affectsFile(IResourceDelta fileDelta) {
		if ((fileDelta.getKind() & (IResourceDelta.ADDED | IResourceDelta.REMOVED)) > 0) {
			return true;
		}
		if ((fileDelta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.ENCODING | IResourceDelta.SYNC
				| IResourceDelta.TYPE | IResourceDelta.REPLACED)) > 0) {
			return true;
		}
		return false;
	}
}
