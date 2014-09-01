package ar.com.dcsys.firmware.database;

public interface InOutDAO {

	public void persist(InOut io);
	public InOut findBy(String personId);
	
}
