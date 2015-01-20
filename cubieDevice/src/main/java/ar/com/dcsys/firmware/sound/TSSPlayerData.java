package ar.com.dcsys.firmware.sound;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;


public class TSSPlayerData {

	@Inject @Config String proto;
	@Inject @Config String ip;
	@Inject @Config String port;
	@Inject @Config String url;
	@Inject @Config String enableSound;
	
	public String getProto() {
		return proto;
	}

	public void setProto(String proto) {
		this.proto = proto;
	}

	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Boolean getEnableSound() {
		
		if (enableSound == null) {
			return false;
		}
		
		if (enableSound.compareToIgnoreCase("1") == 0 || enableSound.compareToIgnoreCase("true") == 0 || enableSound.compareToIgnoreCase("yes") == 0 || enableSound.compareToIgnoreCase("si") == 0) {
			return true;			
		}
		
		return false;
	}

	public void setEnableSound(Boolean enableSound) {
		if (enableSound != null && enableSound) { 
			this.enableSound = "true";
		} else {
			this.enableSound = "false";
		}
	}	
	
	
	
}
