package ar.com.dcsys.firmware;

import java.util.concurrent.LinkedBlockingQueue;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;
import ar.com.dcsys.firmware.websocket.CommandsEndpoint;



/**
 * Aplicación principal del proyecto para el firmware del reloj.
 *
 */

public class App {
	
	private static Weld weld = null;
	
	public static Weld getWeld() {
		if (weld == null) {
			weld = new Weld();
			container = weld.initialize();
		}
		return weld;
	}
	
	private static WeldContainer container = null;
	
	public static WeldContainer getWeldContainer() {
		if (container == null) {
			getWeld();
		}
		return container;
	}
	
	private static volatile boolean end = false;
	
	public static void setEnd() {
		end = true;
	}
	
	private static final LinkedBlockingQueue<Runnable> commands = new LinkedBlockingQueue<Runnable>();
	
	public static void addCommand(Runnable r) {
		commands.add(r);
	}
	
    public static void main( String[] args ) {
    	
    	
    	// inicializo el puerto serie para comunicarme con el lector.
    	
    	SerialDevice sd = getWeldContainer().instance().select(SerialDevice.class).get();
		try {
			if (!sd.open()) {
				return;
			}
		} catch (SerialException e2) {
			e2.printStackTrace();
			System.exit(1);
		}

		// le doy un tiempito al serie a que se estabilice.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}    	
		
		
		// inicializo el server de websockets para obtener comandos desde el servidor remotamente.
		
    	
    	Server server = new Server("localhost",8025, "/websocket", null, CommandsEndpoint.class);
    	try {
			server.start();
		} catch (DeploymentException e1) {
			e1.printStackTrace();
		}
    	
    	while (!end) {
    		
    		try {
	    		Runnable r = commands.take();
	    		Thread t = new Thread(r);
	    		t.start();
	    		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	server.stop();
    	getWeld().shutdown();
    	
  /*  	
    	try {
	    	Firmware firmware = container.instance().select(Firmware.class).get();
	    	firmware.run();
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    		
    	} finally {
    		server.stop();
    		getWeld().shutdown();
    	}
    */
    }
}
