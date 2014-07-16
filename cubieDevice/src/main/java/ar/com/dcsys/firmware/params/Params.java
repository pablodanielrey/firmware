package ar.com.dcsys.firmware.params;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.justification.JustificationDAO;
import ar.com.dcsys.data.period.PeriodDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.data.log.AttLogDAO;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.exceptions.PersonException;


public class Params implements JustificationDAO.Params, AttLogDAO.Params, PeriodDAO.Params {

	@Override
	public Device findDeviceById(String device_id) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Person findPersonById(String id) throws PersonException {
		// TODO Auto-generated method stub
		return null;
	}

}
