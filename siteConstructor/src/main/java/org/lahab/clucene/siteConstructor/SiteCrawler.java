package org.lahab.clucene.siteConstructor;

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

import java.util.logging.Logger;
import java.util.regex.Pattern;


import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class SiteCrawler extends WebCrawler {
	public final static Logger LOGGER = Logger.getLogger(SiteCrawler.class.getName());
	
	public final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
            + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	public static String DOMAIN = "http://en.wikipedia.org";

	
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && href.startsWith(DOMAIN);
	}
	
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		LOGGER.info("crawl:" + url);
		
        try {
			Main.addData(page.getWebURL().getPath(), page.getContentData());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
