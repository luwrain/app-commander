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

/**
 * Implementation of the files copying procedure. To make its behaviour
 * more predictable, impose three simple rules, which this algorithm must
 * always follow:
 *
 * <ul>
 * <li>Only regular files may be overwritten if they are exist</li>
 * <li>Symlinks are always copied as symlinks (their copying never led to
 * creating of regular files or directories)</li>
 * <li>Files of other types than regular files, directories or symlinks are
 * silently skipped</li>
 * </ul>
 */
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
	    try {
		status("calculating size of " + f);
		totalBytes += TotalSize.getTotalSize(f);
	    }
	    catch (IOException e)
	    {
		throw new OperationException(INACCESSIBLE_SOURCE, f.toString(), e);
	    }
	status("total size is " + totalBytes);
	if (copyFrom.length == 1)
	    singleSource(copyFrom[0], copyTo); else
	    multipleSource(copyFrom, copyTo);
    }

    private void singleSource(Path fileFrom, Path fileTo) throws OperationException
    {
	status("single source mode: " + fileFrom + " -> " + fileTo);
	//The destination directory already exists, just copying whatever fileFrom is
	if (isDirectory(fileTo, true))
	{
	    status("" + fileTo + " exists and is a directory");
	    copyRecurse(new Path[]{fileFrom}, fileTo);
	    return;
	}
	//The destination doesn't exist and we are about to create it depending on what fileFrom is
	if (isDirectory(fileFrom, false))
	{
	    status("" + fileFrom + " is a directory");
	    //If fileTo points to a non-directory item, these operations will fail
	    createDirectories(fileTo);
	    if (!isDirectory(fileTo, false))//It cannot be a symlink, we have just created it
		throw new OperationException(PROBLEM_CREATING_DIRECTORY, fileTo.toString());
	    status("ensured that " + fileTo + "exists and is a directory");
	    copyRecurse(getDirContent(fileFrom), fileTo);
	    return;
	}
	if (!isSymlink(fileFrom) && !isRegularFile(fileFrom, false))
	{
	    status("" + fileFrom + "is not a symlink and is not a regular file, nothing to do");
	    return;//Silently do nothing
	}
	status("" + fileFrom + " is a symlink or a regular file");
	if (exists(fileTo, false))
	{
	    status("" + fileTo + " exists");
	    //We may overwrite only a regular file
	    if (!isRegularFile(fileTo, false))
		throw new OperationException(DEST_EXISTS_NOT_REGULAR, fileTo.toString());
	    status("" + fileTo + "is a regular file, requesting confirmation to overwrite");
	    if (!listener.confirmOverwrite(fileTo))
		throw new OperationException(NOT_CONFIRMED_OVERWRITE, fileTo.toString());
	    overwriteApproved = true;
	    status("overwriting approved");
	}
	copySingleFile(fileFrom, fileTo);//This takes care if fromFile is a symlink
    }

    private void multipleSource(Path[] filesFrom, Path fileTo) throws OperationException
    {
	status("multiple source mode");
	if (!isDirectory(fileTo, true))
	{
	    status("" + fileTo + "does not exist or not a a directory");
	    createDirectories(fileTo);
	    if (!isDirectory(fileTo, true))
		throw new OperationException(PROBLEM_CREATING_DIRECTORY, fileTo.toString());
	    status("ensured that " + fileTo + " exists and is a directory");
	}
	copyRecurse(filesFrom, fileTo);
    }

    private void copyRecurse(Path[] filesFrom, Path fileTo) throws OperationException
    {
	status("copying " + filesFrom.length + " items to " + fileTo);
	//toFile should already exist and should be a directory
	for(Path f: filesFrom)
	    if (isDirectory(f, false))
	    {
		//checking the type
		final Path newDest = fileTo.resolve(f.getFileName());
		if (exists(newDest, false))
		{
		    status("" + newDest + " already exists");
		    if (!isDirectory(newDest, true))
			throw new OperationException(DEST_EXISTS_NOT_DIR, newDest.toString());
		    status("" + newDest + " is a directory");
		} else
		    createDirectory(newDest);
		if (!isDirectory(newDest, true))
		    throw new OperationException(PROBLEM_CREATING_DIRECTORY, newDest.toString());
		status("" + newDest + " prepared");
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
	status("copying single file " + fromFile + " to " + toFile);
	if (isSymlink(fromFile))
	{
	    status("" + fromFile + "is a symlink");
	    if (exists(toFile, false))
		throw new OperationException(DEST_EXISTS, toFile.toString());
	    createSymlink(toFile, readSymlink(fromFile));
	    status("new symlink " + toFile + " is created");
	    return;
	}
	if (exists(toFile, false))
	{
	    status("" + toFile + " already exists");
	    if (!isRegularFile(toFile, false))
		throw new OperationException(DEST_EXISTS_NOT_REGULAR, toFile.toString());
	    status("" + toFile + " is a regular file, need a confirmation, overwriteApproved=" + overwriteApproved);
	    if (!overwriteApproved && !listener.confirmOverwrite())
		throw new OperationException(NOT_CONFIRMED_OVERWRITE, toFile.toString());
	    overwriteApproved = true;
	}
	status("opening streams and copying data");
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
	status("" + fromFile + " successfully copied to " + toFile);
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
