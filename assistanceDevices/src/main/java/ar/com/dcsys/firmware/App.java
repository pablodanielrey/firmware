package ar.com.dcsys.firmware;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import ar.com.dcsys.firmware.cmd.CmdException;



/**
 * Aplicación principal del proyecto para el firmware del reloj.
 *
 */

public class App {

    public static void main( String[] args ) {
    	Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
    	try {
	    	Firmware firmware = container.instance().select(Firmware.class).get();
	    	firmware.run();
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    		
    	} finally {
    		weld.shutdown();
    	}
    	
    }
}
