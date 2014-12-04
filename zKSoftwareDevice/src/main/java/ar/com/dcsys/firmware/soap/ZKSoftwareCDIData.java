package ar.com.dcsys.firmware.soap;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class ZKSoftwareCDIData {
	
	@Inject @Config String soapUrl;
	@Inject @Config String ip;
	@Inject @Config String debug;
	
	public String getSoapUrl() {
		return soapUrl;
	}
	
	public void setSoapUrl(String soapUrl) {
		this.soapUrl = soapUrl;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}
	
	

}
