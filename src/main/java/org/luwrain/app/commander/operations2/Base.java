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

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;
import org.luwrain.app.commander.Operation;
import org.luwrain.app.commander.OperationListener;

abstract class Base implements Operation
{
    protected OperationListener listener;
    protected String opName = "";
    protected boolean finished = false;
    protected boolean finishingAccepted = false ;
    protected int opCode;
    protected String extInfo = "";
    protected boolean interrupted;

    Base(OperationListener listener, String opName)
    {
	this.listener = listener;
	this.opName = opName;
	NullCheck.notNull(listener, "listener");
	NullCheck.notNull(opName, "opName");
	if (opName.trim().isEmpty())
	    throw new IllegalArgumentException("opName may not be empty");
	interrupted = false;
	finished = false;
	opCode = 0;
	extInfo = "";
    }

    abstract protected void work() throws OperationException;

    @Override public void run()
    {
	try {
	    work();
	    opCode = OK;
	    finished = true;
	}
	catch (OperationException e)
	{
	    e.printStackTrace();
	    opCode = e.code();
	    extInfo = e.extInfo();
	    finished = true;
	}
	catch (Throwable e)
	{
	    e.printStackTrace();
	    opCode = UNEXPECTED_PROBLEM;
	    extInfo = "";
	    finished = true;
	}
	listener.onOperationProgress(this);
    }

    protected void createDirectories(Path path) throws OperationException
    {
	try {
	    Files.createDirectories(path);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_CREATING_DIRECTORY, path.toString(), e);
	}
    }

    protected void createDirectory(Path path) throws OperationException
    {
	try {
	    Files.createDirectory(path);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_CREATING_DIRECTORY, path.toString(), e);
	}
    }

    protected InputStream newInputStream(Path path) throws OperationException
    {
	try {
	    return Files.newInputStream(path);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_READING_FILE, path.toString(), e);
	}
    }

    protected OutputStream newOutputStream(Path path) throws OperationException
    {
	try {
	    return Files.newOutputStream(path);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_WRITING_FILE, path.toString(), e);
	}
    }

    protected int read(InputStream stream, byte[] buf) throws OperationException
    {
	try {
	    return stream.read(buf);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_READING_FILE, "", e);
	}
    }

    protected void write(OutputStream stream, byte[] buf, int len) throws OperationException
    {
	try {
	    stream.write(buf, 0, len);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_WRITING_FILE, "", e);
	}
    }

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

    @Override public synchronized  int getFinishCode()
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

    protected boolean isDirectory(Path path, boolean followSymlinks) throws OperationException
    {
	try {
	    if (followSymlinks)
		return Files.isDirectory(path); else
		return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
	}
	catch(Throwable e)
	{
	    throw new OperationException(UNEXPECTED_PROBLEM, path.toString());
	}
    }

    protected Path[] getDirContent(final Path path) throws OperationException
    {
	status("enumerating items in " + path);
	final LinkedList<Path> res = new LinkedList<Path>();
	final FileVisitor visitor = new SimpleFileVisitor<Path>() {
	    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
	    {
		res.add(file);
		return FileVisitResult.CONTINUE;
	    }
	    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException
	    {
		if (dir.equals(path))
		return FileVisitResult.CONTINUE;
		res.add(dir);
		return FileVisitResult.SKIP_SUBTREE;
	    }
	};
	try {
	    Files.walkFileTree(path, visitor);
	}
	catch(Throwable e)
	{
	    throw new OperationException(INACCESSIBLE_SOURCE, path.toString(), e);
	}
	status("" + path + " contains " + res.size() + " items");
	/*
	for(Path p: res)
	    status("" + p);
	*/
	return res.toArray(new Path[res.size()]);
    }

    protected boolean isSymlink(Path path) throws OperationException
    {
	try {
	    return Files.isSymbolicLink(path);
	}
	catch (Throwable e)
	{
	    throw new OperationException(UNEXPECTED_PROBLEM, path.toString());
	}
    }

    protected boolean isRegularFile(Path path, boolean followSymlinks) throws OperationException
    {
	try {
	    if (followSymlinks)
		return Files.isRegularFile(path); else
		return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
	}
	catch(Throwable  e)
	{
	    throw new OperationException(UNEXPECTED_PROBLEM, path.toString());
	}
    }

    protected boolean exists(Path path, boolean followSymlinks) throws OperationException
    {
	try {
	    if (followSymlinks)
		return Files.exists(path); else
		return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
	}
	catch(Throwable e)
	{
	    throw new OperationException(UNEXPECTED_PROBLEM, path.toString());
	}
    }

    protected void createSymlink(Path symlink, Path dest) throws OperationException
    {
	try {
	    Files.createSymbolicLink(symlink, dest);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_CREATING_SYMLINK, symlink.toString(), e);
	}
    }

    protected Path readSymlink(Path path) throws OperationException
    { 
	try {
	    return Files.readSymbolicLink(path);
	}
	catch(Throwable e)
	{
	    throw new OperationException(PROBLEM_READING_SYMLINK, path.toString(), e);
	}
    }

    protected void status(String message)
    {
	Log.debug("commander", message);
    }
}
