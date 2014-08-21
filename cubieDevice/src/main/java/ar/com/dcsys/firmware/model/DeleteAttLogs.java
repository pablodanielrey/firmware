package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.assistance.server.AttLogSerializer;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.model.log.AttLogsManager;
import ar.com.dcsys.person.server.PersonSerializer;

public class DeleteAttLogs implements Cmd {

	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "deleteAttLogs";
	
	private final Leds leds;
	private final PersonsManager personsManager;
	private final PersonSerializer personSerializer;
	private final AttLogsManager attLogsManager;
	private final AttLogSerializer attLogSerializer;
	
	
	@Inject
	public DeleteAttLogs(Leds leds, 
						AttLogsManager attLogsManager,
						AttLogSerializer attLogSerializer,
						PersonsManager personsManager,
						PersonSerializer personSerializer) {

		this.leds = leds;
		this.personsManager = personsManager;
		this.personSerializer = personSerializer;
		this.attLogsManager = attLogsManager;
		this.attLogSerializer = attLogSerializer;
	}
	
	
	
	@Override
	public String getCommand() {
		return CMD;
	}
	
	@Override
	public boolean identify(String cmd) {
		return cmd.startsWith(CMD);
	}
	
	
	@Override
	public void execute(String cmd, final Response remote) {

		try {
			leds.onCommand(Leds.BLOCKED);
						
			String params = cmd.substring(CMD.length() + 1);
			String[] ids = params.split(";");
			if (ids == null) {
				throw new Exception("ids == null");
			}
			
			for (String id : ids) {
				attLogsManager.remove(id);
				remote.sendText("ok " + id);
				leds.onCommand(Leds.SUB_OK);
			}

			leds.onCommand(Leds.OK);
			remote.sendText("OK " + ids.length);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			leds.onCommand(Leds.ERROR);
			
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		}
		
	}					
	
}
