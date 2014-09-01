package ar.com.dcsys.firmware.cmd.enroll;

import ar.com.dcsys.firmware.cmd.template.TemplateResult;


public interface EnrollResult extends TemplateResult {
	
	public void needFirstSweep();
	public void needSecondSweep();
	public void needThirdSweep();
	
	public void releaseFinger();
	
	public void onDuplicated();
	
	public void onTimeout();
	public void onBadQuality();
	
	

}
