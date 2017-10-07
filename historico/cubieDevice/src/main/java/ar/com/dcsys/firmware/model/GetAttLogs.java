package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.assistance.server.AttLogSerializer;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.model.log.AttLogsManager;

public class GetAttLogs implements Cmd {

	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "getAttLogs";
	
	private final Leds leds;
	private final AttLogsManager attLogsManager;
	private final AttLogSerializer attLogSerializer;
	
	
	@Inject
	public GetAttLogs(Leds leds, 
						AttLogsManager attLogsManager,
						AttLogSerializer attLogSerializer) {

		this.leds = leds;
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
	public void setResponse(Response remote) {
		this.remote = remote;
	}
	
	@Override
	public void cancel() {
		cancel = true;
	}		
	
	private Response remote;
	private boolean cancel = false;
		
	
	@Override
	public void execute() {

		try {
			leds.onCommand(Leds.BLOCKED);
						
			List<String> ids = attLogsManager.findAll();
			remote.sendText("ok size = " + String.valueOf(ids.size()));
			
			int count = 0;
			for (String id : ids) {
				AttLog log = attLogsManager.findById(id);
				String json = attLogSerializer.toJson(log);
				remote.sendText("ok " + json);
				count++;
				leds.onCommand(Leds.SUB_OK);
				
				if (cancel) {
					break;
				}
			}

			remote.sendText("OK " + count);
			leds.onCommand(Leds.OK);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			leds.onCommand(Leds.ERROR);
			
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		} finally {
			cancel = false;
		}
		
	}					
	
}
