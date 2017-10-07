package ar.com.dcsys.firmware.database;

public class FingerprintMapping {

	private String personId;
	private String fingerprintId;
	private int fpNumber;
	
	public String getPersonId() {
		return personId;
	}
	
	public void setPersonId(String personId) {
		this.personId = personId;
	}
	
	public String getFingerprintId() {
		return fingerprintId;
	}
	
	public void setFingerprintId(String fingerprintId) {
		this.fingerprintId = fingerprintId;
	}
	
	public int getFpNumber() {
		return fpNumber;
	}
	
	public void setFpNumber(int fpNumber) {
		this.fpNumber = fpNumber;
	}
	
}
