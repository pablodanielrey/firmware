package ar.com.dcsys.firmware.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

public class ZkSoftwareImpl implements ZkSoftware {
	
	private boolean debug = false;
	private URL device;
	private final XMLOutputter xmlOut = new XMLOutputter();
	private final SAXBuilder builder = new SAXBuilder();
	
	public ZkSoftwareImpl(URL device) {
		this.device = device;
	}

	public void setDebug(boolean d) {
		this.debug = d;
	}
	
	/**
	 * Chequea que la respuesta tenga el siguiente formato :
	 * (No importa el método!!)
	 * 
	 * <SetUserInfoResponse>
	 * 	<Row>
	 * 		<Result>1</Result>
	 * 		<Information>Successfully!</Information>
	 * 	</Row>
	 * </SetUserInfoResponse>
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private boolean checkResult(Document response) throws IOException {
		try {
			String result = response.getRootElement().getChild("Row").getChildText("Result");
			if (result.trim().equals("1")) {
				// OK
				return true;
			}
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		return false;
	}
	
	/**
	 * Envia y recibe un xml al servicio web del device.
	 * @param xml
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	private Document sendAndReceive(Document xml) throws IOException, ZkSoftwareException {
		try {

			if (debug) {
				System.out.println(xmlOut.outputString(xml));
			}
			
			URLConnection url = device.openConnection();
			url.setDoOutput(true);
			url.setRequestProperty("Content-type", "text/xml");
			url.setRequestProperty("SOAPAction", "uri:zksoftware");

			OutputStreamWriter out = new OutputStreamWriter(url.getOutputStream());
			out.write(xmlOut.outputString(xml));
			out.flush();
			out.close();
			
			InputStream in = url.getInputStream();
			Document response = builder.build(in);
			
			if (debug) {
				System.out.println(xmlOut.outputString(response));
			}
			
			return response;
			
		} catch (MalformedURLException e) {
			throw new IOException(e);
		} catch (JDOMException e) {
			throw new ZkSoftwareException(e);
		}
	}
	
	
	/**
	 * Reinicia el reloj.
	 * 
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	@Override
	public void reboot() throws IOException, ZkSoftwareException {
		Element soapMethod = new Element("Restart");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		
		Document doc = new Document(soapMethod);
		sendAndReceive(doc);
	}
	
	
	/**
	 * Actualiza la base interna del reloj despues de cada cambio realizado.
	 * 
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public void refreshDB() throws IOException, ZkSoftwareException {
		Element soapMethod = new Element("RefreshDB");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		
		Document doc = new Document(soapMethod);
		sendAndReceive(doc);
	}
	
	/**
	 * Retorna los templates de las huellas de TODOS los usuarios.
	 * 
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public List<UserTemplate> getAllUserTemplates() throws IOException, ZkSoftwareException {
		return getUserTemplate("ALL");
	}
	
	
	
	/**
	 * Obtiene la hora del reloj indicado
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public Date getDate() throws IOException, ZkSoftwareException {
		Element soapMethod = new Element("GetDate");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		Element data = resp.getRootElement().getChild("Row");
		String dateS = data.getChildText("Date");
		String timeS = data.getChildText("Time");
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return format.parse(dateS.trim() + " " + timeS.trim());
		} catch (java.text.ParseException e) {
			throw new ZkSoftwareException(e);
		}
	}	
	
	/**
	 * Obtiene los templates de las huellas del usuario con el pin pasado como parámetro.
	 * en el caso de pin = "ALL" retorna todas las huellas de todos los usuarios.
	 * 
	 * @param pin
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public List<UserTemplate> getUserTemplate(String pin) throws IOException, ZkSoftwareException {
		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(pin));

		Element soapMethod = new Element("GetUserTemplate");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);
		
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		
		@SuppressWarnings("unchecked")
		List<Element> data = resp.getRootElement().getChildren("Row");
		List<UserTemplate> templates = new ArrayList<UserTemplate>(data.size());
		
		for (Element e : data) {
			UserTemplate template = new UserTemplate();
			
			template.setPin(e.getChildText("PIN"));
			template.setFingerId(e.getChildText("FingerID"));
			template.setSize(e.getChildText("Size"));
			template.setValid(e.getChildText("Valid"));
			template.setTemplate(e.getChildText("Template"));
			
			templates.add(template);
		}
		
		return templates;
	}
	
	/**
	 * Retorna los datos del template de la huella indicada por el parámetro num.
	 * num comienza desde 0.
	 * en el caso de no existir retorna null.
	 * 
	 * 
	 * @param pin
	 * @param num
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public UserTemplate getUserTemplate(String pin, int num) throws IOException, ZkSoftwareException {
		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(pin));
		arg.addContent(new Element("FingerID").setText(String.valueOf(num)));

		Element soapMethod = new Element("GetUserTemplate");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);
		
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		Element e = resp.getRootElement().getChild("Row");
		if (e == null) {
			return null;
		}

		UserTemplate template = new UserTemplate();
		
		template.setPin(e.getChildText("PIN"));
		template.setFingerId(e.getChildText("FingerID"));
		template.setSize(e.getChildText("Size"));
		template.setValid(e.getChildText("Valid"));
		template.setTemplate(e.getChildText("Template"));
			
		return template;
	}
	
	
	/**
	 * Setea la info del template de la huella para un usuario.
	 * No se por que estoy viendo que duplica el usuario aunque exista ya en la base. lo inicializa a 0 todo.
	 * Según la documentación hay que refrescar la base.
	 * 
	 * @param t
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public boolean setUserTemplate(UserTemplate t) throws IOException, ZkSoftwareException {
		
		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(t.getPin()));
		arg.addContent(new Element("FingerID").setText(t.getFingerId()));
		arg.addContent(new Element("Size").setText(t.getSize()));
		arg.addContent(new Element("Valid").setText(t.getValid()));
		arg.addContent(new Element("Template").setText(t.getTemplate()));

		Element soapMethod = new Element("SetUserTemplate");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);
		
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		return checkResult(resp);
	}
	
	/**
	 * Elimina TODA la info de las huellas de un determinado usuario.
	 * 
	 * @param pin
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public boolean deleteUserTemplate(String pin) throws IOException, ZkSoftwareException {

		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(pin));

		Element soapMethod = new Element("DeleteTemplate");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);
		
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		return checkResult(resp);
	}
	
	
	/**
	 * REtorna la info de usuario de TODOS los usuarios.
	 * idem a getUserInfo("ALL")
	 * 
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public List<UserInfo> getAllUserInfo() throws IOException, ZkSoftwareException {
		Element soapMethod = new Element("GetAllUserInfo");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));

		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		@SuppressWarnings("unchecked")
		List<Element> data = resp.getRootElement().getChildren("Row");
		List<UserInfo> users = new ArrayList<UserInfo>(data.size());
		
		for (Element e : data) {
			UserInfo u = new UserInfo();
			
			u.setPin(e.getChildText("PIN"));
			u.setName(e.getChildText("Name"));
			u.setPassword(e.getChildText("Password"));
			u.setGroup(e.getChildText("Group"));
			u.setPrivilege(e.getChildText("Privilege"));
			u.setCardNumber(e.getChildText("Card"));
			u.setPin2(e.getChildText("PIN2"));
			
			users.add(u);
		}
		
		return users;
	}
	
	/**
	 * Retorna la info de usuario del usuario pasádo como parámetro.
	 * si se le pasa ALL entonces retorna la de TODOS los usuarios.
	 * 
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public List<UserInfo> getUserInfo(String pin) throws IOException, ZkSoftwareException {
		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(pin));

		Element soapMethod = new Element("GetUserInfo");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);
		
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		
		@SuppressWarnings("unchecked")
		List<Element> data = resp.getRootElement().getChildren("Row");
		List<UserInfo> users = new ArrayList<UserInfo>(data.size());
		
		for (Element e : data) {
			UserInfo u = new UserInfo();
			
			u.setPin(e.getChildText("PIN"));
			u.setName(e.getChildText("Name"));
			u.setPassword(e.getChildText("Password"));
			u.setGroup(e.getChildText("Group"));
			u.setPrivilege(e.getChildText("Privilege"));
			u.setCardNumber(e.getChildText("Card"));
			u.setPin2(e.getChildText("PIN2"));
			
			users.add(u);
		}
		
		return users;
	}
	
	
	/**
	 * Setea información de un NUEVO usuario!!!
	 * es equivalente a enrolar a un usuario nuevo!!. no importa que el PIN2 sea el mismo que el de otro usuario!!.
	 * lo duplica!!. 
	 * 
	 * @param info
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public boolean setUserInfo(UserInfo info) throws IOException, ZkSoftwareException {
		
		/*
		 * IMPORTANTE!!!!
		 * ENCONTRE UN BUG :
		 * CUANDO EL NOMBRE TIENE CUALQUEIR COSA DISTINTA A "", CUANDO SE GUARDA EN EL 
		 * APARATO LA INFO DEL USUARIO CAMBIA EL PASSWORD.
		 * QUEDA PASSWORD + NAME
		 * ASI QUE PARA EVITAR PROBLEMAS SETEO EL NAME = ""
		 */
		
