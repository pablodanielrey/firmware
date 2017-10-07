package ar.com.dcsys.firmware.cmd.template;

import ar.com.dcsys.security.Fingerprint;

public class TemplateData {

	private Fingerprint fingerprint;
	private int number;
	
	public Fingerprint getFingerprint() {
		return fingerprint;
	}
	
	public void setFingerprint(Fingerprint fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	
}
