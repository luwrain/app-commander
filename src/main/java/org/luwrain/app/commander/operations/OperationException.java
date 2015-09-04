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

class OperationException extends Exception
{
    private int code;
    private String extInfo;

    public OperationException(int code)
    {
	super("");
	this.code = code;
	extInfo = "";
    }

    public OperationException(int code, String extInfo)
    {
	super(extInfo);
	this.code = code;
	this.extInfo = extInfo;
	if (extInfo == null)
	    throw new NullPointerException("extInfo may not be null");
    }

    public int code()
    {
	return code;
    }

    public String  extInfo()
    {
	return extInfo != null?extInfo:"";
    }
}
