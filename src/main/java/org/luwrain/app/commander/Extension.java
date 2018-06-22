/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{

	    new Command(){
		@Override public String getName()
		{
		    return "term";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final String currentDir = luwrain.getActiveAreaDir();
		    if (currentDir != null && !currentDir.isEmpty())
			luwrain.launchApp("term", new String[]{currentDir}); else
			luwrain.launchApp("term", new String[]{luwrain.getFileProperty("luwrain.dir.userhome").getAbsolutePath()});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "commander";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("commander");
		}
	    },

	};
    }

    @Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
    {
	return new ExtensionObject[]{

	    new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "commander";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length < 1)
			return new Application[]{new CommanderApp()};
		    final LinkedList<Application> v = new LinkedList<Application>();
		    for(String s: args)
			if (s != null)
			    v.add(new CommanderApp(s));
		    if (v.isEmpty())
			return new Application[]{new CommanderApp()};
		    return v.toArray(new Application[v.size()]);
		}
	    },

	};
    }
}
