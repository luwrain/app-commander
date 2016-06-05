
package org.luwrain.app.commander.operations;

import java.nio.file.*;

import org.luwrain.app.commander.Operation;
import org.luwrain.app.commander.OperationListener;

public class Operations
{
    static public Operation copy(OperationListener listener, String opName,
				 Path[] copyFrom, Path copyTo)
    {
	return new Copy(listener, opName, copyFrom, copyTo);
    }

    static public Operation delete(OperationListener listener, String opName,
				 Path[] deleteWhat)
    {
	return new Delete(listener, opName, deleteWhat);
    }
}
