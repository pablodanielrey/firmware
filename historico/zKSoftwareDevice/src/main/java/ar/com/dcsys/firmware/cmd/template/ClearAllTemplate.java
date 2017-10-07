package ar.com.dcsys.firmware.cmd.template;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.soap.ZkSoftware;
import ar.com.dcsys.firmware.soap.ZkSoftwareException;

public class ClearAllTemplate {
	
	public interface ClearAllTemplateResult {
		public void onSuccess(int number);
		public void onFailure(int errorCode);
		public void onCancel();
	}
	
	private static final Logger logger = Logger.getLogger(ClearAllTemplate.class.getName());
	
	public void execute (ZkSoftware zk, ClearAllTemplateResult result) throws CmdException {
		try {
			if (zk.deleteTemplates()) {
				result.onSuccess(0);
			} else {
				result.onFailure(1);
			}
		} catch (IOException | ZkSoftwareException e) {
			logger.log(Level.SEVERE, e.getMessage());
			throw new CmdException(e);
		}
	}

}
