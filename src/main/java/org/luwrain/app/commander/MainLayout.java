
package org.luwrain.app.commander;

import java.util.*;
import java.io.*;
import java.io.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;
import org.luwrain.popups.*;

import org.luwrain.app.commander.Base.Side;
import org.luwrain.template.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    private Luwrain luwrain = null;
    private final PanelArea leftPanel;
    private final PanelArea rightPanel;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
 	this.leftPanel = new PanelArea(createLeftPanelParams(), app.getLuwrain(), null) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onSystemEvent(event);
		}
	    };

 	this.rightPanel = new PanelArea(createRightPanelParams(), app.getLuwrain(), null) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onSystemEvent(event);
		}
	    };

	leftPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		luwrain.runUiSafely(()->leftPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });
	rightPanel.setLoadingResultHandler((location, data, selectedIndex, announce)->{
		luwrain.runUiSafely(()->rightPanel.acceptNewLocation(location, data, selectedIndex, announce));
	    });
	leftPanel.openInitial(app.startFrom);
	rightPanel.openInitial(app.startFrom);

    }

	/*
	case INTRODUCE:
	    luwrain.playSound(Sounds.INTRO_REGULAR);
	    switch(side)
	    {
	    case LEFT:
		luwrain.speak(strings.leftPanelName() + " " + panel.getAreaName());
		break;
	    case RIGHT:
		luwrain.speak(strings.rightPanelName() + " " + panel.getAreaName());
		break;
	    }
	    return true;
	*/

    private CommanderArea.Params createLeftPanelParams()
    {
	return null;
    }

        private CommanderArea.Params createRightPanelParams()
    {
	return null;
    }


	

	AreaLayout getLayout()
	{
	    return new AreaLayout(AreaLayout.LEFT_RIGHT, leftPanel, rightPanel);
	}

	}
