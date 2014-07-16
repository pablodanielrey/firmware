package ar.com.dcsys.firmware.cmd.enroll;

import ar.com.dcsys.security.Finger;

public class DefaultEnrollData implements EnrollData {

	private String personId;
	private Finger finger;
	
	
	
	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public void setFinger(Finger finger) {
		this.finger = finger;
	}

	@Override
	public String getPersonId() {
		return personId;
	}

	@Override
	public Finger getFinger() {
		return finger;
	}

}
