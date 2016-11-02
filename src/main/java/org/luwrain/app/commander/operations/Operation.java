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

import java.io.IOException;
import java.nio.file.Path;

public interface Operation extends Runnable
{
    public enum Result {
	INTERRUPTED,
	IO_EXCEPTION,
	MOVE_DEST_NOT_DIR,
	OK,
    };

    String getOperationName();
    int getPercents();
    void interrupt();
    boolean isFinished();
    Result getResult();
Path getExtInfoPath();
    IOException getExtInfoIoException();
    boolean finishingAccepted();
}
