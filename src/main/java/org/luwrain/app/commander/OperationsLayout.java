
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
		@Override public boolean onInputEvent(KeyboardEvent event)
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
