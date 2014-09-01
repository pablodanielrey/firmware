package ar.com.dcsys.firmware.model;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.AttLogException;
import ar.com.dcsys.exceptions.DeviceException;
import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.FpCancel;
import ar.com.dcsys.firmware.cmd.FpCancel.FpCancelResult;
import ar.com.dcsys.firmware.cmd.Identify.IdentifyResult;
import ar.com.dcsys.firmware.database.FingerprintMapping;
import ar.com.dcsys.firmware.database.FingerprintMappingDAO;
import ar.com.dcsys.firmware.database.FingerprintMappingException;
import ar.com.dcsys.firmware.database.Initialize;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.inout.InOutModel;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.model.log.AttLogsManager;

public class Identify implements Cmd {

	private static final Logger logger = Logger.getLogger(Model.class.getName());
	public static final String CMD = "identify";
	
	private final FpCancel fpCancel;
	private final ar.com.dcsys.firmware.cmd.Identify identify;
	private final Initialize initialize;

	private final Leds leds;
	private final InOutModel inOutModel;
	private final SerialDevice sd;
	
	private final AttLogsManager attLogsManager;
	private final FingerprintMappingDAO fingerprintMappingDAO;
	
	@Inject
	public Identify(SerialDevice sd, Leds leds, InOutModel inOutModel,
										ar.com.dcsys.firmware.cmd.Identify identify,
										FpCancel fpCancel,
										Initialize initialize, 
										FingerprintMappingDAO fingerprintMappingDAO,
										AttLogsManager attLogsManager) {
		this.identify = identify;
		this.fpCancel = fpCancel;
		this.initialize = initialize;
		this.leds = leds;
		this.inOutModel = inOutModel;
		this.sd = sd;
		
		this.attLogsManager = attLogsManager;
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
	
	/**
	 * Crea el registro de assitencia para el reloj actual y para la persona y huella identificada con fpNumber
	 * @param fpNumber
	 */
	private String createAssistanceLog(int fpNumber) throws AttLogException, FingerprintMappingException, DeviceException {
		
		FingerprintMapping fpm = fingerprintMappingDAO.findBy(fpNumber);
		if (fpm == null) {
			throw new FingerprintMappingException("No existe mapping con ese id de huella " + String.valueOf(fpNumber));
		}
		
		Device d = initialize.getCurrentDevice();
		Person p = new Person();
		p.setId(fpm.getPersonId());
		
		
		AttLog log = new AttLog();
		log.setDate(new Date());
		log.setDevice(d);
		log.setId(UUID.randomUUID().toString());
		log.setPerson(p);
		log.setVerifyMode(1l);
			
		attLogsManager.persist(log);
			
		inOutModel.onLog(log.getPerson().getId());
		
		return fpm.getPersonId();
	}	
	
	
	@Override
	public void setResponse(Response remote) {
		this.remote = remote;
	}
	
	@Override
	public void cancel() throws CmdException {
		if (running.availablePermits() == 0) {
			fpCancel.execute(sd, new FpCancelResult() {
				@Override
				public void onSuccess() {
					logger.log(Level.FINE, "identify canceled");
				}
			});
		}
	}		
	
	private final Semaphore running = new Semaphore(1);
	private Response remote;
	
	@Override
	public void execute() {

		running.acquireUninterruptibly();
		try {
			leds.onCommand(Leds.IDENTIFY);
			
			identify.execute(sd, new IdentifyResult() {
				
				@Override
				public void releaseFinger() {
					try {
						
						remote.sendText("ok liberar dedo");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void onSuccess(int fpNumber) {
					
					try {
						String person = createAssistanceLog(fpNumber);
						
						// disparo el thread de sincronizacion de los logs.
						MutualExclusion.using[MutualExclusion.NEED_ATTLOGS_SYNC].release();
						
						leds.onCommand(Leds.OK);
						
						remote.sendText("OK " + person + " " + String.valueOf(fpNumber));

						
					} catch (AttLogException | FingerprintMappingException | DeviceException | IOException e1) {
						logger.log(Level.SEVERE,e1.getMessage(),e1);

						leds.onCommand(Leds.ERROR);
						
						try {
							remote.sendText("ERROR " + e1.getMessage());
						
						} catch (IOException e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}					
					}
					
				}
				
				@Override
				public void onNotFound() {
					try {
						leds.onCommand(Leds.ERROR);
						remote.sendText("OK huella no encontrada");
													
					} catch (IOException e) {
						e.printStackTrace();
										
					}
				}
				
				@Override
				public void onBadQuality() {
					try {
						leds.onCommand(Leds.ERROR);
						remote.sendText("OK mala calidad de la huella");
													
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
						remote.sendText("ERROR identificación cancelada");
						
					} catch (IOException e) {
						e.printStackTrace();
						
					}
				}
			});
			
		} catch (CmdException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			leds.onCommand(Leds.ERROR);
			
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}

		} finally {
			running.release();
		}
	}					
	
}
