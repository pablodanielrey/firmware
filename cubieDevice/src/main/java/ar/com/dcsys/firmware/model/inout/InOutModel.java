package ar.com.dcsys.firmware.model.inout;

import javax.inject.Inject;
import javax.inject.Singleton;

import ar.com.dcsys.firmware.database.InOut;
import ar.com.dcsys.firmware.database.InOut.Status;
import ar.com.dcsys.firmware.database.InOutDAO;
import ar.com.dcsys.firmware.leds.Leds;

@Singleton
public class InOutModel {

	private final Leds leds;
	private final InOutDAO iodao;
	
	@Inject
	public InOutModel(InOutDAO iodao, Leds leds) {
		this.iodao = iodao;
		this.leds = leds;
	}
	
	public void onLog(String personId) {
		
		InOut io = iodao.findBy(personId);
		if (io == null) {
			io = new InOut(personId,Status.IN);
		} else {
			switch (io.getStatus()) {
				case IN:
					io.setStatus(Status.OUT);
					leds.onCommand(Leds.OUT);
					break;
					
				case OUT:
					io.setStatus(Status.IN);
					leds.onCommand(Leds.IN);
					break;
			}
		}
		
		iodao.persist(io);
	}
	
}
