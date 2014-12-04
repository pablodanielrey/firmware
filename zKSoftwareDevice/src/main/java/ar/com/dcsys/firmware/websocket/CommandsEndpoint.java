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
import ar.com.dcsys.firmware.model.Model;
import ar.com.dcsys.firmware.model.Response;

@ServerEndpoint(value="/cmd", configurator=WebsocketConfigurator.class)
public class CommandsEndpoint {
	
	private static final Logger logger = Logger.getLogger(CommandsEndpoint.class.getName());
	
	private final Firmware app;
	private final Model model;
	//private final Reader reader;
	
	@Inject
	public CommandsEndpoint(Firmware app,
							Model model) {
		this.app = app;
		this.model = model;
	}
	
	@OnOpen
	public void onOpen(Session s, EndpointConfig config) throws IOException {
		logger.fine("opOpen");
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
		
		if (m.startsWith("model;")) {
			String subcommand = m.substring(6);
			model.onCommand(subcommand, remote);
		} else if (m.startsWith("reader;")) {
			String subcommand = m.substring(7);
			//reader.onCommand(subcommand,remote);
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
