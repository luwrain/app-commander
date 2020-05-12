
package org.luwrain.app.commander.popups;

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;
import org.luwrain.app.commander.*;

public final class DestPathPopup extends FilePopup
{
    public enum Type {COPY, MOVE};

    private final Type type;
    private final Strings strings;

    public DestPathPopup(Luwrain luwrain, Strings strings, Type type,
		  File srcDir, File[] files, File destDir)
{
    super(luwrain,
	  makeName(strings),
	  makePrefix(),
	  createAcceptance(),
	  null, //startFrom
	  srcDir,
	  null,
	  Popups.DEFAULT_POPUP_FLAGS); //flags
    NullCheck.notNull(strings, "strings");
    NullCheck.notNull(type, "type");
    this.strings = strings;
    this.type = type;
}

    static private FileAcceptance createAcceptance()
    {
	return (file, announce)->{
	    return true;
	};
    }

static private String makeName(Strings strings)
{
    NullCheck.notNull(strings, "strings");
    return strings.copyPopupName();
}

static private String makePrefix()
{
    return "";
}

    private String copyPopupPrefix(File[] toCopy)
	{
	    return strings.copyPopupPrefix(toCopy.length > 1?luwrain.i18n().getNumberStr(toCopy.length, "items"):toCopy[0].getName());
	}

private String movePopupPrefix(File[] toMove)
	{
	    return strings.movePopupPrefix(toMove.length > 1?luwrain.i18n().getNumberStr(toMove.length, "items"):toMove[0].getName());
	}

}
