package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;

public class Model {

	private final Logger logger = Logger.getLogger(Model.class.getName());
	private final List<Cmd> commands = new ArrayList<Cmd>();

	private final Firmware firmware;
	
	@Inject
	public Model(Firmware firmware, Identify identify, 
								   PersistPerson persistPerson,
				  				   PersistFingerprint persistFingerprint,
				  				   GetAttLogs getAttLogs,
				  				   DeleteAttLogs deleteAttLogs) {
		this.firmware = firmware;
	
		commands.add(persistPerson);
		commands.add(persistFingerprint);
		commands.add(getAttLogs);
		commands.add(deleteAttLogs);
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

				c.setResponse(response);
				firmware.addCommand(c);
				
				Cmd cmd = firmware.getRunningCommand();
				if (cmd != null) {
					try {
						cmd.cancel();
					} catch (CmdException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				}
				
				break;
			}
		}
		
	}
	
}
