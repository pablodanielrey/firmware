package ar.com.dcsys.firmware.database;

public class InOut {

	public enum Status {
		IN, OUT
	}
	
	private String id;
	private String personId;
	private Status status;


	public InOut(String id, String personId, Status status) {
		this.id = id;
		this.personId = personId;
		this.status = status;
	}
	
	
	public InOut(String personId, Status status) {
		this.personId = personId;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}
	
	
}
