package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.template.TemplateData;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate;
import ar.com.dcsys.firmware.cmd.template.WriteTemplateResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class WriteRawTemplate implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "writeRawTemplate";
	
	private final WriteTemplate writeTemplate;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public WriteRawTemplate(Firmware app, Leds leds, SerialDevice sd, WriteTemplate writeTemplate) {

		this.writeTemplate = writeTemplate;
		this.sd = sd;
		this.leds = leds;
		this.app = app;
		
	}
	
	@Override
	public String getCommand() {
		return CMD;
	}

	public void execute(TemplateData td, WriteTemplateResult result) throws CmdException {
		writeTemplate.execute(sd, result, td);
	}
	
	
	@Override
	public boolean identify(String cmd) {
		if (cmd.startsWith(CMD)) {
			this.cmd = cmd;
			return true;
		} else {
			return false;
		}
	}
	
	
	@Override
	public void setResponse(Response remote) {
		this.remote = remote;
	}
	
	@Override
	public void cancel() {
		
	}		
	
	private String cmd;
	private Response remote;	
	
	
	@Override
	public void execute() {

		// por ahora nada. no lo voy a ejecutar desde el websocket.
		try {
			remote.sendText("ERROR not implemented");
		} catch (IOException e) {
		}
		
		
		/*
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
					leds.onCommand(Leds.SUB_ERROR);
					
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
*/
	}	
	
	
}
