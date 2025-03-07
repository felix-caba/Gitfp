package com.cosm.gitfp.lib.Security;



import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import java.util.Base64;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.nio.charset.StandardCharsets;



public class CrossAppEncryption {
  
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_SIZE = 256;
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    
    /**
     * Encripta un archivo para que pueda ser leído por otra aplicación
     * @param sourceFile Archivo a encriptar
     * @param destFile Archivo encriptado resultante
     * @param password Contraseña para encriptar (la misma se usará para desencriptar)
     */

    public static void encryptFile(File sourceFile, File destFile, String password) throws Exception {
        // Salt aleatorio
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        
        SecretKey key = getKeyFromPassword(password, salt);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        
        // Añadir metadatos (formato del archivo)
        // Los primeros bytes contienen: 
        // - Marca de formato (4 bytes)
        // - Longitud del salt (1 byte)
        // - Salt
        // - Longitud del IV (1 byte)
        // - IV
        
        try (FileInputStream inFile = new FileInputStream(sourceFile);
             FileOutputStream outFile = new FileOutputStream(destFile)) {
            
            outFile.write("CAEF".getBytes(StandardCharsets.UTF_8)); // CrossApp Encryption Format
            
            // Escribir salt
            outFile.write(salt.length);
            outFile.write(salt);
            
            // Escribir IV
            outFile.write(iv.length);
            outFile.write(iv);
            
    
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inFile.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    outFile.write(output);
                }
            }
            
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                outFile.write(outputBytes);
            }
        }
    }
    
    /**
     * Desencripta un archivo que fue encriptado por cualquier aplicación usando este sistema
     * @param encryptedFile Archivo encriptado
     * @param destFile Archivo desencriptado resultante
     * @param password Contraseña utilizada para encriptar
     */
    public static void decryptFile(File encryptedFile, File destFile, String password) throws Exception {
        try (FileInputStream inFile = new FileInputStream(encryptedFile);
             FileOutputStream outFile = new FileOutputStream(destFile)) {
            
            // Leer y verificar la marca de formato
            byte[] formatMark = new byte[4];
            inFile.read(formatMark);
            String format = new String(formatMark, StandardCharsets.UTF_8);
            if (!"CAEF".equals(format)) {
                throw new IllegalArgumentException("Formato de archivo no reconocido");
            }
            
            // Leer salt
            int saltLength = inFile.read();
            byte[] salt = new byte[saltLength];
            inFile.read(salt);
            
            // Leer IV
            int ivLength = inFile.read();
            byte[] iv = new byte[ivLength];
            inFile.read(iv);
            
            // Derivar la clave de la contraseña y salt
            SecretKey key = getKeyFromPassword(password, salt);
            
            // Configurar descifrado
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            // Leer los datos encriptados y desencriptar
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inFile.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    outFile.write(output);
                }
            }
            
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                outFile.write(outputBytes);
            }
        }
    }
    
    /**
     * Deriva una clave segura a partir de una contraseña y salt usando PBKDF2
     */
    private static SecretKey getKeyFromPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}