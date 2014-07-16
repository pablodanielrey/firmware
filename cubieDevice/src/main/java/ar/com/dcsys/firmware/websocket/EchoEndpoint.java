package ar.com.dcsys.firmware.websocket;

import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.cmd.Identify.IdentifyResult;
import ar.com.dcsys.firmware.serial.SerialDevice;

@ServerEndpoint(value="/echo", configurator=WebsocketConfigurator.class)
public class EchoEndpoint {

	private static final Logger logger = Logger.getLogger(EchoEndpoint.class.getName());
	
	private final SerialDevice sd;
	private final Identify identify;
	
	@Inject
	public EchoEndpoint(SerialDevice sd, Identify i) {
		this.sd = sd;
		identify = i;
	}
	
	
	@OnOpen
	public void onOpen(Session s, EndpointConfig config) throws IOException {
		logger.fine("onOpen");
	}
	
	@OnMessage
	public void onMessage(String m, final Session session) {
		logger.fine("Mensaje recibido : " + m);
		
		if ("identify".equals(m)) {
			logger.fine("Mensaje de identificación");
			
			final RemoteEndpoint.Basic remote = session.getBasicRemote();
			try {
				identify.execute(sd, new IdentifyResult() {
					
					@Override
					public void releaseFinger() {
						try {
							remote.sendText("liberar dedo");
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onSuccess(int fpNumber) {
						try {
							remote.sendText("huella encontrada : " + String.valueOf(fpNumber));
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onNotFound() {
						try {
							remote.sendText("huella no encontrada");
						
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onFailure(int errorCode) {
						try {
							remote.sendText("Error : " + String.valueOf(errorCode));
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onCancel() {
						try {
							remote.sendText("identificación cancelada");
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (CmdException e) {
				try {
					remote.sendText(e.getMessage());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
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
