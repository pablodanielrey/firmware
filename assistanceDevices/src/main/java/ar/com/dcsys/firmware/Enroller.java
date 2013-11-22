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
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.security.Finger;
import ar.com.dcsys.security.FingerprintCredentials;

public class Enroller implements Runnable, Cmd {

	private final Logger logger;
	private final SerialDevice sd;
	private final EnrollAndStoreInRam enroll;
	private final FpCancel cancel;
	private final Semaphore terminate = new Semaphore(0);
	
	@Inject
	public Enroller(Logger logger, SerialDevice sd, EnrollAndStoreInRam enroll, FpCancel cancel) {
		this.logger = logger;
		this.sd = sd;
		this.enroll = enroll;
		this.cancel = cancel;
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
					logger.info("Expir� el tiempo de espera para tomar una huella");
				}
				
				@Override
				public void onSuccess(FingerprintCredentials fp) {
					StringBuilder sb = new StringBuilder();
					sb.append("Huella exitósamente obtenida\n");
					sb.append("Algoritmo ").append(fp.getAlgorithm()).append("\n");
					sb.append("Codificación ").append(fp.getCodification()).append("\n");
					sb.append("Template : ").append(Utils.getHex(fp.getTemplate())).append("\n");
					sb.append("Tama�o del template : ").append(fp.getTemplate().length).append("\n");
					
					logger.info(sb.toString());
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
