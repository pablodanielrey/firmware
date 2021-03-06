package ar.com.dcsys.firmware.database;

import javax.inject.Inject;

import ar.com.dcsys.config.Config;

public class CubieDeviceData {

	@Inject @Config String id;
	@Inject @Config String ip;
	@Inject @Config String netmask;
	@Inject @Config String name;
	@Inject @Config String description;
	@Inject @Config String enabled;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getNetmask() {
		return netmask;
	}
	
	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	
}
