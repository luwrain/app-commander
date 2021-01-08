/*
   Copyright 2012-2021 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.commander.fileops;

import java.io.*;
import java.nio.file.Path;

import org.luwrain.core.*;
import org.luwrain.app.commander.*;

class Delete extends Base
{
    private final Path[] toDelete;

    Delete(OperationListener listener, String name,
	   Path[] toDelete)
    {
	super(listener, name);
	this.toDelete = toDelete;
	NullCheck.notNullItems(toDelete, "toDelete");
	NullCheck.notEmptyArray(toDelete, "toDelete");
	for(int i = 0;i < toDelete.length;++i)
	    if (!toDelete[i].isAbsolute())
		throw new IllegalArgumentException("toDelete[" + i + "] must be absolute");
    }

    @Override protected Result work() throws IOException
    {
	for(Path p: toDelete)
	{
	    final Result res = deleteFileOrDir(p);
	    if (res.getType() != Result.Type.OK)
		return res;
	}
	return new Result();
    }

    public int getPercents()
    {
	return 0;
    }
}
