package ar.com.dcsys.firmware;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.TestConnection;
import ar.com.dcsys.firmware.cmd.TestConnection.TestConnectionResult;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

@Singleton
public class Firmware {
	
	private static Logger logger = Logger.getLogger(Firmware.class.getName());
	
	private final SerialDevice sd;
	private final Identifier identifier;
	private final Enroller enroller;
	private final TestConnection test;
	private final KeyboardReader reader;
	
	@Inject
	public Firmware(SerialDevice sd, Identifier identifier, Enroller enroller, TestConnection test) {
		logger.info("Inicializando Firmware");
		
		this.sd = sd;
		this.identifier = identifier;
		this.enroller = enroller;
		this.test = test;
		this.reader = new KeyboardReader();
	}
	
	public void run() throws CmdException {
		
		logger.info("Ejecutando Firmware");

    	try {
    		if (!sd.open()) {
    			return;
    		}

    		// le doy un tiempito al serie a que se estabilice.
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		try {
    			
    			test.execute(sd, new TestConnectionResult() {
					@Override
					public void onSuccess() {
						enroller.run();
					}
					
					@Override
					public void onFailure() {
						logger.log(Level.SEVERE,"Falla testeando la conexión con el lector de huellas");
					}
				});
    			
    			
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
    			
    			
    			
	    		
    		} finally {
    			sd.close();
    		}
	    	
    	} catch (SerialException e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}		
	
	}
	
}
