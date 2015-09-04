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

package org.luwrain.app.commander;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

class Base
{
    private Luwrain luwrain;
    private Strings strings;

    public boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	return true;
    }

    public boolean mkdir(File createIn)
    {
	final File f = Popups.file(luwrain,
				   strings.mkdirPopupName(), strings.mkdirPopupPrefix(),
				   createIn, FilePopup.ANY, 0);
	if (f == null)
	    return false;
	try {
	    Path p = f.toPath();
	    final Vector<File> parents = new Vector<File>();
	    while (p != null)
	    {
		parents.add(p.toFile());
		p = p.getParent();
	    }
	    if (parents.isEmpty())
		return false;
	    for(int i = parents.size();i > 0;--i)
	    {
		final File ff = parents.get(i - 1);
		if (!ff.isDirectory())
		    if (!ff.mkdir())
		    {
			luwrain.message(strings.mkdirErrorMessage(), Luwrain.MESSAGE_ERROR);
			return false;
		    }
	    }
	}
	catch (Throwable e)
	{
	    e.printStackTrace();
	    luwrain.message(strings.mkdirErrorMessage(), Luwrain.MESSAGE_ERROR);
	    return false;
	}
	luwrain.playSound(Sounds.MESSAGE_OK);
	return true;
    }
}
