/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
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

public class Copy implements Operation
{
    private OperationListener listener;
    private String name = "";
    private File[] copyFrom;
    private File copyTo;
    private boolean finished = false;
    private boolean finishingAccepted = false ;
    private int code;
    private String extInfo = "";
    private long totalBytes;
    private long processedBytes;
    private int percents;
    private int lastPercents;
    private boolean interrupted;

    public Copy(OperationListener listener,
		String name,
		File[] copyFrom,
		File copyTo)
    {
	this.listener = listener;
	this.name = name;
	this.copyFrom = copyFrom;
	this.copyTo = copyTo;
	if (listener == null)
	    throw new NullPointerException("listener may not be null");
	if (name == null)
	    throw new NullPointerException("name may not be null");
	if (copyFrom == null)
	    throw new NullPointerException("copyFrom may not be null");
	if (copyFrom.length < 1)
	    throw new NullPointerException("copyFrom may not be empty");
	for(int i = 0;i < copyFrom.length;++i)
	    if (copyFrom[i] == null)
		throw new NullPointerException("copyFrom[" + i + "] may not be nul");
	if (copyTo == null)
	    throw new NullPointerException("copyTo may not be null");
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
    }

    private void work()
    {
	//Calculating total size of source files;
	totalBytes = 0;
	try {
	    for(File f: copyFrom)
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
	final boolean destReady = copyTo.exists() && copyTo.isDirectory();
	if (!destReady)
	{
	    if (copyTo.exists() && copyTo.isFile())
	    {
		//We can copy to file only a single file;
		if (copyFrom.length != 1 || !copyFrom[0].isFile())
		{
		    code = COPYING_NON_FILE_TO_FILE;
		    finished = true;
		    return;
		}
		try {
		    copySingleFile(copyFrom[0], copyTo);
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
	if (!destReady && copyFrom.length == 1 && copyFrom[0].isDirectory())
	    copyFrom = copyFrom[0].listFiles();

	try {
	    copyRecurse(copyFrom, copyTo);
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
    }

    private void copyRecurse(File[] fileFrom, File fileTo) throws OperationException
    {
	//toFile should already exist;
	for(File f: fileFrom)
	    if (f.isDirectory())
	    {
		File newDest = new File(fileTo, f.getName());
		try {
		    newDest.mkdir();
		}
		catch (Throwable t)
		{
		    t.printStackTrace();
		    throw new OperationException(PROBLEM_CREATING_DIRECTORY, newDest.getAbsolutePath());
		}
		copyRecurse(f.listFiles(), newDest);
	    } else
		copyFileToDir(f, fileTo);
    }

    private void copyFileToDir(File file, File destDir) throws OperationException
    {
	copySingleFile(file, new File(destDir, file.getName()));
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
	    Path p = copyTo.toPath();
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
		    f.mkdir();
	    }
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
	    throw new OperationException(PROBLEM_CREATING_DIRECTORY, copyTo.getAbsolutePath());
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
}
