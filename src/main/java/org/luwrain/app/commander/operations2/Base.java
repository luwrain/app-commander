
package org.luwrain.app.commander.operations2;

import java.io.*;
import java.nio.file.*;

import org.luwrain.core.NullCheck;
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
	//FIXME:
	return true;
    }

    protected Path[] getDirContent(Path path) throws OperationException
    {
	//FIXME:
	return null;
    }

    protected boolean isSymlink(Path path) throws OperationException
    {
	//FIXME:
	return false;
    }

    protected boolean isRegularFile(Path path, boolean followSymlinks) throws OperationException
    {
	//FIXME:
	return false;
    }

    protected boolean exists(Path path, boolean followSymlinks) throws OperationException
    {
	//FIXME:
	return false;
    }

    protected void createSymlink(Path symlink, Path dest) throws OperationException
    {
	//FIXME:
    }

    protected Path readSymlink(Path path) throws OperationException
    { 
	return null;
    }
}
