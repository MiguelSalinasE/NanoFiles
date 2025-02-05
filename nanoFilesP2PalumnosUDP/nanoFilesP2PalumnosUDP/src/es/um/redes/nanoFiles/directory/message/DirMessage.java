package es.um.redes.nanoFiles.directory.message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

import es.um.redes.nanoFiles.client.application.NanoFiles;
import es.um.redes.nanoFiles.message.PeerMessage;
import es.um.redes.nanoFiles.util.FileInfo;


public class DirMessage {

	public static final int PACKET_MAX_SIZE = 65507;

	public static final byte OPCODE_SIZE_BYTES = 1;

	private byte opcode;

	private String userName;
	
	private Set<String> userList;
	
	private int port;
	
	private String ip;
	
	private String puertoBrowse;
	
	private FileInfo[] fileList;
	
	private int numServers;
	
	public DirMessage(byte operation) {
		assert (operation == DirMessageOps.OPCODE_LOGIN || operation == DirMessageOps.OPCODE_SERVE_STOP || operation == DirMessageOps.OPCODE_QUIT || operation == DirMessageOps.OPCODE_SERVE_STOP_OK || operation == DirMessageOps.OPCODE_GETFILES ) ;
		opcode = operation;
	}
	
	public DirMessage(byte operation, int num) {
		assert(operation == DirMessageOps.OPCODE_LOGIN_OK);
		opcode = operation;
		numServers=num;
	}

	public DirMessage(byte operation, Set<String> lista) {
		assert(operation == DirMessageOps.OPCODE_USERLIST);
		opcode = operation;
		userList=lista;
	}

	public DirMessage(byte operation, String nickname ,int puerto, FileInfo[] ficheros ) {
		assert(operation == DirMessageOps.OPCODE_SERVE_FILES);
		opcode=operation;
		port=puerto;
		fileList=ficheros;
		this.userName=nickname;
	}
	/*
	 * TODO: Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros tipos de datos
	 * 
	 */
	public DirMessage(byte operation, String nick) {
		assert (operation == DirMessageOps.OPCODE_REGISTER_USERNAME || operation == DirMessageOps.OPCODE_LOOKUP_USERNAME || operation == DirMessageOps.OPCODE_LOGOUT || operation == DirMessageOps.OPCODE_SERVE_STOP);
		/*sendUserList
		 * TODO: Añadir al aserto el resto de opcodes de mensajes con los mismos campos
		 * (utilizan el mismo constructor)
		 */
		opcode = operation;
		userName = nick;
	}
	
	public DirMessage(byte operation, FileInfo[] ficheros ) {
		assert(operation == DirMessageOps.OPCODE_FILELIST);
		opcode=operation;
		fileList=ficheros;

	}
	
