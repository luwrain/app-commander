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

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.luwrain.core.*;
import org.luwrain.script.*;
import org.luwrain.script.hooks.*;

final class Hooks
{
    static private final String LOCAL_FILES_INFO_HOOK = "luwrain.commander.info.files.local";

    private final App app;

    Hooks(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    boolean localFilesInfo(File[] files, MutableLines lines)
    {
	NullCheck.notNullItems(files, "files");
	NullCheck.notNull(lines, "lines");
	final Object arg = ScriptUtils.createReadOnlyArray(files);
	final Object res = new ProviderHook(app.getLuwrain()).run(LOCAL_FILES_INFO_HOOK, new Object[]{arg});
	if (res == null)
	    return false;
	final List<String> items = ScriptUtils.getStringArray(res);
	if (items == null)
	    return false;
	lines.addLine("");
	for(String s: items)
	    if (s != null)
		lines.addLine(s);
	lines.addLine("");
	return true;
    }
}
