package org.lahab.clucene.siteConstructor;

/*
 * #%L
 * siteConstructor
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.lahab.clucene.utils.CloudStorage;
import org.lahab.clucene.utils.Configuration;
import org.lahab.clucene.utils.JSONConfiguration;
import org.lahab.clucene.utils.Parametizer;
import org.lahab.clucene.utils.ParametizerException;

import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


public class Main {
	public static String CONFIG_FILE = "config.json";
	
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("container", "pages");
		DEFAULTS.put("storageFolder", "crawl");
		DEFAULTS.put("maxPagesToFetch", "-1");
		DEFAULTS.put("nbCrawlers", 3);
		DEFAULTS.put("seed", null);
		DEFAULTS.put("azure", null);
		DEFAULTS.put("download", false);
		DEFAULTS.put("directory", null);
	}
	
	public static Parametizer _params;
	public static CloudStorage _cloudStorage;
	public static volatile long size = 0;

	private static ThreadPoolExecutor _pool;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 0 ) {
			CONFIG_FILE = args[0];
		}
		Configuration config = new JSONConfiguration(CONFIG_FILE);
		_params = new Parametizer(DEFAULTS, config);
		_cloudStorage = new CloudStorage(config.get("azure"));
		_cloudStorage.addContainer("crawler", _params.getString("container"));
		_cloudStorage.getContainer("crawler").createIfNotExist();
		
		if (_params.getBoolean("download")) {
			startDownloading();
		} else {
			startCrawling();
		}
	}

	private static void startDownloading() throws ParametizerException {
		System.out.println("start download");
		int nb = _params.getInt("nbCrawlers");
		_pool = new ThreadPoolExecutor(nb, nb, 0, TimeUnit.SECONDS,
				   new ArrayBlockingQueue<Runnable>(10, false),
				   new ThreadPoolExecutor.CallerRunsPolicy());
		DownloadJob.DIRECTORY = _params.getString("directory");
		for (ListBlobItem blobItem : _cloudStorage.getContainer("crawler").listBlobs()) {
			if (blobItem instanceof CloudBlob) {
				_pool.execute(new DownloadJob((CloudBlob)blobItem));
			}
		}
		_pool.shutdown();
		System.out.println("finished");		
	}

	private static void startCrawling() throws Exception {
		CrawlConfig crawlConfig = new CrawlConfig();
		crawlConfig.setPolitenessDelay(100);
		crawlConfig.setCrawlStorageFolder(_params.getString("storageFolder"));
		crawlConfig.setMaxPagesToFetch(_params.getInt("maxPagesToFetch"));
        
        PageFetcher pageFetcher = new PageFetcher(crawlConfig);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		
		CrawlController crawl = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);
		crawl.addSeed(_params.getString("seed"));
		crawl.start(SiteCrawler.class, _params.getInt("nbCrawlers"));
	}

	public static void addData(String name, byte[] html) throws Exception {
		CloudBlockBlob blob = _cloudStorage.getContainer("crawler").getBlockBlobReference(name);
		
		size += 1;
		InputStream source = new ByteArrayInputStream(html);
		System.out.println("Size of Blob:" + size);
		blob.upload(source, html.length);
	}
}
