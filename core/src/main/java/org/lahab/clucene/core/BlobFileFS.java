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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.lahab.clucene.core.cache.Cache;


public class BlobFileFS extends BlobFile {
	private static final long serialVersionUID = 1L;
	RandomAccessFile raf;
	File file;
	
	public BlobFileFS(BlobDirectory blob, String name, Cache cache) throws IOException {
		super(blob, name, cache);
		file = new File(blob.path, name);
		if (!file.exists()) {
			file.createNewFile();
		}
		raf = new RandomAccessFile(file, "rw");
		loadInfos();
		
	}

	@Override
	protected void loadInfos() throws IOException {
		length = raf.length();
	}

	@Override
	protected int readFile(byte[] b, int offset, int size) throws IOException {
		return raf.read(b, offset, size);
	}

	@Override
	protected void write(byte[] b, int offset, int size) throws IOException {
		raf.write(b, offset, size);
		this.length += size;
	}

	@Override
	protected void close() throws IOException {
		super.close();
		raf.close();
	}

	@Override
	public void delete() throws IOException {
		if (!file.delete()) {
			throw new IOException("can't delete the file");
		}	
	}

	@Override
	public void setLength(long newLength) {
		try {
			raf.setLength(newLength);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		length = newLength;
	}

	@Override
	public long getLastModified() {
		return file.lastModified();
	}

	@Override
	public void seek(long pos) throws IOException {
		this.position = pos;
		raf.seek(pos);
	}
}
