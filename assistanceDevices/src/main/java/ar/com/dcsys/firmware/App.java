package ar.com.dcsys.firmware;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;



/**
 * Aplicación principal del proyecto para el firmware del reloj.
 *
 */

public class App {

    public static void main( String[] args ) {
 
    	
    	try {
    		BufferedInputStream bin = new BufferedInputStream(App.class.getResourceAsStream("/Noise.wav"));
			
			AudioInputStream ain = AudioSystem.getAudioInputStream(bin);
			AudioFormat format = ain.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			
			Clip source = (Clip)AudioSystem.getLine(info);
			source.open(ain);
			source.start();
			
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
    	
    	
    	
    	Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
    	try {
	    	Firmware firmware = container.instance().select(Firmware.class).get();
	    	firmware.run();
    		
    	} finally {
    		weld.shutdown();
    	}
    	
    }
}
