package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.soap.UserInfo;
import ar.com.dcsys.firmware.soap.SoapDevice;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.person.server.PersonSerializer;

public class PersistPerson implements Cmd {
	
	private static final String CMD = "persistPerson";
	private static Logger logger = Logger.getLogger(PersistPerson.class.getName());
	
	private final PersonSerializer personSerializer;
	private final PersonsManager personsManager;
	private final ZkSoftware zkSoftware;
	private Response remote;
	private String cmd;
	
	@Inject
	public PersistPerson(SoapDevice zk, PersonSerializer personSerializer, PersonsManager personsManager) {
		this.personSerializer = personSerializer;
		this.personsManager = personsManager;
		this.zkSoftware = zk.getZkSoftware();
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
			
			//busco si existe en la base por el dni, si existe y posee otro id tiro una exepcion
			String dni = person.getDni();
			if (dni != null) {
				Person p = personsManager.findByDni(dni);
				if (p != null && !(p.getId().equals(person.getId()))) {
					logger.log(Level.SEVERE,"La persona ya existe en la base del firmware y posee otro id");
					remote.sendText("ERROR La persona ya existe en la base del firmware y posee otro id" );
					throw new PersonException("La persona ya existe en la base del firmware y posee otro id");
				}
			}
			
			//lo actualizo en el sistema
			personsManager.persist(person);
			
			//busco si existe en el reloj
			List<UserInfo> uis = zkSoftware.getUserInfo(person.getDni());
			UserInfo ui;
			if (uis == null || uis.size() <= 0) {
				ui = new UserInfo();
				ui.setPin2(person.getDni());
				ui.setName("");				
			} else {
				ui = uis.get(0);
			}
			
			//lo actualizo en el reloj
			zkSoftware.setUserInfo(ui);
			zkSoftware.refreshDB();

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
