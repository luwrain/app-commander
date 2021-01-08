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
import java.util.concurrent.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.app.base.*;
import org.luwrain.app.commander.fileops.*;

final class App extends AppBase<Strings>
{
    static private final String REGISTRY_PATH = "/org/luwrain/app/commander";

    enum Side {LEFT, RIGHT};

    final String startFrom;

    final List<Operation> operations = new ArrayList();
    private Settings sett = null;
    private Conversations conv = null;
    private Hooks hooks = null;
    
    private MainLayout mainLayout = null;

    App()
    {
	this(null);
    }

    App(String startFrom)
    {
	super(Strings.NAME, Strings.class, "luwrain.commander");
	if (startFrom != null && !startFrom.isEmpty())
	    this.startFrom = startFrom; else
	    this.startFrom = null;
    }

    @Override public boolean onAppInit()
    {
	this.sett = RegistryProxy.create(getLuwrain().getRegistry(), REGISTRY_PATH, Settings.class);
	this.conv = new Conversations(this);
	this.hooks = new Hooks(this);
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return true;
    }

    void launch(Operation op)
    {
	NullCheck.notNull(op, "op");
	operations.add(op);
	getLuwrain().executeBkg(new FutureTask(op, null));
    }

    boolean allOperationsFinished()
    {
	for(Operation op:operations)
	    if (!op.isFinished())
		return false;
	return true;
    }

    boolean closeOperation(int index)
    {
	if (index < 0 || index >= operations.size())
	    throw new IllegalArgumentException("index (" + index + ") must be positive and less than the number of operations (" + operations.size() + ")");
	if (!operations.get(index).isFinished())
	    return false;
	operations.remove(index);
	return true;
    }

    String getOperationResultDescr(Operation op)
    {
	NullCheck.notNull(op, "op");
	/*
	switch(op.getResult().getType())
	{
	case OK:
	    return getStrings().opResultOk();
	case SOURCE_PARENT_OF_DEST:
	    return "Целевой каталог является подкаталогом родительского";
	case MOVE_DEST_NOT_DIR:
	    return "Целевой путь не указывает на каталог";
	case INTERRUPTED:
	    return getStrings().opResultInterrupted();
	case EXCEPTION:
	    if (op.getResult().getException() != null)
		return getLuwrain().i18n().getExceptionDescr(op.getResult().getException());
	    return "Нет информации об ошибке";
	default:
	    return "";
	}
	*/
	return "";
    }

    private OperationListener createFilesOperationListener()
    {
	return new OperationListener(){
	    @Override public void onOperationProgress(Operation operation)
	    {
		NullCheck.notNull(operation, "operation");
		NullCheck.notNull(operation, "operation");
		getLuwrain().runUiSafely(()->onOperationUpdate(operation));
	    }

	    	    @Override public void onOperationProgress(org.luwrain.app.commander.fileops.Base base)
	    {
	    }

	    
	    /*
	    @Override public FilesOperation.ConfirmationChoices confirmOverwrite(java.nio.file.Path path)
	    {
		NullCheck.notNull(path, "path");
		return (FilesOperation.ConfirmationChoices)getLuwrain().callUiSafely(()->conv.overrideConfirmation(path.toFile()));
	    }
	    */
	};
    }

    private void onOperationUpdate(Operation operation)
    {
	NullCheck.notNull(operation, "operation");
	//	operationsArea.redraw();
	//	luwrain.onAreaNewBackgroundSound();
	if (operation.isFinished())
	{
	    if (operation.getResult().getType() == Result.Type.OK)
		getLuwrain().playSound(Sounds.DONE);
	    //refreshPanels();
	}
    }

    void layout(AreaLayout layout)
    {
	NullCheck.notNull(layout, "layout");
	getLayout().setBasicLayout(layout);
	getLuwrain().announceActiveArea();
    }

    void layout(AreaLayout layout, Area activeArea)
    {
	NullCheck.notNull(layout, "layout");
	NullCheck.notNull(activeArea, "activeArea");
	getLayout().setBasicLayout(layout);
	getLuwrain().announceActiveArea();
	getLuwrain().setActiveArea(activeArea);
    }

    Conversations getConv()
    {
	return this.conv;
    }

    Hooks getHooks()
    {
return this.hooks;
    }

    Settings getSett()
    {
	return this.sett;
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

    @Override public void closeApp()
    {
	if (!allOperationsFinished())
	{
	    getLuwrain().message(getStrings().notAllOperationsFinished(), Luwrain.MessageType.ERROR);
	    return;
	}
	super.closeApp();
    }
}
