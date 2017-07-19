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
import java.net.*;

import org.apache.commons.vfs2.*;

import org.luwrain.core.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;

class PanelArea extends CommanderArea<FileObject>
{
    private final ActionList actionList;

    PanelArea(Params<FileObject> params, ActionList actionList)
    {
	super(params);
	NullCheck.notNull(actionList, "actionList");
	this.actionList = actionList;
    }

    @Override public boolean onAreaQuery(AreaQuery query)
    {
	NullCheck.notNull(query, "query");
	if (query.getQueryCode() == AreaQuery.CURRENT_DIR && query instanceof CurrentDirQuery)
	{
	    final CurrentDirQuery currentDirQuery = (CurrentDirQuery)query;
	    final File f = getOpenedAsFile();
	    if (f == null)
		return false;
	    currentDirQuery.answer(f.getAbsolutePath());
	    return true;
	}
	return super.onAreaQuery(query);
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

    Object[] getObjectsToProcess()
    {
	final FileObject[] objs = getFileObjectsToProcess();
	final List res = new LinkedList();
	for(FileObject f: objs)
	{
	    if (f instanceof org.apache.commons.vfs2.provider.local.LocalFile)
		res.add(new File(f.getName().getPath()));
	    if (f instanceof org.apache.commons.vfs2.provider.ftp.FtpFileObject)
	    {
		try {
		    res.add(new java.net.URL(f.getName().getPath()));
		}
		catch(MalformedURLException e)
		{
		    //FIXME:
		}
	    }
	}
	return res.toArray(new Object[res.size()]);
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

    File getOpenedAsFile()
    {
	if (!isLocalDir())
	    return null;
	final FileObject obj = opened();
	return obj != null?new File(obj.getName().getPath()):null;
    }

    FileObject getOpenedAsFileObject()
    {
	return opened();
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

    void showHidden()
    {
	setCommanderFilter(new CommanderUtilsVfs.AllEntriesFilter());
	reread(false);
    }

    void hideHidden()
    {
	setCommanderFilter(new CommanderUtilsVfs.NoHiddenFilter());
	reread(false);
    }

		@Override public Action[] getAreaActions()
		{
return actionList.getPanelAreaActions(this);
		}

    static Params<FileObject> createParams(ControlEnvironment environment) throws FileSystemException
    {
	NullCheck.notNull(environment, "environment");
	Params<FileObject> params = CommanderUtilsVfs.createParams(environment);
	params.flags = EnumSet.of(Flags.MARKING);
	params.filter = new CommanderUtilsVfs.NoHiddenFilter();
	return params;
    }
}
