package ar.com.dcsys.firmware;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

@Singleton
public class Firmware {
	
	private static Logger logger = Logger.getLogger(Firmware.class.getName());
	
	private final SerialDevice sd;
	private final Identifier identifier;
	private final Enroller enroller;
	private final KeyboardReader reader;
	
	@Inject
	public Firmware(SerialDevice sd, Identifier identifier, Enroller enroller) {
		logger.info("Inicializando Firmware");
		
		this.sd = sd;
		this.identifier = identifier;
		this.enroller = enroller;
		this.reader = new KeyboardReader();
	}
	
	public void run() {
		logger.info("Ejecutando Firmware");

    	try {
    		if (!sd.open()) {
    			return;
    		}
    		try {
    			
    			/*
	    		Thread tidentifier = new Thread(identifier);
	    		tidentifier.start();
	    		
	    		Thread tkeyboardReader = new Thread(reader);
	    		tkeyboardReader.start();
	    		
	    		// espero a que el thread del teclado decida finalizar.
	    		
	    		try {
					tkeyboardReader.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    		
	    		while (true) {
		    		try {
		    			identifier.terminate();
		    			break;
		    		} catch (CmdException e) {
		    			// ocurrio una exception asi que no se ejecuto bien el comando terminate.
		    		}
	    		}
	    		*/
    			
    			enroller.run();
    			
	    		
    		} finally {
    			sd.close();
    		}
	    	
    	} catch (SerialException e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}		
	
	}
	
}
