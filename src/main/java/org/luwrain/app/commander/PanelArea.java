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

import java.io.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.core.Registry;

import org.luwrain.app.commander.operations.TotalSize;

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
		     File startFrom,
		     int side)
    {
	super(new DefaultControlEnvironment(luwrain),
	      luwrain.os(),
	      startFrom,
	      true,
	      new NoHiddenCommanderFilter(),
	      new ByNameCommanderComparator());
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
	if (selected == null)
	    return false;
	System.out.println("click " + selected.length);
	final String fileNames[] = new String[selected.length];
	for(int i = 0;i < selected.length;++i)
	    fileNames[i] = selected[i].getAbsolutePath();
							luwrain.openFiles(fileNames);
	return true;
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (!event.isCommand() && !event.isModified())
	    switch(event.getCharacter())
	    {
	    case ' ':
		return calcSize();
	    case '=':
		setFilter(new AllFilesCommanderFilter());
		refresh();
		return true;
	    case '-':
		setFilter(new NoHiddenCommanderFilter());
		refresh();
		return true;
	    default:
		return super.onKeyboardEvent(event);
	    }
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
	if (event.getCommand() == KeyboardEvent.ENTER && event.withControlOnly())
	    return onShortInfo(event);
	if (event.isModified())
	    return super.onKeyboardEvent(event);
	switch(event.getCommand())
	    {
	    case KeyboardEvent.TAB:
		if (side == LEFT)
		    actions.gotoRightPanel(); else
		    if (side == RIGHT)
		    {
			if (actions.hasOperations())
			    actions.gotoOperations(); else
			    actions.gotoLeftPanel();
		    }
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
		luwrain.say(strings.leftPanel() + " " + getAreaName());
		break;
	    case RIGHT:
		luwrain.say(strings.rightPanel() + " " + getAreaName());
		break;
	    }
    return true;
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	case EnvironmentEvent.ACTION:
	    if (ActionEvent.isAction(event, "read"))
	    {
		actions.openReader(side);
		return true;
	    }
	    return false;

	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("open", "Открыть"),
	    new Action("read", "Просмотреть с указанием формата"),
	};
    }

    private boolean onShortInfo(KeyboardEvent event)
    {
	final File[] f = selected();
	if (f == null)
	    return false;
	long res = 0;
	try {
	    for(File ff: f)
		res += TotalSize.getTotalSize(ff);
	}
	catch (Throwable e)
	{
	    e.printStackTrace();
	    return false;
	}
	luwrain.message(strings.bytesNum(res));
	return true;
    }

    private boolean calcSize()
    {
	final File[] f = selected();
	if (f == null || f.length < 1)
	    return false;
	long res = 0;
	try {
	    for(File ff: f)
		res += org.luwrain.app.commander.operations2.TotalSize.getTotalSize(ff.toPath());
	}
	catch (Throwable e)
	{
	    e.printStackTrace();
	    luwrain.message("Невозможно получить необходимый доступ к файлам, возможно, недостаточно прав доступа", Luwrain.MESSAGE_ERROR);
	    return true;
	}
	luwrain.message(strings.bytesNum(res), Luwrain.MESSAGE_DONE);
	return true;
    }

}
