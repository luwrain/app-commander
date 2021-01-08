
package org.luwrain.app.commander;

import org.luwrain.app.commander.fileops.*;

public interface OperationListener
{
    void onOperationProgress(Operation operation);
    void onOperationProgress(Base base);
}
