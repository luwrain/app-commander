
package org.luwrain.app.commander;

import java.util.*;

import org.luwrain.core.*;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{
	    new Command(){
		@Override public String getName()
		{
		    return "commander";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("commander");
		}
	    }};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	return new Shortcut[]{
	    new Shortcut() {
		@Override public String getName()
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
	    }};
    }
}
