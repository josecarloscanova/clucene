package org.lahab.clucene.server.searcher;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.lahab.clucene.core.BlobDirectoryFS;
import org.lahab.clucene.server.Worker;
import org.lahab.clucene.utils.CloudStorage;
import org.lahab.clucene.utils.Configuration;
/*
 * #%L
 * client
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

/**
 * Hello world!
 *
 */
public class SearcherNode extends Worker {
	public final static Logger LOGGER = Logger.getLogger(SearcherNode.class.getName());
	
	protected SearcherManager _manager = null;
	/** The directory that will write */
	private Directory _directory;
	private QueryParser _parser;
	
	protected ReaderRefresher _refresher;

	protected CloudStorage _cloudStorage;

	static {
		DEFAULTS.put("container", "clucene");
		DEFAULTS.put("stats", false);
		DEFAULTS.put("folder", "indexCache");
	}
	
	public SearcherNode(CloudStorage storage, Configuration config) throws Exception {
		super(config);
		_cloudStorage = storage;
		String containerName = _params.getString("container");
		_cloudStorage.addContainer("directory", containerName);
	    _directory = new BlobDirectoryFS(_cloudStorage.getAccount(), containerName, 
	    								 FSDirectory.open(new File(_params.getString("folder"))));

		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		_manager = new SearcherManager(_directory, new SearcherFactory());
		_refresher = new ReaderRefresher(_manager);
		_parser = new QueryParser(Version.LUCENE_36, "content", analyzer);
	}
	
	public Document[] search(String str) throws ParseException, IOException {
		IndexSearcher searcher = _manager.acquire();
		Document[] results = null;
		try {
			Query query = _parser.parse(str);
			TopDocs docs = searcher.search(query, 10);
			results = new Document[docs.totalHits > 10 ? 10 : docs.totalHits];
			for (int i = 0; i < results.length; i++) {
				results[i] = searcher.doc(docs.scoreDocs[i].doc);
			}
		} finally {
			_manager.release(searcher);
		}
		return results;	
	}

	public void stop() throws IOException {
		_refresher.stop();
		_directory.close();
	}

	public void start() throws IOException {
		_refresher.start();
	}

}
