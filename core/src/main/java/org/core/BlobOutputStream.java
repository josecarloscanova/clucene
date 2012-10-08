package org.core;


import java.io.IOException;

import org.apache.lucene.store.BufferedIndexOutput;

public class BlobOutputStream extends BufferedIndexOutput {
	private final BlobFile file;
    private volatile boolean isOpen; // remember if the file is open, so that we don't try to close it more than once
    
	public BlobOutputStream(BlobFile file) {
		this.file = file;
		isOpen = true;
		// TODO Auto-generated constructor stub
	}

    /** output methods: */
    public void flushBuffer(byte[] b, int offset, int size) throws IOException {
        file.write(b, offset, size);
    }
      
    public void close() throws IOException {
        // only close the file if it has not been closed yet
        if (isOpen) {
          boolean success = false;
          try {
            super.close();
            success = true;
          } finally {
            isOpen = false;
            if (!success) {
              try {
                file.close();
              } catch (Throwable t) {
                // Suppress so we don't mask original exception
              }
            } else {
              file.close();
            }
          }
        }
    }

    /** Random-access methods */
    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
    }

    @Override
    public long length() throws IOException {
        return file.getLength();
    }

    @Override
    public void setLength(long length) throws IOException {
        file.setLength(length);
    }

}
