/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

import org.luwrain.app.commander.Base.Side;
import org.luwrain.app.commander.operations.Operation;

class CommanderApp implements Application, org.luwrain.app.commander.operations.Listener
{
    static final int NORMAL_LAYOUT_INDEX = 0;
    static final int OPERATIONS_LAYOUT_INDEX = 1;
    static final int PROPERTIES_LAYOUT_INDEX = 2;

    private Luwrain luwrain;
    private Strings strings;
    private CommanderArea leftPanel;
    private CommanderArea rightPanel;
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
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(AreaLayout.LEFT_RIGHT, leftPanel, rightPanel));
	layouts.add(new AreaLayout(AreaLayout.LEFT_RIGHT_BOTTOM, leftPanel, rightPanel, operationsArea));
	layouts.add(new AreaLayout(propertiesArea));
	actions = new Actions(luwrain, strings, layouts);
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

    /*
    Settings settings()
    {
	return base.getSettings();
    }
    */

    private void createAreas()
    {
	final CommanderArea.Params params = new CommanderArea.Params();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.selecting = true;
	params.filter = new CommanderUtils.NoHiddenFilter();
	params.comparator = new CommanderUtils.ByNameComparator();
	params.clickHandler = (area, path, dir)->onClick(area, path, dir);
	params.appearance = new CommanderUtils.DefaultAppearance(params.environment);

 	leftPanel = new CommanderArea(params, startFrom) {

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (onKeyboardEventInPanel(Side.LEFT, event))
								 return true;
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    switch(event.getCode())
		    {
		    case OPEN:
			return onOpenEvent(event, this);
		    case INTRODUCE:
			luwrain.playSound(Sounds.INTRO_REGULAR);
			luwrain.say(strings.leftPanelName() + " " + getAreaName());
			return true;
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			return onPanelAreaAction(event, Side.LEFT, this);
		    case PROPERTIES:
			return actions.showPropertiesArea(infoAndProps, this, propertiesArea);
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return Actions.getPanelAreaActions(strings, this);
		}
	    };

	params.environment = new DefaultControlEnvironment(luwrain);
	params.selecting = true;
	params.filter = new CommanderUtils.NoHiddenFilter();
	params.comparator = new CommanderUtils.ByNameComparator();
	params.clickHandler = (area, path, dir)->onClick(area, path, dir);
	params.appearance = new CommanderUtils.DefaultAppearance(params.environment);

 	rightPanel = new CommanderArea(params, startFrom) {

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (onKeyboardEventInPanel(Side.RIGHT, event))
			return true;
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    switch(event.getCode())
		    {
		    case OPEN:
			return onOpenEvent(event, this);
		    case INTRODUCE:
			luwrain.playSound(Sounds.INTRO_REGULAR);
			luwrain.say(strings.rightPanelName() + " " + getAreaName());
			return true;
		    case CLOSE:
			closeApp();
			return true;
		    case ACTION:
			return onPanelAreaAction(event, Side.RIGHT, this);
		    case PROPERTIES:
			return showPropertiesArea(this);
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		@Override public Action[] getAreaActions()
		{
		    return Actions.getPanelAreaActions(strings, this);
		}
	    };

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
	Log.debug("commander", "showing confirmation event for " + event.getPath().toString());
	final String cancel = "Прервать";
	final String overwrite = "Перезаписать";
	final String overwriteAll = "Перезаписать все";
	final String skip = "Пропустить";
	final String skipAll = "Пропустить все";
	final Object res = Popups.fixedList(luwrain, "Подтверждение перезаписи " + event.getPath().toString(), new String[]{overwrite, overwriteAll, skip, skipAll, cancel});
	if (res == overwrite || res == overwriteAll)
	    event.setAnswer(ConfirmationChoices.OVERWRITE); else
	    if (res == skip || res == skipAll)
		event.setAnswer(ConfirmationChoices.SKIP); else
		event.setAnswer(ConfirmationChoices.CANCEL);
	Log.debug("commander", "popup closed, answer is " + event.getAnswer().toString());
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
		    if (!event.isSpecial()  && !event.isModified())
			switch(Character.toLowerCase(event.getChar()))
			{
			case 'w':
			    return base.copyToClipboard(getPanel(side));
			}
		    if (event.isSpecial()  && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    return onTabInPanel(side);
			}
		    return false;
		}

private boolean onPanelAreaAction(Event event, Side side, CommanderArea area)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(side, "side");
	NullCheck.notNull(area, "area");
	if (ActionEvent.isAction(event, "edit-text"))
	    return actions.onOpenFilesWithApp("notepad", Base.entriesToProcess(area), false);
	if (ActionEvent.isAction(event, "size"))
	    return infoAndProps.calcSize(Base.entriesToProcess(area));

	if (ActionEvent.isAction(event, "preview"))
	    return actions.onOpenFilesWithApp("reader", Base.entriesToProcess(area), true);
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
	    return actions.onCopy(getPanel(side), getAnotherPanel(side), 
				base, this, operationsArea, layouts);
	if (ActionEvent.isAction(event, "move"))
	    return actions.onMove(getPanel(side), getAnotherPanel(side), 
				  base, this, operationsArea, layouts);
	if (ActionEvent.isAction(event, "mkdir"))
	    return actions.mkdir(this, getPanel(side));
	return false;
    }

    private CommanderArea.ClickHandler.Result onClick(CommanderArea area, Path path, boolean dir)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(path, "path");
	if (dir)
	    return CommanderArea.ClickHandler.Result.OPEN_DIR;
	luwrain.openFile(path.toString());
	return CommanderArea.ClickHandler.Result.OK;
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
	org.luwrain.hardware.Partition part = null;
	switch(side)
	{
	case LEFT:
	    part = Popups.mountedPartitions(luwrain);
	    if (part == null)
		return true;
	    leftPanel.open(part.file().toPath(), null);
	    luwrain.setActiveArea(leftPanel);
	    return true;
	case RIGHT:
	    part = Popups.mountedPartitions(luwrain);
	    if (part == null)
		return true;
	    rightPanel.open(part.file().toPath(), null);
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

    private CommanderArea getPanel(Side side)
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

    private CommanderArea getAnotherPanel(Side side)
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

    private boolean showPropertiesArea(CommanderArea area)
    {
	NullCheck.notNull(area, "area");
	final Path[] paths = Base.entriesToProcess(area);
	if (paths.length < 1)
	    return false;
	propertiesArea.clear();
	infoAndProps.fillProperties(propertiesArea, paths);
	layouts.show(PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
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

    private boolean onOpenEvent(EnvironmentEvent event, CommanderArea area)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(area, "area");
	if (!(event instanceof OpenEvent))
	    return false;
	final Path path = Paths.get(((OpenEvent)event).path());
	if (!Files.isDirectory(path))
	    return false;
	area.open(path, null);
	return true;
    }

    private void onOperationUpdate(Operation operation)
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

    @Override public void onOperationProgress(Operation operation)
    {
	NullCheck.notNull(operation, "operation");
	luwrain.runInMainThread(()->onOperationUpdate(operation));
    }

    @Override public ConfirmationChoices confirmOverwrite(Path path)
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
	if (event.getAnswer() == null)
	    Log.warning("commander", "confirmation event for " + path.toString() + " returned with null answer"); else
	    Log.debug("commander", "the confirmation for " + path.toString() + " came:" + event.getAnswer().toString());
	return event.getAnswer();
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
