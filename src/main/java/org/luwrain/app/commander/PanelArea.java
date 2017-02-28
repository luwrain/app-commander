/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import org.apache.commons.vfs2.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;

class PanelArea extends NgCommanderArea<FileObject>
{

    PanelArea(Params<FileObject> params)
    {
	super(params);
    }

    boolean isLocalDir()
    {
	final FileObject o = opened();
	if (o == null)
	    return false;
	return o instanceof org.apache.commons.vfs2.provider.local.LocalFile;
    }

    FileObject[] getFileObjectsToProcess()
    {
	final LinkedList<FileObject> res = new LinkedList<FileObject>();
	for(Object o: getMarked())
	    res.add((FileObject)o);
	if (!res.isEmpty())
	    return res.toArray(new FileObject[res.size()]);
	final FileObject entry = getSelectedEntry();
	return entry != null?new FileObject[]{entry}:new FileObject[0];
    }

    File getOpenedAsFile()
    {
	final FileObject obj = opened();
	return obj != null?new File(obj.getName().getPath()):null;
    }

    File[] getFilesToProcess()
    {
	if (!isLocalDir())
	    return new File[0];
	final FileObject[] objects = getFileObjectsToProcess();
	final File[] res = new File[objects.length];
	for(int i = 0;i < objects.length;++i)
	    res[i] = new File(objects[i].getName().getPath());
	return res;
    }

    boolean openLocalPath(String path)
    {
	NullCheck.notNull(path, "path");
	try {
	    open(CommanderUtilsVfs.prepareLocation((CommanderUtilsVfs.Model)getCommanderModel(), path));
	    return true;
	}
	catch(org.apache.commons.vfs2.FileSystemException e)
	{
	    Log.error("commander", "opening " + path + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return false;
	}
    }

    boolean openInitial(String path)
    {
	NullCheck.notNull(path, "path");
	try {
	    return open(CommanderUtilsVfs.prepareLocation((CommanderUtilsVfs.Model)getCommanderModel(), path), false);
	}
	catch(org.apache.commons.vfs2.FileSystemException e)
	{
	    Log.error("commander", "opening " + path + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return false;
	}
    }


    static Params<FileObject> createParams(ControlEnvironment environment) throws FileSystemException
    {
	NullCheck.notNull(environment, "environment");
	Params<FileObject> params = CommanderUtilsVfs.createParams(environment);
	return params;
    }

}
