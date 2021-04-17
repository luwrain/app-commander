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
	super(app);
	this.app = app;
	setCloseHandler(()->{ app.layouts().main(); return true; });
	{
	    final ListArea.Params params = new ListArea.Params();
	    params.context = getControlContext();
	    params.name = app.getStrings().operationsAreaName();
	    params.model = new ListUtils.ListModel(app.operations);
	    params.appearance = new ListUtils.DefaultAppearance(params.context);
	    this.operationsArea = new ListArea(params) {
		    @Override protected String noContentStr()
		    {
			return "Файловые операции отсутствуют";//FIXME:
		    }
		};
	}
	final Actions operationsActions = actions();
	setAreaLayout(operationsArea, operationsActions);
    }
}
