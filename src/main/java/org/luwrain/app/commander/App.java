
package org.luwrain.app.commander;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import org.apache.commons.vfs2.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.io.*;
import org.luwrain.popups.*;

import org.luwrain.template.*;

final class App extends AppBase<Strings>
{
    enum Side {LEFT, RIGHT};
    
    final String startFrom;
        final Vector<FilesOperation> operations = new Vector<FilesOperation>();

        static private final String REGISTRY_PATH = "/org/luwrain/app/commander";

    private Settings sett = null;


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
	this.sett = RegistryProxy.create(getLuwrain().getRegistry(), REGISTRY_PATH, Settings.class);
	this.conv = new Conversations(this);
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return true;
    }

        boolean allAllOperationsFinished()
    {
	/*
	for(FilesOperation op:operations)
	    if (!op.isFinished())
		return false;
	*/
	return true;
    }

        void launch(FilesOperation op)
    {
	NullCheck.notNull(op, "op");
	operations.add(op);
	getLuwrain().executeBkg(new FutureTask(op, null));
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

    boolean closeOperation(int index)
    {
	if (index < 0 || index >= operations.size())
	    throw new IllegalArgumentException("index (" + index + ") must be positive and less than the number of operations (" + operations.size() + ")");
	if (!operations.get(index).isFinished())
	    return false;
	operations.remove(index);
	return true;
    }

    String getOperationResultDescr(FilesOperation op)
    {
	NullCheck.notNull(op, "op");
	switch(op.getResult().getType())
	{
	case OK:
	    return getStrings().opResultOk();
	case SOURCE_PARENT_OF_DEST:
	    return "Целевой каталог является подкаталогом родительского";
	case MOVE_DEST_NOT_DIR:
	    return "Целевой путь не указывает на каталог";
	case INTERRUPTED:
	    return getStrings().opResultInterrupted();
	case EXCEPTION:
	    if (op.getResult().getException() != null)
		return getLuwrain().i18n().getExceptionDescr(op.getResult().getException());
	    return "Нет информации об ошибке";
	default:
	    return "";
	}
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

    Conversations getConv()
    {
	return this.conv;
    }

    Settings getSett()
    {
	return this.sett;
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
