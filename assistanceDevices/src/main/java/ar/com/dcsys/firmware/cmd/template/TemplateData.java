package ar.com.dcsys.firmware.cmd.template;

import ar.com.dcsys.security.FingerprintCredentials;

public class TemplateData {

	private FingerprintCredentials fingerprint;
	private int number;
	
	public FingerprintCredentials getFingerprint() {
		return fingerprint;
	}
	
	public void setFingerprint(FingerprintCredentials fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	
}
