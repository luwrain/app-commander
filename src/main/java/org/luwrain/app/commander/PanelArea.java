/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.commander;

import java.io.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.core.Registry;

public class PanelArea extends CommanderArea
{
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private Luwrain luwrain;
    private Actions actions;
    private Strings strings;
    private int side = LEFT;

    public PanelArea(Luwrain luwrain,
		     Actions actions,
		     Strings strings,
		     int side)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.actions = actions;
	this.strings = strings;
	this.side = side;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
    }

    @Override protected boolean onClick(File current, File[] selected)
    {
	//FIXM :
	return false;
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	if (event == null)
	    throw new NullPointerException("event may not be null");
	if (!event.isCommand())
	    return super.onKeyboardEvent(event);
	if (event.getCommand() == KeyboardEvent.F1 && event.withLeftAltOnly())
	{
	    actions.selectLocationsLeft();
	    return true;
	}
	if (event.getCommand() == KeyboardEvent.F2 && event.withLeftAltOnly())
	{
	    actions.selectLocationsRight();
	    return true;
	}
	if (event.isModified())
	    return super.onKeyboardEvent(event);
	switch(event.getCommand())
	    {
	    case KeyboardEvent.TAB:
		if (side == LEFT)
		    actions.gotoRightPanel(); else
		    if (side == RIGHT)
			actions.gotoTasks();
		return true;
	    case KeyboardEvent.F5:
		return actions.copy(side);
	    case KeyboardEvent.F6:
		return actions.move(side);
	    case KeyboardEvent.F7:
		return actions.mkdir(side);
	    case KeyboardEvent.F8:
		return actions.delete(side);
	    case KeyboardEvent.DELETE:
		return actions.delete(side);
	    default:
		return super.onKeyboardEvent(event);
	    }
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	switch(event.getCode())
	{
	case EnvironmentEvent.INTRODUCE:
	    luwrain.playSound(Sounds.INTRO_REGULAR);
	    switch (side)
	    {
	    case LEFT:
		luwrain.say(getName() + " " + strings.leftPanel());
		break;
	    case RIGHT:
		luwrain.say(getName() + " " + strings.rightPanel());
		break;
	    }
    return true;
	case EnvironmentEvent.CLOSE:
	    actions.close();
	    return true;
	    /*
	case EnvironmentEvent.OPEN:
	    return onOpen(event);
	    */
	default:
	    return false;
	}
    }

    /*
    private boolean onOpen(EnvironmentEvent event)
    {
	if (current == null || !current.isDirectory())
	    return false;
	//	File f = luwrain.openPopup(null, null, current);
	File f = null;//FIXME:
	if (f == null)
	    return true;
	if (f.isDirectory())
	    openByFile(f); else
	    luwrain.openFile(f.getAbsolutePath());
	return true;
    }
    */
}
