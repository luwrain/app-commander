/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.linux.fileops;

import java.nio.file.*;

import org.luwrain.base.*;

class DummyListener implements FilesOperation.Listener
{
    @Override public void onOperationProgress(FilesOperation operation)
    {
    }

    @Override public FilesOperation.ConfirmationChoices confirmOverwrite(Path path)
    {
	return FilesOperation.ConfirmationChoices.SKIP;
    }
}
