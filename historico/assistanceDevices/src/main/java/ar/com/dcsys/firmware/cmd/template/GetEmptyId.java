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

public class GetEmptyId {
	
	public interface GetEmptyIdResult {
		public void onSuccess(int tmplNumber);
		public void onCancel();
		public void onFailure(int errorCode);
		public void onEmptyNotExistent();
	}
	
	
	private final Logger logger;
	
	
	@Inject
	public GetEmptyId(Logger logger) {
		this.logger = logger;
	}
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_GET_EMTPY_ID) {
			throw new CmdException("RCM != GetEmptyId");
		}
	}
	
	
	public void execute(SerialDevice sd, GetEmptyIdResult result) throws CmdException {
		
		try {
			byte[] cmd = CamabioUtils.getEmptyId();
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
					
					int tmplNumber = CamabioUtils.getDataIn2ByteInt(rsp.data);
					try {
						result.onSuccess(tmplNumber);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					return;
					
				} else if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					if (code == CamabioUtils.ERR_EMPTY_ID_NOEXIST) {
						try {
							result.onEmptyNotExistent();
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
