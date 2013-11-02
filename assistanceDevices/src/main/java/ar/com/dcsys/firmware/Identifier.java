package ar.com.dcsys.firmware;

import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.CmdResult;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class Identifier implements Runnable {

	private final SerialDevice sd;
	
	public Identifier(SerialDevice sd) {
		this.sd = sd;
	}
	
	
	@Override
	public void run() {
		
		Identify identify = new Identify();
		
		while (true) {
		
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
						System.out.println("Error");
						switch (code) {
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
	
}
