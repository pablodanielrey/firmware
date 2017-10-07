package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;

public class RunningCmd implements Cmd {

	private static final Logger logger = Logger.getLogger(RunningCmd.class.getName());
	
	private final Firmware firmware;
	private Response response;
	
	@Inject
	public RunningCmd(Firmware firmware) {
		this.firmware = firmware;
	}
	
	@Override
	public String getCommand() {
		return "runningCmd";
	}

	@Override
	public boolean identify(String cmd) {
		return getCommand().equals(cmd);
	}

	@Override
	public void setResponse(Response remote) {
		this.response = remote;
	}

	@Override
	public void execute() {
		if (response != null && firmware != null) {
			try {
				Cmd cmd = firmware.getRunningCommand();
				if (cmd == null) {
					response.sendText("OK null");
				} else {
					String rc = cmd.getCommand();
					response.sendText("OK " + rc);
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(),e);
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				try {
					response.sendText("ERROR " + e.getMessage());
				} catch (IOException e1) {
					logger.log(Level.SEVERE,e1.getMessage(),e1);
				}
			}
		}
	}

	@Override
	public void cancel() throws CmdException {
		
	}

}
