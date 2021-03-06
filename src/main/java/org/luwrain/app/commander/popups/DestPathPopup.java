/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.commander.popups;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;
import org.luwrain.app.commander.*;

public final class DestPathPopup extends FilePopup
{
    public enum Type {COPY, MOVE};

    private final Type type;
    private final Strings strings;

    public DestPathPopup(Luwrain luwrain, Strings strings, Type type,
		  Path srcDir, Path[] files, Path destDir)
{
    super(luwrain,
	  name(strings), prefix(), acceptance(),
	  destDir.toFile(), srcDir.toFile(),
	  EnumSet.noneOf(FilePopup.Flags.class), Popups.DEFAULT_POPUP_FLAGS);
    NullCheck.notNull(strings, "strings");
    NullCheck.notNull(type, "type");
    this.strings = strings;
    this.type = type;
}

    static private FileAcceptance acceptance()
    {
	return (file, announce)->{
	    return true;
	};
    }

static private String name(Strings strings)
{
    NullCheck.notNull(strings, "strings");
    return strings.copyPopupName();
}

static private String prefix()
{
    return "";
}

    private String copyPopupPrefix(File[] toCopy)
	{
	    return strings.copyPopupPrefix(toCopy.length > 1?luwrain.i18n().getNumberStr(toCopy.length, "items"):toCopy[0].getName());
	}

private String movePopupPrefix(File[] toMove)
	{
	    return strings.movePopupPrefix(toMove.length > 1?luwrain.i18n().getNumberStr(toMove.length, "items"):toMove[0].getName());
	}
}
