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
import java.util.logging.Logger;

import org.lahab.clucene.server.Worker;
import org.lahab.clucene.server.utils.CloudStorage;
import org.lahab.clucene.server.utils.Configuration;
import org.lahab.clucene.server.utils.ParametizerException;
import org.lahab.clucene.server.utils.Statable;

/**
 * A worker that is in charge of the crawling and indexing of documents.
 * @author charlymolter
 *
 */
public class IndexerNode extends Worker {
	public final static Logger LOGGER = Logger.getLogger(IndexerNode.class.getName());

	/** The crawler thread */
	protected BlobParser _crawler;
	/** The thread pools */
	protected PoolManager _pool;
	/** The indexer that encapsulates communication with the lucene index*/
	protected Indexer _indexer;

	static {
		DEFAULTS.put("downloadDir", "download");
		DEFAULTS.put("indexer", null);
		DEFAULTS.put("crawler", null);
	}
	
	/**
	 * Creates a new IndexerNode
	 * @param storage
	 * @param config
	 * @throws Exception
	 */
	public IndexerNode(CloudStorage storage, Configuration config) throws Exception {
		super(storage, config);	
		_pool = new PoolManager(config);
		_crawler = new BlobParser(storage, _pool, config.get("crawler"));
		_indexer = new Indexer(storage, _pool, config.get("indexer"));
		
		PoolJobs.INDEX = _indexer;
		
		Statable[] statables = {_indexer, _pool};
		initStats(config.get("stats"), statables);
	}

	/**
	 * Download the whole index to the server's hardrive (usefull for looking at it with luke for eg)
	 * !!! If this is done on a currently indexing server this will suspend the indexing
	 * @throws Exception
	 */
	public void download() throws Exception {
		//TODO add a way to suspend/resuming the indexing when downloading
		_indexer.download(_params.getString("downloadDir"));
	}
	
	@Override
	public void start() {
		try {
			_pool.open();
			_indexer.open();		
			try {
				if (_params.getBoolean("stats")) {
					_stats.start();
				}
			} catch (ParametizerException e) {
				System.err.println("Param stats don't exist");
			}
			_crawler.start();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws IOException {
		_crawler.stop();
		try {
			_pool.shutdown();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		_indexer.close();
		try {
			if (_params.getBoolean("stats")) {
				_stats.stop();
			}
		} catch (ParametizerException e) {
			System.err.println("Param stats don't exist");
		}
		LOGGER.info("Indexer node stopped");
	}
	
}
