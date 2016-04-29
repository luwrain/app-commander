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

package org.luwrain.app.commander;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.luwrain.core.NullCheck;

public class TotalSize
{
    static public long getTotalSize(Path p) throws IOException
    {
	NullCheck.notNull(p, "p");
	if (Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
	    return Files.size(p);
	if (!Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
	    return 0;
	long res = 0;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(p)) {
		for (Path pp : directoryStream) 
		    res += getTotalSize(pp);
	    } 
	return res;
    }
}
