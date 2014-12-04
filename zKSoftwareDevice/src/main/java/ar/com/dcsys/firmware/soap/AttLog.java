package ar.com.dcsys.firmware.soap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AttLog {
	private String pin;
	private String dateTime;
	private String verifiedMode;
	private String status;
	private String workCode;
	
	public String getPin() {
		return pin;
	}
	
	public void setPin(String pin) {
		this.pin = pin;
	}
	
	/**
	 * el formato es : 2011-11-09 08:49:34
	 * @return
	 */
	public String getDateTime() {
		return dateTime;
	}
	
	/**
	 * el formato es : 2011-11-09 08:49:34
	 * @param dateTime
	 */
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	
	public String getVerifiedMode() {
		return verifiedMode;
	}
	
	public void setVerifiedMode(String verifiedMode) {
		this.verifiedMode = verifiedMode;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getWorkCode() {
		return workCode;
	}
	
	public void setWorkCode(String workCode) {
		this.workCode = workCode;
	}

	public Date getDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return df.parse(getDateTime());
		} catch (ParseException e) {
			return null;
		}
	}
}
