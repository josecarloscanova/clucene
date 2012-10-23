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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.lahab.clucene.server.Worker;
import org.lahab.clucene.server.utils.Configuration;

import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;

/**
 * A thread that will start crawling and indexing documents
 * @author charlymolter
 *
 */
public class IndexerNode extends Worker {
	public final static Logger LOGGER = Logger.getLogger(IndexerNode.class.getName());
	
	/** The maximum documents in the queue that are parsed but not indexed yet */
	public static int MAXDOCS = 3;
	/** The directory where the index is written when we download it */
	public static String DOWNLOAD_DIR = null;
	/** The indexer thread */
	protected Indexer _indexer;
	/** The crawler thread */
	protected CrawlerController _crawler;
	/** The number of crawling threads */
	protected int _nbCrawlers;
	/** The queue that links the crawler with the indexer */
	protected BlockingQueue<Document> _queueDocs;
	private volatile Thread _myThread;
	
	public IndexerNode(CloudStorageAccount storageAccount, String container, String seed, int nbCrawlers, String storageFolder) throws Exception {
		_queueDocs = new LinkedBlockingQueue<Document>(MAXDOCS);
		_indexer = new Indexer(storageAccount, container, _queueDocs);
		_nbCrawlers = nbCrawlers;
		_crawler = CrawlerController.NEW_Basic(seed, storageFolder, _queueDocs);
		_myThread = new Thread(this);
	}
	
	public IndexerNode(Configuration _config) throws Exception {
		this(_config.getStorageAccount(), _config.getContainer(),
			 _config.getSeed(), _config.getNbCrawler(), _config.getCrawlerFolder());
	}

	/**
	 * Download the whole index to the server's hardrive (usefull for looking at it with luke for eg)
	 * !!! If this is done on a currently indexing server this will suspend the indexing
	 * @throws Exception
	 */
	public void download() throws Exception {
		//TODO add a way to suspend/resuming the indexing when downloading
		//_crawler.wait();
		_indexer.download(DOWNLOAD_DIR);
		//_crawler.notify();
	}
	
	@Override
	public void run() {
		_indexer.start();
		_crawler.startNonBlocking(WikipediaCrawler.class, _nbCrawlers);
	}
	

	public void stop() throws IOException {
		_myThread = null;
		_crawler.Shutdown();
		_indexer.stop();
	}
	
	public void start() {
		_myThread.start();
	}
	
	
	
}
