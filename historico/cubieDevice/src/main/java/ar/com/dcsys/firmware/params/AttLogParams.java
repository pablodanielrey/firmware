package ar.com.dcsys.firmware.params;

import javax.inject.Inject;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.device.DeviceDAO;
import ar.com.dcsys.data.log.AttLogDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.model.device.DevicesManager;


public class AttLogParams implements AttLogDAO.Params {

	private final PersonsManager personsManager;
	private final DeviceDAO deviceDAO;
	
	@Inject
	public AttLogParams(PersonsManager personsManager, DeviceDAO deviceDAO) {
		this.personsManager = personsManager;
		this.deviceDAO = deviceDAO;
	}
	
	@Override
	public Device findDeviceById(String device_id) throws DeviceException {
		return deviceDAO.findById(device_id);
	}

	@Override
	public Person findPersonById(String id) throws PersonException {
		return personsManager.findById(id);
	}

}
