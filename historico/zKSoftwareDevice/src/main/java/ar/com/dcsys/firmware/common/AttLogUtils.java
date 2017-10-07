package ar.com.dcsys.firmware.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.model.PersonsManager;

public class AttLogUtils {
	

	public static AttLog deepCopy(AttLog log) throws Exception {
        //Serialization of object
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(log);
 
        //De-serialization of object
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        AttLog copied = (AttLog) in.readObject();
  
        return copied;
    }
	
	public static AttLog convertLog(PersonsManager personsManager, Device device, ar.com.dcsys.firmware.soap.AttLog logDevice) throws PersonException {
		
		Person person = personsManager.findByDni(logDevice.getPin());
		
		if (person == null) {
			person = PersonUtils.createPerson(personsManager, logDevice.getPin());
		}
		AttLog log = new AttLog();
		log.setDate(logDevice.getDate());
		log.setPerson(person);
		log.setDevice(device);
		log.setVerifyMode(Long.parseLong(logDevice.getVerifiedMode()));
		
		return log;
				
	}	
	
	public static List<AttLog> convertLogs(PersonsManager personsManager, Device device, List<ar.com.dcsys.firmware.soap.AttLog> logsDevice) throws PersonException {
		List<AttLog> logs = new ArrayList<AttLog>();
		
		if (logsDevice != null && logsDevice.size() > 0) {
			for (ar.com.dcsys.firmware.soap.AttLog l : logsDevice) {
				AttLog log = convertLog(personsManager, device, l);
				
				logs.add(log);
			}
		}
		
		return logs;		
	}
	
	private static boolean isEquals (AttLog log0, AttLog log1) {
		
		if (log0 == null && log1 == null) {
			return true;
		}
		
		if (log0 == null || log1 == null) {
			return false;
		}
		
		if (!log0.getDate().equals(log1.getDate())) {
			return false;
		}
		
		if (!log0.getPerson().getId().equals(log1.getPerson().getId())) {
			return false;
		}
		
		if (!log0.getDevice().getId().equals(log1.getDevice().getId())) {
			return false;
		}
		
		return true;
	}
	
	public static boolean include(AttLog log, List<AttLog> logs) {
		for (AttLog l : logs) {
			if (isEquals(log,l)) {
				return true;
			}
		}
		return false;
	}
}
