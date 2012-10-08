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
import java.util.Arrays;
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
import org.lahab.clucene.core.cache.RandomCache;


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
        byte[] data = new byte[size];
        getRandomBytes(data, size);
        File file = folder.newFile("read" + size);
        FileUtils.writeByteArrayToFile(file, data);
        
        BlobDirectory dir = new BlobDirectoryFS(folder.getRoot().getCanonicalPath(), null);
        BlobFileFS f = new BlobFileFS(dir, "read" + size, new DummyCache());
        
        int[] pos = {0, size / 2, size / 4, size / 6, 3 * size / 4};
        Random rand = new Random();
        byte[] b = new byte[size];
        int rdE;
        for (int i: pos) {
            if (data.length - i > 0) {
                // Random length from the begining of the file
                rdE = rand.nextInt(data.length - i);    
                f = new BlobFileFS(dir, "read" + size, new DummyCache());
                assertEquals("We haven't read the right number of bytes",
                             rdE, f.read(b, i, rdE));
                
                assertTrue("Not reading the right parts:" + i + ":" + rdE,
                           checkArrays(data, b, i, rdE));
                
                // Random offset from the begining of the file
                rdE = rand.nextInt(data.length - i);
                f = new BlobFileFS(dir, "read" + size, new DummyCache());
                f.read(b, rdE, i);
                assertTrue("Not reading the right parts:" + i, checkArrays(data, b, rdE, i));
                
                // Not from the begining of the file
                byte[] subData = Arrays.copyOfRange(data, i, data.length - 1);
                if (i < subData.length) {
                    rdE = rand.nextInt(subData.length - i);
                    assertEquals("We haven't read the right number of bytes",
                                 rdE, f.read(b, i, rdE));
                    
                    assertTrue("Not reading the right parts:" + i + ":" + rdE,
                               checkArrays(subData, b, i, rdE));    
                }
            }
        }   
    }

    private boolean checkArrays(byte[] data, byte[] b, int offset, int len) {
        for (int j = 0; j < len; j++) {
            if (data[j] != b[j + offset]) {
                return false;    
            }
        }
        return true;
    }

    @Theory
    public void testEquals() throws IOException {
        File file = folder.newFile("equals");   
        BlobDirectory dir = new BlobDirectoryFS(folder.getRoot().getCanonicalPath(), null);
        BlobFileFS f = new BlobFileFS(dir, "equals", new DummyCache());
        assertTrue("same files should be equal", f.equals(new BlobFileFS(dir, "equals", new RandomCache(1))));
        assertFalse("same files should be equal", f.equals(null));
        File file2 = folder.newFile("equals2");
        assertFalse("different file shouldn't be equals", f.equals(new BlobFileFS(dir, "equals2", new RandomCache(1))));
    }

    public static @DataPoints int[] sizes = {0, 100, 1000, 10000};
}
