

package org.luwrain.app.commander.operations;

class OperationException extends Exception
{
    private int code;
    private String path;

    public OperationException(int code)
    {
	super("");
	this.code = code;
	path = "";
    }

    public OperationException(int code, String path)
    {
	super(path);
	this.code = code;
	this.path = path;
	if (path == null)
	    throw new NullPointerException("path may not be null");
    }

    public int code()
    {
	return code;
    }

    public String path()
    {
	return path;
    }
}
