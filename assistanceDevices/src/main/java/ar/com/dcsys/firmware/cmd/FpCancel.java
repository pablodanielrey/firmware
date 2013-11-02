package ar.com.dcsys.firmware.cmd;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class FpCancel implements Cmd {

	private int getDataIn4ByteInt(byte[] data) throws ProcessingException, CmdException {
		if (data.length != 4) {
			throw new CmdException("longitud erronea");
		}
		int result = (data[0] & 0xff) + ((data[1] & 0xff) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xff) << 24);
		return result;
	}	
	
	@Override
	public void execute(SerialDevice serialPort, CmdResult result) throws CmdException {

		try {
			byte[] cmd = CamabioUtils.fpCancel();
			System.out.println("Comando Enviado : " + Utils.getHex(cmd));
			
			serialPort.writeBytes(cmd);

			boolean ok = false;
			while (!ok) {
			
				// debo recibir 2 comandos : 
				// ERR_FP_CANCEL y ERR_SUCCESS
				// o en caso de cancelar comando mas simples solo:
				// ERR_SUCCESS
				
				byte[] data = SerialUtils.readPackage(serialPort);
				System.out.println("Datos recibidos : " + Utils.getHex(data));
	
				int id = CamabioUtils.getId(data);
				if (id != CamabioUtils.RSP) {
					throw new CmdException();
				}
				int ret = CamabioUtils.getRet(data);
				
				if (ret == CamabioUtils.ERR_SUCCESS) {
					ok = true;
				}
				
			    if (ret == CamabioUtils.ERR_FAIL) {
				
			    	try {
				    	byte[] d = CamabioUtils.getData(data);
	
				    	ret = getDataIn4ByteInt(d);
					
				    	if (ret == CamabioUtils.ERR_FP_CANCEL) {
				    		// respuesta de cancelamiento del comando anterior.
				    		
				    	}
			    	
			    	} catch (ProcessingException e) {
			    		e.printStackTrace();
			    	}
			    	
				}
			}
			
			result.onSuccess();
			    
		} catch (SerialException e) {
			e.printStackTrace();
		}
		
	}

}
