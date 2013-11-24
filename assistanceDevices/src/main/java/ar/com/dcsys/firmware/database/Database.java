package ar.com.dcsys.firmware.database;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

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
import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.template.TemplateData;
import ar.com.dcsys.firmware.exceptions.DatabaseException;
import ar.com.dcsys.security.FingerprintCredentials;

public class Database {

	private final PersonDAO personDAO;
	private final DeviceDAO deviceDAO;
	private final AttLogDAO attLogDAO;
	private final FingerprintDAO fingerprintDAO;
	private final FingerprintReaderMappingDAO fingerprintReaderMappingDAO;
	
	@Inject
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
	

	
    public static void main( String[] args ) {
    	Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
    	try {

    		Database database = container.instance().select(Database.class).get();
    		database.createTables();
    		
    	} finally {
    		weld.shutdown();
    	}
    	
    }
	
	
    private void createTables() {
		fingerprintDAO.init();
    }
	
	
	
	
	
	
	
	
	
	/**
	 * Enrola una template dentro de la base de datos. si el usuario no existe entonces lo crea.
	 *
	 * @param person
	 * @param templateData
	 * @throws DatabaseException
	 * @throws PersonException
	 * @throws FingerprintException
	 */
	public synchronized void enroll(Person person, TemplateData templateData) throws DatabaseException, PersonException, FingerprintException {

		Person actualPerson = findOrCreatePerson(person);
		
		FingerprintCredentials fpc = templateData.getFingerprint();
		Fingerprint fp = new Fingerprint();
		fp.setFingerprint(fpc);
		fp.setPerson(actualPerson);
		String fpId = fingerprintDAO.persist(fp);

		
		int number = templateData.getNumber();
		FingerprintReaderMapping fprm = new FingerprintReaderMapping();
		fprm.setFingerprintId(fpId);
		fprm.setId(Long.valueOf(number));
		fingerprintReaderMappingDAO.persist(fprm);
		
	}
	
	
	/**
	 * Genera un log en la base correspondiente al usuario dueño de la huella identificada por el lector con el número = template.
	 * @param template
	 * @throws DatabaseException
	 */
	public synchronized void generateLog(int template) throws DatabaseException, FingerprintException {

		try {
			Device device = findCurrentDevice();
			Fingerprint fp = findFingerprintByTemplateNumber(Long.valueOf(template));
			Person person = fp.getPerson();
			
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
	 * Busca la persona y si no existe la crea dentro de la base.
	 * @param person
	 * @return
	 * @throws PersonException
	 */
	private Person findOrCreatePerson(Person person) throws PersonException {
		
		String id = person.getId();
		if (id != null) {
			return person;
		}
		
		String dni = person.getDni();
		if (dni == null) {
			throw new PersonException("person.dni == null");
		}
		
		Person actualPerson = personDAO.findByDni(dni);
		if (actualPerson == null) {
			id = personDAO.persist(person);
			actualPerson = personDAO.findById(id);
		}
		
		return actualPerson;
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
	
	
}
