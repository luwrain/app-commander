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

package org.luwrain.app.commander.operations2;

import java.io.*;
import java.nio.file.Path;

import org.luwrain.core.NullCheck;
import org.luwrain.app.commander.OperationListener;

/*
 * Overwrites only regular files
 * Symlinks are always saved
 * Files of other types silently skipped*/




class Copy extends Base
{
    private Path[] copyFrom;
    private Path copyTo;
    private long totalBytes;
    private long processedBytes;
    private int percents;
    private int lastPercents;//Useful for filtering out notifications with the same number of percent
    private boolean overwriteApproved = false;

    Copy(OperationListener listener, String opName,
		Path[] copyFrom, Path copyTo)
    {
	super(listener, opName);
	this.copyFrom = copyFrom;
	this.copyTo = copyTo;
	NullCheck.notNull(copyFrom, "copyFrom");
	if (copyFrom.length < 1)
	    throw new IllegalArgumentException("copyFrom may not be empty");
	for(int i = 0;i < copyFrom.length;++i)
	    NullCheck.notNull(copyFrom[i], "copyFrom[" + i + "]");
	NullCheck.notNull(copyTo, "copyTo");
	totalBytes = 0;
	processedBytes = 0;
	percents = 0;
	lastPercents = 0;
    }

    @Override protected void work() throws OperationException
    {
	//Calculating the total size of source files
	totalBytes = 0;
	for(Path f: copyFrom)
	    totalBytes += TotalSize.getTotalSize(f);
	if (copyFrom.length == 1)
	    singleFile(copyFrom[0], copyTo); else
	    multipleFiles(copyFrom, copyTo);
    }

    private void singleFile(Path fileFrom, Path fileTo) throws OperationException
    {
	//The destination directory already exists, just copying whatever fileFrom is
	if (isDirectory(fileTo, true))
	{
	    copyRecurse(new Path[]{fileFrom}, fileTo);
	    return;
	}
	//The destination doesn't exist and we are about to create it depending what fileFrom is
	if (isDirectory(fileFrom, false))
	{
	    //If fileTo points to a non-directory item, these operations will fail
	    createDirectories(fileTo);
	    if (!isDirectory(fileTo, false))//It cannot be a symlink, we have just created it
		throw new OperationException(PROBLEM_CREATING_DIRECTORY, fileTo.toString());
	    copyRecurse(getDirContent(fileFrom), fileTo);
	    return;
	}
	if (!isSymlink(fileFrom) && !isRegularFile(fileFrom, false))
	    return;//Silently do nothing
	if (exists(fileTo, false))
	{
	    //We may overwrite only a regular file
	    if (!isRegularFile(fileTo, false))
		throw new OperationException(DEST_EXISTS_NOT_REGULAR, fileTo.toString());
	    if (!listener.confirmOverwrite(fileTo))
		throw new OperationException(NOT_CONFIRMED_OVERWRITE, fileTo.toString());
	    overwriteApproved = true;
	}
	copySingleFile(fileFrom, fileTo);//This takes care if fromFile is a symlink
    }

    private void multipleFiles(Path[] filesFrom, Path fileTo) throws OperationException
    {
	if (!isDirectory(fileTo, true))
	{
	    createDirectories(fileTo);
	    if (!isDirectory(fileTo, true))
		throw new OperationException(PROBLEM_CREATING_DIRECTORY, fileTo.toString());
	}
	copyRecurse(filesFrom, fileTo);
    }

    private void copyRecurse(Path[] filesFrom, Path fileTo) throws OperationException
    {
	//toFile should already exist and should be a directory
	for(Path f: filesFrom)
	    if (isDirectory(f, false))
	    {
		//checking the type
		final Path newDest = fileTo.resolve(f.getFileName());
		if (exists(newDest, false))
		{
		    if (!isDirectory(newDest, true))
			throw new OperationException(DEST_EXISTS_NOT_DIR, newDest.toString());
		} else
		    createDirectory(newDest);
		if (!isDirectory(newDest, true))
		    throw new OperationException(PROBLEM_CREATING_DIRECTORY, newDest.toString());
		copyRecurse(getDirContent(f), newDest);
	    } else
		copyFileToDir(f, fileTo);
    }

    private void copyFileToDir(Path file, Path destDir) throws OperationException
    {
	copySingleFile(file, destDir.resolve(file.getFileName()));
    }

//Saves the symlinks and asks confirmation if overriteApproved is false
    private void copySingleFile(Path fromFile, Path toFile) throws OperationException
    {
	if (isSymlink(fromFile))
	{
	    if (exists(toFile, false))
		throw new OperationException(DEST_EXISTS, toFile.toString());
	    createSymlink(toFile, readSymlink(fromFile));
	    return;
	}
	if (exists(toFile, false))
	{
	    if (!isRegularFile(toFile, false))
		throw new OperationException(DEST_EXISTS_NOT_REGULAR, toFile.toString());
	    if (!overwriteApproved && !listener.confirmOverwrite())
		throw new OperationException(NOT_CONFIRMED_OVERWRITE, toFile.toString());
	    overwriteApproved = true;
	}
	final InputStream in = newInputStream(fromFile);
	final OutputStream out = newOutputStream(toFile);
	final byte[] buf = new byte[2048];
	int length;
	while (true)
	{ 
	    length = read(in, buf);
	    if (length <= 0)
		break;
	    onNewPortion(length);
	    write(out, buf, length);
	}
	close(in);
	close(out);
    }

    private void onNewPortion(int bytes) throws OperationException
    {
	if (interrupted)
	    throw new OperationException(INTERRUPTED, "");
	processedBytes += bytes;
	long lPercents = (processedBytes * 100) / totalBytes;
	percents = (int)lPercents;
	if (percents > lastPercents)
	{
	    listener.onOperationProgress(this);
	    lastPercents = percents;
	}
    }

    @Override public synchronized  int getPercents()
    {
	return percents;
    }
}
