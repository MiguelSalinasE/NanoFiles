package es.um.redes.nanoFiles.directory.server;

import java.net.SocketException;

public class Directory {
	public static final int DIRECTORY_PORT = 6868;
	public static final double DEFAULT_CORRUPTION_PROBABILITY = 0.0;

	public static void main(String[] args) {
		double datagramCorruptionProbability = DEFAULT_CORRUPTION_PROBABILITY;

		/**
		 * Command line argument to directory is optional, if not specified, default
		 * value is used: -loss: probability of corruption of received datagrams
		 */
		String arg;

		// Analizamos si hay parámetro
		if (args.length > 0 && args[0].startsWith("-")) {
			arg = args[0];
			// Examinamos si es un parámetro válido
			if (arg.equals("-loss")) {
				if (args.length == 2) {
					try {
						// El segundo argumento contiene la probabilidad de descarte
						datagramCorruptionProbability = Double.parseDouble(args[1]);
					} catch (NumberFormatException e) {
						System.err.println("Valor erróneo en la opción " + arg);
						return;
					}
				} else
					System.err.println("Opción" + arg + " requiere de un valor");
			} else {
				System.err.println("Opción ilegal " + arg);
			}
		}
		System.out.println("Probabilidad de que los datagramas recibidos estén corruptos: " + datagramCorruptionProbability);
		DirectoryThread dt;
		try {
			dt = new DirectoryThread(DIRECTORY_PORT, datagramCorruptionProbability);
			dt.start();
		} catch (SocketException e) {
			System.err.println("El directorio no puede crear un socket UDP en el puerto " + DIRECTORY_PORT);
			System.err.println("Lo más probable es que un proceso directorio este siendo ejecutado o escuchando en ese puerto...");
			System.exit(-1);
		}
	}
}
