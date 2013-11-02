package ar.com.dcsys.firmware;

import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialDeviceJssC;
import ar.com.dcsys.firmware.serial.SerialException;

/**
 * Aplicación principal del proyecto para el firmware del reloj.
 *
 */
public class App 
{

    public static void main( String[] args )
    {
    	
    	System.out.println("Inicializando Sistema Control de Asistencia");
    	
    	try {
    		SerialDevice sd = new SerialDeviceJssC();
    		if (!sd.open()) {
    			return;
    		}
	    	

    		Identifier identifier = new Identifier(sd);
    		Thread tidentifier = new Thread(identifier);
    		tidentifier.start();
    		
    		KeyboardReader reader = new KeyboardReader();
    		Thread tkeyboardReader = new Thread(reader);
    		tkeyboardReader.start();
    		
    		try {
				tidentifier.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
	    	sd.close();
	    	
	    	
    	} catch (SerialException e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}
    	
    }
}
