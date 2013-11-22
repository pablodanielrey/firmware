package ar.com.dcsys.firmware.cmd.enroll;

import ar.com.dcsys.security.FingerprintCredentials;

public interface EnrollResult {
	
	public void needFirstSweep();
	public void needSecondSweep();
	public void needThirdSweep();
	
	public void releaseFinger();
	
	public void onTimeout();
	public void onBadQuality();
	
	public void onSuccess(FingerprintCredentials fp);
	public void onFailure(int errorCode);
	public void onCancel();
}
