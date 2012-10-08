package org.lahab.clucene.core;

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
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.lahab.clucene.core.BlobDirectory;
import org.lahab.clucene.core.BlobDirectoryFS;
import org.lahab.clucene.core.BlobFileFS;
import org.lahab.clucene.core.cache.DummyCache;


@RunWith(Theories.class)
public class TestBlobFileFS {

	@Rule  
	public TemporaryFolder folder = new TemporaryFolder();
	
	private void getRandomBytes(byte[] data, int size) {
		Random rand = new Random();
		rand.nextBytes(data);
	}

	@Theory
	public void testBlobFileFS(int size) throws IOException {
		byte[] data = new byte[size];
		getRandomBytes(data, size);
		File file = folder.newFile("construct" + size);
		FileUtils.writeByteArrayToFile(file, data);
		
		BlobDirectory dir = new BlobDirectoryFS(folder.getRoot().getCanonicalPath(), null);
		
		BlobFileFS f = new BlobFileFS(dir, "construct" + size, new DummyCache());
		
		assertEquals("We should be at the begining of the document", 0, f.position);
		f.close();
	}

	
	@Theory
	public void testWrite(int size) throws IOException {
		byte[] data = new byte[size];
		getRandomBytes(data, size);
		File file = folder.newFile("writeRef" + size);
		FileUtils.writeByteArrayToFile(file, data);
		
		
		BlobDirectory dir = new BlobDirectoryFS(folder.getRoot().getCanonicalPath(), null);
		File file2 = folder.newFile("write" + size);
		BlobFileFS f = new BlobFileFS(dir, "write" + size, new DummyCache());
		
		f.write(data, 0, size);
		
		assertTrue("files differ write file is not working", FileUtils.contentEquals(file, file2));
	}
	
	@Theory
	public void testSeek(int size) throws IOException {
		byte[] data = new byte[size];
		getRandomBytes(data, size);
		File file = folder.newFile("seek" + size);
		FileUtils.writeByteArrayToFile(file, data);
		
		BlobDirectory dir = new BlobDirectoryFS(folder.getRoot().getCanonicalPath(), null);

		BlobFileFS f = new BlobFileFS(dir, "seek" + size, new DummyCache());
		
		if (size != 0) {
			int[] pos = {0, size / 2, size / 4, size / 6, 3 * size / 4, size - 1};
			for (int i :pos) {
				f.seek(i);
				assertEquals("Cursor not at the right position", f.position, i);
				assertEquals("Can't seek the correct element", data[i], f.raf.readByte());
			}
		}
	}

	@Theory
	public void testRead(int size) throws IOException {
		/*byte[] data = new byte[size];
		getRandomBytes(data, size);
		File file = folder.newFile("seek" + size);
		FileUtils.writeByteArrayToFile(file, data);
		
		BlobDirectory dir = new BlobDirectoryFS(folder.getRoot().getCanonicalPath(), null);
		BlobFileFS f = new BlobFileFS(dir, "seek" + size, new DummyCache());
		
		int[] pos = {0, size / 2, size / 4, size / 6, 3 * size / 4, size - 1};
		f.read(b, offset, size);*/

		
	}

	@Theory
	public void testEquals() {
		//fail("Not yet implemented");
	}

	public static @DataPoints int[] sizes = {0, 100, 1000, 10000};
}
