package lib.Utils;

import java.util.Scanner;

public class ScannerKey {
    private static final Scanner scanner = new Scanner(System.in);
    
    public static String scannerStr(String message) {
        System.out.println(message);
        return scanner.nextLine();
    }

    public static int scannerInt(String message) {
        System.out.println(message);
        int value = scanner.nextInt();
        scanner.nextLine(); 
        return value;
    }
    
    
    public static void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}