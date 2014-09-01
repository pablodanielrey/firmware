package ar.com.dcsys.firmware.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class GetFirmwareVersion {

	private final static Logger logger = Logger.getLogger(GetFirmwareVersion.class.getName());
	
	public interface GetFirmwareVersionResult {
		public void onSuccess(String version);
		public void onFailure();
	}

	private void checkPreconditions(int rcm, CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != rcm) {
			throw new CmdException("RCM != " + GetFirmwareVersion.class.getName());
		}
	}	
	
	public void execute(SerialDevice serialPort, GetFirmwareVersionResult result) throws CmdException {
		
		MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].acquireUninterruptibly();
		try {
			byte[] cmd = CamabioUtils.getFirmwareVersion();
			int rcm = CamabioUtils.getCmd(cmd);
			serialPort.writeBytes(cmd);
			
			byte[] data = SerialUtils.readPackage(serialPort);
			CamabioResponse rsp = CamabioUtils.getResponse(data);
			checkPreconditions(rcm, rsp);
			
			if (rsp.ret == CamabioUtils.ERR_SUCCESS) {
	
				int version = CamabioUtils.getDataIn2ByteInt(rsp.data);
				result.onSuccess(String.valueOf(version));
				
			} else {
				
				result.onFailure();
			}
			
		} catch (SerialException | ProcessingException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new CmdException(e);
			
		} finally {
			MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].release();
		}
		
	}
	
}
