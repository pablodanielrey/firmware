package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.enroll.EnrollAndStoreInRam;
import ar.com.dcsys.firmware.cmd.enroll.EnrollData;
import ar.com.dcsys.firmware.cmd.enroll.EnrollResult;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Model;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.security.Finger;
import ar.com.dcsys.security.Fingerprint;

public class Enroll implements Cmd {

	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "enroll";
	
	private final EnrollAndStoreInRam enroll;
	
	private final Firmware app;
	private final Leds leds;
	private final SerialDevice sd;
	
	private final FingerprintSerializer fingerprintSerializer;
	
	@Inject
	public Enroll(Firmware app, SerialDevice sd, Leds leds, EnrollAndStoreInRam enroll, FingerprintSerializer fingerprintSerializer) {
		this.enroll = enroll;
		this.app = app;
		this.leds = leds;
		this.sd = sd;
		
		this.fingerprintSerializer = fingerprintSerializer;
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
		
			final String personId = cmd.substring(CMD.length() + 1);
			if (personId == null || "".equals(personId)) {
				remote.sendText("ERROR wrong person.id format");
				return;
			}
			
			
			Runnable r = new Runnable() {
				@Override
				public void run() {
							
					try {
						leds.onCommand(Leds.ENROLL);
						
						EnrollData ed = new EnrollData() {
							@Override
							public String getPersonId() {
								return personId;
							}
							
							@Override
							public Finger getFinger() {
								return Finger.LEFT_INDEX;
							}
						};
						
						enroll.execute(sd, new EnrollResult() {
										
							@Override
							public void onSuccess(final Fingerprint fp) {
																
								try {
									StringBuilder sb = new StringBuilder();
									
									sb.append("OK ");
									
									String fps = fingerprintSerializer.toJson(fp);
									sb.append(fps);
	//								String template = DatatypeConverter.printBase64Binary(fp.getTemplate());
									
									remote.sendText(sb.toString());
								
									leds.onCommand("ok");
									
								} catch (IOException e) {
									logger.log(Level.SEVERE, e.getMessage(),e);
									leds.onCommand("error");
								}
																
							}
							
							
							@Override
							public void onFailure(int errorCode) {
								try {
									leds.onCommand(Leds.ERROR);
									
									remote.sendText("ERROR " + String.valueOf(errorCode));
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void onCancel() {
								try {
									leds.onCommand(Leds.ERROR);
									
									
									remote.sendText("ERROR comando cancelado");
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void releaseFinger() {
								try {
									remote.sendText("OK levantar el dedo del lector");
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void onTimeout() {
								try {
									leds.onCommand(Leds.ERROR);
									
									remote.sendText("ERROR timeout");
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void onBadQuality() {
								try {
									leds.onCommand(Leds.ERROR);
									
									remote.sendText("ERROR mala calidad");
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void needThirdSweep() {
								try {
									leds.onCommand(Leds.PHASE_OK + ";3");
									
									remote.sendText("OK necesita tercera huella");
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void needSecondSweep() {
								try {
									leds.onCommand(Leds.PHASE_OK + ";2");
									
									remote.sendText("OK necesita segunda huella");
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							@Override
							public void needFirstSweep() {
								try {
									leds.onCommand(Leds.PHASE_OK + ";1");
									
									remote.sendText("OK necesita primera huella");
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, ed);
						
					} catch (CmdException e) {
						e.printStackTrace();
						logger.log(Level.SEVERE,e.getMessage(),e);
						try {
							remote.sendText("ERROR " + e.getMessage());
						} catch (IOException e1) {
							e1.printStackTrace();
							logger.log(Level.SEVERE,e1.getMessage(),e1);
						}
						
					}
				}
			};
			app.addCommand(r);
			
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(),e);
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
		}
		
	}
	
	
}
