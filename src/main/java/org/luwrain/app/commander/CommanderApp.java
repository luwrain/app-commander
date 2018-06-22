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

import java.util.*;
import java.io.*;
import java.io.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;
import org.luwrain.popups.*;

import org.luwrain.app.commander.Base.Side;

class CommanderApp implements Application
{
    private Luwrain luwrain = null;
    private Strings strings = null;
    private Base base = null;
    private Actions actions = null;
    private ActionList actionList = null;
    private InfoAndProperties infoAndProps = null;

    private PanelArea leftPanel = null;
    private PanelArea rightPanel = null;
    private ListArea operationsArea = null;
    private AreaLayoutHelper layout = null;

    private final String startFrom;

    CommanderApp()
    {
	startFrom = null;
    }

    CommanderApp(String startFrom)
    {
	NullCheck.notNull(startFrom, "startFrom");
	if (!startFrom.isEmpty())
	    this.startFrom = startFrom; else
	    this.startFrom = null;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o =  luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	this.actionList = new ActionList(strings);
	this.infoAndProps = new InfoAndProperties(luwrain);
	this.actions = new Actions(luwrain, base, strings);
	try {
	    if (startFrom != null && !startFrom.isEmpty())
		createAreas(startFrom); else
		createAreas(luwrain.getFileProperty("luwrain.dir.userhome").getAbsolutePath());
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return new InitResult(e);
	}
	layout = new AreaLayoutHelper(()->{
		luwrain.onNewAreaLayout();
		luwrain.announceActiveArea();
	    }, new AreaLayout(AreaLayout.LEFT_RIGHT_BOTTOM, leftPanel, rightPanel, operationsArea));
	return new InitResult();
    }

