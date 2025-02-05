# nanoFile

# Aplicación para la transmisión de ficheros entre diferentes usuarios
 Se trata de una aplicación de escritorio para Linux, desarrollada en Java, que permite la comunicación Cliente-Servidor con un Directorio en Internet, así como la transmisión de archivos entre pares (Peer-to-Peer) utilizando dicho Directorio como punto de referencia.

Para la comunicación con el Directorio, se emplea el protocolo UDP, que es sin conexión y no confiable, mientras que para la comunicación directa entre pares y la transferencia de archivos se utiliza el protocolo TCP, que es orientado a conexión y confiable.

Entre las funcionalidades principales de la aplicación se incluyen la conexión y registro en el servidor, la posibilidad de compartir archivos con otros usuarios, la conexión con otros usuarios y la descarga de los archivos que ofrecen.

Consulte el fichero Plantilla_Protocolos_Bueno.docx en caso de dudas sobre como ha sido desarrollado el proyecto.

# Manual de usuario
Utiliza los dos jars, contenidos en la carpeta llamada Jars, contienen dos ejecutables, Directory y NanoFiles. Directory se debe ejecutar en el ordenador que se desee que actúe como servidor remoto. NanoFiles es la aplicación cliente. Los ficheros que se el cliente desee compartir deben ser colocados en la carpeta nf-shared. En esa misma carpeta recibirá aquellos ficheros que descargue.

Fecha de desarrollo: Febrero - Mayo 2022

Desarrollado como proyecto de prácticas de la asignatura REDES DE COMUNICACIONES en la Facultad de Informática de la Universidad de Murcia
