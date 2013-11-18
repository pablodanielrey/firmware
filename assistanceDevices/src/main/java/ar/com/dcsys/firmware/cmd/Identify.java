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

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioException;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

@Named
public class Identify implements Cmd {
	
	private final Logger logger;
	
	@Inject
	public Identify(Logger logger) {
		this.logger = logger;
	}
	
	
	@Override
	public void execute(SerialDevice serialPort, CmdResult result) throws CmdException {
		try {
			byte[] cmd = CamabioUtils.identify();
			logger.fine("Enviando comando : " + Utils.getHex(cmd));
			serialPort.writeBytes(cmd);
			
			while (true) {
				byte[] data = SerialUtils.readPackage(serialPort);
				logger.fine("Datos recibidos : " + Utils.getHex(data));
				
				int id = CamabioUtils.getId(data);
				if (id != CamabioUtils.RSP) {
					throw new CmdException();
				}
				int ret = CamabioUtils.getRet(data);
				
				if (ret == CamabioUtils.ERR_SUCCESS) {

					byte[] d = CamabioUtils.getData(data);
					ret = CamabioUtils.getDataIn4ByteInt(d);
					
					if (ret == CamabioUtils.GD_NEED_RELEASE_FINGER) {
						logger.info("Debe levantar el dedo del lector");
						continue;
					}
					
					try {
						result.onSuccess(ret);
					} catch (Exception e) {
						
					}
					return;
					
				} else if (ret == CamabioUtils.ERR_FAIL) {
					
					byte[] d = CamabioUtils.getData(data);
					ret = CamabioUtils.getDataIn4ByteInt(d);
					
					if (ret == CamabioUtils.ERR_TIME_OUT || ret == CamabioUtils.ERR_ALL_TMPL_EMPTY || ret == CamabioUtils.ERR_BAD_CUALITY) {
						try {
							result.onFailure(ret);
						} catch (Exception e) {
							
						}
						return;
					}
					
					if (ret == CamabioUtils.ERR_IDENTIFY) {
						logger.info("No se pudo encontrar la huella dentro de la base");
						try {
							result.onFailure();
						} catch (Exception e) {
							
						}
						return;
					}
					
					if (ret == CamabioUtils.ERR_FP_CANCEL) {
						try {
							result.onFailure(CamabioUtils.ERR_FP_CANCEL);
						} catch (Exception e) {
							
						}
						return;
					}
					
					throw new CmdException("Resultado inesperado");
				}
			}
			
			
		} catch (SerialException | CmdException | CamabioException | ProcessingException e) {
			throw new CmdException(e);
		}
	}
}
