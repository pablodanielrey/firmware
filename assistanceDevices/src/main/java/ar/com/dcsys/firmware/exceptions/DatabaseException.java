package ar.com.dcsys.firmware.exceptions;

public class DatabaseException extends Exception {

	private static final long serialVersionUID = 1L;


	public DatabaseException() {
		super();
	}
	
	public DatabaseException(Throwable t) {
		super(t);
	}
	
	public DatabaseException(String t) {
		super(t);
	}
	
	
}
