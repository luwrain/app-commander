
package org.luwrain.app.commander.operations;

import java.io.*;
import java.nio.file.Path;

import org.luwrain.core.NullCheck;
import org.luwrain.app.commander.OperationListener;

class Delete extends Base
{
    private Path[] deleteWhat;

    Delete(OperationListener listener, String opName,
	   Path[] deleteWhat)
    {
	super(listener, opName);
	this.deleteWhat = deleteWhat;
	NullCheck.notNullItems(deleteWhat, "deleteWhat");
	if (deleteWhat.length < 1)
	    throw new IllegalArgumentException("deleteWhat may not be empty");
	for(int i = 0;i < deleteWhat.length;++i)
	    if (!deleteWhat[i].isAbsolute())
		throw new IllegalArgumentException("deleteWhat[" + i + "] must be absolute");
    }

    @Override protected void work() throws OperationException
    {
	    for(Path p: deleteWhat)
	    deleteFileOrDir(p);
    }

    private void deleteFileOrDir(Path p) throws OperationException
    {
	    if (interrupted)
		throw new OperationException(Result.INTERRUPTED);
	    if (isDirectory(p, false))
	{
	    final Path[] content = getDirContent(p);
	    for(Path pp: content)
		deleteFileOrDir(pp);
	}
	    delete(p);
    }

    @Override public int getPercents()
    {
	return 0;
    }
	   }
