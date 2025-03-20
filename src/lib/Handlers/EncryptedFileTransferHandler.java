package lib.Handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;


import lib.Interfaces.FileTransferHandler;
import lib.Security.CrossAppEncryption;
import lib.Utils.Logger;

/**
 * Decorador que añade encriptación a la transferencia de archivos
 */
public class EncryptedFileTransferHandler implements FileTransferHandler {

    private final FileTransferHandler baseHandler;
    private final String encryptionPassword;
    private static final String TEMP_DIR = "temp_encrypted";

    public EncryptedFileTransferHandler(FileTransferHandler baseHandler, String encryptionPassword) {
        this.baseHandler = baseHandler;
        this.encryptionPassword = encryptionPassword;

        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

    }

    @Override
    public boolean uploadFile(File localFile, String remotePath) throws IOException {
        /**
         * Desde la factoria, el baseHandler se le da a este Encrypted File Transfer
         * Handler, y usa
         * el upload de su propio "baseHandler" para subir el archivo encriptado, del
         * Standard.
         */

        try {

            File encryptedFile = new File(TEMP_DIR, localFile.getName() + ".enc");

            CrossAppEncryption.encryptFile(localFile, encryptedFile, encryptionPassword);
            Logger.info("Encrypted file for upload: " + localFile.getName());

            /**
             * El remota path contiene tambien el nombre del archivo, por lo que el archivo
             * encriptado
             * se sube con el nombre del archivo original.
             */
            boolean success = baseHandler.uploadFile(encryptedFile, remotePath);

            Files.deleteIfExists(encryptedFile.toPath());

            return success;
        } catch (Exception e) {
            Logger.error("Error uploading encrypted file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean replaceFile(File localFile, String remotePath) throws IOException {
        try {

            File encryptedFile = new File(TEMP_DIR, localFile.getName() + ".enc");

            try {

                CrossAppEncryption.encryptFile(localFile, encryptedFile, encryptionPassword);
                Logger.info("Encrypted file for replace operation: " + localFile.getName());

                boolean success = baseHandler.replaceFile(encryptedFile, remotePath);

                return success;
            } catch (Exception e) {
                Logger.error("Error encrypting file for replacement: " + e.getMessage());
                return false;
            } finally {

                Files.deleteIfExists(encryptedFile.toPath());
            }
        } catch (Exception e) {
            Logger.error("Error in replaceFile operation: " + e.getMessage());
            return false;
        }
    }

        @Override
    public List<String> listFiles(String remotePath) throws IOException {

        return baseHandler.listFiles(remotePath);
        
    }

    @Override
    public boolean downloadFile(String remotePath, File localFile) throws IOException {
        return baseHandler.downloadFile(remotePath, localFile);
    }

    @Override
    public boolean deleteFile(String remotePath) throws IOException {
        return baseHandler.deleteFile(remotePath);
    }

    @Override
    public String getHandlerName() {
        return "EncryptedTransfer(" + baseHandler.getHandlerName() + ")";
    }

    @Override
    public HashMap<LocalDateTime, File> checkVersions(String fileName) throws IOException {
        return baseHandler.checkVersions(fileName);
    }


    
}