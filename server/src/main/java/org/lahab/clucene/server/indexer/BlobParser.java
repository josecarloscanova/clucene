package org.lahab.clucene.server.indexer;

/*
 * #%L
 * server
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.lahab.clucene.server.utils.CloudStorage;
import org.lahab.clucene.server.utils.Configuration;
import org.lahab.clucene.server.utils.Parametizer;

import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;

/**
 * The point of this class is to parse an Azure blob container with wikipedia web pages in it
 * @author charlymolter
 *
 */
public class BlobParser implements Runnable {
	public final static Logger LOGGER = Logger.getLogger(BlobParser.class.getName());
	protected PoolManager _pool;
	private Thread _thread;
	public Parametizer _params;
	private CloudStorage _cloudStorage;
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		// Default parameters
		DEFAULTS.put("container", "pages");
	}

	public BlobParser(CloudStorage storage, PoolManager pool, Configuration config) throws Exception {
		_params = new Parametizer(DEFAULTS, config);
		_pool = pool;
		_cloudStorage = storage;
		storage.addContainer("pages", _params.getString("container"));
		_thread = new Thread(this);
	}

	@Override
	public void run() {
		LOGGER.info("indexer start");
		Thread thisThread = Thread.currentThread();
		for (ListBlobItem blobItem : _cloudStorage.getContainer("pages").listBlobs()) {
			if (thisThread != _thread) {
				break;
			}
			if (blobItem instanceof CloudBlob) {
				_pool.addCrawlJob((CloudBlob)blobItem);
			}
		}
		LOGGER.info("finished indexing all documents");
	}
	
	public void start() {
		_thread.start();
	}
	
	public void stop() throws IOException {
		LOGGER.info("Closing the crawling thread");
		_thread = null;
	}
}
