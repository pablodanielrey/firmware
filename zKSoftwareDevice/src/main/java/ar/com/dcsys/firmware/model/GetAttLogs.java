package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.assistance.server.AttLogSerializer;
import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.common.AttLogUtils;
import ar.com.dcsys.firmware.soap.ZKSoftwareCDI;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.firmware.soap.ZkSoftwareException;
import ar.com.dcsys.model.PersonsManager;

public class GetAttLogs implements Cmd {
	
	private static final Logger logger = Logger.getLogger(GetAttLogs.class.getName());
	public static final String CMD = "getAttLogs";
	
	private final ZkSoftware zkSoftware;
	private final Device device;
	private final PersonsManager personsManager;
	private final AttLogSerializer attLogSerializer;
	private Response remote;
	
	@Inject
	public GetAttLogs(ZKSoftwareCDI zk, Device device, PersonsManager personsManager, AttLogSerializer attLogSerializer) {
		this.zkSoftware = zk.getZkSoftware();
		this.device = device;
		this.personsManager = personsManager;
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
	public void execute() {
		try {
			List<AttLog> logs = AttLogUtils.convertLogs(this.personsManager,this.device,this.zkSoftware.getAllAttLogs());
			
			int count = 0;
			for (AttLog log : logs) {
				String json = attLogSerializer.toJson(log);
				remote.sendText("ok " + json);
				count++;				
			}
			
			remote.sendText("OK " + count);
			
		} catch (PersonException | IOException | ZkSoftwareException e) {
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		}
	}
	@Override
	public void cancel() throws CmdException {
		// TODO Auto-generated method stub
		
	}
	
	

}
