/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.nio.file.attribute.*;

import org.apache.commons.vfs2.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class InfoAndProperties
{
    private Luwrain luwrain;

    void init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    void fillLocalDirInfo(File file, MutableLines lines)
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(lines, "lines");
	lines.addLine("Каталог: " +  file.getAbsolutePath());
	lines.addLine("Свободно: " + formatSize(file.getFreeSpace()));
    }

    void fillDirInfo(FileObject fileObj, MutableLines lines)
    {
	NullCheck.notNull(fileObj, "fileObj");
	NullCheck.notNull(lines, "lines");
	lines.addLine("URL: " + fileObj.toString());
	lines.addLine("");
    }


    /*
    void fillProperties(MutableLines lines, Path[] items)
    {
	NullCheck.notNull(lines, "lines");
	NullCheck.notNullItems(items, "items");
	for(Path p: items)
	{
	    try {
		final StringBuilder b = new StringBuilder();
		b.append(p.getFileName().toString());
		if (Files.isDirectory(p))
		    b.append(FileSystems.getDefault().getSeparator());
		b.append(" ");
		boolean symlink = false;
		boolean directory = false;
		final BasicFileAttributeView basic = Files.getFileAttributeView(p, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		if (basic != null)
		{
		    final BasicFileAttributes attr = basic.readAttributes();
		    if (attr.isDirectory())
			b.append(luwrain.i18n().getStaticStr("CommanderDirectory") + " "); else 
			if (attr.isSymbolicLink())
			    b.append(luwrain.i18n().getStaticStr("CommanderSymlink") + " "); else 
			    if (attr.isOther())
				b.append(luwrain.i18n().getStaticStr("CommanderSymlink") + " ");
		    symlink = attr.isSymbolicLink();
		    directory = attr.isDirectory();
		}
		final PosixFileAttributeView posix = Files.getFileAttributeView(p, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		if (posix != null && !symlink)
		{
		    final PosixFileAttributes attr = posix.readAttributes();
		    final Set<PosixFilePermission> perm = attr.permissions();
		    b.append(perm.contains(PosixFilePermission.OWNER_READ)?"r":"-");
		    b.append(perm.contains(PosixFilePermission.OWNER_WRITE)?"w":"-");
		    b.append(perm.contains(PosixFilePermission.OWNER_EXECUTE)?"x":"-");
		    b.append(perm.contains(PosixFilePermission.GROUP_READ)?"r":"-");
		    b.append(perm.contains(PosixFilePermission.GROUP_WRITE)?"w":"-");
		    b.append(perm.contains(PosixFilePermission.GROUP_EXECUTE)?"x":"-");
		    b.append(perm.contains(PosixFilePermission.OTHERS_READ)?"r":"-");
		    b.append(perm.contains(PosixFilePermission.OTHERS_WRITE)?"w":"-");
		    b.append(perm.contains(PosixFilePermission.OTHERS_EXECUTE)?"x":"-");
		    b.append(" ");
		    b.append(attr.owner() + " ");
		    b.append(attr.group() + " ");
		}
		if (basic != null && !symlink)
		{
		    final BasicFileAttributes attr = basic.readAttributes();
		    b.append(directory?attr.creationTime():attr.lastModifiedTime() + " ");
		}
		lines.addLine(new String(b));
	    }
	    catch (Exception e)
	    {
		lines.addLine(p.getFileName().toString() + ":FIXME:ERROR:" + e.getMessage());
		Log.error("commander", p.toString() + ":" + e.getMessage());
		e.printStackTrace();
	    }
	}
	lines.addLine("");
    }

    boolean shortInfo(Path[] paths)
    {
	NullCheck.notNullItems(paths, "paths");
	if (paths.length < 1)
	    return false;
	try {
	luwrain.message(Files.getFileStore(paths[0]).name());
	}
	catch(IOException e)
	{
	}
	return true;
    }
    */


    boolean calcSize(FileObject[] fileObjects)
    {
	NullCheck.notNullItems(fileObjects, "fileObjects");
	if (fileObjects.length < 1)
	    return false;
	new Thread(()->{
		   long res = 0;
		   try {
		       for(FileObject obj: fileObjects)
			   res += getTotalSize(obj);
		   }
		   catch(org.apache.commons.vfs2.FileSystemException e)
		   {
		       luwrain.crash(e);
		       return;
		   }
    final long finalRes = res;
	luwrain.runInMainThread(()->luwrain.message(formatSize(finalRes)));
	}).start();
	return true;
}

    static private String formatSize(long size)
    {
	if (size >= ((long)1024 * 1024 * 1024 * 10))
	{
	    final long value = size / (1024 * 1024 * 1024);
	    return "" + value + "G";
	}
	if (size >= ((long)1048576 * 10))
	{
	    final long value = size / 1048576;
	    return "" + value + "M";
	}
	if (size >= ((long)1024 * 10))
	{
	    final long value = size / 1024;
	    return "" + value + "K";
	}
	return "" + size;
    }

    static public long getTotalSize(FileObject fileObj) throws org.apache.commons.vfs2.FileSystemException
    {
	NullCheck.notNull(fileObj, "fileObj");

	if (!fileObj.isFolder() && !fileObj.isFile())
	    return 0;
	if (fileObj instanceof org.apache.commons.vfs2.provider.local.LocalFile &&
java.nio.file.Files.isSymbolicLink(java.nio.file.Paths.get(fileObj.getName().getPath())))
	    return 0;
	if (!fileObj.isFolder())
	    return fileObj.getContent().getSize();
	long res = 0;
	for(FileObject child: fileObj.getChildren())
		    res += getTotalSize(child);
	return res;
    }
}
