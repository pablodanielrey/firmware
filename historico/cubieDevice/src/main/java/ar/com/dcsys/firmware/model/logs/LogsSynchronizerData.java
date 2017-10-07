package ar.com.dcsys.firmware.model.logs;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class LogsSynchronizerData {

	@Inject @Config String sleep;

	public String getSleep() {
		return sleep;
	}

	public void setSleep(String sleep) {
		this.sleep = sleep;
	}

	
}
