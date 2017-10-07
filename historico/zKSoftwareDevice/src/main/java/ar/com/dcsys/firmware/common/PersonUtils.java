package ar.com.dcsys.firmware.common;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.model.PersonsManager;

public class PersonUtils {

	public static Person createPerson(PersonsManager personsManager, String dni) throws PersonException {
		Person person = new Person();
		person.setDni(dni);
		person.setName("Usuario");
		person.setLastName("Nuevo");
		String id = personsManager.persist(person);
		person = personsManager.findById(id);
		
		return person;
	}
}
