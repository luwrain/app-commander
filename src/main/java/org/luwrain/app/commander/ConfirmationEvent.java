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

import java.nio.file.Path;

import org.luwrain.core.Event;

class ConfirmationEvent extends Event
{
    private Path path;//May be null
    private boolean answer = false;

    ConfirmationEvent(Path path)
    {
	super(100);
	this.path = path;
    }

    Path path()
    {
	return path;
    }

    void setAnswer(boolean answer)
    {
	this.answer = answer;
    }

    boolean answer()
    {
	return answer;
    }
}

