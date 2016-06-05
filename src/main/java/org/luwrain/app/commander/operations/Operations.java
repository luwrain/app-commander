
package org.luwrain.app.commander.operations;

import java.nio.file.*;

public class Operations
{
    static public Operation copy(Listener listener, String opName,
				 Path[] copyFrom, Path copyTo)
    {
	return new Copy(listener, opName, copyFrom, copyTo);
    }

    static public Operation delete(Listener listener, String opName,
				 Path[] deleteWhat)
    {
	return new Delete(listener, opName, deleteWhat);
    }
}
