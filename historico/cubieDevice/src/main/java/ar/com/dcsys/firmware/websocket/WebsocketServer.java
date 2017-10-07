package ar.com.dcsys.firmware.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;

@Singleton
public class WebsocketServer {

	private final Server server;
	
	@Inject
	public WebsocketServer(WebsocketServerData wsd) {
		String ip = wsd.getIp();
		int port = wsd.getPort();
		String url = wsd.getUrl();
		
    	server = new Server(ip,port,url, null, CommandsEndpoint.class);
	}
	
	public void start() throws DeploymentException {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
	
}
