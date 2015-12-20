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

package org.luwrain.app.commander.operations;

import java.nio.file.*;
import org.luwrain.app.commander.Operation;

class OperationException extends Exception
{
    private Operation.Result code;
    private String extInfo = "";

    OperationException(Operation.Result code, Path extInfo, 
Throwable cause)
    {
	super(cause);
	this.code = code;
	if (extInfo != null)
	    this.extInfo = extInfo.toString();
    }

    OperationException(Operation.Result code, Path extInfo)
    {
	this.code = code;
	if (extInfo != null)
	    this.extInfo = extInfo.toString();
    }

    OperationException(Operation.Result code)
    {
	this.code = code;
    }


    Operation.Result code()
    {
	return code;
    }

    String  extInfo()
    {
	return extInfo != null?extInfo:"";
    }
}
