
package org.luwrain.app.commander;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import org.junit.*;

import org.luwrain.app.commander.operations.*;

public class OperationsTest extends Assert
{
    private static final String TEST_DIR = "test-data";
    private static final String DEST_DIR = "test-dest";
    private static final long EXPECTED_TOTAL_SIZE = 172644000;

    @Before public void createData()  throws IOException
    {
	mkStandardTestDir(new File(TEST_DIR));
    }

    @After public void clear()
    {
	rmdir(new File(TEST_DIR));
	rmdir(new File(DEST_DIR));
    }

    @Test public void copySameLevel() throws IOException
    {
	Copy copy = new Copy(new EmptyOperationListener(), "",
			     new File[]{new File(TEST_DIR)}, new File(DEST_DIR));
	copy.run();
	assertTrue(checkStandardTestDir(new File(DEST_DIR)));
    }

    @Test public void copyNextLevel() throws IOException
    {
	new File(DEST_DIR).mkdir();
	Copy copy = new Copy(new EmptyOperationListener(), "",
			     new File[]{new File(TEST_DIR)}, new File(DEST_DIR));
	copy.run();
	assertTrue(checkStandardTestDir(new File(new File(DEST_DIR), TEST_DIR)));
    }

    @Test public void totalSize() throws IOException
    {
	final long totalSize = TotalSize.getTotalSize(new File(TEST_DIR));
	assertTrue(totalSize == EXPECTED_TOTAL_SIZE);
    }

    private void mkStandardTestDir(File f) throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	f.mkdir();
	for(char i = 'a';i < 'z';++i)
	    mkFirstLevelDir(new File(f, "test_" + i));
    }

    private boolean checkStandardTestDir(File f) throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	for(char i = 'a';i < 'z';++i)
	    if (!checkFirstLevelDir(new File(f, "test_" + i)))
		return false;
	return true;
    }

    private void mkFirstLevelDir(File f)  throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	f.mkdir();
	for(int i = 0;i < 10;++i)
	    mkSecondLevelDir(new File(f, "test_" + i));
    }

    private boolean checkFirstLevelDir(File f)  throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	for(int i = 0;i < 10;++i)
	    if (!checkSecondLevelDir(new File(f, "test_" + i)))
		return false;
	return true;
    }


    private void mkSecondLevelDir(File f)  throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	f.mkdir();
	for(int i = 0;i < 32;++i)
	    writeTestFile(new File(f, "test_file_" + i).getAbsolutePath());
    }

    private boolean checkSecondLevelDir(File f)  throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	for(int i = 0;i < 32;++i)
	    if (!checkTestFile(new File(f, "test_file_" + i).getAbsolutePath()))
		return false;
	return true;
    }

    private void writeTestFile(String fileName) throws IOException
    {
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
	final String baseName = new File(fileName).getName();
	int counter = 1;
	Path path = Paths.get(fileName);
	try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset()))
	    {
		for(int i = 0;i < 1000;++i)
		{
		    writer.write(baseName + " line " + (counter++));
		    writer.newLine();
		}
	    }
    }

    /*
    private void writeTestFile(String fileName) throws IOException
    {
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
	final String baseName = new File(fileName).getName();
	int counter = 1;
	Path path = Paths.get(fileName);
	try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset()))
	    {
		for(int i = 0;i < 1000;++i)
		{
		    writer.write(baseName + " line " + (counter++));
		    writer.newLine();
		}
	    }
	    }
*/

    private boolean checkTestFile(String fileName) throws IOException
    {
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
	final String baseName = new File(fileName).getName();
	int counter = 1;
	Path path = Paths.get(fileName);
	try (Scanner scanner = new Scanner(path, Charset.defaultCharset().name()))
	    {
		while (scanner.hasNextLine())
		{
		    final String line = scanner.nextLine();
		    final String mustBe = baseName + " line " + (counter++);
		    if (!mustBe.equals(line))
			return false;
		}
	    }
	return counter == 1001;
    }

    private void rmdir(File f)
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	if (!f.exists())
	    return;
	if (f.isDirectory())
	{
	    File[] items = f.listFiles();
	    for(File ff: items)
		rmdir(ff);
	}
	f.delete();
    }
}
