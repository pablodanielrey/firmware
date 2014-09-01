package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.SensorLedControl;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class SetLed implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "setLed";
	
	private final SensorLedControl sensorLedControl;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public SetLed(Firmware app, Leds leds, SerialDevice sd, SensorLedControl sensorLedControl) {

		this.sensorLedControl = sensorLedControl;
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
			final String svalue = cmd.substring(CMD.length() + 1);
			boolean value = false;
			if ("on".equalsIgnoreCase(svalue)) {
				value = true;
			}
			final boolean finalValue = value;
			

			sensorLedControl.execute(sd, finalValue, new SensorLedControl.SensorLedControlResult() {
					@Override
					public void onSuccess() {
						try {
							remote.sendText("OK Led " + svalue);
						} catch (IOException e) {
							e.printStackTrace();
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
					}
					@Override
					public void onFailure() {
						try {
							remote.sendText("ERROR");
						} catch (IOException e) {
							e.printStackTrace();
							logger.log(Level.SEVERE,e.getMessage(),e);
						}						
					}
				});
			
			} catch (CmdException e) {
				e.printStackTrace();
				try {
					remote.sendText("ERROR " + e.getMessage());
				} catch (IOException e1) {
					e1.printStackTrace();
					logger.log(Level.SEVERE,e1.getMessage(),e1);
				}					
			}					
		}
	
}
