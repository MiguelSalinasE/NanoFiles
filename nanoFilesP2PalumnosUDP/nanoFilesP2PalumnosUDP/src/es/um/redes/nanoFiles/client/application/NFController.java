package es.um.redes.nanoFiles.client.application;

import java.io.IOException;

import es.um.redes.nanoFiles.client.comm.NFConnector;
import es.um.redes.nanoFiles.client.shell.NFCommands;
import es.um.redes.nanoFiles.client.shell.NFShell;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFController {
	/**
	 * Diferentes estados del cliente de acuerdo con el autómata
	 */
	private static final byte PRE_LOGIN = 0;
	private static final byte PRE_REGISTRATION = 1;
	private static final byte OFF_BROWSER = 2;
	private static final byte IN_BROWSER = 3;

	/**
	 * Shell para leer comandos de usuario de la entrada estándar
	 */
	private NFShell shell;
	/**
	 * Último comando proporcionado por el usuario
	 */
	private byte currentCommand;

	/**
	 * Objeto controlador encargado de la comunicación con el directorio
	 */
	private NFControllerLogicDir controllerDir;
	/**
	 * Objeto controlador encargado de la comunicación con otros peers (como
	 * servidor o cliente)
	 */
	private NFControllerLogicP2P controllerPeer;

	/**
	 * El estado en que se encuentra este peer (según el autómata)
	 */
	private byte clientStatus;
	/*
	 * Atributos donde se establecen los argumentos pasados a los distintos comandos
	 * del shell
	 */
	private String nickname; // Nick del usuario (register)
	private int serverPort; // Puerto TCP en el que escucha este peer (bgserve/fgserve)
	private String directory; // Nombre/IP del host donde está el directorio (login)
	private String browseUser; // Nickname del peer con el que conectar (browse)
	private String downloadTargetFileHash; // Hash del fichero a descargar (download)
	private String downloadLocalFileName; // Nombre con el que se guardará el fichero descargado

	// Constructor
	public NFController() {
		shell = new NFShell();
		controllerDir = new NFControllerLogicDir();
		controllerPeer = new NFControllerLogicP2P(controllerDir);
		clientStatus = PRE_LOGIN;
	}

	/**
	 * Devuelve el comando actual introducido por el usuario
	 */
	public byte getCurrentCommand() {
		return this.currentCommand;
	}

	/**
	 * Establece el comando actual
	 * 
	 * @param command el comando tecleado en el shell
	 */
	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	/**
	 * Registra en atributos internos los posibles parámetros del comando tecleado
	 * por el usuario.
	 */
	public void setCurrentCommandArguments(String[] args) {
		switch (currentCommand) {
		case NFCommands.COM_LOGIN:
			directory = args[0];
			break;
		case NFCommands.COM_USERLIST:
			break;
		case NFCommands.COM_REGISTER:
			nickname = args[0];
			break;
		case NFCommands.COM_BROWSE:
			browseUser = args[0];
			break;
		case NFCommands.COM_FGSERVE:
		case NFCommands.COM_BGSERVE:
			serverPort = Integer.parseInt(args[0]);
			break;
		case NFCommands.COM_DOWNLOAD:
			downloadTargetFileHash = args[0];
			downloadLocalFileName = args[1];
			break;
		default:
		}
	}

	/**
	 * Método para leer un comando general (fuera del browser)
	 */
	public void readGeneralCommandFromShell() {
		// Pedimos el comando al shell
		shell.readGeneralCommand();
		// Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		// Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	/**
	 * Procesa los comandos introducidos por un usuario que aún no está dentro del
	 * browser. Debe comprobar mediante "clientStatus" si el comando es válido según
	 * el estado actual del autómata (no todos los comandos son válidos en cualquier
	 * estado) y actualizar "clientStatus" cuando se produzca un cambio a otro
	 * estado
	 * @throws IOException 
	 */
	public void processCommand() throws IOException {
		boolean result;
		switch (currentCommand) {
		case NFCommands.COM_LOGIN:
			// Se loguea en el directorio para comprobar que está disponible
			if (clientStatus == PRE_LOGIN) {
				result = controllerDir.logIntoDirectory(directory);
				if (result) {
					clientStatus = PRE_REGISTRATION;
				}
			} else {
				System.out.println("* Ya estas logeado en el directorio");
			}
			break;
		case NFCommands.COM_MYFILES:
			showMyLocalFiles(); // Muestra los ficheros en el directorio local compartido
			break;
		case NFCommands.COM_USERLIST:
			if(clientStatus != PRE_LOGIN){
				result = controllerDir.getUserListFromDirectory();
				if(!result){
					System.out.println("Ha ocurrido un error ");
				}
			}else {
				System.out.println("Debes loggearte antes de mostrar los usuarios, porfavor logeate");
			}
			break;
		case NFCommands.COM_FILELIST:
			/*
			 * TODO: Pedir la lista de ficheros en el directorio (a través del
			 * controllerDir) Obtiene los ficheros que otros peers están sirviendo (comandos
			 * fgserve/bgserve)
			 */
			if(clientStatus == OFF_BROWSER) {
				controllerDir.getFileListFromDirectory();
			}else {
				System.out.println("No pueden mostrarse los archivos, por favor registrese");
			}
			
			break;
		case NFCommands.COM_REGISTER:
			/*
			 * TODO: Registrar un nombre de usuario en el directorio (a través del
			 * controllerDir) Este comando sólo se debe poder procesar una vez se ha hecho
			 * el login Comprobar la respuesta para ver si se ha registrado correctamente En
			 * caso de éxito, podemos pasar a otro estado del autómata que permite nuevos
			 * comandos
			 */
			if (clientStatus == PRE_REGISTRATION) {
				result = controllerDir.registerNickInDirectory(nickname);
				System.out.println("Te has registrado correctamente");
				if (result) {
					clientStatus = OFF_BROWSER;
				}else {
					System.out.println("FAIL");
				}
			}else {
				System.out.println("Ya estas registrado, pruebe otro nombre");
			}
			break;
		case NFCommands.COM_BROWSE:
			if (clientStatus == OFF_BROWSER) {
				enterBrowser();
			} else {
				System.out.println("* Debes registrarte antes de poder hacer 'browse' de un usuario");
			}
			break;
		case NFCommands.COM_FGSERVE:
			/*
			 * TODO: Lanzar un servidor en primer plano (a través del controllerPeer)
			 * serverPort: el puerto en el que escuchar nickname: nick a usar para publicar
			 * ficheros
			 */
			if(clientStatus == OFF_BROWSER) {
				controllerPeer.foregroundServeFiles(serverPort, nickname);
			}else {
				System.out.println("Debes registrarte antes para poder servir ficheros");
			}
			break;
		case NFCommands.COM_BGSERVE:
			/*
			 * TODO: Lanzar un servidor en segundo plano (a través del controllerPeer)
			 * serverPort: el puerto en el que escuchar nickname: nick a usar para publicar
			 * ficheros
			 */
			break;
		case NFCommands.COM_QUIT:
			/*
			 * TODO: Dar de baja el nick, cerrar sockets, etc. (a través del controllerDir)
			 */
			result=true;
			if(clientStatus == OFF_BROWSER) {
				result = controllerDir.logout(nickname);
			}
			if(result) {
				System.out.println("Cerrando programa");
				System.exit(-1);
			}else {
				System.out.println("No se ha podido cerrar");
			}

			
			break;
		default:
		}
	}

	private void showMyLocalFiles() {
		System.out.println("Lista de ficheros en la carpeta local:");
		FileInfo.printToSysout(NanoFiles.db.getFiles());
	}

	/**
	 * Método para leer un comando en el browser
	 */
	private void readBrowserCommandFromShell() {
		// Pedimos un nuevo comando de browser al shell (es posible pasar el objeto
		// NFConnector para avisar de si nos llega un mensaje asíncrono del servidor)
		shell.readBrowserCommand(null);
		// Establecemos el comando tecleado (o el mensaje recibido) como comando actual
		setCurrentCommand(shell.getCommand());
		// Procesamos los posibles parámetros (si los hubiera)
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	/**
	 * Método para procesar los comandos específicos del browser
	 */
	private void enterBrowser() {
		boolean result = controllerPeer.browserEnter(browseUser);
		if (result) {
			System.out.println("* Buscando archivos de " + browseUser);
			clientStatus = IN_BROWSER;
			do {
				readBrowserCommandFromShell();
				processBrowserCommand();
			} while (currentCommand != NFCommands.COM_CLOSE);
			System.out.println("* Estas fuera del buscador de ficheros");
		} else {
			System.out.println("* No puedes buscar archivos de este usuario" + browseUser);
		}
		// Cambiamos el estado del autómata para aceptar nuevos comandos
		clientStatus = OFF_BROWSER;
	}

	/**
	 * Método para procesar los comandos específicos del browser
	 */
	private void processBrowserCommand() {
		switch (currentCommand) {
		// TODO: Procesar resto del comandos del modo browser
		case NFCommands.COM_DOWNLOAD:
			controllerPeer.browserDownloadFile(downloadTargetFileHash, downloadLocalFileName);
			break;
		case NFCommands.COM_QUERYFILES:
			controllerPeer.browserQueryFiles();
			break;
		case NFCommands.COM_CLOSE:
			controllerPeer.browserClose();
			break;
		default:
		}
	}

	/**
	 * Método que comprueba si el usuario ha introducido el comando para salir de la
	 * aplicación
	 */
	public boolean shouldQuit() {
		return currentCommand == NFCommands.COM_QUIT;
	}

}
