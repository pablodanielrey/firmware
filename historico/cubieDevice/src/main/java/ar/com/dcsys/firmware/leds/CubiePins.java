package ar.com.dcsys.firmware.leds;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class CubiePins {

	private final Map<String,String> pinsU14 = new HashMap<String,String>();
	private final Map<String,String> pinsU15 = new HashMap<String,String>();
	
	/**
	 * Inicializo los nombres de los pines, solo los que uso.
	 */
	@PostConstruct
	private void init() {
		pinsU14.clear();
		
		pinsU14.put("47", "pi11");
		pinsU14.put("45", "pi10");
		pinsU14.put("44", "3.3v");
		pinsU14.put("42", "gnd");
		
		pinsU15.clear();
	
		pinsU15.put("8", "pg3");
		pinsU15.put("9", "pg2");
		pinsU15.put("10", "pg1");
		pinsU15.put("11", "pg4");
		pinsU15.put("12", "pg5");
		pinsU15.put("13", "pg6");
		pinsU15.put("14", "pg7");
		pinsU15.put("15", "pg8");
		pinsU15.put("16", "pg9");
		pinsU15.put("17", "pg10");
		pinsU15.put("18", "pg11");
		pinsU15.put("19", "gnd");
		pinsU15.put("20", "gnd");
		
	}
	
	
	public String getName(int side, String pin) {
		if (side == 0) {
			return pinsU14.get(pin);
		} else {
			return pinsU15.get(pin);
		}
	}
	
	public String getPin(int side, String name) {
		Map<String,String> m = null;
		if (side == 0) {
			m = pinsU14;
		} else {
			m = pinsU15;
		}
		
		for (String k : m.keySet()) {
			if (name.equalsIgnoreCase(m.get(k))) {
				return k;
			}
		}
		return null;
	}
	
}
