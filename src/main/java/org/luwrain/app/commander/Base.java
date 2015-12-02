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
import org.luwrain.app.commander.operations2.Operations;

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
	luwrain.message(strings.mkdirOkMessage(f.getName()), Luwrain.MESSAGE_OK);
	return true;
    }

    boolean openReader(File[] files)
    {
	NullCheck.notNull(files, "files");
	for(File f: files)
	    if (f.isDirectory())
	{
	    luwrain.message("Просмотр не применим к каталогам", Luwrain.MESSAGE_ERROR);
	    return false;
	}
	final Object  o = luwrain.getSharedObject("luwrain.reader.formats");
	if (!(o instanceof String[]))
	    return false;
	final String[] formats = (String[])o;
	final String[] formatsStr = new String[formats.length];
	for(int i = 0;i < formats.length;++i)
	{
	    final int pos = formats[i].indexOf(":");
	    if (pos < 0 || pos + 1 >= formats[i].length())
	    {
		formatsStr[i] = formats[i];
		continue;
	    }
	    formatsStr[i] = formats[i].substring(pos + 1);
	}
	final Object selected = Popups.fixedList(luwrain, "Выберите формат для просмотра:", formatsStr, 0);//FIXME:
	if (selected == null)
	    return false;
	String format = null;
	for(int i = 0;i < formatsStr.length;++i)
	    if (selected == formatsStr[i])
		format = formats[i];
	if (format == null)
	    return false;
	final int pos = format.indexOf(":");
	if (pos < 1)
	    return false;
	final String id = format.substring(0, pos);
	for(File f: files)
	    luwrain.launchApp("reader", new String[]{
		    f.getAbsolutePath(),
		    id,
		});
	return true;
    }

	//returns true if copying begings;
    boolean copy(OperationArea operations,
	      File[] filesToCopy, File copyTo)
    {
	NullCheck.notNull(operations, "operations");
	NullCheck.notNull(filesToCopy, "filesToCopy");
	NullCheck.notNull(copyTo, "copyTo");
	final File dest = Popups.file(luwrain,
				      strings.copyPopupName(), strings.copyPopupPrefix(filesToCopy),
			     copyTo, FilePopup.ANY, 0);
	if (dest == null)
	    return false;
	final Path[] pathFilesToCopy = new Path[filesToCopy.length];
	for(int i = 0;i < filesToCopy.length;++i)
	    pathFilesToCopy[i] = filesToCopy[i].toPath();
	final Path pathDest = dest.toPath();
 	operations.launch(Operations.copy(operations, strings.copyOperationName(filesToCopy, dest), 
				   pathFilesToCopy, pathDest));
	return true;
    }
}
