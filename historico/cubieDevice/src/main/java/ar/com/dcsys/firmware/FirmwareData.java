package ar.com.dcsys.firmware;

import javax.inject.Inject;

public class FirmwareData {

	@Inject String generateIdentify;

	public boolean getGenerateIdentify() {
		return Boolean.valueOf(generateIdentify);
	}

	
	
	
}
