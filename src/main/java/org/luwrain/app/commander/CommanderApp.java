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

import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;
import org.luwrain.app.commander.operations.*;

public class CommanderApp implements Application, Actions
{
    public static final String STRINGS_NAME = "luwrain.commander";

    private Luwrain luwrain;
    private Strings strings;
    private PanelArea leftPanel;
    private PanelArea rightPanel;
    private OperationArea operations;

    public CommanderApp()
    {
    }

    public CommanderApp(String arg)
    {
	//FIXME:
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	Object o =  luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	this.luwrain = luwrain;
	strings = (Strings)o;
	leftPanel = new PanelArea(luwrain, this, strings, PanelArea.LEFT);
	rightPanel = new PanelArea(luwrain, this, strings, PanelArea.RIGHT);
	operations = new OperationArea(luwrain, this, strings);
	return true;
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public void selectLocationsLeft()
    {
	final File f = Popups.importantLocationsAsFile(luwrain, Popup.WEAK);
	if (f == null)
	    return;
	leftPanel.open(f, null);
	luwrain.setActiveArea(leftPanel);
    }

    @Override public void selectLocationsRight()
    {
	final File f = Popups.importantLocationsAsFile(luwrain, Popup.WEAK);
	if (f == null)
	    return;
	rightPanel.open(f, null);
	luwrain.setActiveArea(rightPanel);
    }

    @Override public boolean copy(int panelSide)
    {
	File[] filesToCopy = null;
	File copyTo = null;
	switch(panelSide)
	{
	case PanelArea.LEFT:
	    filesToCopy = leftPanel.selected();
	    copyTo= rightPanel.opened(); 
	    break;
	case PanelArea.RIGHT:
	    filesToCopy = rightPanel.selected();
	    copyTo= leftPanel.opened(); 
	    break;
	default:
	    return false;
	}
	if (filesToCopy == null || filesToCopy.length < 1|| copyTo == null)
	    return false;
	copyTo = Popups.file(luwrain,
			     strings.copyPopupName(),
			     strings.copyPopupPrefix(filesToCopy),
			     copyTo,
			     FilePopup.ANY, 0);
	if (copyTo == null)
	    return true;
 	operations.launch(new Copy(operations, strings.copyOperationName(filesToCopy, copyTo), filesToCopy, copyTo));
	return true;
    }

    @Override public boolean move(int panelSide)
    {
	File[] filesToMove = null;
	File moveTo = null;
	switch(panelSide)
	{
	case PanelArea.LEFT:
	    filesToMove = leftPanel.selected();
	    moveTo= rightPanel.opened(); 
	    break;
	case PanelArea.RIGHT:
	    filesToMove = rightPanel.selected();
	    moveTo= leftPanel.opened(); 
	    break;
	default:
	    return false;
	}
	if (filesToMove == null || filesToMove.length < 1|| moveTo == null)
	    return false;
	moveTo = Popups.file(luwrain,
			     strings.movePopupName(),
			     strings.movePopupPrefix(filesToMove),
			     moveTo,
			     FilePopup.ANY, 0);
	if (moveTo == null)
	    return true;
 	operations.launch(new Move(operations, strings.moveOperationName(filesToMove, moveTo), filesToMove, moveTo));
	return true;
    }

    @Override public boolean mkdir(int panelSide)
    {
	File createIn = panelSide == PanelArea.LEFT?leftPanel.opened():rightPanel.opened();
	if (createIn == null)
	    return false;
	File f = Popups.file(luwrain,
			     strings.mkdirPopupName(),
			     strings.mkdirPopupPrefix(),
			     createIn,
			     FilePopup.ANY, 0);
	if (f == null)
	    return true;
	try {
	    Path p = f.toPath();
	    Vector<File> parents = new Vector<File>();
	    while (p != null)
	    {
		parents.add(p.toFile());
		p = p.getParent();
	    }
	    if (parents.isEmpty())
		return true;
	    for(int i = parents.size();i > 0;--i)
	    {
		final File ff = parents.get(i - 1);
		if (!ff.isDirectory())
		    if (!ff.mkdir())
		    {
			luwrain.message(strings.mkdirErrorMessage(), Luwrain.MESSAGE_ERROR);
			return true;
		    }
	    }
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
	    luwrain.message(strings.mkdirErrorMessage(), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	refreshPanels();
	return true;
    }

    @Override public boolean delete(int panelSide)
    {
	File[] filesToDelete = panelSide == PanelArea.LEFT?leftPanel.selected():rightPanel.selected();
	if (filesToDelete == null || filesToDelete.length < 1)
	    return false;
	YesNoPopup popup = new YesNoPopup(luwrain, strings.delPopupName(),
					strings.delPopupText(filesToDelete), false);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	if (!popup.result())
	    return true;
 	operations.launch(new Delete(operations, strings.delOperationName(filesToDelete), filesToDelete));
	return true;
    }

    @Override public void refreshPanels()
    {
	leftPanel.refresh();
	rightPanel.refresh();
    }

    @Override public boolean openPopup(int side)
    {
	File current = null;
	switch(side)
	{
	case PanelArea.LEFT:
	    current = leftPanel.opened();
	    break;
	case PanelArea.RIGHT:
	    current = rightPanel.opened();
	    break;
	default:
	    return false;
	}
	final File f = Popups.open(luwrain, current, Popup.WEAK);
	if (f == null)
	    return true;
	if (!f.isDirectory())
	{
	    luwrain.openFiles(new String[]{f.getAbsolutePath()});
	    return true;
	}
	if (side == PanelArea.LEFT)
	{
	    leftPanel.open(f, null);
	    luwrain.setActiveArea(leftPanel);
	} else
	{
	    rightPanel.open(f, null);
	    luwrain.setActiveArea(rightPanel);
	}
	return true;
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT_BOTTOM, leftPanel, rightPanel, operations);
    }

    @Override public void gotoLeftPanel()
    {
	luwrain.setActiveArea(leftPanel);
    }

    @Override public void gotoRightPanel()
    {
	luwrain.setActiveArea(rightPanel);
    }

    @Override public void gotoOperations()
    {
	luwrain.setActiveArea(operations);
    }

    @Override public void close()
    {
	if (!operations.allOperationsFinished())
	{
	    luwrain.message(strings.notAllOperationsFinished(), Luwrain.MESSAGE_ERROR);
	    return;
	}
	luwrain.closeApp();
    }

    @Override public boolean hasOperations()
    {
	return operations.hasOperations();
    }
}
