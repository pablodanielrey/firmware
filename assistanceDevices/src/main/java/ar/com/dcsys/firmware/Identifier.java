package ar.com.dcsys.firmware;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.FpCancel;
import ar.com.dcsys.firmware.cmd.FpCancel.FpCancelResult;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.cmd.Identify.IdentifyResult;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.sound.Player;

public class Identifier implements Runnable {

	private final Logger logger;
	private final SerialDevice sd;
	private final Identify identify;
	private final FpCancel cancel;
	private final Player player;

	private final String soundOk = "/ok.wav";
	private final String soundError = "/error.wav";
	
	private volatile boolean exit = false;
	
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
						try {
						    player.play(soundError);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					
					@Override
					public void onFailure(int errorCode) {
						try {
						    player.play(soundError);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					
					@Override
					public void onCancel() {
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
			
	}
	
	public void terminate() {
		try {
			cancel.execute(sd, new FpCancelResult() {
				
				@Override
				public void onSuccess() {
					//ok
				};
			});
		} catch (CmdException e) {
			e.printStackTrace();
		}
	}
	
}
