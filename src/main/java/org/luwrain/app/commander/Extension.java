
package org.luwrain.app.commander;

import java.util.*;

import org.luwrain.base.*;
import org.luwrain.core.*;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{ new SimpleShortcutCommand("commander")};
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
			return new Application[]{new App()};
		    final LinkedList<Application> v = new LinkedList<Application>();
		    for(String s: args)
			if (s != null)
			    v.add(new App(s));
		    if (v.isEmpty())
			return new Application[]{new App()};
		    return v.toArray(new Application[v.size()]);
		}
	    },

	};
    }
}
