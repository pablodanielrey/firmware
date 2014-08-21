package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId.GetEmptyIdResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class GetEmptyId implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "getEmptyId";
	
	private final ar.com.dcsys.firmware.cmd.template.GetEmptyId getEmptyId;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public GetEmptyId(Firmware app, Leds leds, SerialDevice sd, ar.com.dcsys.firmware.cmd.template.GetEmptyId getEmptyId) {

		this.getEmptyId = getEmptyId;
		this.sd = sd;
		this.leds = leds;
		this.app = app;
		
	}
	
	@Override
	public String getCommand() {
		return CMD;
	}

	@Override
	public boolean identify(String cmd) {
		return (CMD.equals(cmd));
	}

	
	public void execute(GetEmptyIdResult result) throws CmdException {
		getEmptyId.execute(sd, result);
	}
	
	
	@Override
	public void execute(String cmd, final Response remote) {

		try {
			GetEmptyIdResult geir = new GetEmptyIdResult() {
				
				@Override
				public void onSuccess(int tmplNumber) {
					leds.onCommand(Leds.SUB_OK);
					
					try {
						remote.sendText("OK " + String.valueOf(tmplNumber));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					}
				}
				
				@Override
				public void onCancel() {
					leds.onCommand(Leds.READY);
					
					try {
						remote.sendText("ERROR cancel");
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					}
				}
				
				
				@Override
				public void onEmptyNotExistent() {
					leds.onCommand(Leds.SUB_OK);
					
					try {
						remote.sendText("OK -1");
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					}
				}
				
				public void onFailure(int errorCode) {
					leds.onCommand(Leds.SUB_ERROR);
					
					try {
						remote.sendText("ERROR " + String.valueOf(errorCode));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					}
				};
			};
			execute(geir);
			
		} catch (CmdException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			leds.onCommand(Leds.SUB_ERROR);
		}

	}	
	
}
