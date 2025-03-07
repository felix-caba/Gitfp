package com.cosm.gitfp.lib.Interfaces;

import java.nio.file.Path;
import com.cosm.gitfp.lib.Enum.LocalEvent;

public interface FileEventListener {
    void onFileEvent(LocalEvent eventType, Path filePath);
}