
package org.luwrain.app.commander;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
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
	NullCheck.notNull(createIn, "createIn");
	final Path p = Popups.path(luwrain,
				   strings.mkdirPopupName(), strings.mkdirPopupPrefix(), createIn, (path)->{
				       NullCheck.notNull(path, "path");
				       if (Files.exists(path))
				       {
					   luwrain.message("Указанный файл существует", Luwrain.MESSAGE_ERROR);
					   return false;
				       }
				       return true;
				   });
	if (p == null)
	    return null;
	try {
	    Files.createDirectories(p);
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    luwrain.message(strings.mkdirErrorMessage() + ":" + e.getMessage(), Luwrain.MESSAGE_ERROR);
	    return null;
	}
	luwrain.message(strings.mkdirOkMessage(p.getFileName().toString()), Luwrain.MESSAGE_OK);
	return p;
    }

    boolean copy(CommanderArea copyFromArea, CommanderArea copyToArea, OperationsArea operationsArea)
    {
	NullCheck.notNull(copyFromArea, "copyFromArea");
	NullCheck.notNull(copyToArea, "copyToArea");
	NullCheck.notNull(operationsArea, "operationsArea");
	final Path copyFromDir = copyFromArea.opened();
	final Path[] pathsToCopy = entriesToProcess(copyFromArea);
	final Path copyTo = copyToArea.opened();
	if (pathsToCopy.length < 1)
	    return false;
	final Path dest = Popups.path(luwrain,
				      strings.copyPopupName(), copyPopupPrefix(pathsToCopy),
				      copyTo, copyFromDir,
				      (path)->{
					  NullCheck.notNull(path, "path");
					  return true;
				      },
				      Popups.loadFilePopupFlags(luwrain), Popups.DEFAULT_POPUP_FLAGS);
	if (dest == null)
	    return true;
 	operationsArea.launch(Operations.copy(operationsArea, copyOperationName(pathsToCopy, dest), pathsToCopy, dest));
	return true;
    }

    boolean move(CommanderArea moveFromArea, CommanderArea moveToArea, OperationsArea operationsArea)
    {
	NullCheck.notNull(moveFromArea, "moveFromArea");
	NullCheck.notNull(moveToArea, "moveToArea");
	NullCheck.notNull(operationsArea, "operationsArea");
	final Path moveFromDir = moveFromArea.opened();
	final Path[] pathsToMove = entriesToProcess(moveFromArea);
	final Path moveTo = moveToArea.opened();
	if (pathsToMove.length < 1)
	    return false;
	final Path dest = Popups.path(luwrain,
				      strings.movePopupName(), movePopupPrefix(pathsToMove),
				      moveTo, moveFromDir,
				      (path)->{
					  NullCheck.notNull(path, "path");
					  return true;
				      },
				      Popups.loadFilePopupFlags(luwrain), Popups.DEFAULT_POPUP_FLAGS);
	if (dest == null)
	    return true;
 	operationsArea.launch(Operations.move(operationsArea, moveOperationName(pathsToMove, dest), pathsToMove, dest));
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

    boolean runWithShortcut(Path[] selected)
    {
	NullCheck.notNullItems(selected, "selected");
	final String[] shortcuts = luwrain.getAllShortcutNames();
	Popups.fixedList(luwrain, "Выберите приложение:", shortcuts);
	return true;
    }

    boolean onCopyToClipboard(CommanderArea area)
    {
	NullCheck.notNull(area, "area");
	final Path[] marked = area.marked();
	if (marked.length > 0)
	{
	    final LinkedList<String> fileNames = new LinkedList<String>();
	    for(Path p: marked)
		fileNames.add(p.getFileName().toString());
	    luwrain.setClipboard(new RegionContent(fileNames.toArray(new String[fileNames.size()]), marked));
	    return true;
	}
	final CommanderArea.Entry entry = area.selectedEntry();
	if (entry == null || entry.type() == CommanderArea.Entry.Type.PARENT)
	    return false;
	final Path path = entry.path();
	luwrain.setClipboard(new RegionContent(new String[]{path.getFileName().toString()}, new Object[]{path}));
	return true;
    }

    static Path[] entriesToProcess(CommanderArea area)
    {
	NullCheck.notNull(area, "area");
	final Path[] marked = area.marked();
	if (marked.length > 0)
	    return marked;
	final CommanderArea.Entry entry = area.selectedEntry();
	if (entry == null || entry.type() == CommanderArea.Entry.Type.PARENT)
	    return new Path[0];
	return new Path[]{entry.path()};
    }

    private String copyPopupPrefix(Path[] pathsToCopy)
	{
	    return strings.copyPopupPrefix(pathsToCopy.length > 1?luwrain.i18n().getNumberStr(pathsToCopy.length, "items"):pathsToCopy[0].getFileName().toString());
	}

    private String movePopupPrefix(Path[] pathsToMove)
	{
	    return strings.movePopupPrefix(pathsToMove.length > 1?luwrain.i18n().getNumberStr(pathsToMove.length, "items"):pathsToMove[0].getFileName().toString());
	}

    private String copyOperationName(Path[] pathsToCopy, Path copyTo)
    {
	if (pathsToCopy.length < 1)
	    return "";
	if (pathsToCopy.length > 1)
	    return strings.copyOperationName(pathsToCopy[0].getFileName().toString() + ",...", copyTo.toString());
	return strings.copyOperationName(pathsToCopy[0].getFileName().toString(), copyTo.toString());
    }

    private String moveOperationName(Path[] pathsToMove, Path moveTo)
    {
	if (pathsToMove.length < 1)
	    return "";
	if (pathsToMove.length > 1)
	    return strings.moveOperationName(pathsToMove[0].getFileName().toString() + ",...", moveTo.toString());
	return strings.moveOperationName(pathsToMove[0].getFileName().toString(), moveTo.toString());
    }
}
