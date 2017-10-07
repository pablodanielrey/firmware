package ar.com.dcsys.firmware.websocket;

import javax.inject.Inject;
import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;

public class WebsocketServer {
	
	private final Server server;
	
	@Inject
	public WebsocketServer() {
		server = new Server("localhost",8025, "/websocket", null, CommandsEndpoint.class);
	}
	
	public void start() throws DeploymentException {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
}
