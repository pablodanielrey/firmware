package ar.com.dcsys.firmware.serial;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import ar.com.dcsys.firmware.Utils;

public class SerialDeviceJssC implements SerialDevice {

	private  SerialPort serialPort;
	private ConcurrentLinkedQueue<Byte> queue;
	private final Semaphore sem;
	
	public SerialDeviceJssC() {
		sem = new Semaphore(0);
		queue = new ConcurrentLinkedQueue<>();
	}
	
	@Override
	public boolean open() throws SerialException {
		queue.clear();
	
		try {
		
	    	serialPort = new SerialPort("/dev/ttyUSB0");
	    	if (!serialPort.openPort()) {
	    		return false;
	    	}
	    	serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, false, false);
	    	
	    	
	    	int mask = SerialPort.MASK_RXCHAR;
	    	serialPort.setEventsMask(mask);
	    	serialPort.addEventListener(new SerialPortEventListener() {
				@Override
				public void serialEvent(SerialPortEvent event) {
					System.out.println("SerialEvent");
					if (event.isRXCHAR()) {
						int bytes = event.getEventValue();
						
						System.out.println("llegaron : " + String.valueOf(bytes));
						
						if (bytes > 0) {
							try {
								byte[] data = serialPort.readBytes(bytes);
								
								System.out.println("Datos : " + Utils.getHex(data));
								
								for (byte b : data) {
									queue.add(b);
								}
							} catch (SerialPortException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					sem.release();
				}
			});    	
	    	
	    	return true;
    	
		} catch (SerialPortException e) {
			throw new SerialException(e);
		}
	}
	
	@Override
	public void close() throws SerialException {
		try {
			serialPort.closePort();
			serialPort = null;
		} catch (SerialPortException e) {
			throw new SerialException(e);
		}
		
		queue.clear();
	}
	
	@Override
	public void writeBytes(byte[] data) throws SerialException {
		try {
			serialPort.writeBytes(data);
		} catch (SerialPortException e) {
			throw new SerialException(e);
		}
	}
	
	@Override
	public byte[] readBytes(int count) {
		while (queue.size() < count) {
			try {
				sem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		byte[] rd = new byte[count];
		for (int i = 0; i < count; i++) {
			rd[i] = queue.remove();
		}
		return rd;
	}
	
}
