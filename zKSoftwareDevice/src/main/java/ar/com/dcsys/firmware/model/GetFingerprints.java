package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.database.Initialize;
import ar.com.dcsys.firmware.database.ZKDeviceData;
import ar.com.dcsys.firmware.soap.UserTemplate;
import ar.com.dcsys.firmware.soap.ZKSoftwareCDI;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Finger;
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
	
	private Fingerprint toFingerprint(UserTemplate ut, String personId) {
		//creo finger
		String fingerId = ut.getFingerId();
		int iid = Integer.parseInt(fingerId);
		Finger finger = Finger.getFinger(iid);
						
		//convierto template
		String templateStr = ut.getTemplate();
		Charset charset = StandardCharsets.UTF_8;
		byte[] template = templateStr.getBytes(charset);
		
		//obtengo el algoritmo
		ZKDeviceData deviceData = initialize.getZKDeviceData();
		String algorithm = getAlgorithm(deviceData);

		//obtengo la codificacion
		String codification = deviceData.getCodification();
		
		//creo fingerprint
		Fingerprint fp = new Fingerprint(finger, algorithm, codification, template);
		
		//seteo la persona
		fp.setPersonId(personId);
		
		return fp;
		
	}
	
	private String getAlgorithm(ZKDeviceData data) {
		return "ZK" + data.getAlgorithm();
	}

	private boolean equals(Fingerprint fp, UserTemplate ut, String personId) {
		
		//comparo la persona
		if (!personId.equals(fp.getPersonId())) {
			return false;
		}
		
		//comparo el dedo
		String id = ut.getFingerId();
		int iid = Integer.parseInt(id);
		
		Finger finger = Finger.getFinger(iid);
		
		if (!fp.getFinger().equals(finger)) {
			return false;
		}
		
		//comparo template
		String templateStr = ut.getTemplate();
		Charset charset = StandardCharsets.UTF_8;
		byte[] template = templateStr.getBytes(charset);
		if (!template.equals(fp.getTemplate())) {
			return false;
		}
		
		ZKDeviceData deviceData = initialize.getZKDeviceData();
		
		//comparo el algoritmo
		String algorithm = getAlgorithm(deviceData);
		if (!algorithm.equals(fp.getAlgorithm())) {
			return false;
		}
		
		//comparo codificacion
		String codification = deviceData.getCodification();
		if (!codification.equals(fp.getCodification())) {
			return false;
		}
				
		return true;
	}
	
	private Fingerprint getFingerprint(List<Fingerprint> fps, UserTemplate t, String personId) {
		for (Fingerprint fp : fps) {
			if (equals(fp,t,personId)) {
				return fp;
			}
		}
		
		return null;
	}
	
	@Override
	public void execute() {
		try {
			String json = cmd.substring(CMD.length() + 1);
			Person person = personSerializer.read(json);
			String pin = person.getDni();
			
			List<UserTemplate> templates = zkSoftware.getUserTemplate(pin);
			List<Fingerprint> fps = fingerprintDAO.findByPerson(person.getId());
			
			Integer count = 0;
			
			for (UserTemplate t : templates) {
				Fingerprint fp = getFingerprint(fps,t,person.getId());
				if (fp == null) {
					fp = toFingerprint(t,person.getId());
				}
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
