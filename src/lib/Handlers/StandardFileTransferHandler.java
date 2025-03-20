package lib.Handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import lib.Connection.FTPConnectionPool;
import lib.Factory.FileTransferFactory;
import lib.Interfaces.FileTransferHandler;
import lib.Utils.Logger;

/**
 * Implementación estándar para transferir archivos sin encriptación
 */
public class StandardFileTransferHandler implements FileTransferHandler {

    private static final String DIR_HISTORY = FileTransferFactory.getDIR_HISTORY();
    private static final String WORK_DIR = FileTransferFactory.getWORK_DIR();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public boolean uploadFile(File localFile, String remotePath) throws IOException {

        FTPClient ftpClient = null;

        try {
            try {
                ftpClient = FTPConnectionPool.getInstance().getConnection(60);

                ftpClient.changeWorkingDirectory(WORK_DIR);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interrupted while waiting for FTP connection");
                return false;
            }

            boolean success;
            try (FileInputStream fis = new FileInputStream(localFile)) {
                success = ftpClient.storeFile(remotePath, fis);
                if (success) {
                    Logger.info("Successfully uploaded: " + localFile.getName());
                } else {
                    Logger.error("Failed to upload: " + localFile.getName());
                }
            }

            return success;
        } finally {
            if (ftpClient != null) {

                FTPConnectionPool.getInstance().releaseConnection(ftpClient);
            }
        }
    }

    @Override
    public boolean deleteFile(String remotePath) throws IOException {
        FTPClient ftpClient = null;
        try {
            try {
                ftpClient = FTPConnectionPool.getInstance().getConnection(60);
                ftpClient.changeWorkingDirectory(WORK_DIR);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for FTP connection", e);
            }

            boolean success = ftpClient.deleteFile(remotePath);
            if (success) {
                Logger.info("Successfully deleted remote file: " + remotePath);
            } else {
                Logger.warning("Failed to delete remote file: " + remotePath);
            }

            return success;
        } finally {
            if (ftpClient != null) {
                FTPConnectionPool.getInstance().releaseConnection(ftpClient);
            }
        }
    }

    @Override
    public boolean replaceFile(File localFile, String remotePath) throws IOException {

        /**
         * Create the history dir in the FTP if it doesnt exist
         */

        FTPClient ftpClient = null;

        try {
            try {
                ftpClient = FTPConnectionPool.getInstance().getConnection(60);
                ftpClient.changeWorkingDirectory(WORK_DIR);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interrupted while waiting for FTP connection");
                return false;
            }

            boolean success;
            try (FileInputStream fis = new FileInputStream(localFile)) {

                /**
                 * Move to history directory the file to be replaced
                 */
                String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
                boolean renameSuccess = ftpClient.rename(remotePath,
                        DIR_HISTORY + "/" + remotePath + "_" + timestamp);
                if (renameSuccess) {
                    Logger.info("Successfully moved file to history: " + remotePath + " with date "
                            + LocalDate.now().toString());
                } else {
                    Logger.error("Failed to move file to history: " + remotePath);
                }

                ftpClient.changeWorkingDirectory(WORK_DIR);

                success = ftpClient.storeFile(remotePath, fis);
                if (success) {
                    Logger.info("Successfully replaced: " + remotePath);
                } else {
                    Logger.error("Failed to replace: " + remotePath);
                }
            }

            return success;
        } finally {
            if (ftpClient != null) {
                FTPConnectionPool.getInstance().releaseConnection(ftpClient);
            }
        }
    }

    @Override
    public String getHandlerName() {
        return "StandardTransfer";
    }

    @Override
    public List<String> listFiles(String remotePath) throws IOException {

        FTPClient ftpClient = null;

        try {
            try {
                ftpClient = FTPConnectionPool.getInstance().getConnection(60);
                ftpClient.changeWorkingDirectory(WORK_DIR);
                System.out.println("Actual WORK_DIR: " + ftpClient.printWorkingDirectory());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interrupted while waiting for FTP connection");
                return null;
            }

            String[] files = ftpClient.listNames(remotePath);
            if (files == null) {
                Logger.warning("No files found in remote path: " + remotePath);
                return null;
            }

            return List.of(files);
        } finally {
            if (ftpClient != null) {
                FTPConnectionPool.getInstance().releaseConnection(ftpClient);
            }
        }

    }

    @Override
    public boolean downloadFile(String remotePath, File localFile) throws IOException {
        FTPClient ftpClient = null;
        try {
            try {
                ftpClient = FTPConnectionPool.getInstance().getConnection(60);
                ftpClient.changeWorkingDirectory(WORK_DIR);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interrupted while waiting for FTP connection");
                return false;
            }

            boolean success;
            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                success = ftpClient.retrieveFile(remotePath, fos);
                if (success) {
                    Logger.info("Successfully downloaded: " + remotePath);
                } else {
                    Logger.error("Failed to download: " + remotePath);
                }
            }

            return success;
        } finally {
            if (ftpClient != null) {
                FTPConnectionPool.getInstance().releaseConnection(ftpClient);
            }
        }
    }

    @Override
    public HashMap<LocalDateTime, File> checkVersions(String fileName) throws IOException {
        HashMap<LocalDateTime, File> versions = new HashMap<>();

        FTPClient ftpClient = null;
       
        try {
            try {
                ftpClient = FTPConnectionPool.getInstance().getConnection(60);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interrupted while waiting for FTP connection");
                return null;
            }

            if (!ftpClient.changeWorkingDirectory(DIR_HISTORY)) {
                Logger.warning("Could not change to directory: " + DIR_HISTORY);
                return null;
            }
            
            String[] files = ftpClient.listNames();
            if (files == null || files.length == 0) {
                Logger.warning("No files found in remote path: " + DIR_HISTORY);
                return null;
            }
            
            Logger.info("Found " + files.length + " files in " + DIR_HISTORY);
            
            for (String file : files) {
                Logger.info("Checking file: " + file);
                if (file.contains(fileName)) {
                    try {
                        int underscoreIndex = file.lastIndexOf("_");
                        if (underscoreIndex > 0 && underscoreIndex < file.length() - 1) {
                            String dateStr = file.substring(underscoreIndex + 1);
                            
                            // Intentar parsear primero como LocalDateTime (nuevo formato)
                            LocalDateTime dateTime;
                            try {
                                dateTime = LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
                            } catch (Exception e) {
                                // Si falla, intentar con el formato antiguo (solo fecha)
                                LocalDate date = LocalDate.parse(dateStr);
                                dateTime = date.atStartOfDay(); // Convertir LocalDate a LocalDateTime
                            }
                            
                            Logger.info("Found version from datetime: " + dateTime);
                            versions.put(dateTime, new File(file));
                        }
                    } catch (Exception e) {
                        Logger.error("Error parsing date from file " + file + ": " + e.getMessage());
                    }
                }
            }
            
            ftpClient.changeWorkingDirectory(WORK_DIR);

        } finally {
            if (ftpClient != null) {
                FTPConnectionPool.getInstance().releaseConnection(ftpClient);
            }
        }
        
        return versions;
    }

}