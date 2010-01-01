package ar.com.dcsys.firmware.serial;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
	private final Semaphore okToRead;
	private final Semaphore reading;
	private final SerialJsscDeviceParams params;
	
	private final SerialPortEventListener serialPortReader = new SerialPortEventListener() {
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
			okToRead.release();
			
			logger.finest("Tamaño de la cola de bytes leídos : " + queue.size());
		}		
	};
	
	
	
	@Inject
	public SerialDeviceJssC(SerialJsscDeviceParams params) {
		this.params = params;
		okToRead = new Semaphore(0);
		reading = new Semaphore(1);
		queue = new ConcurrentLinkedQueue<>();
	}
	
	@Override
	public synchronized boolean open() throws SerialException {
		queue.clear();
	
		try {
			logger.fine("obteniendo puerto seriel");
			
			serialPort = params.getSerialPort();
			if (serialPort.isOpened()) {
				return true;
			}
			
	    	if (!serialPort.openPort()) {
	    		return false;
	    	}
	    	
//	    	logger.fine("seteando parámetros de configuración");
	    	
//	    	params.setParams(serialPort);
	    	
	    	
	    	int mask = SerialPort.MASK_RXCHAR;
	    	
	    	serialPort.setEventsMask(mask);
	    	serialPort.addEventListener(serialPortReader);    	
	    	
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
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new SerialException(e);
		}
		
		queue.clear();
	}
	
	@Override
	public void writeBytes(byte[] data) throws SerialException {
		try {
			logger.finest("escribiendo bytes : " + Utils.getHex(data));
			if (!serialPort.writeBytes(data)) {
				logger.log(Level.SEVERE, "No se pudo escribir en el puerto. writeBytes == false");
				throw new SerialException("Escritura fallida");
			}
		} catch (SerialPortException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new SerialException(e);
		}
	}
	
	
	private boolean canceled = false;
	
	@Override
	public void cancel() {
		if (reading.tryAcquire()) {
			// no hay nadie leyendo asi que no se cancela nada.
			reading.release();
			return;
		}
		canceled = true;
	}
	
	@Override
	public boolean isReading() {
		int permits = reading.availablePermits();
		return (permits == 0);
	}
	
	@Override
	public byte[] readBytes(int count) {
		reading.acquireUninterruptibly();
		try {
			while (queue.size() < count) {
				try {
					while (!okToRead.tryAcquire(1000l, TimeUnit.MILLISECONDS)) {

						if (canceled) {
							canceled = false;
							int size = queue.size();
							if (size <= 0) {
								return new byte[0];
							} else {
								byte[] data = new byte[queue.size()];
								for (int i = 0; i < size; i++) {
									data[i] = queue.remove();
								}
								return data;
							}
							
						}
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			byte[] rd = new byte[count];
			for (int i = 0; i < count; i++) {
				rd[i] = queue.remove();
			}
			return rd;
			
		} finally {
			reading.release();
		}
	}
	
	@Override
	public void clearBuffer() throws SerialException {
		try {
			int i = serialPort.getInputBufferBytesCount();
			if (i > 0) {
				serialPort.readBytes();
			}
			serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);

		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			
		} finally {
			queue.clear();
		}
	}
	
}
