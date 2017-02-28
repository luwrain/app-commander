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
import java.nio.file.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;
import org.luwrain.popups.*;

import org.luwrain.app.commander.Base.Side;

class CommanderApp implements Application, FilesOperation.Listener
{
    static final int NORMAL_LAYOUT_INDEX = 0;
    static final int OPERATIONS_LAYOUT_INDEX = 1;
    static final int PROPERTIES_LAYOUT_INDEX = 2;

    private Luwrain luwrain;
    private Strings strings;
    private PanelArea leftPanel;
    private PanelArea rightPanel;
    private ListArea operationsArea;
    private SimpleArea propertiesArea;
    private AreaLayoutSwitch layouts;

    private final Base base = new Base();
    private Actions actions;
    private final InfoAndProperties infoAndProps = new InfoAndProperties();

    private Path startFrom = null;

    CommanderApp()
    {
	startFrom = null;
    }

    CommanderApp(String startFrom)
    {
	NullCheck.notNull(startFrom, "startFrom");
	if (!startFrom.isEmpty())
	    this.startFrom = Paths.get(startFrom);
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o =  luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	infoAndProps.init(luwrain);
	if (startFrom == null)
	    startFrom = luwrain.getPathProperty("luwrain.dir.userhome");
	try {
	createAreas();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(AreaLayout.LEFT_RIGHT, leftPanel, rightPanel));
	layouts.add(new AreaLayout(AreaLayout.LEFT_RIGHT_BOTTOM, leftPanel, rightPanel, operationsArea));
	layouts.add(new AreaLayout(propertiesArea));
	actions = new Actions(luwrain, base, strings, layouts);
	return true;
    }

    void closeApp()
    {
	if (!base.allOperationsFinished())
	{
	    luwrain.message(strings.notAllOperationsFinished(), Luwrain.MESSAGE_ERROR);
	    return;
	}
	luwrain.closeApp();
    }

    private void createAreas() throws Exception
    {
	final PanelArea.Params leftPanelParams = PanelArea.createParams(new DefaultControlEnvironment(luwrain));
	leftPanelParams.clickHandler = (area, obj, dir)->onClick(area, obj, dir);

	final PanelArea.Params rightPanelParams = PanelArea.createParams(new DefaultControlEnvironment(luwrain));

 	leftPanel = new PanelArea(leftPanelParams) {

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (onKeyboardEventInPanel(Side.LEFT, event))
								 return true;
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    if (onEnvironmentEventInPanel(event, this, Side.LEFT))
			return true;
			return super.onEnvironmentEvent(event);
		}

		@Override public Action[] getAreaActions()
		{
return actions.getPanelAreaActions(this);
		}
	    };

 	rightPanel = new PanelArea(rightPanelParams) {

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (onKeyboardEventInPanel(Side.RIGHT, event))
			return true;
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    if (onEnvironmentEventInPanel(event, this, Side.RIGHT))
			return true;
			return super.onEnvironmentEvent(event);
		}

