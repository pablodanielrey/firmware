package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;

import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.firmware.cmd.template.ClearAllTemplate.ClearAllTemplateResult;
import ar.com.dcsys.firmware.database.FingerprintMappingDAO;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.reader.PurgeTemplates;
import ar.com.dcsys.security.Fingerprint;

public class RegenerateFingerprintsInReader implements Cmd {

	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "regenerateFingerprints";
	
	private final Leds leds;
	private final Provider<PersistFingerprint> persistFingerprint;
	private final Provider<PurgeTemplates> purgeTemplates;
	private final FingerprintDAO fingerprintDAO;
	private final FingerprintMappingDAO fingerprintMappingDAO;
	
	@Inject
	public RegenerateFingerprintsInReader(Leds leds,
						Provider<PersistFingerprint> persistFingerprint,
						Provider<PurgeTemplates> purgeTemplates,
						FingerprintDAO fingerprintDAO,
						FingerprintMappingDAO fingerprintMappingDAO) {

		this.leds = leds;
		this.persistFingerprint = persistFingerprint;
		this.purgeTemplates = purgeTemplates;
		this.fingerprintDAO = fingerprintDAO;
		this.fingerprintMappingDAO = fingerprintMappingDAO;
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
	
	private class CountContainer { int count = 0; };
	private class BooleanContainer { boolean v = false; };
	
	@Override
	public void execute() {

		try {
			leds.onCommand(Leds.BLOCKED);
			
			final CountContainer count = new CountContainer();
			
			Response resp = new Response() {
				@Override
				public void sendText(String text) throws IOException {
					if (text.startsWith("OK")) {
						count.count = count.count + 1;
						remote.sendText("ok " + String.valueOf(count.count));
					} else {
						remote.sendText("error " + text);
					}
				}
			};
			
			final BooleanContainer purgeError = new BooleanContainer();
			
			PurgeTemplates pt = purgeTemplates.get();
			pt.execute(new ClearAllTemplateResult() {
				@Override
				public void onSuccess(int number) {
					purgeError.v = false;
					try {
						remote.sendText("ok " + String.valueOf(number) + " reader purged");
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						try {
							remote.sendText("ERROR " + e.getMessage());
						} catch (IOException e1) {
							logger.log(Level.SEVERE,e1.getMessage(),e1);
						}
					}
					
				}
				@Override
				public void onFailure(int errorCode) {
					purgeError.v = true;
					try {
						remote.sendText("ERROR " + String.valueOf(errorCode));
					} catch (IOException e1) {
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}
				}
				@Override
				public void onCancel() {
					purgeError.v = true;
					try {
						remote.sendText("ERROR canceled");
					} catch (IOException e1) {
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}
				}
			});
			
			// si existió un error en el purge de las huellas retorno y no sigo con nada.
			if (purgeError.v) {
				return;
			}
			
			fingerprintMappingDAO.deleteAll();
			remote.sendText("ok mappings deleted");
			
			List<String> ids = fingerprintDAO.findAll();
			remote.sendText("ok " + ids.size());
			
			for (String id : ids) {
				Fingerprint fp = fingerprintDAO.findById(id);
				PersistFingerprint pf = persistFingerprint.get();
				pf.setResponse(resp);
				pf.execute(fp);
			}
						
			
			if (count.count == ids.size()) {
				remote.sendText("OK " + String.valueOf(count.count));
				leds.onCommand(Leds.OK);
			} else {
				remote.sendText("Error " + String.valueOf(count.count));
				leds.onCommand(Leds.ERROR);
			}
			
			
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
