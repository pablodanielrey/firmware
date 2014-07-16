package ar.com.dcsys.firmware.camabio;

import java.util.logging.Logger;

import jssc.SerialPortException;
import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class SerialUtils {

	private static final Logger logger = Logger.getLogger(SerialUtils.class.getName());
	
	/**
	 * Lee un packete de datos o de comando desde el serialPort.
	 * @param serialPort
	 * @return
	 * @throws SerialPortException
	 */
	public static byte[] readPackage(SerialDevice serialPort) throws SerialException {
		byte[] id = serialPort.readBytes(2);
		int i = CamabioUtils.getId(id);

		logger.finest("Recibido id : " + i);
		
		if (i == CamabioUtils.RSP) {
			byte[] cmd = new byte[24];
			byte[] data = serialPort.readBytes(22);
			int inx = 2;
			cmd[0] = id[0];
			cmd[1] = id[1];
			for (byte b : data) {
				cmd[inx] = b;
				inx++;
			}

			logger.finest("recibido : " + Utils.getHex(cmd));
			
			return cmd;
			
		} else if (i == CamabioUtils.RSP_DATA) {
			
			byte[] ccmd = serialPort.readBytes(2);
			byte[] len = serialPort.readBytes(2);
			
			int ilen = ((len[1] & 0xff) << 8) + (len[0] & 0xff);
			
			logger.finest(String.format("Longitud leida desde el paquete (%h,%h) : %d",len[0],len[1],ilen));
			
			
			byte[] data = serialPort.readBytes(ilen + 2);

			byte[] cmd = new byte[2 + 2 + 2 + ilen + 2];
			
			cmd[0] = id[0];
			cmd[1] = id[1];
			cmd[2] = ccmd[0];
			cmd[3] = ccmd[1];
			cmd[4] = len[0];
			cmd[5] = len[1];
			
			int inx = 6;
			for (byte b : data) {
				cmd[inx] = b;
				inx++;
			}
			
			logger.finest("recibido : " + Utils.getHex(cmd));
			
			return cmd;
		} else {
			throw new SerialException("recibido id : " + i);
		}
	}
		
	/**
	 * Lee la cantidad de bytes especificadas por el parametro size.
	 * @param size
	 * @param serialPort
	 * @return
	 * @throws SerialException
	 */
	public static byte[] readPackage(int size, SerialDevice serialPort) throws SerialException {		
		byte[] id = serialPort.readBytes(2);
		int i = CamabioUtils.getId(id);

		logger.finest("Recibido id : " + i);

		byte[] cmd = new byte[size + 2];
		byte[] data = serialPort.readBytes(size);
		int inx = 2;
		cmd[0] = id[0];
		cmd[1] = id[1];
		for (byte b : data) {
			cmd[inx] = b;
			inx++;
		}

		logger.finest("recibido : " + Utils.getHex(cmd));
		
		return cmd;
			
	
	}		
	
}
