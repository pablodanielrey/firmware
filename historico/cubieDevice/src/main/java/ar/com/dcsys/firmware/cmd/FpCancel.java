package ar.com.dcsys.firmware.cmd;

import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class FpCancel {

	public interface FpCancelResult {
		public void onSuccess();
	}
	
	private final Logger logger; 
	
	@Inject
	public FpCancel(Logger logger) {
		this.logger = logger;
	}
	
	public void execute(SerialDevice serialPort, FpCancelResult result) throws CmdException {

		try {
			byte[] cmd = CamabioUtils.fpCancel();
			serialPort.writeBytes(cmd);
			result.onSuccess();
			
		} catch (SerialException e) {
			throw new CmdException(e);
		}
		
	}

}
