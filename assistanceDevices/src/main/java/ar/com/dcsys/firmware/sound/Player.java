package ar.com.dcsys.firmware.sound;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import ar.com.dcsys.firmware.App;

public class Player {

	public static void play(String file) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		
		BufferedInputStream sound = new BufferedInputStream(App.class.getResourceAsStream(file));
		try {
			AudioInputStream asound = AudioSystem.getAudioInputStream(sound);
			try {
				AudioFormat format = asound.getFormat();
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				Clip source = (Clip)AudioSystem.getLine(info);
				source.open(asound);
				try {
					source.start();
					while (source.isActive()) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
					}
				} finally {
					source.close();
				}
				
			} finally {
				asound.close();
			}
		} finally {
			sound.close();
		}
	}
	
}
