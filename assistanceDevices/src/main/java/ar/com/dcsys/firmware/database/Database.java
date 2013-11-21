package ar.com.dcsys.firmware.database;

import java.util.Date;
import java.util.List;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.device.DeviceDAO;
import ar.com.dcsys.data.fingerprint.Fingerprint;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.data.log.AttLogDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.data.person.PersonDAO;
import ar.com.dcsys.exceptions.AttLogException;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.exceptions.FingerprintException;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.exceptions.DatabaseException;

public class Database {

	private final PersonDAO personDAO;
	private final DeviceDAO deviceDAO;
	private final AttLogDAO attLogDAO;
	private final FingerprintDAO fingerprintDAO;
	private final FingerprintReaderMappingDAO fingerprintReaderMappingDAO;
	

	public Database(PersonDAO personDAO,
					DeviceDAO deviceDAO,
					AttLogDAO attLogDAO,
					FingerprintDAO fingerprintDAO,
					FingerprintReaderMappingDAO fingerprintReaderMappingDAO) {
		this.personDAO = personDAO;
		this.deviceDAO = deviceDAO;
		this.attLogDAO = attLogDAO;
		this.fingerprintDAO = fingerprintDAO;
		this.fingerprintReaderMappingDAO = fingerprintReaderMappingDAO;
	}
	
	
	/**
	 * retorna el dispositivo actual en donde se está corriendo el firmware.
	 * en este caso existe 1 solo dispositivo guardado dentro de la base y es el actual.
	 * @return
	 * @throws DatabaseException
	 * @throws DeviceException
	 */
	private Device findCurrentDevice() throws DatabaseException, DeviceException {
		
		List<Device> devices = deviceDAO.findAll();
		if (devices == null || devices.size() != 1) {
			// solo puede existir 1 solo. el acutal!!.
			throw new DatabaseException("Devices != 1");
		}
		Device device = devices.get(0);
		
		return device;
	}
	
	
	/**
	 * Busca un Fingerprint dado un número de huella identificado por el lector.
	 * @param template
	 * @return
	 * @throws DatabaseException
	 * @throws FingerprintException
	 */
	private Fingerprint findFingerprintByTemplateNumber(Long template) throws DatabaseException, FingerprintException {
		
		FingerprintReaderMapping frm = fingerprintReaderMappingDAO.findById(template);
		if (frm == null) {
			// no debería psar nunca. esto es una exception FATAL!!!.
			// ocurre cuando el lector identifica un template que no tiene mapeo en la base de datos!!!.
			throw new DatabaseException("FingerprintReaderMappping == null");
		}
		
		String fpId = frm.getFingerprintId();
		Fingerprint fp = fingerprintDAO.findById(fpId);
		if (fp == null) {
			// error FATAL!!!
			// ocurre cuando existe un mapeo inválido a una huella no existente!!!.
			throw new DatabaseException("Fingerprint == null");
		}
		
		return fp;
	}
	
	
	
	
	
	/**
	 * Genera un log en la base correspondiente al usuario dueño de la huella identificada por el lector con el número = template.
	 * @param template
	 * @throws DatabaseException
	 */
	public synchronized void generateLog(int template) throws DatabaseException {

		try {
			Device device = findCurrentDevice();
			Person person = null;
			
			AttLog log = new AttLog();
			log.setDate(new Date());
			log.setPerson(person);
			log.setDevice(device);
			log.setVerifyMode(1l);		// compatible con ZkSoftware :  0 == Clave, 1 == Huella, 2 == Tarjeta
			attLogDAO.persist(log);
			
		} catch (DeviceException | AttLogException e) {
			throw new DatabaseException(e);
		}
			
	}

	/**
	 * Genera un log en la base correspondiente al usuario identificado por el dni del parámetro.
	 * @param template
	 * @throws DatabaseException
	 */
	public synchronized void generateLog(String dni) throws DatabaseException {

		try {
			Device device = findCurrentDevice();
			Person person = personDAO.findByDni(dni);
			
			AttLog log = new AttLog();
			log.setDate(new Date());
			log.setPerson(person);
			log.setDevice(device);
			log.setVerifyMode(0l);		// compatible con ZkSoftware :  0 == Clave, 1 == Huella, 2 == Tarjeta
			attLogDAO.persist(log);
			
		} catch (DeviceException | PersonException | AttLogException e) {
			throw new DatabaseException(e);
		}
			
	}	
	
}
