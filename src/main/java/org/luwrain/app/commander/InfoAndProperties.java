
package org.luwrain.app.commander;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.luwrain.core.*;

public class InfoAndProperties
{
    private Luwrain luwrain;

    void init(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    void fillProperties(MutableLines lines, Path[] items)
    {
	NullCheck.notNull(lines, "lines");
	NullCheck.notNullItems(items, "items");
	for(Path p: items)
	{
	    try {
		final StringBuilder b = new StringBuilder();
		b.append(p.getFileName().toString());
		if (Files.isDirectory(p))
		    b.append(FileSystems.getDefault().getSeparator());
		b.append(" ");
		boolean symlink = false;
		boolean directory = false;
		final BasicFileAttributeView basic = Files.getFileAttributeView(p, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		if (basic != null)
		{
		    final BasicFileAttributes attr = basic.readAttributes();
		    if (attr.isDirectory())
			b.append(luwrain.i18n().getStaticStr("CommanderDirectory") + " "); else 
			if (attr.isSymbolicLink())
			    b.append(luwrain.i18n().getStaticStr("CommanderSymlink") + " "); else 
			    if (attr.isOther())
				b.append(luwrain.i18n().getStaticStr("CommanderSymlink") + " ");
		    symlink = attr.isSymbolicLink();
		    directory = attr.isDirectory();
		}
		final PosixFileAttributeView posix = Files.getFileAttributeView(p, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		if (posix != null && !symlink)
		{
		    final PosixFileAttributes attr = posix.readAttributes();
		    final Set<PosixFilePermission> perm = attr.permissions();
		    b.append(perm.contains(PosixFilePermission.OWNER_READ)?"r":"-");
		    b.append(perm.contains(PosixFilePermission.OWNER_WRITE)?"w":"-");
		    b.append(perm.contains(PosixFilePermission.OWNER_EXECUTE)?"x":"-");
		    b.append(perm.contains(PosixFilePermission.GROUP_READ)?"r":"-");
		    b.append(perm.contains(PosixFilePermission.GROUP_WRITE)?"w":"-");
		    b.append(perm.contains(PosixFilePermission.GROUP_EXECUTE)?"x":"-");
		    b.append(perm.contains(PosixFilePermission.OTHERS_READ)?"r":"-");
		    b.append(perm.contains(PosixFilePermission.OTHERS_WRITE)?"w":"-");
		    b.append(perm.contains(PosixFilePermission.OTHERS_EXECUTE)?"x":"-");
		    b.append(" ");
		    b.append(attr.owner() + " ");
		    b.append(attr.group() + " ");
		}
		if (basic != null && !symlink)
		{
		    final BasicFileAttributes attr = basic.readAttributes();
		    b.append(directory?attr.creationTime():attr.lastModifiedTime() + " ");
		}
		lines.addLine(new String(b));
	    }
	    catch (Exception e)
	    {
		lines.addLine(p.getFileName().toString() + ":FIXME:ERROR:" + e.getMessage());
		Log.error("commander", p.toString() + ":" + e.getMessage());
		e.printStackTrace();
	    }
	}
	lines.addLine("");
    }

    boolean calcSize(Path[] paths)
    {
	NullCheck.notNullItems(paths, "paths");
	if (paths.length < 1)
	    return false;
	new Thread(()->{
		   long res = 0;
		   try {
		       for(Path p: paths)
			   res += getTotalSize(p);
		   }
		   catch(IOException e)
		   {
		       luwrain.crash(e);
		       return;
		   }
    final long finalRes = res;
	luwrain.runInMainThread(()->luwrain.message(formatSize(finalRes), Luwrain.MESSAGE_OK));
	}).start();
	return true;
}

    static private String formatSize(long size)
    {
	if (size >= ((long)1024 * 1024 * 1024 * 10))
	{
	    final long value = size / (1024 * 1024 * 1024);
	    return "" + value + "G";
	}
	if (size >= ((long)1048576 * 10))
	{
	    final long value = size / 1048576;
	    return "" + value + "M";
	}
	if (size >= ((long)1024 * 10))
	{
	    final long value = size / 1024;
	    return "" + value + "K";
	}
	return "" + size;
    }

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
