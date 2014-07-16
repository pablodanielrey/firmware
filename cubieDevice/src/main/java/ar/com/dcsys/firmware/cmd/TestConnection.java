package ar.com.dcsys.firmware.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class TestConnection {

	public interface TestConnectionResult {
		public void onSuccess();
		public void onFailure();
	}

	private final Logger logger;
	
	@Inject
	public TestConnection(Logger logger) {
		this.logger = logger;
	}
	
	private void checkPreconditions(int rcm, CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != rcm) {
			throw new CmdException("RCM != TestConnection");
		}
	}	
	
	public void execute(SerialDevice serialPort, TestConnectionResult result) throws CmdException {
		
		try {
			byte[] cmd = CamabioUtils.testConnection();
			int rcm = CamabioUtils.getCmd(cmd);
			serialPort.writeBytes(cmd);
			
			byte[] data = SerialUtils.readPackage(serialPort);
			CamabioResponse rsp = CamabioUtils.getResponse(data);
			checkPreconditions(rcm, rsp);
			
			result.onSuccess();
			
		} catch (SerialException | ProcessingException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new CmdException(e);
		}
		
	}
	
}
