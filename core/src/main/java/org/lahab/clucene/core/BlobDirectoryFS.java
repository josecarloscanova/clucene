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
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;

public class BlobDirectoryFS extends Directory {
	private static final Logger LOGGER = Logger.getLogger(BlobDirectoryFS.class.getName());
	
	
	protected CloudBlobContainer container;
	protected CloudBlobClient client;
	protected String catalog;
	protected Directory cacheDirectory;
	
	public BlobDirectoryFS (CloudStorageAccount storageAccount) throws URISyntaxException, StorageException {
		this(storageAccount, null, null);
	}
	
	public BlobDirectoryFS (CloudStorageAccount storageAccount, String catalog, Directory cacheDirectory) throws URISyntaxException, StorageException {
		assert storageAccount != null;
		this.catalog = catalog;
		client = storageAccount.createCloudBlobClient();
		initCacheDirectory(cacheDirectory);
	}

	public void clearCache() throws IOException {
		for(String file : cacheDirectory.listAll()) {
			cacheDirectory.deleteFile(file);
		}
	}
	
	public CloudBlobContainer getBlobContainer() {
		return container;
	}
	
	public Directory getCacheDirectory() {
		return cacheDirectory;
	}
	
	public void setCacheDirectory(Directory cacheDir) {
		cacheDirectory = cacheDir;
	}
	
	protected void initCacheDirectory(Directory cacheDir) throws URISyntaxException, StorageException {
		assert cacheDir != null;
		cacheDirectory = cacheDir;
		createContainer();
	}
	
	public void createContainer() throws URISyntaxException, StorageException {
		container = client.getContainerReference(catalog);
		container.createIfNotExist();
	}
	
	@Override
	public String[] listAll() {
		Set<String> fileNames = new LinkedHashSet<String>();
		for (ListBlobItem blobItem : container.listBlobs()) {
		    fileNames.add(blobItem.getUri().relativize(container.getUri()).toString());
		}
		return fileNames.toArray(new String[fileNames.size()]);
	}
	
	@Override
	public boolean fileExists(String name) {
		CloudBlockBlob blob;
		try {
			blob = container.getBlockBlobReference(name);
			blob.downloadAttributes();
			return true;
		} catch (StorageException e) {
			LOGGER.finer("The file" + name + "doesn't exist");
			return false;
		} catch (URISyntaxException e) {
			assert false;
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public long fileModified(String name) {
		CloudBlockBlob blob;
		try {
			blob = container.getBlockBlobReference(name);
			blob.downloadAttributes();
			return blob.getProperties().getLastModified().getTime();
		} catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	@Deprecated
	public void touchFile(String name) {
		assert false;
	}
	
	@Override
	public void deleteFile(String name) throws IOException{
		CloudBlockBlob blob;
		try {
			blob = container.getBlockBlobReference(name);
			blob.deleteIfExists();
			LOGGER.finer("DELETE" + name);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			// That is a fall through...
			//e.printStackTrace();
		}
		if (cacheDirectory.fileExists(name + ".blob")) {
			cacheDirectory.deleteFile(name + ".blob");
		}
		if (cacheDirectory.fileExists(name)) {
			cacheDirectory.deleteFile(name);
		}
	}

	@Override
	public long fileLength(String name) {
		CloudBlockBlob blob;
		try {
			blob = container.getBlockBlobReference(name);
			blob.downloadAttributes();
			return blob.getProperties().getLength();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			assert false;
		} catch (StorageException e) {
			e.printStackTrace();
			assert false;
		}
		return 0;
	}
	
	@Override
	public IndexOutput createOutput(String name) {
		CloudBlockBlob blob;
		try {
			blob = container.getBlockBlobReference(name);
			LOGGER.fine("Output stream on " + name + " blob: " + blob);
			return new BlobOutputStream(this, blob);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert false;
		return null;
	}
	
	@Override
	public IndexInput openInput(String name) {
		CloudBlockBlob blob;
		try {
			blob = container.getBlockBlobReference(name);
			LOGGER.fine("Trying to open stream on " + name + " blob: " + blob);
			blob.downloadAttributes();
			IndexInput stream = new BlobInputStream(this, blob);
			return stream;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//throw new FileNotFoundException();
		}
		return null;
	}
	
    public InputStream openCachedInputAsStream(String name) throws IOException {
        return new StreamInput(cacheDirectory.openInput(name));
    }

    public OutputStream createCachedOutputAsStream(String name) throws IOException {
        return new StreamOutput(cacheDirectory.createOutput(name));
    }

	@Override
	public void close() throws IOException {
        container = null;
        client = null;
    }
	
	// This will have to move in a lock factory
	protected Map<String, BlobLock> locks = new HashMap<String, BlobLock>();
	
	@Override
	public synchronized Lock makeLock(String name) {
		if (!locks.containsKey(name)) {
			LOGGER.finer("Obtaining a lock on" + name);
			BlobLock lock = new BlobLock(name, this);
			locks.put(name, lock);
			return lock;
		}
		return locks.get(name);
	}
	
	@Override
	public synchronized void clearLock(String name) {
		if (locks.containsKey(name)) {
			try {
				LOGGER.finer("Releasing a lock on" + name);
				locks.get(name).release();
				cacheDirectory.clearLock(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}