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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.lucene.store.Lock;

import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.StorageException;

public class BlobLock extends Lock {
	private static final Logger LOGGER = Logger.getLogger(BlobLock.class.getName());
	protected String lockedFile;
	protected BlobDirectoryFS directory;
	protected String leaseId;
	
	public BlobLock(String name, BlobDirectoryFS blobDirectoryFS) {
		lockedFile = name;
		directory = blobDirectoryFS;
	}

	@Override
	public boolean obtain() throws IOException {
		CloudBlockBlob blob;
		try {
			
			blob = directory.getBlobContainer().getBlockBlobReference(lockedFile);
			System.out.println("Acquiring lock for" + lockedFile);
			try {
				if (leaseId == null || leaseId == "") {
					leaseId = blob.acquireLease(null, null);
					LOGGER.finer("Acquired lock for" + lockedFile + "id:" + leaseId);
				}
				return (leaseId != null && leaseId != "");
			} catch (StorageException e1) {
				if (handleStorageException(blob, e1)) {
					return obtain();
				}
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			assert false; //peut etre que la faudra ajouter quelque chose
		} catch (StorageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return false;	
	}

	@Override
	public void release() throws IOException {
		LOGGER.finer("Locks relase" + lockedFile + "leaseId" + leaseId);
		if (leaseId != null && leaseId != "") {
			try {
				CloudBlockBlob blob = directory.getBlobContainer().getBlockBlobReference(lockedFile);
				blob.releaseLease(AccessCondition.generateLeaseCondition(leaseId));
				leaseId = null;
				LOGGER.finer("Locks relased" + lockedFile + "leaseId" + leaseId);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isLocked() throws IOException {
		CloudBlockBlob blob;
		try {
			blob = directory.getBlobContainer().getBlockBlobReference(lockedFile);

			try {
				LOGGER.finest("IsLocked?" + lockedFile + "leaseId:" + leaseId);
				if (leaseId == null || leaseId == "") {
					String tmpLeaseId = blob.acquireLease(null, null);
					if (tmpLeaseId == null || tmpLeaseId == "") {
						LOGGER.finest("Yes it is Locked" + lockedFile + "leaseId" + leaseId);
						return true;
					}
					blob.releaseLease(AccessCondition.generateLeaseCondition(tmpLeaseId));
					LOGGER.finest("IsLocked Now?" + lockedFile + "leaseId:" + leaseId);
					return leaseId == null || leaseId == "";
				}
			} catch (StorageException e1) {
				if (handleStorageException(blob, e1)) {
					return isLocked();
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			assert false; //peut etre que la faudra ajouter quelque chose
		}
		leaseId = null;
		return false;
	}

	private boolean handleStorageException(CloudBlockBlob blob, StorageException e) {
		if (e.getHttpStatusCode() == 404) {
				try {
					directory.createContainer();
					blob.upload(new ByteArrayInputStream(lockedFile.getBytes("UTF-8")), -1);
					return true;
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (StorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
		assert false; // en priant!
		return false;
		
	}

}
