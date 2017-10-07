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

public class ClearTemplates implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "clearTemplates";
	
	private final ClearAllTemplate clearAllTemplate;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public ClearTemplates(Firmware app, Leds leds, SerialDevice sd, ClearAllTemplate clearAllTemplate) {

		this.clearAllTemplate = clearAllTemplate;
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

		try {
			clearAllTemplate.excecute(sd, new ClearAllTemplateResult() {
				
				@Override
				public void onSuccess(int number) {
					try {
						leds.onCommand(Leds.OK);
						remote.sendText("OK " + String.valueOf(number));
						
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}

				}
				
				@Override
				public void onFailure(int errorCode) {
					try {
						leds.onCommand(Leds.ERROR);
						remote.sendText("ERROR " + String.valueOf(errorCode));
						
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}

				}
				
				@Override
				public void onCancel() {
					try {
						leds.onCommand(Leds.READY);
						remote.sendText("ERROR cancelado");
						
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}

				}
			});
			
		} catch (CmdException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			try {
				remote.sendText("ERROR " + e.getMessage());
				
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		}
	}	
	
}
