package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.soap.ZKSoftwareCDI;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.model.log.AttLogsManager;

public class DeleteAttLogs implements Cmd {
	
	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "deleteAttLogs";
	
	private final AttLogsManager attLogsManager;
	private final ZkSoftware zkSoftware;
	
	private String cmd;
	private Response remote;
	private boolean cancel = false;
	
	@Inject
	public DeleteAttLogs(ZKSoftwareCDI zk, AttLogsManager attLogsManager) {
		this.attLogsManager = attLogsManager;
		this.zkSoftware = zk.getZkSoftware();
	}

	@Override
	public String getCommand() {
		return CMD;
	}

	@Override
	public boolean identify(String cmd) {
		if (cmd.startsWith(CMD)) {
			this.cmd = cmd;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setResponse(Response remote) {
		this.remote = remote;
	}

	@Override
	public void execute() {
		try {
			
			String params = cmd.substring(CMD.length() + 1);
			String[] ids = params.split(";");
			if (ids == null) {
				throw new Exception("ids == null");
			}
			
			int count = 0;
			for (String id : ids) {
				attLogsManager.remove(id);
				remote.sendText("ok " + id);
				count++;
				if (cancel) {
					break;
				}
			}

			remote.sendText("OK " + count);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			
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

	@Override
	public void cancel() throws CmdException {
		cancel = true;
	}

}
