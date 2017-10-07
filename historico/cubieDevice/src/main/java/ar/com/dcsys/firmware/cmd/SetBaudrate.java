package ar.com.dcsys.firmware.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class SetBaudrate {
	
	public static final int B9600 = 1;
	public static final int B19200 = 2;
	public static final int B38400 = 3;
	public static final int B57600 = 4;
	public static final int B115200 = 5;
	
	
	public interface SetBaudrateResult {
		public void onSuccess(int index);
		public void onFailure(int errorCode);
		public void onInvalidBaudrate();
	}
	
	private static final Logger logger = Logger.getLogger(SetBaudrate.class.getName());
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_SET_BAUDRATE) {
			throw new CmdException("RCM != SetBaudrate");
		}
	}
	
	
	/**
	 * Setea los baudios del serie del lector
	 * 1 - 9600
	 * 2 - 19200
	 * 3 - 38400
	 * 4 - 57600
	 * 5 - 115200
	 * @param sd
	 * @param bauds
	 * @param result
	 * @throws CmdException
	 */
	public void execute(SerialDevice sd, int bauds, SetBaudrateResult result) throws CmdException {
		
		MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].acquireUninterruptibly();
		try {
			byte[] cmd = CamabioUtils.setBaudRateIndex(bauds);
			sd.writeBytes(cmd);

			while (true) {
				
				byte[] data = SerialUtils.readPackage(sd);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rsp);


				if (rsp.ret == CamabioUtils.ERR_SUCCESS && rsp.prefix == CamabioUtils.RSP) {
					
					int index = CamabioUtils.getDataIn2ByteInt(rsp.data);
					try {
						result.onSuccess(index);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				} else if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					if (code == CamabioUtils.ERR_INVALID_BAUDRATE) {
						try {
							result.onInvalidBaudrate();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						return;
					}
				
					try {
						result.onFailure(code);
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
