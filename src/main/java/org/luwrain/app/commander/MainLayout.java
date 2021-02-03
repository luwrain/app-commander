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
import java.io.*;
import java.net.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;
import org.luwrain.popups.*;

import org.luwrain.app.commander.App.Side;
import org.luwrain.app.base.*;
import org.luwrain.app.commander.fileops.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private final PanelArea leftPanel;
    private final PanelArea rightPanel;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
 	this.leftPanel = new PanelArea(createPanelParams(), app.getLuwrain()) {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case INTRODUCE:
			    return announcePanel(Side.LEFT);
			case PROPERTIES:
			    return showFilesInfo(this);
			}
		    if (app.onSystemEvent(this, event, getPanelActions(Side.LEFT)))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return getPanelActions(Side.LEFT).getAreaActions();
		}
	    };
 	this.rightPanel = new PanelArea(createPanelParams(), app.getLuwrain()) {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case INTRODUCE:
			    return announcePanel(Side.RIGHT);
			    			case PROPERTIES:
			    return showFilesInfo(this);
			}
		    if (app.onSystemEvent(this, event, getPanelActions(Side.RIGHT)))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return getPanelActions(Side.RIGHT).getAreaActions();
		}
	    };
	leftPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		app.getLuwrain().runUiSafely(()->leftPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });
	rightPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		app.getLuwrain().runUiSafely(()->rightPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });
	if (app.startFrom != null)
	{
	    leftPanel.openInitial(app.startFrom);
	    rightPanel.openInitial(app.startFrom);
	} else
	{
	    final String location = app.getLuwrain().getProperty("luwrain.dir.userhome");
	    	    leftPanel.openInitial(location);
	    rightPanel.openInitial(location);
	}
    }

    private Actions getPanelActions(Side side)
    {
	NullCheck.notNull(side, "side");
	final PanelArea panelArea;
	final PanelArea oppositePanelArea;
	if (side == Side.LEFT)
	{
	    panelArea = leftPanel;
	    oppositePanelArea = rightPanel;
	} else
	{
	    panelArea = rightPanel;
	    oppositePanelArea = leftPanel;
	}
	return actions(
		       action("mkdir", app.getStrings().actionMkdir(), new InputEvent(InputEvent.Special.F7), ()->actLocalMkdir(panelArea)),
		       action("left-panel-volume", app.getStrings().leftPanelVolume(), new InputEvent(InputEvent.Special.F1, EnumSet.of(InputEvent.Modifiers.ALT)), ()->actPanelVolume(leftPanel)),
		       action("right-panel-volume", app.getStrings().rightPanelVolume(), new InputEvent(InputEvent.Special.F2, EnumSet.of(InputEvent.Modifiers.ALT)), ()->actPanelVolume(rightPanel)),
		       action("info", "info", ()->showFilesInfo(panelArea))
		       );
    }

    private boolean announcePanel(Side side)
    {
	NullCheck.notNull(side, "side");
	app.getLuwrain().playSound(Sounds.INTRO_REGULAR);
	    switch(side)
	    {
	    case LEFT:
		app.getLuwrain().speak(app.getStrings().leftPanelName() + " " + app.getLuwrain().getSpeakableText(leftPanel.getAreaName(), Luwrain.SpeakableTextType.PROGRAMMING));
		return true;
	    case RIGHT:
		app.getLuwrain().speak(app.getStrings().rightPanelName() + " " + app.getLuwrain().getSpeakableText(rightPanel.getAreaName(), Luwrain.SpeakableTextType.PROGRAMMING));
		return true;
	    }
	    return false;
    }

        private PanelArea.ClickHandler.Result onClick(CommanderArea area, Object obj, boolean dir)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(obj, "obj");
	if (dir)
	    return PanelArea.ClickHandler.Result.OPEN_DIR;
	final PanelArea panelArea = (PanelArea)area;
	if (!panelArea.isLocalDir())//FIXME:
	    return PanelArea.ClickHandler.Result.REJECTED;
	try {
	    final FileObject fileObject = (FileObject)obj;
	    final File file = org.luwrain.util.Urls.toFile(fileObject.getURL());
	    app.getLuwrain().openFile(file.getAbsolutePath());
	    return CommanderArea.ClickHandler.Result.OK;
	}
	catch(Exception e)
	{
	    app.getLuwrain().crash(e);
	    return PanelArea.ClickHandler.Result.REJECTED;
	}
    }

    private boolean todoLocalCopy(PanelArea copyFromArea, PanelArea copyToArea, OperationListener listener)
    {
	NullCheck.notNull(copyFromArea, "copyFromArea");
	NullCheck.notNull(copyToArea, "copyToArea");
	NullCheck.notNull(listener, "listener");
	if (!copyFromArea.isLocalDir() || !copyToArea.isLocalDir())
	    return false;
	final File copyFromDir = copyFromArea.getOpenedAsFile();
	if (copyFromDir == null || !copyFromDir.isAbsolute())
	    return false;
	final File[] filesToCopy = PanelArea.asFile(copyFromArea.getToProcess());
	if (filesToCopy.length == 0)
	    return false;
	final File copyToDir = copyToArea.getOpenedAsFile();
	if (copyToDir == null || !copyToDir.isAbsolute())
	    return false;
	final File dest = app.getConv().copyPopup(copyFromDir, filesToCopy, copyToDir);
	if (dest == null)
	    return true;
	final String opName = copyOperationName(filesToCopy, dest);
	//app.launch(app.getLuwrain().getFilesOperations().copy(listener, opName, filesToCopy, dest));
	return true;
    }

    private boolean todoLocalMove(PanelArea moveFromArea, PanelArea moveToArea, OperationListener listener)
    {
	NullCheck.notNull(moveFromArea, "moveFromArea");
	NullCheck.notNull(moveToArea, "moveToArea");
	NullCheck.notNull(listener, "listener");
	if (!moveFromArea.isLocalDir() || !moveToArea.isLocalDir())
	    return false;
	final File moveFromDir = moveFromArea.getOpenedAsFile();
	final File[] filesToMove = PanelArea.asFile(moveFromArea.getToProcess());
	final File moveTo = moveToArea.getOpenedAsFile();
	if (filesToMove.length < 1)
	    return false;
	final File dest = app.getConv().movePopup(moveFromDir, filesToMove, moveTo);
	if (dest == null)
	    return true;
	final String opName = moveOperationName(filesToMove, dest);
	//app.launch(app.getLuwrain().getFilesOperations().move(listener, opName, filesToMove, dest));
	return true;
    }

    private boolean actLocalMkdir(PanelArea panelArea)
    {
	NullCheck.notNull(panelArea, "panelArea");
	if (!panelArea.isLocalDir())
	    return false;
	final File createIn = panelArea.getOpenedAsFile();
	if (createIn == null || !createIn.isAbsolute())
	    return false;
	final File newDir = app.getConv().mkdirPopup(createIn);
	if (newDir == null)
	    return true;
	try {
	    java.nio.file.Files.createDirectories(newDir.toPath());
	}
	catch (IOException e)
	{
	    app.getLuwrain().crash(e);
	    return true;
	}
	app.getLuwrain().message(app.getStrings().mkdirOkMessage(newDir.getName()), Luwrain.MessageType.OK);
	panelArea.reread(newDir.getName(), false);
	return true;
    }

    boolean onLocalDelete(PanelArea area, OperationListener listener)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(listener, "listener");
	if (!area.isLocalDir())
	    return false;
	final File[] files = PanelArea.asFile(area.getToProcess());
	if (files.length == 0)
	    return false;
	if (!app.getConv().deleteConfirmation(files))
	    return true;
	final String opName = app.getStrings().delOperationName(files);
	//app.launch(app.getLuwrain().getFilesOperations().delete(listener, opName, files));
	return true;
    }

    private boolean actPanelVolume(PanelArea panelArea)
    {
	NullCheck.notNull(panelArea, "panelArea");
	final File res = panelArea == leftPanel?app.getConv().leftPanelVolume():app.getConv().rightPanelVolume();
	return true;
    }

     private boolean showFilesInfo(PanelArea panelArea)
    {
	NullCheck.notNull(panelArea, "panelArea");

		    final FilesInfoLayout info = new FilesInfoLayout(app, new File[0], ()->app.layout(getLayout(), panelArea));
	    app.layout(info.getLayout());
	    return true;

	    
	/*
	if (panelArea.isLocalDir())
	{
	    final File[] files = panelArea.getFilesToProcess();
	    if (files.length == 0)
		return false;
	    final MutableLinesImpl lines = new MutableLinesImpl();
	    try {
		if (!app.getHooks().localFilesInfo(files, lines))
		    return false;
	    }
	    catch(RuntimeException e)
	    {
		app.getLuwrain().crash(e);
		return true;
	    }
	}
	*/
    }

    boolean showVolumeInfo(PanelArea area, SimpleArea propsArea)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(propsArea, "propsArea");
	if (area.isLocalDir())
	{
	    final File opened = area.getOpenedAsFile();
	    if (opened == null)
		return false;
	    //	    infoAndProps.fillLocalVolumeInfo(opened, propsArea);
	} else
	{
	    final FileObject fileObj = area.getOpenedAsFileObject();
	    if (fileObj == null)
		return false;
	    //	    infoAndProps.fillDirInfo(fileObj, propsArea);
	}
	return true;
    }

    boolean onOpenFtp(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	final String addr = app.getConv().ftpAddress();
	if (addr == null)
	    return true;
	area.openLocalPath(addr);
	return true;
    }

    boolean onCopyUrls(PanelArea panelArea)
    {
	/*
	NullCheck.notNull(panelArea, "panelArea");
	final Object[] objs = panelArea.getNativeObjectsToProcess();
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
	*/
	return true;
    }

    private String copyOperationName(File[] whatToCopy, File copyTo)
    {
	if (whatToCopy.length < 1)
	    return "";
	if (whatToCopy.length > 1)
	    return app.getStrings().copyOperationName(whatToCopy[0].getName() + ",...", copyTo.getName());
	return app.getStrings().copyOperationName(whatToCopy[0].getName(), copyTo.getName());
    }

private String moveOperationName(File[] whatToMove, File moveTo)
    {
	if (whatToMove.length < 1)
	    return "";
	if (whatToMove.length > 1)
	    return app.getStrings().moveOperationName(whatToMove[0].getName() + ",...", moveTo.getName());
	return app.getStrings().moveOperationName(whatToMove[0].getName(), moveTo.getName());
    }

        private CommanderArea.Params createPanelParams()
    {
	try {
	final CommanderArea.Params params = PanelArea.createParams(app.getLuwrain());
	params.clickHandler = this::onClick;
		return params;
	}
	catch(Exception e)
	{
	    throw new RuntimeException(e);
	}
	    }

	AreaLayout getLayout()
	{
	    return new AreaLayout(AreaLayout.LEFT_RIGHT, leftPanel, rightPanel);
	}

	}
