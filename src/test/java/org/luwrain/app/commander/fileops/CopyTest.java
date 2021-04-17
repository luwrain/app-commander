/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.commander.fileops;

import java.io.*;
import java.nio.file.*;

import org.junit.*;

import org.luwrain.core.*;

public class CopyTest extends Assert
{
    @Ignore @Test public void singleFileToEmptyDir() throws Exception
    {
	final String fileName = "testing.dat";
	final File srcFile = createSingleTestingFile(fileName, 5123456);
	final File destDir = createDestDir();
	final Copy copyOp = new Copy(new DummyListener(), "test", new Path[]{srcFile.toPath()}, destDir.toPath());
	copyOp.run();
	assertTrue(copyOp.getResult().isOk());
	assertTrue(TestingBase.calcSha1(srcFile).equals(TestingBase.calcSha1(new File(destDir, fileName))));
    }

    @Ignore @Test public void singleFileToNonExistingPlace() throws Exception
    {
	final String fileName = "testing.dat";
	final File srcFile = createSingleTestingFile(fileName, 5123456);
	final File destDir = createDestDir();
	final File destFile = new File(destDir, fileName);
	final Copy copyOp = new Copy(new DummyListener(), "test", new Path[]{srcFile.toPath()}, destFile.toPath());
	copyOp.run();
	assertTrue(copyOp.getResult().isOk());
	assertTrue(TestingBase.calcSha1(srcFile).equals(TestingBase.calcSha1(destFile)));
    }

    @Ignore @Test public void singleFileToNonExistingPlaceInNonExistingDir() throws Exception
    {
	final String fileName = "testing.dat";
	final File srcFile = createSingleTestingFile(fileName, 5123456);
	final File destDir = createDestDir();
	final File nonExistingDir = new File(destDir, "non-existing");
	final File destFile = new File(nonExistingDir, fileName);
	final Copy copyOp = new Copy(new DummyListener(), "test", new Path[]{srcFile.toPath()}, destFile.toPath());
	copyOp.run();
	assertTrue(copyOp.getResult().isOk());
	assertTrue(TestingBase.calcSha1(srcFile).equals(TestingBase.calcSha1(destFile)));
    }

    @Ignore @Test public void twoFilesToEmptyDir() throws Exception
    {
	final String fileName1 = "testing1.dat";
	final String fileName2 = "testing2.dat";
	final File srcFile1 = createSingleTestingFile(fileName1, 5123456);
	final File srcFile2 = createSingleTestingFile(fileName2, 5123456);
	final File destDir = createDestDir();
	final Copy copyOp = new Copy(new DummyListener(), "test", new Path[]{srcFile1.toPath(), srcFile2.toPath()}, destDir.toPath());
	copyOp.run();
	assertTrue(copyOp.getResult().isOk());
	assertTrue(TestingBase.calcSha1(srcFile1).equals(TestingBase.calcSha1(new File(destDir, fileName1))));
	assertTrue(TestingBase.calcSha1(srcFile2).equals(TestingBase.calcSha1(new File(destDir, fileName2))));
    }

    @Ignore @Test public void twoFilesToNonExistingPlaceInNonExistingDir() throws Exception
    {
	final String fileName1 = "testing1.dat";
	final String fileName2 = "testing2.dat";
	final File srcFile1 = createSingleTestingFile(fileName1, 5123456);
	final File srcFile2 = createSingleTestingFile(fileName2, 5123456);
	final File destDir = createDestDir();
	final File nonExistingPlace1 = new File(destDir, "non-existing1");
	final File nonExistingPlace2 = new File(nonExistingPlace1, "non-existing2");
	final Copy copyOp = new Copy(new DummyListener(), "test", new Path[]{srcFile1.toPath(), srcFile2.toPath()}, nonExistingPlace2.toPath());
	copyOp.run();
	assertTrue(copyOp.getResult().isOk());
	assertTrue(TestingBase.calcSha1(srcFile1).equals(TestingBase.calcSha1(new File(nonExistingPlace2, fileName1))));
	assertTrue(TestingBase.calcSha1(srcFile2).equals(TestingBase.calcSha1(new File(nonExistingPlace2, fileName2))));
    }

        //FIXME:copy single dir to existing dir
    //FIXME:copy single dir to non existing dir
    //FIXME:copy multiple dirs to non existing dir
    //FIXME:copy multiple dirs to existing dirs
    //FIXME:symlinks
    //FIXME:non existing dest, must be an error

    private File createSingleTestingFile(String fileName, int len) throws IOException
    {
	TestingBase.TMP_DIR.mkdir();
	final File srcDir = new File(TestingBase.TMP_DIR, "src");
	srcDir.mkdir();
	final File file = new File(srcDir, fileName);
	final TestingBase base = new TestingBase();
	base.writeRandFile(file, len);
	return file;
    }

    private File createDestDir() throws IOException
    {
	TestingBase.TMP_DIR.mkdir();
	final File destDir = new File(TestingBase.TMP_DIR, "dest");
	destDir.mkdir();
	return destDir;
    }
}
