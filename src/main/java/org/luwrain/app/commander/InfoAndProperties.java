/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.commander;

import java.util.*;
import java.io.*;
import java.nio.file.attribute.*;

import org.apache.commons.vfs2.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class InfoAndProperties
{
    private final Luwrain luwrain;

    InfoAndProperties(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    boolean calcSize(FileObject[] fileObjects)
    {
	NullCheck.notNullItems(fileObjects, "fileObjects");
	if (fileObjects.length < 1)
	    return false;
	new Thread(()->{
		long res = 0;
		try {
		    for(FileObject obj: fileObjects)
			res += getTotalSize(obj);
		}
		catch(org.apache.commons.vfs2.FileSystemException e)
		{
		    luwrain.crash(e);
		    return;
		}
		final long finalRes = res;
		luwrain.message(formatSize(finalRes), Luwrain.MessageType.DONE);
	}).start();
	return true;
    }

    static private String formatSize(long size)
    {
	if (size >= ((long)1024 * 1024 * 1024 * 10))
	{
	    final long value = size / (1024 * 1024 * 1024);
	    return "" + value + "G";
	}
	if (size >= ((long)1048576 * 10))
	{
	    final long value = size / 1048576;
	    return "" + value + "M";
	}
	if (size >= ((long)1024 * 10))
	{
	    final long value = size / 1024;
	    return "" + value + "K";
	}
	return "" + size;
    }

    static public long getTotalSize(FileObject fileObj) throws org.apache.commons.vfs2.FileSystemException
    {
	NullCheck.notNull(fileObj, "fileObj");
	if (!fileObj.isFolder() && !fileObj.isFile())
	    return 0;
	if (fileObj instanceof org.apache.commons.vfs2.provider.local.LocalFile &&
	    java.nio.file.Files.isSymbolicLink(java.nio.file.Paths.get(fileObj.getName().getPath())))
	    return 0;
	if (!fileObj.isFolder())
	    return fileObj.getContent().getSize();
	long res = 0;
	for(FileObject child: fileObj.getChildren())
	    res += getTotalSize(child);
	return res;
    }
}
