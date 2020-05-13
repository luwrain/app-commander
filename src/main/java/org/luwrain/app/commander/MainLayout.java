
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
import org.luwrain.template.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private Luwrain luwrain = null;
    private final PanelArea leftPanel;
    private final PanelArea rightPanel;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
 	this.leftPanel = new PanelArea(createLeftPanelParams(), app.getLuwrain()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onSystemEvent(event);
		}
	    };

 	this.rightPanel = new PanelArea(createRightPanelParams(), app.getLuwrain()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onSystemEvent(event);
		}
	    };

	leftPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		luwrain.runUiSafely(()->leftPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });
	rightPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		luwrain.runUiSafely(()->rightPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });
	leftPanel.openInitial(app.startFrom);
	rightPanel.openInitial(app.startFrom);

    }

	/*
	case INTRODUCE:
	    luwrain.playSound(Sounds.INTRO_REGULAR);
	    switch(side)
	    {
	    case LEFT:
		luwrain.speak(strings.leftPanelName() + " " + panel.getAreaName());
		break;
	    case RIGHT:
		luwrain.speak(strings.rightPanelName() + " " + panel.getAreaName());
		break;
	    }
	    return true;
	*/

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
	final File dest = app.getConv().copyPopup(copyFromDir, filesToCopy, copyToDir);
	if (dest == null)
	    return true;
	final String opName = copyOperationName(filesToCopy, dest);
	app.launch(luwrain.getFilesOperations().copy(listener, opName, filesToCopy, dest));
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
	final File dest = app.getConv().movePopup(moveFromDir, filesToMove, moveTo);
	if (dest == null)
	    return true;
	final String opName = moveOperationName(filesToMove, dest);
	app.launch(luwrain.getFilesOperations().move(listener, opName, filesToMove, dest));
	return true;
    }

    boolean onLocalMkdir(App app, PanelArea area)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(area, "area");
	if (!area.isLocalDir())
	    return false;
	final File createIn = area.getOpenedAsFile();
	if (createIn == null || !createIn.isAbsolute())
	    return false;
	final File newDir = app.getConv().mkdirPopup(createIn);
	if (newDir == null)
	    return true;
	try {
	    java.nio.file.Files.createDirectories(newDir.toPath());//FIXME:
	}
	catch (IOException e)
	{
	    app.getLuwrain().message(app.getStrings().mkdirErrorMessage(luwrain.i18n().getExceptionDescr(e)), Luwrain.MessageType.ERROR);
	    return true;
	}
	app.getLuwrain().message(app.getStrings().mkdirOkMessage(newDir.getName()), Luwrain.MessageType.OK);
	area.reread(newDir.getName(), false);
	return true;
    }

    boolean onLocalDelete(PanelArea area, FilesOperation.Listener listener)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(listener, "listener");
	if (!area.isLocalDir())
	    return false;
	final File[] files = area.getFilesToProcess();
	if (files.length == 0)
	    return false;
	if (!app.getConv().deleteConfirmation(files))
	    return true;
	final String opName = app.getStrings().delOperationName(files);
	app.launch(luwrain.getFilesOperations().delete(listener, opName, files));
	return true;
    }

    boolean getFilesInfo(PanelArea area, MutableLines lines)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(lines, "lines");
	if (area.isLocalDir())
	{
	    final File[] files = area.getFilesToProcess();
	    if (files.length == 0)
		return false;
	    final InfoHook hook = new InfoHook(luwrain);
	    try {
		return hook.localFilesInfo(files, lines);
	    }
	    catch(RuntimeException e)
	    {
		luwrain.crash(e);
		return true;
	    }
	}
	return false;
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


    private CommanderArea.Params createLeftPanelParams()
    {
	return null;
    }

        private CommanderArea.Params createRightPanelParams()
    {
	return null;
    }


	

	AreaLayout getLayout()
	{
	    return new AreaLayout(AreaLayout.LEFT_RIGHT, leftPanel, rightPanel);
	}

	}
