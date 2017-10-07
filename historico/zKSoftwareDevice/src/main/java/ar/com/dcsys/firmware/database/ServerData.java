package ar.com.dcsys.firmware.database;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class ServerData {

	@Inject @Config String ip;
	@Inject @Config String port;
	@Inject @Config String url;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
}
