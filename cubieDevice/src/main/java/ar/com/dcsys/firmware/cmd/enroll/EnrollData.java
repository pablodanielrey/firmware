package ar.com.dcsys.firmware.cmd.enroll;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.security.Finger;

public interface EnrollData {
	public Person getPerson();
	public Finger getFinger();
}
