package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.assistance.server.AttLogSerializer;
import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.exceptions.AttLogException;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.common.AttLogUtils;
import ar.com.dcsys.firmware.database.Initialize;
import ar.com.dcsys.firmware.soap.SoapDevice;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.firmware.soap.ZkSoftwareException;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.model.log.AttLogsManager;

public class GetAttLogs implements Cmd {
	
	private static final Logger logger = Logger.getLogger(GetAttLogs.class.getName());
	public static final String CMD = "getAttLogs";
	
	private final ZkSoftware zkSoftware;
	private final Initialize initialize;
	private final PersonsManager personsManager;
	private final AttLogsManager attLogsManager;
	private final AttLogSerializer attLogSerializer;
	private Response remote;
	
	@Inject
	public GetAttLogs(SoapDevice zk, Initialize initialize, PersonsManager personsManager, AttLogSerializer attLogSerializer, AttLogsManager attLogsManager) {
		this.zkSoftware = zk.getZkSoftware();
		this.initialize = initialize;
		this.personsManager = personsManager;
		this.attLogSerializer = attLogSerializer;
		this.attLogsManager = attLogsManager;
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
			
			//verifico si hay nuevos logs en el reloj, si los hay los agrego
			Device device = initialize.getCurrentDevice();
			List<ar.com.dcsys.firmware.soap.AttLog> logsDevice = this.zkSoftware.getAllAttLogs();
			for (ar.com.dcsys.firmware.soap.AttLog l : logsDevice) {
				AttLog log =  AttLogUtils.convertLog(personsManager, device, l);
				attLogsManager.persist(log);
			}
			
			//retorno los logs de la base
			int count = 0;
			List<String> logs = attLogsManager.findAll();
			for (String logId : logs) {
				AttLog log = attLogsManager.findById(logId); 
				String json = attLogSerializer.toJson(log);
				remote.sendText("ok " + json);
				count++;				
			}
			
			remote.sendText("OK " + count);
			
		} catch (PersonException | IOException | ZkSoftwareException | AttLogException | DeviceException e) {
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
