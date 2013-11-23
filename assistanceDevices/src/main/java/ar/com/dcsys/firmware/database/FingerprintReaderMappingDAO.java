package ar.com.dcsys.firmware.database;

import ar.com.dcsys.firmware.exceptions.DatabaseException;


public interface FingerprintReaderMappingDAO {

	public FingerprintReaderMapping findById(Long id) throws DatabaseException;
	public void persist(FingerprintReaderMapping fprm) throws DatabaseException;
	
}
