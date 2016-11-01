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

import org.luwrain.core.NullCheck;
import org.luwrain.app.commander.InfoAndProperties;

/**
 * Implementation of the files copying procedure. To make its behaviour
 * more predictable, impose three simple rules, which this algorithm must
 * always follow:
 *
 * <ul>
 * <li>Only regular files may be overwritten if they are exist</li>
 * <li>Symlinks are always copied as symlinks (their copying never led to
 * creating of regular files or directories)</li>
 * <li>Files of other types than regular files, directories or symlinks are
 * silently skipped</li>
 * <li>Source files may not be given by relative pathes</li>
 * <li>If the destination is given by a relative pathe, the parent of the first source used to resolve it</li>
 * </ul>
 */
class Copy extends CopyingBase
{
    private final Path[] copyFrom;
    private final Path copyTo;

    Copy(Listener listener, String opName,
	 Path[] copyFrom, Path copyTo)
    {
	super(listener, opName);
	NullCheck.notNullItems(copyFrom, "copyFrom");
	NullCheck.notEmptyArray(copyFrom, "copyFrom");
	NullCheck.notNull(copyTo, "copyTo");
	this.copyFrom = copyFrom;
	this.copyTo = copyTo;
    }

    @Override protected Result work() throws IOException
    {
	return copy(copyFrom, copyTo);
    }
}
