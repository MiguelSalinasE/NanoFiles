package es.um.redes.nanoFiles.client.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import es.um.redes.nanoFiles.client.comm.NFConnector;
import es.um.redes.nanoFiles.server.NFServer;
import es.um.redes.nanoFiles.server.NFServerSimple;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/**
	 * El servidor de ficheros de este peer
	 */
	private NFServer bgFileServer = null;
	/**
	 * El cliente para conectarse a otros peers
	 */
	NFConnector nfConnector;
	/**
	 * El controlador que permite interactuar con el directorio
	 */
	private NFControllerLogicDir controllerDir;

	protected NFControllerLogicP2P() {
	}

	protected NFControllerLogicP2P(NFControllerLogicDir controller) {
		// Referencia al controlador que gestiona la comunicación con el directorio
		controllerDir = controller;
	}

	/**
	 * Método para ejecutar un servidor de ficheros en primer plano. Debe arrancar
	 * el servidor y darse de alta en el directorio para publicar el puerto en el
	 * que escucha.
	 * 
	 * 
	 * @param port     El puerto en que el servidor creado escuchará conexiones de
	 *                 otros peers
	 * @param nickname El nick de este peer, parar publicar los ficheros al
	 *                 directorio
	 */
	protected void foregroundServeFiles(int port, String nickname) {
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		try {
			// TODO: Crear objeto servidor NFServerSimple ligado al puerto especificado
			NFServerSimple servidor = new NFServerSimple(port);
			// TODO: Publicar ficheros compartidos al directorio
			controllerDir.publishLocalFilesToDirectory(port, nickname);
			// TODO: Ejecutar servidor en primer plano
			servidor.run();
			
			controllerDir.stopServing(nickname);
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor y darse de alta en el directorio para publicar el puerto en el
	 * que escucha.
	 * 
	 * @param port     El puerto en que el servidor creado escuchará conexiones de
	 *                 otros peers
	 * @param nickname El nick de este peer, parar publicar los ficheros al
	 *                 directorio
	 */
	protected void backgroundServeFiles(int port, String nickname) {
		/*
		 * TODO: Las excepciones que puedan lanzarse deben ser capturadas y tratadas en
		 * este método. Si se produce una excepción de entrada/salida (error del que no
		 * es posible recuperarse), se debe informar sin abortar el programa
		 */
		// TODO: Comprobar que no existe ya un objeto NFServer previamente creado, en
		// cuyo caso el servidor ya está en marcha

		// TODO: Crear objeto servidor NFServer ligado al puerto especificado

		// TODO: Arrancar un hilo servidor en segundo plano

		// TODO: Publicar ficheros compartidos al directorio

		// TODO: Imprimir mensaje informando de que el servidor está en marcha
	}

	/**
	 * Método para establecer una conexión con un peer servidor de ficheros
	 * 
	 * @param nickname El nick del servidor al que conectarse (o su IP:puerto)
	 * @return true si se ha podido establecer la conexión
	 */
	protected boolean browserEnter(String nickname) {
		boolean connected = false;
		/*
		 * TODO: Averiguar si el nickname es en realidad una cadena con IP:puerto, en
		 * cuyo caso no es necesario comunicarse con el directorio.
		 */
		try {
		InetSocketAddress addr;
		String IP="";
		String PUERTO="";
		if(nickname.contains(":")) {
			String[] IPPORT = nickname.split(":");
			IP = IPPORT[0];
			PUERTO= IPPORT[1];
			addr = new InetSocketAddress(InetAddress.getByName(IP),Integer.parseInt(PUERTO));
		}else {
			addr = controllerDir.lookupUserInDirectory(nickname);
			
		}
		
		/*
		 * TODO: Si es un nickname, preguntar al directorio la IP:puerto asociada a
		 * dicho peer servidor.
		 */
		/*
		 * TODO: Comprobar si la respuesta del directorio contiene una IP y puerto
		 * válidos (el peer servidor al que nos queremos conectar ha comunicado
		 * previamente al directorio el puerto en el que escucha). En caso contrario,
		 * informar y devolver falso.
		 */
		/*
		 * TODO: Crear un objeto NFConnector para establecer la conexión con el peer
		 * servidor de ficheros. Si la conexión se establece con éxito, informar y
		 * devolver verdadero.
		 */
		
			
			nfConnector = new NFConnector(addr);
			connected=true;
		} catch (UnknownHostException e) {
			System.err.println("No reconoce IP");
		}catch (IOException e) {
			e.printStackTrace();
		}
		return connected;
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros al que nos
	 * hemos conectador mediante browser Enter
	 * 
	 * @param targetFileHash El hash del fichero a descargar
	 * @param localFileName  El nombre con el que se guardará el fichero descargado
	 */
	protected void browserDownloadFile(String targetFileHash, String localFileName) {
		
		
		File archivo = new File(localFileName);
		boolean descargado = false;
		if(!archivo.exists()) {
			try {
				descargado = nfConnector.download(targetFileHash, archivo);
				if(!descargado) {
					archivo.delete();
					System.out.println("No se ha podido descargar");
				}else {
					System.out.println("Descarga completada");
				}
			} catch (IOException e) {
				archivo.delete();
				e.printStackTrace();
				System.err.println("Fallo al descargar el fichero");
			}   
		}else {
			System.out.println("El archivo ya existe");
			archivo.delete();
		}
		
		
		/*
		 * TODO: Usar el NFConnector creado por browserEnter para descargar el fichero
		 * mediante el método "download". Se debe omprobar si ya existe un fichero con
		 * el mismo nombre en esta máquina, en cuyo caso se informa y no se realiza la
		 * descarga
		 */

	}

	protected void browserClose() {
		/*
		 * TODO: Cerrar el explorador de ficheros remoto (informar al servidor de que se
		 * va a desconectar)
		 */
		try {
			nfConnector.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	protected void browserQueryFiles() {
		/*
		 * TODO: Crear un objeto NFConnector y guardarlo el atributo correspondiente
		 * para ser usado por otros métodos de esta clase mientras se está en una sesión
		 * del explorador de ficheros remoto.
		 * 
		 */
		try {
			List<FileInfo> listaFicheros = nfConnector.searchFilesQuery();
			FileInfo[] ficheros = new FileInfo[listaFicheros.size()];
			int i=0;
			for (FileInfo fileInfo : listaFicheros) {
				ficheros[i] = fileInfo;
				i++;
			}
			FileInfo.printToSysout(ficheros);
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
	}

