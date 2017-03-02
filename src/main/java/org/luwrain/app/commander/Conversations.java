
package org.luwrain.app.commander;

import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;

class Conversations
{
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    File copyPopup(File copyFromDir, File[] filesToCopy, File copyTo)
    {
	final Path res = Popups.path(luwrain,
				     strings.copyPopupName(), copyPopupPrefix(filesToCopy),
				     copyTo.toPath(), copyFromDir.toPath(),
				     (path)->{
					 NullCheck.notNull(path, "path");
					 return true;
				     });
	return res != null?res.toFile():null;
    }

    File movePopup(File moveFromDir, File[] filesToMove, File moveTo)
    {
	final Path res = Popups.path(luwrain,
				     strings.movePopupName(), movePopupPrefix(filesToMove),
				     moveTo.toPath(), moveFromDir.toPath(),
				     (path)->{
					 NullCheck.notNull(path, "path");
					 return true;
				     });
	return res != null?res.toFile():null;
    }

    File mkdirPopup(File createIn)
    {
	final Path res = Popups.path(luwrain,
				     strings.mkdirPopupName(), strings.mkdirPopupPrefix(), createIn.toPath(), 
				     (path)->{
					 NullCheck.notNull(path, "path");
					 if (Files.exists(path))
					 {
					     luwrain.message(strings.enteredPathExists(path.toString()), Luwrain.MESSAGE_ERROR);
					     return false;
					 }
					 return true;
				     });
	return res != null?res.toFile():null;
    }

    String ftpAddress()
    {
	return Popups.simple(luwrain, "Подключение к FTP-серверу", "Адрес FTP-сервера:", "ftp://");
    }

private String copyPopupPrefix(File[] toCopy)
	{
	    return strings.copyPopupPrefix(toCopy.length > 1?luwrain.i18n().getNumberStr(toCopy.length, "items"):toCopy[0].getName());
	}

private String movePopupPrefix(File[] toMove)
	{
	    return strings.movePopupPrefix(toMove.length > 1?luwrain.i18n().getNumberStr(toMove.length, "items"):toMove[0].getName());
	}

String copyOperationName(File[] whatToCopy, File copyTo)
    {
	if (whatToCopy.length < 1)
	    return "";
	if (whatToCopy.length > 1)
	    return strings.copyOperationName(whatToCopy[0].getName() + ",...", copyTo.getName());
	return strings.copyOperationName(whatToCopy[0].getName(), copyTo.getName());
    }

String moveOperationName(File[] whatToMove, File moveTo)
    {
	if (whatToMove.length < 1)
	    return "";
	if (whatToMove.length > 1)
	    return strings.moveOperationName(whatToMove[0].getName() + ",...", moveTo.getName());
	return strings.moveOperationName(whatToMove[0].getName(), moveTo.getName());
    }

}
