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

import java.util.*;
import org.apache.commons.vfs2.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

final class ActionList
{
    private final Strings strings;

    ActionList(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	this.strings = strings;
    }

    Action[] getPanelAreaActions(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	final Action hiddenShow = new Action("hidden-show", strings.actionHiddenShow(), new KeyboardEvent('=')); 
	final Action hiddenHide = new Action("hidden-hide", strings.actionHiddenHide(), new KeyboardEvent('-')); 
	final FileObject[] toProcess = area.getFileObjectsToProcess();
	if (toProcess.length < 1)
	    return new Action[]{
		new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
		new Action("open-ftp", "Подключиться к FTP-серверу", new KeyboardEvent(KeyboardEvent.Special.F9)), 
		new Action("volume-info", "Показать информацию о разделе", new KeyboardEvent(KeyboardEvent.Special.F10)), 
		hiddenShow,
		hiddenHide,
	    };
	return new Action[]{
	    new Action("copy", strings.actionCopy(), new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("move", strings.actionMove(), new KeyboardEvent(KeyboardEvent.Special.F6)),
	    new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
	    new Action("delete", strings.actionDelete(), new KeyboardEvent(KeyboardEvent.Special.DELETE)),
	    new Action("open", strings.actionOpen()),
	    new Action("size", strings.actionSize(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.ALT))),
	    new Action("copy-url", strings.actionCopyUrl(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.ALT))),
	    new Action("play", strings.actionPlay(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("preview", strings.actionPreview(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("edit-text", strings.actionEditAsText(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("open-ftp", strings.actionOpenFtp()), 
	    new Action("volume-info", "Показать информацию о разделе", new KeyboardEvent(KeyboardEvent.Special.F10)), 
	    hiddenShow,
	    hiddenHide,
	};
    }
}
