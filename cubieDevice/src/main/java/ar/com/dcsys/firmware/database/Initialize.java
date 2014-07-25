package ar.com.dcsys.firmware.database;

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
			
			d.setEnabled(true);
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
