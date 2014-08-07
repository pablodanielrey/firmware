package ar.com.dcsys.firmware.leds;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class LedsConfig {
	
	@Inject @Config String identifyLed;
	@Inject @Config String enrollLed;
	@Inject @Config String blockedLed;
	
	@Inject @Config String okLed;
	@Inject @Config String okDelay;
	
	@Inject @Config String errorLed;
	@Inject @Config String errorDelay;
	
	@Inject @Config String subOkLed;
	@Inject @Config String subOkDelay;
	
	@Inject @Config String subErrorLed;
	@Inject @Config String subErrorDelay;
	
	@Inject @Config String phaseOkLed;
	@Inject @Config String phaseOkDelay;
	
	@Inject @Config String phaseErrorLed;
	@Inject @Config String phaseErrorDelay;
	
	
	public Integer intValue(String v) {
		return Integer.parseInt(v);
	}
	
	
	
	public String getIdentifyLed() {
		return identifyLed;
	}



	public void setIdentifyLed(String identifyLed) {
		this.identifyLed = identifyLed;
	}



	public String getEnrollLed() {
		return enrollLed;
	}



	public void setEnrollLed(String enrollLed) {
		this.enrollLed = enrollLed;
	}



	public String getBlockedLed() {
		return blockedLed;
	}



	public void setBlockedLed(String blockedLed) {
		this.blockedLed = blockedLed;
	}



	public String getOkLed() {
		return okLed;
	}
	
	public void setOkLed(String okLed) {
		this.okLed = okLed;
	}
	
	public String getOkDelay() {
		return okDelay;
	}
	
	public void setOkDelay(String okDelay) {
		this.okDelay = okDelay;
	}
	public String getErrorLed() {
		return errorLed;
	}
	public void setErrorLed(String errorLed) {
		this.errorLed = errorLed;
	}
	public String getErrorDelay() {
		return errorDelay;
	}
	public void setErrorDelay(String errorDelay) {
		this.errorDelay = errorDelay;
	}
	public String getSubOkLed() {
		return subOkLed;
	}
	public void setSubOkLed(String subOkLed) {
		this.subOkLed = subOkLed;
	}
	public String getSubOkDelay() {
		return subOkDelay;
	}
	public void setSubOkDelay(String subOkDelay) {
		this.subOkDelay = subOkDelay;
	}
	public String getSubErrorLed() {
		return subErrorLed;
	}
	public void setSubErrorLed(String subErrorLed) {
		this.subErrorLed = subErrorLed;
	}
	public String getSubErrorDelay() {
		return subErrorDelay;
	}
	public void setSubErrorDelay(String subErrorDelay) {
		this.subErrorDelay = subErrorDelay;
	}
	public String getPhaseOkLed() {
		return phaseOkLed;
	}
	public void setPhaseOkLed(String phaseOkLed) {
		this.phaseOkLed = phaseOkLed;
	}
	public String getPhaseOkDelay() {
		return phaseOkDelay;
	}
	public void setPhaseOkDelay(String phaseOkDelay) {
		this.phaseOkDelay = phaseOkDelay;
	}
	public String getPhaseErrorLed() {
		return phaseErrorLed;
	}
	public void setPhaseErrorLed(String phaseErrorLed) {
		this.phaseErrorLed = phaseErrorLed;
	}
	public String getPhaseErrorDelay() {
		return phaseErrorDelay;
	}
	public void setPhaseErrorDelay(String phaseErrorDelay) {
		this.phaseErrorDelay = phaseErrorDelay;
	}
	
	
	
}
