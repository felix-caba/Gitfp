

import java.util.InputMismatchException;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lib.Configurador;
import lib.Connection.FileOperationService;
import lib.Factory.FileTransferFactory;
import lib.Jefazo;
import lib.Utils.Logger;
import lib.Utils.ScannerKey;

public class App {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
       
        Configurador.cargarConfiguracion();

        Jefazo jefazo = new Jefazo();
        jefazo.start();

        FileOperationService fileService = new FileOperationService();

        try {
            FileTransferFactory.initFileSystems();
        } catch (IOException e) {
            Logger.error("Error al inicializar sistemas de archivos: " + e.getMessage());
            System.exit(1);
        }
        
        

        boolean running = true;

        while (running) {
            System.out.println("\n==== GitFP Menu ====");
            System.out.println("1. Listar archivos remotos");
            System.out.println("2. Descargar archivo");
            System.out.println("3. Descargar versión");
            System.out.println("4. Borrar archivo remoto");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                int option = ScannerKey.scannerInt("Introduce la opción");

                switch (option) {
                    case 1:

                        _handleListView(fileService);
                        break;

                    case 2:

                        _handleListView(fileService);

                        String remoteFileToDownload = ScannerKey
                                .scannerStr("Introduce el nombre del archivo a descargar");

                        if (fileService.downloadFile(remoteFileToDownload)) {
                            System.out.println("Archivo descargado exitosamente en la carpeta 'downloads'");
                        } else {
                            System.out.println("Error al descargar el archivo");
                        }
                        break;

                    case 3:
                        _handleListView(fileService);

                        String remoteFile = ScannerKey
                                .scannerStr("Introduce el nombre del archivo antiguo a descargar sin fecha." +
                                " Introduce X si el archivo ya no está disponible y quieres ver el historial");

                        if (remoteFile.equalsIgnoreCase("X")) {
                            _handleListViewHistory(fileService);
                            continue;
                        }
                        
                        HashMap<LocalDateTime, File> versionsMap = fileService.checkVersions(remoteFile);

                        if (versionsMap == null || versionsMap.isEmpty()) {
                            System.out.println("No se encontraron versiones para el archivo: " + remoteFile);
                            break;
                        }

                        TreeMap<LocalDateTime, File> sortedVersions = new TreeMap<>(versionsMap);
                        ArrayList<LocalDateTime> datesList = new ArrayList<>(sortedVersions.keySet());

                        System.out.println("Versiones disponibles:");
                        int numVersions = Math.min(datesList.size(), 9);

                        for (int i = 0; i < numVersions; i++) {
                            LocalDateTime dateTime = datesList.get(i);
                            String fileName = sortedVersions.get(dateTime).getName();
                            // Mostrar fecha y hora con formato amigable
                            System.out.println((i + 1) + ". " + dateTime.format(DISPLAY_FORMATTER) + " - " + fileName);
                        }

                        int versionChoice = ScannerKey.scannerInt("Seleccione una versión (1-" + numVersions + ")");

                        if (versionChoice < 1 || versionChoice > numVersions) {
                            System.out.println("Opción inválida. Debe elegir entre 1 y " + numVersions);
                            break;
                        }

                        LocalDateTime selectedDateTime = datesList.get(versionChoice - 1);
                        File selectedFile = sortedVersions.get(selectedDateTime);

                        String selectedFileName = selectedFile.getName();
                        String remotePath = "history/" + selectedFileName;

                        System.out.println("Path remoto: " + remotePath);

                        System.out.println("Descargando versión de " + selectedDateTime.format(DISPLAY_FORMATTER) + ": "
                                + selectedFileName);

                        if (fileService.downloadFile(remotePath)) {
                            System.out.println("Archivo descargado exitosamente en la carpeta 'downloads'");
                        } else {
                            System.out.println("Error al descargar el archivo");
                        }
                        break;

                    case 4:
                        _handleListView(fileService);
                        String fileToDelete = ScannerKey.scannerStr("Introduce el nombre del archivo a borrar");

                        if (fileService.deleteRemoteFile(fileToDelete)) {
                            System.out.println("Archivo borrado exitosamente");
                        } else {
                            System.out.println("Error al borrar el archivo");
                        }
                        break;

                    case 5:
                        System.out.println("Saliendo...");
                        running = false;
                        break;

                    default:
                        System.out.println("Opción no válida");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Error: Introduce un número válido");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        ScannerKey.close();
        Logger.shutdown();
        System.exit(0);
    }

    static void _handleListView(FileOperationService fileService) {
        System.out.println("Archivos remotos:");
        List<String> files = fileService.listRemoteFiles("");
        if (files != null) {
            for (String file : files) {
                if (file.contains("history")) {
                    System.out.println(" - " + file + " (historial)");
                    continue;
                }
                System.out.println(" - " + file);
            }
        } else {
            System.out.println("No hay archivos o ocurrió un error");
        }
    }

    static void _handleListViewHistory(FileOperationService fileService) {
        System.out.println("Archivos remotos:");
        List<String> files = fileService.listRemoteFiles("history");
        if (files != null) {
            for (String file : files) {
                System.out.println(" - " + file);
            }
        } else {
            System.out.println("No hay archivos o ocurrió un error");
        }
    }
}