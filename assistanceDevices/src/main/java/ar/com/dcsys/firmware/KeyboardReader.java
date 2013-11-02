package ar.com.dcsys.firmware;

import java.io.IOException;
import java.io.InputStream;

public class KeyboardReader implements Runnable {

	
	@Override
	public void run() {
		
		InputStream in = System.in;
		
		while (true) {
			
			try {
				
				while (in.available() <= 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				StringBuffer st = new StringBuffer();

				while (in.available() > 0) {
					byte[] buffer = new byte[in.available()];
					in.read(buffer);
					st.append(buffer);					 
				}
				
				System.out.println("\n-----------------------\n" + st.toString() + "\n------------------\n");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
