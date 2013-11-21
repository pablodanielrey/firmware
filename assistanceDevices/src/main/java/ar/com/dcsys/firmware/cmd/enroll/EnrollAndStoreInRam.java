package ar.com.dcsys.firmware.cmd.enroll;

import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.ProcessingException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class EnrollAndStoreInRam {
	
	private final Logger logger;
	private final GetEnrollData getEnrollData;
	
	@Inject
	public EnrollAndStoreInRam(Logger logger, GetEnrollData getEnrollData) {
		this.logger = logger;
		this.getEnrollData = getEnrollData;
	}
	
	private void checkPreconditions(int rcm, CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP || rsp.prefix != CamabioUtils.RSP_DATA) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != rcm) {
			throw new CmdException("RCM != GetEnrollData");
		}
	}	
	
	public void execute(SerialDevice serialPort, EnrollResult result, EnrollData edata) throws CmdException {
		try {
			byte[] cmd = CamabioUtils.enrollAndStoreInRam();
			int rcm = CamabioUtils.getCmd(cmd);
			serialPort.writeBytes(cmd);
			
			while (true) {
				byte[] data = SerialUtils.readPackage(serialPort);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rcm, rsp);
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS) {

					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					
					if (code == CamabioUtils.GD_NEED_FIRST_SWEEP) {
						logger.info("Primera toma");
						continue;
					}
					
					if (code == CamabioUtils.GD_NEED_SECOND_SWEEP) {
						logger.info("Segunda toma");
						continue;
					}
					
					if (code == CamabioUtils.GD_NEED_THIRD_SWEEP) {
						logger.info("Tercera toma");
						continue;
					}

					if (code == CamabioUtils.GD_NEED_RELEASE_FINGER) {
						logger.info("Debe levantar el dedo");
						continue;
					}
					
					getEnrollData.execute(serialPort,result,edata);
					return;
				}
				
				if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);

					// errores no fatales.
						if (code == CamabioUtils.ERR_TIME_OUT ||
						code == CamabioUtils.ERR_BAD_CUALITY) {
						continue;
					}
					
						// error fatal.
					if (code == CamabioUtils.ERR_GENERALIZE) {
						result.onFailure(code);
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
