package ar.com.dcsys.firmware;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;

public class KeyboardReader implements Runnable {

	
	@Override
	public void run() {
		
//		InputStream in = System.in;
		
		Console con = System.console();
		if (con == null) {
			return;
		}

		BufferedReader reader = new BufferedReader(con.reader());
		boolean exit = false;
		while (!exit) {
	
			try {

				String line = reader.readLine();
				System.out.println("\n-----------------------\n" + line + "\n------------------\n");

				
				if (line.equals("salir")) {
					exit = true;
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
