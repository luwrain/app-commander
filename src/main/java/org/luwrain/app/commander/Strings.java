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

import java.io.*;
import java.nio.file.*;

import org.luwrain.app.commander.operations.*;

public interface Strings
{
    static final String NAME = "luwrain.commander";

    String appName();
    String leftPanelName();
    String rightPanelName();
    String infoAreaName();
    String operationsAreaName();
    String panelActionTitle(String actionName, boolean multiple);
    String infoActionTitle(String actionName);
    String copyPopupName();
    String copyPopupPrefix(String copyWhat);
    String copyOperationName(String copyWhat, String copyTo);
    String movePopupName();
    String movePopupPrefix(String moveWhat);
    String moveOperationName(String moveWhat, String moveTo);
    String mkdirPopupName();
    String mkdirPopupPrefix();
    String mkdirErrorMessage();
    String mkdirOkMessage(String dirName);
    String delPopupName();
    String delPopupText(File[] files);
    String delOperationName(File[] filesToDelete);
    String operationCompletedMessage(Operation op);
    String notAllOperationsFinished();
    String cancelOperationPopupName();
    String cancelOperationPopupText(Operation op);
    String bytesNum(long num);
    String opResultOk();
    String opResultInterrupted();
    String opResultUnexpectedProblem();
    String opResultProblemCreatingDirectory(String arg);
    String opResultProblemReadingFile(String arg);
    String opResultProblemWritingFile(String arg);
    String opResultInaccessibleSource();
    String opResultProblemCreatingSymlink(String arg);
    String opResultProblemReadingSymlink(String arg);
    String opResultProblemDeleting(String arg);
    String opResultDestExistsNotRegular(String arg);
    String opResultNotConfirmedOverride(String arg);
    String opResultDestExistsNotDir(String arg);
    String opResultDestExists(String arg);
    String actionHiddenShow();
    String actionHiddenHide();
    String actionOpen();
    String actionEditAsText();
    String actionPreview();
    String actionPreviewAnotherFormat();
    String actionOpenChoosingApp();
    String actionCopy();
    String actionMove();
    String actionMkdir();
    String actionDelete();
    String actionSize();
    String dirMayNotBePreviewed();
}
