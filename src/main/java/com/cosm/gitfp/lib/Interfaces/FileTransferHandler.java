package com.cosm.gitfp.lib.Interfaces;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Interfaz que define las operaciones básicas de transferencia de archivos
 */
public interface FileTransferHandler {


    /**
     * Sube un archivo al servidor remoto
     */
    boolean uploadFile(File localFile, String remotePath) throws IOException;
    
    /**
     * Elimina un archivo del servidor remoto
     */
    boolean deleteFile(String remotePath) throws IOException;


    /**
     * Reemplaza un archivo en el servidor remoto
     */
    boolean replaceFile(File localFile, String remotePath) throws IOException;


    /**
     * Lista los archivos en un directorio remoto
    */
    List<String> listFiles(String remotePath) throws IOException;

    /**
     * Descarga un archivo del servidor remoto
     */
    boolean downloadFile(String remotePath, File localFile) throws IOException;

    HashMap<LocalDateTime, File> checkVersions(String fileName) throws IOException;
    
    /**
     * Obtiene el nombre del manejador para logging
     */
    String getHandlerName();
}