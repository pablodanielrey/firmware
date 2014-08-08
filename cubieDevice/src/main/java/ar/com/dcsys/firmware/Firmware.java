package ar.com.dcsys.firmware;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.model.Response;

@Singleton
public class Firmware {
	
	private final Logger logger;
	private final LinkedBlockingQueue<Runnable> commands = new LinkedBlockingQueue<Runnable>();
	private volatile boolean end = false;	
	private DefaultCommandProvider defaultCommandProvider;

	public interface DefaultCommandProvider {
		public void defaultCommand();
	}

	public void setDefaultCommandProvider(DefaultCommandProvider defaultCommandProvider) {
		this.defaultCommandProvider = defaultCommandProvider;
	}

	public void setEnd() {
		end = true;
		addCommand(new Runnable() {
			@Override
			public void run() {
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
		
    	Response response = new Response() {
    		@Override
    		public void sendText(String text) throws IOException {
    			logger.log(Level.INFO,text);
    		}    		
    	};		
		
    	while (!end) {
    		
    		try {
    			if (commands.isEmpty()) {
    				if (defaultCommandProvider != null) {
    					defaultCommandProvider.defaultCommand();
    				}
    			}
	    		Runnable r = commands.take();
	    		Thread t = new Thread(r);
	    		t.start();
	    		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}	
	
	
}
