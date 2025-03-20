package lib.Connection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import lib.Configurador;
import lib.Factory.FileTransferFactory;
import lib.Interfaces.FileTransferHandler;
import lib.Security.CrossAppEncryption;
import lib.Security.EncryptionCredentials;
import lib.Utils.Logger;

public class FileOperationService {
    private FileTransferHandler transferHandler;

    public FileOperationService() {
        this.transferHandler = FileTransferFactory.getHandler();
    }

    public boolean initDownloadFolder(String localFolderPath) {
        File localFolder = Paths.get(localFolderPath).toFile();
        if (!localFolder.exists()) {
            Logger.info("Creating download folder: " + localFolder.getAbsolutePath());
            return localFolder.mkdirs();
        }
        return true;
    }

    public HashMap<LocalDateTime, File> checkVersions(String fileName) {
        try {
            return transferHandler.checkVersions(fileName);
        } catch (IOException e) {
            Logger.error("Error checking versions: " + e.getMessage());
            return null;
        }
    }

    public boolean downloadFile(String remoteFileName) {
        try {
            initDownloadFolder("downloads");
            
            String fileName = new File(remoteFileName).getName();
            File encryptedFile = new File("downloads", "temp_" + fileName);
            File finalFile = new File("downloads", fileName);

            boolean result = transferHandler.downloadFile(remoteFileName, encryptedFile);

            if (!result) {
                Logger.error("Failed to download file: " + remoteFileName);
                return false;
            }

            if (Configurador.isEncryptionEnabled()) {
                Logger.info("Decrypting file: " + encryptedFile.getAbsolutePath());
                try {
                    // Desencriptar a archivo final
                    CrossAppEncryption.decryptFile(encryptedFile, finalFile, EncryptionCredentials.PASSWORD);
                    // Eliminar archivo temporal encriptado
                    encryptedFile.delete();
                    return true;
                } catch (IllegalArgumentException e) {
                    // El archivo no está encriptado, lo renombro
                    Logger.info("File is not encrypted, using as is: " + remoteFileName);
                    encryptedFile.renameTo(finalFile);
                    return true;
                } catch (Exception e) {
                    Logger.error("Error decrypting file: " + e.getMessage());
                    return false;
                }
            } else {
                encryptedFile.renameTo(finalFile);
                Logger.info("File downloaded successfully: " + remoteFileName);
                return true;
            }
        } catch (IOException e) {
            System.out.println(e);
            Logger.error("Error downloading file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Borra un archivo del servidor FTP
     * 
     * @param remoteFileName Nombre del archivo a borrar
     * @return true si la operación fue exitosa
     */
    public boolean deleteRemoteFile(String remoteFileName) {
        try {
            boolean result = transferHandler.deleteFile(remoteFileName);
            if (result) {
                Logger.info("File deleted successfully: " + remoteFileName);
            } else {
                Logger.error("Failed to delete file: " + remoteFileName);
            }
            return result;
        } catch (IOException e) {
            Logger.error("Error deleting file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lista los archivos disponibles en el servidor FTP
     * 
     * @param remotePath Ruta en el servidor (usar "" para directorio raíz)
     * @return Lista de nombres de archivos
     */
    public List<String> listRemoteFiles(String remotePath) {
        
        try {
            return transferHandler.listFiles(remotePath);
        } catch (IOException e) {
            Logger.error("Error listing files: " + e.getMessage());
            return null;
        }
    }
}