		@Override public Action[] getAreaActions()
		{
return actions.getPanelAreaActions(this);
		}
	    };

	leftPanel.setLoadingResultHandler((location, wrappers, selectedIndex, announce)->{
		luwrain.runInMainThread(()->leftPanel.acceptNewLocation(location, wrappers, selectedIndex, announce));
	    });

	rightPanel.setLoadingResultHandler((location, wrappers, selectedIndex, announce)->{
		luwrain.runInMainThread(()->rightPanel.acceptNewLocation(location, wrappers, selectedIndex, announce));
	    });

	leftPanel.openLocalPath("/");
	rightPanel.openLocalPath("/");

	final ListArea.Params listParams = new ListArea.Params();
	listParams.environment = new DefaultControlEnvironment(luwrain);
	listParams.model = base.getOperationsListModel();
	listParams.appearance = new OperationsAppearance(luwrain, strings, base);
	listParams.name = strings.operationsAreaName();

	operationsArea = new ListArea(listParams) {

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    gotoLeftPanel();
			    return true;
			}
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
	if (event.getType() != EnvironmentEvent.Type.REGULAR)
	    return super.onEnvironmentEvent(event);
	if (event instanceof ConfirmationEvent)
	    return onConfirmationEvent((ConfirmationEvent)event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

    private boolean onConfirmationEvent(ConfirmationEvent event)
    {
	NullCheck.notNull(event, "event");
	Log.debug("commander", "showing confirmation event for " + event.path.toString());
	final String cancel = "Прервать";
	final String overwrite = "Перезаписать";
	final String overwriteAll = "Перезаписать все";
	final String skip = "Пропустить";
	final String skipAll = "Пропустить все";
	final Object res = Popups.fixedList(luwrain, "Подтверждение перезаписи " + event.path.toString(), new String[]{overwrite, overwriteAll, skip, skipAll, cancel});
	if (res == overwrite || res == overwriteAll)
	    event.answer = FilesOperation.ConfirmationChoices.OVERWRITE; else
	    if (res == skip || res == skipAll)
		event.answer = FilesOperation.ConfirmationChoices.SKIP; else
		event.answer = FilesOperation.ConfirmationChoices.CANCEL;
	Log.debug("commander", "popup closed, answer is " + event.answer.toString());
	return true;
    }
	    };

	propertiesArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.infoAreaName()){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    return closePropertiesArea();
			}
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
    }

    private boolean onKeyboardEventInPanel(Side side, KeyboardEvent event)
		{
		    NullCheck.notNull(side, "side");
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && event.withAltOnly())
			switch(event.getSpecial())
			{
			case F1:
			    return selectPartition(Side.LEFT);
			case F2:
			    return selectPartition(Side.RIGHT);
			}
		    if (event.isSpecial()  && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return onTabInPanel(side);
			}
		    return false;
		}

    private boolean onEnvironmentEventInPanel(EnvironmentEvent event, PanelArea area, Side side)
		{
		    NullCheck.notNull(event, "event");
		    NullCheck.notNull(area, "area");
		    NullCheck.notNull(side, "side");
		    switch(event.getCode())
		    {
case OPEN:
return onOpenEvent(event, area);
		    case INTRODUCE:
			luwrain.playSound(Sounds.INTRO_REGULAR);
			luwrain.say(strings.rightPanelName() + " " + area.getAreaName());
			return true;
		    case CLOSE:
			closeApp();
			return true;
case ACTION:
			return onPanelAreaAction(event, side, area);
case PROPERTIES:
return showPropertiesArea(area);
		    default:
			return false;
}
		}

private boolean onPanelAreaAction(Event event, Side side, PanelArea area)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(side, "side");
	NullCheck.notNull(area, "area");
	if (ActionEvent.isAction(event, "edit-text"))
	    return actions.onOpenFilesWithApp("notepad", area.getFileObjectsToProcess(), false);
	if (ActionEvent.isAction(event, "size"))
	    return infoAndProps.calcSize(area.getFileObjectsToProcess());
	if (ActionEvent.isAction(event, "preview"))
	    return actions.onOpenFilesWithApp("reader", area.getFileObjectsToProcess(), true);
	if (ActionEvent.isAction(event, "hidden-show"))
	{
	    //		setFilter(new CommanderFilters.AllFiles());
	    //		refresh();
	    return true;
	}
	if (ActionEvent.isAction(event, "hidden-hide"))
	{
	    //		setFilter(new CommanderFilters.NoHidden());
	    //		refresh();
	    return true;
	}
	if (ActionEvent.isAction(event, "copy"))
	    return actions.onCopy(getPanel(side), getAnotherPanel(side), this, operationsArea);
	if (ActionEvent.isAction(event, "move"))
	    return actions.onMove(getPanel(side), getAnotherPanel(side), this, operationsArea);
	if (ActionEvent.isAction(event, "mkdir"))
	    return actions.mkdir(this, getPanel(side));
	if (ActionEvent.isAction(event, "open-ftp"))
	    return actions.onOpenFtp(area);
	return false;
    }

    private PanelArea.ClickHandler.Result onClick(NgCommanderArea area, Object obj, boolean dir)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(obj, "obj");
	if (dir)
	    return NgCommanderArea.ClickHandler.Result.OPEN_DIR;
	final PanelArea panelArea = (PanelArea)area;
	if (!panelArea.isLocalDir())
return PanelArea.ClickHandler.Result.REJECTED;
	final FileObject fileObject = (FileObject)obj;
	luwrain.openFile(fileObject.getName().getPath());
	return NgCommanderArea.ClickHandler.Result.OK;
    }

