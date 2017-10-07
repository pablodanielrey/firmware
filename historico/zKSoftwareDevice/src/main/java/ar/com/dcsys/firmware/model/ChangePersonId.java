package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.common.AttLogUtils;
import ar.com.dcsys.firmware.common.FingerUtils;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.model.log.AttLogsManager;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Fingerprint;

public class ChangePersonId implements Cmd {

	private static final String CMD = "changePersonId";
	private static Logger logger = Logger.getLogger(ChangePersonId.class.getName());
	
	private final PersonSerializer personSerializer;
	private final PersonsManager personsManager;
	private final AttLogsManager logsManager;
	private final FingerprintDAO fingerprintDAO;
	private Response remote;
	private String cmd;
	
	@Inject
	public ChangePersonId(FingerprintDAO fingerprintDAO, PersonSerializer personSerializer, AttLogsManager logsManager, PersonsManager personsManager) {
		this.personSerializer = personSerializer;
		this.personsManager = personsManager;
		this.logsManager = logsManager;
		this.fingerprintDAO = fingerprintDAO;
	}

	@Override
	public String getCommand() {
		return CMD;
	}

	@Override
	public boolean identify(String cmd) {
		if (cmd.startsWith(CMD)) {
			this.cmd = cmd;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setResponse(Response remote) {
		this.remote = remote;
	}
	
	@Override
	public void execute() {
		try {
			String json = cmd.substring(CMD.length() + 1);
			Person person = personSerializer.read(json);
			
			if (person == null) {
				logger.log(Level.SEVERE,"Person == null");
				remote.sendText("ERROR person == null" );
				throw new PersonException("Person == null");
			}
			
			//busco si existe en la base por el dni, si no existe tiro una exepcion
			String dni = person.getDni();
			Person personOld = null;
			if (dni != null) {
				personOld = personsManager.findByDni(dni);
				if (personOld == null) {
					logger.log(Level.SEVERE,"La persona " + dni +" no existe en la base del firmware");
					remote.sendText("ERROR La persona " + dni +" no existe en la base del firmware" );
					throw new PersonException("La persona " + dni +" no existe en la base del firmware");
				}
			}
			
			if (person.getId().equals(personOld.getId())) {
				remote.sendText("OK " + person.getId());
				return;
			}
			
			// ----- Person -------

			//agrego la nueva persona
			personsManager.persist(person);
			
			// -------- AttLogs -----------
			
			//obtengo todos los logs de la persona y le realizo una copia
			List<String> idsLogs = logsManager.findAll();
			List<AttLog> logs = new ArrayList<AttLog>();
			List<AttLog> newLogs = new ArrayList<AttLog>();
			for (String idLog : idsLogs) {
				AttLog log = logsManager.findById(idLog);
				if (log.getPerson().getId().equals(personOld.getId())) {
					logs.add(log);
					AttLog copyLog = AttLogUtils.deepCopy(log);
					newLogs.add(copyLog);
				}
			}
			
			//le cambio el person a los nuevos logs y le hago un persist
			for (AttLog l : newLogs) {
				l.setPerson(person);
				l.setId(null);
				logsManager.persist(l);
			}
			
			//elimino los logs viejos
			for (AttLog l : logs) {
				logsManager.remove(l.getId());
			}
			
			// ----- Fingerprint ----------
			
			//obtengo todas las huellas de la persona y le realizo una copia
			//les cambio el id de person a las nuevas huellas y le hago un persist
			//elimino las huellas viejas 
			List<Fingerprint> fps = fingerprintDAO.findByPerson(personOld.getId());
			for (Fingerprint fp : fps) {
				Fingerprint newFp = FingerUtils.deepCopy(fp);
				newFp.setPersonId(person.getId());
				newFp.setId(null);
				fingerprintDAO.persist(newFp);
				fingerprintDAO.remove(fp);
			}
						
			// ----- Person -------
			
			//elimino la persona que existia anteriormente
			personsManager.remove(personOld);
			
			
			remote.sendText("OK " + person.getId());
			
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		}
	}

	@Override
	public void cancel() throws CmdException {
		// TODO Auto-generated method stub
		
	}
	
	
}
