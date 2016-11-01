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
    private FixedListModel operationsListModel = new FixedListModel();

    boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	settings = RegistryProxy.create(luwrain.getRegistry(), REGISTRY_PATH, Settings.class);
	return true;
    }

    Settings settings()
    {
	return settings;
    }


    boolean move(CommanderArea moveFromArea, CommanderArea moveToArea, 
		 Listener listener, ListArea area, AreaLayoutSwitch layouts)
    {
	NullCheck.notNull(moveFromArea, "moveFromArea");
	NullCheck.notNull(moveToArea, "moveToArea");
	NullCheck.notNull(listener, "listener");
	NullCheck.notNull(area, "area");
	NullCheck.notNull(layouts, "layouts");
	final Path moveFromDir = moveFromArea.opened();
	final Path[] pathsToMove = entriesToProcess(moveFromArea);
	final Path moveTo = moveToArea.opened();
	if (pathsToMove.length < 1)
	    return false;
	final Path dest = Popups.path(luwrain,
				      strings.movePopupName(), movePopupPrefix(pathsToMove),
				      moveTo, moveFromDir,
				      (path)->{
					  NullCheck.notNull(path, "path");
					  return true;
				      },
				      Popups.loadFilePopupFlags(luwrain), Popups.DEFAULT_POPUP_FLAGS);
	if (dest == null)
	    return true;
	launch(Operations.move(listener, moveOperationName(pathsToMove, dest), pathsToMove, dest));
	area.refresh();
	layouts.show(CommanderApp.OPERATIONS_LAYOUT_INDEX);
	return true;
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

    static Path[] entriesToProcess(CommanderArea area)
    {
	NullCheck.notNull(area, "area");
	final Path[] marked = area.marked();
	if (marked.length > 0)
	    return marked;
	final CommanderArea.Entry entry = area.selectedEntry();
	if (entry == null || entry.getType() == CommanderArea.Entry.Type.PARENT)
	    return new Path[0];
	return new Path[]{entry.getPath()};
    }

    private String movePopupPrefix(Path[] pathsToMove)
	{
	    return strings.movePopupPrefix(pathsToMove.length > 1?luwrain.i18n().getNumberStr(pathsToMove.length, "items"):pathsToMove[0].getFileName().toString());
	}

    private String moveOperationName(Path[] pathsToMove, Path moveTo)
    {
	if (pathsToMove.length < 1)
	    return "";
	if (pathsToMove.length > 1)
	    return strings.moveOperationName(pathsToMove[0].getFileName().toString() + ",...", moveTo.toString());
	return strings.moveOperationName(pathsToMove[0].getFileName().toString(), moveTo.toString());
    }

    String opResultDescr(Operation op)
    {
	NullCheck.notNull(op, "op");
	switch(op.getResult())
	{
	case OK:
	    return strings.opResultOk();
	case INTERRUPTED:
	    return strings.opResultInterrupted();
	case UNEXPECTED_PROBLEM:
	    return strings.opResultUnexpectedProblem();
	case PROBLEM_CREATING_DIRECTORY:
	    return strings.opResultProblemCreatingDirectory(op.getExtInfo());
	case PROBLEM_READING_FILE:
	    return strings.opResultProblemReadingFile(op.getExtInfo());
	case PROBLEM_WRITING_FILE:
	    return strings.opResultProblemWritingFile(op.getExtInfo());
	case INACCESSIBLE_SOURCE:
	    return strings.opResultInaccessibleSource();
	case PROBLEM_CREATING_SYMLINK:
	    return strings.opResultProblemCreatingSymlink(op.getExtInfo());
	case PROBLEM_READING_SYMLINK:
	    return strings.opResultProblemReadingSymlink(op.getExtInfo());
	case PROBLEM_DELETING:
	    return strings.opResultProblemDeleting(op.getExtInfo());
	case DEST_EXISTS_NOT_REGULAR:
	    return strings.opResultDestExistsNotRegular(op.getExtInfo());
	case NOT_CONFIRMED_OVERWRITE:
	    return strings.opResultNotConfirmedOverride(op.getExtInfo());
	case DEST_EXISTS_NOT_DIR:
	    return strings.opResultDestExistsNotDir(op.getExtInfo());
	case DEST_EXISTS:
	    return strings.opResultDestExists(op.getExtInfo());
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

    void launch(Operation op)
    {
	NullCheck.notNull(op, "op");
	operations.add(op);
	operationsListModel.setItems(operations.toArray(new Operation[operations.size()]));
	new Thread(op).start();
    }
}
