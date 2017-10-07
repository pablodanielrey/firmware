package ar.com.dcsys.firmware.cmd;

public class ProcessingException extends Throwable {

	private static final long serialVersionUID = 1L;

	public ProcessingException(Throwable e) {
		super(e);
	}
	
	public ProcessingException() {
		super();
	}
	
	public ProcessingException(String cmd) {
		super(cmd);
	}	
	
}
