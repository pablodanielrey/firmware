package ar.com.dcsys.firmware.websocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Model;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.reader.Reader;

@ServerEndpoint(value="/cmd", configurator=WebsocketConfigurator.class)
public class CommandsEndpoint {

	private static final Logger logger = Logger.getLogger(CommandsEndpoint.class.getName());
	
	private final Firmware app;
	private final Leds leds;
	private final Model model;
	private final Reader reader;
	
	@Inject
	public CommandsEndpoint(Firmware app,
							Leds leds,
							Model model,
							Reader reader) {
		
		this.app = app;
		this.model = model;
		this.reader = reader;
		this.leds = leds;
		
	}
	
	
	@OnOpen
	public void onOpen(Session s, EndpointConfig config) throws IOException {
		logger.fine("onOpen");
	}
	
	
	@OnMessage
	public void onMessage(String m, final Session session) {
		
		logger.fine("Mensaje recibido : " + m);
		
		Response remote = new Response() {
			@Override
			public void sendText(String text) throws IOException {
				session.getBasicRemote().sendText(text);
			}
		};
		
		
		//// leds /////
		
		if (m.startsWith("leds;")) {
			
			String subcommand = m.substring(5);
			leds.onCommand(subcommand);
		

		// modelo ///
			
		} else if (m.startsWith("model;")) {
			
			String subcommand = m.substring(6);
			model.onCommand(subcommand,remote);
			
			
		//// lector ////
			
			
		} else if (m.startsWith("reader;")) {
			
			String subcommand = m.substring(7);
			reader.onCommand(subcommand,remote);
			
			
		//// base ////
			/*
			
		} else if ("initialize".equals(m)) {
			
			initializeDevice(remote);
			
		} else if ("enable".equals(m)) {
			
			setEnabled(true, remote);
			
		} else if ("disable".equals(m)) {
			
			setEnabled(false, remote);
			
			
			
		/// base y lector ////	
			
		} else if ("check".equals(m)) {
			
			checkIntegrity(remote);
			
		} else if ("regenerate".equals(m)) {
			
			consistency(remote);
			
			
		} else if ("purgeFingerprints".equals(m)) {
			
			purgeFingerprints(remote);				


		} else if (m.startsWith("persistFingerprint;")) {
			
			// extraigo la huella del comando.
			String cmd = "persistFingerprint;";
			String json = m.substring(cmd.length());
			FingerprintSerializer fps = new FingerprintSerializer();
			Fingerprint fp = fps.read(json);
			
			updateFingerprint(fp, remote);
			
			
		} else if ("summary".equals(m)) {
			
			summary(remote);
		
			
			
		/////// aplicacion ////////////	

			*/
		} else {
			
			try {
				remote.sendText("comando desconocido");
				
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		
		}

	}
	
	@OnError
	public void onError(Throwable t) {
	}

	@OnClose
	public void onClose(Session s, CloseReason reason) {
		logger.fine("onClose");
	}
}
