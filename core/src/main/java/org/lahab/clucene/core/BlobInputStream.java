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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	  protected Lock mutex = new ReentrantLock();

	public BlobInputStream(BlobDirectoryFS dir, CloudBlockBlob blob) throws IOException {
		
		  try {
			name = blob.getName();
			mutex.lock();
			System.out.println("Opening InputStream: " + blob.getName());
			directory = dir;
			container = directory.getBlobContainer();
			this.blob = blob;
			String fname = name;
			boolean loadInCache = false;
			if (!directory.getCacheDirectory().fileExists(fname)) {
				loadInCache = true;
				System.out.println("File doesn't exist in cache adding it: " + fname);
			} else {
				long cachedLength = directory.getCacheDirectory().fileLength(fname);
				blob.downloadAttributes();
				long blobLength = blob.getProperties().getLength();
				long cacheLastModified = directory.getCacheDirectory().fileModified(fname);
				
				long lastModified = blob.getProperties().getLastModified().getTime();
				if (cachedLength != blobLength || lastModified - cacheLastModified > 10) {
					loadInCache = true;
				}	
				System.out.println("File too old in cache refreshing it: " + fname);
			}
			if (loadInCache) {

				OutputStream os = directory.createCachedOutputAsStream(fname);
				System.out.println("Downloading distant version of: " + fname);
				blob.download(os);
				System.out.println("GET file "+ name + " retrieved " + directory.fileLength(fname));
				os.flush();
				os.close();
			}
			input = directory.getCacheDirectory().openInput(name);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            mutex.unlock();
        }
	  }
	  
	  public BlobInputStream(BlobInputStream clone) {
		  mutex.lock();
		  directory = clone.directory;
		  container = clone.container;
		  blob = clone.blob;
		  input = (IndexInput) clone.input.clone();
		  
		  mutex.unlock();
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
	  public void close() {
		  mutex.lock();
		  try {
			System.out.println("Closing local input stream for " + name);
			input.close();
			input = null;
			directory = null;
			container = null;
			blob = null;  
		  } catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  } finally {
			  mutex.unlock();
		  }
	  }
	  
	  @Override
	  public long length() {
		  return input.length();
	  }
	  
	  @Override
	  public Object clone() {
		  IndexInput clone = null;
		  mutex.lock();
		  clone = (IndexInput) new BlobInputStream(this);
		  assert clone != null;
		  mutex.unlock();
		  return clone;
	  }
	  
	  
  }