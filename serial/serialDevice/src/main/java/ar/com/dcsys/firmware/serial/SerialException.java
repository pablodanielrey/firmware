package ar.com.dcsys.firmware.serial;

public class SerialException extends Exception {

	private static final long serialVersionUID = 1L;

	public SerialException() {
		super();
	}
	
	public SerialException(String msg) {
		super(msg);
	}
	
	public SerialException(Throwable t) {
		super(t);
	}
	
}
