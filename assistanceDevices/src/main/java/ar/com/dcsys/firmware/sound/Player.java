package ar.com.dcsys.firmware.sound;

/**
 * Implementa un player básico para reproducir sonidos.
 * es muy básico y no se tiene en cuenta NADA de eficiencia.
 * solo reproduce un sonido a la vez, no hace mux ni nada parecido.
 */


import java.io.BufferedInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import ar.com.dcsys.firmware.App;

@Singleton
public class Player {
	
	private Clip clip;
	
	@PostConstruct
	void init() {
		try {
			clip = AudioSystem.getClip();
		} catch (Exception e) {
			
		}
	}
	
	@PreDestroy
	void destroy() {
		if (clip != null && clip.isOpen()) {
			clip.close();
		}
	}
	
	
	public synchronized void play(String file) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		
		if (clip == null) {
			throw new IOException("clip == null");
		}
		
		BufferedInputStream sound = new BufferedInputStream(App.class.getResourceAsStream(file));
		try {
			AudioInputStream asound = AudioSystem.getAudioInputStream(sound);
			try {
				/*
				 * No funco como se esperaba.
				AudioFormat format = asound.getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				Clip clip = (Clip)AudioSystem.getLine(info);
				 */
				if (clip != null && clip.isOpen()) {
					clip.close();
				}
				clip.open(asound);
				try {
					clip.start();
				} catch (Exception e) {
					clip.close();
					throw e;
				}
			} catch (Exception e) {
				asound.close();
				throw e;
			}
		} catch (Exception e) {
			sound.close();
			throw e;
		}

	}
	
}
