package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;

public class Reader {

	private final List<Cmd> commands = new ArrayList<Cmd>();

	
	@Inject
	public Reader(GetEmptyId getEmptyId) {
		
		commands.add(getEmptyId);
		
	}
	
	public void onCommand(String command, Response response) {
		
		if (command.equalsIgnoreCase("help")) {
			StringBuilder sb = new StringBuilder();
			for (Cmd c : commands) {
				sb.append(c.getCommand()).append("\n");
			}
			try {
				response.sendText(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		
		for (Cmd c : commands) {
			if (c.identify(command)) {
				c.execute(command, response);
				break;
			}
		}
		
	}
	
}
