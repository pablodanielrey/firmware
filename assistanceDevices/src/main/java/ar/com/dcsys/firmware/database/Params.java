package ar.com.dcsys.firmware.database;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.device.DeviceDAO;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.log.AttLogDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.data.person.PersonDAO;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.exceptions.PersonException;

@Singleton
public class Params implements AttLogDAO.Params, FingerprintDAO.Params {

	private final DeviceDAO deviceDAO;
	private final PersonDAO personDAO;
	
	@Inject
	public Params(DeviceDAO deviceDAO, PersonDAO personDAO) {
		this.deviceDAO = deviceDAO;
		this.personDAO = personDAO;
	}
	
	@Override
	public Device findDeviceById(String id) throws DeviceException {
		return deviceDAO.findById(id);
	}

	@Override
	public Person findPersonById(String id) throws PersonException {
		return personDAO.findById(id);
	}

	
	
}
