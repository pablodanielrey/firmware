package ar.com.dcsys.firmware.model;

import java.io.IOException;

public interface Response {

	public void sendText(String text) throws IOException;
	
}
