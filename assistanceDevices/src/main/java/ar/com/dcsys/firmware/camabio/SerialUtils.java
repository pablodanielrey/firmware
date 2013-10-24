package ar.com.dcsys.firmware.camabio;

import jssc.SerialPortException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class SerialUtils {

	/**
	 * Lee un packete de datos o de comando desde el serialPort.
	 * @param serialPort
	 * @return
	 * @throws SerialPortException
	 */
	public static byte[] readPackage(SerialDevice serialPort) throws SerialException {
		
		byte[] id = serialPort.readBytes(2);
		int i = CamabioUtils.getId(id);
		
		if (i == CamabioUtils.CMD || i == CamabioUtils.RSP) {
			byte[] cmd = new byte[24];
			byte[] data = serialPort.readBytes(22);
			int inx = 2;
			cmd[0] = id[0];
			cmd[1] = id[1];
			for (byte b : data) {
				cmd[inx] = b;
				inx++;
			}
			return cmd;
			
		} else if (i == CamabioUtils.CMD_DATA || i == CamabioUtils.RSP_DATA) {
			byte[] cmd = new byte[512];
			byte[] data = serialPort.readBytes(510);
			int inx = 2;
			cmd[0] = id[0];
			cmd[1] = id[1];
			for (byte b : data) {
				cmd[inx] = b;
				inx++;
			}
			return cmd;
		}
		
		return null;
	}		
	
}
