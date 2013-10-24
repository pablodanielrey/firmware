package ar.com.dcsys.firmware.cmd;

import ar.com.dcsys.firmware.serial.SerialDevice;


public interface Cmd {
	public void execute(SerialDevice serialPort, CmdResult result) throws CmdException;
}