		info.setName("");
		
		// BUG!!! no importa que pin se le ponga siempre agrega un usuario.
		//info.setPin("0");
		
		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(info.getPin2()));
		arg.addContent(new Element("Name").setText(info.getName()));
		arg.addContent(new Element("Password").setText(info.getPassword()));
		arg.addContent(new Element("Group").setText(info.getGroup()));
		arg.addContent(new Element("Privilege").setText(info.getPrivilege()));
		arg.addContent(new Element("Card").setText(info.getCardNumber()));
		arg.addContent(new Element("PIN2").setText(info.getPin2()));
		arg.addContent(new Element("TZ1").setText("0"));
		arg.addContent(new Element("TZ2").setText("0"));
		arg.addContent(new Element("TZ3").setText("0"));

		Element soapMethod = new Element("SetUserInfo");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);
		
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		return checkResult(resp);
	}
	
	/**
	 * Borra un usuario del reloj con el pin = al pasado como parámetro.
	 * NO funciona pasarle ALL, si se quiere borrar a todos los usuarios hay que usar clearData.
	 * 
	 * @param pin
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public boolean deleteUser(String pin) throws IOException, ZkSoftwareException {
		
		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(pin));

		Element soapMethod = new Element("DeleteUser");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);

		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		return checkResult(resp);
	}
	
	
	public boolean clearUserPassword(String pin) throws IOException, ZkSoftwareException {
		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(pin));

		Element soapMethod = new Element("ClearUserPassword");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);

		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);
		
		return checkResult(resp);
	}
	
	
	/**
	 * Obtiene TODOS los logs de transacciones de marcaciones.
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public List<AttLog> getAllAttLogs() throws IOException, ZkSoftwareException {
		return getAttLogs("ALL");
	}
	
	/**
	 * Obtiene los logs de marcaciones del pin pasado como parámetro.
	 * si pin = "ALL" entonces obtiene TODOS los logs de TODOS los usuarios.
	 * 
	 * @param pin
	 * @return
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public List<AttLog> getAttLogs(String pin) throws IOException, ZkSoftwareException {

		Element arg = new Element("Arg");
		arg.addContent(new Element("PIN").setText(pin));

		Element soapMethod = new Element("GetAttLog");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);
		
		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);

		
		List<AttLog> returnLogs = new ArrayList<AttLog>();
		
		@SuppressWarnings("unchecked")
		List<Element> data = resp.getRootElement().getChildren("Row");
		for (Element e : data) {
			AttLog log = new AttLog();
			
			log.setPin(e.getChildText("PIN"));
			log.setDateTime(e.getChildText("DateTime"));
			log.setVerifiedMode(e.getChildText("Verified"));
			log.setStatus(e.getChildText("Status"));
			log.setWorkCode(e.getChildText("WorkCode"));
			
			returnLogs.add(log);
		}
		
		return returnLogs;
	}
	
	
	/**
	 * Elimina todos los usuarios del reloj y sus templates de huellas.
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public boolean deleteUsers() throws IOException, ZkSoftwareException {
		return clearData("1");
	}
	
	/**
	 * Elimina los templates de las huellas
	 * 
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public boolean deleteTemplates() throws IOException, ZkSoftwareException {
		return clearData("2");
	}
	
	/**
	 * Elimina los registros de marcaciones.
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	public boolean deleteAttLogs() throws IOException, ZkSoftwareException {
		return clearData("3");
	}
	
	
	/**
	 * data = 
	 * 		1 - usuarios + templates
	 * 		2 - templates
	 * 		3 - transactions 
	 * 
	 * respuesta:
	 * 
	 * <ClearDataResponse>
	 * 	<Row>
	 * 		<Result>1</Result>
	 * 		<Information>Successfully!</Information>
	 * 	</Row>
	 * </ClearDataResponse>
	 * 
	 * @throws IOException
	 * @throws ZkSoftwareException
	 */
	private boolean clearData(String data) throws IOException, ZkSoftwareException {
		Element arg = new Element("Arg");
		
		// valores : 1 = usuarios + finger, 2 = finger, 3 = transactions 
		arg.addContent(new Element("Value").setText(data));

		Element soapMethod = new Element("ClearData");
		soapMethod.addContent(new Element("ArgComKey").setText("0"));
		soapMethod.addContent(arg);

		Document doc = new Document(soapMethod);
		Document resp = sendAndReceive(doc);

		return checkResult(resp);
	}
	
}
