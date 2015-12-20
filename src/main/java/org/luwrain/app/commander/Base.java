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
    static private final String REGISTRY_PATH = "/org/luwrain/app/commander";

    private Luwrain luwrain;
    private Strings strings;
    private Settings settings = null;

    public boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	settings = RegistryProxy.create(luwrain.getRegistry(), REGISTRY_PATH, Settings.class);
	return true;
    }

    Settings settings()
    {
	return settings;
    }

    //Returns what exactly has been created
    Path mkdir(Path createIn)
    {
	Path p = Popups.chooseFile(luwrain,
				   strings.mkdirPopupName(), strings.mkdirPopupPrefix(),
				   createIn, createIn,
				   DefaultFileAcceptance.Type.NOT_EXISTING);
	if (p == null)
	    return null;
	try {
	    Files.createDirectories(p);
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    luwrain.message(strings.mkdirErrorMessage(), Luwrain.MESSAGE_ERROR);
	    return null;
	}
	luwrain.message(strings.mkdirOkMessage(p.getFileName().toString()), Luwrain.MESSAGE_OK);
	return p;
    }

	//returns true if copying begings;
    boolean copy(OperationArea operations, Path copyFromDir,
	      Path[] filesToCopy, Path copyTo)
    {
	NullCheck.notNull(operations, "operations");
	NullCheck.notNull(copyFromDir, "copyFromDir");
	NullCheck.notNullItems(filesToCopy, "filesToCopy");
	NullCheck.notNull(copyTo, "copyTo");
	final Path dest = Popups.chooseFile(luwrain,
				      strings.copyPopupName(), strings.copyPopupPrefix(filesToCopy),
					    copyTo, copyFromDir,
					    DefaultFileAcceptance.Type.ANY);
	if (dest == null)
	    return false;
 	operations.launch(Operations.copy(operations, strings.copyOperationName(filesToCopy, dest), 
					  filesToCopy, dest));
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
}
