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


import java.io.EOFException;
import java.io.IOException;

import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.Constants;

public class BlobInputStream extends BufferedIndexInput {
    protected final int chunkSize;
	protected BlobFile file;
	protected boolean isClone = false;
    public static final int DEFAULT_READ_CHUNK_SIZE = Constants.JRE_IS_64BIT ? Integer.MAX_VALUE : 100 * 1024 * 1024;

    public BlobInputStream(String resourceDesc, BlobFile f) throws IOException {
      super(resourceDesc, 1024);
      file = f;
      this.chunkSize = DEFAULT_READ_CHUNK_SIZE;
    }
  
    /** IndexInput methods */
    @Override
    protected void readInternal(byte[] b, int offset, int len)
         throws IOException {
      synchronized (file) {
        long position = getFilePointer();
        if (position != file.position) {
          file.seek(position);
          file.position = position;
        }
        int total = 0;

        try {
          do {
            final int readLength;
            if (total + chunkSize > len) {
              readLength = len - total;
            } else {
              // LUCENE-1566 - work around JVM Bug by breaking very large reads into chunks
              readLength = chunkSize;
            }
            final int i = file.read(b, offset + total, readLength);
            if (i == -1) {
              throw new EOFException("read past EOF: " + this);
            }
            file.position += i;
            total += i;
          } while (total < len);
        } catch (OutOfMemoryError e) {
          // propagate OOM up and add a hint for 32bit VM Users hitting the bug
          // with a large chunk size in the fast path.
          final OutOfMemoryError outOfMemoryError = new OutOfMemoryError(
              "OutOfMemoryError likely caused by the Sun VM Bug described in "
              + "https://issues.apache.org/jira/browse/LUCENE-1566; try calling FSDirectory.setReadChunkSize "
              + "with a value smaller than the current chunk size (" + chunkSize + ")");
          outOfMemoryError.initCause(e);
          throw outOfMemoryError;
        } catch (IOException ioe) {
          IOException newIOE = new IOException(ioe.getMessage() + ": " + this);
          newIOE.initCause(ioe);
          throw newIOE;
        }
      }
    }
  
    @Override
    public void close() throws IOException {
      // only close the file if this is not a clone
      if (!isClone) file.close();
    }
  
    @Override
    protected void seekInternal(long position) {
    }
  
    @Override
    public long length() {
      return file.getLength();
    }
  
    @Override
    public Object clone() {
      BlobInputStream clone = (BlobInputStream)super.clone();
      clone.isClone = true;
      return clone;
    }
    
    @Override
    public void copyBytes(IndexOutput out, long numBytes) throws IOException {
      numBytes -= flushBuffer(out, numBytes);
      // If out is FSIndexOutput, the copy will be optimized
      out.copyBytes(this, numBytes);
    }

}
