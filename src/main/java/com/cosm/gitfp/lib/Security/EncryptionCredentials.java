package com.cosm.gitfp.lib.Security;

import java.util.Scanner;

import com.cosm.gitfp.lib.Utils.ScannerKey;

public class EncryptionCredentials {
    public static String PASSWORD;

    public static void setPassword() {
        PASSWORD = ScannerKey.scannerStr("Enter the password for the encryption key: ");
    }
}
