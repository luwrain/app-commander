/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

class Base
{
    enum Side {LEFT, RIGHT};

    static private final String REGISTRY_PATH = "/org/luwrain/app/commander";

    private Luwrain luwrain;
    private Strings strings;
    private Settings settings = null;
    final Vector<FilesOperation> operations = new Vector<FilesOperation>();
    private ListUtils.FixedModel operationsListModel = new ListUtils.FixedModel();

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	settings = RegistryProxy.create(luwrain.getRegistry(), REGISTRY_PATH, Settings.class);
	return true;
    }

    void launch(FilesOperation op)
    {
	NullCheck.notNull(op, "op");
	operations.add(op);
	operationsListModel.setItems(operations.toArray(new FilesOperation[operations.size()]));
	new Thread(op).start();
    }

    boolean hasOperations()
    {
	return !operations.isEmpty();
    }

    boolean allOperationsFinished()
    {
	for(FilesOperation op:operations)
	    if (!op.isFinished())
		return false;
	return true;
    }

    String getOperationResultDescr(FilesOperation op)
    {
	NullCheck.notNull(op, "op");
	switch(op.getResult().getType())
	{
	case OK:
	    return strings.opResultOk();
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

    Settings getSettings()
    {
	return settings;
    }

    ListArea.Model getOperationsListModel()
    {
	return operationsListModel;
    }
}
