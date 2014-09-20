package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;

public class GenerateIdentify implements Cmd {

	private static final Logger logger = Logger.getLogger(GenerateIdentify.class.getName());

	private final Firmware firmware;
	private Response response;
	private String cmd;
	
	
	@Inject
	public GenerateIdentify(Firmware firmware) {
		this.firmware = firmware;
	}
	
	@Override
	public String getCommand() {
		return "generateIdentify";
	}

	@Override
	public boolean identify(String cmd) {
		if (cmd.startsWith(getCommand())) {
			this.cmd = cmd;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setResponse(Response remote) {
		this.response = remote;
	}

	@Override
	public void execute() {
		if (response != null && firmware != null) {
			try {
				String svalue = cmd.substring(getCommand().length() + 1);
				boolean value = false;
				if ("on".equalsIgnoreCase(svalue)) {
					value = true;
				}
				firmware.setGenerateIdentify(value);
				response.sendText("OK");
				
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
