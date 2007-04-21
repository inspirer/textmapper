package net.sf.lapg.lalr;

public interface IError {

	void error(String error);
	void warn(String warning);
	void debug(String info);
}
