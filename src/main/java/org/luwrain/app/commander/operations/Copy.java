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

import org.luwrain.app.commander.Operation;
import org.luwrain.app.commander.OperationListener;

public class Copy implements Operation
{
    private OperationListener listener;
    private String name = "";
    private File[] copyFrom;
    private File copyTo;
    private boolean finished;
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
	final boolean destReady = copyTo.exists() && copyTo.isDirectory();
	if (!destReady)
	{
	    if (copyTo.exists() && copyTo.isFile())
	    {
		if (copyFrom.length != 1 || !copyFrom[0].isFile())
		    problem(COPYING_NON_FILE_TO_FILE);
		try {
		    copySingleFile(copyFrom[0], copyTo);
		}
		catch (OperationException e)
		{
		    //FIXME:
		    return;
		}
		return;
	    } //Copying single file;
	    mkToFile();
		} //Preparing destination;
	//	System.out.println("destReady=" + destReady);
	if (!destReady && copyFrom.length == 1 && copyFrom[0].isDirectory())
	    copyFrom = copyFrom[0].listFiles();
	try {
	    copyRecurse(copyFrom, copyTo);
	}
	catch (OperationException e)
	{
	    //FIXME:
	}
    }

    private void copyRecurse(File[] fileFrom, File fileTo) throws OperationException
    {
	//	System.out.println("copyRecurse(...," + fileTo.getPath() + ")");
	//toFile should already exist;
	for(File f: fileFrom)
	    if (f.isDirectory())
	    {
		File newDest = new File(fileTo, f.getName());
		newDest.mkdir();
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
	catch(FileNotFoundException e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_OPENING_FILE, fromFile.getPath());
	}
	try {
out = new FileOutputStream(toFile.getPath());
	}
	catch(FileNotFoundException e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_CREATING_FILE, toFile.getPath());
	}

	byte[] buf = new byte[4096];
	int length;
	while (true)
	{ 
	    try {
	    length = in.read(buf);
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
		throw new OperationException(PROBLEM_READING_FILE, fromFile.getPath());
	    }
	    if (length <= 0)
		break;
	    //	    onNewData(length);
	    try {
	    out.write(buf, 0, length);
	    }
	    catch(IOException e)
	    {
		e.printStackTrace();
		throw new OperationException(PROBLEM_WRITING_FILE, toFile.getPath());
	    }
	}
	try {
	in.close();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_READING_FILE, fromFile.getPath());
	}
	try {
	out.close();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	    throw new OperationException(PROBLEM_WRITING_FILE, toFile.getPath());
	}
    }

    private void mkToFile()
    {
	copyTo.mkdir();
    }

    private void problem(int code)
    {

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
}
