package ar.com.dcsys.firmware;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.logging.SqlHandler;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Identify;
import ar.com.dcsys.firmware.model.Response;

@Singleton
public class Firmware {
	
	private final Logger logger = Logger.getLogger(Firmware.class.getName());
	private final LinkedBlockingQueue<Cmd> commands = new LinkedBlockingQueue<Cmd>();
	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final Provider<Identify> identify;
	private volatile boolean end = false;	
	
	private final Semaphore commandsAvailable = new Semaphore(0);
	private final Semaphore runningCommand = new Semaphore(1);
	private final Semaphore lastResetAccess = new Semaphore(1);
	private Date lastReset = new Date();
	
	
	public void addCommand(Cmd ... r) {
		if (r == null) {
			logger.log(Level.FINE,"cmds == null");
			return;
		}
		List<Cmd> cmds = Arrays.asList(r);
		for (Cmd c : cmds) {
			logger.log(Level.FINE, "Agregando comando a la cola : " + c.toString());
		}	
		commands.addAll(cmds);
		logger.log(Level.FINE,"liberando " + r.length + " perms");
		commandsAvailable.release(r.length);
	}
	
	@Inject
	public Firmware(Provider<Identify> identify, SqlHandler sqlLogging) {
		this.identify = identify;
		logger.addHandler(sqlLogging);
	}
	
	
	private final Response remote = new Response() {
		@Override
		public void sendText(String text) throws IOException {
			logger.log(Level.INFO, text);
		}
	};
	
	
	private volatile boolean generatingIdentify = false;
	private Cmd runningCmd;
	
	public Cmd getRunningCommand() {
		if (runningCmd == null) {
			logger.log(Level.FINE, "Comando ejecutandose : null");
			return null;
		}
		logger.log(Level.FINE, "Comando ejecutandose : " + runningCmd.toString());
		return runningCmd;
	}
	
	
	/**
	 * Genera un delay para crear los identify cuando no existen comandos en la cola.
	 */
	private final Runnable gen = new Runnable() {
		
		private final int end = 2;
		
		@Override
		public void run() {
			
			generatingIdentify = true;
			try {
			
				int count = 1;
				while (runningCmd == null && count < end) {
					try {
						Thread.sleep(1000l);
						count++;
					} catch (InterruptedException e) {
					}
					
				}
				
				if (runningCmd == null) {
					logger.info("Generando identify ya que no existe comando pendiente");
					Identify i = identify.get();
					i.setResponse(remote);
					addCommand(i);
				}
			} finally {
				generatingIdentify = false;
			}
			
		}
	};
	
	
	public void processCommands() {
		
    	while (!end) {
    		
    		try {
    			commandsAvailable.tryAcquire(1000l, TimeUnit.MILLISECONDS);
    			runningCommand.acquireUninterruptibly();
    			try {
    				runningCmd = commands.poll();
    			
	    			if (runningCmd != null) {
	    				
		    			logger.info("iniciando comando : " + runningCmd.getCommand());
		   				runningCmd.execute();
		    			logger.info("comando finalizado : " + runningCmd.getCommand());
		    			
	    			} else if (!generatingIdentify) {
	    				executor.execute(gen);
	    				Thread.yield();
	    			}
	    			
    			} catch (Exception e1) {
    				logger.log(Level.SEVERE,e1.getMessage(),e1);
    				
    			} finally {
    				runningCmd = null;
    				runningCommand.release();
    			}
	    			
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			}
    	}
	}	
	
	
}
