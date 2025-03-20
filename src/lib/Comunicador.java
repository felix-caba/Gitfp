package lib;

import java.io.File;
import java.nio.file.Path;

import lib.Enum.LocalEvent;
import lib.Interfaces.FileEventListener;


/**
 * Comunicador: This class is in charge of communicating the events that are
 * going to be handled. Has a Jefazo Reference to add the events to the
 * ConcurrentHashMap.
 */

public class Comunicador implements FileEventListener {

    private Jefazo jefazo;
    
    public Comunicador(Jefazo jefazo) {
        this.jefazo = jefazo;
    }
    
    @Override
    public void onFileEvent(LocalEvent eventType, Path filePath) {
        File file = filePath.toFile();
        jefazo.addTodoEvent(eventType, file);
    }
    
}
