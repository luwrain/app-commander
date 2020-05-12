
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

final class App extends AppBase<Strings>
{
    final String startFrom;
        final Vector<FilesOperation> operations = new Vector<FilesOperation>();

    private Conversations conv = null;
    private MainLayout mainLayout = null;

    App()
    {
	this(null);
    }

    App(String startFrom)
    {
	super(Strings.NAME, Strings.class);
	if (startFrom != null && !startFrom.isEmpty())
	    this.startFrom = startFrom; else
	    this.startFrom = null;
    }

    @Override public boolean onAppInit()
    {
	this.conv = new Conversations(this);
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return true;
    }

        boolean allOperationsFinished()
    {
	/*
	for(FilesOperation op:operations)
	    if (!op.isFinished())
		return false;
	*/
	return true;
    }


    private FilesOperation.Listener createFilesOperationListener()
    {
	return new FilesOperation.Listener(){
	    @Override public void onOperationProgress(FilesOperation operation)
	    {
		NullCheck.notNull(operation, "operation");
		NullCheck.notNull(operation, "operation");
		getLuwrain().runUiSafely(()->onOperationUpdate(operation));
	    }
	    @Override public FilesOperation.ConfirmationChoices confirmOverwrite(java.nio.file.Path path)
	    {
		NullCheck.notNull(path, "path");
		return (FilesOperation.ConfirmationChoices)getLuwrain().callUiSafely(()->conv.overrideConfirmation(path.toFile()));
	    }
	};
    }



    private void onOperationUpdate(FilesOperation operation)
    {
	NullCheck.notNull(operation, "operation");
	//	operationsArea.redraw();
	//	luwrain.onAreaNewBackgroundSound();
	if (operation.isFinished())
	{
	    if (operation.getResult().getType() == FilesOperation.Result.Type.OK)
		getLuwrain().playSound(Sounds.DONE);
	    //refreshPanels();
	}
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getLayout();
    }

    @Override public void closeApp()
    {
	if (!allOperationsFinished())
	{
	    getLuwrain().message(getStrings().notAllOperationsFinished(), Luwrain.MessageType.ERROR);
	    return;
	}
	super.closeApp();
    }
}
