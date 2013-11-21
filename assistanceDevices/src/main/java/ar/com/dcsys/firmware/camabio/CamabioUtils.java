package ar.com.dcsys.firmware.camabio;

import java.util.Arrays;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.cmd.ProcessingException;

public class CamabioUtils {
	
	
	public static final String ALGORITHM = "Camabio";
	public static final String CODIFICATION = "none";
	

	public static final int CMD = 0xAA55;
	public static final int CMD_DATA = 0xA55A;
	public static final int RSP = 0x55AA;
	public static final int RSP_DATA = 0x5AA5;
	
	public static final int ERR_SUCCESS = 0x00;
	public static final int ERR_FAIL = 0x01;
	public static final int ERR_VERIFY = 0x11;
	public static final int ERR_IDENTIFY = 0x12;
	public static final int ERR_TMPL_EMPTY = 0x13;
	public static final int ERR_TMPL_NOT_EMPTY = 0x14;
	public static final int ERR_ALL_TMPL_EMPTY = 0x15;
	public static final int ERR_EMPTY_ID_NOEXIST = 0x16;
	public static final int ERR_BROKEN_ID_NOEXIST = 0x17;
	public static final int ERR_INVALID_TMPL_DATA = 0x18;
	public static final int ERR_DUPLICATION_ID = 0x19;
	public static final int ERR_BAD_CUALITY = 0x21;
	public static final int ERR_TIME_OUT = 0x23;
	public static final int ERR_NOT_AUTHORIZED = 0x24;
	public static final int ERR_GENERALIZE = 0x30;
	public static final int ERR_FP_CANCEL = 0x41;
	public static final int ERR_INTERNAL = 0x50;
	public static final int ERR_MEMORY = 0x51;
	public static final int ERR_EXCEPTION = 0x52;
	public static final int ERR_INVALID_TMPL_NO = 0x60;
	public static final int ERR_INVALID_SEC_VAL = 0x61;
	public static final int ERR_INVALID_TIME_OUT = 0x62;
	public static final int ERR_INVALID_BAUDRATE = 0x63;
	public static final int ERR_DEVICE_ID_EMPTY = 0x64;
	public static final int ERR_INVALID_DUP_VAL = 0x65;
	public static final int ERR_INVALID_PARAM = 0x70;
	public static final int ERR_NO_RELEASE = 0x71;
	
	public static final int GD_DOWNLOAD_SUCCESS = 0xA1;
	public static final int GD_NEED_FIRST_SWEEP = 0xFFF1;
	public static final int GD_NEED_SECOND_SWEEP = 0xFFF2;
	public static final int GD_NEED_THIRD_SWEEP = 0xFFF3;
	public static final int GD_NEED_RELEASE_FINGER = 0xFFF4;
	public static final int GD_DETECT_FINGER = 0x01;
	public static final int GD_NO_DETECT_FINGER = 0x00;
	public static final int GD_TEMPLATE_NOT_EMPTY = 0x01;
	public static final int GD_TEMPLATE_EMPTY = 0X00;	
	
	
	private static void zeroBuffer(byte[] b) {
		Arrays.fill(b,(byte)0);
//		for (int i = 0; i < b.length; i++) {
//			b[i] = 0;
//		}
	}
	
	private static void writeCommand(byte[] cmd, int command) {
		cmd[0] = 0x55;
		cmd[1] = (byte)0xaa;
		
		cmd[2] = (byte)(command & 0xff);
		cmd[3] = (byte)((command & 0xff00) >> 8);
	}
	
	private static void writeLength(byte[] cmd, int length) {
		cmd[4] = (byte)(length & 0xff);
		cmd[5] = (byte)((length & 0xff00) >> 8);
	}
	

	/**
	 * Calcula el checksum del paquete de datos y lo retorna en su orden para insertar en el paquete.
	 * @param cmd
	 * @return
	 */
	private static byte[] chksum(byte[] cmd) {
		byte[] csm = new byte[2];
		
		long sum = 0;
		int length = cmd.length;
		for (int i = 0; i < length; i++) {
			sum = sum + (cmd[i] & 0xff);
		}

		csm[0] = (byte)(sum & 0xff);
		csm[1] = (byte)((sum & 0xff00) >> 8);

		return csm;
	}
	
	/**
	 * Calcula y setea el checksum del paquete de datos.
	 * @param cmd
	 */
	private static void calcChksum(byte[] cmd) {
		cmd[cmd.length - 2] = 0;
		cmd[cmd.length - 1] = 0;
		
		byte[] csm = chksum(cmd);
		
		cmd[cmd.length - 2] = csm[0];
		cmd[cmd.length - 1] = csm[1];
	}

