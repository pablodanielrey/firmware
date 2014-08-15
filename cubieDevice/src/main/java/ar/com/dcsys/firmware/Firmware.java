package ar.com.dcsys.firmware;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Firmware {
	
	private final Logger logger;
	private final LinkedBlockingQueue<Runnable> commands = new LinkedBlockingQueue<Runnable>();
	private final ExecutorService executor = Executors.newCachedThreadPool();

	private volatile boolean end = false;	
	

	public void setEnd() {
		addCommand(new Runnable() {
			@Override
			public void run() {
				end = true;
			}
		});
	}
		
	public void addCommand(Runnable r) {
		commands.add(r);
	}
	
	@Inject
	public Firmware(Logger logger) {
		this.logger = logger;
	}
	
	
	public void processCommands() {
		
    	while (!end) {
    		
    		try {
    
	 /*   			
	    			// ejecuto el comando usando un Callable asi puedo esperar al fin del thread.
		    		final Runnable r = commands.take();
		    		Callable<Void> c = new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							r.run();
							return null;
						}
		    		};
		    		Future<Void> f = executor.submit(c);
		    		f.get();
	*/
	    			Runnable r = commands.take();
	    			executor.execute(r);
	    			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}	
	
	
}
