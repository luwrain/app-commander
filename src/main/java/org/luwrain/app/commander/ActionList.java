
package org.luwrain.app.commander;

import java.util.*;
import org.apache.commons.vfs2.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

class ActionList
{private final Strings strings;

    ActionList(Strings strings)
    {
	NullCheck.notNull(strings, "strings");
	this.strings = strings;
    }

    Action[] getPanelAreaActions(PanelArea area)
    {
	NullCheck.notNull(area, "area");
	final Action hiddenShow = new Action("hidden-show", strings.actionHiddenShow(), new KeyboardEvent('=')); 
	final Action hiddenHide = new Action("hidden-hide", strings.actionHiddenHide(), new KeyboardEvent('-')); 
	final FileObject[] toProcess = area.getFileObjectsToProcess();
	if (toProcess.length < 1)
	    return new Action[]{
		new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
		new Action("open-ftp", "Подключиться к FTP-серверу"), 
		new Action("volume-info", "Показать информацию о разделе", new KeyboardEvent(KeyboardEvent.Special.F10)), 
		hiddenShow,
		hiddenHide,
	    };
	return new Action[]{
	    new Action("copy", strings.actionCopy(), new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("move", strings.actionMove(), new KeyboardEvent(KeyboardEvent.Special.F6)),
	    new Action("mkdir", strings.actionMkdir(), new KeyboardEvent(KeyboardEvent.Special.F7)),
	    new Action("delete", strings.actionDelete(), new KeyboardEvent(KeyboardEvent.Special.DELETE)),
	    new Action("open", strings.actionOpen()),
	    new Action("size", strings.actionSize(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.ALT))),
	    new Action("preview", strings.actionPreview(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("play", strings.actionPlay(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("edit-text", strings.actionEditAsText(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))),
	    new Action("preview-another-format", strings.actionPreviewAnotherFormat()),
	    new Action("open-choosing-app", strings.actionOpenChoosingApp()),
	    new Action("copy-to-clipboard", strings.actionCopyToClipboard(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.ALT))),
	    new Action("open-ftp", "Подключиться к FTP-серверу"), 
	    new Action("volume-info", "Показать информацию о разделе", new KeyboardEvent(KeyboardEvent.Special.F10)), 
	    hiddenShow,
	    hiddenHide,
	};
    }
}
