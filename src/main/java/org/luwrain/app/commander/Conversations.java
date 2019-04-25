/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

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

import java.io.*;
import java.nio.file.*;

import org.luwrain.base.FilesOperation;
import org.luwrain.core.*;
import org.luwrain.popups.*;

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
	final File res = Popups.path(luwrain,
				     strings.copyPopupName(), copyPopupPrefix(filesToCopy),
				     copyTo, /*copyFromDir,*/
				     (fileToCheck, announce)->{
					 NullCheck.notNull(fileToCheck, "fileToCheck");
					 return true;
				     });
	return res != null?res:null;
    }

    File movePopup(File moveFromDir, File[] filesToMove, File moveTo)
    {
	final File res = Popups.path(luwrain,
				     strings.movePopupName(), movePopupPrefix(filesToMove),
				     moveTo, /*moveFromDir,*/
				     (fileToCheck, announce)->{
					 NullCheck.notNull(fileToCheck, "fileToCheck");
					 return true;
				     });
	return res != null?res:null;
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
