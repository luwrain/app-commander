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

import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

class Actions
{
    private Luwrain luwrain;
    private AreaLayoutSwitch layouts;

    void init(Luwrain luwrain, AreaLayoutSwitch layouts)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(layouts, "layouts");
	this.luwrain = luwrain;
	this.layouts = layouts;
    }

    boolean showPropertiesArea(InfoAndProperties infoAndProps,
			       CommanderArea area, SimpleArea propertiesArea)
    {
	NullCheck.notNull(area, "area");
	final Path[] paths = Base.entriesToProcess(area);
	if (paths.length < 1)
	    return false;
	propertiesArea.clear();
	infoAndProps.fillProperties(propertiesArea, paths);
	layouts.show(CommanderApp.PROPERTIES_LAYOUT_INDEX);
	luwrain.announceActiveArea();
	return true;
    }

    boolean onOpenFilesWithApp(String appName, Path[] paths)
    {
	NullCheck.notEmpty(appName, "appName");
	NullCheck.notNullItems(paths, "paths");
	boolean atLeastOne = false;
	for(Path p: paths)
	    if (!Files.isDirectory(p))
	{
	    atLeastOne = true;
	    luwrain.launchApp(appName, new String[]{p.toString()});
	}
	return atLeastOne;
    }

    static Action[] getPanelAreaActions(Strings strings, CommanderArea area)
    {
	NullCheck.notNull(area, "area");
	final Path[] toProcess = Base.entriesToProcess(area);
	if (toProcess.length < 1)
	    return new Action[]{
		new Action("hidden-show", strings.actionHiddenShow()), 
			   new Action("hidden-hide", strings.actionHiddenHide()), 
	    };
	return new Action[]{
	    new Action("open", strings.actionOpen()),
	    new Action("edit-text", strings.actionEditAsText(), new KeyboardEvent('n')),
	    new Action("preview", strings.actionPreview(), new KeyboardEvent('v')),
	    new Action("preview-another-format", strings.actionPreviewAnotherFormat()),
	    new Action("play", "Воспроизвести в плеере", new KeyboardEvent('p')),
	    new Action("open-choosing-app", strings.actionOpenChoosingApp()),
	    new Action("copy", strings.actionCopy(), new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("move", strings.actionMove(), new KeyboardEvent(KeyboardEvent.Special.F6)),
	    new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
	    new Action("delete", strings.actionDelete(), new KeyboardEvent(KeyboardEvent.Special.F8)),
	    new Action("hidden-show", strings.actionHiddenShow()), 
	    new Action("hidden-hide", strings.actionHiddenHide()), 
	    new Action("size", strings.actionSize(), new KeyboardEvent('s')),
	};
    }
}
