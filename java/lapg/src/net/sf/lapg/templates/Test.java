package net.sf.lapg.templates;

import net.sf.lapg.templates.api.ITemplate;

public class Test {
	public static void main(String[] args) {
		ITemplate[] templ = TemplateService.loadTemplatesFromFile("C:\\projects\\sf\\lapg_java\\lapg\\src\\net\\sf\\lapg\\templates\\test.ltp");
		String res = TemplateService.executeTemplate(templ[0], templ, null);
		
		System.out.println(res);
	}
}
