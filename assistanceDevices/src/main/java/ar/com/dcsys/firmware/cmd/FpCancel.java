package ar.com.dcsys.firmware.cmd;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

@Named
public class FpCancel implements Cmd {

	private final Logger logger; 
	
	@Inject
	public FpCancel(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void execute(SerialDevice serialPort, CmdResult result) throws CmdException {

		try {
			byte[] cmd = CamabioUtils.fpCancel();
			logger.fine("Enviando comando (fpcancel) " + Utils.getHex(cmd));
			serialPort.writeBytes(cmd);
			result.onSuccess();
		} catch (SerialException e) {
			logger.severe(e.getMessage());
			result.onFailure();
		}
		
	}

}
