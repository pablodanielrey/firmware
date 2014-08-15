package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.TestConnection;
import ar.com.dcsys.firmware.cmd.TestConnection.TestConnectionResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class Test implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "test";
	
	private final TestConnection testConnection;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public Test(Firmware app, Leds leds, SerialDevice sd, TestConnection testConnection) {

		this.testConnection = testConnection;
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
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					testConnection.execute(sd, new TestConnectionResult() {
						@Override
						public void onSuccess() {
							try {
								remote.sendText("OK Test de conección exitoso");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure() {
							try {
								remote.sendText("ERROR error ejecutando test");
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
		};
		app.addCommand(r);
		
	}	
	
}