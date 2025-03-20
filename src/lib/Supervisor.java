package lib;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import lib.Enum.LocalEvent;
import lib.Interfaces.FileEventListener;
import lib.Utils.Logger;

import static java.nio.file.StandardWatchEventKinds.*;
import java.io.IOException;

/**
 * OrejaListener
 * This class instantiates a Thread that will Listen to changes inside my
 * folder. It will be transfering those changes to the @MozoExecutor class which
 * will be in charge of executing the commands.
 */
public class Supervisor extends Thread {

    private boolean on = true;
    Path directory = Paths.get("./syncro");
    private List<FileEventListener> listeners = new ArrayList<>();

    public void addListener(FileEventListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(LocalEvent eventType, Path filePath) {
        for (FileEventListener listener : listeners) {
            listener.onFileEvent(eventType, filePath);
        }
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            System.out.println("La Oreja esta escuchando cambios en Local");
    
            while (on) {
                WatchKey key = watchService.take(); 
    
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();
    
                    
                    Logger.info("Evento de sistema detectado - Tipo: " + kind.name() + " Archivo: " + fileName);
    
                    if (fileName.toString().equals(".DS_Store")) {
                        System.out.println("Ignorando archivo de sistema macOS: .DS_Store");
                        continue;
                    }
    
                    System.out.println(kind.name() + ": " + fileName);
                    if (kind == ENTRY_CREATE) {
                        System.out.println("El archivo " + fileName + " ha sido creado");
                        notifyListeners(LocalEvent.CREATE, fileName);
                    } else if (kind == ENTRY_DELETE) {
                        System.out.println("El archivo " + fileName + " ha sido eliminado");
                        notifyListeners(LocalEvent.DELETE, fileName);
                    } else if (kind == ENTRY_MODIFY) {
                        System.out.println("El archivo " + fileName + " ha sido modificado");
                        Logger.info("Notificando evento MODIFY para: " + fileName);
                        notifyListeners(LocalEvent.MODIFY, fileName);
                    } 
                }
    
                boolean valid = key.reset();
                if (!valid) {
                    break; 
                }
            }
        } catch (IOException e) {
            Logger.error("Error al registrar el directorio: " + e.getMessage());
        } catch (InterruptedException e) {
            Logger.error("Interrupción al observar el directorio: " + e.getMessage());
        }
    }

    public void stopListening() {
        on = false;
    }

}
