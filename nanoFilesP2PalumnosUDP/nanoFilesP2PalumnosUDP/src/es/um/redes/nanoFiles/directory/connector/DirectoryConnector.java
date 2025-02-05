package es.um.redes.nanoFiles.directory.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.*;

import es.um.redes.nanoFiles.directory.message.DirMessage;
import es.um.redes.nanoFiles.directory.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DEFAULT_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 60000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	public DirectoryConnector(String address) throws IOException {
		/*
		 * TODO: Crear el socket UDP para comunicación con el directorio durante el
		 * resto de la ejecución del programa, y guardar su dirección (IP:puerto) en
		 * atributos
		 */

		socket = new DatagramSocket();
		directoryAddress = new InetSocketAddress(InetAddress.getByName(address), DEFAULT_PORT);
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	public byte[] sendAndReceiveDatagrams(byte[] requestData) throws IOException {

		/*
		 * TODO: Enviar datos en un datagrama al directorio y recibir una respuesta.
		 * Debe implementarse un mecanismo de reintento usando temporizador, en caso de
		 * que no se reciba respuesta en el plazo de TIMEOUT. En caso de salte el
		 * timeout, se debe reintentar como máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones
		 */
		InetSocketAddress addr = new InetSocketAddress(directoryAddress.getAddress(), DEFAULT_PORT);
		DatagramPacket packet = new DatagramPacket(requestData, requestData.length, addr);
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket response = new DatagramPacket(responseData, responseData.length);
		int n = 0;
		boolean recibido=false;
		while (n < MAX_NUMBER_OF_ATTEMPTS && recibido==false) {
			try {
				socket.send(packet);
				// receive response
				socket.setSoTimeout(TIMEOUT);
				socket.receive(response);
				SocketAddress responseAddr = response.getSocketAddress();
				byte opcode = responseData[0];
				/*System.out.println("Datagram received from server at addr " + responseAddr);
				System.out.println("   Contents: " + DirMessageOps.opcodeToOperation(opcode));*/
				recibido=true;
			} catch (SocketTimeoutException e) {
				n++;
			}
		}

		return responseData;

	}

	public int logIntoDirectory() throws IOException { // Returns number of file servers
		byte[] requestData = DirMessage.buildLoginRequestMessage();
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.processLoginResponse(responseData);
	}

	/*
	 * TODO: Crear un método distinto para cada intercambio posible de mensajes con
	 * el directorio, basándose en logIntoDirectory o registerNickname, haciendo uso
	 * de los métodos adecuados de DirMessage para construir mensajes de petición y
	 * procesar mensajes de respuesta
	 */
	public boolean registerNickname(String nick) throws IOException {
		byte[] requestData = DirMessage.buildRegisterRequestMessage(nick);
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.processRegisterResponse(responseData);
		
	}
	
	public Set<String> getUserList() throws IOException{
		byte[] requestData = DirMessage.buildUserListRequestMessage();
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.processUserListResponseMessage(responseData);
	}
	
	
	public boolean serverFiles(int port, String nickname) throws IOException{
		byte[] requestData = DirMessage.buildServerFilesRequestMessage(port, nickname);
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.processServerFilesResponseMessage(responseData);
	}
	
	public InetSocketAddress searchUser(String nick) throws IOException{
		byte[] requestData = DirMessage.buildSearchRequestMessage(nick);
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.procesSearchResponseMessage(responseData);
	}
	
	public boolean logOutDirectory(String nickname) throws IOException{
		byte[] requestData = DirMessage.buildLogOutRequestMessage(nickname);
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.processLogOutResponseMessage(responseData);
	}
	
	public boolean stopServer(String nickname) throws IOException {
		byte[] requestData = DirMessage.buildStopServerRequestMessage(nickname);
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.processStopServeResponseMessage(responseData);
	}
	
	public FileInfo[] getFiles() throws IOException{
		byte[] requestData = DirMessage.buildFileListRequestMessage();
		byte[] responseData = this.sendAndReceiveDatagrams(requestData);
		return DirMessage.processFileListResponseMessage(responseData);
	}
}
