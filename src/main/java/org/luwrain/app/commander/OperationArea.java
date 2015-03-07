/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.commander;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

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
	if (event.isCommand() && !event.isModified())
	    switch (event.getCommand())
	    {
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
	    actions.close();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getName()
    {
	return strings.operationsAreaName();
    }

    @Override public synchronized  void onOperationProgress(Operation operation)
    {
	if (operation == null)
	    throw new NullPointerException("operation may not be null");
	luwrain.enqueueEvent(new UpdateEvent(this, operation));
    }

    public boolean allOperationsFinished()
    {
	for(Operation op:operations)
	    if (!op.isFinished())
		return false;
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
	    luwrain.message(strings.operationCompletedMessage(op), op.getFinishCode() == Operation.OK?Luwrain.MESSAGE_OK:Luwrain.MESSAGE_ERROR);
	    actions.refresh();//Update list of files on opened panels;
	}
	luwrain.onAreaNewContent(this);
    }

    private String getLineForScreen(Operation op)
    {
	if (op == null)
	    throw new NullPointerException("op may not be null");
	if (op.isFinished())
	    return strings.operationFinishDescr(op);
	return op.getOperationName() + ", " + op.getPercents() + "%";
    }
}
