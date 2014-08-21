package ar.com.dcsys.firmware;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.model.Identify;
import ar.com.dcsys.firmware.model.Response;

@Singleton
public class DefaultCommandGenerator implements Runnable {

	public static Logger logger = Logger.getLogger(DefaultCommandGenerator.class.getName());
	private final Identify identify;
	private final Firmware firmware;
	
	private volatile boolean needToSleep = false;
	
	private final Response remote = new Response() {
		@Override
		public void sendText(String text) throws IOException {
			if (text.startsWith("ERROR") && text.contains("no templates")) {
				needToSleep = true;
			}
			logger.log(Level.INFO, text);
			
			if (text.startsWith("OK") || text.startsWith("ERROR")) {
				MutualExclusion.using[MutualExclusion.EXECUTING_COMMAND].release();
			}
			
		}
	};
	
	@Inject
	public DefaultCommandGenerator(Firmware firmware, Identify identify) {
		this.identify = identify;
		this.firmware = firmware;
	}

	@PostConstruct
	public void init() {
		Thread t = new Thread(this);
		t.start();
	}
	
	
	@Override
	public void run() {
		while (true) {
			
			while (needToSleep) {
				try {
					Thread.sleep(5000l);
					needToSleep = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			MutualExclusion.using[MutualExclusion.NEED_DEFAULT_COMMAND].acquireUninterruptibly();
			MutualExclusion.using[MutualExclusion.EXECUTING_COMMAND].acquireUninterruptibly();
			
			try {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						identify.execute("", remote);
					}
				};
				firmware.addCommand(r);
			
			} finally {
				MutualExclusion.using[MutualExclusion.EXECUTING_COMMAND].release();
			}
		}
	}
	
}
