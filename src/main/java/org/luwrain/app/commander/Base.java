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
import java.nio.file.attribute.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;
import org.luwrain.app.commander.operations.Operation;
import org.luwrain.app.commander.operations.Operations;
import org.luwrain.app.commander.operations.Listener;

class Base
{
    enum Side {LEFT, RIGHT};

    static private final String REGISTRY_PATH = "/org/luwrain/app/commander";

    private Luwrain luwrain;
    private Strings strings;
    private Settings settings = null;
    final Vector<Operation> operations = new Vector<Operation>();
    private ListUtils.FixedModel operationsListModel = new ListUtils.FixedModel();

    boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	settings = RegistryProxy.create(luwrain.getRegistry(), REGISTRY_PATH, Settings.class);
	return true;
    }

    void launch(Operation op)
    {
	NullCheck.notNull(op, "op");
	operations.add(op);
	operationsListModel.setItems(operations.toArray(new Operation[operations.size()]));
	new Thread(op).start();
    }

    Settings getSettings()
    {
	return settings;
    }

    boolean runWithShortcut(Path[] selected)
    {
	NullCheck.notNullItems(selected, "selected");
	final String[] shortcuts = luwrain.getAllShortcutNames();
	Popups.fixedList(luwrain, "Выберите приложение:", shortcuts);
	return true;
    }

    boolean onClickInFiles(Path[] selected)
    {
    	final String fileNames[] = new String[selected.length];
	for(int i = 0;i < selected.length;++i)
	    fileNames[i] = selected[i].toString();
	luwrain.openFiles(fileNames);
	return true;
    }

    /*
    boolean copyToClipboard(CommanderArea area)
    {
	NullCheck.notNull(area, "area");
	final Path[] marked = area.marked();
	if (marked.length > 0)
	{
	    final LinkedList<String> fileNames = new LinkedList<String>();
	    for(Path p: marked)
		fileNames.add(p.getFileName().toString());
	    luwrain.setClipboard(new RegionContent(fileNames.toArray(new String[fileNames.size()]), marked));
	    return true;
	}
	final CommanderArea.Entry entry = area.selectedEntry();
	if (entry == null || entry.getType() == CommanderArea.Entry.Type.PARENT)
	    return false;
	final Path path = entry.getPath();
	luwrain.setClipboard(new RegionContent(new String[]{path.getFileName().toString()}, new Object[]{path}));
	return true;
    }
    */

    String getOperationResultDescr(Operation op)
    {
	NullCheck.notNull(op, "op");
	switch(op.getResult())
	{
	case OK:
	    return strings.opResultOk();
	case MOVE_DEST_NOT_DIR:
	    return "Целевой путь не указывает на каталог";
	case INTERRUPTED:
	    return strings.opResultInterrupted();
	case IO_EXCEPTION:
	    return luwrain.i18n().getExceptionDescr(op.getExtInfoIoException());
	default:
	    return "";
	}
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

    ListArea.Model getOperationsListModel()
    {
	return operationsListModel;
    }

    static Path[] entriesToProcess(CommanderArea area)
    {
	NullCheck.notNull(area, "area");
	final Path[] marked = area.marked();
	if (marked.length > 0)
	    return marked;
	final CommanderArea.Entry entry = area.getSelectedEntry();
	if (entry == null || entry.getType() == CommanderArea.Entry.Type.PARENT)
	    return new Path[0];
	return new Path[]{entry.getPath()};
    }


}
