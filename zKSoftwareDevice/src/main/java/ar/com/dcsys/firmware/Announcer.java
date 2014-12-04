package ar.com.dcsys.firmware;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import ar.com.dcsys.assistance.server.DeviceSerializer;
import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.database.Initialize;
import ar.com.dcsys.firmware.database.ServerData;
import ar.com.dcsys.model.device.GenericWebsocketClient;
import ar.com.dcsys.model.device.GenericWebsocketClient.WebsocketClient;

@Singleton
public class Announcer implements Runnable {
	
	private static final Logger logger = Logger.getLogger(Announcer.class.getName());
	private final DeviceSerializer deviceSerializer;
	private final Initialize initialize;
	private final ServerData serverData;
	
	
	private long interConnectionSleep;
	private long intraConnectionSleep;
	private long backOffMilis;

	
	@Inject
	public Announcer(DeviceSerializer deviceSerializer, Initialize initialize, ServerData serverData, AnnouncerData announcerData) {
		this.deviceSerializer = deviceSerializer;
		this.initialize = initialize;
		this.serverData = serverData;
		
		try {
			interConnectionSleep = Long.parseLong(announcerData.getInterConnectionSleep());
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			interConnectionSleep = 60000l;
		}
		
		try {
			intraConnectionSleep = Long.parseLong(announcerData.getIntraConnectionSleep());
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			intraConnectionSleep = 60000l;
		}
		
		try {
			backOffMilis = Long.parseLong(announcerData.getBackOffMilis());
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			backOffMilis = 60000l;
		}
	}
	
	
	@PostConstruct
	private void initialize() {
		
		//si no esta inicializado, trato de inicializar el dispositivo
		try {
			Device device = initialize.getCurrentDevice();
			if (device == null) {
				initialize.execute();
			}
		} catch (DeviceException | CmdException e) {
			e.printStackTrace();
		}
		
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	public void run() {
		
		final Semaphore sem = new Semaphore(0);
		
		GenericWebsocketClient gwc = new GenericWebsocketClient(new WebsocketClient() {

			@Override
			public void onOpen(Session session, EndpointConfig config) {
				
				Device device;
				try {
					device = initialize.getCurrentDevice();
					if (device != null) {
						String json = deviceSerializer.toJson(device);
						String cmdJson = "device;" + json;
						
						logger.info(cmdJson);
						session.getBasicRemote().sendText(cmdJson);
					}
				} catch (DeviceException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
				} catch (IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					onMessage("OK", session);
				}
			}

			@Override
			public void onMessage(String m, Session session) {
				logger.info("onMessage " + m);
				try {
					session.close();
				} catch (IOException e) {					
				} finally {
					sem.release();
				}
			}

			@Override
			public void onClose(Session s, CloseReason reason) {
				logger.info("onClose");
				sem.release();
			}
			
		});
		
		final URI uri = URI.create("ws://" + serverData.getIp() + ":" + serverData.getPort() + serverData.getUrl());
		
		long currentSleep = interConnectionSleep;
		
		while (true) {
			
			try {
				logger.info("connecting to : " + uri.toString());
				ContainerProvider.getWebSocketContainer().connectToServer(gwc, uri);
				
				try {
					sem.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (DeploymentException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			} catch (IOException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			}
			
			try {
				Thread.sleep(currentSleep);
				currentSleep = currentSleep + backOffMilis;
			} catch (InterruptedException e) {
				
			}
		}
	}
}
