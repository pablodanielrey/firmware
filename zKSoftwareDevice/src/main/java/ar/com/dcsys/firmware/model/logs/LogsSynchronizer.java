package ar.com.dcsys.firmware.model.logs;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ar.com.dcsys.exceptions.AttLogException;
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
			sleep = 24l * 1000l * 60l * 60l; 		// 24 horas.
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
		final List<String> logs = new ArrayList<String>();
		final Map<String,List<String>> logsToSync = new HashMap<String,List<String>>();
		final List<Session> sessions = new ArrayList<Session>();
		
		GenericWebsocketClient gwc = new GenericWebsocketClient(new WebsocketClient() {
			@Override
			public void onOpen(final Session session, EndpointConfig config) {
				try {
					sessions.add(session);
					String sid = session.getId();
					List<String> sl = Collections.synchronizedList(new ArrayList<String>(logs));
					logsToSync.put(sid,sl);
					
					for (String id : sl) {
						AttLog log = attLogsManager.findById(id);
						String json = attLogSerializer.toJson(log);
						String cmdJson = "attLog;" + json;
	
						logger.info(cmdJson);
						session.getBasicRemote().sendText(cmdJson);
					}

				} catch (Exception e) {
					logger.log(Level.SEVERE, e.getMessage(),e);
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
				
				String sid = session.getId();
				List<String> sl = logsToSync.get(sid);
				
				String cmdDelete = "OK;delete;";
				if (m.startsWith(cmdDelete)) {
					
					String id = m.substring(cmdDelete.length());
					try {
						attLogsManager.remove(id);
						if (sl != null) {
							sl.remove(id);
						}

						if (sl == null || sl.size() <= 0) {
							sem.release();
						}
						
					} catch (AttLogException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						sem.release();
					}
				}
			}
		});
		
		final URI uri = URI.create("ws://" + serverData.getIp() + ":" + serverData.getPort() + serverData.getUrl());
		
		while (true) {
			
			// busco a ver si existen logs a ser sincronizados.
			logs.clear();
			try {
				List<String> ls = attLogsManager.findAll();
				
				if (ls != null && ls.size() > 0) {
					
					logs.addAll(ls);	
					sem.drainPermits();
					
					logger.info("connecting to : " + uri.toString());
					ContainerProvider.getWebSocketContainer().connectToServer(gwc, uri);
					
					try {
						sem.acquire();
						
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}					
					
				}
				
				
			} catch (DeploymentException | AttLogException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);

			} catch (IOException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			}

			
			
			// limpio todas las sesiones que hayan quedado habiertas.
			for (Session s : sessions) {
				try {
					if (s.isOpen()) {
						s.close();
					}
				} catch (IOException e) {
				}
			}
			sessions.clear();
			logsToSync.clear();
			logs.clear();
			
			try {
				// espero a que alguien marque o hasta que se cumpla el sleep.
				MutualExclusion.using[MutualExclusion.NEED_ATTLOGS_SYNC].tryAcquire(sleep, TimeUnit.MILLISECONDS);
				
			} catch (InterruptedException e) {

			}
			
		}
	}
	
}
