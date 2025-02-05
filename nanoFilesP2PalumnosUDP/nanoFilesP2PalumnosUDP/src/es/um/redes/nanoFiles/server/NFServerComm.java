package es.um.redes.nanoFiles.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import es.um.redes.nanoFiles.client.application.NanoFiles;
import es.um.redes.nanoFiles.message.PeerMessage;
import es.um.redes.nanoFiles.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	private static DataInputStream dis;
	private static DataOutputStream dos;
	private static final double utfLimit =32000.0; 

	public static void serveFilesToClient(Socket socket) {
		boolean clientConnected = true;
		// Bucle para atender mensajes del cliente
		try {
			/*
			 * TODO: Crear dis/dos a partir del socket
			 */
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());

			while (clientConnected) { // Bucle principal del servidor
				// TODO: Leer un mensaje de socket y convertirlo a un objeto PeerMessage
				String dataFromClient = dis.readUTF();
				PeerMessage mensaje = PeerMessage.fromString(dataFromClient);
				/*
				 * TODO: Actuar en función del tipo de mensaje recibido. Se pueden crear métodos
				 * en esta clase, cada uno encargado de procesar/responder un tipo de petición.
				 */

				switch (mensaje.getOperation()) {
				case PeerMessageOps.OP_DOWNLOAD:
					processDownloadRequest(mensaje.getHash());
					break;
				case PeerMessageOps.OP_QUERYFILES:
					processQueryFilesRequest();
					break;
				case PeerMessageOps.OP_CLOSE:
					clientConnected=false;
					break;
				default:
					break;
				}
			}
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void processDownloadRequest(String fileHash) {
		try {
			String path = NanoFiles.db.lookupFilePath(fileHash);
			File f = new File(path);
			FileInputStream fis = new FileInputStream(f);
			long longitud = f.length();
			byte data[] = new byte[(int) longitud];
			long datosEnviado=0;
			fis.read(data);
			fis.close();
			int numMen = (int) (data.length / utfLimit + 1);
			PeerMessage mensaje;
			for (int i = 0; i < numMen; i++) {
				byte buf[] = new byte[(int) utfLimit];
				if (numMen != 1) {
					if(i == numMen-1) {
						if(data.length-datosEnviado>0) {
							buf = Arrays.copyOfRange(data, (int)datosEnviado, data.length);
						}
						
					}else {
						buf = Arrays.copyOfRange(data, i * (int)utfLimit, i * (int)utfLimit + (int)utfLimit);
						datosEnviado=datosEnviado + (long)utfLimit;
					}
				} else {
					buf = Arrays.copyOfRange(data, i * (int)utfLimit, data.length);
				}
				String encoded = java.util.Base64.getEncoder().encodeToString(buf);
				mensaje = new PeerMessage(PeerMessageOps.OP_FILE, encoded, numMen-i-1);
				
				String respuesta = mensaje.toEncodedString();
				dos.writeUTF(respuesta);
			}
		} catch (NullPointerException fi) {
			try {
				PeerMessage mensaje = new PeerMessage(PeerMessageOps.OP_FILE_NOT_FOUND);
				String respuesta = mensaje.toEncodedString();
				dos.writeUTF(respuesta);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}

	private static void processQueryFilesRequest() {
		FileInfo[] ficheros = NanoFiles.db.getFiles();
		LinkedList<FileInfo> listaFicheros = new LinkedList<FileInfo>();
		for (FileInfo f : ficheros) {
			listaFicheros.add(f);
		}
		PeerMessage respuestaObjeto = new PeerMessage(PeerMessageOps.OP_SERVEDFILES, listaFicheros);
		
		String respuesta = respuestaObjeto.toEncodedString();
		try {
			dos.writeUTF(respuesta);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

}
