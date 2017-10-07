package ar.com.dcsys.firmware.database;

import java.util.List;


public interface FingerprintMappingDAO {

	public void persist(FingerprintMapping fp) throws FingerprintMappingException;
	public void delete(FingerprintMapping fp) throws FingerprintMappingException;
	public List<FingerprintMapping> findAll() throws FingerprintMappingException;
	public FingerprintMapping findBy(int fpNumber) throws FingerprintMappingException;
	public FingerprintMapping fingBy(String personId, String fingerprintId) throws FingerprintMappingException;
	public void deleteAll() throws FingerprintMappingException;
	
}
