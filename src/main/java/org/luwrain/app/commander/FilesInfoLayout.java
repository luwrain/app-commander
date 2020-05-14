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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.template.*;

final class FilesInfoLayout extends LayoutBase
{
    private final App app;
    private final NavigationArea area;

    FilesInfoLayout(App app, Lines lines, Runnable closing)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(lines, "lines");
	NullCheck.notNull(closing, "closing");
	this.app = app;
	this.area = new NavigationArea(new DefaultControlContext(app.getLuwrain())) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event, closing))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
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
		@Override public String getAreaName()
		{
		    return app.getStrings().infoAreaName();
		}
		@Override public int getLineCount()
		{
		    return lines.getLineCount() > 0?lines.getLineCount():1;
		}
		@Override public String getLine(int index)
		{
		    if (index >= lines.getLineCount())
			return "";
		    return lines.getLine(index);
		}
	    };
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(area);
    }
}
