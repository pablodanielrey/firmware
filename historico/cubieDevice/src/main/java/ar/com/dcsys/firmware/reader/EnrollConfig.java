package ar.com.dcsys.firmware.reader;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class EnrollConfig {

	@Inject @Config String init;
	@Inject @Config String fingerprintDupplicated;
	@Inject @Config String ok;
	@Inject @Config String error;
	@Inject @Config String cancel;
	@Inject @Config String releaseFinger;
	@Inject @Config String timeout;
	@Inject @Config String badQuality;
	@Inject @Config String needFirstFinger;
	@Inject @Config String needSecondFinger;
	@Inject @Config String neddThirdFinger;
	
	public String getInit() {
		return init;
	}
	
	public void setInit(String init) {
		this.init = init;
	}
	public String getFingerprintDupplicated() {
		return fingerprintDupplicated;
	}
	public void setFingerprintDupplicated(String fingerprintDupplicated) {
		this.fingerprintDupplicated = fingerprintDupplicated;
	}
	public String getOk() {
		return ok;
	}
	public void setOk(String ok) {
		this.ok = ok;
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
	public String getReleaseFinger() {
		return releaseFinger;
	}
	public void setReleaseFinger(String releaseFinger) {
		this.releaseFinger = releaseFinger;
	}
	public String getTimeout() {
		return timeout;
	}
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}
	public String getBadQuality() {
		return badQuality;
	}
	public void setBadQuality(String badQuality) {
		this.badQuality = badQuality;
	}
	public String getNeedFirstFinger() {
		return needFirstFinger;
	}
	public void setNeedFirstFinger(String needFirstFinger) {
		this.needFirstFinger = needFirstFinger;
	}
	public String getNeedSecondFinger() {
		return needSecondFinger;
	}
	public void setNeedSecondFinger(String needSecondFinger) {
		this.needSecondFinger = needSecondFinger;
	}
	public String getNeddThirdFinger() {
		return neddThirdFinger;
	}
	public void setNeddThirdFinger(String neddThirdFinger) {
		this.neddThirdFinger = neddThirdFinger;
	}
	
	
	
	
}
