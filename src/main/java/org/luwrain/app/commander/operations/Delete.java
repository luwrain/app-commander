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
//import java.nio.file.*;
//import java.util.*;

import org.luwrain.app.commander.Operation;
import org.luwrain.app.commander.OperationListener;

public class Delete implements Operation
{
    private OperationListener listener;
    private String name = "";
    private File[] deleteWhat;
    private boolean finished = false;
    private boolean finishingAccepted = false ;
    private int code;
    private String extInfo = "";
    private boolean interrupted;

    public Delete(OperationListener listener,
		String name,
		File[] deleteWhat)
    {
	this.listener = listener;
	this.name = name;
	this.deleteWhat = deleteWhat;
	if (listener == null)
	    throw new NullPointerException("listener may not be null");
	if (name == null)
	    throw new NullPointerException("name may not be null");
	if (deleteWhat == null)
	    throw new NullPointerException("deleteWhat may not be null");
	if (deleteWhat.length < 1)
	    throw new IllegalArgumentException("deleteWhat may not be empty");
	for(int i = 0;i < deleteWhat.length;++i)
	    if (deleteWhat[i] == null)
		throw new NullPointerException("deleteWhat[" + i + "] may not be nul");
	finished = false;
	code = 0;
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
	code = OK;
	finished = true;
	listener.onOperationProgress(this);
    }

    private void work()
    {
	try {
	    for(File f: deleteWhat)
	    deleteFileOrDir(f);
	}
	catch(OperationException e)
	{
	    code = e.code();
	    extInfo = e.extInfo();
	    finished = true;
	}
	}

    private void deleteFileOrDir(File f) throws OperationException
    {
	    if (interrupted)
		throw new OperationException(INTERRUPTED);
	if (f == null)
	    throw new NullPointerException("f may not be null");
	if (!f.isDirectory())
	{
	    if (!f.delete())
		throw new OperationException(PROBLEM_DELETING_FILE, f.getAbsolutePath());
	    return;
	}
	final File[] ff = f.listFiles();
	if (ff != null)
	{
	    if (interrupted)
		throw new OperationException(INTERRUPTED);
	    for(File fff: ff)
		deleteFileOrDir(fff);
	}
	    if (!f.delete())
		throw new OperationException(PROBLEM_DELETING_DIRECTORY, f.getAbsolutePath());
	    if (interrupted)
		throw new OperationException(INTERRUPTED);
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
	return 0;
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
