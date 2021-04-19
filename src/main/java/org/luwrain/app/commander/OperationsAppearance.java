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
	if (!(item instanceof Operation))
	    return;
		final Operation op = (Operation)item;
	final Sounds sound;

	if (op.isDone())
	{
	    if (op.getException() == null)
		sound = Sounds.SELECTED; else
		sound = Sounds.ATTENTION;
	} else
	    sound = Sounds.LIST_ITEM;
	luwrain.setEventResponse(DefaultEventResponse.listItem(sound, op.name, null));
    }

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	if (!(item instanceof Operation))
	    return item.toString();
	final Operation op = (Operation)item;
	return op.name;
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
