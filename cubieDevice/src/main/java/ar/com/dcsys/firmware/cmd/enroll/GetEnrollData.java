package ar.com.dcsys.firmware.cmd.enroll;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.ProcessingException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;
import ar.com.dcsys.security.Fingerprint;

public class GetEnrollData {
	
	private static final Logger logger = Logger.getLogger(GetEnrollData.class.getName());
	
	/**
	 * Retorna los datos del fingerprint.
	 * @param len
	 * @param data
	 * @param edata
	 * @return
	 */
	private Fingerprint getFingerprint(int len, byte[] data, EnrollData edata) {
		Fingerprint fp = new Fingerprint();
		
		fp.setAlgorithm(CamabioUtils.ALGORITHM);
		fp.setCodification(CamabioUtils.CODIFICATION);
		fp.setPersonId(edata.getPersonId());
		fp.setFinger(edata.getFinger());
		
		byte[] templ = Arrays.copyOfRange(data, 0, len);
		fp.setTemplate(templ);
		
		return fp;
	}
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP && rsp.prefix != CamabioUtils.RSP_DATA) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_GET_ENROLL_DATA) {
			throw new CmdException("RCM != GetEnrollData");
		}
	}
	
	
	public void execute(SerialDevice sd, EnrollResult result, EnrollData edata) throws CmdException {
		
		try {
			
			byte[] cmd = CamabioUtils.getEnrollData();
			sd.writeBytes(cmd);

			int tmplSize = 0;
			final int headerSize = 8;
			
			while (true) {
				
				byte[] data;
				if (tmplSize <= 0) {
					data = SerialUtils.readPackage(sd);	
				} else {
					data = SerialUtils.readPackage(tmplSize + headerSize,sd);
				}
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rsp);

				// evaluo la respuesta del FP_CANCEL.
				if (rsp.rcm == CamabioUtils.CMD_FP_CANCEL && rsp.ret == CamabioUtils.ERR_SUCCESS) {
					result.onCancel();
					return;
				}						
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS && rsp.prefix == CamabioUtils.RSP) {
					
					logger.fine("Recibido paquete de respuesta");
					tmplSize = CamabioUtils.getDataIn4ByteInt(rsp.data);
					
					logger.info("Tamaño del template : " + tmplSize);
					
				} else if (rsp.ret == CamabioUtils.ERR_SUCCESS && rsp.prefix == CamabioUtils.RSP_DATA) {
						
					logger.fine("Recibido paquete de datos");
					int len = rsp.len - 2;
					if (tmplSize != len) {
						throw new CmdException("Len != templSize");
					}
					
					logger.info("Leyendo huella con tamaño : " + len);
					logger.info("Tamaño del paquete : " + rsp.data.length);
					logger.info("Paquete : " + Utils.getHex(rsp.data));
					
				    Fingerprint fp = getFingerprint(len, rsp.data, edata);
				    try {
				    	result.onSuccess(fp);
				    } catch (Exception e) {
				    	logger.log(Level.SEVERE,e.getMessage(),e);
				    }
				    return;
						
				} else if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					int error = CamabioUtils.getDataIn4ByteInt(rsp.data);
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
