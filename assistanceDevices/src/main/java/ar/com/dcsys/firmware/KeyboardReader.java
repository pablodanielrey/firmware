package ar.com.dcsys.firmware;

import java.io.Console;
import java.io.IOException;
import java.io.Reader;

public class KeyboardReader implements Runnable {

	
	@Override
	public void run() {
		
//		InputStream in = System.in;
		
		Console con = System.console();
		if (con == null) {
			return;
		}

		Reader reader = con.reader();
		
		while (true) {
	
			try {
				
				int c = reader.read();
				if (c == -1) {
					return;
				}
				
				String chars = new String(Character.toChars(c));
				
				System.out.println("\n-----------------------\n" + chars + "\n------------------\n");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
			
			
		}
		
	}
	
}
