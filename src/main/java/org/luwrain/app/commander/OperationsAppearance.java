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

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;

class OperationsAppearance implements ListArea.Appearance
{
    private Luwrain luwrain;
    private Strings strings;
    private Base base;

    OperationsAppearance(Luwrain luwrain, Strings strings,
Base base)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(base, "base");
	this.luwrain = luwrain;
	this.strings = strings;
	this.base = base;
    }

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");

	final FilesOperation op = (FilesOperation)item;
	if (op.isFinished())
	{
	    luwrain.speak(base.getOperationResultDescr(op) + " " + op.getOperationName());
	    return;
	}
	final int percents = op.getPercents();
	if (percents > 0)
	    luwrain.speak("" + luwrain.i18n().getNumberStr(percents, "percents") + " " + op.getOperationName()); else
	    luwrain.speak(op.getOperationName());


    }

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	final FilesOperation op = (FilesOperation)item;
	if (op.isFinished())
	    return base.getOperationResultDescr(op);
	final int percents = op.getPercents();
	if (percents == 0)
	    return op.getOperationName() + "...";
	return  percents + "%: "+ op.getOperationName();
    }

    @Override public int getObservableLeftBound(Object item)
    {
	return 0;
    }

    @Override public int getObservableRightBound(Object item)
    {
	return getScreenAppearance(item, EnumSet.noneOf(Flags.class)).length();
    }
}
