package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.exceptions.FingerprintException;
import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId.GetEmptyIdResult;
import ar.com.dcsys.firmware.cmd.template.TemplateData;
import ar.com.dcsys.firmware.cmd.template.WriteTemplateResult;
import ar.com.dcsys.firmware.database.FingerprintMapping;
import ar.com.dcsys.firmware.database.FingerprintMappingDAO;
import ar.com.dcsys.firmware.database.FingerprintMappingException;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.reader.GetEmptyId;
import ar.com.dcsys.firmware.reader.WriteRawTemplate;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Fingerprint;

public class PersistFingerprint implements Cmd {

	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "persistFingerprint";
	
	private final Leds leds;
	private final FingerprintSerializer fingerprintSerializer;
	private final FingerprintMappingDAO fingerprintMappingDAO;
	private final FingerprintDAO fingerprintDAO;
	
	private final GetEmptyId getEmptyId;
	private final WriteRawTemplate writeRawTemplate;
	
	
	@Inject
	public PersistFingerprint(Leds leds, 
						FingerprintDAO fingerprintDAO,
						FingerprintMappingDAO fingerprintMappingDAO,
						FingerprintSerializer fingerprintSerializer,
						PersonsManager personsManager,
						PersonSerializer personSerializer,
						WriteRawTemplate writeRawTemplate,
						GetEmptyId getEmptyId) {

		this.leds = leds;
		this.fingerprintMappingDAO = fingerprintMappingDAO;
		this.fingerprintSerializer = fingerprintSerializer;
		this.fingerprintDAO = fingerprintDAO;
		
		this.writeRawTemplate = writeRawTemplate;
		this.getEmptyId = getEmptyId;

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
			
			String json = cmd.substring(CMD.length() + 1);
			Fingerprint fp = fingerprintSerializer.read(json);
			
			
			try {
				// actualizo la huella en la base.
				fingerprintDAO.persist(fp);
				
				leds.onCommand(Leds.SUB_OK);
				
			} catch (FingerprintException fe) {
				try {
					remote.sendText("ERROR " + fe.getMessage());
					return;
							
				} catch (IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					return;
					
				} finally {
					leds.onCommand(Leds.ERROR);
				}
			}
			
			updateFingerprintIntoReader(fp,remote);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			
		} finally {
			MutualExclusion.using[MutualExclusion.DISABLE_GENERATOR].release();
		}
	}
	
	
	
	
	
	private void updateFingerprintIntoReader(final Fingerprint fp, final Response remote) {
			
			
			//chequeo si ya existe el mapeo dentro del lector.
			final String personId = fp.getPersonId();
			final String fingerprintId = fp.getId();
			FingerprintMapping fpm = null;
			try {
				fpm = fingerprintMappingDAO.fingBy(personId, fingerprintId);
				
				leds.onCommand(Leds.SUB_OK);
				
			} catch (FingerprintMappingException e2) {
				try {
					remote.sendText("ERROR " + e2.getMessage());
					return;
					
				} catch (IOException e1) {
					e1.printStackTrace();
					logger.log(Level.SEVERE,e1.getMessage(),e1);
					return;
					
				} finally {
					leds.onCommand(Leds.ERROR);
					
				}
			}					
			
			
			
			final WriteTemplateResult writeTemplateResult = new WriteTemplateResult() {
				
				@Override
				public void onSuccess(int tmplNumber) {
					leds.onCommand(Leds.OK);
					try {
						remote.sendText("OK " + String.valueOf(tmplNumber));
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				}
				
				@Override
				public void onInvalidTemplateSize(int size) {
					leds.onCommand(Leds.ERROR);
					try {
						remote.sendText("ERROR invalid template size");
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					
				}
				
				@Override
				public void onInvalidTemplateNumber(int number) {
					leds.onCommand(Leds.ERROR);
					try {
						remote.sendText("ERROR invalid template number");
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					
				}
				
				@Override
				public void onFailure(int errorCode) {
					leds.onCommand(Leds.ERROR);
					try {
						remote.sendText("ERROR " + String.valueOf(errorCode));
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				}
				
				@Override
				public void onCancel() {
					leds.onCommand(Leds.ERROR);
					try {
						remote.sendText("ERROR canceled");
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				}
			};
			
			
			
			if (fpm != null) {
				
				// sobre escribo la rom del lector en la posicion que ya tenía esa huella.
				
				TemplateData tedata = new TemplateData();
				tedata.setFingerprint(fp);
				tedata.setNumber(fpm.getFpNumber());

				try {
					writeRawTemplate.execute(tedata,writeTemplateResult);

				} catch (CmdException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					leds.onCommand(Leds.ERROR);
					try {
						remote.sendText("ERROR");
					} catch (IOException e1) {
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}
				}						
				
			} else {
			
				try {
					getEmptyId.execute(new GetEmptyIdResult() {
						
						@Override
						public void onSuccess(int tmplNumber) {
							
							try {
								leds.onCommand(Leds.SUB_OK);
								// genero el mapeo en la base
								
								FingerprintMapping fpMapping = new FingerprintMapping();
								fpMapping.setFingerprintId(fingerprintId);
								fpMapping.setPersonId(personId);
								fpMapping.setFpNumber(tmplNumber);
								
								fingerprintMappingDAO.persist(fpMapping);
								
								
								leds.onCommand(Leds.SUB_OK);
								
								// escribo la rom del lector.
								
								TemplateData tedata = new TemplateData();
								tedata.setFingerprint(fp);
								tedata.setNumber(tmplNumber);

								try {
									writeRawTemplate.execute(tedata,writeTemplateResult);

								} catch (CmdException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
									leds.onCommand(Leds.ERROR);
									try {
										remote.sendText("ERROR");
									} catch (IOException e1) {
										logger.log(Level.SEVERE,e1.getMessage(),e1);
									}
								}		
								
							} catch (FingerprintMappingException e) {
								try {
									remote.sendText("ERROR " + e.getMessage());
									
								} catch (IOException e1) {
									e1.printStackTrace();
									logger.log(Level.SEVERE,e1.getMessage(),e1);
									
								} finally {
									leds.onCommand("error");
								}
								
							}
							
						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText("ERROR " + String.valueOf(errorCode));
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
								
							} finally {
								leds.onCommand(Leds.ERROR);
							}							
						}
						
						@Override
						public void onEmptyNotExistent() {
							try {
								remote.sendText("ERROR no existe lugar libre en la rom del lector");
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
								
							} finally {
								leds.onCommand(Leds.ERROR);									
							}							
						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("ERROR comando cancelado");
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
								
							} finally {
								leds.onCommand(Leds.ERROR);									
							}							
						}
					});

					
				} catch (CmdException cmdE) {
					try {
						remote.sendText("ERROR " + cmdE.getMessage());
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					} finally {
						leds.onCommand(Leds.ERROR);							
					}						
				}
			}
					
		}					
	
}
