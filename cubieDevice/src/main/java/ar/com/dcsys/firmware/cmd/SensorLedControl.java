package ar.com.dcsys.firmware.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class SensorLedControl {

	private final static Logger logger = Logger.getLogger(SensorLedControl.class.getName());
	
	public interface SensorLedControlResult {
		public void onSuccess();
		public void onFailure();
	}

	private void checkPreconditions(int rcm, CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != rcm) {
			throw new CmdException("RCM != SensorLedControl");
		}
	}	
	
	public void execute(SerialDevice serialPort, boolean led, SensorLedControlResult result) throws CmdException {
		
		MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].acquireUninterruptibly();
		try {
			byte[] cmd = CamabioUtils.sensorLedControl(led);
			int rcm = CamabioUtils.getCmd(cmd);
			serialPort.writeBytes(cmd);
			
			byte[] data = SerialUtils.readPackage(serialPort);
			CamabioResponse rsp = CamabioUtils.getResponse(data);
			checkPreconditions(rcm, rsp);
			
			result.onSuccess();
			
		} catch (SerialException | ProcessingException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new CmdException(e);
			
		} finally {
			MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].release();
		}
		
	}
	
}
