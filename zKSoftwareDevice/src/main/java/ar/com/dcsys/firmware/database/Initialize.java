package ar.com.dcsys.firmware.database;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.model.device.DevicesManager;

public class Initialize {
	
	private final ZKDeviceData zkd;
	private final DevicesManager devicesManager;
	
	@Inject
	public Initialize(ZKDeviceData zkDeviceData, DevicesManager devicesManager) {
		this.zkd = zkDeviceData;
		this.devicesManager = devicesManager;
	}
	
	public Device getCurrentDevice() throws DeviceException {
		String id = zkd.getId();
		if ("initialize".equals(id)) {
			List<String> ids = devicesManager.findAll();
			if (ids == null || ids.size() <= 0) {
				return null;
			}
			id = ids.get(0);
		}
		return devicesManager.findById(id);
	}
	
	public ZKDeviceData getZKDeviceData() {
		return zkd;
	}
	
	public String execute() throws CmdException {
		
		try {
			String id = zkd.getId();
			if (id.equals("initialize")) {
				id = UUID.randomUUID().toString();
			}
			
			Device d = devicesManager.findById(id);
			if (d == null) {
				d = new Device();
				d.setId(id);
			}
			
			String enabled = zkd.getEnabled();
			if (enabled == null) {
				d.setEnabled(true);
			} else {
				try {
					d.setEnabled(Boolean.parseBoolean(enabled));
				} catch (Exception e) {
					d.setEnabled(true);
				}
			}
			
			d.setName(zkd.getName());
			d.setDescription(zkd.getDescription());
			d.setIp(zkd.getIp());
			d.setNetmask(zkd.getNetmask());
			
			devicesManager.persist(d);
			
			return id;
		} catch (DeviceException e) {
			throw new CmdException(e);
		}
	}

}
