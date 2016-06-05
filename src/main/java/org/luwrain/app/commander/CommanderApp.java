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

class CommanderApp implements Application, Actions
{
    static private final String STRINGS_NAME = "luwrain.commander";

    static private final int NORMAL_LAYOUT_INDEX = 0;
    static private final int OPERATIONS_LAYOUT_INDEX = 1;
    static private final int INFO_LAYOUT_INDEX = 2;

    private Luwrain luwrain;
    private final Base base = new Base();
    private Strings strings;
    private CommanderArea leftPanel;
    private CommanderArea rightPanel;
    private OperationsArea operationsArea;
    private SimpleArea infoArea;
    private AreaLayoutSwitch layouts;
    private Path startFrom;

    CommanderApp()
    {
	startFrom = null;
    }

    CommanderApp(String arg)
    {
	NullCheck.notNull(arg, "arg");
	if (!arg.isEmpty())
	    this.startFrom = Paths.get(arg); else
	    this.startFrom = null;
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o =  luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(AreaLayout.LEFT_RIGHT, leftPanel, rightPanel));
	layouts.add(new AreaLayout(AreaLayout.LEFT_RIGHT_BOTTOM, leftPanel, rightPanel, operationsArea));
	layouts.add(new AreaLayout(infoArea));
	return true;
    }

    private void createAreas()
    {
	final Actions actions = this;

	final CommanderArea.CommanderParams params = new CommanderArea.CommanderParams();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.selecting = true;
	params.filter = new CommanderUtils.NoHiddenFilter();
	params.comparator = new CommanderUtils.ByNameComparator();
	//	params.clickHandler = null;
	params.appearance = new CommanderUtils.DefaultAppearance(params.environment);
    
	//	leftPanel = new CommanderArea(params, startFrom);

 	leftPanel = new CommanderArea(params, startFrom) {
    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
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
		return onTabInPanel(Side.LEFT);
	    }
	return super.onKeyboardEvent(event);
    }
    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	switch(event.getCode())
	{
	    /*
	case OPEN:
	    if (event instanceof OpenEvent)
	    {
		final Path path = Paths.get(((OpenEvent)event).path());
		if (Files.isDirectory(path))
		{
		    open(path, null);
		    return true;
		}
	    }
	    return false;
	case INTRODUCE:
	    luwrain.playSound(Sounds.INTRO_REGULAR);
	    switch (side)
	    {
	    case LEFT:
		luwrain.say(strings.leftPanelName() + " " + getAreaName());
		break;
	    case RIGHT:
		luwrain.say(strings.rightPanelName() + " " + getAreaName());
		break;
	    }
	    return true;
	    */
	case CLOSE:
closeApp();
	    return true;
	case ACTION:
	    return onPanelAreaAction(event, Side.LEFT, selected());
	default:
	    return super.onEnvironmentEvent(event);
	}
    }
    @Override public Action[] getAreaActions()
    {
	return getPanelAreaActions(selected());
    }
	    };
	/*
	leftPanel = new PanelArea(luwrain, this, strings, 
				  startFrom, PanelArea.Side.LEFT);
	rightPanel = new PanelArea(luwrain, this, strings, 
				   startFrom, PanelArea.Side.RIGHT);
	operationsArea = new OperationArea(luwrain, this, strings);
	*/

	infoArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.infoAreaName()){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
closeApp();
			return true;
		    case ACTION:
			if (ActionEvent.isAction(event, "close-info"))
closeInfoArea();
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    //		    return getInfoAreaActions();
		    return new Action[0];
		}
	    };
    }

    @Override public Action[] getPanelAreaActions(Path[] selected)
    {
	NullCheck.notNullItems(selected, "selected");
	if (selected.length < 1)
	    return new Action[]{
		new Action("hidden-show", strings.panelActionTitle("hidden-show", false)), 
		new Action("hidden-hide", strings.panelActionTitle("hidden-hide", false)), 
	    };
	return new Action[]{
	    new Action("open", strings.panelActionTitle("open", false)),
	    new Action("edit-text", strings.panelActionTitle("edit-as-text", false)),
	    new Action("preview", strings.panelActionTitle("preview", false)),
	    new Action("preview-another-format", strings.panelActionTitle("preview-another-format", false)),
	    new Action("open-choosing-app", strings.panelActionTitle("open-choosing-app", false)),
	    new Action("copy", strings.panelActionTitle("copy", false), new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("move", strings.panelActionTitle("move", false), new KeyboardEvent(KeyboardEvent.Special.F6)),
	    new Action("mkdir", strings.panelActionTitle("mkdir", false), new KeyboardEvent(KeyboardEvent.Special.F7)),
	    new Action("delete", strings.panelActionTitle("delete", false), new KeyboardEvent(KeyboardEvent.Special.F8)),
	    new Action("hidden-show", strings.panelActionTitle("hidden-show", false)), 
	    new Action("hidden-hide", strings.panelActionTitle("hidden-hide", false)), 
	    new Action("size", strings.panelActionTitle("size", false), new KeyboardEvent('?')),
	    new Action("info", strings.panelActionTitle("info", selected.length > 1)),
	};
    }

private boolean onPanelAreaAction(Event event, Side side, Path[] selected)
    {
	if (ActionEvent.isAction(event, "preview"))
	    return openReader(side);
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
	if (ActionEvent.isAction(event, "info"))
	    return showInfoArea(selected);
	if (ActionEvent.isAction(event, "copy"))
	    return copy(side);
	if (ActionEvent.isAction(event, "move"))
	    return move(side);
	if (ActionEvent.isAction(event, "mkdir"))
	    return mkdir(side);
	if (ActionEvent.isAction(event, "size"))
	    return calcSize(selected);
	return false;
    }

    @Override public boolean onClickInPanel(Path[] selected)
    {
	NullCheck.notNullItems(selected, "selected");
	//FIXME:
	return false;
    }

private boolean onTabInPanel(Side side)
    {
	NullCheck.notNull(side, "side");
	//FIXME:
	return false;
    }

@Override public boolean selectPartition(Side side)
    {
	NullCheck.notNull(side, "side");
	File f = null;
	switch(side)
	{
	case LEFT:
	    f = Popups.mountedPartitionsAsFile(luwrain);
	    if (f == null)
		return true;
	    leftPanel.open(f.toPath(), null);
	    luwrain.setActiveArea(leftPanel);
	    return true;
	case RIGHT:
	    f = Popups.mountedPartitionsAsFile(luwrain);
	    if (f == null)
		return true;
	    rightPanel.open(f.toPath(), null);
	    luwrain.setActiveArea(rightPanel);
	    return true;
	default:
	    return false;
	}
    }

    private boolean openReader(Side panelSide)
    {
	/*
	  File[] files = null;
	  switch(panelSide)
	  {
	  case LEFT:
	  files = leftPanel.selectedAsFiles();
	  break;
	  case RIGHT:
	  files = rightPanel.selectedAsFiles();
	  break;
	  default:
	  return false;
	  }
	  if (files == null || files.length < 1)
	  return false;
	  base.openReader(files);
	*/
	return true;
    }

    private boolean copy(Side panelSide)
    {
	NullCheck.notNull(panelSide, "panelSide");
	final CommanderArea fromPanel = getPanel(panelSide);
	final CommanderArea toPanel = getAnotherPanel(panelSide);
	final Path copyFromDir = fromPanel.opened();
	final Path[] filesToCopy = fromPanel.selected();
	final Path copyTo = toPanel.opened();
	if (filesToCopy == null || filesToCopy.length < 1|| 
	    copyFromDir == null || copyTo == null)
	    return false;
	base.copy(operationsArea, copyFromDir, filesToCopy, copyTo);
	return true;
    }

    private boolean move(Side panelSide)
    {
	return false;
	/*
	File[] filesToMove = null;
	File moveTo = null;
	switch(panelSide)
	{
	case LEFT:
	    filesToMove = leftPanel.selectedAsFiles();
	    moveTo= rightPanel.openedAsFile(); 
	    break;
	case RIGHT:
	    filesToMove = rightPanel.selectedAsFiles();
	    moveTo= leftPanel.openedAsFile(); 
	    break;
	default:
	    return false;
	}
	if (filesToMove == null || filesToMove.length < 1|| moveTo == null)
	    return false;
	moveTo = Popups.file(luwrain,
			     strings.movePopupName(),
			     strings.movePopupPrefix(filesToMove),
			     moveTo,
			     FilePopup.ANY, 0);
	if (moveTo == null)
	    return true;
 	operations.launch(new Move(operations, strings.moveOperationName(filesToMove, moveTo), filesToMove, moveTo));
	return true;
	*/
    }

    private boolean mkdir(Side panelSide)
    {
	NullCheck.notNull(panelSide, "panelSide");
	final CommanderArea area = getPanel(panelSide);
	final Path createIn = area.opened();
	if (createIn == null)
	    return false;
	final Path created = base.mkdir(createIn);
	if (created == null)
	    return true;
	refreshPanels();
	area.find(created, false);
	return true;
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

    @Override public void refreshPanels()
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

    @Override public AreaLayout getAreasToShow()
    {
	return layouts.getCurrentLayout();
    }

    @Override public void gotoLeftPanel()
    {
	luwrain.setActiveArea(leftPanel);
    }

    @Override public void gotoRightPanel()
    {
	luwrain.setActiveArea(rightPanel);
    }

    @Override public void gotoOperations()
    {
	luwrain.setActiveArea(operationsArea);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public void closeApp()
    {
	if (!operationsArea.allOperationsFinished())
	{
	    luwrain.message(strings.notAllOperationsFinished(), Luwrain.MESSAGE_ERROR);
	    return;
	}
	luwrain.closeApp();
    }

    @Override public boolean hasOperations()
    {
	return operationsArea.hasOperations();
    }

    private boolean showInfoArea(Path[] selected)
    {
	NullCheck.notNullItems(selected, "selected");
	infoArea.clear();
	base.fillInfo(infoArea, selected);
	layouts.show(INFO_LAYOUT_INDEX);
	//FIXME:add introduction
	return true;
    }

private boolean closeInfoArea()
    {
	if (hasOperations())
	    layouts.show(OPERATIONS_LAYOUT_INDEX); else
	    layouts.show(NORMAL_LAYOUT_INDEX);
	//FIXME:add introduction
	return true;
    }

    @Override public Settings settings()
    {
	return base.settings();
    }

    private boolean calcSize(Path[] selected)
    {
	NullCheck.notNull(selected, "selected");
	try {
	    long res = 0;
	    for(Path p: selected)
		res += TotalSize.getTotalSize(p);
	    System.out.println("" + res);
	    luwrain.message(strings.bytesNum(res), Luwrain.MESSAGE_DONE);
	}
	catch(IOException e)
	{
	}
	return true;

    }
}