	/**
	 * Template para los comandos que no llevan ningun parámetro.
	 * @param command
	 * @return
	 */
	public static byte[] cmd(int command) {
		
		byte[] cmd = new byte[24];
		zeroBuffer(cmd);
		
		writeCommand(cmd, command);
//		writeLength(cmd, 0x00);
		
		calcChksum(cmd);
		
 		return cmd;
	}
	
	
	/**
	 * Template del comando para los que llevan como parametro el id del template
	 * @param command
	 * @param template
	 * @return
	 */
	public static byte[] cmdWithTemplateId(int command, int template) {
		
		byte[] cmd = new byte[24];
		zeroBuffer(cmd);
		
		writeCommand(cmd, command);
		writeLength(cmd, 0x02);
		
		cmd[6] = (byte)(template & 0xff);
		cmd[7] = (byte)((template & 0xff00) >> 8);

		calcChksum(cmd);
		
 		return cmd;
	}	
	
	/**
	 * Template para los comandos que llevan 1 parametro de 2 bytes.
	 * @param command
	 * @param param
	 * @return
	 */
	public static byte[] cmdWith2ByteParam(int command, int param) {
		return cmdWithTemplateId(command, param);
	}
	
	
	public static byte[] verify(int template) {
		return cmdWithTemplateId(0x0101, template);
	}
	
	
	public static byte[] identify() {
		return cmd(0x0102);
	}
	
	public static byte[] enroll(int template) {
		return cmdWithTemplateId(0x0103, template);
	}
	
	
	public static byte[] enrollOneTime(int template) {
		return cmdWithTemplateId(0x0104, template);
	}
	
	
	public static byte[] clearTemplate(int template) {
		return cmdWithTemplateId(0x0105, template);
	}
	
	
	public static byte[] clearAllTemplate() {
		return cmd(0x0106);
	}	
	
	
	public static byte[] getEmptyId() {
		return cmd(0x0107);
	}	
	
	/**
	 * Chequea si es vacío el numero de template o si ya tiene algun template.
	 * @param template
	 * @return
	 */
	public static byte[] getTemplateStatus(int template) {
		return cmdWithTemplateId(0x0108, template);
	}
	
	/**
	 * Chequea si la flash del modulo puede estar corrupa y cual es la primera corrupta.
	 * @return
	 */
	public static byte[] getBrokenTemplate() {
		return cmd(0x0109);
	}	

	public static byte[] readTemplate(int template) {
		return cmdWithTemplateId(0x010a, template);
	}	
	
	public static byte[] setSecurityLevel(int level) {
		return cmdWith2ByteParam(0x010c, level);
	}
	
	public static byte[] getSecurityLevel() {
		return cmd(0x010d);
	}	
	
	/**
	 * Setea el período de espera para que el lector lea una huella.
	 * valores posibles : 0s - 60s
	 * default : 5s
	 * @param time
	 * @return
	 */
	public static byte[] setFingerTimeOut(int time) {
		return cmdWith2ByteParam(0x010e, time);
	}
	
	public static byte[] getFingerTimeOut() {
		return cmd(0x010f);
	}		

	/**
	 * Setea el id del dispositivo.
	 * valores posibles : 1 - 254
	 * default : 1
	 * @param id
	 * @return
	 */
	public static byte[] setDeviceId(int id) {
		return cmdWith2ByteParam(0x0110, id);
	}
	
	public static byte[] getDeviceId() {
		return cmd(0x0111);
	}		

	public static byte[] getFirmwareVersion() {
		return cmd(0x0112);
	}		
	
	public static byte[] detectFinger() {
		return cmd(0x0113);
	}		

	/**
	 * Setea los baudios del modulo con la comunicación serie.
	 * 1 - 9600
	 * 2 - 19200
	 * 3 - 38400
	 * 4 - 57600
	 * 5 - 115200 (por defecto)
	 * 
	 * despues de setear el valor debe resetearse el dispositivo para que se haga efectivo el cambio
	 * @param id
	 * @return
	 */
	public static byte[] setBaudRateIndex(int id) {
		return cmdWith2ByteParam(0x0114, id);
	}
	
	public static byte[] setDupplicationCheck(boolean v) {
		if (v) {
			return cmdWith2ByteParam(0x0115, 1);
		} else {
			return cmdWith2ByteParam(0x0115, 0);
		}
	}
	
	public static byte[] getDupplicationCheck() {
		return cmd(0x0116);
	}		
	
	public static byte[] enterStandByMode() {
		return cmd(0x0117);
	}		
	
	public static byte[] enrollAndStoreInRam() {
		return cmd(0x0118);
	}		
	
	/**
	 * Obtiene los datos de enrolamiento que estan almacenados en la ram. 
	 * previamente almacenados usando enrollAndStoreInRam.
	 * @return
	 */
	public static byte[] getEnrollData() {
		return cmd(0x0119);
	}		

	/**
	 * parecido a hacer un EnrollOneTime + getEnrollData
	 * se enrola al usuario pidiendo solo una huella y se retorna ese template al host.
	 * no se almacena nada en el dispositivo. todo se hace en ram.
	 * @return
	 */
	public static byte[] getFeatureDataOfCapturedFP() {
		return cmd(0x011a);
	}		
	
	public static byte[] getDeviceName() {
		return cmd(0x0121);
	}		
	
