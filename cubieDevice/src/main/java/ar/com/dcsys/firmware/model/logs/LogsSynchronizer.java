package ar.com.dcsys.firmware.model.logs;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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

import ar.com.dcsys.assistance.server.AttLogSerializer;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.database.ServerData;
import ar.com.dcsys.model.device.GenericWebsocketClient;
import ar.com.dcsys.model.device.GenericWebsocketClient.WebsocketClient;
import ar.com.dcsys.model.log.AttLogsManager;

@Singleton
public class LogsSynchronizer implements Runnable {

	private static final Logger logger = Logger.getLogger(LogsSynchronizer.class.getName());
	
	private final ServerData serverData;
	private final AttLogSerializer attLogSerializer;
	private final AttLogsManager attLogsManager;

	private long sleep;
	
	@Inject
	public LogsSynchronizer(AttLogSerializer attLogSerializer, 
							AttLogsManager attLogsManager, 
							ServerData serverData, 
							LogsSynchronizerData logsSynchronizerData) {
		
		this.serverData = serverData;
		this.attLogSerializer = attLogSerializer;
		this.attLogsManager = attLogsManager;
		
		try {
			sleep = Long.parseLong(logsSynchronizerData.getSleep());
			
		} catch (Exception e) {
			sleep = 3800000l;
		}
	}

	
	
	@PostConstruct
	private void initialize() {
	
		Thread t = new Thread(this);
		t.start();
		
	}
	
	
	@Override
	public void run() {

		final Semaphore sem = new Semaphore(0);
		
		GenericWebsocketClient gwc = new GenericWebsocketClient(new WebsocketClient() {
			@Override
			public void onOpen(final Session session, EndpointConfig config) {
				
				try {
					List<String> logsToSync = attLogsManager.findAll();
					for (String id : logsToSync) {
					
						AttLog log = attLogsManager.findById(id);
						String json = attLogSerializer.toJson(log);
						String cmdJson = "attLog;" + json;
	
						logger.info(cmdJson);
						session.getBasicRemote().sendText(cmdJson);
					}

				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(),e);
				}
				
				
				try {
					session.close();
					
				} catch (IOException e1) {
					logger.log(Level.SEVERE, e1.getMessage(),e1);
					sem.release();
					
				}
					
			}
			@Override
			public void onClose(Session s, CloseReason reason) {
				logger.info("onClose");
				sem.release();
			}
			@Override
			public void onMessage(String m, Session session) {
				logger.info("onMessage " + m);
			}
		});
		
		final URI uri = URI.create("ws://" + serverData.getIp() + ":" + serverData.getPort() + serverData.getUrl());
		
		while (true) {
			
			try {
				logger.info("connecting to : " + uri.toString());
				ContainerProvider.getWebSocketContainer().connectToServer(gwc, uri);
				
				try {
					int permits = sem.drainPermits();
					if (permits == 0) {
						sem.acquire();
					}
					
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
				// espero a que alguien marque o hasta que se cumpla el sleep.
				MutualExclusion.using[MutualExclusion.NEED_ATTLOGS_SYNC].tryAcquire(sleep, TimeUnit.MILLISECONDS);
				
			} catch (InterruptedException e) {

			}
			
		}
	}
	
}
