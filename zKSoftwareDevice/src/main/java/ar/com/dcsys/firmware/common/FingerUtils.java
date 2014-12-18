package ar.com.dcsys.firmware.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import ar.com.dcsys.firmware.soap.Base64;
import ar.com.dcsys.firmware.soap.SoapDevice;
import ar.com.dcsys.firmware.soap.UserTemplate;
import ar.com.dcsys.security.Finger;
import ar.com.dcsys.security.Fingerprint;

public class FingerUtils {
	
	private static final Charset charset = StandardCharsets.UTF_8;

	/**
	 * Obtiene el algoritmo del reloj en su forma completa
	 * @param data
	 * @return
	 */
	public static String getAlgorithm(SoapDevice data) {
		return "ZK" + data.getAlgorithm();
	}
	
	/**
	 * Convierte ut a FingerPrint
	 * @param initialize
	 * @param ut
	 * @param personId
	 * @return
	 */
	public static Fingerprint toFingerprint(SoapDevice soapDevice, UserTemplate ut, String personId) {
		//creo finger
		String fingerId = ut.getFingerId();
		int iid = Integer.parseInt(fingerId);
		Finger finger = Finger.getFinger(iid);
						
		//convierto template
		String templateStr = ut.getTemplate();
		byte[] template = templateStr.getBytes(charset);
		
		//obtengo el algoritmo
		String algorithm = getAlgorithm(soapDevice);

		//obtengo la codificacion
		String codification = soapDevice.getCodification();
		
		//creo fingerprint
		Fingerprint fp = new Fingerprint(finger, algorithm, codification, template);
		
		//seteo la persona
		fp.setPersonId(personId);
		
		return fp;
		
	}
	
	/**
	 * El tamaño de la huella enviado al dispositivo y recibido del dispositivo es el tamaño de los datos decodificados del base64
	 * @param template
	 * @return
	 * @throws IOException
	 */
	private static int calculateSize(String template) throws IOException {
		try {
			byte[] data = Base64.decode(template);
			return data.length;
		} catch (IOException e) {
			throw new IOException("error calculando el tamaño de la huella decodificada (" + e.getMessage() + ")",e);
		}
	}	
	
	/**
	 * Convierte fp a UserTemplate
	 * @param initialize
	 * @param fp
	 * @param pin
	 * @return
	 * @throws IOException
	 */
	public static UserTemplate toUserTemplate(Fingerprint fp, String pin) throws IOException {
		//creo UserTemplate
		UserTemplate ut = new UserTemplate();
		
		//obtengo el fingerId
		String fingerId = String.valueOf(fp.getFinger().getId());
		ut.setFingerId(fingerId);
		
		//convierto el template
		byte[] template = fp.getTemplate();
		String templateStr = new String(template,charset);
		ut.setTemplate(templateStr);
		
		ut.setPin(pin);
		ut.setValid("1");
		
		
		String size = String.valueOf(calculateSize(templateStr));
		ut.setSize(size);
		
		return ut;
		
	}
	
	/**
	 * Compara fp con ut
	 * @param initialize
	 * @param fp
	 * @param ut
	 * @param personId
	 * @return
	 */
	public static boolean equals(SoapDevice soapDevice, Fingerprint fp, UserTemplate ut, String personId) {
		
		//comparo la persona
		if (!personId.equals(fp.getPersonId())) {
			return false;
		}
		
		//comparo el dedo
		String id = ut.getFingerId();
		int iid = Integer.parseInt(id);
		
		Finger finger = Finger.getFinger(iid);
		
		if (!fp.getFinger().equals(finger)) {
			return false;
		}
		
		//comparo template
		String templateStr = ut.getTemplate();
		Charset charset = StandardCharsets.UTF_8;
		byte[] template = templateStr.getBytes(charset);
		if (!template.equals(fp.getTemplate())) {
			return false;
		}
				
		//comparo el algoritmo
		String algorithm = getAlgorithm(soapDevice);
		if (!algorithm.equals(fp.getAlgorithm())) {
			return false;
		}
		
		//comparo codificacion
		String codification = soapDevice.getCodification();
		if (!codification.equals(fp.getCodification())) {
			return false;
		}
				
		return true;
	}
	
	public static Fingerprint getFingerprint(SoapDevice soapDevice,List<Fingerprint> fps, UserTemplate t, String personId) {
		for (Fingerprint fp : fps) {
			if (equals(soapDevice,fp,t,personId)) {
				return fp;
			}
		}
		
		return null;
	}
	
	public static UserTemplate getUserTemplate(SoapDevice soapDevice, List<UserTemplate> uts, Fingerprint fp, String personId) {
		for (UserTemplate ut : uts) {
			if (equals(soapDevice, fp, ut, personId)) {
				return ut;
			}
		}
		
		return null;
	}
	
	/**
	 * Verifico que en la lista de UserTemplate (uts) exista fp. Comparo por el fingerId y el pin
	 * @param uts
	 * @param fp
	 * @param pin
	 * @return
	 */
	public static UserTemplate findUserTemplate(List<UserTemplate> uts, Fingerprint fp, String pin) {
		for (UserTemplate ut : uts) {
			String fingerId = String.valueOf(fp.getFinger().getId());
			if (ut.getPin().equals(pin) && ut.getFingerId().equals(fingerId)) {
				return ut;
			}
		}
		
		return null;
	}
	
	
	public static Fingerprint deepCopy(Fingerprint fp) throws Exception {
        //Serialization of object
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(fp);
 
        //De-serialization of object
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        Fingerprint copied = (Fingerprint) in.readObject();
  
        return copied;
    }
	
}
