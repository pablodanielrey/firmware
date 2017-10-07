package ar.com.dcsys.firmware.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class GetTimeout {
	
	public interface GetTimeoutResult {
		public void onSuccess(int seconds);
		public void onFailure();
	}
	
	private static final Logger logger = Logger.getLogger(GetTimeout.class.getName());
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_GET_FINGER_TIME_OUT) {
			throw new CmdException("RCM != GetEmptyId");
		}
	}
	
	
	public void execute(SerialDevice sd, GetTimeoutResult result) throws CmdException {
		
		MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].acquireUninterruptibly();
		try {
			byte[] cmd = CamabioUtils.getFingerTimeOut();
			sd.writeBytes(cmd);

			while (true) {
				
				byte[] data = SerialUtils.readPackage(sd);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rsp);

				if (rsp.ret == CamabioUtils.ERR_SUCCESS && rsp.prefix == CamabioUtils.RSP) {
					
					int seconds = CamabioUtils.getDataIn2ByteInt(rsp.data);
					try {
						result.onSuccess(seconds);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				} else if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					try {
						result.onFailure();
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				} else {

					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					throw new CmdException("Datos inesperados - rcm " + rsp.rcm + " ret " + rsp.ret + " code " + code);
				}
				
			} 
			
		} catch (SerialException | ProcessingException e) {
			sd.cancel();
			throw new CmdException(e);
			
		} finally {
			MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].release();
		}
	}
	
	
}
