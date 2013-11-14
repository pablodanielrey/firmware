package ar.com.dcsys.firmware;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;

public class LoggerProducer {

	private static final Logger logger = Logger.getLogger(Firmware.class.getName()); 
	
	@Produces
	public Logger logger() {
		return logger;
	}
	
}
