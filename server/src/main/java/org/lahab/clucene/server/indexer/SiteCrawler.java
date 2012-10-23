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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.tika.metadata.DublinCore;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class SiteCrawler extends WebCrawler {
	public final static Logger LOGGER = Logger.getLogger(SiteCrawler.class.getName());
	
	static Set<String> textMetadataFields = new HashSet<String>();
	
	public final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
            + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	public static String DOMAIN = null;

	static {
		textMetadataFields.add(DublinCore.TITLE.getName());
	}
	
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && href.startsWith(DOMAIN);
	}
	
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		LOGGER.info("crawl:" + url);
		
		ParseData parseData = page.getParseData();
        if (parseData instanceof HtmlParseData) {
        	HtmlParseData htmlParseData = (HtmlParseData) parseData;
 
        	Document doc = new Document();
    		
        	String content = htmlParseData.getText();
        	LOGGER.finest("Text extracted:" + content);
    		doc.add(new Field("content", content, 
					  		  Field.Store.NO, Field.Index.ANALYZED));
    		String title = htmlParseData.getTitle();
        	LOGGER.finer("Title extracted:" + title);			
    		doc.add(new Field("title", title,
    						  Field.Store.YES, Field.Index.ANALYZED));
    		
    		doc.add(new Field("URI", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
    		
			try {
				CrawlController myController = this.getMyController();
				if (myController instanceof CrawlerController) {
					((CrawlerController) myController).getQueue().put(doc);
				} else {
					assert false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
}
