package ar.com.dcsys.firmware.serial;

import javax.inject.Inject;

import jssc.SerialPort;
import jssc.SerialPortException;
import ar.com.dcsys.config.Config;

public class DefaultJsscDeviceParams implements SerialJsscDeviceParams {

	@Inject @Config String port;
	@Inject @Config String bauds;
	@Inject @Config String dataBits;
	@Inject @Config String stopBit;
	@Inject @Config String parity;
	
	
	@Override
	public SerialPort getSerialPort() {
		return new SerialPort(port);
	}

	@Override
	public void setParams(SerialPort serialPort) throws SerialPortException {
		
		int b = Integer.parseInt(bauds);
		int db = Integer.parseInt(dataBits);
		int sb = Integer.parseInt(stopBit);
		int p = Integer.parseInt(parity);
		
		serialPort.setParams(b, db, sb, p, false, false);
	}

}
