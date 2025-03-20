package lib;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lib.Enum.LocalEvent;
import lib.Utils.Logger;

/**
 * Jefazo: This class is in charge of managing the events that are going to be
 * handled.
 */
public class Jefazo extends Thread {

    /**
     * OrejaListener: This class is in charge of listening to the changes that are
     * going to be made in the local folder.
     */

    private Supervisor supervisor;

    /**
     * Pool of MozoExecutors
     */

    private ExecutorService obreros = Executors.newFixedThreadPool(10);
    private ConcurrentHashMap<LocalEvent, File> todoHash = new ConcurrentHashMap<LocalEvent, File>();

    @Override
    public void run() {


        Logger.info("Jefazo initializing");
        supervisor = new Supervisor();
        Comunicador comunicador = new Comunicador(this);
        supervisor.addListener(comunicador);
        supervisor.start();

        while (supervisor.isAlive()) {

            for (LocalEvent event : todoHash.keySet()) {
                
                Logger.info("Active Threads: " + Thread.activeCount());
                File file = todoHash.get(event);


                Logger.info("Processing event " + event + " for file " + file.getName());
                Obrero obrero = new Obrero(file, event);
                obreros.execute(obrero);
                removeTodoEvent(event);
            }

        }

        obreros.shutdown();
        
    }

    /**
     * Constructor.
     * 
     * @param supervisor:  Supervisor instance.
     *                     Sees the changes in the folder structure and notifies the
     *                     listeners.
     * 
     * @param comunicador: Comunicador instance.
     *                     Implements the Listener itself. Which communicates with
     *                     Jefazo.
     */
    public Jefazo() {

       

    }

    public void addTodoEvent(LocalEvent event, File file) {
        todoHash.put(event, file);
    }

    public File getTodoEvent(LocalEvent event) {
        return todoHash.get(event);
    }

    public void removeTodoEvent(LocalEvent event) {
        todoHash.remove(event);
    }

    public boolean containsEvent(LocalEvent event) {
        return todoHash.containsKey(event);
    }

}
