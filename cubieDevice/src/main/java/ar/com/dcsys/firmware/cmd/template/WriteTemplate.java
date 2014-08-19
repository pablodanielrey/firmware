package ar.com.dcsys.firmware.cmd.template;

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

public class WriteTemplate {
	
	private static final Logger logger = Logger.getLogger(WriteTemplate.class.getName());
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP && rsp.prefix != CamabioUtils.RSP_DATA) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_WRITE_TEMPLATE) {
			throw new CmdException("RCM != WriteTemplate");
		}
	}
	
	
	public void execute(SerialDevice sd, WriteTemplateResult result, TemplateData edata) throws CmdException {
		
		final byte[] template = edata.getFingerprint().getTemplate();
		final int templateSize = template.length;
		
		try {
			byte[] cmd = CamabioUtils.writeTemplate(templateSize);
			sd.writeBytes(cmd);

			while (true) {
				
				byte[] data = SerialUtils.readPackage(sd);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rsp);

				// evaluo la respuesta del FP_CANCEL.
				if (rsp.rcm == CamabioUtils.CMD_FP_CANCEL && rsp.ret == CamabioUtils.ERR_SUCCESS) {
					result.onCancel();
					return;
				}						
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS && rsp.prefix == CamabioUtils.RSP) {
					
					int ok = CamabioUtils.getDataIn2ByteInt(rsp.data);

					// envío el CommandDataPacket con el número de template y los datos del template.
					cmd = CamabioUtils.writeTemplateData(edata.getNumber(), template);
					sd.writeBytes(cmd);
					
					continue;
					

				} else if (rsp.ret == CamabioUtils.ERR_FAIL && rsp.prefix == CamabioUtils.RSP) {

					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					
					if (code == CamabioUtils.ERR_INVALID_PARAM) {
						try {
							result.onInvalidTemplateSize(templateSize);
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						return;
					}
					
				
				} else if (rsp.ret == CamabioUtils.ERR_SUCCESS && rsp.prefix == CamabioUtils.RSP_DATA) {

					int templateNumber = CamabioUtils.getDataIn2ByteInt(rsp.data);
					if (templateNumber != edata.getNumber()) {
						try {
							result.onFailure(-1);
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						return;
					}
					
					try {
						result.onSuccess(templateNumber);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
						
				} else if (rsp.ret == CamabioUtils.ERR_FAIL && rsp.prefix == CamabioUtils.RSP_DATA) {
					
					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					try {
						result.onFailure(code);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				}
				
				throw new CmdException("Datos inesperados - prefix " + rsp.prefix + " rcm " + rsp.rcm + " ret " + rsp.ret);
			} 
			
		} catch (SerialException | ProcessingException e) {
			throw new CmdException(e);
		}
	}
	
	
}
