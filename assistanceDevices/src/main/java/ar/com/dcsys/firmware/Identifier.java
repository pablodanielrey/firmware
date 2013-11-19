package ar.com.dcsys.firmware;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.data.person.PersonDAO;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.cmd.Cmd;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.CmdResult;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.sound.Player;

public class Identifier implements Runnable {

	private final Logger logger;
	private final SerialDevice sd;
	private final Cmd identify;
	private final Cmd cancel;
	private final PersonDAO personDAO;

	private final String soundOk = "/ok.wav";
	private final String soundError = "/error.wav";
	
	private volatile boolean exit = false;
	
	@Inject
	public Identifier(Logger logger, SerialDevice sd, @Named("identify") Cmd identify, @Named("fpCancel") Cmd cancel, @Named("personHsqlDAO") PersonDAO personDAO) {
		this.logger = logger;
		this.sd = sd;
		this.identify = identify;
		this.cancel = cancel;
		this.personDAO = personDAO;
	}
	
	
	@Override
	public void run() {
		exit = false;
		while (!exit) {

			try {
				identify.execute(sd, new CmdResult() {
					
					@Override
					public void onSuccess(byte[] data) {
						logger.info("Datos : " + Utils.getHex(data));
					}
					
					@Override
					public void onSuccess(int i) {
						logger.info("Huella identificada : " + String.valueOf(i));
						try {
							Player.play(soundOk);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						
			    		try {
							List<Person> persons = personDAO.findAll();
				    		for (Person p2 : persons) {
				    			System.out.println(p2.getId());
				    		}
						} catch (PersonException e) {
							e.printStackTrace();
						}						
						
						
					}
					
					@Override
					public void onSuccess() {
						// nunca es llamado
					}
					
					@Override
					public void onFailure() {
						logger.info("No se pudo identificar la huella");
						try {
							Player.play(soundError);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					
					@Override
					public void onFailure(int code) {
						logger.info("Error " + String.valueOf(code)) ;
						try {
							Player.play(soundError);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						switch (code) {
							case CamabioUtils.ERR_FP_CANCEL:
								logger.info("Comando cancelado");
								exit = true;
								break;
							case CamabioUtils.ERR_BAD_CUALITY:
								logger.info("Mala calidad de la imagen!!");
								break;
							case CamabioUtils.ERR_ALL_TMPL_EMPTY:
								logger.info("No existe ninguna huella enrolada");
								break;
							case CamabioUtils.ERR_TIME_OUT:
								logger.info("Timeout");
								break;
						}
					}
				});
	    		
			} catch (CmdException e) {
				e.printStackTrace();
			}		
		
		}
	}
	
	public void terminate() {
		try {
			cancel.execute(sd, new CmdResult() {
				@Override
				public void onSuccess(byte[] data) {
					logger.info("Cancel ok");
				}
				
				@Override
				public void onSuccess(int i) {
					logger.info("Cancel ok");
				}
				
				@Override
				public void onSuccess() {
					logger.info("Cancel ok");
				}
				
				@Override
				public void onFailure(int code) {
					logger.info("Cancel Failure");
				}
				
				@Override
				public void onFailure() {
					logger.info("Cancel Failure");
				}
			});
		} catch (CmdException e) {
			e.printStackTrace();
		}
	}
	
}
