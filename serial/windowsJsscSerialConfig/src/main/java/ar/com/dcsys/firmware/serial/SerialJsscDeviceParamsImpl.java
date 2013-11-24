package ar.com.dcsys.firmware.serial;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialJsscDeviceParamsImpl implements SerialJsscDeviceParams {

	@Override
	public SerialPort getSerialPort() {
		return new SerialPort("COM5");
	}

	@Override
	public void setParams(SerialPort serialPort) throws SerialPortException {
		serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, false, false);
	}

}
