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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.lahab.clucene.utils.Configuration;
import org.lahab.clucene.utils.Parametizer;

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
	protected File _folder;
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		// Default parameters
		DEFAULTS.put("directory", "index/files/");
	}

	public BlobParser(PoolManager pool, Configuration config) throws Exception {
		_params = new Parametizer(DEFAULTS, config);
		_pool = pool;
		_folder = new File(_params.getString("directory"));
		if (!_folder.isDirectory()) {
			throw new Exception("the path given as directory for crawling is not a directory");
		}
		_thread = new Thread(this);
	}

	@Override
	public void run() {
		LOGGER.info("indexer start");
		Thread thisThread = Thread.currentThread();
		
		for (File file : _folder.listFiles()) {
			if (thisThread != _thread) {
				break;
			}
			if (file.isFile()) {
				_pool.addCrawlJob(file);
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
