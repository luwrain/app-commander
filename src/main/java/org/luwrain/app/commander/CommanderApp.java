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
import org.luwrain.core.*;
import org.luwrain.popups.*;

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
	Popups.importantLocationsAsFile(luwrain, 0);
    }

    @Override public void selectLocationsRight()
    {
	Popups.importantLocationsAsFile(luwrain, 0);
    }

    @Override public boolean copy(int panelSide)
    {
	File[] filesToCopy = null;
	File copyTo = null;
	if (panelSide == PanelArea.LEFT)
	{
	    filesToCopy = leftPanel.selected();
	    copyTo= rightPanel.opened(); 
	} else
	if (panelSide == PanelArea.RIGHT)
	{
	    filesToCopy = rightPanel.selected();
	    copyTo= leftPanel.opened(); 
	} else
	    return false;
	if (filesToCopy == null || filesToCopy.length < 1|| copyTo == null)
	    return false;
	FilePopup popup = new FilePopup(luwrain, strings.copyPopupName(),
					strings.copyPopupPrefix(filesToCopy), copyTo);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	copyTo = popup.getFile();
	//	Operations.copy(luwrain, strings, tasks, filesToCopy, copyTo);
	return true;
    }

    public boolean move(int panelSide)
    {
	File[] filesToMove = null;
	File moveTo = null;
	if (panelSide == PanelArea.LEFT)
	{
	    filesToMove = leftPanel.selected();
	    moveTo= rightPanel.opened(); 
	} else
	if (panelSide == PanelArea.RIGHT)
	{
	    filesToMove = rightPanel.selected();
	    moveTo= leftPanel.opened(); 
	} else
	    return false;
	if (filesToMove == null || filesToMove.length < 1|| moveTo == null)
	    return false;
	FilePopup popup = new FilePopup(luwrain, strings.movePopupName(),
					strings.movePopupPrefix(filesToMove), moveTo);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	moveTo = popup.getFile();
	return true;
    }

    public boolean mkdir(int panelSide)
    {
	File createIn = panelSide == PanelArea.LEFT?leftPanel.opened():rightPanel.opened();
	if (createIn == null)
	    return false;
	FilePopup popup = new FilePopup(luwrain, strings.mkdirPopupName(),
					strings.mkdirPopupPrefix(), createIn);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	return true;
    }

    public boolean delete(int panelSide)
    {
	File[] filesToDelete = panelSide == PanelArea.LEFT?leftPanel.selected():rightPanel.selected();
	if (filesToDelete == null || filesToDelete.length < 1)
	    return false;
	YesNoPopup popup = new YesNoPopup(luwrain, strings.delPopupName(),
					strings.delPopupPrefix(filesToDelete), false);
	luwrain.popup(popup);
	if (popup.closing.cancelled())
	    return true;
	return true;
    }



    public void refresh()
    {
	leftPanel.refresh();
	rightPanel.refresh();
    }

    public void openFiles(String[] fileNames)
    {
	Log.debug("commander", "need to open " + fileNames.length + " files");
	if (fileNames != null && fileNames.length > 0)
	    luwrain.openFiles(fileNames);
    }

    public AreaLayout getAreasToShow()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT_BOTTOM, leftPanel, rightPanel, operations);
    }

    public void gotoLeftPanel()
    {
	luwrain.setActiveArea(leftPanel);
    }

    public void gotoRightPanel()
    {
	luwrain.setActiveArea(rightPanel);
    }

    public void gotoTasks()
    {
	luwrain.setActiveArea(operations);
    }

    public void close()
    {
	luwrain.closeApp();
    }
}
