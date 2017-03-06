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

import java.io.*;
import java.util.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

import org.luwrain.app.commander.Base.Side;

class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;
    private final AreaLayoutSwitch layouts;

    private final Conversations conversations;

    Actions(Luwrain luwrain, Base base,
Strings strings, AreaLayoutSwitch layouts)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(layouts, "layouts");
	this.luwrain = luwrain;
	this.base = base;
	this.strings = strings;
	this.layouts = layouts;
	this.conversations = new Conversations(luwrain, strings);
    }

    Action[] getPanelAreaActions(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	final FileObject[] toProcess = area.getFileObjectsToProcess();
	if (toProcess.length < 1)
	    return new Action[]{
		new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
		new Action("open-ftp", "Подключиться к FTP-серверу"), 
		new Action("volume-info", "Показать информацию о разделе", new KeyboardEvent(KeyboardEvent.Special.F10)), 
		new Action("hidden-show", strings.actionHiddenShow()), 
		new Action("hidden-hide", strings.actionHiddenHide()), 
	    };
	return new Action[]{

	    new Action("copy", strings.actionCopy(), new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("move", strings.actionMove(), new KeyboardEvent(KeyboardEvent.Special.F6)),
	    new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
	    new Action("delete", strings.actionDelete(), new KeyboardEvent(KeyboardEvent.Special.F8)),
	    new Action("open", strings.actionOpen()),
	    new Action("size", strings.actionSize(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.ALT))),
	    new Action("preview", strings.actionPreview(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("play", strings.actionPlay(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("edit-text", strings.actionEditAsText(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("preview-another-format", strings.actionPreviewAnotherFormat()),
	    new Action("open-choosing-app", strings.actionOpenChoosingApp()),
	    new Action("copy-to-clipboard", strings.actionCopyToClipboard(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.ALT))),
	    new Action("open-ftp", "Подключиться к FTP-серверу"), 
		new Action("volume-info", "Показать информацию о разделе", new KeyboardEvent(KeyboardEvent.Special.F10)), 
	    new Action("hidden-show", strings.actionHiddenShow()), 
	    new Action("hidden-hide", strings.actionHiddenHide()), 
	};
    }

PanelArea.ClickHandler.Result onClick(CommanderArea area, Object obj, boolean dir)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(obj, "obj");
	if (dir)
	    return CommanderArea.ClickHandler.Result.OPEN_DIR;
	final PanelArea panelArea = (PanelArea)area;
	if (!panelArea.isLocalDir())
return PanelArea.ClickHandler.Result.REJECTED;
	final FileObject fileObject = (FileObject)obj;
	luwrain.openFile(fileObject.getName().getPath());
	return CommanderArea.ClickHandler.Result.OK;
    }

    boolean showFileObjectsProperties(InfoAndProperties infoAndProps,
			       PanelArea area, SimpleArea propertiesArea)
    {
	NullCheck.notNull(area, "area");
	final FileObject[] paths = area.getFileObjectsToProcess();
	if (paths.length < 1)
	    return false;
	propertiesArea.clear();
	//	infoAndProps.fillProperties(propertiesArea, paths);
	layouts.show(CommanderApp.PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }

    boolean showVolumeInfo(InfoAndProperties infoAndProps,
			       PanelArea area, SimpleArea propertiesArea)
    {
	NullCheck.notNull(area, "area");
	//	final FileObject[] paths = area.getFileObjectsToProcess();
	//	if (paths.length < 1)
	//	    return false;
	propertiesArea.clear();
	//	infoAndProps.fillProperties(propertiesArea, paths);
	layouts.show(CommanderApp.PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
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

    boolean onLocalCopy(PanelArea copyFromArea, PanelArea copyToArea, FilesOperation.Listener listener, ListArea area/*, AreaLayoutSwitch layouts*/)
    {
	NullCheck.notNull(copyFromArea, "copyFromArea");
	NullCheck.notNull(copyToArea, "copyToArea");
	NullCheck.notNull(listener, "listener");
	NullCheck.notNull(area, "area");
	if (!copyFromArea.isLocalDir() || !copyToArea.isLocalDir())
	    return false;
	final File copyFromDir = copyFromArea.getOpenedAsFile();
	final File[] filesToCopy = copyFromArea.getFilesToProcess();
	final File copyTo = copyToArea.getOpenedAsFile();
	if (filesToCopy.length < 1)
	    return false;
	final File dest = conversations.copyPopup(copyFromDir, filesToCopy, copyTo);
	if (dest == null)
	    return true;
base.launch(luwrain.getFilesOperations().copy(listener, 
conversations.copyOperationName(filesToCopy, dest), filesToCopy, dest));
	area.refresh();
	layouts.show(CommanderApp.OPERATIONS_LAYOUT_INDEX);
	return true;
    }

    boolean onLocalMove(PanelArea moveFromArea, PanelArea moveToArea, 
FilesOperation.Listener listener, ListArea area)
    {
	NullCheck.notNull(moveFromArea, "moveFromArea");
	NullCheck.notNull(moveToArea, "moveToArea");
	NullCheck.notNull(listener, "listener");
	NullCheck.notNull(area, "area");
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
	area.refresh();
	layouts.show(CommanderApp.OPERATIONS_LAYOUT_INDEX);
	return true;
    }

    boolean mkdir(CommanderApp app, PanelArea area)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(area, "area");
	final File createIn = area.getOpenedAsFile();
	if (createIn == null)
	    return false;

	final File newDir = conversations.mkdirPopup(createIn);
	if (newDir == null)
	    return true;
	try {
	    java.nio.file.Files.createDirectories(newDir.toPath());
	}
	catch (IOException e)
	{
	    luwrain.message(strings.mkdirErrorMessage(luwrain.i18n().getExceptionDescr(e)), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	luwrain.message(strings.mkdirOkMessage(newDir.getName()), Luwrain.MESSAGE_OK);
	Log.debug("mkdir", "rereading for " + newDir.getName());
	area.reread(newDir.getName(), false);
	return true;
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


    private boolean ddelete(Side panelSide)
    {
	/*
	  File[] filesToDelete = panelSide == PanelArea.Side.LEFT?leftPanel.selectedAsFiles():rightPanel.selectedAsFiles();
	  if (filesToDelete == null || filesToDelete.length < 1)
	  return false;
	  YesNoPopup popup = new YesNoPopup(luwrain, strings.delPopupName(),
	  strings.delPopupText(filesToDelete), false);
	  luwrain.popup(popup);
	  if (popup.closing.cancelled())
	  return true;
	  if (!popup.result())
	  return true;
	  operations.launch(Operations.delete(operations, strings.delOperationName(filesToDelete), 
	  filesToDelete));
	*/
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
