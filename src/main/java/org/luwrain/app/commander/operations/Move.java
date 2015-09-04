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

package org.luwrain.app.commander.operations;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.luwrain.app.commander.Operation;
import org.luwrain.app.commander.OperationListener;

public class Move implements Operation
{
    private OperationListener listener;
    private String name = "";
    private File[] moveFrom;
    private File moveTo;
    private boolean finished = false;
    private boolean finishingAccepted = false ;
    private int code;
    private String extInfo = "";
    private long totalBytes;
    private long processedBytes;
    private int percents;
    private int lastPercents;
    private boolean interrupted;

    public Move(OperationListener listener,
		String name,
		File[] moveFrom,
		File moveTo)
    {
	this.listener = listener;
	this.name = name;
	this.moveFrom = moveFrom;
	this.moveTo = moveTo;
	if (listener == null)
	    throw new NullPointerException("listener may not be null");
	if (name == null)
	    throw new NullPointerException("name may not be null");
	if (moveFrom == null)
	    throw new NullPointerException("moveFrom may not be null");
	if (moveFrom.length < 1)
	    throw new IllegalArgumentException("moveFrom may not be empty");
	for(int i = 0;i < moveFrom.length;++i)
	    if (moveFrom[i] == null)
		throw new NullPointerException("moveFrom[" + i + "] may not be nul");
	if (moveTo == null)
	    throw new NullPointerException("moveTo may not be null");
	finished = false;
	code = 0;
	totalBytes = 0;
	processedBytes = 0;
	percents = 0;
	lastPercents = 0;
	interrupted = false;
	extInfo = "";
    }

    @Override public void run()
    {
	finished = false;
	finishingAccepted = false;
	try {
	    work();
	    }
	catch (Throwable t)
	{
	    t.printStackTrace();
	    code = UNEXPECTED_PROBLEM;
	    extInfo = "";
	    finished = true;
	}
	listener.onOperationProgress(this);
    }

    private void work()
    {
	//Calculating total size of source files;
	totalBytes = 0;
	try {
	    for(File f: moveFrom)
		totalBytes += TotalSize.getTotalSize(f);
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    code = INACCESSIBLE_SOURCE;
	    finished = true;
	    return;
	}

	//Preparing destination directory;
	final boolean destReady = moveTo.exists() && moveTo.isDirectory();
	if (!destReady)
	{
	    if (moveTo.exists() && moveTo.isFile())
	    {
		//We can move to file only a single file;
		if (moveFrom.length != 1 || !moveFrom[0].isFile())
		{
		    code = MOVING_NON_FILE_TO_FILE;
		    finished = true;
		    return;
		}
		try {
		    moveSingleFile(moveFrom[0], moveTo);
		}
		catch (OperationException e)
		{
		    code = e.code();
		    extInfo = e.extInfo();
		    finished = true;
		    return;
		}
		code = OK;
		finished = true;
		return;
	    } //Copying single file;
	    try {
		mkDestDir();
	    }
	    catch (OperationException e)
	    {
		code = e.code();
		extInfo = e.extInfo();
		finished = true;
		return;
	    }
	} //Preparing destination;

	//If destination directory didn't exist and we are copying a single directory we should put its content instead of it itself;
	File srcBeforeReplace = null;
	if (!destReady && moveFrom.length == 1 && moveFrom[0].isDirectory())
	{
	    srcBeforeReplace = moveFrom[0];
	    moveFrom = moveFrom[0].listFiles();
	}

	try {
	    moveRecurse(moveFrom, moveTo);
	}
	catch (OperationException e)
	{
	    code = e.code();
	    extInfo = e.extInfo();
	    finished = true;
	    return;
	}
	try {
	    if (srcBeforeReplace != null)
		if (!srcBeforeReplace.delete())
		{
		    code = PROBLEM_DELETING_DIRECTORY;
		    extInfo = srcBeforeReplace.getAbsolutePath();
		    finished = true;
		    return;
		}
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
		    code = PROBLEM_DELETING_DIRECTORY;
		    extInfo = srcBeforeReplace.getAbsolutePath();
		    finished = true;
		    return;
	}

	code = OK;
	finished = true;
    }

