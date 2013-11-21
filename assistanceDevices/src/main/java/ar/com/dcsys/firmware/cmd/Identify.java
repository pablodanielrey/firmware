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
import javax.inject.Named;

import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

@Named
public class Identify {
	
	public interface IdentifyResult {
		public void onSuccess(int fpNumber);
		public void onNotFound();
		public void onCancel();
		public void onFailure(int errorCode);
	}
	
	private final Logger logger;
	
	@Inject
	public Identify(Logger logger) {
		this.logger = logger;
	}
	
	private void checkPreconditions(int rcm, CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != rcm) {
			throw new CmdException("RCM != Identify");
		}
	}	
	
	
	public void execute(SerialDevice serialPort, IdentifyResult result) throws CmdException {
		try {
			byte[] cmd = CamabioUtils.identify();
			int rcm = CamabioUtils.getCmd(cmd);
			serialPort.writeBytes(cmd);
			
			while (true) {
				byte[] data = SerialUtils.readPackage(serialPort);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rcm, rsp);
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS) {

					int code = CamabioUtils.getDataIn4ByteInt(rsp.data);
					
					if (code == CamabioUtils.GD_NEED_RELEASE_FINGER) {
						logger.info("Debe levantar el dedo del lector");
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
						logger.info("No se pudo encontrar la huella dentro de la base");
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
						return;
					}
					
					throw new CmdException("Resultado inesperado");
				}
			}
			
			
		} catch (SerialException | CmdException | ProcessingException e) {
			throw new CmdException(e);
		}
	}
}
