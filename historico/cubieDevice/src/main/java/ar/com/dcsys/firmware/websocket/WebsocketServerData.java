package ar.com.dcsys.firmware.websocket;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class WebsocketServerData {

	@Inject @Config String ip;
	@Inject @Config String port;
	@Inject @Config String url;
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public int getPort() {
		return Integer.parseInt(port);
	}
	
	public void setPort(int port) {
		this.port = String.valueOf(port);
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}


}
