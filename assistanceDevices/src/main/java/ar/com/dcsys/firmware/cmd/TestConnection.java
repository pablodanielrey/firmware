package ar.com.dcsys.firmware.cmd;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class TestConnection implements Cmd {

	@Override
	public void execute(SerialDevice serialPort, CmdResult result) throws CmdException {
		
		try {
			byte[] cmd = CamabioUtils.testConnection();
			System.out.println(Utils.getHex(cmd));
			serialPort.writeBytes(cmd);
			
			byte[] data = SerialUtils.readPackage(serialPort);
			
			int id = CamabioUtils.getId(data);
			if (id == 0x55aa) {
				CamabioUtils.printPacket(data);
				result.onSuccess();
			} else {
				result.onFailure();
			}
			
		} catch (SerialException | ProcessingException e) {
			e.printStackTrace();
		}
		
	}
	
}
