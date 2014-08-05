package ar.com.dcsys.firmware.leds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Led {

	private final String path;
	
	public Led(String file) {
		this.path = file;
	}
	
	
	private void direction() {
		File f = new File(path + "/direction");
		try {
			PrintWriter out = new PrintWriter(f);
			try {
				out.println("out");
				
			} finally {
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	public void init() {
		direction();
		writeValue("0");
	}
	
	public void close() {
		
	}
	
	/**
	 * Escribe un valor en el archvio que representa el led.
	 * @param v
	 */
	public void writeValue(String v) {
		File f = new File(path + "/value");
		try {
			PrintWriter out = new PrintWriter(f);
			try {
				out.println(v);
				
			} finally {
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Escribe varios valores esperando distintos delayus entre cada uno de ellos en el archivo que representa el led.
	 * @param v
	 * @param delays
	 */
	public void writeValues(String[] v, int[] delays) {
		if (v.length != delays.length) {
			return;
		}
		
		File f = new File(path + "/value");
		try {
			PrintWriter out = new PrintWriter(f);
			try {
				int i = 0;
				while (i < v.length) {
					String value = v[i];
					int delay = delays[i];
					out.println(value);
					out.flush();
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
					}
					i++;
				}
				
			} finally {
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	
	
	public void turnOff() {
		writeValue("0");
	}

	public void turnOn() {
		writeValue("1");
	}

	public void blink(int times, int delay) {
		
		String[] values = new String[times * 2];
		int[] delays = new int[times * 2];
		
		int i = 0;
		while (i < values.length) {
			values[i] = "1";
			delays[i] = delay;
			i++;
			values[i] = "0";
			delays[i] = delay;
			i++;
		}
		
		writeValues(values,delays);
	}
	
	
	
}
