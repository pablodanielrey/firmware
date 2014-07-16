package ar.com.dcsys.firmware.cmd;

/**
 * Identifica una huella dentro de la base de datos del lector.
 * En el caso de identificación exitosa retorna :
 * 
 * onSuccess(numero de huella)
 * 
 * en el caso de no poder identificar la huella llama :
 * 
 * onFailure
 * 
 * en el caso de algun error de lectura, etc llama :
 * 
 * onFailure(codigo de error)
 * 
 */

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class Identify {
	
	public interface IdentifyResult {
		public void releaseFinger();
		public void onSuccess(int fpNumber);
		public void onNotFound();
		public void onCancel();
		public void onFailure(int errorCode);
	}
	
	private final Logger logger = Logger.getLogger(Identify.class.getName());

	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_IDENTIFY) {
			throw new CmdException("RCM != Identify");
		}
	}	

	
	public void execute(SerialDevice serialPort, IdentifyResult result) throws CmdException {
		try {
			byte[] cmd = CamabioUtils.identify();
			serialPort.writeBytes(cmd);
			
			boolean canceled = false;
			
			while (true) {
				byte[] data = SerialUtils.readPackage(serialPort);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rsp);
		
				// evaluo la respuesta del FP_CANCEL.
				if (rsp.rcm == CamabioUtils.CMD_FP_CANCEL && rsp.ret == CamabioUtils.ERR_SUCCESS) {
					if (canceled) {
						return;
					} else {
						canceled = true;
					}
					continue;
				}
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS) {

					int code = CamabioUtils.getDataIn4ByteInt(rsp.data);
					
					if (code == CamabioUtils.GD_NEED_RELEASE_FINGER) {
						try {
							result.releaseFinger();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						continue;
					}
					
					try {
						result.onSuccess(code);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				} else if (rsp.ret == CamabioUtils.ERR_FAIL) {
		
					int code = CamabioUtils.getDataIn4ByteInt(rsp.data);
					
					if (code == CamabioUtils.ERR_TIME_OUT || 
						code == CamabioUtils.ERR_ALL_TMPL_EMPTY || 
						code == CamabioUtils.ERR_BAD_CUALITY) {
						try {
							result.onFailure(code);
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						return;
					}
					
					if (code == CamabioUtils.ERR_IDENTIFY) {
						try {
							result.onNotFound();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						return;
					}
					
					if (code == CamabioUtils.ERR_FP_CANCEL) {
						try {
							result.onCancel();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}

						if (canceled) {
							return;
						} else {
							canceled = true;
						}
						continue;
					}
					
					throw new CmdException("Datos inesperados - rcm " + rsp.rcm + " ret " + rsp.ret + " code " + code);
				}
			}
			
			
		} catch (SerialException | ProcessingException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new CmdException(e);
		}
	}
}
