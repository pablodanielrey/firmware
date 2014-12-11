package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.common.PersonUtils;
import ar.com.dcsys.firmware.soap.UserInfo;
import ar.com.dcsys.firmware.soap.ZKSoftwareCDI;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.person.server.PersonSerializer;

public class GetPersons implements Cmd {
	
	public static final String CMD = "getPersons";
	private static final Logger logger = Logger.getLogger(GetPersons.class.getName());
	
	private final ZkSoftware zkSoftware;
	private final PersonsManager personsManager;
	private final PersonSerializer personSerializer;
	private Response remote;

	@Inject
	public GetPersons(ZKSoftwareCDI zk,
					  PersonsManager personsManager,
					  PersonSerializer personSerializer) {
		this.zkSoftware = zk.getZkSoftware();
		this.personsManager = personsManager;
		this.personSerializer = personSerializer;
	}
	
	@Override
	public String getCommand() {
		return CMD;
	}

	@Override
	public boolean identify(String cmd) {
		return cmd.startsWith(CMD);
	}

	@Override
	public void setResponse(Response remote) {
		this.remote = remote;
	}

	@Override
	public void execute() {
		try {
			List<UserInfo> users = this.zkSoftware.getAllUserInfo();
			int count = 0;
			
			for (UserInfo ui : users) {
				String pin = ui.getPin2();
				Person p = personsManager.findByDni(pin);
				if (p == null) {
					p = PersonUtils.createPerson(personsManager, pin);
				}
				String json = personSerializer.toJson(p);
				remote.sendText("ok " + json);
				count++;
			}
			
			remote.sendText("OK " + count);
			
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
