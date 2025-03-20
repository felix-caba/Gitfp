package lib.Interfaces;

import java.nio.file.Path;
import lib.Enum.LocalEvent;

public interface FileEventListener {
    void onFileEvent(LocalEvent eventType, Path filePath);
}