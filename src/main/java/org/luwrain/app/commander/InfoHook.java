/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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
