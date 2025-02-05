package es.um.redes.nanoFiles.message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases NFServerComm y NFConnector, y se
 * codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class PeerMessage {
	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */
	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	private String operation;
	private String fileHash;
	private String DatosFichero;
	private List<FileInfo> arrayFicheros;
	private int totalMensaje;
	
	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */
	
	public PeerMessage(String operation) {
		assert(operation == PeerMessageOps.OP_QUERYFILES || operation == PeerMessageOps.OP_FILE_NOT_FOUND  || operation == PeerMessageOps.OP_CLOSE);
		this.operation=operation;
	}
	
	public PeerMessage(String operation, String segundoCampo, int total) {
		assert(operation == PeerMessageOps.OP_FILE );
		this.operation=operation;
		this.totalMensaje=total;
			this.DatosFichero=segundoCampo;
	}
	
	public PeerMessage(String operation, String segundoCampo) {
		assert(operation == PeerMessageOps.OP_DOWNLOAD);
			this.operation=operation;
			this.fileHash=segundoCampo;
		
	}
	
	public PeerMessage(String operation, List<FileInfo> ficheros) {
		assert(operation == PeerMessageOps.OP_SERVEDFILES );
		this.operation = operation;
		this.arrayFicheros=ficheros;
		
	}
	
	public String getOperation() {
		return operation;
	}
	
	public String getHash() {
		return fileHash;
	}
	
	public String getDatos() {
		return DatosFichero;
	}
	
	public int getNumMensaje() {
		return totalMensaje;
		
	}
	
	public List<FileInfo> getFicheros(){
		return Collections.unmodifiableList(arrayFicheros);
	}

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static PeerMessage fromString(String message) {
		
		try {
			BufferedReader reader = new BufferedReader(new StringReader(message));
			String line = reader.readLine();
			LinkedList<String> fields = new LinkedList<String>();
			LinkedList<String> valores = new LinkedList<String>();
			while(!line.equals("")) {
				int idx = line.indexOf(DELIMITER);
				fields.add(line.substring(0, idx).toLowerCase());
				valores.add(line.substring(idx+1).trim());
				line = reader.readLine();
			}
		PeerMessage mensaje = null;
		switch (valores.get(0)) {
		case PeerMessageOps.OP_DOWNLOAD:
			mensaje = new PeerMessage(valores.get(0), valores.get(1));
			break;
		case PeerMessageOps.OP_FILE_NOT_FOUND:
			mensaje = new PeerMessage(valores.get(0));
			break;
		case PeerMessageOps.OP_FILE:
			mensaje = new PeerMessage(valores.get(0), valores.get(1), Integer.parseInt(valores.get(2)));
			break;
		case PeerMessageOps.OP_QUERYFILES:
			mensaje = new PeerMessage(valores.get(0));
			break;
		case PeerMessageOps.OP_SERVEDFILES:
			LinkedList<FileInfo> ficheros = new LinkedList<FileInfo>();
			for(int i=1; i<valores.size()-1; i=i+3) {
				ficheros.add(new FileInfo(valores.get(i+2), valores.get(i), Long.parseLong(valores.get(i+1)), ""));
			}
			mensaje = new PeerMessage(valores.get(0), ficheros);
			break;
		case PeerMessageOps.OP_CLOSE:
			mensaje = new PeerMessage(valores.get(0));
			break;
		default:
			System.err.println();
			System.exit(-1);
		}
		return mensaje;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */
		/*
		 * TODO: En función del tipo del mensaje, llamar a uno u otro constructor con
		 * los argumentos apropiados, para establecer los atributos correpondiente, y
		 * devolver el objeto creado. Se debe detectar que sólo aparezcan los campos
		 * esperados para cada tipo de mensaje.	DataInputStream dis = new DataInputStream(s
		 */
		return null;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toEncodedString() {
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		LinkedList<String> field = new LinkedList<String>();
		LinkedList<String> valores = new LinkedList<String>();
		StringBuffer sb = new StringBuffer();
		switch (operation) {
		case PeerMessageOps.OP_DOWNLOAD:
			field.add("operation");
			valores.add(operation);
			field.add("hash");
			valores.add(fileHash);
			sb.append(field.get(0)+ DELIMITER + valores.get(0)+ END_LINE);
			sb.append(field.get(1)+ DELIMITER + valores.get(1)+ END_LINE);
			sb.append(END_LINE);
			break;
		case PeerMessageOps.OP_FILE:
			field.add("operation");
			valores.add(operation);
			field.add("Datos");
			valores.add(DatosFichero);
			field.add("Secuencia");
			valores.add(String.valueOf(totalMensaje));
			sb.append(field.get(0)+ DELIMITER + valores.get(0)+ END_LINE);
			sb.append(field.get(1)+ DELIMITER + valores.get(1)+ END_LINE);
			sb.append(field.get(2)+ DELIMITER + valores.get(2)+ END_LINE);
			sb.append(END_LINE);
			break;
		case PeerMessageOps.OP_FILE_NOT_FOUND:	
			field.add("operation");
			valores.add(operation);
			sb.append(field.get(0) + DELIMITER + valores.get(0)+ END_LINE);
			sb.append(END_LINE);
			break;
		case PeerMessageOps.OP_QUERYFILES:
			field.add("operation");
			valores.add(operation);
			sb.append(field.get(0) + DELIMITER + valores.get(0)+ END_LINE);
			sb.append(END_LINE);
			break;
		case PeerMessageOps.OP_SERVEDFILES:
			field.add("operation");
			valores.add(operation);
			sb.append(field.get(0) + DELIMITER + valores.get(0)+ END_LINE);
			for (FileInfo ficheros : arrayFicheros) {
				sb.append("Name" + DELIMITER + ficheros.getName() + END_LINE);
				sb.append("Size" + DELIMITER + ficheros.getSize() + END_LINE);
				sb.append("Hash" + DELIMITER + ficheros.getHash() + END_LINE);
			}
			sb.append(END_LINE);
			break;
		case PeerMessageOps.OP_CLOSE:
			field.add("operation");
			valores.add(operation);
			sb.append(field.get(0) + DELIMITER + valores.get(0)+ END_LINE);
			sb.append(END_LINE);
			break;
		default:
			System.err.println("Operacion desconocida");
			System.exit(-1);
		}
		return sb.toString();
	}

}
