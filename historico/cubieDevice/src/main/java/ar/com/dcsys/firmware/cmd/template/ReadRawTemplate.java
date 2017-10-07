package ar.com.dcsys.firmware.cmd.template;

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

public class ReadRawTemplate {
	
	public interface ReadRawTemplateResult {
		
		public void onSuccess(byte[] templ);
		public void onInvalidTemplateNumber(int number);
		public void onEmptyTemplate(int number);
		
		public void onFailure(int errorCode);

	}
	
	
	private static final Logger logger = Logger.getLogger(ReadRawTemplate.class.getName());
	
	
	
	private byte[] getTemplate(int len, byte[] data) {
		byte[] templ = Arrays.copyOfRange(data, 2, len);
		return templ;
	}
	
	
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP && rsp.prefix != CamabioUtils.RSP_DATA) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_READ_TEMPLATE) {
			throw new CmdException("RCM != ReadTemplate");
		}
	}
	
	
	public void execute(SerialDevice sd, int number, ReadRawTemplateResult result) throws CmdException {
	
		MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].acquireUninterruptibly();
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
					
				    byte[] template = getTemplate(len, rsp.data);
				    try {
				    	result.onSuccess(template);
				    	
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
			sd.cancel();
			throw new CmdException(e);
			
		} finally {
			MutualExclusion.using[MutualExclusion.SERIAL_DEVICE].release();
		}
	}
	
	
}
