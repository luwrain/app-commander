
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
