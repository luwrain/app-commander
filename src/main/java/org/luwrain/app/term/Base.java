/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.term;

import java.util.concurrent.*;

import org.luwrain.linux.term.*;
import org.luwrain.core.*;

class Base
{
    static private final int STEP_DELAY = 10;
    static private final String SHELL_COMMAND = "/bin/sh";//FIXME:System dependent, it is better to read it from the registry;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private final Terminal terminal = new Terminal();
    private FutureTask task;

    boolean init(Luwrain luwrain)
    {
	this.luwrain = luwrain;
	NullCheck.notNull(luwrain, "luwrain");
	return true;
    }

    boolean start(String dir,
		  final Actions actions)
    {
	if (task != null && !task.isDone())
	    return false;
	try {
	    terminal.open(SHELL_COMMAND, dir);
	}
	catch(TerminalException e)
	{
	    e.printStackTrace();
	    return false;
	}
	task = new FutureTask(()->{
		while(terminal.isActive())
		{
		    if (terminal.sync())
			luwrain.runInMainThread(()->{
				actions.notify(terminal.newText(), 
					       terminal.getHotPointX(), terminal.getHotPointY(),
					       terminal.bell);
			    });
		    try {
			Thread.sleep(STEP_DELAY);
		    } 
		    catch (InterruptedException e) 
		    {
			Thread.currentThread().interrupt();
		    }
		}
	    }, null);
	executor.execute(task);
	return true;
    }

    void close()
    {
	terminal.close();
    }

    Terminal getTerm()
    {
	return terminal;
    }
}
