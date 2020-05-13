
package org.luwrain.app.commander;

import java.util.*;

import org.luwrain.base.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;

class OperationsAppearance implements ListArea.Appearance
{
        private final App app;
    private final Luwrain luwrain;
    private final Strings strings;


    OperationsAppearance(App app)
    {
	NullCheck.notNull(app, "app");
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
	this.app = app;
    }

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");

	final FilesOperation op = (FilesOperation)item;
	if (op.isFinished())
	{
	    app.getLuwrain().speak(app.getOperationResultDescr(op) + " " + op.getOperationName());
	    return;
	}
	final int percents = op.getPercents();
	if (percents > 0)
	    luwrain.speak("" + luwrain.i18n().getNumberStr(percents, "percents") + " " + op.getOperationName()); else
	    luwrain.speak(op.getOperationName());


    }

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	final FilesOperation op = (FilesOperation)item;
	if (op.isFinished())
	    return app.getOperationResultDescr(op);
	final int percents = op.getPercents();
	if (percents == 0)
	    return op.getOperationName() + "...";
	return  percents + "%: "+ op.getOperationName();
    }

    @Override public int getObservableLeftBound(Object item)
    {
	return 0;
    }

    @Override public int getObservableRightBound(Object item)
    {
	return getScreenAppearance(item, EnumSet.noneOf(Flags.class)).length();
    }
}
