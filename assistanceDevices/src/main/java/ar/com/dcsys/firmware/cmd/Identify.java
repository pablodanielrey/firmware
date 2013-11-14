package ar.com.dcsys.firmware.cmd;

import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class Identify implements Cmd {
	
	private final Logger logger;
	
	private int getDataIn4ByteInt(byte[] data) throws ProcessingException, CmdException {
		if (data.length != 4) {
			throw new CmdException("longitud erronea");
		}
		int result = (data[0] & 0xff) + ((data[1] & 0xff) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xff) << 24);
		return result;
	}

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
					ret = getDataIn4ByteInt(d);
					
					if (ret == CamabioUtils.GD_NEED_RELEASE_FINGER) {
						System.out.println("Debe levantar el dedo del lector");
						continue;
					}
					
					result.onSuccess(ret);
					return;
					
				} else if (ret == CamabioUtils.ERR_FAIL) {
					
					byte[] d = CamabioUtils.getData(data);
					ret = getDataIn4ByteInt(d);
					
					if (ret == CamabioUtils.ERR_TIME_OUT || ret == CamabioUtils.ERR_ALL_TMPL_EMPTY || ret == CamabioUtils.ERR_BAD_CUALITY) {
						result.onFailure(ret);
						return;
					}
					
					if (ret == CamabioUtils.ERR_IDENTIFY) {
//						System.out.println("No se pudo encontrar la persona dentro de la base");
						result.onSuccess();
						return;
					}
					
					if (ret == CamabioUtils.ERR_FP_CANCEL) {
						result.onFailure(CamabioUtils.ERR_FP_CANCEL);
						return;
					}
					
					throw new CmdException("Resultado inesperado");
				}
			}
			
			
		} catch (SerialException | CmdException | ProcessingException e) {
			
			try {
				
				byte[] cmd = CamabioUtils.fpCancel();
				serialPort.writeBytes(cmd);
				SerialUtils.readPackage(serialPort);			// ERR_FP_CANCEL
				SerialUtils.readPackage(serialPort);			// ERR_SUCCESS
				
			} catch (SerialException ex) {
				
			}
			
			throw new CmdException(e);
		}
	}
}
