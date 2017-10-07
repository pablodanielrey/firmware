package ar.com.dcsys.firmware;

import java.util.concurrent.Semaphore;

public class MutualExclusion {

	public static int SERIAL_DEVICE = 0;
	public static int NEED_ATTLOGS_SYNC = 1;
	public static int EXECUTING_COMMAND = 2;
	
	public static Semaphore[] using = {new Semaphore(1), new Semaphore(1), new Semaphore(1)};
	
}
