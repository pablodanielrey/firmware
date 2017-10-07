package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.FpCancel.FpCancelResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class FpCancel implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "cancel";
	
	private final ar.com.dcsys.firmware.cmd.FpCancel fpCancel;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public FpCancel(Firmware app, Leds leds, SerialDevice sd, ar.com.dcsys.firmware.cmd.FpCancel fpCancel) {

		this.fpCancel = fpCancel;
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
			fpCancel.execute(sd, new FpCancelResult() {
				
				@Override
				public void onSuccess() {
					try {
						remote.sendText("OK cancel ok");
						
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
