package ar.com.dcsys.firmware;

import java.util.List;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.data.person.PersonHsqlDAO;
import ar.com.dcsys.exceptions.PersonException;



/**
 * Aplicación principal del proyecto para el firmware del reloj.
 *
 */

public class App {

    public static void main( String[] args ) {
 
    	Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
    	try {
//	    	Firmware firmware = container.instance().select(Firmware.class).get();
//	    	firmware.run();
    		
    		PersonHsqlDAO personDAO = container.instance().select(PersonHsqlDAO.class).get();
    		
    		try {
        		Person p = new Person();
        		p.setName("pablo");
        		p.setLastName("rey");
        		p.setDni("27294557");

        		personDAO.persist(p);
    			
        		p = new Person();
        		p.setName("pablo");
        		p.setLastName("rey");
        		p.setDni("27294557");
    			
        		personDAO.persist(p);
        		
    		} catch (PersonException e) {
    			e.printStackTrace();
    		}
    		
    		
    		try {
				List<Person> persons = personDAO.findAll();
	    		for (Person p2 : persons) {
	    			System.out.println(p2.getId());
	    		}
			} catch (PersonException e) {
				e.printStackTrace();
			}
    		
    		
    		
    	} finally {
    		weld.shutdown();
    	}
    	
    }
}
