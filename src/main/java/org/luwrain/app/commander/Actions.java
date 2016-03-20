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
import org.luwrain.controls.*;


interface Actions
{
    void gotoLeftPanel();
    void gotoRightPanel();
    void gotoOperations();
    void closeApp();
    boolean openReader(PanelArea.Side panelSide);
    void refreshPanels();
    boolean selectLocations(PanelArea.Side side);
    boolean hasOperations();
    Settings settings();
    boolean exitFromInfoArea();
    Action[] getPanelAreaActions(Path[] selected);
    boolean onClickInPanel(Path[] selected);
    boolean onTabInPanel(PanelArea.Side side);
    boolean onPanelAction(Event event, PanelArea.Side side, Path[] selected);
}
