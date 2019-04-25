/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

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

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;

final class DestPathPopup extends FilePopup
{
    private final Strings strings;

    DestPathPopup(Luwrain luwrain, Strings strings, File[] files, File dest)
{
    super(luwrain,
	  makeName(strings),
	  makePrefix(),
	  createAcceptance(),
	  null, //startFrom
	  null, //default dir
	  null,
	  Popups.DEFAULT_POPUP_FLAGS); //flags
    NullCheck.notNull(strings, "strings");
    this.strings = strings;
}

    static private Acceptance createAcceptance()
    {
	return (file, announce)->{
	    return true;
	};
    }

static private String makeName(Strings strings)
{
    NullCheck.notNull(strings, "strings");
    return strings.copyPopupName();
}

static private String makePrefix()
{
    return "";
}
}
