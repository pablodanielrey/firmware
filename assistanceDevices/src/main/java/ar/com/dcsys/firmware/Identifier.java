package ar.com.dcsys.firmware;

import javax.inject.Inject;
import javax.inject.Named;

import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.cmd.Cmd;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.CmdResult;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class Identifier implements Runnable {

	private final SerialDevice sd;
	private final Cmd identify;
	private final Cmd cancel;
	private volatile boolean exit = false;
	
	@Inject
	public Identifier(SerialDevice sd, @Named("identify") Cmd identify, @Named("fpCancel") Cmd cancel) {
		this.sd = sd;
		this.identify = identify;
		this.cancel = cancel;
	}
	
	
	@Override
	public void run() {
		exit = false;
		while (!exit) {

			try {
				identify.execute(sd, new CmdResult() {
					
					@Override
					public void onSuccess(byte[] data) {
						System.out.println("Datos : " + Utils.getHex(data));
					}
					
					@Override
					public void onSuccess(int i) {
						System.out.println("Huella identificada : " + String.valueOf(i));
					}
					
					@Override
					public void onSuccess() {
						System.out.println("No se pudo identificar la huella");
					}
					
					@Override
					public void onFailure() {
						System.out.println("Error ejecutando el comando");
					}
					
					@Override
					public void onFailure(int code) {
						System.out.println("Error " + String.valueOf(code)) ;
						
						switch (code) {
							case CamabioUtils.ERR_FP_CANCEL:
								System.out.println("Comando cancelado");
								exit = true;
								break;
							case CamabioUtils.ERR_BAD_CUALITY:
								System.out.println("Mala calidad de la imagen!!");
								break;
							case CamabioUtils.ERR_ALL_TMPL_EMPTY:
								System.out.println("No existe ninguna huella enrolada");
								break;
							case CamabioUtils.ERR_TIME_OUT:
								System.out.println("Timeout");
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
					System.out.println("Cancel ok");
				}
				
				@Override
				public void onSuccess(int i) {
					System.out.println("Cancel ok");
				}
				
				@Override
				public void onSuccess() {
					System.out.println("Cancel ok");
				}
				
				@Override
				public void onFailure(int code) {
					System.out.println("Cancel Failure");
				}
				
				@Override
				public void onFailure() {
					System.out.println("Cancel Failure");
				}
			});
		} catch (CmdException e) {
			e.printStackTrace();
		}
	}
	
}
