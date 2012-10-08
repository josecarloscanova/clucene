package org.core;

/*
 * #%L
 * core
 * %%
 * Copyright (C) 2012 NTNU
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.core.BlobDirectory;
import org.core.BlobDirectoryFS;
import org.core.BlobFileFS;
import org.core.cache.DummyCache;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;


@RunWith(Theories.class)
public class TestBlobFileFS {

	@Rule  
	public TemporaryFolder folder = new TemporaryFolder();

	@Theory
	public void testBlobFileFS(String fileName) throws IOException {
		
		File file = folder.newFile(fileName);
		FileUtils.writeStringToFile(file, "lfezlfelpzlfpezpfpzelfepzlpfezl"); 
		BlobDirectory dir = new BlobDirectoryFS(folder.getRoot().getCanonicalPath(), null);
		BlobFileFS f = new BlobFileFS(dir, fileName, new DummyCache());
		assertEquals("Size should be equal", f.length, 
					 folder.newFile(fileName).length());
		assertEquals("We should be at the begining of the document", 0, f.position);
		if (fileName == "nonExistant") {
			f.delete();
		}
		f.close();
	}

	private void testWriteMain(byte[] data, long offset, File file) throws IOException {
		/*BlobDirectory dir = new BlobDirectoryFS("test_files/blobFileFS", null);
		BlobFileFS f = new BlobFileFS(dir, "write", new DummyCache());
		f.write(data, 0, data.length);
		f.close();*/
	}
	/*
	@Theory
	public void testWrite(byte[] data) throws IOException {

		testWriteMain(data, 0, file);
		File file = new File("test_files/blobFileFS/write");
		RandomAccessFile raf = new RandomAccessFile(file, null);
		assertTrue("Files are different", file);
		file.delete();
		
		file = new File("test_files/blobFileFS/writeExist");
		testWriteMain(data, file.length(), file);
		file.
	}
	*/
	@Test
	public void testSeek() {
		//fail("Not yet implemented");
	}

	@Test
	public void testRead() {
		//fail("Not yet implemented");
	}

	@Test
	public void testEquals() {
		//fail("Not yet implemented");
	}

	public static @DataPoints String[] fileNames = {"construct", "empty"};
}