	public DirMessage(byte operation, String puerto, String ipN) {
		assert(operation == DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND);
		opcode=operation;
		ip=ipN;
		puertoBrowse=puerto;
	}
	/**
	 * Método para obtener el tipo de mensaje (opcode)
	 * @return
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	public Set<String> getUserList(){
		return userList;
	}
	
	public int getPort() {
		return port;
	}
	
	public FileInfo[] getFiles() {
		return fileList;
	}
	
	public String getIp() {
		return ip;
	}
	
	public String getPuertoBrowse() {
		return puertoBrowse;
	}
	
	public int getNumSer() {
		return numServers;
	}


	public String getUserName() {
		if (userName == null) {
			System.err.println(
					"PANIC: DirMessage.getUserName called but 'userName' field is not defined for messages of type "
							+ DirMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return userName;
	}


	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El
	 * @return
	 */
	public static DirMessage buildMessageFromReceivedData(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get();
		DirMessage mensaje = null;
		switch (opcode) {
		case DirMessageOps.OPCODE_LOGIN: 
			mensaje = new DirMessage(opcode);
			break;
		case DirMessageOps.OPCODE_REGISTER_USERNAME:
			int longitud = buf.getInt();
			byte[] nombre = new byte[longitud];
			buf.get(nombre);
			mensaje = new DirMessage(opcode, new String(nombre));
			break;
		case DirMessageOps.OPCODE_GETUSERS:
			mensaje = new DirMessage(opcode);
			break;
		case DirMessageOps.OPCODE_USERLIST:
			Set<String> nombres = new HashSet<String>();
			int numUsers = buf.getInt();
			for(int i=0;i<numUsers;i++){
				int longitud2 = buf.getInt();
				byte[] nombre2 = new byte[longitud2];
				buf.get(nombre2);
				nombres.add(new String(nombre2));
			}
			mensaje = new DirMessage(opcode, nombres);
			
			break;
			
		case DirMessageOps.OPCODE_SERVE_FILES:
			int longitudNombre = buf.getInt();
			byte[] nick = new byte[longitudNombre];
			buf.get(nick);
			int port = buf.getInt();
			int numCampos = buf.getInt();
			FileInfo[] datos = new FileInfo[numCampos];
			for(int i=0;i<numCampos;i++) {
				int longNom = buf.getInt();
				byte[] nomFich = new byte[longNom];
				buf.get(nomFich);
				int longHash = buf.getInt();
				byte[] nomHash = new byte[longHash];
				buf.get(nomHash);
				long sizeFich = buf.getLong();
				FileInfo f = new FileInfo(new String(nomHash), new String(nomFich), sizeFich, "../nf-shared/" + new String(nomFich));
				datos[i] = f;
			}
			mensaje = new DirMessage(opcode,new String(nick) ,port, datos);
			break;
		case DirMessageOps.OPCODE_LOOKUP_USERNAME:
			int longitudNick = buf.getInt();
			byte[] nickSer = new byte[longitudNick];
			buf.get(nickSer);
			mensaje = new DirMessage(opcode, new String(nickSer));
			break;
		case DirMessageOps.OPCODE_LOGOUT:
			int longitud2 = buf.getInt();
			byte[] nombre2 = new byte[longitud2];
			buf.get(nombre2);
			mensaje = new DirMessage(opcode, new String(nombre2));
			break;
		case DirMessageOps.OPCODE_SERVE_STOP:
			int longitud3 = buf.getInt();
			byte[] nombre3 = new byte[longitud3];
			buf.get(nombre3);
			mensaje = new DirMessage(opcode, new String(nombre3));
			break;
		case DirMessageOps.OPCODE_GETFILES:
			mensaje = new DirMessage(opcode);
			break;
		case DirMessageOps.OPCODE_FILELIST:
			int numCampos1 = buf.getInt();
			FileInfo[] datos1 = new FileInfo[numCampos1];
			for(int i=0;i<numCampos1;i++) {
				int longNom = buf.getInt();
				byte[] nomFich = new byte[longNom];
				buf.get(nomFich);
				int longHash = buf.getInt();
				byte[] nomHash = new byte[longHash];
				buf.get(nomHash);
				long sizeFich = buf.getLong();
				FileInfo f = new FileInfo(new String(nomHash), new String(nomFich), sizeFich, "../nf-shared/" + new String(nomFich));
				datos1[i] = f;
			}
			mensaje = new DirMessage(opcode, datos1);
		
			break;
		case DirMessageOps.OPCODE_QUIT:
			mensaje = new DirMessage(opcode);
			break;
		case DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND:
					int longIpport = buf.getInt();
					byte[] ipportB = new byte[longIpport];
					buf.get(ipportB);
					String ipport = new String(ipportB);
					String[] IPPORT = ipport.split(":");
					String IP = IPPORT[0];
					String PUERTO= IPPORT[1];
					mensaje = new DirMessage(opcode, PUERTO, IP);
			break;
		case DirMessageOps.OPCODE_LOOKUP_USERNAME_NOTFOUND:
			mensaje=null;
			break;
		case DirMessageOps.OPCODE_SERVE_FILES_OK:
			mensaje = new DirMessage(opcode);
			break;
		case DirMessageOps.OPCODE_REGISTER_USERNAME_OK:
			mensaje = new DirMessage(opcode);
			break;
		case DirMessageOps.OPCODE_REGISTER_USERNAME_FAIL:
			mensaje = new DirMessage(opcode);
			break;
		case DirMessageOps.OPCODE_LOGIN_OK:
			int servers = buf.getInt();
			mensaje = new DirMessage(opcode, servers);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + data[0]);
		}
		return mensaje;
	}

	/**
	 * Método para construir una solicitud de ingreso en el directorio
	 * 
	 * @return El array de bytes con el mensaje de solicitud de login
	 */
	public static byte[] buildLoginRequestMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_LOGIN);
		return bb.array();
	}

	/**
	 * Método para construir una respuesta al ingreso del peer en el directorio
	 * 
	 * @param numServers El número de peer registrados como servidor en el
	 *                   directorio
	 * @return El array de bytes con el mensaje de solicitud de login
	 */
	public static byte[] buildLoginOKResponseMessage(int numServers) {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES);
		bb.put(DirMessageOps.OPCODE_LOGIN_OK);
		bb.putInt(numServers);
		return bb.array();
	}

	/**
	 * Método que procesa la respuesta a una solicitud de login
	 * 
	 * @param data El mensaje de respuesta recibido del directorio
	 * @return El número de peer servidores registrados en el directorio en el
	 *         momento del login, o -1 si el login en el servidor ha fallado
	 */
	public static int processLoginResponse(byte[] data) {
		DirMessage mensaje = buildMessageFromReceivedData(data);
		if (mensaje.getOpcode() == DirMessageOps.OPCODE_LOGIN_OK) {
			return mensaje.getNumSer(); // Return number of available file servers
		} else {
			return -1;
		}
		
	}

	/*
	 * TODO: Crear métodos buildXXXXRequestMessage/buildXXXXResponseMessage para
	 * construir mensajes de petición/respuesta
	 */
	public static byte[] buildRegisterRequestMessage(String responseData){
		byte[] nombre = responseData.getBytes();
		int longitud = responseData.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES +longitud);
		bb.put(DirMessageOps.OPCODE_REGISTER_USERNAME);
		bb.putInt(longitud);
		bb.put(nombre);
		return bb.array();
	}
	
	
	public static byte[] buildRegisterOKResponseMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_REGISTER_USERNAME_OK);
		return bb.array();
	}
	
	public static byte[] buildRegisterFailResponseMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_REGISTER_USERNAME_FAIL);
		return bb.array();
	}
	
	public static boolean processRegisterResponse(byte[] data) {
		DirMessage mensaje = buildMessageFromReceivedData(data); 
		if (mensaje.getOpcode() == DirMessageOps.OPCODE_REGISTER_USERNAME_OK) {
			return true; // Return number of available file servers
		} else if(mensaje.getOpcode() == DirMessageOps.OPCODE_REGISTER_USERNAME_FAIL) {
			System.out.println("EL NICK YA ESTA REGISTRADO");
			return false;
		}
		return false;
	}
	
	public static byte[] buildUserListRequestMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_GETUSERS);
		return bb.array();
	}
	
	public static byte[] buildUserListResponseMessage(Set<String> set) {
		int lon = set.size()*Integer.BYTES;
		for (String string : set) {
			lon=lon+string.length();
		}
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES + (byte)lon);
		bb.put(DirMessageOps.OPCODE_USERLIST);
		bb.putInt(set.size());
		for (String string : set) {
			bb.putInt(string.getBytes().length);
			bb.put(string.getBytes());
		}
		return bb.array();
	}
	
	public static Set<String> processUserListResponseMessage(byte[] data) {
		DirMessage mensaje = buildMessageFromReceivedData(data);
		return mensaje.getUserList();
		 
	}
	
	public static byte[] buildServerFilesRequestMessage(int port, String nickname) {
		FileInfo[] datos = NanoFiles.db.getFiles();
		int metaDatos = datos.length * (Integer.BYTES * 2 + Long.BYTES);
		for (FileInfo f : datos) {
			metaDatos = metaDatos + f.getName().length() + f.getHash().length();
		}
		byte[] nombre = nickname.getBytes();
		int longitud = nickname.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES + longitud + Integer.BYTES + Integer.BYTES + metaDatos);
		bb.put(DirMessageOps.OPCODE_SERVE_FILES);
		bb.putInt(longitud);
		bb.put(nombre);
		bb.putInt(port);
		bb.putInt(datos.length);
		for (FileInfo f : datos) {
			bb.putInt(f.getName().length());
			bb.put(f.getName().getBytes());
			bb.putInt(f.getHash().length());
			bb.put(f.getHash().getBytes());
			bb.putLong(f.getSize());
		}
		return bb.array();
	}
	
	public static byte[] buildServerResponseMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_SERVE_FILES_OK);
		return bb.array();
	}
	
	public static boolean processServerFilesResponseMessage(byte[] data) {
		DirMessage mensaje = buildMessageFromReceivedData(data);
		if (mensaje.getOpcode() == DirMessageOps.OPCODE_SERVE_FILES_OK) {
			return true; 
		}else{
			System.err.println("FALLO GRAVE");
			System.exit(-1);
			return false;
		}
	}
	
	public static byte[] buildSearchRequestMessage(String nickname){
		byte[] nombre = nickname.getBytes();
		int longitud = nickname.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES +longitud);
		bb.put(DirMessageOps.OPCODE_LOOKUP_USERNAME);
		bb.putInt(longitud);
		bb.put(nombre);
		return bb.array();
		
	}
	
	public static InetSocketAddress procesSearchResponseMessage(byte[] data) {
		DirMessage mensaje = buildMessageFromReceivedData(data);
		if(DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND == mensaje.getOpcode()) {
			try {
				InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(mensaje.getIp()),Integer.parseInt(mensaje.getPuertoBrowse()));
				return addr;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static byte[] buildSeachFoundMessage(InetSocketAddress nick) {
		String ip = nick.getHostName();
		String puerto = Integer.toString(nick.getPort());
		String ipport = ip + ":" + puerto;
		byte[] buf = ipport.getBytes();
		int longitud = ipport.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES +longitud);
		bb.put(DirMessageOps.OPCODE_LOOKUP_USERNAME_FOUND);
		bb.putInt(longitud);
		bb.put(buf);
		return bb.array();
		
	}
	
	public static byte[] buildSearchNotFoundMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_LOOKUP_USERNAME_NOTFOUND);
		return bb.array();
	}
	
	public static byte[] buildLogOutRequestMessage(String nickname) {
		byte[] nombre = nickname.getBytes();
		int longitud = nickname.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES +longitud);
		bb.put(DirMessageOps.OPCODE_LOGOUT);
		bb.putInt(longitud);
		bb.put(nombre);
		return bb.array();
	}
	
	public static byte[] buildQuitMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_QUIT);
		return bb.array();
	}
	
	
	public static boolean processLogOutResponseMessage(byte[] data) {
		DirMessage mensaje = buildMessageFromReceivedData(data);
		if (mensaje.getOpcode() == DirMessageOps.OPCODE_QUIT) {
			return true; 
		} else {
			return false;
		}
	}
	/*
	 * TODO: Crear métodos processXXXXRequestMessage/processXXXXResponseMessage para
	 * parsear el mensaje recibido y devolver un objeto según el tipo de dato que
	 * contiene, o boolean si es únicamente éxito fracaso.
	 */
	// public static boolean processXXXXXXXResponseMessage(byte[] responseData)
	public static byte[] buildStopServerRequestMessage(String nickname) {
		byte[] nombre = nickname.getBytes();
		int longitud = nickname.length();
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES +longitud);
		bb.put(DirMessageOps.OPCODE_SERVE_STOP);
		bb.putInt(longitud);
		bb.put(nombre);
		return bb.array();
	}
	
	public static byte[] buildStopOk() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_SERVE_STOP_OK);
		return bb.array();
	}
	public static boolean processStopServeResponseMessage(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		byte opcode = buf.get();
		if (opcode == DirMessageOps.OPCODE_SERVE_STOP_OK) {
			return true; // Return number of available file servers
		} else {
			return false;
		}
	}
	
	public static byte[] buildFileListRequestMessage() {
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES);
		bb.put(DirMessageOps.OPCODE_GETFILES);
		return bb.array();
	}
	
	public static byte[] buildFileListResponseMessage(FileInfo[] ficheros) {
		int metaDatos = ficheros.length * (Integer.BYTES * 2 + Long.BYTES);
		for (FileInfo f : ficheros) {
			metaDatos = metaDatos + f.getName().length() + f.getHash().length();
		}
		ByteBuffer bb = ByteBuffer.allocate(DirMessage.OPCODE_SIZE_BYTES + Integer.BYTES + metaDatos);
		bb.put(DirMessageOps.OPCODE_FILELIST);
		bb.putInt(ficheros.length);
		for (FileInfo f : ficheros) {
			bb.putInt(f.getName().length());
			bb.put(f.getName().getBytes());
			bb.putInt(f.getHash().length());
			bb.put(f.getHash().getBytes());
			bb.putLong(f.getSize());
		}
		return bb.array();
	}
	
	
    public static FileInfo[] processFileListResponseMessage(byte[] data) {
    	DirMessage mensaje = buildMessageFromReceivedData(data);
		return mensaje.getFiles();
    }
	
}
