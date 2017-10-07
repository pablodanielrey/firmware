package ar.com.dcsys.firmware.database;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.model.device.DevicesManager;

public class Initialize {

	private final CubieDeviceData cd;
	private final DevicesManager devicesManager;
	
	@Inject
	public Initialize(CubieDeviceData cubieDeviceData, DevicesManager devicesManager) {
		this.cd = cubieDeviceData;
		this.devicesManager = devicesManager;
	}
	
	public Device getCurrentDevice() throws DeviceException {
		String id = cd.getId();
		if ("initialize".equals(id)) {
			List<String> ids = devicesManager.findAll();
			if (ids == null || ids.size() <= 0) {
				throw new DeviceException("no existe ningun dispositivo");
			}
			id = ids.get(0);
		}
		return devicesManager.findById(id);
	}
	
	public CubieDeviceData getCubieDeviceData() {
		return cd;
	}
	
	public String execute() throws CmdException {
		
		try {
			String id = cd.getId();	
			if (id.equals("initialize")) {
				id = UUID.randomUUID().toString();
			}
			
			
			Device d = devicesManager.findById(id);
			if (d == null) {
				d = new Device();
				d.setId(id);
			}
			
			
			String enabled = cd.getEnabled();
			if (enabled == null) {
				d.setEnabled(true);
				
			} else {
				try {
					d.setEnabled(Boolean.parseBoolean(enabled));
					
				} catch (Exception e) {
					d.setEnabled(true);
				}
			}

			d.setName(cd.getName());
			d.setDescription(cd.getDescription());
			d.setIp(cd.getIp());
			d.setNetmask(cd.getNetmask());
			
			devicesManager.persist(d);
			
			return id;
			
		} catch (DeviceException e) {
			throw new CmdException(e);
		}
		
	}
	
}
