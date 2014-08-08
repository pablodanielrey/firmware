package ar.com.dcsys.firmware.model;

public interface Cmd {

	public String getCommand();
	public boolean identify(String cmd);
	public void execute(String cmd, Response remote);
	
}