    private void createAreas(String startFrom) throws Exception
    {
	NullCheck.notEmpty(startFrom, "startFrom");

	final PanelArea.Params leftPanelParams = PanelArea.createParams(new DefaultControlEnvironment(luwrain));
	leftPanelParams.clickHandler = (area, obj, dir)->actions.onClick(area, obj, dir);
	final PanelArea.Params rightPanelParams = PanelArea.createParams(new DefaultControlEnvironment(luwrain));
	rightPanelParams.clickHandler = (area, obj, dir)->actions.onClick(area, obj, dir);

 	leftPanel = new PanelArea(leftPanelParams, actionList) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (onInputEventInPanel(Side.LEFT, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    if (onSystemEventInPanel(event, this, Side.LEFT))
			return true;
		    return super.onSystemEvent(event);
		}
	    };

 	rightPanel = new PanelArea(rightPanelParams, actionList) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (onInputEventInPanel(Side.RIGHT, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    if (onSystemEventInPanel(event, this, Side.RIGHT))
			return true;
		    return super.onSystemEvent(event);
		}
	    };

	leftPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		luwrain.runUiSafely(()->leftPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });
	rightPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		luwrain.runUiSafely(()->rightPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });

	leftPanel.openInitial(startFrom);
	rightPanel.openInitial(startFrom);

	final ListArea.Params listParams = new ListArea.Params();
	listParams.context = new DefaultControlEnvironment(luwrain);
	listParams.model = base.createOperationsListModel();
	listParams.appearance = new OperationsAppearance(luwrain, strings, base);
	listParams.name = strings.operationsAreaName();
	listParams.clickHandler = (area,index,obj)->{
	    if (!base.closeOperation(index))
		return false;
	    area.redraw();
	    luwrain.playSound(Sounds.OK);
	    return true;
	};

	operationsArea = new ListArea(listParams) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(leftPanel);
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override protected String noContentStr()
		{
		    return "Файловые операции отсутствуют";//FIXME:
		}
	    };
    }

    private boolean onInputEventInPanel(Side side, KeyboardEvent event)
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
		{
		    if (side == Side.RIGHT && !base.operations.isEmpty())
			luwrain.setActiveArea(operationsArea); else
			luwrain.setActiveArea(getAnotherPanel(side));
		    return true;
		}
	    }
	return false;
    }

    private boolean onSystemEventInPanel(EnvironmentEvent event, PanelArea area, Side side)
    {
	NullCheck.notNull(event, "event");
	NullCheck.notNull(area, "area");
	NullCheck.notNull(side, "side");
	switch(event.getCode())
	{
	case OPEN:
	    return actions.onOpenEvent(event, area);
	case INTRODUCE:
	    luwrain.playSound(Sounds.INTRO_REGULAR);
	    switch(side)
	    {
	    case LEFT:
		luwrain.say(strings.leftPanelName() + " " + area.getAreaName());
		break;
	    case RIGHT:
		luwrain.say(strings.rightPanelName() + " " + area.getAreaName());
		break;
	    }
	    return true;
	case CLOSE:
	    closeApp();
	    return true;
	case ACTION:
	    {
		if (ActionEvent.isAction(event, "edit-text"))
		    return actions.onOpenFilesWithApp("notepad", area.getFileObjectsToProcess(), false);
		if (ActionEvent.isAction(event, "size"))
		    return infoAndProps.calcSize(area.getFileObjectsToProcess());
		if (ActionEvent.isAction(event, "copy-url"))
		    return actions.onCopyUrls(area);
		if (ActionEvent.isAction(event, "preview"))
		    return actions.onOpenFilesWithApp("reader", area.getFileObjectsToProcess(), true);
		if (ActionEvent.isAction(event, "hidden-show"))
		{
		    area.showHidden();
		    luwrain.message("Скрытые файлы показаны");
		    return true;
		}
		if (ActionEvent.isAction(event, "hidden-hide"))
		{
		    area.hideHidden();
		    luwrain.message("Скрытые файлы убраны");
		    return true;
		}
		if (ActionEvent.isAction(event, "copy"))
		{
		    if (actions.onLocalCopy(getPanel(side), getAnotherPanel(side), createFilesOperationListener()))
		    {
			operationsArea.redraw();
			return true;
		    }
		    return false;
		}
		if (ActionEvent.isAction(event, "move"))
		{
		    if (actions.onLocalMove(getPanel(side), getAnotherPanel(side), createFilesOperationListener()))
		    {
			operationsArea.refresh();
			return true;
		    }
		    return false;
		}
		if (ActionEvent.isAction(event, "mkdir"))
		    return actions.mkdir(this, getPanel(side));
		if (ActionEvent.isAction(event, "delete"))
		    return actions.onLocalDelete(area);
		if (ActionEvent.isAction(event, "open-ftp"))
		    return actions.onOpenFtp(area);
		if (ActionEvent.isAction(event, "volume-info"))
		    return showVolumeInfo(area);
		return false;
	    }
	case PROPERTIES:
	    return showPropertiesArea(area);
	default:
	    return false;
	}
    }

    private FilesOperation.Listener createFilesOperationListener()
    {
	return new FilesOperation.Listener(){
	    @Override public void onOperationProgress(FilesOperation operation)
	    {
		NullCheck.notNull(operation, "operation");
		NullCheck.notNull(operation, "operation");
		luwrain.runUiSafely(()->onOperationUpdate(operation));
	    }
	    @Override public FilesOperation.ConfirmationChoices confirmOverwrite(java.nio.file.Path path)
	    {
		NullCheck.notNull(path, "path");
		return (FilesOperation.ConfirmationChoices)luwrain.callUiSafely(()->actions.conversations.overrideConfirmation(path.toFile()));
	    }
	};
    }

    private boolean showVolumeInfo(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	final SimpleArea propsArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.infoAreaName()){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    layout.closeTempLayout();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
	if (!actions.showVolumeInfo(infoAndProps, area, propsArea))
	    return false;
	layout.openTempArea(propsArea);
	return true;
    }

    private boolean selectPartition(Side side)
    {
	NullCheck.notNull(side, "side");
	File file = null;
	switch(side)
	{
	case LEFT:
	    file = Popups.disksVolumes(luwrain, "Выберите раздел:");//FIXME:
	    if (file == null)
		return true;
	    leftPanel.openLocalPath(file.getAbsolutePath());
	    luwrain.setActiveArea(leftPanel);
	    return true;
	case RIGHT:
	    file = Popups.disksVolumes(luwrain, "Выберите раздел:");//FIXME:
	    if (file == null)
		return true;
	    rightPanel.openLocalPath(file.getAbsolutePath());
	    luwrain.setActiveArea(rightPanel);
	    return true;
	default:
	    return false;
	}
    }

    private void refreshPanels()
    {
	leftPanel.reread(false);
	rightPanel.reread(false);
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
	NullCheck.notNull(area, "area");
	final SimpleArea propertiesArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.infoAreaName()){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case ESCAPE:
			    layout.closeTempLayout();
			    luwrain.announceActiveArea();
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };
	//FIXME:filling
	layout.openTempArea(propertiesArea);
	luwrain.announceActiveArea();
	return true;
    }

    private void onOperationUpdate(FilesOperation operation)
    {
	NullCheck.notNull(operation, "operation");
	operationsArea.redraw();
	//	luwrain.onAreaNewBackgroundSound();
	if (operation.isFinished())
	{
	    if (operation.getResult().getType() == FilesOperation.Result.Type.OK)
		luwrain.playSound(Sounds.DONE);
	    refreshPanels();
	}
    }

    @Override public AreaLayout getAreaLayout()
    {
	return layout.getLayout();
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public void closeApp()
    {
	if (!base.allOperationsFinished())
	{
	    luwrain.message(strings.notAllOperationsFinished(), Luwrain.MessageType.ERROR);
	    return;
	}
	luwrain.closeApp();
    }
}
