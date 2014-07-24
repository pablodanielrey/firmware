package ar.com.dcsys.firmware.database;

public class FingerprintMappingException extends Exception {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FingerprintMappingException(String m) {
		super(m);
	}
	
	public FingerprintMappingException(Throwable t) {
		super(t);
	}
}
