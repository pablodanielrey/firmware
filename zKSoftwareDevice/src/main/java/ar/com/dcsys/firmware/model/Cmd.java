package ar.com.dcsys.firmware.model;

import ar.com.dcsys.firmware.cmd.CmdException;

public interface Cmd {

	public String getCommand();
	public boolean identify(String cmd);
	public void setResponse(Response remote);
	public void execute();
	public void cancel() throws CmdException;
	
}
