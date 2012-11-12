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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.lahab.clucene.server.utils.Configuration;
import org.lahab.clucene.server.utils.Parametizer;
import org.lahab.clucene.server.utils.StatRecorder;
import org.lahab.clucene.server.utils.Statable;

import com.microsoft.windowsazure.services.blob.client.CloudBlob;

/**
 * Handles each Thread pool 
 * This is where most of the indexing concurrency is encapsulated
 * @author charlymolter
 *
 */
public class PoolManager implements Statable {
	public final static Logger LOGGER = Logger.getLogger(PoolManager.class.getName());
	
	/** The pool of crawling jobs */
	protected ThreadPoolExecutor _poolCrawl;
	/** The pool of Indexing jobs */
	protected ThreadPoolExecutor _poolIndex;
	/** The commit thread if it exists */
	protected volatile Thread _commitThread = null;
	protected StatRecorder _stats = null;
	protected PoolJobs _jobFactory = new PoolJobs();
	
	public Parametizer _params;
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("indexer.queueSize", 10);
		DEFAULTS.put("crawler.queueSize", 10);
		DEFAULTS.put("indexer.nbThreads", 3);
		DEFAULTS.put("crawler.nbThreads", 3);
	}
	
	public PoolManager(Configuration conf) throws Exception {
		_params = new Parametizer(DEFAULTS, conf);
	}
	
	/**
	 * Stops accepting new jobs and finishes the alreadys started ones
	 * Also waits for the end of a commit if one is started
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		if (_poolCrawl != null && _poolIndex != null) {
			LOGGER.info("Shutting down crawler pool");
			shutdownPool(_poolCrawl);
			LOGGER.info("Shutting down indexer pool");
			shutdownPool(_poolIndex);
			LOGGER.info("Shutting down commit pool");
			while(_commitThread.isAlive()) {
				Thread.sleep(100);
			}
			LOGGER.info("Shutdown finished");
			_poolCrawl = null;
			_poolIndex = null;
			return;
		}
	}
	
	/**
	 * Creates all necessary pools
	 * @throws Exception
	 */
	public void open() throws Exception {
		if (_poolCrawl != null || _poolIndex != null || _commitThread != null) {
			throw new Exception("The pools should be terminated");
		}
		int nb = _params.getInt("crawler.nbThreads");
		_poolCrawl = new ThreadPoolExecutor(nb, nb, 0, TimeUnit.SECONDS,
				   new ArrayBlockingQueue<Runnable>(_params.getInt("crawler.queueSize"), false),
				   new ThreadPoolExecutor.CallerRunsPolicy());
		
		nb = _params.getInt("indexer.nbThreads");
		_poolIndex = new ThreadPoolExecutor(nb, nb, 0, TimeUnit.SECONDS,
				   new ArrayBlockingQueue<Runnable>(_params.getInt("indexer.queueSize"), false),
				   new ThreadPoolExecutor.CallerRunsPolicy());
		_commitThread = null;
	}
	
	/**
	 * shutdown gracefully one pool
	 * @param pool
	 */
	protected void shutdownPool(ExecutorService pool) {
		pool.shutdown();
		while (true) {
			try {
				if (pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}		
	}
	
	/**
	 * Adds an indexing job
	 * @param doc
	 */
	public void addIndexJob(Document doc) {
		LOGGER.fine("Add Indexing job");
		_poolIndex.execute(_jobFactory.NEW_documentIndexer(doc));
	}
	
	/**
	 * Adds a crawling job
	 * @param blobItem
	 */
	public void addCrawlJob(CloudBlob blobItem) {
		LOGGER.fine("Add Crawling job");
		_poolCrawl.execute(_jobFactory.NEW_documentParser(blobItem));
	}
	
	/**
	 * Adds a commit job if no commit is already happening
	 */
	public void addCommitJob() {
		if (_commitThread == null || !_commitThread.isAlive()) {
			LOGGER.fine("Starting a new commit thread");
			_commitThread = new Thread(_jobFactory.NEW_commitIndexer());
			_commitThread.start();
		} else {
			LOGGER.fine("commit already happening ignoring");
		}
	}
	
	@Override
	public String[] header() {
		String[] stats = {"Size crawler Queue", "Size indexer Queue", "Commiting"};
		return stats;
	}

	@Override
	public String[] record() {
		if (_poolCrawl == null || _poolIndex == null) {
			String[] stats = {"", "", "0"};
			return stats;
		}
		String[] stats = {String.valueOf(_poolCrawl.getQueue().size()), 
						  String.valueOf(_poolIndex.getQueue().size()),
						  (_commitThread != null && _commitThread.isAlive()) ? "1" : "0"};
		return stats;
	}
}
