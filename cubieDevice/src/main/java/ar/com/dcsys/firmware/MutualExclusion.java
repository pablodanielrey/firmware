package ar.com.dcsys.firmware;

import java.util.concurrent.Semaphore;

import ar.com.dcsys.firmware.cmd.TestConnection;
import ar.com.dcsys.firmware.cmd.TestConnection.TestConnectionResult;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class MutualExclusion {
	
	public static int DEFAULT_GENERATOR = 0;
	public static int SERIAL_DEVICE = 1;
	
	public static Semaphore[] using = {new Semaphore(1), new Semaphore(1)};
	
	
	
	
	private static final TestConnection test = new TestConnection();
	
	public static void recoverFromError(SerialDevice sd) {
		int tries = 10;
		while (tries > 0) {
			tries = tries + 1;
			try {
				sd.clearBuffer();
				break;
				
			} catch (Exception e) {
			}
		}
		
		// ejecuto un test para chequear que funcione el tema del lector.
		try {
			test.execute(sd, new TestConnectionResult() {
				@Override
				public void onSuccess() {
				}
				
				@Override
				public void onFailure() {
					throw new RuntimeException();
				}
			});
		} catch (Exception e) {
			
			// aca hace falta resetear el lector y probar nuevamente.
			e.printStackTrace();
			
		}
	}
	
}
