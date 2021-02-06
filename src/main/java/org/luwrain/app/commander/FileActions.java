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

import java.util.*;
import java.io.*;
import java.nio.file.*;
import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.commander.fileops.*;

final class FileActions
{
    private final App app;
    FileActions(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    boolean size(PanelArea panelArea)
    {
	NullCheck.notNull(panelArea, "panelArea");
	final FileObject[] files = panelArea.getToProcess();
	if (files.length == 0)
	    return false;
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{
		final long res;
		try {
		    res = getSize(files);
		}
		catch(org.apache.commons.vfs2.FileSystemException e)
		{
		    app.getLuwrain().crash(e);
		    return;
		}
		app.finishedTask(taskId, ()->app.getLuwrain().message(String.valueOf(res)));
	    });
    }

    private long getSize(FileObject fileObj) throws org.apache.commons.vfs2.FileSystemException
    {
	NullCheck.notNull(fileObj, "fileObj");
	if (fileObj.getType().hasChildren())
	return getSize(fileObj.getChildren());
	if (fileObj.isFile() && !fileObj.isSymbolicLink())
    	    return fileObj.getContent().getSize();
	return 0;
    }

    private long getSize(FileObject[] files) throws org.apache.commons.vfs2.FileSystemException
    {
	NullCheck.notNullItems(files, "files");
	long sum = 0;
	for(FileObject f: files)
	    sum += getSize(f);
	return sum;
    }

    boolean zipCompress(PanelArea panelArea)
    {
	NullCheck.notNull(panelArea, "panelArea");
	final Path[] toProcess = PanelArea.asPath(panelArea.getToProcess());
	if (toProcess.length == 0)
	    return false;
	final ZipCompress zipCompress = new ZipCompress(app.createOperationListener(), "zip", toProcess, Paths.get("/tmp/proba.zip"));
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{
		zipCompress.run();
	    });
    }
}
