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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.SingleInstanceLockFactory;

/**
 * @author charlymolter
 * BlobFile are not entirely in RAM 
 */
public abstract class BlobDirectory extends Directory implements Serializable {
	
	private static final long serialVersionUID = 1l;
	protected final Map<String, BlobFile> fileMap = new ConcurrentHashMap<String, BlobFile>();
	protected final AtomicLong sizeInBytes = new AtomicLong();
	protected String path = "";
	
	/** Constructs an empty {@link Directory}. */
	public BlobDirectory(String path, LockFactory lockFactory) throws IOException {	  
	  if (lockFactory == null) {
	      lockFactory = new SingleInstanceLockFactory();
	  }
	  this.path = path;
	  init(path);
	  setLockFactory(lockFactory);
	}

	/** Removes an existing file in the directory.
	 * @throws IOException if the file does not exist
	 */
	@Override
	public void deleteFile(String name) throws IOException {
	  ensureOpen();
	  BlobFile file = fileMap.remove(name);
	  if (file != null) {
	      file.directory = null;
	      file.delete();
	      sizeInBytes.addAndGet(-file.length);
	  } else {
	      throw new FileNotFoundException(name);
	  }
	}

	@Override
	public boolean fileExists(String name) throws IOException {
	    ensureOpen();
	    return fileMap.containsKey(name);
	}

	/** Returns the length in bytes of a file in the directory.
	 * @throws IOException if the file does not exist
	 */
	@Override
	public final long fileLength(String name) throws IOException {
		ensureOpen();
		BlobFile file = fileMap.get(name);
		if (file == null) {
		    throw new FileNotFoundException(name);
		}
		return file.getLength();
	}

	@Override
	public long fileModified(String name) throws IOException {
		ensureOpen();
		BlobFile file = fileMap.get(name);
		if (file == null) {
		    throw new FileNotFoundException(name);
		}
		return file.getLastModified();
	}

	@Override
	public String[] listAll() throws IOException {
		ensureOpen();
	    // NOTE: fileMap.keySet().toArray(new String[0]) is broken in non Sun JDKs,
	    // and the code below is resilient to map changes during the array population.
	    Set<String> fileNames = fileMap.keySet();
	    List<String> names = new ArrayList<String>(fileNames.size());
	    for (String name : fileNames) {
	    	names.add(name);
	    }
	    return names.toArray(new String[names.size()]);
	}

	/** Returns a stream reading an existing file. */
	@Override
	public IndexInput openInput(String name) throws IOException {
	    ensureOpen();
	    BlobFile file = fileMap.get(name);
	    if (file == null) {
	        throw new FileNotFoundException(name);
	    }
	    return new BlobInputStream("BlobInputStream(path=\"" + path + "\")" + name, file);
	}

	/** Creates a new, empty file in the directory with the given name. Returns a stream writing this file. */
	@Override
	public IndexOutput createOutput(String name) throws IOException {
	    ensureOpen();
	    BlobFile file = NEW_BlobFile(name);
	    BlobFile existing = fileMap.remove(name);
	    if (existing != null) {
	        sizeInBytes.addAndGet(-existing.length);
	        existing.directory = null;
	    }
	    fileMap.put(name, file);
	    return new BlobOutputStream(file);
	}
	
	/** Set the modified time of an existing file to now.
 	 *  @throws IOException if the file does not exist
 	 *  @deprecated Lucene never uses this API; it will be
 	 *  removed in 4.0. */
	public void touchFile(String arg0) throws IOException {
		// As it's deprecated we just assert systematically
		assert false;
	}
	
	/** Closes the store to future operations, releasing associated memory. */
	@Override
	public void close() {
		isOpen = false;
		fileMap.clear();
	}
	
	protected BlobFile NEW_BlobFile(String name) throws IOException {
		assert false;
		return null; 
	}
	
	/** 
	 * Inits and load the infos of the Directory stored at path 
	 * @throws IOException 
	 */
	abstract protected void init(String path) throws IOException;

}
