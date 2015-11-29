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

package org.luwrain.app.commander.operations2;

class OperationException extends Exception
{
    private int code;
    private String extInfo;

    OperationException(int code, String extInfo, 
Throwable cause)
    {
	super(cause);
	this.code = code;
	this.extInfo = extInfo;
    }

    OperationException(int code, String extInfo)
    {
	this.code = code;
	this.extInfo = extInfo;
    }


    int code()
    {
	return code;
    }

    String  extInfo()
    {
	return extInfo != null?extInfo:"";
    }
}
