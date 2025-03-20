package lib.Security;

import lib.Utils.ScannerKey;

public class EncryptionCredentials {
    public static String PASSWORD;

    public static void setPassword() {
        PASSWORD = ScannerKey.scannerStr("Enter the password for the encryption key: ");
    }
}
