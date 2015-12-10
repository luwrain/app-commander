
package org.luwrain.app.commander;

import java.nio.file.Path;

import org.luwrain.core.Event;

class ConfirmationEvent extends Event
{
    private Path path;//May be null
    private boolean answer = false;

    ConfirmationEvent(Path path)
    {
	super(100);
	this.path = path;
    }

    Path path()
    {
	return path;
    }

    void setAnswer(boolean answer)
    {
	this.answer = answer;
    }

    boolean answer()
    {
	return answer;
    }
}

