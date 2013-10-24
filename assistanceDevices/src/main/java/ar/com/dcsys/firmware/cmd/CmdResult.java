package ar.com.dcsys.firmware.cmd;


public interface CmdResult {
	public void onSuccess();
	public void onSuccess(int i);
	public void onSuccess(byte[] data);
	public void onFailure();
	public void onFailure(int code);
}
