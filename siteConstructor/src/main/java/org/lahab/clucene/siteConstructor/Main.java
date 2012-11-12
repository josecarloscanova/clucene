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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


public class Main {
	public static String CONFIG_FILE = "config.json";
	public static JSONObject _config;
	public static CloudStorageAccount _storageAccount = null;
	public static CloudBlobContainer _container;
	public static volatile long size = 0;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if (_config == null) {
			if (args.length != 0 ) {
				CONFIG_FILE = args[0];
			}
			parseConfig(CONFIG_FILE);
		}
		
		CrawlConfig config = new CrawlConfig();
		config.setPolitenessDelay(100);
        config.setCrawlStorageFolder(_config.getString("storageFolder"));
        config.setMaxPagesToFetch(_config.getInt("maxPagesToFetch"));
        
        PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		
		CrawlController crawl = new CrawlController(config, pageFetcher, robotstxtServer);
		crawl.addSeed(_config.getString("seed"));
		crawl.start(SiteCrawler.class, _config.getInt("nbCrawlers"));
	}

	public static void parseConfig(String confFile) throws Exception {
        InputStream is = new FileInputStream(new File(confFile));
        String jsonTxt = IOUtils.toString(is);
        _config = (JSONObject) JSONSerializer.toJSON(jsonTxt);
        JSONObject azureConf = _config.getJSONObject("azure");
        _storageAccount = CloudStorageAccount.parse("DefaultEndpointsProtocol="+ 
					 azureConf.getString("defaultEndpointsProtocol") +
					 ";AccountName=" +
					 azureConf.getString("accountName") +
					 ";AccountKey=" +
					 azureConf.getString("accountKey") + ";");
        
        CloudBlobClient blobClient = _storageAccount.createCloudBlobClient();

        _container = blobClient.getContainerReference(azureConf.getString("container"));
        _container.createIfNotExist();
	}

	public static void addData(String name, byte[] html) throws Exception {
		CloudBlockBlob blob = _container.getBlockBlobReference(name);
		
		size += 1;
		InputStream source = new ByteArrayInputStream(html);
		System.out.println("Size of Blob:" + size);
		blob.upload(source, html.length);
	}
}
