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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.app.commander.App.Side;
import org.luwrain.app.commander.fileops.*;

final class OperationsLayout extends LayoutBase
{
    private final App app;
    final ListArea operationsArea;

    OperationsLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.operationsArea = new ListArea(createOperationsParams()) {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event, ()->app.layouts().main()))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override protected String noContentStr()
		{
		    return "Файловые операции отсутствуют";//FIXME:
		}
	    };
    }

    private ListArea.Params createOperationsParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.name = app.getStrings().operationsAreaName();
	params.model = new ListUtils.ArrayModel(()->app.operations.toArray(new Operation[app.operations.size()]));
	params.appearance = new ListUtils.DefaultAppearance(params.context);
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(operationsArea);
    }
}