    private void moveRecurse(File[] fileFrom, File fileTo) throws OperationException
    {
	//toFile should already exist;
	for(File f: fileFrom)
	{
	    final File newDest = new File(fileTo, f.getName());
	    if (moveAtomically(f, newDest))
		continue;
	    if (f.isDirectory())
	    {
		try {
		    newDest.mkdir();
		}
		catch (Throwable t)
		{
		    t.printStackTrace();
		    throw new OperationException(PROBLEM_CREATING_DIRECTORY, newDest.getAbsolutePath());
		}
		moveRecurse(f.listFiles(), newDest);
	    } else
		copyFileToDir(f, fileTo);
	    try {
		if (!f.delete())
		    throw new OperationException(f.isDirectory()?PROBLEM_DELETING_DIRECTORY:PROBLEM_DELETING_FILE, f.getAbsolutePath());
	    }
	    catch (Throwable t)
	    {
		t.printStackTrace();
		throw new OperationException(f.isDirectory()?PROBLEM_DELETING_DIRECTORY:PROBLEM_DELETING_FILE, f.getAbsolutePath());
	    }
	}
    }

    private void copyFileToDir(File file, File destDir) throws OperationException
    {
	copySingleFile(file, new File(destDir, file.getName()));
    }

    private void moveSingleFile(File fromFile, File toFile) throws OperationException
    {
	if (moveAtomically(fromFile, toFile))
	    return;
	copySingleFile(fromFile, toFile);

	try {
	    if (!fromFile.delete())
		    throw new OperationException(fromFile.isDirectory()?PROBLEM_DELETING_DIRECTORY:PROBLEM_DELETING_FILE, fromFile.getAbsolutePath());
	    }
	    catch (Throwable t)
	    {
		t.printStackTrace();
		throw new OperationException(fromFile.isDirectory()?PROBLEM_DELETING_DIRECTORY:PROBLEM_DELETING_FILE, fromFile.getAbsolutePath());
	    }
    }

    private void copySingleFile(File fromFile, File toFile) throws OperationException
    {
	InputStream in = null;
	OutputStream out = null;
	try {
	    in = new FileInputStream(fromFile.getPath());
	}
	catch(Throwable e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_OPENING_FILE, fromFile.getPath());
	}
	try {
	    out = new FileOutputStream(toFile.getPath());
	}
	catch(Throwable e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_CREATING_FILE, toFile.getPath());
	}
	byte[] buf = new byte[2048];
	int length;
	while (true)
	{ 
	    try {
		length = in.read(buf);
	    }
	    catch (Throwable e)
	    {
		e.printStackTrace();
		throw new OperationException(PROBLEM_READING_FILE, fromFile.getPath());
	    }
	    if (length <= 0)
		break;
	    onNewPortion(length);
	    try {
		out.write(buf, 0, length);
	    }
	    catch(Throwable e)
	    {
		e.printStackTrace();
		throw new OperationException(PROBLEM_WRITING_FILE, toFile.getPath());
	    }
	}
	try {
	    in.close();
	}
	catch(Throwable e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_READING_FILE, fromFile.getPath());
	}
	try {
	    out.close();
	}
	catch(Throwable e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_WRITING_FILE, toFile.getPath());
	}
    }

    private void mkDestDir() throws OperationException
    {
	try {
	    Path p = moveTo.toPath();
	    Vector<File> parents = new Vector<File>();
	    while (p != null)
	    {
		parents.add(p.toFile());
		p = p.getParent();
	    }
	    if (parents.isEmpty())
		return;
	    for(int i = parents.size();i > 0;--i)
	    {
		final File f = parents.get(i - 1);
		if (!f.isDirectory())
		    if (!f.mkdir())
			throw new OperationException(PROBLEM_CREATING_DIRECTORY, moveTo.getAbsolutePath());
	    }
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
	    throw new OperationException(PROBLEM_CREATING_DIRECTORY, moveTo.getAbsolutePath());
	}
    }

    private void onNewPortion(int bytes) throws OperationException
    {
	if (interrupted)
	    throw new OperationException(INTERRUPTED);
	processedBytes += bytes;
	long lPercents = (processedBytes * 100) / totalBytes;
	percents = (int)lPercents;
	if (percents > lastPercents)
	{
	    listener.onOperationProgress(this);
	    lastPercents = percents;
	}
    }

    @Override public synchronized void interrupt()
    {
	interrupted = true;
    }

    @Override public synchronized  String getOperationName()
    {
	return name;
    }

    @Override public synchronized  int getPercents()
    {
	return percents;
    }

    @Override public synchronized  boolean isFinished()
    {
	return finished;
    }

    @Override public synchronized  int getFinishCode()
    {
	return code;
    }

    @Override public synchronized  String getExtInfo()
    {
	return extInfo != null?extInfo:"";
    }

    @Override public boolean finishingAccepted()
    {
	if (finishingAccepted)
	    return true;
	finishingAccepted = true;
	return false;
    }

    private static boolean moveAtomically(File fromFile, File toFile)
    {
	try {
	    if (!fromFile.renameTo(toFile))
		return false;
	    return true;
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
	    return false;
	}
    }
}
