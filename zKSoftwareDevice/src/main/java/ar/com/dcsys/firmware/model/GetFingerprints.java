package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.common.FingerUtils;
import ar.com.dcsys.firmware.database.Initialize;
import ar.com.dcsys.firmware.soap.UserTemplate;
import ar.com.dcsys.firmware.soap.ZKSoftwareCDI;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Fingerprint;

public class GetFingerprints implements Cmd {
	
	public static final String CMD = "getFingerprints";
	private static final Logger logger = Logger.getLogger(GetFingerprints.class.getName());
	
	private final ZkSoftware zkSoftware;
	private final PersonSerializer personSerializer;
	private final FingerprintDAO fingerprintDAO;
	private final FingerprintSerializer fingerprintSerializer;
	private final Initialize initialize;

	private Response remote;
	private String cmd;

	@Inject
	public GetFingerprints(ZKSoftwareCDI zk,
						   PersonSerializer personSerializer,
						   Initialize initialize,
						   FingerprintDAO fingerprintDAO,
						   FingerprintSerializer fingerprintSerializer) {
		this.fingerprintDAO = fingerprintDAO;
		this.fingerprintSerializer = fingerprintSerializer;
		this.personSerializer = personSerializer;
		this.initialize = initialize;
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
			String pin = person.getDni();
			
			List<UserTemplate> templates = zkSoftware.getUserTemplate(pin);
			List<Fingerprint> fps = fingerprintDAO.findByPerson(person.getId());
									
			for (UserTemplate t : templates) {
				Fingerprint fp = FingerUtils.getFingerprint(this.initialize,fps,t,person.getId());
				if (fp == null) {
					fp = FingerUtils.toFingerprint(this.initialize,t,person.getId());
					fingerprintDAO.persist(fp);
				}
			}
			
			//devolver todas las huellas de usuario que estan en la base
			fps = fingerprintDAO.findByPerson(person.getId());
			Integer count = 0;
			for (Fingerprint fp : fps) {
				String jsonFp = fingerprintSerializer.toJson(fp);
				remote.sendText("ok " + jsonFp);
				count ++;
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
