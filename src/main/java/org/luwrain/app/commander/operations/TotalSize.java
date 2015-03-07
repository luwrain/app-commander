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

public class TotalSize
{
    public static long getTotalSize(File f) throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	final File[] items = f.listFiles();
	long res = 0;
	for(File ff: items)
	{
	    if (ff.isDirectory())
		res += getTotalSize(ff); else
		res += ff.length();
	}
	return res;
    }
}
