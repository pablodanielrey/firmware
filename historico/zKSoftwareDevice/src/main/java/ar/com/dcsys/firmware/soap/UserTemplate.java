package ar.com.dcsys.firmware.soap;

import java.io.Serializable;

public class UserTemplate implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String pin;
	
	private String fingerId;
	
	private String size;
	
	private String valid;
	
	private String template;

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getFingerId() {
		return fingerId;
	}

	public void setFingerId(String fingerId) {
		this.fingerId = fingerId;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getValid() {
		return valid;
	}

	public void setValid(String valid) {
		this.valid = valid;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

}
