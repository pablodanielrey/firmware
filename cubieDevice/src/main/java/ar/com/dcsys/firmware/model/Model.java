package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.MutualExclusion;

public class Model {

	private final List<Cmd> commands = new ArrayList<Cmd>();

	private final Firmware firmware;
	
	@Inject
	public Model(Firmware firmware, Identify identify) {
		this.firmware = firmware;
		
		commands.add(identify);
	}
	
	
	public void onCommand(final String command, final Response response) {
		
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
		
		
		for (final Cmd c : commands) {
			if (c.identify(command)) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						c.execute(command, response);
						//MutualExclusion.using[MutualExclusion.CMD].release();
					}
				};
				firmware.addCommand(r);
				break;
			}
		}
		
	}
	
}
