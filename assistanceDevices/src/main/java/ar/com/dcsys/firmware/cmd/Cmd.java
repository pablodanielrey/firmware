package ar.com.dcsys.firmware.cmd;

public interface Cmd {
	public void terminate() throws CmdException;
}
