package ar.com.dcsys.firmware.camabio;

import java.util.Arrays;

import ar.com.dcsys.firmware.Utils;
import ar.com.dcsys.firmware.cmd.ProcessingException;

public class CamabioUtils {
	
	
	public static final String ALGORITHM = "Camabio";
	public static final String CODIFICATION = "none";
	
	
	public static final int CMD_VERIFY = 0x101;
	public static final int CMD_IDENTIFY = 0x102;
	public static final int CMD_ENROLL = 0x103;
	public static final int CMD_ENROLL_ONE_TIME = 0x104;
	public static final int CMD_CLEAR_TEMPLATE = 0x105;
	public static final int CMD_CLEAR_ALL_TEMPLATE = 0x106;
	public static final int CMD_GET_EMTPY_ID = 0x107;
	public static final int CMD_GET_TEMPLATE_STATUS = 0x108;
	public static final int CMD_GET_BROKEN_TEMPLATE = 0x109;
	public static final int CMD_READ_TEMPLATE = 0x10A;
	public static final int CMD_WRITE_TEMPLATE = 0x10B;
	public static final int CMD_SET_SECURITY_LEVEL = 0x10C;
	public static final int CMD_GET_SECURITY_LEVEL = 0x10D;
	public static final int CMD_SET_FINGER_TIME_OUT = 0x10E;
	public static final int CMD_GET_FINGER_TIME_OUT = 0x10F;
	public static final int CMD_SET_DEVICE_ID = 0x110;
	public static final int CMD_GET_DEVICE_ID = 0x111;
	public static final int CMD_GET_FW_VERSION = 0x112;
	public static final int CMD_FINGER_DETECT = 0x113;
	public static final int CMD_SET_BAUDRATE = 0x114;
	public static final int CMD_SET_DUPLICATION_CHECK = 0x115;
	public static final int CMD_GET_DUPLICATION_CHECK = 0x116;
	public static final int CMD_ENTER_STAND_BY = 0x117;
	public static final int CMD_ENROLL_AND_STORE_IN_RAM = 0x118;
	public static final int CMD_GET_ENROLL_DATA = 0x119;
	public static final int CMD_GET_FEATURE_DATA_CAPTURED_FP = 0x11A;
	public static final int CMD_VERIFY_DOWNLOADED_CAPTURED_FP = 0x11B;
	public static final int CMD_IDENTIFY_DOWNLOADED_CAPTURED_FP = 0x11C;
	public static final int CMD_GET_DEVICE_NAME = 0x121;
	public static final int CMD_SENSOR_LED_CONTROL = 0x124;
	public static final int CMD_IDENTIFY_FREE = 0x125;
	public static final int CMD_SET_DEVICE_PASSWORD = 0x126;
	public static final int CMD_VERIFY_DEVICE_PASSWORD = 0x127;
	public static final int CMD_GET_ENROLL_COUNT = 0x128;
	public static final int CMD_FP_CANCEL = 0x130;
	public static final int CMD_TEST_CONNECTION = 0x150;
	public static final int CMD_INCORRECT_COMMAND = 0x160;
	
	

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
	
	private static void writeCommandData(byte[] cmd, int command) {
		cmd[0] = (byte)0xA5;
		cmd[1] = (byte)0x5A;
		
		cmd[2] = (byte)(command & 0xff);
		cmd[3] = (byte)((command & 0xff00) >> 8);
	}	
	
