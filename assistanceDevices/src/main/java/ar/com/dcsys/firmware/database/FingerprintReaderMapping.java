package ar.com.dcsys.firmware.database;

import java.io.Serializable;

public class FingerprintReaderMapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String fingerprintId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFingerprintId() {
		return fingerprintId;
	}

	public void setFingerprintId(String fingerprintId) {
		this.fingerprintId = fingerprintId;
	}
	
}
