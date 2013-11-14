package ar.com.dcsys.firmware.cmd;

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
public class IdentifyFree implements Cmd {
	
	private final Logger logger;
	
	@Inject
	public IdentifyFree(Logger logger) {
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
						System.out.println("Debe levantar el dedo del lector");
						continue;
					}
					
					result.onSuccess(ret);
//					return;
					
				} else if (ret == CamabioUtils.ERR_FAIL) {
					
					byte[] d = CamabioUtils.getData(data);
					ret = CamabioUtils.getDataIn4ByteInt(d);
					
					if (ret == CamabioUtils.ERR_TIME_OUT || ret == CamabioUtils.ERR_ALL_TMPL_EMPTY || ret == CamabioUtils.ERR_BAD_CUALITY) {
						result.onFailure(ret);
//						return;
					}
					
					if (ret == CamabioUtils.ERR_IDENTIFY) {
						// no se pudo encontrar la persona dentro de la base.
						result.onSuccess();
//						return;
					}
					
					if (ret == CamabioUtils.ERR_FP_CANCEL) {
						result.onFailure(CamabioUtils.ERR_FP_CANCEL);
//						return;
					}
					
					throw new CmdException("Resultado inesperado");
				}
			}
			
			
		} catch (SerialException | CmdException | CamabioException | ProcessingException e) {
			throw new CmdException(e);
		}
	}
}
