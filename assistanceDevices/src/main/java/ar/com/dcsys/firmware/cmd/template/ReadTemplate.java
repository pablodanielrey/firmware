package ar.com.dcsys.firmware.cmd.template;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.ProcessingException;
import ar.com.dcsys.firmware.cmd.enroll.EnrollData;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;
import ar.com.dcsys.security.Fingerprint;

public class ReadTemplate {
	
	public interface ReadTemplateResult extends TemplateResult {
		
		public void onInvalidTemplateNumber(int number);
		public void onEmptyTemplate(int number);
		
	}
	
	
	private final Logger logger;
	
	
	@Inject
	public ReadTemplate(Logger logger) {
		this.logger = logger;
	}
	
	
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
		fp.setFinger(edata.getFinger());
		
		byte[] templ = Arrays.copyOfRange(data, 2, len);
		fp.setTemplate(templ);
		
		return fp;
	}
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP && rsp.prefix != CamabioUtils.RSP_DATA) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_READ_TEMPLATE) {
			throw new CmdException("RCM != ReadTemplate");
		}
	}
	
	
	public void execute(SerialDevice sd, ReadTemplateResult result, EnrollData edata) throws CmdException {
		
		final int number = 1;
		
		try {
			byte[] cmd = CamabioUtils.readTemplate(number);
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
					
					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					if (code == CamabioUtils.ERR_INVALID_TMPL_NO) {
						try {
							result.onInvalidTemplateNumber(number);
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						return;
					}
				
					if (code == CamabioUtils.ERR_TMPL_EMPTY) {
						try {
							result.onEmptyTemplate(number);
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
					
				}
			} 
			
		} catch (SerialException | ProcessingException e) {
			throw new CmdException(e);
		}
	}
	
	
}
