
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
