package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;

public class Reader {

	private final Firmware firmware;
	private final FpCancel fpCancel;
	private final List<Cmd> commands = new ArrayList<Cmd>();

	
	@Inject
	public Reader(Firmware firmware, FpCancel fpCancel, GetEmptyId getEmptyId, Enroll enroll, GetVersion getVersion, ReadTemplate readTemplate, SetLed setLed, Test test) {
		
		this.firmware = firmware;
		this.fpCancel = fpCancel;
		
		commands.add(getEmptyId);
		commands.add(enroll);
		commands.add(getVersion);
		commands.add(readTemplate);
		commands.add(setLed);
		commands.add(test);
		commands.add(fpCancel);
		
	}
	
	public void onCommand(final String command, final Response response) {
		
		if (command.equalsIgnoreCase("help")) {
			StringBuilder sb = new StringBuilder();
			for (Cmd c : commands) {
				sb.append(c.getCommand()).append("\n");
			}
			try {
				response.sendText(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		

		if (command.equalsIgnoreCase(fpCancel.getCommand())) {
			
			fpCancel.execute("", response);
			
			/*
			Runnable r = new Runnable() {
				@Override
				public void run() {
					fpCancel.execute("", response);
				}
			};
			firmware.addCommand(r);
			MutualExclusion.using[MutualExclusion.CMD].release();
			*/
			
		} else {
		
			for (final Cmd c : commands) {
				if (c.identify(command)) {
					
					
					Runnable r2 = new Runnable() {
						@Override
						public void run() {
							c.execute(command, response);
							//MutualExclusion.using[MutualExclusion.CMD].release();
						}
					};
					firmware.addCommand(r2);
					
					fpCancel.execute("", response);
					
					/*
					Runnable r = new Runnable() {
						@Override
						public void run() {
							fpCancel.execute("", response);
						}
					};
					Runnable r2 = new Runnable() {
						@Override
						public void run() {
							c.execute(command, response);
							//MutualExclusion.using[MutualExclusion.CMD].release();
						}
					};
					firmware.addCommand(r,r2);
					MutualExclusion.using[MutualExclusion.CMD].release();
					 
					 */
					break;
				}
			}
		}
		
	}
	
}
