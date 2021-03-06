package ar.com.dcsys.firmware.serial;

import java.util.logging.Logger;

import javax.inject.Inject;

import jssc.SerialPort;
import jssc.SerialPortException;
import ar.com.dcsys.config.Config;

public class DefaultJsscDeviceParams implements SerialJsscDeviceParams {

	public static final Logger logger = Logger.getLogger(DefaultJsscDeviceParams.class.getName());
	
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
		
		logger.fine("Seteando parámetros de config : " + bauds + " " + dataBits + " " + stopBit + " " + parity);
		
		int b = Integer.parseInt(bauds);
		int db = Integer.parseInt(dataBits);
		int sb = Integer.parseInt(stopBit);
		int p = Integer.parseInt(parity);

		serialPort.setParams(b, db, sb, p, true, true);
		serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
	}

}
