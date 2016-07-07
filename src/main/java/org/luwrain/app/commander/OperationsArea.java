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
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

import org.luwrain.app.commander.Base.Side;

import org.luwrain.app.commander.operations.*;

class OperationsArea extends NavigationArea implements Listener
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    private Base base;
    private final Vector<Operation> operations = new Vector<Operation>();

    OperationsArea(Luwrain luwrain, Base base,
		     Actions actions, Strings strings)
    {
	super(new DefaultControlEnvironment(luwrain));
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
	this.base = base;
	this.strings = strings;
	this.actions = actions;
    }

void launch(Operation op)
    {
	NullCheck.notNull(op, "op");
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
	NullCheck.notNull(event, "event");
	if (event.isSpecial() && event.withAltOnly())
	    switch(event.getSpecial())
	    {
	    case F1:
		return actions.selectPartition(Side.LEFT);
	    case F2:
		return actions.selectPartition(Side.RIGHT);
	    }
	if (event.isSpecial() && !event.isModified())
	    switch (event.getSpecial())
	    {
	    case ENTER:
		return onEnter(event);
	    case ESCAPE:
		return onEscape(event);
	    case TAB:
		actions.gotoLeftPanel();
		return true;
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case CLOSE:
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

    boolean hasOperations()
    {
	return !operations.isEmpty();
    }

    boolean allOperationsFinished()
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
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.cancelOperationPopupName(),
					  strings.cancelOperationPopupText(op), false, Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	if (!popup.result())
	    return true;
	op.interrupt();
	luwrain.onAreaNewContent(this);
	return true;
    }

    private void onUpdate(Operation issuer)
    {
	int index = 0;
	while(index < operations.size() && operations.get(index) != issuer)
	    ++index;
	if (index >= operations.size())
	    return;
	if (issuer.isFinished())
	{
	    if (issuer.finishingAccepted())
		return;
	    luwrain.message(strings.operationCompletedMessage(issuer), issuer.getResult() == Operation.Result.OK?Luwrain.MESSAGE_OK:Luwrain.MESSAGE_ERROR);
	    actions.refreshPanels();//Update list of files on opened panels;
	}
	luwrain.onAreaNewContent(this);
    }

    private String getLineForScreen(Operation op)
    {
	NullCheck.notNull(op, "op");
	if (op.isFinished())
	    return base.opResultDescr(op.getResult());
	final int percents = op.getPercents();
	if (percents == 0)
	    return op.getOperationName() + "...";
	return  percents + "%, "+ op.getOperationName();
    }

    @Override public void onOperationProgress(Operation operation)
    {
	NullCheck.notNull(operation, "operation");
	luwrain.runInMainThread(()->onUpdate(operation));
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
