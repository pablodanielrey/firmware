package ar.com.dcsys.firmware;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class AnnouncerData {

	@Inject @Config String interConnectionSleep;
	@Inject @Config String intraConnectionSleep;
	@Inject @Config String backOffMilis;
	
	public String getInterConnectionSleep() {
		return interConnectionSleep;
	}
	
	public void setInterConnectionSleep(String interConnectionSleep) {
		this.interConnectionSleep = interConnectionSleep;
	}
	
	public String getIntraConnectionSleep() {
		return intraConnectionSleep;
	}
	
	public void setIntraConnectionSleep(String intraConnectionSleep) {
		this.intraConnectionSleep = intraConnectionSleep;
	}
	
	public String getBackOffMilis() {
		return backOffMilis;
	}
	
	public void setBackOffMilis(String backOffMilis) {
		this.backOffMilis = backOffMilis;
	}
	
	
	
}
