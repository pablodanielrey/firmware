package ar.com.dcsys.firmware.leds;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Leds {

	private final Map<String,String> ledsM = new HashMap<String,String>();
	private final Led[] leds = new Led[3];
	
	private void initMappings() {
		ledsM.clear();
		ledsM.put("0", "8");
		ledsM.put("1", "10");
		ledsM.put("2", "12");
		ledsM.put("3", "14");
	}
	
	private void createLeds() {
		int i = 0;
		while (i < leds.length) {
			String base = "/sys/class/gpio/";
			
			String l = String.valueOf(i);
			String pinCubie = ledsM.get(l);
			String pinName = cubiePins.getName(1, pinCubie);
			String gpioPin = gpio.getPin(pinName);
			
			String file = base + "gpio" + gpioPin + "_" + pinName;
			
			leds[i] = new Led(file);
			leds[i].init();
			
			i++;
		}
	}
	
	private final LinkedBlockingQueue<Runnable> commands = new LinkedBlockingQueue<Runnable>();
	private volatile boolean end = false;
	private final Executor executor = Executors.newCachedThreadPool();
	private final GPIO gpio;
	private final CubiePins cubiePins;
	
	public void addCommand(Runnable r) {
		commands.add(r);
	}
	
	@Inject
	public Leds(GPIO gpio, CubiePins cubiePins) {
		this.gpio = gpio;
		this.cubiePins = cubiePins;
	}
	
	
	@PostConstruct
	public void start() {
		
		try {
			gpio.initialize();
			initMappings();
			createLeds();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Runnable r = new Runnable() {
			@Override
			public void run() {				
				while (!end) {
					try {
						Runnable r = commands.take();
						executor.execute(r);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		};
		(new Thread(r)).start();

		
	}
	
	
	
	private final Runnable bogus = new Runnable() {
		@Override
		public void run() {
		}
	};
	
	
	@PreDestroy
	public void stop() {
		end = true;
		addCommand(bogus);
	}
	
	
	//// ---- comandos aceptados ----
	
	
	public void onCommand(String cmd) {
		
		if (cmd.equals("identify")) {
			
			leds[2].turnOff();
			
		} else if (cmd.equals("enroll")) {
			
			leds[2].turnOn();
			
		} else if (cmd.equalsIgnoreCase("ok")) {
			
			leds[0].blink(1, 300);
			
		} else if (cmd.equalsIgnoreCase("error")) {

			leds[1].blink(1, 300);
			
		} else if (cmd.equals("testleds")) {
			
			for (int i = 0; i < leds.length; i++) {
				leds[i].blink(4, 100);
			}
			
		}
		
	}
	

	
	
}
