
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
	/*
	for(FilesOperation op:operations)
	    if (!op.isFinished())
		return false;
	*/
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


    }
