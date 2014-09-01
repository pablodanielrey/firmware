package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.AttLogException;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.Identify.IdentifyResult;
import ar.com.dcsys.firmware.database.FingerprintMappingException;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.person.server.PersonSerializer;

public class PersistPerson implements Cmd {

	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "persistPerson";
	
	private final Leds leds;
	private final PersonsManager personsManager;
	private final PersonSerializer personSerializer;
	
	
	@Inject
	public PersistPerson(Leds leds, 
						PersonsManager personsManager,
						PersonSerializer personSerializer) {

		this.leds = leds;
		this.personsManager = personsManager;
		this.personSerializer = personSerializer;
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
	public void cancel() {
		
	}		
	
	private String cmd;
	private Response remote;
	
	
	@Override
	public void execute() {

		try {
			leds.onCommand(Leds.BLOCKED);
			
			String json = cmd.substring(CMD.length() + 1);
			Person person = personSerializer.read(json);
			personsManager.persist(person);
			
			leds.onCommand(Leds.OK);
			remote.sendText("OK " + person.getId());
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			leds.onCommand(Leds.ERROR);
			
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		}
		
	}					
	
}
