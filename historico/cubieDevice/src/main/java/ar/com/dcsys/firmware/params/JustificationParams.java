package ar.com.dcsys.firmware.params;

import javax.inject.Inject;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.justification.JustificationDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.model.device.DevicesManager;


public class JustificationParams implements JustificationDAO.Params {

	private final PersonsManager personsManager;
	
	@Inject
	public JustificationParams(PersonsManager personsManager) {
		this.personsManager = personsManager;
	}
	
	@Override
	public Person findPersonById(String id) throws PersonException {
		return personsManager.findById(id);
	}

}
