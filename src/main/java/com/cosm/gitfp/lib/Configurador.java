package com.cosm.gitfp.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.cosm.gitfp.lib.Security.EncryptionCredentials;
import com.cosm.gitfp.lib.Utils.Logger;

public class Configurador {

    /**
     * Saca todos los datos del config.properties y los guarda en las variables
     * de la clase
     */

    
    private static boolean encryptionEnabled;
    private static String ftpServer;
    private static int ftpPort;
    private static String ftpUser;
    private static String ftpPassword;

    public static void cargarConfiguracion() {

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");
            prop.load(input);

            encryptionEnabled = Boolean.parseBoolean(prop.getProperty("file.encryption.enabled"));
            ftpServer = prop.getProperty("ftp.server");
            ftpPort = Integer.parseInt(prop.getProperty("ftp.port"));
            ftpUser = prop.getProperty("ftp.username");
            ftpPassword = prop.getProperty("ftp.password");

            Logger.info("Configuración cargada: "
                    + "Encryption enabled: " + encryptionEnabled
                    + ", FTP Server: " + ftpServer
                    + ", FTP Port: " + ftpPort
                    + ", FTP User: " + ftpUser
                    + ", FTP Password: " + ftpPassword);

            if (encryptionEnabled) {
                EncryptionCredentials.setPassword();
                Logger.info("Encriptación de archivos activada");
            } else {
                Logger.info("Encriptación de archivos desactivada");
            }

        } catch (IOException ex) {
            Logger.error("Error al cargar el archivo de configuración: " + ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Logger.error("Error al cerrar el archivo de configuración");
                }
            }
        }

    }

    public static boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public static String getFtpServer() {
        return ftpServer;
    }

    public static int getFtpPort() {
        return ftpPort;
    }

    public static String getFtpUser() {
        return ftpUser;
    }

    public static String getFtpPassword() {
        return ftpPassword;
    }

    
    
}