	private static void writeLength(byte[] cmd, int length) {
		cmd[4] = (byte)(length & 0xff);
		cmd[5] = (byte)((length & 0xff00) >> 8);
	}
	

	
	/**
	 * calcula y chequea el checksum del paquete. y retorna ok si esta ok.
	 * @param cmd
	 * @return
	 */
	public static boolean checkChksum(byte[] cmd) {
		byte[] sum = chksum(cmd);
		int len = cmd.length;
		return (sum[0] == cmd[len-2] && sum[1] == cmd[len-1]);
	}
	
	
	/**
	 * Calcula el checksum del paquete de datos y lo retorna en su orden para insertar en el paquete.
	 * @param cmd
	 * @return
	 */
	private static byte[] chksum(byte[] cmd) {
		byte[] csm = new byte[2];
		
		long sum = 0;
		int length = cmd.length - 2;			// los 2 ultimos bytes se ignoran (son los del chksum)
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
	 * Template del comando para los que llevan como parametro el id del template y datos adicionales por ejemplo el template mismo.
	 * @param command
	 * @param template
	 * @return
	 */
	public static byte[] cmdWithTemplateIdAndData(int command, int template, byte[] data) {
		
		byte[] cmd = new byte[24];
		zeroBuffer(cmd);
		
		writeCommandData(cmd, command);
		writeLength(cmd, data.length + 2);
		
		cmd[6] = (byte)(template & 0xff);
		cmd[7] = (byte)((template & 0xff00) >> 8);

		// copio los datos.
		for (int i = 0; i < data.length; i++) {
			cmd[8 + i] = data[i];
		}
		
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
		return cmdWithTemplateId(CMD_VERIFY, template);
	}
	
	
	public static byte[] identify() {
		return cmd(CMD_IDENTIFY);
	}
	
	public static byte[] enroll(int template) {
		return cmdWithTemplateId(CMD_ENROLL, template);
	}
	
	
	public static byte[] enrollOneTime(int template) {
		return cmdWithTemplateId(CMD_ENROLL_ONE_TIME, template);
	}
	
	
	public static byte[] clearTemplate(int template) {
		return cmdWithTemplateId(CMD_CLEAR_TEMPLATE, template);
	}
	
	
	public static byte[] clearAllTemplate() {
		return cmd(CMD_CLEAR_ALL_TEMPLATE);
	}	
	
	
	public static byte[] getEmptyId() {
		return cmd(CMD_GET_EMTPY_ID);
	}	
	
	/**
	 * Chequea si es vacío el numero de template o si ya tiene algun template.
	 * @param template
	 * @return
	 */
	public static byte[] getTemplateStatus(int template) {
		return cmdWithTemplateId(CMD_GET_TEMPLATE_STATUS, template);
	}
	
	/**
	 * Chequea si la flash del modulo puede estar corrupa y cual es la primera corrupta.
	 * @return
	 */
	public static byte[] getBrokenTemplate() {
		return cmd(CMD_GET_BROKEN_TEMPLATE);
	}	

	public static byte[] readTemplate(int template) {
		return cmdWithTemplateId(CMD_READ_TEMPLATE, template);
	}	
	
	
	public static byte[] writeTemplate(int size) {
		return cmdWith2ByteParam(CMD_WRITE_TEMPLATE, size);
	}
	
	public static byte[] writeTemplateData(int number, byte[] template) {
		return cmdWithTemplateIdAndData(CMD_WRITE_TEMPLATE, number, template);
	}
	
	
	public static byte[] setSecurityLevel(int level) {
		return cmdWith2ByteParam(CMD_SET_SECURITY_LEVEL, level);
	}
	
	public static byte[] getSecurityLevel() {
		return cmd(CMD_GET_SECURITY_LEVEL);
	}	
	
	/**
	 * Setea el período de espera para que el lector lea una huella.
	 * valores posibles : 0s - 60s
	 * default : 5s
	 * @param time
	 * @return
	 */
	public static byte[] setFingerTimeOut(int time) {
		return cmdWith2ByteParam(CMD_SET_FINGER_TIME_OUT, time);
	}
	
	public static byte[] getFingerTimeOut() {
		return cmd(CMD_GET_FINGER_TIME_OUT);
	}		

	/**
	 * Setea el id del dispositivo.
	 * valores posibles : 1 - 254
	 * default : 1
	 * @param id
	 * @return
	 */
	public static byte[] setDeviceId(int id) {
		return cmdWith2ByteParam(CMD_SET_DEVICE_ID, id);
	}
	
	public static byte[] getDeviceId() {
		return cmd(CMD_GET_DEVICE_ID);
	}		

	public static byte[] getFirmwareVersion() {
		return cmd(CMD_GET_FW_VERSION);
	}		
	
	public static byte[] detectFinger() {
		return cmd(CMD_FINGER_DETECT);
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
		return cmdWith2ByteParam(CMD_SET_BAUDRATE, id);
	}
	
	public static byte[] setDupplicationCheck(boolean v) {
		if (v) {
			return cmdWith2ByteParam(CMD_SET_DUPLICATION_CHECK, 1);
		} else {
			return cmdWith2ByteParam(CMD_SET_DUPLICATION_CHECK, 0);
		}
	}
	
	public static byte[] getDupplicationCheck() {
		return cmd(CMD_GET_DUPLICATION_CHECK);
	}		
	
	public static byte[] enterStandByMode() {
		return cmd(CMD_ENTER_STAND_BY);
	}		
	
	public static byte[] enrollAndStoreInRam() {
		return cmd(CMD_ENROLL_AND_STORE_IN_RAM);
	}		
	
	/**
	 * Obtiene los datos de enrolamiento que estan almacenados en la ram. 
	 * previamente almacenados usando enrollAndStoreInRam.
	 * @return
	 */
	public static byte[] getEnrollData() {
		return cmd(CMD_GET_ENROLL_DATA);
	}		

	/**
	 * parecido a hacer un EnrollOneTime + getEnrollData
	 * se enrola al usuario pidiendo solo una huella y se retorna ese template al host.
	 * no se almacena nada en el dispositivo. todo se hace en ram.
	 * @return
	 */
	public static byte[] getFeatureDataOfCapturedFP() {
		return cmd(CMD_GET_FEATURE_DATA_CAPTURED_FP);
	}		
	
	public static byte[] getDeviceName() {
		return cmd(CMD_GET_DEVICE_NAME);
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
			return cmdWith2ByteParam(CMD_SENSOR_LED_CONTROL, 1);
		} else {
			return cmdWith2ByteParam(CMD_SENSOR_LED_CONTROL, 0);
		}
	}
	
	public static byte[] identifyFree() {
		return cmd(CMD_IDENTIFY_FREE);
	}	
	
	public static byte[] getEnrollCount() {
		return cmd(CMD_GET_ENROLL_COUNT);
	}	

	public static byte[] fpCancel() {
		return cmd(CMD_FP_CANCEL);
	}
	
	public static byte[] testConnection() {
		return cmd(CMD_TEST_CONNECTION);
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
		rsp.ret = getRet(data);
		rsp.data = getData(data);
		
		byte[] sum = chksum(data);
		rsp.chksum = ((sum[1] & 0xff) << 8) + (sum[0] & 0xff);
		
		// verifico el chksum
		int len = data.length;
		if (sum[0] != data[len-2] || sum[1] != data[len-1]) {
			throw new ProcessingException(String.format("chksum = %h ==> %h,%h != %h,%h",rsp.chksum, sum[0], sum[1], data[len-2], data[len-1]));
		}
		
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
