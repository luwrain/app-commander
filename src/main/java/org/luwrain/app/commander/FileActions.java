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

final class FileActions extends OperationsNames
{
    FileActions(App app)
    {
	super(app);
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

        boolean localCopy(PanelArea copyFromArea, PanelArea copyToArea)
    {
	NullCheck.notNull(copyFromArea, "copyFromArea");
	NullCheck.notNull(copyToArea, "copyToArea");
	if (!copyFromArea.isLocalDir() || !copyToArea.isLocalDir())
	    return false;
	final Path copyFromDir = PanelArea.asPath(copyFromArea.opened());
	if (copyFromDir == null || !copyFromDir.isAbsolute())
	    return false;
	final Path[] filesToCopy = PanelArea.asPath(copyFromArea.getToProcess());
	if (filesToCopy.length == 0)
	    return false;
	final Path copyToDir = PanelArea.asPath(copyToArea.opened());
	if (copyToDir == null || !copyToDir.isAbsolute())
	    return false;
	final Path dest = app.getConv().copyPopup(copyFromDir, filesToCopy, copyToDir);
	if (dest == null)
	    return true;
	final String name = copyOperationName(filesToCopy, dest);
	final Copy copy = new Copy(app.createOperationListener(), name, null, null);
	app.operations.add(0, copy);
	return true;
    }

    

    private boolean todoLocalMove(PanelArea moveFromArea, PanelArea moveToArea, OperationListener listener)
    {
	NullCheck.notNull(moveFromArea, "moveFromArea");
	NullCheck.notNull(moveToArea, "moveToArea");
	NullCheck.notNull(listener, "listener");
	if (!moveFromArea.isLocalDir() || !moveToArea.isLocalDir())
	    return false;
	final File moveFromDir = PanelArea.asFile(moveFromArea.opened());
	final File[] filesToMove = PanelArea.asFile(moveFromArea.getToProcess());
	final File moveTo = PanelArea.asFile(moveToArea.opened());
	if (filesToMove.length < 1)
	    return false;
	final File dest = null;//app.getConv().movePopup(moveFromDir, filesToMove, moveTo);
	if (dest == null)
	    return true;
	final String opName = moveOperationName(filesToMove, dest);
	//app.launch(app.getLuwrain().getFilesOperations().move(listener, opName, filesToMove, dest));
	return true;
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
