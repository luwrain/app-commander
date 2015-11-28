/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

class OperationArea extends NavigateArea implements OperationListener
{
    class UpdateEvent extends ThreadSyncEvent
    {
	public Operation operation;

	public UpdateEvent(Area destArea, Operation operation)
	{
	    super(destArea);
	    this.operation = operation;
	    if (operation == null)
		throw new NullPointerException("operation may not be null");
	}
    }

    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    Vector<Operation> operations = new Vector<Operation>();

    public OperationArea(Luwrain luwrain,
		     Actions actions,
		     Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
    }

    public void launch(Operation op)
    {
	if (op == null)
	    throw new NullPointerException("op may not be null");
	operations.add(op);
	luwrain.onAreaNewContent(this);
	new Thread(op).start();
    }

    @Override public int getLineCount()
    {
	return operations.size() + 1;
    }

    @Override public String getLine(int index)
    {
	return index < operations.size()?getLineForScreen(operations.get(index)):"";
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	if (event == null)
	    throw new NullPointerException("event may not be null");
	if (event.isCommand() &&
	    event.getCommand() == KeyboardEvent.F1 &&
	    event.withLeftAltOnly())
	{
	    actions.selectLocationsLeft();
	    return true;
	}
	if (event.isCommand() &&
	    event.getCommand() == KeyboardEvent.F2 &&
	    event.withLeftAltOnly())
	{
	    actions.selectLocationsRight();
	    return true;
	}
	if (event.isCommand() && !event.isModified())
	    switch (event.getCommand())
	    {
	    case KeyboardEvent.ENTER:
		return onEnter(event);
	    case KeyboardEvent.ESCAPE:
		return onEscape(event);
	    case KeyboardEvent.TAB:
	    actions.gotoLeftPanel();
	    return true;
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	if (event == null)
	    throw new NullPointerException("event may not be null");
	switch(event.getCode())
	{
	case EnvironmentEvent.THREAD_SYNC:
	    if (event instanceof UpdateEvent)
		onUpdate((UpdateEvent)event);
	    return true;
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getAreaName()
    {
	return strings.operationsAreaName();
    }

    @Override public synchronized  void onOperationProgress(Operation operation)
    {
	if (operation == null)
	    throw new NullPointerException("operation may not be null");
	luwrain.enqueueEvent(new UpdateEvent(this, operation));
    }

    public boolean hasOperations()
    {
	return !operations.isEmpty();
    }

    public boolean allOperationsFinished()
    {
	for(Operation op:operations)
	    if (!op.isFinished())
		return false;
	return true;
    }

    private boolean onEnter(KeyboardEvent event)
    {
	if (operations == null || getHotPointY() >= operations.size())
	    return false;
	if (!operations.get(getHotPointY()).isFinished())
	    return false;
	operations.remove(getHotPointY());
	luwrain.onAreaNewContent(this);
	return true;
    }

    private boolean onEscape(KeyboardEvent event)
    {
	if (operations == null || getHotPointY() >= operations.size())
	    return false;
	final Operation op = operations.get(getHotPointY());
	if (op.isFinished())
	    return false;
	YesNoPopup popup = new YesNoPopup(luwrain, strings.cancelOperationPopupName(),
					strings.cancelOperationPopupText(op), false);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	if (!popup.result())
	    return true;
	op.interrupt();
	luwrain.onAreaNewContent(this);
	return true;
    }

    private void onUpdate(UpdateEvent event)
    {
	int index = 0;
	while(index < operations.size() && operations.get(index) != event.operation)
	    ++index;
	if (index >= operations.size())
	    throw new IllegalArgumentException("event addresses an unknown operation");
	final Operation op = event.operation;
	if (op.isFinished())
	{
	    if (op.finishingAccepted())
		return;
	    luwrain.message(strings.operationCompletedMessage(op), op.getFinishCode() == Operation.OK?Luwrain.MESSAGE_OK:Luwrain.MESSAGE_ERROR);
	    actions.refreshPanels();//Update list of files on opened panels;
	}
	luwrain.onAreaNewContent(this);
    }

    private String getLineForScreen(Operation op)
    {
	if (op == null)
	    throw new NullPointerException("op may not be null");
	if (op.isFinished())
	    return strings.operationFinishDescr(op);
	final int percents = op.getPercents();
	if (percents == 0)
	    return op.getOperationName() + "...";
	return  percents + "%, "+ op.getOperationName();
}

    @Override public boolean confirmOverwrite(Path path)
    {
	return true;
    }

    @Override public boolean confirmOverwrite()
    {
	return true;
    }


}