private boolean onTabInPanel(Side side)
    {
	NullCheck.notNull(side, "side");
	switch(side)
	{
	case LEFT:
	    luwrain.setActiveArea(rightPanel);
	    return true;
	case RIGHT:
	    if (base.hasOperations())
		luwrain.setActiveArea(operationsArea); else
		luwrain.setActiveArea(leftPanel);
	    return true;
	default:
	    return false;
	}
    }

    boolean selectPartition(Side side)
    {
	NullCheck.notNull(side, "side");
	org.luwrain.base.Partition part = null;
	switch(side)
	{
	case LEFT:
	    part = Popups.mountedPartitions(luwrain);
	    if (part == null)
		return true;
	    //	    leftPanel.open(part.file().toPath(), null);
	    luwrain.setActiveArea(leftPanel);
	    return true;
	case RIGHT:
	    part = Popups.mountedPartitions(luwrain);
	    if (part == null)
		return true;
	    //	    rightPanel.open(part.file().toPath(), null);
	    luwrain.setActiveArea(rightPanel);
	    return true;
	default:
	    return false;
	}
    }

    private boolean delete(Side panelSide)
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

    void refreshPanels()
    {
	leftPanel.refresh();
	rightPanel.refresh();
    }

    private PanelArea getPanel(Side side)
    {
	NullCheck.notNull(side, "side");
	switch(side)
	{
	case LEFT:
	    return leftPanel;
	case RIGHT:
	    return rightPanel;
	default:
	    throw new IllegalArgumentException("Unknown panel side");
	}
    }

    private PanelArea getAnotherPanel(Side side)
    {
	NullCheck.notNull(side, "side");
	switch(side)
	{
	case LEFT:
	    return rightPanel;
	case RIGHT:
	    return leftPanel;
	default:
	    throw new IllegalArgumentException("Unknown panel side");
	}
    }

    private boolean showPropertiesArea(PanelArea area)
    {
	/*
	NullCheck.notNull(area, "area");
	final Path[] paths = Base.entriesToProcess(area);
	if (paths.length < 1)
	    return false;
	propertiesArea.clear();
	infoAndProps.fillProperties(propertiesArea, paths);
	layouts.show(PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	*/
	return true;
    }

private boolean closePropertiesArea()
    {
	if (base.hasOperations())
	    layouts.show(OPERATIONS_LAYOUT_INDEX); else
	    layouts.show(NORMAL_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }

    private boolean onOpenEvent(EnvironmentEvent event, PanelArea area)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(area, "area");
	if (!(event instanceof OpenEvent))
	    return false;
	final Path path = Paths.get(((OpenEvent)event).path());
	if (!Files.isDirectory(path))
	    return false;
	//	area.open(path, null);
	return true;
    }

    private void onOperationUpdate(FilesOperation operation)
    {
	NullCheck.notNull(operation, "operation");
	operationsArea.refresh();
	//	luwrain.onAreaNewBackgroundSound();
    }

    void gotoLeftPanel()
    {
	luwrain.setActiveArea(leftPanel);
    }

    private void gotoRightPanel()
    {
	luwrain.setActiveArea(rightPanel);
    }

    private void gotoOperations()
    {
	luwrain.setActiveArea(operationsArea);
    }

    @Override public void onOperationProgress(FilesOperation operation)
    {
	NullCheck.notNull(operation, "operation");
	NullCheck.notNull(operation, "operation");
	luwrain.runInMainThread(()->onOperationUpdate(operation));
    }

    @Override public FilesOperation.ConfirmationChoices confirmOverwrite(Path path)
    {
	NullCheck.notNull(path, "path");
	final ConfirmationEvent event = new ConfirmationEvent(operationsArea, path);
	Log.debug("commander", "sending confirmation event for " + path.toString());
	luwrain.enqueueEvent(event);
	Log.debug("commander", "starting to wait the event to be processed for " + path.toString());
	try {
	    event.waitForBeProcessed();
	}
	catch(InterruptedException e)
	{
	    Log.debug("commander", "thread was interrupted while waiting the confirmation for " + path.toString());
	    Thread.currentThread().interrupt();
	}
	if (event.answer == null)
	    Log.warning("commander", "confirmation event for " + path.toString() + " returned with null answer"); else
	    Log.debug("commander", "the confirmation for " + path.toString() + " came:" + event.answer.toString());
	return event.answer;
    }

    @Override public AreaLayout getAreasToShow()
    {
	return layouts.getCurrentLayout();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

}
