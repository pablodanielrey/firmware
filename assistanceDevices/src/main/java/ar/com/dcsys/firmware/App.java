package ar.com.dcsys.firmware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.CmdResult;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.cmd.TestConnection;
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
    	try {
    		SerialDevice sd = new SerialDeviceJssC();
    		if (!sd.open()) {
    			return;
    		}
	    	
	    	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    	
	    	Identify identify = new Identify();
	    	TestConnection testConnection = new TestConnection();
	    	
	    	boolean end = false;
	    	while (!end) {
	    		
	    		try {
	    			
	    			identify.execute(sd, new CmdResult() {
						
						@Override
						public void onSuccess(byte[] data) {
							System.out.println("Datos : " + Utils.getHex(data));
						}
						
						@Override
						public void onSuccess(int i) {
							System.out.println("Huella identificada : " + String.valueOf(i));
						}
						
						@Override
						public void onSuccess() {
							System.out.println("No se pudo identificar la huella");
						}
						
						@Override
						public void onFailure() {
							System.out.println("Error ejecutando el comando");
						}
					});
	    			
	    			
					
					System.out.println("Escriba n o N para salir. si no cualquier otro caracter o enter");
		    		String line = in.readLine();
		    		if (line.trim().equals("n") || line.trim().equals("N")) {
		    			end = true;
		    		}
		    		
	    		} catch (CmdException e) {
	    			e.printStackTrace();
	    		}
	    	}
	    	
	    	sd.close();
	    	
	    	
    	} catch (SerialException | IOException e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}
    	
    }
}
