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

import org.luwrain.core.*;
import org.luwrain.core.events.*;

class ConfirmationEvent extends AddressedEnvironmentEvent
{
    private final Path path;
    private ConfirmationChoices answer = null;

    ConfirmationEvent(Area destArea, Path path)
    {
	super(destArea, EnvironmentEvent.Code.USER);
	NullCheck.notNull(path, "path");
	this.path = path;
    }

    Path getPath()
    {
	return path;
    }

    void setAnswer(ConfirmationChoices answer)
    {
	NullCheck.notNull(answer, "answer");
	this.answer = answer;
    }

    ConfirmationChoices getAnswer()              
    {
	return answer;
    }
}

