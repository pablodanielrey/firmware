package ar.com.dcsys.firmware;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Identify;
import ar.com.dcsys.firmware.model.Response;

@Singleton
public class Firmware {
	
	private final Logger logger;
	private final LinkedBlockingQueue<Cmd> commands = new LinkedBlockingQueue<Cmd>();
	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final Identify identify;
	private volatile boolean end = false;	
	
	
	/**
	 * Tarea que genera los identify en el caso que la cola de comandos este vacía.
	 */
	private final TimerTask generator = new TimerTask() {
		@Override
		public void run() {
			
			if (!runningCommand.tryAcquire()) {
				return;
			}

			try {
				// chequeo que exista por lo menos 5 segundos despues del ultimo reset.
				long lastResetLong = 0l;
				lastResetAccess.acquireUninterruptibly();
				try {
					lastResetLong = lastReset.getTime();
				} finally {
					lastResetAccess.release();
				}
				
				long currentTime = (new Date()).getTime();
				if (lastResetLong + 5000l > currentTime) {
					return;
				}
				
				// genero el comando en el caso de que no exista comando en espera.
				
				if (commands.isEmpty()) {
					/*
					commands.add(new Runnable() {
						@Override
						public void run() {
							identify.execute("", remote);
						}
					});
					*/
				}
				
			} finally {
				runningCommand.release();
			}
		}
	};
	
	private final Semaphore commandsAvailable = new Semaphore(0);
	private final Semaphore runningCommand = new Semaphore(1);
	private final Semaphore lastResetAccess = new Semaphore(1);
	private Date lastReset = new Date();
	
	
	/**
	 * Resetea el timer para la generación de los identify.
	 */
	public void resetTimer() {
		lastResetAccess.acquireUninterruptibly();
		try {
			lastReset = new Date();
		} finally {
			lastResetAccess.release();
		}
	}
	
	public void addCommand(Cmd ... r) {
		commands.addAll(Arrays.asList(r));
		commandsAvailable.release(r.length);
	}
	
	@Inject
	public Firmware(Logger logger, Identify identify) {
		this.logger = logger;
		this.identify = identify;
	}
	
	
	private final Response remote = new Response() {
		@Override
		public void sendText(String text) throws IOException {
			logger.log(Level.INFO, text);
		}
	};
	
	
	private Cmd runningCmd;
	
	public Cmd getRunningCommand() {
		return runningCmd;
	}
	
	
	
	public void processCommands() {
		
		// genero comandos espaciados por 5 segundos cada uno.
//		Timer t = new Timer();
//		t.schedule(generator, 10000l, 5000l);
		
    	while (!end) {
    		
    		try {
    			commandsAvailable.tryAcquire(5000l, TimeUnit.MILLISECONDS);
    			runningCommand.acquireUninterruptibly();
    			try {
    				runningCmd = commands.poll();
    			
	    			if (runningCmd != null) {
		    			logger.info("iniciando comando : " + runningCmd.getCommand());
		   				runningCmd.execute();
		    			logger.info("comando finalizado : " + runningCmd.getCommand());
	    			}
	    			
    			} finally {
    				runningCommand.release();
    			}
	    			
			} catch (Exception e) {
				logger.log(Level.INFO,e.getMessage(),e);
				e.printStackTrace();
			}
    	}
	}	
	
	
}
