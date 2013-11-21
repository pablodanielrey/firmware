package ar.com.dcsys.firmware.cmd.enroll;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.ProcessingException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;
import ar.com.dcsys.security.FingerprintCredentials;

public class GetEnrollData {
	
	private final Logger logger;
	
	@Inject
	public GetEnrollData(Logger logger) {
		this.logger = logger;
	}
	
	
	/**
	 * Retorna los datos del fingerprint.
	 * @param len
	 * @param data
	 * @param edata
	 * @return
	 */
	private FingerprintCredentials getFingerprint(int len, byte[] data, EnrollData edata) {
		FingerprintCredentials fp = new FingerprintCredentials();
		
		fp.setAlgorithm(CamabioUtils.ALGORITHM);
		fp.setCodification(CamabioUtils.CODIFICATION);
		fp.setFinger(edata.getFinger());
		
		byte[] templ = Arrays.copyOfRange(data, 0, len);
		fp.setTemplate(templ);
		
		return fp;
	}
	
	private void checkPreconditions(int rcm, CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP || rsp.prefix != CamabioUtils.RSP_DATA) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != rcm) {
			throw new CmdException("RCM != GetEnrollData");
		}
	}
	
	
	public void execute(SerialDevice sd, EnrollResult result, EnrollData edata) throws CmdException {
		try {
			byte[] cmd = CamabioUtils.getEnrollData();
			int rcm = CamabioUtils.getCmd(cmd);
			sd.writeBytes(cmd);

			int tmplSize = 0;
			
			while (true) {
				byte[] data = SerialUtils.readPackage(sd);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rcm, rsp);
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS) {
					
					if (rsp.prefix == CamabioUtils.RSP) {
						
						logger.fine("Recibido paquete de respuesta");
						tmplSize = CamabioUtils.getDataIn2ByteInt(rsp.data);
						
					} else if (rsp.prefix == CamabioUtils.RSP_DATA) {
						
						logger.fine("Recibido paquete de datos");
						int len = rsp.len - 2;
						if (tmplSize != len) {
							throw new CmdException("Len != templSize");
						}
					    FingerprintCredentials fp = getFingerprint(len, rsp.data, edata);
					    try {
					    	result.onSuccess(fp);
					    } catch (Exception e) {
					    	logger.log(Level.SEVERE,e.getMessage(),e);
					    }
					    return;
						
					} else {
						logger.log(Level.SEVERE,"Prefijo inválido : " + rsp.prefix);
						throw new CmdException("Prefijo inválido : " + rsp.prefix);
					}
					
				} else if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					int error = CamabioUtils.getDataIn2ByteInt(rsp.data);
					try {
						result.onFailure(error);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				}
			} 
			
		} catch (SerialException | ProcessingException e) {
			throw new CmdException(e);
		}
	}
	
	
}
