package org.textmapper.lapg.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.textmapper.lapg.gen.LapgOptions;
import org.textmapper.lapg.ui.settings.SettingsTree.TextSource;

public class SettingsPersister {

	public static Map<IPath, LapgOptions> load(String content) {
		SettingsTree<AstInput> tree = SettingsTree.parse(new TextSource(".lapg", content.toCharArray(), 1));
		if (tree.getRoot() == null) {
			return Collections.emptyMap();
		}
		Map<IPath, LapgOptions> result = new LinkedHashMap<IPath, LapgOptions>();
		for (AstSettings s : tree.getRoot().getSettingsList()) {
			IPath p = new Path(s.getScon());
			LapgOptions opts = new LapgOptions();
			for (AstOption o : s.getOptionsList()) {
				if (o.getIsVardef()) {
					opts.getAdditionalOptions().put(o.getIdentifier(), o.getScon());
				} else {
					String key = o.getIdentifier();
					if (key.equals("no-default-templates")) {
						opts.setUseDefaultTemplates(false);
					} else if(key.equals("include-folders")) {
						List<String> list = o.getStringList();
						if(list != null) {
							opts.getIncludeFolders().addAll(list);
						}
					} else if(key.equals("template")) {
						String val = o.getScon();
						if(val != null) {
							opts.setTemplateName(val);
						}
					}
				}
			}
			result.put(p, opts);
		}

		return result;
	}

	public static String serialize(Map<IPath, LapgOptions> settings) {
		StringBuilder sb = new StringBuilder();
		List<IPath> paths = new ArrayList<IPath>(settings.keySet());
		Collections.sort(paths, new Comparator<IPath>() {
			public int compare(IPath o1, IPath o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		for (IPath p : paths) {
			sb.append("[");
			sb.append(p.toString());
			sb.append("]\n");
			LapgOptions opts = settings.get(p);
			serialize(opts, sb);
			sb.append("\n");
		}
		return sb.toString();
	}

	private static void serialize(LapgOptions opts, StringBuilder sb) {
		if (!opts.isUseDefaultTemplates()) {
			sb.append("no-default-templates\n");
		}
		if (!opts.getIncludeFolders().isEmpty()) {
			serialize("include-folders", opts.getIncludeFolders(), sb);
		}
		if(opts.getTemplateName() != null) {
			sb.append("template = ");
			serialize(opts.getTemplateName(), sb);
		}
		if(!opts.getAdditionalOptions().isEmpty()) {
			// TODO
		}

	}
	
	private static void serialize(String title, List<String> list, StringBuilder sb) {
		sb.append(title);
		sb.append(" = (");
		boolean first = true;
		for (String s : list) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			serialize(s, sb);
		}
		sb.append(")\n");
	}

	private static void serialize(String s, StringBuilder sb) {
		sb.append("\"");
		for (char c : s.toCharArray()) {
			switch (c) {
			case '\n':
				sb.append("\\n");
				continue;
			case '\r':
				sb.append("\\r");
				continue;
			case '\t':
				sb.append("\\t");
				continue;
			}
			if (c < 0x20 || c >= 128) {
				sb.append("\\x");
				String number = Integer.toString(c, 16);
				if(number.length() < 4) {
					sb.append("0000".substring(number.length()));
				}
				sb.append(number);
			}
			if (c == '"') {
				sb.append("\\");
			}
			sb.append(c);
		}
		sb.append("\"");
	}
}
