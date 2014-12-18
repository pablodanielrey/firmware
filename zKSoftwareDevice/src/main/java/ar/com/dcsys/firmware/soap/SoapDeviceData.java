package ar.com.dcsys.firmware.soap;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class SoapDeviceData {
	
	@Inject @Config String soapUrl;
	@Inject @Config String ip;
	@Inject @Config String debug;
	@Inject @Config String algorithm;
	@Inject @Config String codification;
	
	
	
	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getCodification() {
		return codification;
	}

	public void setCodification(String codification) {
		this.codification = codification;
	}

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
