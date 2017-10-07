package ar.com.dcsys.firmware;

public class Utils {

	public static String getHex(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
	    for (byte b : bytes) {
	        sb.append(String.format("%02X ", b));
	    }
	    return sb.toString();
	}
	
}
