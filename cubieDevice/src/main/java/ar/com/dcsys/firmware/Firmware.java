package ar.com.dcsys.firmware;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.cmd.Identify.IdentifyResult;
import ar.com.dcsys.firmware.cmd.TestConnection;
import ar.com.dcsys.firmware.cmd.TestConnection.TestConnectionResult;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

@Singleton
public class Firmware {
	
	private static Logger logger = Logger.getLogger(Firmware.class.getName());
	
	private final SerialDevice sd;
	private final TestConnection test;
	private final Identify identify;

	
	@Inject
	public Firmware(SerialDevice sd, TestConnection test, Identify identify) {
		logger.info("Inicializando Firmware");
		this.sd = sd;
		this.test = test;
		this.identify = identify;
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
						logger.log(Level.INFO, "Test ok");
						/*
						try {
							sd.close();
						} catch (SerialException e) {
							e.printStackTrace();
						}
						*/
					}
					
					@Override
					public void onFailure() {
						logger.log(Level.SEVERE,"Falla testeando la conexión con el lector de huellas");
					}
				});
    			
    			
    			identify.execute(sd, new IdentifyResult() {
					
					@Override
					public void releaseFinger() {
						logger.info("levantar el dedo");
					}
					
					@Override
					public void onSuccess(int fpNumber) {
				
						logger.info("huella ok número : " + String.valueOf(fpNumber));
					}
					
					@Override
					public void onNotFound() {
						logger.info("huella no encontrada");
					}
					
					@Override
					public void onFailure(int errorCode) {
						logger.info("Error : " + String.valueOf(errorCode));
					}
					
					@Override
					public void onCancel() {
						logger.info("Identificación Cancelada");
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
