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


import java.io.IOException;
import java.io.Serializable;

import org.core.cache.Cache;

/**
 * @author charlymolter
 * A blobFile is a super class to encapsulate all type of communication with the storing facility. 
 * subclasses:
 * <ul>
 * 	<li> {@link BlobAzure} is a straightforward way to store 
 * 		 and read your index in a Windows azure blob storage
 *  <li> {@link BlobFS} an easy way to test by storing everything on the local file system
 * </ul>
 */
public abstract class BlobFile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Cache cache;
	public BlobDirectory directory;
	public long position;
	long length;
	String name;
	
	public BlobFile(BlobDirectory blob, String name, Cache cache) throws IOException {
		assert cache != null : "You must give a non null cache";
		this.cache = cache;
		this.name = name;
		this.directory = blob;
	}
	
	abstract protected void loadInfos() throws IOException;
	
	public void seek(long pos) throws IOException {
		if (pos < 0) {
			throw new IOException();
		}
		this.position = pos;
	}
	
	protected int read(byte[] b, int offset, int size) throws IOException {
		Object obj = cache.get(Integer.valueOf(offset));
		b = null;
		if (obj != null) {
			byte[] data = (byte[])obj;
			if (data.length <= size) {
				b = data;
			}
		}
		if (b == null) {
			b = new byte[size];
			readFile(b, offset, size);
			cache.add(Integer.valueOf(offset), b);
		}
		return size;	
	}
	
	abstract protected int readFile(byte[] b, int offset, int size) throws IOException;
	
	/**
	 * There is no caching on the writing part so we just let the FS deal with the writing
	 * @param b
	 * @param offset
	 * @param size
	 * @throws IOException 
	 */
	abstract protected void write(byte[] b, int offset, int size) throws IOException;
	
	abstract public void delete() throws IOException;

	protected void close() throws IOException {
		cache.clean();
		directory.close();
	}

	abstract public long getLastModified();
	
	public long getLength() {
		return length;
	}

	abstract public void setLength(long newLength);	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BlobFile)) {
			return false;
		}
		BlobFile b = (BlobFile)obj;
		return directory.equals(b.directory) && name.equals(b.name);
		
	}



}

