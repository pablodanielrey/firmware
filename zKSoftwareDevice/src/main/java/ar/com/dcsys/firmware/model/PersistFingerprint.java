package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.FingerprintException;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.common.FingerUtils;
import ar.com.dcsys.firmware.soap.SoapDevice;
import ar.com.dcsys.firmware.soap.UserTemplate;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.security.Fingerprint;

public class PersistFingerprint implements Cmd {
	
	private static final String CMD = "persistFingerprint";
	private static Logger logger = Logger.getLogger(PersistFingerprint.class.getName());
	
	private Response remote;
	private String cmd;
	
	private final SoapDevice zkDevice;
	private final FingerprintSerializer fingerprintSerializer;
	private final FingerprintDAO fingerprintDAO;
	private final PersonsManager personsManager;
	
	@Inject
	public PersistFingerprint(SoapDevice zk, FingerprintSerializer fingerprintSerializer, FingerprintDAO fingerprintDAO, PersonsManager personsManager) {
		this.zkDevice = zk;
		this.fingerprintDAO =  fingerprintDAO;
		this.fingerprintSerializer = fingerprintSerializer;
		this.personsManager = personsManager;
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
			Fingerprint fp = fingerprintSerializer.read(json);
			
			//verifico que la huella no sea null
			if (fp == null) {
				logger.log(Level.SEVERE, "Fingerprint == null");
				remote.sendText("ERROR fingerprint == null");
				throw new FingerprintException(new Throwable("Fingerprint == null"));
			}
			//verifico que la persona no sea null y que tenga documento
			String personId = fp.getPersonId();
			Person person = personsManager.findById(personId);
			
			if (person == null || person.getDni() == null) {
				logger.log(Level.SEVERE, "Person == null");
				remote.sendText("ERROR person == null");
				throw new PersonException("person == null");				
			}
			
			//verifico que la huella tenga el mismo algoritmo y la misma codificacion
			String algorithm = FingerUtils.getAlgorithm(zkDevice);
			if (!algorithm.equals(fp.getAlgorithm())) {
				logger.log(Level.SEVERE, "el algoritmo de la huella es diferente al del reloj");
				remote.sendText("ERROR el algoritmo de la huella es diferente al del reloj");
				throw new FingerprintException(new Throwable("el algoritmo de la huella es diferente al del reloj"));				
			}
			
			String codification = zkDevice.getCodification();
			if (!codification.equals(fp.getCodification())) {
				logger.log(Level.SEVERE, "la codificacion de la huella es diferente al del reloj");
				remote.sendText("ERROR la codificacion de la huella es diferente al del reloj");
				throw new FingerprintException(new Throwable("la codificacion de la huella es diferente al del reloj"));				
			}
			
			//lo  actualizo en la base
			fingerprintDAO.persist(fp);
			
			ZkSoftware zkSoftware = zkDevice.getZkSoftware();
			
			//busco si existe en el reloj
			String pin = person.getDni();
			List<UserTemplate> uts = zkSoftware.getUserTemplate(pin);
			UserTemplate ut = FingerUtils.findUserTemplate(uts, fp, pin);
			
			if (ut == null) {
				ut = FingerUtils.toUserTemplate(fp, pin);
			}
			
			//lo actualizo en el reloj
			zkSoftware.setUserTemplate(ut);
			zkSoftware.refreshDB();
			
			remote.sendText("OK " + String.valueOf(fp.getId()));
			
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
