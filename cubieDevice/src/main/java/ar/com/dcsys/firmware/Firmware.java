package ar.com.dcsys.firmware;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.model.Identify;
import ar.com.dcsys.firmware.model.Response;

@Singleton
public class Firmware {
	
	private final Logger logger;
	private final LinkedBlockingQueue<Runnable> commands = new LinkedBlockingQueue<Runnable>();
	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final Identify identify;
	private volatile boolean end = false;	
	

	public void setEnd() {
		addCommand(new Runnable() {
			@Override
			public void run() {
				end = true;
			}
		});
	}
		
	public void addCommand(Runnable ... r) {
		commands.addAll(Arrays.asList(r));
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
			
			if (text.startsWith("OK") || text.startsWith("ERROR")) {
				MutualExclusion.using[MutualExclusion.EXECUTING_COMMAND].release();
			}
			
		}
	};
	
	
	public void processCommands() {
		
    	while (!end) {
    		
    		try {
				if (commands.isEmpty()) {
					commands.add(new Runnable() {
						@Override
						public void run() {
							identify.execute("", remote);
						}
					});
				}
    
    			Runnable r = commands.take();
    			r.run();
	    			
			} catch (Exception e) {
				logger.log(Level.INFO,e.getMessage(),e);
				e.printStackTrace();
			}
    	}
	}	
	
	
}
