
package org.luwrain.app.commander;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.luwrain.core.*;
import org.luwrain.script.*;

final class InfoHook
{
    private final Luwrain luwrain;

    InfoHook(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    boolean localFilesInfo(File[] files, MutableLines lines)
    {
	NullCheck.notNullItems(files, "files");
	NullCheck.notNull(lines, "lines");
	final AtomicReference res = new AtomicReference();
	final Object arg = ScriptUtils.createReadOnlyArray(files);
	luwrain.xRunHooks("luwrain.commander.info.local", (hook)->{
		try {
		    final Object obj = hook.run(new Object[]{arg});
		    if (obj == null)
			return Luwrain.HookResult.CONTINUE;
		    res.set(obj);
		    return Luwrain.HookResult.BREAK;
		}
		catch(RuntimeException e)
		{
		    res.set(e);
		    return Luwrain.HookResult.BREAK;
		}
	    });
	if (res.get() == null)
	    return false;
	if (res.get() instanceof RuntimeException)
	    throw (RuntimeException)res.get();
	final List<String> items = ScriptUtils.getStringArray(res.get());
	for(String s: items)
	    if (s != null)
		lines.addLine(s);
	return true;
    }
}
