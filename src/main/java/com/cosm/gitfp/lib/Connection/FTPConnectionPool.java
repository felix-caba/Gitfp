package com.cosm.gitfp.lib.Connection;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.net.ftp.FTPClient;

import com.cosm.gitfp.lib.Configurador;
import com.cosm.gitfp.lib.Factory.FileTransferFactory;
import com.cosm.gitfp.lib.Utils.Logger;

public class FTPConnectionPool {

    private static FTPConnectionPool instance;
    private final ConcurrentLinkedQueue<FTPClient> connectionPool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    private String server;
    private int port;
    private String username;
    private String password;
    private final Semaphore connectionSemaphore;
    private int maxConnections;

    private FTPConnectionPool() {
        this.server = Configurador.getFtpServer();
        this.port = Configurador.getFtpPort();
        this.username = Configurador.getFtpUser();
        this.password = Configurador.getFtpPassword();
        this.maxConnections = 10;
        this.connectionSemaphore = new Semaphore(maxConnections, true);
    }

    public static synchronized FTPConnectionPool getInstance() {
        if (instance == null) {
            instance = new FTPConnectionPool();
        }
        return instance;
    }

    /**
     * Configura el pool de conexiones FTP
     */
    public void configure(String server, int port,
            String username, String password, int maxConnections) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.maxConnections = maxConnections;
        Logger.info("FTP pool configured for " + server + ":" + port + " with max "
                + maxConnections + " connections");

    }

    /**
     * Obtiene una conexión FTP del pool o crea una nueva si es necesario
     * Espera si todas las conexiones están en uso
     * 
     * @param timeoutSeconds Tiempo max de espera en segundos, 0 para esperar
     *                       indefinidamente
     */
    public FTPClient getConnection(int timeoutSeconds) throws IOException, InterruptedException {
        boolean acquired = false;

        try {

            if (timeoutSeconds > 0) {
                acquired = connectionSemaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS);
                if (!acquired) {
                    throw new IOException("Timeout waiting for FTP connection after " + timeoutSeconds + " seconds");
                }
            } else {
                Logger.info("Waiting for available FTP connection...");
                connectionSemaphore.acquire();
                acquired = true;
            }

            // Saca la cabeza de la queue
            FTPClient ftpClient = connectionPool.poll();

            if (ftpClient == null) {
                ftpClient = createConnection();
            } else {
                Logger.info("Reusing existing FTP connection from pool");
            }

            if (!ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (Exception e) {
                }
                ftpClient = createConnection();
            }

            return ftpClient;

        } catch (InterruptedException e) {
            if (acquired) {
                connectionSemaphore.release();
            }
            Thread.currentThread().interrupt();
            throw e;
        } catch (IOException e) {
            if (acquired) {
            
                connectionSemaphore.release();
            }
            throw e;
        }
    }

    /**
     * Devuelve una conexion al pool cuando ya no se necesita
     */
    public void releaseConnection(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                // Siempre al raiz
                ftpClient.changeWorkingDirectory(FileTransferFactory.getWORK_DIR());
            } catch (Exception e) {
                Logger.warning("No se pudo restablecer el directorio al liberar: " + e.getMessage());
            }
            
            connectionPool.offer(ftpClient);
            connectionSemaphore.release();
            Logger.info("FTP connection returned to pool. Available permits: " + 
                       connectionSemaphore.availablePermits());
        }
    }

    /**
     * Crea una nueva conexion FTP
     */
    private FTPClient createConnection() throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            Logger.info("Creating new FTP connection to " + server);
            ftpClient.connect(server, port);
            boolean success = ftpClient.login(username, password);

            if (!success) {
                throw new IOException("Failed to login to FTP server");
            }

            ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            activeConnections.incrementAndGet();
            return ftpClient;
        } catch (IOException e) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    // Ignorar
                }
            }
            activeConnections.decrementAndGet();
            throw e;
        }
    }

    /**
     * Cierra todas las conexiones en el pool
     */
    public void shutdown() {
        FTPClient ftpClient;
        while ((ftpClient = connectionPool.poll()) != null) {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                Logger.error("Error closing FTP connection: " + e.getMessage());
            } finally {
                activeConnections.decrementAndGet();
            }
        }
        Logger.info("FTP connection pool shut down");
    }
}