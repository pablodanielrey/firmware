package ar.com.dcsys.firmware.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.model.Cmd;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.serial.SerialDevice;

public class Reader {

	private static final Logger logger = Logger.getLogger(Reader.class.getName());
	
	private final SerialDevice sd;
	private final Firmware firmware;
	private final FpCancel fpCancel;
	private final Map<String,Cmd> commands = new HashMap<String,Cmd>();

	
	@Inject
	public Reader(Firmware firmware, FpCancel fpCancel, 
									 GetEmptyId getEmptyId, 
									 Enroll enroll, 
									 GetVersion getVersion, 
									 ReadTemplate readTemplate, 
									 SetLed setLed, 
									 Test test,
									 PurgeTemplates purgeTemplates, 
									 SetBaudrate setBaudrate, 
									 SerialDevice sd) {
		
		this.firmware = firmware;
		this.fpCancel = fpCancel;
		this.sd = sd;

		commands.put(getEmptyId.getCommand(),getEmptyId);
		commands.put(enroll.getCommand(),enroll);
		commands.put(getVersion.getCommand(),getVersion);
		commands.put(readTemplate.getCommand(),readTemplate);
		commands.put(setLed.getCommand(),setLed);
		commands.put(test.getCommand(),test);
		commands.put(fpCancel.getCommand(),fpCancel);
		commands.put(purgeTemplates.getCommand(),purgeTemplates);
		commands.put(setBaudrate.getCommand(), setBaudrate);
		
	}
	
	
	public void cancel(Response response) {
		fpCancel.setResponse(response);
		fpCancel.execute();
	}
	
	public void onCommand(final String command, final Response response) {
		
		if (command.equalsIgnoreCase("help")) {
			StringBuilder sb = new StringBuilder();
			for (String c : commands.keySet()) {
				sb.append(c).append("\n");
			}
			try {
				response.sendText(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		

		if (command.equalsIgnoreCase(fpCancel.getCommand())) {
			
			cancel(response);
			
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

			for (final Cmd c : commands.values()) {
				if (c.identify(command)) {
					
					c.setResponse(response);
					firmware.addCommand(c);
					
					Cmd cmd = firmware.getRunningCommand();
					if (cmd != null) {
						try {
							cmd.cancel();
						} catch (CmdException e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
					}
					
					break;
				}
			}
		}
		
	}
	
}