	/**
	 * Setea el led del sensor a on o off.
	 * true = on
	 * false = off
	 * @param v
	 * @return
	 */
	public static byte[] sensorLedControl(boolean v) {
		if (v) {
			return cmdWith2ByteParam(0x0124, 1);
		} else {
			return cmdWith2ByteParam(0x0124, 0);
		}
	}
	
	public static byte[] identifyFree() {
		return cmd(0x0125);
	}	
	
	public static byte[] getEnrollCount() {
		return cmd(0x0128);
	}	

	public static byte[] fpCancel() {
		return cmd(0x0130);
	}
	
	public static byte[] testConnection() {
		return cmd(0x0150);
	}	
	
	/**
	 * Obtiene el id del tipo de packete de datos.
	 * @param data
	 * @return
	 */
	public static int getId(byte[] data) {
		int id = (data[0] & 0xff) + ((data[1] & 0xff) << 8);
		return id;
	}
	
	/**
	 * Obtiene el comando/rcm del packete.
	 * @param data
	 * @return
	 */
	public static int getCmd(byte[] data) {
		int cmd = (data[2] & 0xff) + ((data[3] & 0xff) << 8);
		return cmd;
	}
	
	/**
	 * retorna el campo longitud del paquete (comando, datos, respuesta, etc) en forma de un entero.
	 * @param data
	 * @return
	 */
	public static int getLength(byte[] data) {
		int length = (data[4] & 0xff) + ((data[5] & 0xff) << 8);
		return length;
	}
	
	/**
	 * Obtiene el codigo de retorno del packete de respuesta.
	 * @param data
	 * @return
	 */
	public static int getRet(byte[] data) {
		int cmd = (data[6] & 0xff) + ((data[7] & 0xff) << 8);
		return cmd;
	}
	
	

	
	/**
	 * Obtiene los datos de respuesta del packete de respuesta.
	 * Identifica el tipo de packete y de acuerdo eso busca los datos en los lugares correctos.
	 * @param data
	 * @return
	 */
	public static byte[] getData(byte[] data) throws ProcessingException {
		int id = getId(data);
		int length = getLength(data);
		
		int from = 0;
		int to = 0;
		int lengthToCheck = 0;
		
		if (id == CMD) {			// cmd packet
			
			lengthToCheck = 24 - 2 - 2 - 2 - 2;
			from = 6;
			to = from + length;
			
		} else if (id == CMD_DATA) {	// cmd data packet

			lengthToCheck = 512 - 2 - 2 - 2 - 2;
			from = 6;
			to = from + length;
			
		} else if (id == RSP) {	// response packet

			lengthToCheck = 24 - 2 - 2 - 2 - 2 - 2;
			from = 8;
			to = from + length;
			
		} else if (id == RSP_DATA) {	// response data packet

			lengthToCheck = 512 - 2 - 2 - 2 - 2 - 2;
			from = 8;
			to = from + length;
			
		}
		
		if (length > lengthToCheck) {
			throw new ProcessingException("longitud incorrecta");
		}
		
		return Arrays.copyOfRange(data, from, to);
	}
	
	
	public static void printPacket(byte[] data) throws ProcessingException {
		
		int id = getId(data);
		int cmd = getCmd(data);
		int length = getLength(data);
		byte[] d = getData(data);
		
		StringBuilder sb = new StringBuilder();
		
		if (id == RSP || id == RSP_DATA) {
			int ret = getRet(data);
			
			sb.append("\nRespuesta : ").append("\n");
			sb.append("ID : ").append(Integer.toHexString(id)).append("\n");
			sb.append("RCMD : ").append(Integer.toHexString(cmd)).append("\n");
			sb.append("LEN : ").append(length).append("\n");
			sb.append("RET : ").append(ret).append("\n");
			sb.append("DATA : ").append(Utils.getHex(d)).append("\n");
			
		} else {

			sb.append("\nComando : ").append("\n");
			sb.append("ID : ").append(Integer.toHexString(id)).append("\n");
			sb.append("CMD : ").append(Integer.toHexString(cmd)).append("\n");
			sb.append("LEN : ").append(length).append("\n");
			sb.append("DATA : ").append(Utils.getHex(d)).append("\n");
		
		}
		
		System.out.println(sb.toString());
		
	}

	
	public static CamabioResponse getResponse(byte[] data) throws ProcessingException {
		CamabioResponse rsp = new CamabioResponse();
		rsp.prefix = getId(data);
		rsp.rcm = getCmd(data);
		rsp.len = getLength(data);
		rsp.data = getData(data);
		return rsp;
		
	}
	
	public static int getDataIn4ByteInt(byte[] data) {
		int result = (data[0] & 0xff) + ((data[1] & 0xff) << 8) + ((data[2] & 0xff) << 16) + ((data[3] & 0xff) << 24);
		return result;
	}
	
	public static int getDataIn2ByteInt(byte[] data) {
		int result = (data[0] & 0xff) + ((data[1] & 0xff) << 8);
		return result;
	}		
	
}
