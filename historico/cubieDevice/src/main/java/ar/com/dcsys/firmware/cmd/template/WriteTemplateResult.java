package ar.com.dcsys.firmware.cmd.template;

public interface WriteTemplateResult {
	
	public void onSuccess(int tmplNumber);
	public void onFailure(int errorCode);
	public void onCancel();
	public void onInvalidTemplateSize(int size);
	public void onInvalidTemplateNumber(int number);
	
}