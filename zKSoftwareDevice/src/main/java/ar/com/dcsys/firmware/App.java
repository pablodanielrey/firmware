package ar.com.dcsys.firmware;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.DeploymentException;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import ar.com.dcsys.firmware.model.Model;
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
	
	private final Firmware firmware;
	private final Model model;
	private final Announcer announcer;
	private final WebsocketServer websocketServer;
	
	@Inject
	public App(Firmware firmware, Model model, Announcer announcer, WebsocketServer websocketServer) {
		this.firmware = firmware;
		this.model = model;
		this.announcer = announcer;
		this.websocketServer = websocketServer;
	}
	
	private void initializeWebsockets() {
		try {
			websocketServer.start();
		} catch (DeploymentException e1) {
			e1.printStackTrace();System.exit(1);
		}
	}
	
	@PostConstruct
	private void initialize() {
		initializeWebsockets();
	}
	
	private void shutdown() {
		websocketServer.stop();
	}
	
	public static WeldContainer getWeldContainer() {
		if (container == null) {
			weld = new Weld();
			container = weld.initialize();
		}
		return container;
	}
	
	private void processCommands() {
		firmware.processCommands();
	}
	
	public static void main(String[] args) {
		
		//inicializo weld
		getWeldContainer();
		try {
			//obtengo una instancia de la app para correr el firmware
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
