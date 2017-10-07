package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

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


public class Identify implements Cmd {
	
	public static final String CMD = "identify";
	private static final Logger logger = Logger.getLogger(Identify.class.getName());
	
	private final SoapDevice zkDevice;
	private final AttLogsManager attLogsManager;
	private final PersonsManager personsManager;
	private final Initialize initialize;
	private Response remote;
	private final Semaphore runnig = new Semaphore(1);
	
	@Inject
	public Identify(SoapDevice zk, Initialize initialize, AttLogsManager attLogsManager, PersonsManager personsManager) {
		this.zkDevice = zk;
		this.attLogsManager = attLogsManager;
		this.personsManager = personsManager;
		this.initialize = initialize;
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

	
	/**
	 * TODO: Obtengo las huelllas del reloj y las agrego en la base, si ya existe no la agrega
	 * @throws DeviceException 
	 */
	
	@Override
	public void execute() {
		try {
			
			ZkSoftware zkSoftware = this.zkDevice.getZkSoftware();
			Device device = initialize.getCurrentDevice();
			
			//obtengo los logs del reloj
			List<ar.com.dcsys.firmware.soap.AttLog> logsDevice = zkSoftware.getAllAttLogs();
			
			
			//los agrego en la base, si ya existe el modelo no lo agrega
			for (ar.com.dcsys.firmware.soap.AttLog l : logsDevice) {
				AttLog log = AttLogUtils.convertLog(personsManager, device, l);
				attLogsManager.persist(log);
			}
			
			//borro todas las huellas del reloj
			/*
			 * TODO: descomentar cuando se pase a producción
			 */
			//this.zkSoftware.deleteAttLogs();
			//this.zkSoftware.refreshDB();
								
			remote.sendText("OK");
			
		} catch (IOException | ZkSoftwareException | PersonException | AttLogException | DeviceException e) {
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		} finally {
			runnig.release();
		}
	}

	@Override
	public void cancel() throws CmdException {
		// TODO Auto-generated method stub
		
	}


}
