
package org.luwrain.app.commander;

import java.io.*;
import java.nio.file.*;

import org.luwrain.app.commander.operations.*;

public interface Strings
{
    String appName();
    String leftPanelName();
    String rightPanelName();
    String infoAreaName();
    String operationsAreaName();
    String panelActionTitle(String actionName, boolean multiple);
    String infoActionTitle(String actionName);
    String copyPopupName();
    String copyPopupPrefix(Path[] files);
    String copyOperationName(Path[] filesToCopy, Path copyTo);
    String movePopupName();
    String movePopupPrefix(File[] files);
    String moveOperationName(File[] moveToCopy, File moveTo);
    String mkdirPopupName();
    String mkdirPopupPrefix();
    String mkdirErrorMessage();
    String mkdirOkMessage(String dirName);
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
