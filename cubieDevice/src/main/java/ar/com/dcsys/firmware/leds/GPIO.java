package ar.com.dcsys.firmware.leds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

@Singleton
public class GPIO {

	private final Map<String,String> gpioPins = new HashMap<String,String>();
	
	
	/**
	 * Inicializa la tabla de mappings entre los nombres de los pines y los pines en el gpio asignados en el fex.
	 */
	@PostConstruct
	private void init() {
		gpioPins.clear();
		
		gpioPins.put("pg3","1");
		gpioPins.put("pg1","7");
		gpioPins.put("pg5","5");
		gpioPins.put("pg7","19");
		gpioPins.put("pg9","17");
		gpioPins.put("pg11","15");
		
		initialize();
	}
	
	public String getPin(String n) {
		return gpioPins.get(n);
	}
	
	
	public void initialize() {
		String gpioExportPath = "/sys/class/gpio/export";
		try {
			File fe = new File(gpioExportPath);
			PrintWriter export = new PrintWriter(fe);
			try {
				for (String pin : gpioPins.values()) {
					export.println(pin);
					export.flush();
				}
			} finally {
				export.close();
			}

			// le doy un tiempo para que se estabilice
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	


	@PreDestroy
	private void destroy() {
		String gpioExportPath = "/sys/class/gpio/unexport";
		try {
			File fe = new File(gpioExportPath);
			PrintWriter export = new PrintWriter(fe);
			try {
				for (String pin : gpioPins.values()) {
					export.println(pin);
				}
			} finally {
				export.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
