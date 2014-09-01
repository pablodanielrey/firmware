package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.MutualExclusion;
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
		if (cmd.startsWith(CMD)) {
			ExecutionContext ex = new ExecutionContext();
			ex.cmd = cmd;
			contexts.add(ex);
			return true;
		} else {
			return false;
		}
	}
	
	
	@Override
	public void setResponse(Response remote) {
		contexts.getLast().remote = remote;
	}
	
	@Override
	public void cancel() {
		
	}		
	

	private class ExecutionContext {
		String cmd;
		Response remote;
	}

	private final LinkedList<ExecutionContext> contexts = new LinkedList<Enroll.ExecutionContext>();
	
	
	@Override
	public void execute() {

		ExecutionContext ctx = contexts.removeFirst();
		String cmd = ctx.cmd;
		final Response remote = ctx.remote;
		
		try {
		
			final String personId = cmd.substring(CMD.length() + 1);
			if (personId == null || "".equals(personId)) {
				remote.sendText("ERROR wrong person.id format");
				return;
			}
			
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
				
				private int phase = 1;
							
				@Override
				public void onSuccess(final Fingerprint fp) {
													
					try {
						StringBuilder sb = new StringBuilder();
						
						sb.append("OK ");
						
						String fps = fingerprintSerializer.toJson(fp);
						sb.append(fps);
//								String template = DatatypeConverter.printBase64Binary(fp.getTemplate());
						
						remote.sendText(sb.toString());
					
						leds.onCommand(Leds.OK);
						
					} catch (IOException e) {
						logger.log(Level.SEVERE, e.getMessage(),e);
						leds.onCommand(Leds.ERROR);
					}
													
				}
				
				
				@Override
				public void onDuplicated() {
					try {
						leds.onCommand(Leds.ERROR);
						
						remote.sendText("ERROR huella duplicada");
						
					} catch (IOException e) {
						e.printStackTrace();
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
						leds.onCommand(Leds.READY);
						
						remote.sendText("ERROR comando cancelado");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void releaseFinger() {
					try {
						remote.sendText("ok levantar el dedo del lector");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void onTimeout() {
					try {
						leds.onCommand(Leds.PHASE_ERROR + ";" + String.valueOf(phase));
					
						remote.sendText("error timeout");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void onBadQuality() {
					try {
						leds.onCommand(Leds.PHASE_ERROR + ";" + String.valueOf(phase));
					
						remote.sendText("error mala calidad");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void needThirdSweep() {
					try {
						phase = 3;
						leds.onCommand(Leds.PHASE_OK + ";3");
						
						remote.sendText("ok necesita tercera huella");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void needSecondSweep() {
					try {
						phase = 2;
						leds.onCommand(Leds.PHASE_OK + ";2");
						
						remote.sendText("ok necesita segunda huella");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void needFirstSweep() {
					try {
						phase = 1;
						leds.onCommand(Leds.PHASE_OK + ";1");
						
						remote.sendText("ok necesita primera huella");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, ed);
			
		} catch (IOException | CmdException e) {
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
	
	
}
