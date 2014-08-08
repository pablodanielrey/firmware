package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class Model {

	private final List<Cmd> commands = new ArrayList<Cmd>();

	
	@Inject
	public Model(Identify identify) {
		
		commands.add(identify);
		
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
