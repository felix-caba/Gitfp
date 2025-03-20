package lib.Factory;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;

import lib.Configurador;
import lib.Connection.FTPConnectionPool;
import lib.Handlers.EncryptedFileTransferHandler;
import lib.Handlers.StandardFileTransferHandler;
import lib.Interfaces.FileTransferHandler;
import lib.Security.EncryptionCredentials;
import lib.Utils.Logger;

/**
 * Fábrica para crear manejadores de transferencia de archivos
 */
public class FileTransferFactory {
    private static FileTransferHandler instance;

    private static String WORK_DIR = "";
    private static String DIR_HISTORY = "history";

    public static boolean initFileSystems() throws IOException {
        FTPClient ftpClient = null;
        try {
            try {
                ftpClient = FTPConnectionPool.getInstance().getConnection(60);
                WORK_DIR = ftpClient.printWorkingDirectory();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error("Interrupted while waiting for FTP connection");
                return false;
            }

            Logger.info("Working directory: " + WORK_DIR);

            /**
             * Check si existe el directorio de historial
             */
            if (!ftpClient.changeWorkingDirectory(DIR_HISTORY)) {
                if (ftpClient.makeDirectory(DIR_HISTORY)) {
                    Logger.info("Created history directory: " + DIR_HISTORY);
                    DIR_HISTORY = ftpClient.printWorkingDirectory();
                } else {
                    Logger.error("Failed to create history directory: " + DIR_HISTORY);
                }
            }
            ftpClient.changeWorkingDirectory(WORK_DIR);
            return true;
        } finally {
            if (ftpClient != null) {
                FTPConnectionPool.getInstance().releaseConnection(ftpClient);
            }
        }
    }



    /**
     * Obtiene el manejador de transferencia configurado
     */
    public static synchronized FileTransferHandler getHandler() {
        if (instance == null) {
            instance = createHandler();
        }
        return instance;
    }

    /**
     * Fuerza la recreación del manejador
     */
    public static synchronized void resetHandler() {
        instance = null;
    }

    /**
     * Crea el manejador adecuado según la configuración
     */
    private static FileTransferHandler createHandler() {
        try {

            FileTransferHandler baseHandler = new StandardFileTransferHandler();
            boolean encryptionEnabled = isEncryptionEnabled();

            if (encryptionEnabled) {
                String password = EncryptionCredentials.PASSWORD;
                if (password != null && !password.isEmpty()) {
                    Logger.info("Encryption enabled for file transfers");
                    return new EncryptedFileTransferHandler(baseHandler, password);
                } else {
                    Logger.warning("Encryption configured but no password provided - using standard transfer");
                }
            } else {
                Logger.info("Using standard file transfer (no encryption)");
            }

            return baseHandler;
        } catch (Exception e) {
            Logger.error("Error creating file transfer handler: " + e.getMessage());
            Logger.info("Falling back to standard transfer");
            return new StandardFileTransferHandler();
        }
    }

    /**
     * Verifica si la encriptación está habilitada en la configuracion
     */
    private static boolean isEncryptionEnabled() {
        return Configurador.isEncryptionEnabled();
    }

    public static String getDIR_HISTORY() {
        return DIR_HISTORY;
    }

    

    public static String getWORK_DIR() {
        return WORK_DIR;
    }
}