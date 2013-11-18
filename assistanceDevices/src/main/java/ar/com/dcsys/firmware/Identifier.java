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

public class Identifier implements Runnable {

	private final Logger logger;
	private final SerialDevice sd;
	private final Cmd identify;
	private final Cmd cancel;
	private final PersonDAO personDAO;
	
	private volatile boolean exit = false;
	
	@Inject
	public Identifier(Logger logger, SerialDevice sd, @Named("identify") Cmd identify, @Named("fpCancel") Cmd cancel, @Named("personHsqlDAO") PersonDAO personDAO) {
		this.logger = logger;
		this.sd = sd;
		this.identify = identify;
		this.cancel = cancel;
		this.personDAO = personDAO;
	}
	
	
	private class Sound {
		BufferedInputStream bin;
		AudioInputStream ain;
		DataLine.Info info;
		Clip clip;
		
		public void close() {
			try {
				clip.close();
				ain.close();
				bin.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private Sound getSound(String file) {
		try {
			BufferedInputStream sound = new BufferedInputStream(App.class.getResourceAsStream(file));
			try {
				AudioInputStream asound = AudioSystem.getAudioInputStream(sound);
				try {
					AudioFormat format = asound.getFormat();
					DataLine.Info info = new DataLine.Info(Clip.class, format);
					Clip source = (Clip)AudioSystem.getLine(info);
					source.open(asound);
					
					Sound s = new Sound();
					s.bin = sound;
					s.ain = asound;
					s.info = info;
					s.clip = source;
					return s;
					
					
				} catch (Exception e) {
					asound.close();
					throw e;
				}
			} catch (Exception e) {
				sound.close();
				throw e;
			}
			
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			return null;
		}
		
	}
	
	private void play(Sound s) throws IOException {
		if (s == null) {
			return;
		}
		s.clip.setFramePosition(0);
		s.clip.start();
	}
	
	
	@Override
	public void run() {
		final Sound error = getSound("/error.wav");
		final Sound ok = getSound("/ok.wav");
		
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
							play(ok);
						} catch (IOException e1) {
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
							play(error);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
					
					@Override
					public void onFailure(int code) {
						logger.info("Error " + String.valueOf(code)) ;
						try {
							play(error);
						} catch (IOException e) {
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
		
		error.close();
		ok.close();
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
