package ar.com.dcsys.firmware.cmd.template;

import ar.com.dcsys.security.FingerprintCredentials;

public interface TemplateResult {
	
	public void onSuccess(FingerprintCredentials fp);
	public void onFailure(int errorCode);
	public void onCancel();
	
}
