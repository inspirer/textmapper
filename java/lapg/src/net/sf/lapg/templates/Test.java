package net.sf.lapg.templates;

public class Test {
	public static void main(String[] args) {
		ITemplate[] templ = TemplateService.loadTemplatesFromFile("C:\\projects\\sf\\lapg_java\\lapg\\src\\net\\sf\\lapg\\templates\\test.ltp");
		System.out.println(templ[0].apply(templ, System.err));
	}
}
