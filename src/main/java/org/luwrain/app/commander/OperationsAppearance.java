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

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.commander.fileops.*;

class OperationsAppearance implements ListArea.Appearance
{
        private final App app;
    private final Luwrain luwrain;
    private final Strings strings;


    OperationsAppearance(App app)
    {
	NullCheck.notNull(app, "app");
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
	this.app = app;
    }

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");

	final Operation op = (Operation)item;
	if (op.isFinished())
	{
	    app.getLuwrain().speak(app.getOperationResultDescr(op) + " " + op.name);
	    return;
	}
	final int percents = op.getPercent();
	if (percents > 0)
	    luwrain.speak("" + luwrain.i18n().getNumberStr(percents, "percents") + " " + op.name); else
	    luwrain.speak(op.name);


    }

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	final Operation op = (Operation)item;
	if (op.isFinished())
	    return app.getOperationResultDescr(op);
	final int percent = op.getPercent();
	if (percent == 0)
	    return op.name + "...";
	return  percent + "%: "+ op.name;
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
