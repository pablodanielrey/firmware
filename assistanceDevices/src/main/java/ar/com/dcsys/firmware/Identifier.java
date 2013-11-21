package ar.com.dcsys.firmware;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.cmd.Cmd;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.FpCancel;
import ar.com.dcsys.firmware.cmd.FpCancel.FpCancelResult;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.cmd.Identify.IdentifyResult;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.sound.Player;

public class Identifier implements Runnable, Cmd {

	private final Logger logger;
	private final SerialDevice sd;
	private final Identify identify;
	private final FpCancel cancel;
	private final Player player;

	private final String soundOk = "/ok.wav";
	private final String soundError = "/error.wav";
	
	private volatile boolean exit = false;
	
	private Semaphore terminate = new Semaphore(0);
	
	@Inject
	public Identifier(Logger logger, SerialDevice sd, Identify identify, FpCancel cancel, Player player) {
		this.logger = logger;
		this.sd = sd;
		this.identify = identify;
		this.cancel = cancel;
		this.player = player;
	}
	
	
	@Override
	public void run() {
		exit = false;
		while (!exit) {

			try {
				identify.execute(sd, new IdentifyResult() {
					
					@Override
					public void onSuccess(int fpNumber) {
						
						logger.info("Huella identificada : " + String.valueOf(fpNumber));
						
						try {
						    player.play(soundOk);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					
					@Override
					public void onNotFound() {
						
						logger.info("Huella no encontrada");
						
						try {
						    player.play(soundError);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					
					@Override
					public void onFailure(int errorCode) {
						
						logger.info("Error identificando la huella : " + errorCode);
						
						try {
						    player.play(soundError);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					
					@Override
					public void onCancel() {
						
						logger.info("Cancelando la identificación");
						
						exit = true;
						try {
						    player.play(soundOk);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				
			} catch (CmdException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
			}
		}
		
		// señala la finalización para que terminate() pueda retornar;
		terminate.release();
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
