package net.sf.lapg.lalr;

public interface IError {
	void error( int code, String error);
	void error( int code, String error, Object[] args );
}
