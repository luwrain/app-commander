/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>

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
import java.io.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;
import org.luwrain.popups.*;

import org.luwrain.app.commander.App.Side;
import org.luwrain.template.*;

final class OperationsLayout extends LayoutBase
{
    private final App app;
        private final ListArea operationsArea;
    private AreaLayoutHelper layout = null;

    OperationsLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.operationsArea = new ListArea(createOperationsParams()) {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
			return super.onSystemEvent(event);
		}
		@Override protected String noContentStr()
		{
		    return "Файловые операции отсутствуют";//FIXME:
		}
	    };
    }

    private ListArea.Params createOperationsParams()
    {
	return null;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(operationsArea);
    }

        private class OperationsListModel implements ListArea.Model
    {
	@Override public Object getItem(int index)
	{
	    if (index < 0)
		throw new IllegalArgumentException("index (" + index + ") may not be negative");
	    return app.operations.get(index);
	}
	@Override public int getItemCount()
	{
	    return app.operations.size();
	}
	@Override public void refresh()
	{
	}
    }


    }
