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
	
	public static final String BLOCKED = "blocked";
	public static final String READY = "ready";
	public static final String IDENTIFY = "identify";
	public static final String ENROLL = "enroll";
	public static final String OK = "ok";
	public static final String ERROR = "error";
	public static final String SUB_OK = "subok";
	public static final String SUB_ERROR = "suberror";
	public static final String FATAL_ERROR = "fatalError";
	public static final String TEST = "test";
	public static final String PHASE_OK = "phaseOk";
	public static final String PHASE_ERROR = "phaseError";
	public static final String ON = "on";
	public static final String OFF = "off";
	public static final String BLINK = "blink";
	
	

	private final Map<String,String> ledsM = new HashMap<String,String>();
	private final Led[] leds = new Led[11];
	
	private void initMappings() {
		ledsM.clear();
		ledsM.put("0", "8");
		ledsM.put("1", "9");
		ledsM.put("2", "10");
		ledsM.put("3", "11");
		ledsM.put("4", "12");
		ledsM.put("5", "13");
		ledsM.put("6", "14");
		ledsM.put("7", "15");
		ledsM.put("8", "16");
		ledsM.put("9", "17");
		ledsM.put("10", "18");
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
		
			if (cmd.equals(Leds.IDENTIFY)) {
				
				int i = ledsConfig.intValue(ledsConfig.getIdentifyLed());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].turnOn();
				}
				
			} else if (cmd.equals(Leds.ENROLL)) {
				
				int i = ledsConfig.intValue(ledsConfig.getEnrollLed());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].turnOn();
				}
	
			} else if (cmd.equals(Leds.BLOCKED)) {
				
				int i = ledsConfig.intValue(ledsConfig.getBlockedLed());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].turnOn();
				}			
				
				
			} else if (cmd.equals(Leds.READY)) {
				
				turnOffAll();
				
			} else if (cmd.equalsIgnoreCase(Leds.OK)) {
				
				int i = ledsConfig.intValue(ledsConfig.getOkLed());
				int delay = ledsConfig.intValue(ledsConfig.getOkDelay());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}			
				
				
				
			} else if (cmd.equalsIgnoreCase(Leds.ERROR)) {
	
				int i = ledsConfig.intValue(ledsConfig.getErrorLed());
				int delay = ledsConfig.intValue(ledsConfig.getErrorDelay());
				
				turnOffAll();
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}
				
				
			} else if (cmd.equals(Leds.TEST)) {
				
				for (int i = 0; i < leds.length; i++) {
					leds[i].blink(4, 50);
				}
				
			} else if (cmd.equalsIgnoreCase(Leds.SUB_OK)) {
				
				int i = ledsConfig.intValue(ledsConfig.getSubOkLed());
				int delay = ledsConfig.intValue(ledsConfig.getSubOkDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}			
			
				
			} else if (cmd.equalsIgnoreCase(Leds.SUB_ERROR)) {
				
				int i = ledsConfig.intValue(ledsConfig.getSubErrorLed());
				int delay = ledsConfig.intValue(ledsConfig.getSubErrorDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(1, delay);
				}			
				
			} else if (cmd.equals(Leds.FATAL_ERROR)) {
				
				turnOnAll();
		
			} else if (cmd.startsWith(Leds.PHASE_OK)) {
				
				String phase = cmd.substring(cmd.indexOf(";") + 1);
				int iPhase = Integer.parseInt(phase);
				
				int i = ledsConfig.intValue(ledsConfig.getPhaseOkLed());
				int delay = ledsConfig.intValue(ledsConfig.getPhaseOkDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(iPhase, delay);
				}			
				
				
			} else if (cmd.startsWith(Leds.PHASE_ERROR)) {
				
				String phase = cmd.substring(cmd.indexOf(";") + 1);			
				
				int iPhase = Integer.parseInt(phase);
				
				int i = ledsConfig.intValue(ledsConfig.getPhaseErrorLed());
				int delay = ledsConfig.intValue(ledsConfig.getPhaseErrorDelay());
				
				if (i >= 0 && i < leds.length) {
					leds[i].blink(iPhase, delay);
				}
				
				
			} else if (cmd.startsWith(Leds.ON)) {
				
				String led = cmd.substring(cmd.indexOf(";") + 1);			
				
				int iled = Integer.parseInt(led);
				if (iled >= 0 && iled < leds.length) {
					leds[iled].turnOn();
				}

			} else if (cmd.startsWith(Leds.OFF)) {
				
				String led = cmd.substring(cmd.indexOf(";") + 1);			
				
				int iled = Integer.parseInt(led);
				if (iled >= 0 && iled < leds.length) {
					leds[iled].turnOff();
				}
				
				
			} else if (cmd.startsWith(Leds.BLINK)) {
				
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
