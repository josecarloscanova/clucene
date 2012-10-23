package org.lahab.clucene.indexer;

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

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * A thread that will launch a crawler and add the documents to a queue
 * @author charlymolter
 *
 */
public class CrawlerController extends CrawlController {
	public final static Logger LOGGER = Logger.getLogger(CrawlerController.class.getName());

	public static BlockingQueue<Document> _queue;
	
	/**
	 * Creates a simple controller
	 * @param seed the seed of the crawler
	 * @param storageFolder where the crawler will store its data
	 * @param queue the queue where the crawler will put the data
	 * @return a created crawler
	 * @throws Exception
	 */
	public static CrawlerController NEW_Basic(String seed, String storageFolder, BlockingQueue<Document> queue) throws Exception {
		CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(storageFolder);
        
        PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlerController crawl = new CrawlerController(config, pageFetcher, robotstxtServer, queue);
		crawl.addSeed(seed);
		return crawl;
	}
	
	public CrawlerController(CrawlConfig config, PageFetcher pageFetcher, RobotstxtServer robotstxtServer,
							 BlockingQueue<Document> queue) throws Exception {
		super(config, pageFetcher, robotstxtServer);
		_queue = queue;
	}
	
	public BlockingQueue<Document> getQueue() {
		return _queue;
	}

}
