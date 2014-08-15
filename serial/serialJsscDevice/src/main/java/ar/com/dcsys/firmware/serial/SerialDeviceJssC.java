package ar.com.dcsys.firmware.serial;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


@Singleton
public class SerialDeviceJssC implements SerialDevice {
	
	private static final Logger logger = Logger.getLogger(SerialDevice.class.getName());
	
	private  SerialPort serialPort;
	private final ConcurrentLinkedQueue<Byte> queue;
	private final Semaphore sem;
	private final SerialJsscDeviceParams params;
	
	
	@Inject
	public SerialDeviceJssC(SerialJsscDeviceParams params) {
		this.params = params;
		sem = new Semaphore(0);
		queue = new ConcurrentLinkedQueue<>();
	}
	
	@Override
	public boolean open() throws SerialException {
		queue.clear();
	
		try {
			logger.fine("obteniendo puerto seriel");
			
			serialPort = params.getSerialPort();
	    	if (!serialPort.openPort()) {
	    		return false;
	    	}
	    	
	    	logger.fine("seteando parámetros de configuración");
	    	
	    	params.setParams(serialPort);
	    	
	    	
	    	int mask = SerialPort.MASK_RXCHAR;
	    	serialPort.setEventsMask(mask);
	    	serialPort.addEventListener(new SerialPortEventListener() {
				@Override
				public void serialEvent(SerialPortEvent event) {
					if (event.isRXCHAR()) {
						int bytes = event.getEventValue();
						
						if (bytes > 0) {
							
							try {
								byte[] data = serialPort.readBytes(bytes);

								logger.finest("bytes leidos : " + Utils.getHex(data));
								
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
					
					logger.finest("Tamaño de la cola de bytes leídos : " + queue.size());
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
			logger.finest("escribiendo bytes : " + Utils.getHex(data));
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
	
	@Override
	public void clearBuffer() throws SerialException {
		try {
			int i = serialPort.getInputBufferBytesCount();
			if (i > 0) {
				serialPort.readBytes();
			}
			serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);

			queue.clear();
			
		} catch (Exception e) {
			
		}
	}
	
}
