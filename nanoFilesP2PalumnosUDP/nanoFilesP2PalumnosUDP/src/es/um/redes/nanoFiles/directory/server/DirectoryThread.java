package es.um.redes.nanoFiles.directory.server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import es.um.redes.nanoFiles.directory.message.DirMessage;
import es.um.redes.nanoFiles.directory.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class DirectoryThread extends Thread {

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	protected DatagramSocket socket = null;

	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	protected double messageDiscardProbability;

	/**
	 * Estructura para guardar los nicks de usuarios registrados, y la fecha/hora de
	 * registro
	 * 
	 */
	private HashMap<String, LocalDateTime> nicks;
	/**
	 * Estructura para guardar los usuarios servidores (nick, direcciones de socket
	 * TCP)
	 */
	// TCP)
	private HashMap<String, InetSocketAddress> servers;
	/**
	 * Estructura para guardar la lista de ficheros publicados por todos los peers
	 * servidores, cada fichero identificado por su hash
	 */
	private HashMap<String, FileInfo> files;
	
	private HashMap<String, String> owners;
	
	public DirectoryThread(int directoryPort, double corruptionProbability) throws SocketException {
		/*
		 * TODO: Crear dirección de socket con el puerto en el que escucha el directorio
		 */
		InetSocketAddress serverAddress = new InetSocketAddress(directoryPort);
		// TODO: Crear el socket UDP asociado a la dirección de socket anterior
		socket = new DatagramSocket(serverAddress);
		messageDiscardProbability = corruptionProbability;
		nicks = new HashMap<>();
		servers = new HashMap<>();
		files = new HashMap<>();
		owners = new HashMap<>();
	}

	public void run() {
		byte[] receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket requestPacket = new DatagramPacket(receptionBuffer, receptionBuffer.length);
		InetSocketAddress clientId = null;

		System.out.println("Iniciando directorio...");

		while (true) {
			try {

				// TODO: Recibimos a través del socket el datagrama con mensaje de solicitud
				socket.receive(requestPacket);
				// TODO: Averiguamos quién es el cliente
				InetSocketAddress clientAddr = (InetSocketAddress) requestPacket.getSocketAddress();
				byte opcode = receptionBuffer[0];
				/*System.out.println("Datagrama recibido en la dirección" + clientAddr);
				System.out.println("   Contenido: " + DirMessageOps.opcodeToOperation(opcode));*/

				// Do something with message received in “recvBuf”
				
				// Vemos si el mensaje debe ser descartado por la probabilidad de descarte
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println("Directorio descarto un datagrama de " + clientId);
					continue;
				}

				// Analizamos la solicitud y la procesamos

				if(requestPacket.getLength() > 0){
					processRequestFromClient(requestPacket.getData(), clientAddr);
				}else {
					System.err.println("Directorio recibio un datagrma vacío de " + clientId);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Directorio recibio un datagrma vacío de " + clientId);
				break;
			}
		}
		// Cerrar el socket
		socket.close();
	}

	// Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		// TODO: Construir un objeto mensaje (DirMessage) a partir de los datos
		// recibidos
		DirMessage request = DirMessage.buildMessageFromReceivedData(data);
		switch (request.getOpcode()){
		case DirMessageOps.OPCODE_LOGIN:
			sendLoginOK(clientAddr);
			break;
		case DirMessageOps.OPCODE_REGISTER_USERNAME:
			String nombre = request.getUserName();
			if(nicks.containsKey(nombre)){
				sendRegisterFail(clientAddr);
			}else {
				nicks.put(nombre, LocalDateTime.now());
				sendRegisterOK(clientAddr);
			}
			break;
		case DirMessageOps.OPCODE_GETUSERS:
				sendUserList(clientAddr);
			break;
		case DirMessageOps.OPCODE_SERVE_FILES:
			servers.put(request.getUserName(), new InetSocketAddress(clientAddr.getAddress(), request.getPort()));
			FileInfo[] ficheros = request.getFiles();
			for (FileInfo fi : ficheros) {
				files.put(fi.getHash(), fi);
				owners.put(fi.getHash(), request.getUserName());
			}
 			sendServerOk(clientAddr);
			break;
		case DirMessageOps.OPCODE_LOOKUP_USERNAME:
			String buscado = request.getUserName();
			if(servers.containsKey(buscado)) {
				sendSearchFound(servers.get(buscado), clientAddr);
			}else {
				sendSearchNotFound(servers.get(buscado));
			}
			break;
		case DirMessageOps.OPCODE_LOGOUT:
			nicks.remove(request.getUserName());
			sendLogOut(clientAddr);
			break;
		case DirMessageOps.OPCODE_SERVE_STOP:
			servers.remove(request.getUserName());
			HashSet<String> hash = new HashSet<>();
			for (String nom : owners.keySet()) {
				hash.add(nom);
			}
			
			for (String nom : hash) {
				if(owners.get(nom).equals(request.getUserName())) {
					owners.remove(nom);
					files.remove(nom);
				}
			}
			sendStopOk(clientAddr);
			break;
		case DirMessageOps.OPCODE_GETFILES:
			FileInfo[] fis = new FileInfo[files.keySet().size()] ;
			int i =0;
			for (String s : files.keySet()) {
				fis[i]=files.get(s);
				i++;
			}
			sendFileList(fis, clientAddr);
			break;
		default:
			throw new IllegalArgumentException("Valor inesperado: " + request.getOpcode());
		}
		
		// TODO: Actualizar estado del directorio y enviar una respuesta en función del
		// tipo de mensaje recibido
	}

	// Método para enviar la confirmación del registro
	private void sendLoginOK(InetSocketAddress clientAddr) throws IOException {
		// TODO: Construir el datagrama con la respuesta y enviarlo por el socket al
		// cliente
		byte[] responseData = DirMessage.buildLoginOKResponseMessage(servers.size());
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendRegisterOK(InetSocketAddress clientAddr) throws IOException {
		// TODO: Construir el datagrama con la respuesta y enviarlo por el socket al
		// cliente
		byte[] responseData = DirMessage.buildRegisterOKResponseMessage();
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendRegisterFail(InetSocketAddress clientAddr) throws IOException {
		// TODO: Construir el datagrama con la respuesta y enviarlo por el socket al
		// cliente
		byte[] responseData = DirMessage.buildRegisterFailResponseMessage();
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendUserList(InetSocketAddress clientAddr) throws IOException {
		// TODO: Construir el datagrama con la respuesta y enviarlo por el socket al
		// cliente
		Set<String> lista = new HashSet<>();
		for ( String nick : nicks.keySet()) {
			if(servers.containsKey(nick)) {
				nick = nick + "(Servidor)";
			}
			lista.add(nick);
		}
		
		byte[] responseData = DirMessage.buildUserListResponseMessage(lista);
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendServerOk(InetSocketAddress clientAddr) throws IOException{
		byte[] responseData = DirMessage.buildServerResponseMessage();
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendSearchFound(InetSocketAddress addr, InetSocketAddress clientAddr) throws IOException{
		byte[] responseData = DirMessage.buildSeachFoundMessage(addr);
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}

	private void sendSearchNotFound(InetSocketAddress clientAddr) throws IOException {
		byte[] responseData = DirMessage.buildSearchNotFoundMessage();
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendLogOut(InetSocketAddress clientAddr) throws IOException {
		byte[] responseData = DirMessage.buildQuitMessage();
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendStopOk(InetSocketAddress clientAddr) throws IOException {
		byte[] responseData = DirMessage.buildStopOk();
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
	}
	
	private void sendFileList(FileInfo[] ficheros, InetSocketAddress clientAddr) throws IOException{
		byte[] responseData = DirMessage.buildFileListResponseMessage(ficheros);
		DatagramPacket datagrama = new DatagramPacket(responseData, responseData.length, clientAddr);
		socket.send(datagrama);
		
	}
	
}
