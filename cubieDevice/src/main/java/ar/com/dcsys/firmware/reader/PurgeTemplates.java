package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.template.ClearAllTemplate;
import ar.com.dcsys.firmware.cmd.template.ClearAllTemplate.ClearAllTemplateResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class PurgeTemplates implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "purgeTemplates";
	
	private final ClearAllTemplate clearAllTemplates;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public PurgeTemplates(Firmware app, Leds leds, SerialDevice sd, ClearAllTemplate clearAllTemplates) {

		this.clearAllTemplates = clearAllTemplates;
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

	
	public void execute(ClearAllTemplateResult result) throws CmdException {
		clearAllTemplates.excecute(sd, result);
	}
	
	
	@Override
	public void execute(String cmd, final Response remote) {

		try {
			ClearAllTemplateResult result = new ClearAllTemplateResult() {
				
				@Override
				public void onSuccess(int number) {
					leds.onCommand(Leds.OK);
					
					try {
						remote.sendText("OK " + String.valueOf(number));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					}
				}
				
				@Override
				public void onFailure(int errorCode) {
					leds.onCommand(Leds.ERROR);
					
					try {
						remote.sendText("ERROR " + String.valueOf(errorCode));
						
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
			};
			execute(result);
			
		} catch (CmdException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			leds.onCommand(Leds.ERROR);
		}

	}	
	
}
