package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.GetFirmwareVersion;
import ar.com.dcsys.firmware.cmd.GetFirmwareVersion.GetFirmwareVersionResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class GetVersion implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "getVersion";
	
	private final GetFirmwareVersion getFirmwareVersion;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public GetVersion(Firmware app, Leds leds, SerialDevice sd, GetFirmwareVersion getFirmwareVersion) {

		this.getFirmwareVersion = getFirmwareVersion;
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
	public void execute(String cmd, final Response remote) {

		try {
			getFirmwareVersion.execute(sd, new GetFirmwareVersionResult() {
				@Override
				public void onSuccess(String version) {
					try {
						remote.sendText("OK " + version);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onFailure() {
					try {
						remote.sendText("ERROR");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (CmdException e) {
			e.printStackTrace();
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
