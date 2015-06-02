
package org.luwrain.app.commander;

import java.util.*;

import org.luwrain.core.Application;
import org.luwrain.core.Shortcut;
import org.luwrain.core.Command;
import org.luwrain.core.CommandEnvironment;
import org.luwrain.core.Worker;
import org.luwrain.core.SharedObject;
import org.luwrain.core.I18nExtension;
import org.luwrain.core.Luwrain;
import org.luwrain.core.Registry;

public class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	Command[] res = new Command[1];
	res[0] = new Command(){
		@Override public String getName()
		{
		    return "commander";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("commander");
		}
	    };
	return res;
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	Shortcut[] res = new Shortcut[1];
	res[0] = new Shortcut() {
		@Override public String getName()
		{
		    return "commander";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length < 1)
		    {
			Application[] res = new Application[1];
			res[0] = new CommanderApp();
			return res;
		    }
		    Vector<Application> v = new Vector<Application>();
		    for(String s: args)
			if (s != null)
			    v.add(new CommanderApp(s));
		    if (v.isEmpty())
		    {
			Application[] res = new Application[1];
			res[0] = new CommanderApp();
			return res;
		    }
		    return v.toArray(new Application[v.size()]);
		}
	    };
	return res;
    }

    @Override public void i18nExtension(Luwrain luwrain, I18nExtension i18nExt)
    {
	i18nExt.addCommandTitle("en", "commander", "Browse files and directories");
	i18nExt.addCommandTitle("ru", "commander", "Обзор файлов и папок");
	i18nExt.addStrings("ru", CommanderApp.STRINGS_NAME, new org.luwrain.app.commander.i18n.Ru());
    }
}
