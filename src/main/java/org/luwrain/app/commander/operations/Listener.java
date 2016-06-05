
package org.luwrain.app.commander.operations;

import java.nio.file.Path;

public interface Listener
{
    void onOperationProgress(Operation operation);
    boolean confirmOverwrite(Path path);
    boolean confirmOverwrite();
}
