package ar.com.dcsys.firmware.cmd.enroll;

import ar.com.dcsys.security.FingerprintCredentials;

public interface EnrollResult {
	public void onSuccess(FingerprintCredentials fp);
	public void onFailure(int errorCode);
}
