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

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

public class TotalSize
{
    static private class Visitor extends SimpleFileVisitor<Path>
    {
	long res = 0;

	@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
	{
	    if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
		res += Files.size(file);
		return FileVisitResult.CONTINUE;
	}
    }

    static public long getTotalSize(Path f) throws IOException
    {
	final Visitor visitor = new Visitor();
	Files.walkFileTree(f, visitor);
	return visitor.res;
    }
}
