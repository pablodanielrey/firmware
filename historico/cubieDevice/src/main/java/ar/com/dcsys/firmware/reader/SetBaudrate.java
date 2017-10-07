package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class SetBaudrate implements Cmd {

	public static final Logger logger = Logger.getLogger(Reader.class.getName());
	public static final String CMD = "setBaudrate";
	
	private final ar.com.dcsys.firmware.cmd.SetBaudrate setBaudrate;
	private final SerialDevice sd;
	private final Leds leds;
	private final Firmware app;
	
	@Inject
	public SetBaudrate(Firmware app, Leds leds, SerialDevice sd, ar.com.dcsys.firmware.cmd.SetBaudrate setBaudrate) {

		this.setBaudrate = setBaudrate;
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
			if (svalue == null) {
				try {
					remote.sendText("ERROR no existe parámetro del baudrate");
				} catch (IOException e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,e.getMessage(),e);
				}						
				return;
			}
			
			int index = Integer.valueOf(svalue);
			if (index < 1 || index > 5) {
				try {
					remote.sendText("ERROR indice inválido");
				} catch (IOException e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,e.getMessage(),e);
				}						
				return;
			}

			setBaudrate.execute(sd, index, new ar.com.dcsys.firmware.cmd.SetBaudrate.SetBaudrateResult() {
					@Override
					public void onSuccess(int index) {
						try {
							remote.sendText("OK index " + String.valueOf(index));
						} catch (IOException e) {
							e.printStackTrace();
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
					}
					@Override
					public void onInvalidBaudrate() {
						try {
							remote.sendText("ERROR invalid baudrate");
						} catch (IOException e) {
							e.printStackTrace();
							logger.log(Level.SEVERE,e.getMessage(),e);
						}						
					}
					@Override
					public void onFailure(int errorCode) {
						try {
							remote.sendText("ERROR");
						} catch (IOException e) {
							e.printStackTrace();
							logger.log(Level.SEVERE,e.getMessage(),e);
						}						
					};
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
