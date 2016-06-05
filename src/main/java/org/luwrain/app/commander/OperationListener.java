
package org.luwrain.app.commander;

import java.nio.file.Path;

public interface OperationListener
{
    void onOperationProgress(Operation operation);
    boolean confirmOverwrite(Path path);
    boolean confirmOverwrite();
}
