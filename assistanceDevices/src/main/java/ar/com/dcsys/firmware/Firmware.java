package ar.com.dcsys.firmware;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

@Singleton
public class Firmware {
	
	private static Logger logger = Logger.getLogger(Firmware.class.getName());
	
	private final SerialDevice sd;
	private final Identifier identifier;
	private final KeyboardReader reader;
	
	@Inject
	public Firmware(SerialDevice sd) {
		logger.info("Inicializando Firmware");
		
		this.sd = sd;
		this.identifier = new Identifier(sd);
		this.reader = new KeyboardReader();
	}
	
	public void run() {
		logger.info("Ejecutando Firmware");

    	try {
    		if (!sd.open()) {
    			return;
    		}
    		try {
	    	
	    		Thread tidentifier = new Thread(identifier);
	    		tidentifier.start();
	    		
	    		Thread tkeyboardReader = new Thread(reader);
	    		tkeyboardReader.start();
	    		
	    		try {
					tkeyboardReader.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		
	    		
	    		identifier.terminate();
	    		try {
					tidentifier.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    		
    		} finally {
    			sd.close();
    		}
	    	
    	} catch (SerialException e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}		
	
	}
	
}
