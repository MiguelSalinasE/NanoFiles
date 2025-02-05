package es.um.redes.nanoFiles.client.application;

import java.io.IOException;
import java.util.*;
import java.net.InetSocketAddress;

import es.um.redes.nanoFiles.directory.connector.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {
	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Método para conectar con el directorio y obtener el número de peers que están
	 * sirviendo ficheros
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 * @return true si se ha conseguido contactar con el directorio.
	 * @throws IOException
	 */
	boolean logIntoDirectory(String directoryHostname) throws IOException {
		/*
		 * TODO: Debe crear un objeto DirectoryConnector a partir del parámetro
		 * directoryHostname y guardarlo en el atributo correspondiente. A continuación,
		 * utilizarlo para comunicarse con el directorio y realizar tratar de realizar
		 * el "login", informar por pantalla del éxito/fracaso y devolver dicho valor
		 */
	
		boolean result;
		directoryConnector = new DirectoryConnector(directoryHostname);
		int n = directoryConnector.logIntoDirectory();
		if (n < 0) {
			System.out.println("Ha habido un error al loggearte, inténtalo de nuevo");
			result = false;
		} else {
			System.out.println("Te has loggeado correctamente");
			System.out.println("El numero de servidores disponibles es:" + n);
			result = true;
		}
		return result;
	}

	/**
	 * Método para registrar el nick del usuario en el directorio
	 * 
	 * @param nickname el nombre de usuario a registrar
	 * @return true si el nick es válido (no contiene ":") y se ha registrado
	 *         nickname correctamente con el directorio (no estaba duplicado), falso
	 *         en caso contrario.
	 * @throws IOException
	 */
	boolean registerNickInDirectory(String nickname) throws IOException {
		/*
		 * TODO: Registrar un nick. Comunicarse con el directorio (a través del
		 * directoryConnector) para solicitar registrar un nick. Debe informar por
		 * pantalla si el registro fue exitoso o fallido, y devolver dicho valor
		 * booleano. Se debe comprobar antes que el nick no contiene el carácter ':'.
		 */
		boolean flag = false;
		if(!nickname.contains(":")) {
			flag = directoryConnector.registerNickname(nickname);
		}
		
		return flag;
	}

	/**
	 * Método para obtener de entre los peer servidores registrados en el directorio
	 * la IP:puerto del peer con el nick especificado
	 * 
	 * @param nickname el nick del peer por cuya IP:puerto se pregunta
	 * @return La dirección de socket del peer identificado por dich nick, o null si
	 *         no se encuentra ningún peer con ese nick.
	 */
	InetSocketAddress lookupUserInDirectory(String nickname) {
		/*
		 * TODO: Obtener IP:puerto asociada a nickname. Comunicarse con el directorio (a
		 * través del directoryConnector) para preguntar la dirección de socket en la
		 * que el peer con 'nickname' está sirviendo ficheros. Si no se obtiene una
		 * respuesta con IP:puerto válidos, se debe devolver null.
		 */
		InetSocketAddress peerAddr = null;
		try {
			peerAddr = directoryConnector.searchUser(nickname);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return peerAddr;
		
	}

	/**
	 * Método para publicar la lista de ficheros que este peer está compartiendo.
	 *
	 * @param port     El puerto en el que este peer escucha solicitudes de conexión
	 *                 de otros peers.
	 * @param nickname El nick de este peer, que será asociado a lista de ficheros y
	 *                 su IP:port
	 */
	boolean publishLocalFilesToDirectory(int port, String nickname) {
		/*
		 * TODO: Enviar la lista de ficheros servidos. Comunicarse con el directorio (a
		 * través del directoryConnector) para enviar la lista de ficheros servidos por
		 * este peer con nick 'nickname' en el puerto 'port'. Los ficheros de la carpeta
		 * local compartida están disponibles en NanoFiles.db).
		 */
		boolean flag = false;
		try {
			flag= directoryConnector.serverFiles(port, nickname);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Fallo de comunicacion");
			System.exit(-1);
		}
		if(!flag){
			System.out.println("Fallo al intentar compartir ficheros");
		}
		
		return flag;
	}
	/**
	 * Método para obtener y mostrar la lista de nicks registrados en el directorio
	 */
	boolean getUserListFromDirectory() {
		/*
		 * TODO: Obtener la lista de usuarios registrados. Comunicarse con el directorio
		 * (a través del directoryConnector) para obtener la lista de nicks registrados
		 * e imprimirla por pantalla.
		 */
		boolean flag=false;
		Set<String> listaNicks;
		try {
			listaNicks = directoryConnector.getUserList();
			if(listaNicks.size()==0){
				System.out.println("La lista de usuarios está vacio");
			}else {
				System.out.println("Lista de usuarios: ");
				for (String nick : listaNicks) {
					System.out.println(nick);
				}
			}
			flag = true;
			
		} catch (IOException e) {
			e.printStackTrace();
			flag = false;
		}
		
		return flag;
	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	void getFileListFromDirectory() {
		/*
		 * TODO: Obtener la lista de ficheros servidos. Comunicarse con el directorio (a
		 * través del directoryConnector) para obtener la lista de ficheros e imprimirla
		 * por pantalla.
		 */
		FileInfo[] listaFicheros;
		try {
			listaFicheros = directoryConnector.getFiles();
			if(listaFicheros.length==0) {
				System.out.println("No hay ficheros disponibles");
			}else {
				System.out.println("Ficheros: ");
				FileInfo.printToSysout(listaFicheros);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Método para desconectarse del directorio (cerrar sesión)
	 */
	public boolean logout(String nickname) {
		/*
		 * TODO: Dar de baja el nickname. Al salir del programa, se debe dar de baja el
		 * nick registrado con el directorio y cerrar el socket usado por el
		 * directoryConnector.
		 */
		boolean logOut=true;
		try {
			
			logOut = directoryConnector.logOutDirectory(nickname);
			return logOut;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logOut;
	}
	
	public boolean stopServing(String nickname) {
		/*
		 * Damos de baja el nickname de la lista de servidores a través del objeto directory conector,
		 * y el cliente dejaria de ser un servidor
		 */
		boolean flag=false;
		try {
			flag= directoryConnector.stopServer(nickname);
			return flag;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
