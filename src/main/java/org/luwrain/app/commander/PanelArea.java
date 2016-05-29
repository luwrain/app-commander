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

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.core.Registry;
//import org.luwrain.app.commander.operations.TotalSize;

public class PanelArea extends CommanderArea
{
    enum Side{LEFT, RIGHT};

    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;
    private Side side = Side.LEFT;

    PanelArea(Luwrain luwrain, Actions actions, Strings strings, 
	      Path startFrom, Side side)
    {
	super(constructParams(luwrain, startFrom), startFrom);
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(side, "side");
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	this.side = side;
	//	setClickHandler(this);
    }

public boolean onCommanderClick(Path current, Path[] selected)
    {
	NullCheck.notNull(selected, "selected");
	return actions.onClickInPanel(selected);
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isSpecial() && event.withAltOnly())
	    switch(event.getSpecial())
	    {
	    case F1:
		return actions.selectLocations(Side.LEFT);
	    case F2:
		return actions.selectLocations(Side.RIGHT);
	    }
	if (event.isSpecial()  && !event.isModified())
	    switch(event.getSpecial())
	    {
	    case TAB:
		return actions.onTabInPanel(side);
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	switch(event.getCode())
	{
	case OPEN:
	    if (event instanceof OpenEvent)
	    {
		final Path path = Paths.get(((OpenEvent)event).path());
		if (Files.isDirectory(path))
		{
		    open(path, null);
		    return true;
		}
	    }
	    return false;
	case INTRODUCE:
	    luwrain.playSound(Sounds.INTRO_REGULAR);
	    switch (side)
	    {
	    case LEFT:
		luwrain.say(strings.leftPanelName() + " " + getAreaName());
		break;
	    case RIGHT:
		luwrain.say(strings.rightPanelName() + " " + getAreaName());
		break;
	    }
	    return true;
	case CLOSE:
	    actions.closeApp();
	    return true;
	case ACTION:
	    return actions.onPanelAction(event, side, selected());
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return actions.getPanelAreaActions(selected());
    }

    static private CommanderArea.CommanderParams constructParams(Luwrain luwrain, Path startFrom)
    {
	final CommanderArea.CommanderParams params = new CommanderArea.CommanderParams();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.selecting = true;
	params.filter = new CommanderUtils.NoHiddenFilter();
	params.comparator = new CommanderUtils.ByNameComparator();
	//	params.clickHandler = null;
	params.appearance = new CommanderUtils.DefaultAppearance(params.environment);
	return params;
    }

}
