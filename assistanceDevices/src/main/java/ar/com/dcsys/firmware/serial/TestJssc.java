package ar.com.dcsys.firmware.serial;

import jssc.SerialPort;
import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.camabio.CamabioUtils;

public class TestJssc {

	public static void main(String[] args) {
		
		try {
			
			SerialPort serialPort = new SerialPort("/dev/ttyUSB0");
			//SerialPort serialPort = new SerialPort("COM3");
	    	if (!serialPort.openPort()) {
	    		return;
	    	}
	    	serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, false, false);
	    	serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
	    	serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
	    				
			byte[] cmd = CamabioUtils.identify();
			
//			System.out.println(String.valueOf(cmd.length));
			
			System.out.println(Utils.getHex(cmd));
			serialPort.writeBytes(cmd);
			
			Thread.sleep(5*1000);
			
			while (true) {
				byte[] data = serialPort.readBytes();
				if (data == null || data.length == 0) {
					continue;
				}
				System.out.println(Utils.getHex(data));
			}
			//serialPort.closePort();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
