package lib;

import java.io.File;

import lib.Enum.LocalEvent;

import lib.Factory.FileTransferFactory;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.net.ftp.FTPClient;

import lib.Utils.Logger;
import lib.Interfaces.FileTransferHandler;

public class Obrero implements Runnable {

    private File file;
    private String fileName;
    private LocalEvent event;
    private FileTransferHandler transferHandler;

    public Obrero(File file, LocalEvent event) {
        this.file = file;
        this.fileName = file.getName();
        this.event = event;
        this.transferHandler = FileTransferFactory.getHandler();
    }

    @Override
    public void run() {
        Logger.info("Obrero starting work on file: " + fileName + " for event: " + event);

        FTPClient ftpClient = null;
        try {
            switch (event) {
                case CREATE:
                    Logger.info("Starting FTP upload for: " + fileName);
                    uploadFile();
                    break;
                case MODIFY:
                    Logger.info("Starting FTP replace for " + fileName);
                    replaceFile();
                    break;
                case DELETE:
                    Logger.info("Starting FTP delete for: " + fileName);
                    deleteRemoteFile();
                    break;
                default:
                    Logger.warning("Unsupported event: " + event);
                    break;
            }
        } catch (Exception e) {
            Logger.error("Error processing " + event + " for " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Sube un archivo al servidor FTP
     */
    private void uploadFile() throws IOException {
        File localFile = Paths.get("syncro", fileName).toFile();
        String remotePath = fileName;

        if (!localFile.exists()) {
            throw new IOException("Local file not found: " + localFile.getAbsolutePath());
        }

        transferHandler.uploadFile(localFile, remotePath);
    }

    private void replaceFile() {

        File localFile = Paths.get("syncro", fileName).toFile();
        String remotePath = fileName;
        
        if (!localFile.exists()) {
            Logger.error("Local file not found for replacing: " + localFile.getAbsolutePath());
            return;
        }
        
        try {
            transferHandler.replaceFile(localFile, remotePath);
        } catch (IOException e) {
            Logger.error("Error replacing file: " + e.getMessage());
        }
        
    }

    /**
     * Elimina un archivo en el servidor FTP
     */
    private void deleteRemoteFile() throws IOException {
        String remotePath = fileName;
        transferHandler.deleteFile(remotePath);
    }

}