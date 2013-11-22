package ar.com.dcsys.firmware;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.cmd.Cmd;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.FpCancel;
import ar.com.dcsys.firmware.cmd.FpCancel.FpCancelResult;
import ar.com.dcsys.firmware.cmd.enroll.EnrollAndStoreInRam;
import ar.com.dcsys.firmware.cmd.enroll.EnrollData;
import ar.com.dcsys.firmware.cmd.enroll.EnrollResult;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId.GetEmptyIdResult;
import ar.com.dcsys.firmware.cmd.template.TemplateData;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate.WriteTemplateResult;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.security.Finger;
import ar.com.dcsys.security.FingerprintCredentials;

public class Enroller implements Runnable, Cmd {

	private final Logger logger;
	private final SerialDevice sd;
	private final EnrollAndStoreInRam enroll;
	private final GetEmptyId getEmptyId;
	private final WriteTemplate writeTemplate;
	private final FpCancel cancel;
	private final Semaphore terminate = new Semaphore(0);
	
	
	@Inject
	public Enroller(Logger logger, SerialDevice sd, EnrollAndStoreInRam enroll, GetEmptyId getEmptyId, WriteTemplate writeTemplate, FpCancel cancel) {
		this.logger = logger;
		this.sd = sd;
		this.enroll = enroll;
		this.cancel = cancel;
		this.getEmptyId = getEmptyId;
		this.writeTemplate = writeTemplate;
	}
	
	@Override
	public void run() {
		
		EnrollData edata = new EnrollData() {
			@Override
			public Finger getFinger() {
				return Finger.LEFT_INDEX;
			}
		};
		
		try {
			
			enroll.execute(sd, new EnrollResult() {
				@Override
				public void releaseFinger() {
					logger.info("Debe levantar el dedo del lector");
				}
				
				@Override
				public void onBadQuality() {
					logger.info("Mala calidad de la imagen");
				}
				
				@Override
				public void onTimeout() {
					logger.info("Expiró el tiempo de espera para tomar una huella");
				}
				
				@Override
				public void onSuccess(final FingerprintCredentials fp) {
					
					try {
						getEmptyId.execute(sd, new GetEmptyIdResult() {
							
							@Override
							public void onSuccess(final int tmplNumber) {
								
								TemplateData tdata = new TemplateData();
								tdata.setFingerprint(fp);
								tdata.setNumber(tmplNumber);
								
								try {
									writeTemplate.execute(sd, new WriteTemplateResult() {
										@Override
										public void onSuccess(int tmplNumber2) {
											
											if (tmplNumber2 != tmplNumber) {
												logger.log(Level.SEVERE,"SE ESCRIBIÓ LA HUELLA EN EL NÚMERO INCORRECTO!!! NUMERO A ESCRIBIR : " + tmplNumber + " NUMERO ESCRITO : " + tmplNumber2);
												
												// aca ver si genero una excepción y borro la huella del lugar escrito.
												
											}
											
											// aca genero el registro en la base de datos.
											logger.info("SE GENERA EL REGISTRO DENTRO DE LA BASE :\n HUELLA : " + Utils.getHex(fp.getTemplate()) + 
													    " NUMERO DE HUELLA : " + tmplNumber2);
											
										}
										@Override
										public void onFailure(int errorCode) {
											logger.info("Error código : " + errorCode);
										}
										@Override
										public void onCancel() {
											logger.info("Comando de enrolamiento cancelado");
										}
										@Override
										public void onInvalidTemplateSize(int size) {
											logger.info("Tamaño de huella inválido : " + size);
										}
										@Override
										public void onInvalidTemplateNumber(int number) {
											logger.info("Número de huella inválido : " + number);
										}
									}, tdata);
								
								} catch (CmdException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
								}
								
							}
							
							@Override
							public void onFailure(int errorCode) {
								logger.info("Error código : " + errorCode);
							}
							
							@Override
							public void onEmptyNotExistent() {
								logger.info("No existe ningún id libre para enrolar la huella");
							}
							
							@Override
							public void onCancel() {
								logger.info("Comando de enrolamiento cancelado");
							}
						});
					
					} catch (CmdException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}

				}
				
				@Override
				public void onFailure(int errorCode) {
					logger.info("Error código : " + errorCode);
				}
				
				@Override
				public void onCancel() {
					logger.info("Comando de enrolamiento cancelado");
				}
				
				@Override
				public void needThirdSweep() {
					logger.info("Ponga el dedo por tercera vez");
				}
				
				@Override
				public void needSecondSweep() {
					logger.info("Ponga el dedo por segunda vez");
				}
				
				@Override
				public void needFirstSweep() {
					logger.info("Ponga el dedo por primera vez");
				}
			}, edata);
		
		} catch (CmdException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
		
	}

	@Override
	public void terminate() throws CmdException {
		try {
			cancel.execute(sd, new FpCancelResult() {
				@Override
				public void onSuccess() {
					// espera a que se señalice el semáforo terminate.
					terminate.acquireUninterruptibly();
				};
			});
		} catch (CmdException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw e;
		}		
	}
	
}
