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

package org.luwrain.app.commander;

public interface Operation extends Runnable
{
    public static final int OK = 0;
    public static final int COPYING_NON_FILE_TO_FILE = 1;
    public static final int PROBLEM_OPENING_FILE = 2;
    public static final int PROBLEM_CREATING_FILE = 3;
    public static final int PROBLEM_READING_FILE = 4;
    public static final int PROBLEM_WRITING_FILE = 5;
    public static final int INTERRUPTED = 6;
    public static final int INACCESSIBLE_SOURCE = 7;
    public static final int PROBLEM_CREATING_DIRECTORY = 8;
    public static final int UNEXPECTED_PROBLEM = 9;
    public static final int PROBLEM_DELETING_DIRECTORY = 10;
    public static final int PROBLEM_DELETING_FILE = 11;

    public static final int MOVING_NON_FILE_TO_FILE = 12;

    public static final int DEST_EXISTS_NOT_REGULAR = 20;
    public static final int DEST_EXISTS = 21;
    public static final int DEST_EXISTS_NOT_DIR = 22;

    public static final int NOT_CONFIRMED_OVERWRITE = 25;

    public static final int PROBLEM_READING_SYMLINK = 30;
    public static final int PROBLEM_CREATING_SYMLINK = 31;


    String getOperationName();
    int getPercents();
    void interrupt();
    boolean isFinished();
    int getFinishCode();
    String getExtInfo();
    boolean finishingAccepted();
}
