package ar.com.dcsys.firmware.cmd.enroll;

import ar.com.dcsys.security.Finger;

public interface EnrollData {
	public String getPersonId();
	public Finger getFinger();
}
