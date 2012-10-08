package org.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NoSuchDirectoryException;
import org.core.cache.Cache;
import org.core.cache.RandomCache;


public class BlobDirectoryFS extends BlobDirectory {
	File directory;
	
	public BlobDirectoryFS(String path, LockFactory lockFactory)
			throws IOException {
		super(path, lockFactory);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void init(String path) throws IOException {
		directory = new File(path);
	    if (!directory.exists()) {
	        throw new NoSuchDirectoryException("directory '" + directory + "' does not exist");
	    } else if (!directory.isDirectory()) {
	        throw new NoSuchDirectoryException("file '" + directory + "' exists but is not a directory");
	    }
	    // Exclude subdirs
	    String[] result = directory.list(new FilenameFilter() {
	        public boolean accept(File dir, String file) {
	            return !new File(dir, file).isDirectory();
	        }
	    });

	    if (result == null)
	      throw new IOException("directory '" + directory + "' exists and is a directory, but cannot be listed: list() returned null");

	    for (String str: result) {
	    	fileMap.put(str, NEW_BlobFile(str));
	    }
	}
	
	@Override
	protected BlobFile NEW_BlobFile(String name) throws IOException {
		Cache cache = new RandomCache(1024);
		return new BlobFileFS(this, name, cache);
	}

}
