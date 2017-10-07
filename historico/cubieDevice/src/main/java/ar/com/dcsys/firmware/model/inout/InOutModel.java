package ar.com.dcsys.firmware.model.inout;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.firmware.database.InOut;
import ar.com.dcsys.firmware.database.InOut.Status;
import ar.com.dcsys.firmware.database.InOutDAO;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.sound.TTSPlayer;

@Singleton
public class InOutModel {

	private final Leds leds;
	private final InOutDAO iodao;
	private final TTSPlayer ttsPlayer;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH 'y' mm");
	
	@Inject
	public InOutModel(InOutDAO iodao, Leds leds, TTSPlayer ttsPlayer) {
		this.iodao = iodao;
		this.leds = leds;
		this.ttsPlayer = ttsPlayer;
	}
	
	private void sayTheTime(String status, Date date) {
		String time = InOutModel.sdf.format(date);
		ttsPlayer.say(status + ". " + time + ".");
	}
	
	public void onLog(AttLog log) {
		String personId = log.getPerson().getId();
		Date date = log.getDate();
		
		InOut io = iodao.findBy(personId);
		if (io == null) {
			io = new InOut(personId,Status.IN);
		} else {
			switch (io.getStatus()) {
				case IN:
					io.setStatus(Status.OUT);
					leds.onCommand(Leds.OUT);
					sayTheTime("salida", date);
					break;
					
				case OUT:
					io.setStatus(Status.IN);
					leds.onCommand(Leds.IN);
					sayTheTime("entrada", date);
					break;
			}
		}
		
		iodao.persist(io);

	}
	
}
