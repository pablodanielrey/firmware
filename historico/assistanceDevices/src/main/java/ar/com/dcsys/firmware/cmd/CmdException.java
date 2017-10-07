package ar.com.dcsys.firmware.cmd;

public class CmdException extends Exception {
	private static final long serialVersionUID = 1L;

	public CmdException(Throwable e) {
		super(e);
	}
	
	public CmdException() {
		super();
	}
	
	public CmdException(String cmd) {
		super(cmd);
	}
}
