
package org.luwrain.app.commander;

import java.nio.file.*;
import org.luwrain.core.*;
import org.luwrain.controls.*;


interface Actions
{
    void gotoLeftPanel();
    void gotoRightPanel();
    void gotoOperations();
    void closeApp();
    void refreshPanels();
    boolean hasOperations();
    Settings settings();
    Action[] getPanelAreaActions(Path[] selected);
    boolean onClickInPanel(Path[] selected);
    boolean selectPartition(Base.Side side);
}
