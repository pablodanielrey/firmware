package ar.com.dcsys.firmware;

import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.CmdResult;
import ar.com.dcsys.firmware.cmd.FpCancel;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialDeviceJssC;
import ar.com.dcsys.firmware.serial.SerialException;

public class Reset {

	public static void main(String[] args) {
		
    	try {
    		SerialDevice sd = new SerialDeviceJssC();
    		if (!sd.open()) {
    			return;
    		}
    		
    		
    		FpCancel fpCancel = new FpCancel();
    		fpCancel.execute(sd, new CmdResult() {
				
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
				}
			});
    		
    		
    		sd.close();
    		
    		
    	} catch (SerialException | CmdException e) {
    		System.out.println(e.getMessage());
    		e.printStackTrace();
    	}
		
	}
	
}
