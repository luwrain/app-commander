
package org.luwrain.app.commander;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.luwrain.core.NullCheck;

public class TotalSize
{
    static public long getTotalSize(Path p) throws IOException
    {
	NullCheck.notNull(p, "p");
	if (Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
	    return Files.size(p);
	if (!Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
	    return 0;
	long res = 0;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(p)) {
		for (Path pp : directoryStream) 
		    res += getTotalSize(pp);
	    } 
	return res;
    }
}
