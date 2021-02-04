/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

import org.apache.commons.vfs2.*;

import org.luwrain.core.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.script.*;
import org.luwrain.io.*;

class PanelArea extends CommanderArea<FileObject>
{
    private final Luwrain luwrain;

    PanelArea(Params<FileObject> params, Luwrain luwrain)
    {
	super(params);
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    @Override public boolean onAreaQuery(AreaQuery query)
    {
	NullCheck.notNull(query, "query");
	if (query.getQueryCode() == AreaQuery.CURRENT_DIR && query instanceof CurrentDirQuery)
	{
	    final CurrentDirQuery currentDirQuery = (CurrentDirQuery)query;
	    final File f = asFile(opened());
	    if (f == null)
		return false;
	    currentDirQuery.answer(f.getAbsolutePath());
	    return true;
	}
	return super.onAreaQuery(query);
    }

    boolean runHookOnSelected(String hookPrefix)
    {
	NullCheck.notEmpty(hookPrefix, "hookPrefix");
	final FileObject obj = getSelectedEntry();
	if (obj == null)
	    return false;
	if (isLocalDir())
	{
	    if (!(obj instanceof org.apache.commons.vfs2.provider.local.LocalFile))
		throw new RuntimeException("The entry is not a local file while the local dir is opened");
	    final File f = new File(obj.getName().getPath());
	    try {
		if (luwrain.xRunHooks(hookPrefix + ".local.custom", new Object[]{f}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
		    return true;
		return luwrain.xRunHooks(hookPrefix + ".local.default", new Object[]{f}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY);
	    }
	    catch(RuntimeException e)
	    {
		luwrain.message(luwrain.i18n().getExceptionDescr(e), Luwrain.MessageType.ERROR);
		return true;
	    }
	}
	//FIXME:remote 
	return false;
    }

    boolean runHookOnFilesToProcess(String hookPrefix, boolean background)
    {
	NullCheck.notEmpty(hookPrefix, "hookPrefix");
	final Object arg;
	final String hookName;
	if (isLocalDir())
	{
	    final File[] files = asFile(getToProcess());
	    if (files.length == 0)
		return false;
	    final String[] names = new String[files.length];
	    for(int i = 0;i < files.length;i++)
		names[i] = files[i].getAbsolutePath();
	    arg = ScriptUtils.createReadOnlyArray(names);
	    hookName = hookPrefix + ".local";
	} else
	    return false;
	if (!background)
	    try {
		if (luwrain.xRunHooks(hookName + ".custom", new Object[]{arg}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
		    return true;
		return luwrain.xRunHooks(hookName, new Object[]{arg}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY);
	    }
	    catch(RuntimeException e)
	    {
		luwrain.crash(e);
		return true;
	    }
	luwrain.executeBkg(new FutureTask(()->{
		    try {
			if (luwrain.xRunHooks(hookName + ".custom", new Object[]{arg}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY))
			    return;
			luwrain.xRunHooks(hookName, new Object[]{arg}, Luwrain.HookStrategy.CHAIN_OF_RESPONSIBILITY);
			return;
		    }
		    catch(RuntimeException e)
		    {
			luwrain.crash(e);
		    }
	}, null));
	return true;
    }

    boolean isLocalDir()
    {
	final FileObject o = opened();
	if (o == null)
	    return false;
	return o instanceof org.apache.commons.vfs2.provider.local.LocalFile;
    }

    FileObject[] getToProcess()
    {
	final List<FileObject> res = new ArrayList();
	for(Object o: getMarked())
	    res.add((FileObject)o);
	if (!res.isEmpty())
	    return res.toArray(new FileObject[res.size()]);
	final FileObject entry = getSelectedEntry();
	return entry != null?new FileObject[]{entry}:new FileObject[0];
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
	setCommanderFilter(new CommanderUtils.AllEntriesFilter());
	reread(false);
    }

    void hideHidden()
    {
	setCommanderFilter(new CommanderUtilsVfs.NoHiddenFilter());
	reread(false);
    }

    static File asFile(FileObject fileObject)
    {
	if (fileObject == null)
	    return null;
		    if (fileObject instanceof org.apache.commons.vfs2.provider.local.LocalFile)
			return new File(fileObject.getName().getPath());
		    return null;
    }

    URL asUrl(FileObject fileObject)
    {
	if (fileObject == null)
	    return null;
	if (fileObject instanceof org.apache.commons.vfs2.provider.ftp.FtpFileObject)
	{
	    final org.apache.commons.vfs2.provider.ftp.FtpFileObject ftpFile = (org.apache.commons.vfs2.provider.ftp.FtpFileObject)fileObject;
	    try {
		final java.net.URL root = new java.net.URL(ftpFile.getFileSystem().getRootURI());
		return new java.net.URL(root, fileObject.getName().getPath());
	    }
	    catch(MalformedURLException e)
	    {
		throw new IllegalArgumentException(e);
	    }
	}
	    return null;
    }

        static File[]asFile(FileObject[] fileObjects)
    {
	NullCheck.notNullItems(fileObjects, "fileObjects");
	final List<File> res = new ArrayList();
	for(FileObject f: fileObjects)
	{
	    final File ff = asFile(f);
	    if (ff != null)
		res.add(ff);
	}
	return res.toArray(new File[res.size()]);
    }

    static Params<FileObject> createParams(Luwrain luwrain) throws FileSystemException
    {
	NullCheck.notNull(luwrain, "luwrain");
	Params<FileObject> params = CommanderUtilsVfs.createParams(new DefaultControlContext(luwrain));
	params.flags = EnumSet.of(Flags.MARKING);
	params.filter = new CommanderUtilsVfs.NoHiddenFilter();
	params.clipboardSaver = (area,model,appearance,fromIndex,toIndex,clipboard)->{
	    NullCheck.notNull(model, "model");
	    NullCheck.notNull(clipboard, "clipboard");
	    if (fromIndex < 0 || toIndex < 0)
		throw new IllegalArgumentException("fromIndex and toIndex may not be negative");
	    final int count = model.getItemCount();
	    if (fromIndex >= toIndex || fromIndex >= count || toIndex > count)
		return false;
	    final List<String> names = new ArrayList();
	    final List<Serializable> res = new ArrayList();
	    for(int i = fromIndex;i < toIndex;++i)
	    {
		final CommanderArea.NativeItem<FileObject> nativeObj = (CommanderArea.NativeItem<FileObject>)model.getItem(i);
		final FileObject fileObj = nativeObj.getNativeObj();
		names.add(nativeObj.getBaseName());
		final File file = asFile(fileObj);
		if (file != null)
		    res.add(file); else
		    res.add(fileObj.getName().getBaseName());
	    }
	    return clipboard.set(res.toArray(new Object[res.size()]), names.toArray(new String[names.size()]));
	};
	return params;
    }
}
