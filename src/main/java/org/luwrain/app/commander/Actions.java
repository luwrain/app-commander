/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import java.io.*;
import java.util.*;
import java.net.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.util.*;

import org.luwrain.app.commander.Base.Side;

class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;

    final Conversations conversations;

    Actions(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.base = base;
	this.strings = strings;
	this.conversations = new Conversations(luwrain, strings);
    }

    PanelArea.ClickHandler.Result onClick(CommanderArea area, Object obj, boolean dir)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(obj, "obj");
	if (dir)
	    return PanelArea.ClickHandler.Result.OPEN_DIR;
	final PanelArea panelArea = (PanelArea)area;
	if (!panelArea.isLocalDir())//FIXME:
	    return PanelArea.ClickHandler.Result.REJECTED;
	try {
	    //Maybe it's better to make a separate method translating FileObject to java.io.File
	final FileObject fileObject = (FileObject)obj;
	final File file = org.luwrain.util.Urls.toFile(fileObject.getURL());
	luwrain.openFile(file.getAbsolutePath());
	return CommanderArea.ClickHandler.Result.OK;
	}
	catch(Exception e)
	{
	    luwrain.crash(e);
	    	    return PanelArea.ClickHandler.Result.REJECTED;
	}
    }

    boolean onLocalCopy(PanelArea copyFromArea, PanelArea copyToArea, FilesOperation.Listener listener)
    {
	NullCheck.notNull(copyFromArea, "copyFromArea");
	NullCheck.notNull(copyToArea, "copyToArea");
	NullCheck.notNull(listener, "listener");
	if (!copyFromArea.isLocalDir() || !copyToArea.isLocalDir())
	    return false;
	final File copyFromDir = copyFromArea.getOpenedAsFile();
	if (copyFromDir == null || !copyFromDir.isAbsolute())
	    return false;
	final File[] filesToCopy = copyFromArea.getFilesToProcess();
	if (filesToCopy.length < 1)
	    return false;
	final File copyToDir = copyToArea.getOpenedAsFile();
	if (copyToDir == null || !copyToDir.isAbsolute())
	    return false;
	final File dest = conversations.copyPopup(copyFromDir, filesToCopy, copyToDir);
	if (dest == null)
	    return true;
	base.launch(luwrain.getFilesOperations().copy(listener, conversations.copyOperationName(filesToCopy, dest), filesToCopy, dest));
	return true;
    }

    boolean onLocalMove(PanelArea moveFromArea, PanelArea moveToArea, FilesOperation.Listener listener)
    {
	NullCheck.notNull(moveFromArea, "moveFromArea");
	NullCheck.notNull(moveToArea, "moveToArea");
	NullCheck.notNull(listener, "listener");
	if (!moveFromArea.isLocalDir() || !moveToArea.isLocalDir())
	    return false;
	final File moveFromDir = moveFromArea.getOpenedAsFile();
	final File[] filesToMove = moveFromArea.getFilesToProcess();
	final File moveTo = moveToArea.getOpenedAsFile();
	if (filesToMove.length < 1)
	    return false;
	final File dest = conversations.movePopup(moveFromDir, filesToMove, moveTo);
	if (dest == null)
	    return true;
base.launch(luwrain.getFilesOperations().move(listener, 
conversations.moveOperationName(filesToMove, dest), filesToMove, dest));
	return true;
    }

    boolean mkdir(CommanderApp app, PanelArea area)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(area, "area");
	if (!area.isLocalDir())
	    return false;
	final File createIn = area.getOpenedAsFile();
	if (createIn == null || !createIn.isAbsolute())
	    return false;
	final File newDir = conversations.mkdirPopup(createIn);
	if (newDir == null)
	    return true;
	try {
	    java.nio.file.Files.createDirectories(newDir.toPath());//FIXME:
	}
	catch (IOException e)
	{
	    luwrain.message(strings.mkdirErrorMessage(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return true;
	}
	luwrain.message(strings.mkdirOkMessage(newDir.getName()), Luwrain.MessageType.OK);
	area.reread(newDir.getName(), false);
	return true;
    }

boolean onLocalDelete(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	if (!area.isLocalDir())
	    return false;
	final File[] files = area.getFilesToProcess();
	if (files.length == 0)
	    return false;
	if (!conversations.deleteConfirmation(files))
	    return true;

	//	  operations.launch(Operations.delete(operations, strings.delOperationName(filesToDelete), 
	//	  filesToDelete));
	return true;
    }


    boolean showFileObjectsProperties(InfoAndProperties infoAndProps, PanelArea area, SimpleArea propertiesArea)
    {
	NullCheck.notNull(area, "area");
	final FileObject[] paths = area.getFileObjectsToProcess();
	if (paths.length < 1)
	    return false;
	propertiesArea.clear();
	//	infoAndProps.fillProperties(propertiesArea, paths);
	return true;
    }

    boolean showVolumeInfo(InfoAndProperties infoAndProps, PanelArea area, SimpleArea propsArea)
    {
	NullCheck.notNull(infoAndProps, "infoAndProps");
	NullCheck.notNull(area, "area");
	NullCheck.notNull(propsArea, "propsArea");
	if (area.isLocalDir())
	{
	    final File opened = area.getOpenedAsFile();
	    if (opened == null)
		return false;
	    infoAndProps.fillLocalVolumeInfo(opened, propsArea);
	} else
	{
	    final FileObject fileObj = area.getOpenedAsFileObject();
	    if (fileObj == null)
		return false;
	    infoAndProps.fillDirInfo(fileObj, propsArea);
	}
	return true;
    }

    boolean onOpenFilesWithApp(String appName, FileObject[] paths, boolean asUrls)
    {
	NullCheck.notEmpty(appName, "appName");
	NullCheck.notNullItems(paths, "paths");
	/*
	  boolean atLeastOne = false;
	  for(Path p: paths)
	  if (!Files.isDirectory(p))
	  {
	  atLeastOne = true;
	  String arg;
	  if (asUrls)
	  {
	  try {
	  arg = p.toUri().toURL().toString();
	  }
	  catch(java.net.MalformedURLException e)
	  {
	  e.printStackTrace();
	  arg = p.toString();
	  }
	  } else
	  arg = p.toString();
	  luwrain.launchApp(appName, new String[]{arg});
	  }
	  return atLeastOne;
	*/
	return false;
    }


    boolean onOpenFtp(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	final String addr = conversations.ftpAddress();
	if (addr == null)
	    return true;
	area.openLocalPath(addr);
	return true;
    }

    boolean onCopyUrls(PanelArea panelArea)
    {
	NullCheck.notNull(panelArea, "panelArea");
	final Object[] objs = panelArea.getObjectsToProcess();
	final List<URL> res = new LinkedList<URL>();
	Log.debug("proba", "" + objs.length);
	for(Object o: objs)
	{
	    if (o instanceof URL)
	    {
		res.add((URL)o);
		continue;
	    }
	    if (o instanceof File)
	    {
		res.add(Urls.toUrl((File)o));
		continue;
	    }
	}
	if (res.isEmpty())
	    return false;
	final URL[] urls = res.toArray(new URL[res.size()]);
	luwrain.getClipboard().set(urls);
	if (urls.length == 1)
	    luwrain.message(urls[0].toString(), Luwrain.MessageType.OK); else
	    luwrain.playSound(Sounds.OK);
	return true;
    }

    static boolean onOpenEvent(EnvironmentEvent event, PanelArea area)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(area, "area");
	if (!(event instanceof OpenEvent))
	    return false;
	final File f = new File(((OpenEvent)event).path());
	if (!f.isDirectory())
	    return false;
	area.openLocalPath(f.getAbsolutePath());
	return true;
    }
}
