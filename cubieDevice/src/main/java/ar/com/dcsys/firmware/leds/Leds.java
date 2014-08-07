package ar.com.dcsys.firmware.leds;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
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
	private final LedsConfig ledsConfig;
	
	public void addCommand(Runnable r) {
		commands.add(r);
	}
	
	@Inject
	public Leds(GPIO gpio, CubiePins cubiePins, LedsConfig ledsConfig) {
		this.gpio = gpio;
		this.cubiePins = cubiePins;
		this.ledsConfig = ledsConfig;
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
	
	
	private void turnOffAll() {
		for (Led l : leds) {
			l.turnOff();
		}
	}
	
	private void turnOnAll() {
		for (Led l : leds) {
			l.turnOn();
		}
	}	
	
	//// ---- comandos aceptados ----
	
	public void onCommand(String cmd) {

		try {
		
			if (cmd.equals("identify")) {
				
				int i = ledsConfig.intValue(ledsConfig.getIdentifyLed());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].turnOn();
				}
				
			} else if (cmd.equals("enroll")) {
				
				int i = ledsConfig.intValue(ledsConfig.getEnrollLed());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].turnOn();
				}
	
			} else if (cmd.equals("blocked")) {
				
				int i = ledsConfig.intValue(ledsConfig.getBlockedLed());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].turnOn();
				}			
				
			} else if (cmd.equalsIgnoreCase("ok")) {
				
				int i = ledsConfig.intValue(ledsConfig.getOkLed());
				int delay = ledsConfig.intValue(ledsConfig.getOkDelay());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}			
				
				
				
			} else if (cmd.equalsIgnoreCase("error")) {
	
				int i = ledsConfig.intValue(ledsConfig.getErrorLed());
				int delay = ledsConfig.intValue(ledsConfig.getErrorDelay());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}
				
				
			} else if (cmd.equals("test")) {
				
				for (int i = 0; i < leds.length; i++) {
					leds[i].blink(4, 50);
				}
				
			} else if (cmd.equalsIgnoreCase("subok")) {
				
				int i = ledsConfig.intValue(ledsConfig.getSubOkLed());
				int delay = ledsConfig.intValue(ledsConfig.getSubOkDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}			
			
				
			} else if (cmd.equalsIgnoreCase("suberror")) {
				
				int i = ledsConfig.intValue(ledsConfig.getSubErrorLed());
				int delay = ledsConfig.intValue(ledsConfig.getSubErrorDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}			
				
			} else if (cmd.equals("fatalError")) {
				
				turnOnAll();
		
			} else if (cmd.startsWith("phaseOk;")) {
				
				String nemonic = "phaseOk;";
				String phase = cmd.substring(cmd.indexOf(";") + 1);
				int iPhase = Integer.parseInt(phase);
				
				int i = ledsConfig.intValue(ledsConfig.getPhaseOkLed());
				int delay = ledsConfig.intValue(ledsConfig.getPhaseOkDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(iPhase, delay);
				}			
				
				
			} else if (cmd.startsWith("phaseError;")) {
				
				String nemonic = "phaseError;";
				String phase = cmd.substring(cmd.indexOf(";") + 1);			
				
				int iPhase = Integer.parseInt(phase);
				
				int i = ledsConfig.intValue(ledsConfig.getPhaseErrorLed());
				int delay = ledsConfig.intValue(ledsConfig.getPhaseErrorDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(iPhase, delay);
				}
				
				
			} else if (cmd.startsWith("on;")) {
				
				String led = cmd.substring(cmd.indexOf(";") + 1);			
				
				int iled = Integer.parseInt(led);
				if (iled >= 0 && iled < leds.length) {
					leds[iled].turnOn();
				}

			} else if (cmd.startsWith("off;")) {
				
				String led = cmd.substring(cmd.indexOf(";") + 1);			
				
				int iled = Integer.parseInt(led);
				if (iled >= 0 && iled < leds.length) {
					leds[iled].turnOff();
				}
				
				
			} else if (cmd.startsWith("blink;")) {
				
				// blink;led;times;delay
			
				String[] tokens = cmd.split(";");
				int led = Integer.parseInt(tokens[1]);
				int times = Integer.parseInt(tokens[2]);
				int delay = Integer.parseInt(tokens[3]);
				
				if (led >= 0 && led < leds.length) {
					leds[led].blink(times, delay);
				}
			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	
	
}
