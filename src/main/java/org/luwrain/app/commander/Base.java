/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

final class Base
{
    enum Side {LEFT, RIGHT};

    static private final String REGISTRY_PATH = "/org/luwrain/app/commander";

    final Luwrain luwrain;
    final Strings strings;
    final Settings settings;
    final Vector<FilesOperation> operations = new Vector<FilesOperation>();

    Base (Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.settings = RegistryProxy.create(luwrain.getRegistry(), REGISTRY_PATH, Settings.class);
    }

    void launch(FilesOperation op)
    {
	NullCheck.notNull(op, "op");
	operations.add(op);
	luwrain.executeBkg(new FutureTask(op, null));
    }

    boolean allOperationsFinished()
    {
	for(FilesOperation op:operations)
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

    String getOperationResultDescr(FilesOperation op)
    {
	NullCheck.notNull(op, "op");
	switch(op.getResult().getType())
	{
	case OK:
	    return strings.opResultOk();
	case SOURCE_PARENT_OF_DEST:
	    return "Целевой каталог является подкаталогом родительского";
	case MOVE_DEST_NOT_DIR:
	    return "Целевой путь не указывает на каталог";
	case INTERRUPTED:
	    return strings.opResultInterrupted();
	case EXCEPTION:
	    if (op.getResult().getException() != null)
		return luwrain.i18n().getExceptionDescr(op.getResult().getException());
	    return "Нет информации об ошибке";
	default:
	    return "";
	}
    }

    ListArea.Model createOperationsListModel()
    {
	return new OperationsListModel();
    }

    private class OperationsListModel implements ListArea.Model
    {
	@Override public Object getItem(int index)
	{
	    if (index < 0)
		throw new IllegalArgumentException("index (" + index + ") may not be negative");
	    return operations.get(index);
	}
	@Override public int getItemCount()
	{
	    return operations.size();
	}
	@Override public void refresh()
	{
	}
    }
    }
