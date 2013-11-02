package ar.com.dcsys.firmware;

import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.CmdResult;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.cmd.TestConnection;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialDeviceJssC;
import ar.com.dcsys.firmware.serial.SerialException;

/**
 * Aplicación principal del proyecto para el firmware del reloj.
 *
 */
public class App 
{

    public static void main( String[] args )
    {
    	
    	System.out.println("Inicializando Sistema Control de Asistencia");
    	
    	try {
    		SerialDevice sd = new SerialDeviceJssC();
    		if (!sd.open()) {
    			return;
    		}
	    	
	    	Identify identify = new Identify();
	    	TestConnection testConnection = new TestConnection();
	    	
	    	boolean end = false;
	    	while (!end) {
	    		
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
	    	
	    	sd.close();
	    	
	    	
    	} catch (SerialException e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}
    	
    }
}
