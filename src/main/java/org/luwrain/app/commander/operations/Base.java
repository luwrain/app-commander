/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;

abstract class Base implements Operation
{
    protected final Listener listener;
    protected final String opName;

    protected boolean finished = false;
    protected boolean finishingAccepted = false ;
    protected Result opCode = Result.OK;
    protected Path resultExtInfoPath = null;
    protected String extInfo = "";
    protected boolean interrupted = false;

    Base(Listener listener, String opName)
    {
	NullCheck.notNull(listener, "listener");
	NullCheck.notEmpty(opName, "opName");
	this.listener = listener;
	this.opName = opName;
    }

    abstract protected Result work() throws IOException;

    @Override public void run()
    {
	try {
	    work();
	    opCode = Result.OK;
	    finished = true;
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    //	    opCode = e.code();
	    //	    extInfo = e.extInfo();
	    finished = true;
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    opCode = Result.UNEXPECTED_PROBLEM;
	    extInfo = "";
	    finished = true;
	}
	listener.onOperationProgress(this);
    }

    protected void createDirectory(Path path) throws OperationException
    {
	try {
	    Files.createDirectory(path);
	}
	catch(Exception e)
	{
	    throw new OperationException(Result.PROBLEM_CREATING_DIRECTORY, path, e);
	}
    }

    protected InputStream newInputStream(Path path) throws OperationException
    {
	try {
	    return Files.newInputStream(path);
	}
	catch(Exception e)
	{
	    throw new OperationException(Result.PROBLEM_READING_FILE, path, e);
	}
    }

    protected OutputStream newOutputStream(Path path) throws OperationException
    {
	try {
	    return Files.newOutputStream(path);
	}
	catch(Exception e)
	{
	    throw new OperationException(Result.PROBLEM_WRITING_FILE, path, e);
	}
    }

    /*
    protected int read(InputStream stream, byte[] buf) throws OperationException
    {
	try {
	    return stream.read(buf);
	}
	catch(Exception e)
	{
	    throw new OperationException(Result.PROBLEM_READING_FILE, null, e);
	}
    }

    protected void write(OutputStream stream, byte[] buf, int len) throws OperationException
    {
	try {
	    stream.write(buf, 0, len);
	}
	catch(Exception e)
	{
	    throw new OperationException(Result.PROBLEM_WRITING_FILE, null, e);
	}
    }
    */

    /*
    protected void close(InputStream stream)
    {
	try {
	    stream.close();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
    }
    */

    protected void close(OutputStream stream)
    {
	try {
	    stream.close();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
    }

    @Override public synchronized void interrupt()
    {
	interrupted = true;
    }

    @Override public synchronized  String getOperationName()
    {
	return opName;
    }

    @Override public synchronized  boolean isFinished()
    {
	return finished;
    }

    @Override public synchronized Result getResult()
    {
	return opCode;
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

    protected boolean isDirectory(Path path, boolean followSymlinks) throws IOException
    {
	NullCheck.notNull(path, "path");
	    if (followSymlinks)
		return Files.isDirectory(path); else
		return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    protected Path[] getDirContent(final Path path) throws IOException
    {
	status("enumerating items in " + path);
	final LinkedList<Path> res = new LinkedList<Path>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
		for (Path p : directoryStream) 
		    res.add(p);
	    } 
	status("" + path + " contains " + res.size() + " items");
	return res.toArray(new Path[res.size()]);
    }

    protected boolean isRegularFile(Path path, boolean followSymlinks) throws IOException
    {
	NullCheck.notNull(path, "path");
	    if (followSymlinks)
		return Files.isRegularFile(path); else
		return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
    }

    protected boolean exists(Path path, boolean followSymlinks) throws IOException
    {
	NullCheck.notNull(path, "path");
	    if (followSymlinks)
		return Files.exists(path); else
		return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    /*
    protected void createSymlink(Path symlink, Path dest) throws OperationException
    {
	try {
	    Files.createSymbolicLink(symlink, dest);
	}
	catch(Exception e)
	{
	    throw new OperationException(Result.PROBLEM_CREATING_SYMLINK, symlink, e);
	}
    }
    */

    /*
    protected Path readSymlink(Path path) throws OperationException
    { 
	try {
	    return Files.readSymbolicLink(path);
	}
	catch(Exception e)
	{
	    throw new OperationException(Result.PROBLEM_READING_SYMLINK, path, e);
	}
    }
    */

    protected void delete(Path path) throws OperationException
    {
	try {
	    Files.delete(path);
	}
	catch (Exception e)
	{
	    throw new OperationException(Result.PROBLEM_DELETING, path);
	}
    }

    protected void status(String message)
    {
	Log.debug("commander", message);
    }

    protected boolean confirmOverwrite(Path path)
    {
	return true;
    }

    protected void setResultExtInfoPath(Path path)
    {
	resultExtInfoPath = path;
    }


}
