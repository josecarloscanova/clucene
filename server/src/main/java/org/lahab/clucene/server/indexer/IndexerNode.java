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

import org.apache.lucene.index.CorruptIndexException;
import org.lahab.clucene.server.Worker;
import org.lahab.clucene.server.utils.CloudStorage;
import org.lahab.clucene.server.utils.Configuration;
import org.lahab.clucene.server.utils.Parametizer;
import org.lahab.clucene.server.utils.ParametizerException;
import org.lahab.clucene.server.utils.StatRecorder;
import org.lahab.clucene.server.utils.Statable;



/**
 * A thread that will start crawling and indexing documents
 * @author charlymolter
 *
 */
public class IndexerNode extends Worker {
	public final static Logger LOGGER = Logger.getLogger(IndexerNode.class.getName());

	/** The crawler thread */
	protected BlobParser _crawler;
	
	protected PoolManager _pool;
	
	protected Indexer _indexer;
	
	protected StatRecorder _stats = null;
	
	public Parametizer _params;
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("downloadDir", "download");
		DEFAULTS.put("stats.file", "statsIndexerNode.csv");
		DEFAULTS.put("stats.frequency", 1000);
		DEFAULTS.put("stats", true);
	}
	
	public IndexerNode(CloudStorage storage, Configuration config) throws Exception {
		_params = new Parametizer(DEFAULTS, config);
		
		_indexer = new Indexer(storage, config.get("indexer"));
		DocumentIndexer.INDEX = _indexer;
		
		_pool = new PoolManager(config);
		_crawler = new BlobParser(storage, _pool, config.get("crawler"));
		
		Statable[] statables = {(Statable) _indexer, (Statable) _crawler, (Statable) _pool};
		if (_params.getBoolean("stats")) {
			_stats = new StatRecorder(_params.getString("stats.file"), 
									  _params.getInt("stats.frequency"), statables);
		}
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
	
	public void start() throws CorruptIndexException, IOException {
		_indexer.open();
		try {
			if (_params.getBoolean("stats")) {
				_stats.start();
			}
		} catch (ParametizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_crawler.start();
	}
	

	public void stop() throws IOException {
		_crawler.stop();
		_pool.shutdown();
		_indexer.close();
		try {
			if (_params.getBoolean("stats")) {
				_stats.stop();
			}
		} catch (ParametizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOGGER.info("Indexer node stoped");
	}
	
	
	
}
