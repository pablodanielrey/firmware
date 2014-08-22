package ar.com.dcsys.firmware;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.DeploymentException;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Model;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;
import ar.com.dcsys.firmware.websocket.WebsocketServer;



/**
 * Aplicación principal del proyecto para el firmware del reloj.
 *
 */

@Singleton
public class App {
	
	private static final Logger logger = Logger.getLogger(App.class.getName());
	
	private static Weld weld = null;
	private static WeldContainer container = null;

	public static WeldContainer getWeldContainer() {
		if (container == null) {
			weld = new Weld();
			container = weld.initialize();
		}
		return container;
	}
	
	
	//private final DefaultCommandGenerator defaultCommandGenerator;
	private final Firmware firmware;
	private final Model model;
	private final Leds leds;
	private final WebsocketServer websocketServer;
	private final SerialDevice sd;
	private final Announcer announcer;
	
	

	private void initializeSerialDevice() {

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
	}
	
	private void initializeWebsockets() {
		
    	try {
    		websocketServer.start();
		} catch (DeploymentException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
    	
	}

	@PostConstruct
	private void initialize() {
		leds.onCommand(Leds.BLOCKED);
	
		initializeWebsockets();
		initializeSerialDevice();
		
		leds.onCommand(Leds.READY);
	}

	private void shutdown() {
		try {
			sd.close();
		} catch (SerialException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}		
		websocketServer.stop();
	}
	
	
	@Inject
	public App(Firmware firmware, Model model, Leds leds, WebsocketServer websocketServer, SerialDevice sd, Announcer announcer) {
		this.firmware = firmware;
		this.model = model;
		this.leds = leds;
		this.websocketServer = websocketServer;
		this.sd = sd;
		this.announcer = announcer;
	}
	
	
	private void processCommands() {
		firmware.processCommands();
	}

	
    public static void main( String[] args ) {
    	
    	// inicializo weld
    	getWeldContainer();
    	try {
	    	// obtengo una instancia de la app para correr el firmware.
	    	App app = App.container.instance().select(App.class).get();
	    	try {
	    		app.processCommands();
	    	} finally {
	    		app.shutdown();
	    	}
    	} finally {
    		App.weld.shutdown();
    	}
    	
    }
}
