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

public interface Operation extends Runnable
{
    public enum Result {
	OK,
	INTERRUPTED,
	UNEXPECTED_PROBLEM,
	PROBLEM_CREATING_DIRECTORY,//has arg
	PROBLEM_READING_FILE,//has arg
	PROBLEM_WRITING_FILE,//has arg
	INACCESSIBLE_SOURCE,
	PROBLEM_CREATING_SYMLINK,//has arg
	PROBLEM_READING_SYMLINK,//has arg
	PROBLEM_DELETING,//has arg
	DEST_EXISTS_NOT_REGULAR,//has arg
	NOT_CONFIRMED_OVERWRITE,//has arg
	DEST_EXISTS_NOT_DIR,//has arg
	DEST_EXISTS,//has arg
    };

    String getOperationName();
    int getPercents();
    void interrupt();
    boolean isFinished();
    Result getResult();
    String getExtInfo();
    boolean finishingAccepted();
}
