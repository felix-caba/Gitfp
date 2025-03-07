package com.cosm.gitfp.lib.Utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

public class Logger {
    private static java.util.logging.Logger logger;
    private static FileHandler fileHandler;
    private static boolean initialized = false;
    

    private static class CustomFormatter extends Formatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date(record.getMillis())))
              .append(" [").append(record.getLevel()).append("] ")
              .append(record.getMessage())
              .append("\n");
            return sb.toString();
        }
    }
    
    public static void setup() throws IOException {
        if (initialized) return;
        
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        
        logger = java.util.logging.Logger.getLogger(Logger.class.getName());
        

        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        for (java.util.logging.Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        
        fileHandler = new FileHandler("logs/application.log", true);
        

        fileHandler.setFormatter(new CustomFormatter());
        
        logger.addHandler(fileHandler);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false); 
        
        initialized = true;
    }
    
    public static void info(String message) {
        ensureInitialized();
        logger.info(message);
    }
    
    public static void warning(String message) {
        ensureInitialized();
        logger.warning(message);
    }
    
    public static void error(String message) {
        ensureInitialized();
        logger.severe(message);
    }
    
    public static void debug(String message) {
        ensureInitialized();
        logger.fine(message);
    }
    
    private static void ensureInitialized() {
        if (!initialized) {
            try {
                setup();
            } catch (IOException e) {
                System.err.println("Failed to initialize logger: " + e.getMessage());
            }
        }
    }
    
    // Close properly
    public static void shutdown() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}