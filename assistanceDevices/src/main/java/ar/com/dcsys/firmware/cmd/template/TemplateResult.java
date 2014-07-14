package ar.com.dcsys.firmware.cmd.template;

import ar.com.dcsys.security.Fingerprint;

public interface TemplateResult {
	
	public void onSuccess(Fingerprint fp);
	public void onFailure(int errorCode);
	public void onCancel();
	
}
