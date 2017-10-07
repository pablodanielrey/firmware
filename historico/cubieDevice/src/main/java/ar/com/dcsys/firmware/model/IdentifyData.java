package ar.com.dcsys.firmware.model;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;


public class IdentifyData {

	@Inject @Config String init;
	@Inject @Config String releaseFinger;
	@Inject @Config String notFound;
	@Inject @Config String badQuality;
	@Inject @Config String error;
	@Inject @Config String cancel;
	
	public String getInit() {
		return init;
	}
	
	public void setInit(String init) {
		this.init = init;
	}
	
	public String getReleaseFinger() {
		return releaseFinger;
	}
	
	public void setReleaseFinger(String releaseFinger) {
		this.releaseFinger = releaseFinger;
	}
	
	public String getNotFound() {
		return notFound;
	}
	
	public void setNotFound(String notFound) {
		this.notFound = notFound;
	}
	
	public String getBadQuality() {
		return badQuality;
	}
	
	public void setBadQuality(String badQuality) {
		this.badQuality = badQuality;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
	
	public String getCancel() {
		return cancel;
	}
	
	public void setCancel(String cancel) {
		this.cancel = cancel;
	}
	
}
