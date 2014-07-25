package ar.com.dcsys.firmware.cmd.template;



import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.ProcessingException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class ClearAllTemplate {
	
	public interface ClearAllTemplateResult {
		public void onSuccess(int number);
		public void onFailure(int errorCode);
		public void onCancel();
	}
	
	private static final Logger logger = Logger.getLogger(ClearAllTemplate.class.getName());
	
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_CLEAR_ALL_TEMPLATE) {
			throw new CmdException("RCM != ClearAllTemplate");
		}
	}	
	
	
	public void excecute(SerialDevice sd, ClearAllTemplateResult result) throws CmdException {
		
		try {
			byte[] cmd = CamabioUtils.clearAllTemplate();
			sd.writeBytes(cmd);

			while (true) {
				
				byte[] data = SerialUtils.readPackage(sd);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rsp);

				// evaluo la respuesta del FP_CANCEL.
				if (rsp.rcm == CamabioUtils.CMD_FP_CANCEL && rsp.ret == CamabioUtils.ERR_SUCCESS) {
					try {
						result.onCancel();
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
				}						
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS && rsp.prefix == CamabioUtils.RSP) {
					
					int number = CamabioUtils.getDataIn2ByteInt(rsp.data);
					try {
						result.onSuccess(number);
						
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				} else if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
			
					try {
						result.onFailure(code);
						
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				} else {

					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					throw new CmdException("Datos inesperados - rcm " + rsp.rcm + " ret " + rsp.ret + " code " + code);
				}
				
			} 
			
		} catch (SerialException | ProcessingException e) {
			throw new CmdException(e);
		}		
		
	}

}
