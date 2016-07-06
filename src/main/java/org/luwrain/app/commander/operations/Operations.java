
package org.luwrain.app.commander.operations;

import java.nio.file.*;

import org.luwrain.core.NullCheck;

public class Operations
{
    static public Operation copy(Listener listener, String opName,
				 Path[] copyFrom, Path copyTo)
    {
	return new Copy(listener, opName, copyFrom, copyTo);
    }

    static public Operation move(Listener listener, String opName,
				 Path[] moveFrom, Path moveTo)
    {
	NullCheck.notNull(opName, "opName");
	NullCheck.notNullItems(moveFrom, "moveFrom");
	NullCheck.notNull(moveTo, "moveTo");
	//	return new Copy(listener, opName, copyFrom, copyTo);
	return null;
    }


    static public Operation delete(Listener listener, String opName,
				 Path[] deleteWhat)
    {
	return new Delete(listener, opName, deleteWhat);
    }
}
