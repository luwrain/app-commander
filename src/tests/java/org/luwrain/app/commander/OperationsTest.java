
package org.luwrain.app.commander;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import org.junit.*;

import org.luwrain.app.commander.operations.*;

public class OperationsTest extends Assert
{
    private static final String TEST_DIR = "test-data";
    private static final String DEST_DIR = "test-dest";

    @Before public void createData()  throws IOException
    {
	mkStandardTestDir(new File(TEST_DIR));
    }

     @Test public void copy()
    {
	Copy copy = new Copy(new File[]{new File(TEST_DIR)}, new File(DEST_DIR));
	copy.run();
    }

    private void mkStandardTestDir(File f) throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	f.mkdir();
	for(char i = 'a';i < 'z';++i)
	    mkFirstLevelDir(new File(f, "test_" + i));
    }

    private void mkFirstLevelDir(File f)  throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	f.mkdir();
	for(int i = 0;i < 10;++i)
	    mkSecondLevelDir(new File(f, "test_" + i));
    }

    private void mkSecondLevelDir(File f)  throws IOException
    {
	if (f == null)
	    throw new NullPointerException("f may not be null");
	f.mkdir();
	for(int i = 0;i < 32;++i)
	    writeTestFile(new File(f, "test_file_" + i).getAbsolutePath(), f.getPath());
    }

    private void writeTestFile(String fileName, String line) throws IOException
    {
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
	if (line == null)
	    throw new NullPointerException("line may not be null");
	Path path = Paths.get(fileName);
	try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset()))
	    {
		for(int i = 0;i < 1000;++i)
		{
		    writer.write(line);
		    writer.newLine();
		}
	    }
	    }
}
