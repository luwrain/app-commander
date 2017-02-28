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

class Actions
{
    private final Luwrain luwrain;
    private final Base base;
    private final Strings strings;
    private final AreaLayoutSwitch layouts;

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
    }

    Action[] getPanelAreaActions(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	final FileObject[] toProcess = area.getFileObjectsToProcess();
	if (toProcess.length < 1)
	    return new Action[]{
		new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
		new Action("hidden-show", strings.actionHiddenShow()), 
		new Action("hidden-hide", strings.actionHiddenHide()), 

		new Action("open-ftp", "Подключиться к FTP-серверу"), 
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

	    new Action("hidden-show", strings.actionHiddenShow()), 
	    new Action("hidden-hide", strings.actionHiddenHide()), 
	};
    }

    boolean showPropertiesArea(InfoAndProperties infoAndProps,
			       PanelArea area, SimpleArea propertiesArea)
    {
	NullCheck.notNull(area, "area");
	/*
	final Path[] paths = Base.entriesToProcess(area);
	if (paths.length < 1)
	    return false;
	propertiesArea.clear();
	infoAndProps.fillProperties(propertiesArea, paths);
	layouts.show(CommanderApp.PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	*/
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

    boolean onCopy(PanelArea copyFromArea, PanelArea copyToArea, FilesOperation.Listener listener, ListArea area/*, AreaLayoutSwitch layouts*/)
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
	final java.nio.file.Path destPath = Popups.path(luwrain,
				      strings.copyPopupName(), copyPopupPrefix(filesToCopy),
							copyTo.toPath(), copyFromDir.toPath(),
				      (path)->{
					  NullCheck.notNull(path, "path");
					  return true;
				      });
	if (destPath == null)
	    return true;
	final File dest = destPath.toFile();
base.launch(luwrain.getFilesOperations().copy(listener, copyOperationName(filesToCopy, dest), filesToCopy, dest));
	area.refresh();
	layouts.show(CommanderApp.OPERATIONS_LAYOUT_INDEX);
	return true;
    }

    boolean onMove(PanelArea moveFromArea, PanelArea moveToArea, 
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
	final java.nio.file.Path destPath = Popups.path(luwrain,
				      strings.movePopupName(), movePopupPrefix(filesToMove),
					  moveTo.toPath(), moveFromDir.toPath(),
				      (path)->{
					  NullCheck.notNull(path, "path");
					  return true;
				      });
	if (destPath == null)
	    return true;
	final File dest = destPath.toFile();
base.launch(luwrain.getFilesOperations().move(listener, moveOperationName(filesToMove, dest), filesToMove, dest));
	area.refresh();
	layouts.show(CommanderApp.OPERATIONS_LAYOUT_INDEX);
	return true;
    }

    boolean mkdir(CommanderApp app, PanelArea area)
    {
	/*
	NullCheck.notNull(app, "app");
	NullCheck.notNull(area, "area");
	final Path createIn = area.opened();
	if (createIn == null)
	    return false;
	final Path p = Popups.path(luwrain,
				   strings.mkdirPopupName(), strings.mkdirPopupPrefix(), createIn, (path)->{
				       NullCheck.notNull(path, "path");
				       if (Files.exists(path))
				       {
					   luwrain.message(strings.enteredPathExists(path.toString()), Luwrain.MESSAGE_ERROR);
					   return false;
				       }
				       return true;
				   });
	if (p == null)
	    return true;
	try {
	    Files.createDirectories(p);
	}
	catch (IOException e)
	{
	    luwrain.message(strings.mkdirErrorMessage(luwrain.i18n().getExceptionDescr(e)), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	luwrain.message(strings.mkdirOkMessage(p.getFileName().toString()), Luwrain.MESSAGE_OK);
	app.refreshPanels();
	area.select(p, false);
	*/
	return true;
    }

    boolean onOpenFtp(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	return area.openLocalPath("ftp://ftp.altlinux.org");
    }

    private String copyPopupPrefix(File[] toCopy)
	{
	    return strings.copyPopupPrefix(toCopy.length > 1?luwrain.i18n().getNumberStr(toCopy.length, "items"):toCopy[0].getName());
	}

    private String movePopupPrefix(File[] toMove)
	{
	    return strings.movePopupPrefix(toMove.length > 1?luwrain.i18n().getNumberStr(toMove.length, "items"):toMove[0].getName());
	}

    private String copyOperationName(File[] whatToCopy, File copyTo)
    {
	if (whatToCopy.length < 1)
	    return "";
	if (whatToCopy.length > 1)
	    return strings.copyOperationName(whatToCopy[0].getName() + ",...", copyTo.getName());
	return strings.copyOperationName(whatToCopy[0].getName(), copyTo.getName());
    }

    private String moveOperationName(File[] whatToMove, File moveTo)
    {
	if (whatToMove.length < 1)
	    return "";
	if (whatToMove.length > 1)
	    return strings.moveOperationName(whatToMove[0].getName() + ",...", moveTo.getName());
	return strings.moveOperationName(whatToMove[0].getName(), moveTo.getName());
    }

}
