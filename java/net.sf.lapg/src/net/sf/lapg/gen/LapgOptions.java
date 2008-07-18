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
package net.sf.lapg.gen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.lapg.Lapg;


public class LapgOptions {

	public static final int DEBUG_AMBIG = 1;
	public static final int DEBUG_TABLES = 2;

	private int debug;
	private String input;
	private String outputFolder;
	private String templateName;
	private List<String> includeFolders;
	private boolean useDefaultTemplates;
	private Map<String,String> templateOptions;

	public LapgOptions() {
		this.debug = 0;
		this.input = Lapg.DEFAULT_FILE;
		this.outputFolder = null;
		this.templateName = "java";
		this.includeFolders = new LinkedList<String>();
		this.useDefaultTemplates = true;
		this.templateOptions = new HashMap<String,String>();
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public int getDebug() {
		return debug;
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}

	public boolean isUseDefaultTemplates() {
		return useDefaultTemplates;
	}

	public void setUseDefaultTemplates(boolean useDefaultTemplates) {
		this.useDefaultTemplates = useDefaultTemplates;
	}

	public Map<String, String> getAdditionalOptions() {
		return templateOptions;
	}

	public List<String> getIncludeFolders() {
		return includeFolders;
	}

	static final int HAS_VALUE = 32;
	static final int MULTI_VALUE = 64;

	static final int OPT_DEBUG = 1;
	static final int OPT_EXT_DEBUG = 2;
	static final int OPT_NO_DEF = 3;
	static final int OPT_OUTPUT = 4 | HAS_VALUE;
	static final int OPT_INCLUDE = 5 | HAS_VALUE | MULTI_VALUE;
	static final int OPT_TEMPLATE = 6 | HAS_VALUE;
	static final int OPT_INPUT = 7 | HAS_VALUE;

	public static final String HELP_OPTIONS =
		"  -d,  --debug                   debug info\n"+
		"  -e,  --extended-debug          extended debug info\n"+
		"  -x,  --no-default-templates    removes default templates from engine\n"+
		"  -o folder, --output=folder     target folder\n"+
		"  -i folder, --include=folder    adds folder (or semicolon separated folder list) to the lapg.templates stack\n"+
		"  -t templateId, --template=id   use template for generation\n"+
		"  key=val                        any generation option\n";

	private static Map<String, Integer> buildOptionsHash() {
		HashMap<String, Integer> res = new HashMap<String, Integer>();
		res.put("d", OPT_DEBUG);
		res.put("-debug", OPT_DEBUG);
		res.put("e", OPT_EXT_DEBUG);
		res.put("-extended-debug", OPT_EXT_DEBUG);
		res.put("x", OPT_NO_DEF);
		res.put("-no-default-templates", OPT_NO_DEF);
		res.put("o", OPT_OUTPUT);
		res.put("-output", OPT_OUTPUT);
		res.put("i", OPT_INCLUDE);
		res.put("-include", OPT_INCLUDE);
		res.put("t", OPT_TEMPLATE);
		res.put("-template", OPT_TEMPLATE);
		return res;
	}

	public static LapgOptions parseArguments(String[] args) {
		LapgOptions opts = new LapgOptions();
		Map<String, Integer> optionsHash = buildOptionsHash();
		Set<Integer> usedOptions = new HashSet<Integer>();

		for (int i = 0; i < args.length; i++) {
			int equalIndex = args[i].indexOf('=');
			if (args[i].length() > 1 && args[i].charAt(0) == '-') {
				String option = equalIndex >= 0 ? args[i].substring(1, equalIndex) : args[i].substring(1);
				int optionId = optionsHash.containsKey(option) ? optionsHash.get(option) : 0;
				boolean hasValue = (optionId & HAS_VALUE) != 0;
				if (optionId == 0 || equalIndex >= 0 && !hasValue) {
					System.err.println("lapg: invalid option " + args[i]);
					return null;
				}
				if (hasValue && (equalIndex < 0 && i+1 == args.length || equalIndex+1 == args[i].length())) {
					System.err.println("lapg: no value for option " + args[i]);
					return null;
				}
				if(usedOptions.contains(optionId)) {
					System.err.println("lapg: option cannot be used twice " + args[i]);
					return null;
				}
				if((optionId & MULTI_VALUE) == 0) {
					usedOptions.add(optionId);
				}
				setOption(opts, optionId, hasValue ? (equalIndex >= 0 ? args[i].substring(equalIndex + 1) : args[++i]) : null);

			} else if (equalIndex >= 0) {
				String key = args[i].substring(0, equalIndex);
				String value = args[i].substring(equalIndex + 1);
				if (opts.getAdditionalOptions().containsKey(key)) {
					System.err.println("lapg: key is used twice: " + key);
					return null;
				}
				opts.getAdditionalOptions().put(key, value);

			} else {
				if(usedOptions.contains(OPT_INPUT)) {
					System.err.println("lapg: should be only one input in arguments");
					return null;
				}
				usedOptions.add(OPT_INPUT);
				opts.setInput(args[i]);
			}
		}
		return opts;
	}

	private static void setOption(LapgOptions opts, int optionId, String value) {
		switch(optionId) {
		case OPT_DEBUG:
			opts.setDebug(LapgOptions.DEBUG_AMBIG);
			break;
		case OPT_EXT_DEBUG:
			opts.setDebug(LapgOptions.DEBUG_TABLES);
			break;
		case OPT_NO_DEF:
			opts.setUseDefaultTemplates(false);
			break;
		case OPT_INCLUDE:
			for(String s : value.split(";")) {
				if(s.trim().length() > 0) {
					opts.getIncludeFolders().add(s);
				}
			}
			break;
		case OPT_TEMPLATE:
			opts.setTemplateName(value);
			break;
		case OPT_OUTPUT:
			opts.setOutputFolder(value);
			break;
		}
	}
}
