
package org.luwrain.app.commander;

import java.io.*;
import java.nio.file.*;

import org.luwrain.base.FilesOperation;
import org.luwrain.core.*;
import org.luwrain.popups.*;
import org.luwrain.app.commander.popups.*;

final class Conversations
{
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(App app)
    {
	NullCheck.notNull(app, "app");
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    File copyPopup(File copyFromDir, File[] filesToCopy, File copyTo)
    {
	NullCheck.notNull(copyFromDir, "copyFromDir");
	NullCheck.notNullItems(filesToCopy, "filesToCopy");
	NullCheck.notNull(copyTo, "copyTo");
	final DestPathPopup popup = new DestPathPopup(luwrain, strings, DestPathPopup.Type.COPY, copyFromDir, filesToCopy, copyTo);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return null;
	return popup.result();
    }

    File movePopup(File moveFromDir, File[] filesToMove, File moveTo)
    {
	NullCheck.notNull(moveFromDir, "moveFromDir");
	NullCheck.notNullItems(filesToMove, "filesToMove");
	NullCheck.notNull(moveTo, "moveTo");
	final DestPathPopup popup = new DestPathPopup(luwrain, strings, DestPathPopup.Type.MOVE, moveFromDir, filesToMove, moveTo);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return null;
	return popup.result();
    }

    File mkdirPopup(File createIn)
    {
	final File res = Popups.path(luwrain,
				     strings.mkdirPopupName(), strings.mkdirPopupPrefix(), createIn, 
				     (fileToCheck, announce)->{
					 NullCheck.notNull(fileToCheck, "fileToCheck");
					 if (fileToCheck.exists())
					 {
					     if (announce)
						 luwrain.message(strings.enteredPathExists(fileToCheck.getAbsolutePath()), Luwrain.MessageType.ERROR);
					     return false;
					 }
					 return true;
				     });
	return res != null?res:null;
    }

    boolean deleteConfirmation(File[] files)
    {
	NullCheck.notNullItems(files, "files");
	final String text = strings.delPopupText(luwrain.i18n().getNumberStr(files.length, "items"));
	final YesNoPopup popup = new YesNoPopup(luwrain, strings.delPopupName(), text, false, Popups.DEFAULT_POPUP_FLAGS);
	luwrain.popup(popup);
	if (popup.wasCancelled())
	    return false;
	return popup.result();
    }

    String ftpAddress()
    {
	return Popups.simple(luwrain, strings.ftpConnectPopupName(), strings.ftpConnectPopupText(), "ftp://");
    }

    FilesOperation.ConfirmationChoices overrideConfirmation(File file)
    {
	NullCheck.notNull(file, "file");
	final String cancel = "Прервать";
	final String overwrite = "Перезаписать";
	final String overwriteAll = "Перезаписать все";
	final String skip = "Пропустить";
	final String skipAll = "Пропустить все";
	final Object res = Popups.fixedList(luwrain, "Подтверждение перезаписи " + file.getAbsolutePath(), new String[]{overwrite, overwriteAll, skip, skipAll, cancel});
	if (res == overwrite || res == overwriteAll)
	    return FilesOperation.ConfirmationChoices.OVERWRITE;
	if (res == skip || res == skipAll)
	    return FilesOperation.ConfirmationChoices.SKIP;
	return FilesOperation.ConfirmationChoices.CANCEL;
    }
}
