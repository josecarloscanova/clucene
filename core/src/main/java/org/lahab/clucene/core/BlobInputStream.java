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


import java.io.OutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.lucene.store.IndexInput;

import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.StorageException;

public class BlobInputStream extends IndexInput {
	  protected BlobDirectoryFS directory;
	  protected CloudBlobContainer container;
	  protected CloudBlockBlob blob;
	  protected String name;
	  
	  protected IndexInput input;
	  private static final Logger LOGGER = Logger.getLogger(BlobInputStream.class.getName());
		

	public BlobInputStream(BlobDirectoryFS dir, CloudBlockBlob blob) throws IOException {
		try {
			name = blob.getName();
			LOGGER.fine("Opening InputStream: " + blob.getName());
			directory = dir;
			container = directory.getBlobContainer();
			this.blob = blob;
			String fname = name;
			boolean loadInCache = false;
			if (!directory.getCacheDirectory().fileExists(fname)) {
				loadInCache = true;
				LOGGER.finest("File doesn't exist in cache adding it: " + fname);
			} else if (fname.matches(".*\\.gen")) {
				// the only file that can be changed are segments.gen
				loadInCache = true;
			}
			if (loadInCache) {
				OutputStream os = directory.createCachedOutputAsStream(fname);
				try {
					LOGGER.finer("Downloading distant version of: " + fname);
					blob.download(os);
				} finally {
					os.close();
				}
			}
			input = directory.getCacheDirectory().openInput(name);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  
	  public BlobInputStream(BlobInputStream clone) {
		  directory = clone.directory;
		  container = clone.container;
		  blob = clone.blob;
		  input = (IndexInput) clone.input.clone();
	  }
	  
	  @Override
	  public byte readByte() {
		  try {
			return input.readByte();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert false;
		return 0;
	  }
	  
	  @Override
	  public void readBytes(byte[] b, int offset, int len) {
		  try {
			  input.readBytes(b, offset, len);
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  assert false;
	  }
	  
	  @Override
	  public long getFilePointer() {
		  return input.getFilePointer();
	  }
	  
	  @Override
	  public void seek(long position) {
		  try {
			input.seek(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  
	  @Override
	  public synchronized void close() {
		  try {
			LOGGER.finer("Closing local input stream for " + name);
			input.close();
			input = null;
			directory = null;
			container = null;
			blob = null;  
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	  }
	  
	  @Override
	  public long length() {
		  return input.length();
	  }
	  
	  @Override
	  public synchronized Object clone() {
		  IndexInput clone = null;
		  clone = (IndexInput) new BlobInputStream(this);
		  assert clone != null;
		  return clone;
	  }
	  
	  
  }