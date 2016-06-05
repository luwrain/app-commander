
package org.luwrain.app.commander;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.luwrain.core.*;
import org.luwrain.popups.*;
import org.luwrain.app.commander.operations.Operations;

class Base
{
    enum Side {LEFT, RIGHT};

    static private final String REGISTRY_PATH = "/org/luwrain/app/commander";

    private Luwrain luwrain;
    private Strings strings;
    private Settings settings = null;

    public boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	settings = RegistryProxy.create(luwrain.getRegistry(), REGISTRY_PATH, Settings.class);
	return true;
    }

    Settings settings()
    {
	return settings;
    }

    //Returns what exactly has been created
    Path mkdir(Path createIn)
    {
	Path p = Popups.chooseFile(luwrain,
				   strings.mkdirPopupName(), strings.mkdirPopupPrefix(),
				   createIn, createIn,
				   DefaultFileAcceptance.Type.NOT_EXISTING);
	if (p == null)
	    return null;
	try {
	    Files.createDirectories(p);
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    luwrain.message(strings.mkdirErrorMessage(), Luwrain.MESSAGE_ERROR);
	    return null;
	}
	luwrain.message(strings.mkdirOkMessage(p.getFileName().toString()), Luwrain.MESSAGE_OK);
	return p;
    }

	//returns true if copying begings;
    boolean copy(OperationsArea operations, Path copyFromDir,
	      Path[] filesToCopy, Path copyTo)
    {
	NullCheck.notNull(operations, "operations");
	NullCheck.notNull(copyFromDir, "copyFromDir");
	NullCheck.notNullItems(filesToCopy, "filesToCopy");
	NullCheck.notNull(copyTo, "copyTo");
	final Path dest = Popups.chooseFile(luwrain,
				      strings.copyPopupName(), strings.copyPopupPrefix(filesToCopy),
					    copyTo, copyFromDir,
					    DefaultFileAcceptance.Type.ANY);
	if (dest == null)
	    return false;
 	operations.launch(Operations.copy(operations, strings.copyOperationName(filesToCopy, dest), 
					  filesToCopy, dest));
	return true;
    }


    boolean openReader(File[] files)
    {
	NullCheck.notNull(files, "files");
	for(File f: files)
	    if (f.isDirectory())
	    {
		luwrain.message("Просмотр не применим к каталогам", Luwrain.MESSAGE_ERROR);
		return false;
	    }
	final Object  o = luwrain.getSharedObject("luwrain.reader.formats");
	if (!(o instanceof String[]))
	    return false;
	final String[] formats = (String[])o;
	final String[] formatsStr = new String[formats.length];
	for(int i = 0;i < formats.length;++i)
	{
	    final int pos = formats[i].indexOf(":");
	    if (pos < 0 || pos + 1 >= formats[i].length())
	    {
		formatsStr[i] = formats[i];
		continue;
	    }
	    formatsStr[i] = formats[i].substring(pos + 1);
	}
	final Object selected = Popups.fixedList(luwrain, "Выберите формат для просмотра:", formatsStr);
	if (selected == null)
	    return false;
	String format = null;
	for(int i = 0;i < formatsStr.length;++i)
	    if (selected == formatsStr[i])
		format = formats[i];
	if (format == null)
	    return false;
	final int pos = format.indexOf(":");
	if (pos < 1)
	    return false;
	final String id = format.substring(0, pos);
	for(File f: files)
	    luwrain.launchApp("reader", new String[]{
		    f.getAbsolutePath(),
		    id,
		});
	return true;
    }

    boolean onClickInFiles(Path[] selected)
    {
    	final String fileNames[] = new String[selected.length];
	for(int i = 0;i < selected.length;++i)
	    fileNames[i] = selected[i].toString();
	luwrain.openFiles(fileNames);
	return true;

    }

    /*
    private boolean calcSize()
    {
	final File[] f = selectedAsFiles();
	if (f == null || f.length < 1)
	    return false;
	long res = 0;
	try {
	    for(File ff: f)
		res += org.luwrain.app.commander.operations.TotalSize.getTotalSize(ff.toPath());
	}
	catch (Throwable e)
	{
	    e.printStackTrace();
	    luwrain.message("Невозможно получить необходимый доступ к файлам, возможно, недостаточно прав доступа", Luwrain.MESSAGE_ERROR);
	    return true;
	}
	luwrain.message(strings.bytesNum(res), Luwrain.MESSAGE_DONE);
	return true;
    }

    private boolean openZip(Path path)
    {
	final Map<String, String> prop = new HashMap<String, String>();
	prop.put("encoding", actions.settings().getZipFilesEncoding("UTF-8"));
	try {
	    final URI zipfile = URI.create("jar:file:" + path.toString().replaceAll(" ", "%20"));
	    final FileSystem fs = FileSystems.newFileSystem(zipfile, prop);
	    open(fs.getPath("/"), null);
	    return true;
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	    return false;
	}
    }
    */

    void fillInfo(MutableLines lines, Path[] items)
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
			b.append(luwrain.i18n().staticStr(LangStatic.COMMANDER_DIRECTORY) + " "); else 
			if (attr.isSymbolicLink())
			    b.append(luwrain.i18n().staticStr(LangStatic.COMMANDER_SYMLINK) + " "); else 
			    if (attr.isOther())
				b.append(luwrain.i18n().staticStr(LangStatic.COMMANDER_SYMLINK) + " ");
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

    boolean runWithShortcut(Path[] selected)
    {
	NullCheck.notNullItems(selected, "selected");
	final String[] shortcuts = luwrain.getAllShortcutNames();
	Popups.fixedList(luwrain, "Выберите приложение:", shortcuts);
	return true;
    }
}
