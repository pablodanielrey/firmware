package ar.com.dcsys.firmware.soap;

public class ZkSoftwareException extends Exception {

	private static final long serialVersionUID = 1L;

	public ZkSoftwareException(String message) {
		super(message);
	}
	
	public ZkSoftwareException(Throwable cause) {
		super(cause);
	}
	
}
