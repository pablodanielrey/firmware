package ar.com.dcsys.firmware.serial;

import jssc.SerialPort;
import jssc.SerialPortException;

public interface SerialJsscDeviceParams {

	public SerialPort getSerialPort();
	public void setParams(SerialPort serialPort) throws SerialPortException;
	
}
