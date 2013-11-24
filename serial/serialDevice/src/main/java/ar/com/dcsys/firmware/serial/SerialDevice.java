package ar.com.dcsys.firmware.serial;


public interface SerialDevice {

	public boolean open() throws SerialException;
	public void close() throws SerialException;
	public void writeBytes(byte[] data) throws SerialException;
	public byte[] readBytes(int count) throws SerialException;
	
}
