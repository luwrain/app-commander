/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.commander.operations;

import java.io.*;
import java.nio.file.Path;

import org.luwrain.core.NullCheck;

class Delete extends Base
{
    private Path[] deleteWhat;

    Delete(Listener listener, String opName,
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

    @Override protected Result work() throws IOException
    {
	/*
	    for(Path p: deleteWhat)
	    deleteFileOrDir(p);
	*/
	return Result.OK;
    }

    private void deleteFileOrDir(Path p)
    {
	/*
	    if (interrupted)
		throw new OperationException(Result.INTERRUPTED);
	    if (isDirectory(p, false))
	{
	    final Path[] content = getDirContent(p);
	    for(Path pp: content)
		deleteFileOrDir(pp);
	}
	    delete(p);
	*/
    }

    @Override public int getPercents()
    {
	return 0;
    }
	   }
