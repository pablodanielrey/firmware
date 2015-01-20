package ar.com.dcsys.firmware.sound;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import ar.com.dcsys.model.device.GenericWebsocketClient;
import ar.com.dcsys.model.device.GenericWebsocketClient.WebsocketClient;

@Singleton
public class TTSPlayer {

	private static final Logger logger = Logger.getLogger(TTSPlayer.class.getName());
	
	private final TTSPlayerData serverData;
	private final Boolean enable;
	
	@Inject
	public TTSPlayer(TTSPlayerData serverData) {
		this.serverData = serverData;
		this.enable = serverData.getEnableSound();
	}
	
	/**
	 * Se conecta mediante websocket al servidor de text-to-speak y le envía el texto a reproducir
	 * @param textToSay
	 */
	public void say(final String textToSay) {
		
		if (!(this.enable)) {
			return;
		}
		
		GenericWebsocketClient gwc = new GenericWebsocketClient(new WebsocketClient() {
			
			@Override
			public void onOpen(final Session s, EndpointConfig config) {
				try {
					// armo el json que entiende el servidor. protocolo 0.0.0.1 
					String msg = "{\"say\":\"" + textToSay + "\"}";
					
					s.getBasicRemote().sendText(textToSay);
					
				} catch (IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
				}
			}
			
			@Override
			public void onMessage(String m, Session s) {
				// responde {"ok":""} o algun error.
				try {
					s.close();

				} catch (IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
				}
			}
			
			@Override
			public void onClose(Session s, CloseReason reason) {
				// nada lo ignoro por ahora ya que no me importa.
			}
		});
		
		final URI uri = URI.create(serverData.getProto() + "://" + serverData.getIp() + ":" + serverData.getPort() + serverData.getUrl());
		logger.info("connecting to : " + uri.toString());
		try {
			ContainerProvider.getWebSocketContainer().connectToServer(gwc, uri);
			
		} catch (DeploymentException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);


		} catch (IOException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
		
	}
	
	
	
}
