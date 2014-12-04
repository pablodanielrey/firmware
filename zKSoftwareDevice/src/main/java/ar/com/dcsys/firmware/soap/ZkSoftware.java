package ar.com.dcsys.firmware.soap;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ZkSoftware {

	/**
	 * Reinicia el reloj.
	 */
	public void reboot() throws IOException, ZkSoftwareException;
	
	/**
	 * Actualiza la base interna del reloj despues de cada cambio realizado.
	 */
	public void refreshDB() throws IOException, ZkSoftwareException;
	
	/**
	 * Retorna los templates de las huellas de TODOS los usuarios.
	 */
	public List<UserTemplate> getAllUserTemplates() throws IOException, ZkSoftwareException;
	
	/**
	 * Obtiene la hora del reloj indicado
	 */
	public Date getDate() throws IOException, ZkSoftwareException;
	
	/**
	 * Obtiene los templates de las huellas del usuario con el pin pasado como parámetro.
	 * en el caso de pin = "ALL" retorna todas las huellas de todos los usuarios.
	 */
	public List<UserTemplate> getUserTemplate(String pin) throws IOException, ZkSoftwareException;
	
	/**
	 * Retorna los datos del template de la huella indicada por el parámetro num.
	 * num comienza desde 0.
	 * en el caso de no existir retorna null.
	 * 
	 * @param pin - pin del usuario a obtener la huella
	 * @param num - numero de huella a retornar, (inicia desde 0)
	 */
	public UserTemplate getUserTemplate(String pin, int num) throws IOException, ZkSoftwareException;
	
	
	/**
	 * Setea la info del template de la huella para un usuario.
	 * No se por que estoy viendo que duplica el usuario aunque exista ya en la base. lo inicializa a 0 todo.
	 * Según la documentación hay que refrescar la base.
	 * 
	 * @param t
	 */
	public boolean setUserTemplate(UserTemplate t) throws IOException, ZkSoftwareException;
	
	/**
	 * Elimina TODA la info de las huellas de un determinado usuario.
	 * 
	 * @param pin - pin del usuario a eliminar las huellas.
	 */
	public boolean deleteUserTemplate(String pin) throws IOException, ZkSoftwareException;
	
	/**
	 * Retorna la info de usuario de TODOS los usuarios.
	 * idem a getUserInfo("ALL")
	 * 
	 * @return
	 */
	public List<UserInfo> getAllUserInfo() throws IOException, ZkSoftwareException;
	
	/**
	 * Retorna la info de usuario del usuario pasádo como parámetro.
	 * si se le pasa ALL entonces retorna la de TODOS los usuarios.
	 * 
	 * @return
	 */
	public List<UserInfo> getUserInfo(String pin) throws IOException, ZkSoftwareException;
	
	
	/**
	 * Setea información de un NUEVO usuario!!!
	 * es equivalente a enrolar a un usuario nuevo!!. no importa que el PIN2 sea el mismo que el de otro usuario!!.
	 * lo duplica!!.
	 * 
	 * IMPORTANTE!!!!
	 * ENCONTRE UN BUG :
	 * CUANDO EL NOMBRE TIENE CUALQUEIR COSA DISTINTA A "", CUANDO SE GUARDA EN EL 
	 * APARATO LA INFO DEL USUARIO CAMBIA EL PASSWORD.
	 * QUEDA PASSWORD + NAME
	 * ASI QUE PARA EVITAR PROBLEMAS SETEO EL NAME = ""
	 */
	public boolean setUserInfo(UserInfo info) throws IOException, ZkSoftwareException;
	
	/**
	 * Borra un usuario del reloj con el pin = al pasado como parámetro.
	 * NO funciona pasarle ALL, si se quiere borrar a todos los usuarios hay que usar clearData.
	 */
	public boolean deleteUser(String pin) throws IOException, ZkSoftwareException;

	/**
	 * Elimina la clave del usuario determinado por el pin.	
	 * @param pin
	 */
	public boolean clearUserPassword(String pin) throws IOException, ZkSoftwareException;
	
	/**
	 * Obtiene TODOS los logs de marcaciones de todos los usuarios.
	 */
	public List<AttLog> getAllAttLogs() throws IOException, ZkSoftwareException;
	
	/**
	 * Obtiene los logs de marcaciones del pin pasado como parámetro.
	 * si pin = "ALL" entonces obtiene TODOS los logs de TODOS los usuarios.
	 */
	public List<AttLog> getAttLogs(String pin) throws IOException, ZkSoftwareException;
	
	
	/**
	 * Elimina todos los usuarios del reloj y sus templates de huellas.
	 */
	public boolean deleteUsers() throws IOException, ZkSoftwareException;
	
	/**
	 * Elimina todos los templates de las huellas de todos los usuarios
	 */
	public boolean deleteTemplates() throws IOException, ZkSoftwareException;
	
	/**
	 * Elimina los registros de marcaciones.
	 */
	public boolean deleteAttLogs() throws IOException, ZkSoftwareException;
	
}
	

