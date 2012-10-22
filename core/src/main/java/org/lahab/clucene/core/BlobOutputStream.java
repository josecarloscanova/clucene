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


import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.lucene.store.IndexOutput;

import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.StorageException;

public class BlobOutputStream extends IndexOutput {
	protected BlobDirectoryFS directory;
	protected CloudBlobContainer container;
	protected CloudBlockBlob blob;
	protected String name;
	  
	protected IndexOutput output;
	protected Lock mutex = new ReentrantLock();
	
	public BlobOutputStream(BlobDirectoryFS dir, CloudBlockBlob blob) {
		mutex.lock();
		try {
			directory = dir;
			container = dir.getBlobContainer();
			this.blob = blob;
			name = blob.getName();
			System.out.println("Opening OutputStream " + blob.getName());
			output = directory.getCacheDirectory().createOutput(name);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			mutex.unlock();
		}
	}
	
	@Override
	public void flush() {
		try {
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		mutex.lock();
		try {
			String fname = name;
			output.flush();
			long length = output.length();
			output.close();
			// Difference
			System.out.println("Size of the upload: " + length);
			InputStream bStream = directory.openCachedInputAsStream(fname);
			System.out.println("Uploading cache version of: " + fname);
			blob.upload(bStream, length);
			System.out.println("PUT finished for: " + fname);
		
			bStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			mutex.unlock();
		}
	}
	
	
	@Override
	public long length() {
		try {
			return output.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public void writeByte(byte b) {
		try {
			output.writeByte(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeBytes(byte[] b, int offset, int len) {
		try {
			output.writeBytes(b, offset, len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeBytes(byte[] b, int offset) {
		try {
			output.writeBytes(b, offset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public long getFilePointer() {
		return output.getFilePointer();
	}
	
	@Override
	public void seek(long pos) {
		try {
			output.seek(pos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
}