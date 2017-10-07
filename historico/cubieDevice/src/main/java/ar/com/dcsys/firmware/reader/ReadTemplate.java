package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.template.ReadRawTemplate;
import ar.com.dcsys.firmware.cmd.template.ReadRawTemplate.ReadRawTemplateResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class ReadTemplate implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "readTemplate";
	
	private final ReadRawTemplate readRawTemplate;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public ReadTemplate(Firmware app, Leds leds, SerialDevice sd, ReadRawTemplate readRawTemplate) {

		this.readRawTemplate = readRawTemplate;
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
		
		try {
			String pnumber = cmd.substring(CMD.length() + 1);
			final int number = Integer.parseInt(pnumber);
			
			readRawTemplate.execute(sd, number, new ReadRawTemplateResult() {
				@Override
				public void onSuccess(byte[] templ) {
					
					String etempl = DatatypeConverter.printBase64Binary(templ);
					try {
						remote.sendText("OK " + etempl);
					
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				}
			
				@Override
				public void onInvalidTemplateNumber(int number) {
					try {
						remote.sendText("ERROR invalid template number " + String.valueOf(number));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}							
				}
				
				@Override
				public void onFailure(int errorCode) {
					try {
						remote.sendText("ERROR code " + String.valueOf(errorCode));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}							
				}
				
				@Override
				public void onEmptyTemplate(int number) {
					try {
						remote.sendText("ERROR empty template " + String.valueOf(number));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}							
				}
				
				/*
				@Override
				public void onCancel() {
					try {
						remote.sendText("ERROR comando cancelado");
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}							
				}
				*/
			});
		} catch (CmdException e) {
			logger.log(Level.SEVERE, e.getMessage(),e);
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		}
		 
	}
	
}
