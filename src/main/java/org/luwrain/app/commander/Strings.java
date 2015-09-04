/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

public interface Strings
{
    String appName();
    String leftPanel();
    String rightPanel();
    String operationsAreaName();
    String copyPopupName();
    String copyPopupPrefix(File[] files);
    String copyOperationName(File[] filesToCopy, File copyTo);
    String movePopupName();
    String movePopupPrefix(File[] files);
    String moveOperationName(File[] moveToCopy, File moveTo);
    String mkdirPopupName();
    String mkdirPopupPrefix();
    String mkdirErrorMessage();
    String mkdirOkMessage();
    String delPopupName();
    String delPopupText(File[] files);
    String delOperationName(File[] filesToDelete);
    String operationCompletedMessage(Operation op);
    String operationFinishDescr(Operation op);
    String notAllOperationsFinished();
    String cancelOperationPopupName();
    String cancelOperationPopupText(Operation op);
    String bytesNum(long num);
}
