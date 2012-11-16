package org.lahab.clucene.server.searcher;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
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
	
	protected IndexSearcher _index = null;
	/** The directory that will write */
	private Directory _directory;
	private QueryParser _parser;

	protected CloudStorage _cloudStorage;

	static {
		DEFAULTS.put("container", "clucene");
		DEFAULTS.put("stats", false);
	}
	
	public SearcherNode(CloudStorage storage, Configuration config) throws Exception {
		super(config);
		_cloudStorage = storage;
		String containerName = _params.getString("container");
		_cloudStorage.addContainer("directory", containerName);
	    _directory = new BlobDirectoryFS(_cloudStorage.getAccount(), containerName, new RAMDirectory());

		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		IndexReader reader = IndexReader.open(_directory);
		
		_index = new IndexSearcher(reader);
		_parser = new QueryParser(Version.LUCENE_36, "content", analyzer);
	}
	
	public Document[] search(String str) throws ParseException, IOException {
		Query query = _parser.parse(str);
		TopDocs docs = _index.search(query, 10);
		Document[] results = new Document[docs.totalHits > 10 ? 10 : docs.totalHits];
		for (int i = 0; i < results.length; i++) {
			results[i] = _index.doc(docs.scoreDocs[i].doc);
		}
		return results;	
	}

	public void stop() throws IOException {
		_index.close();
		_directory.close();
	}

	public void start() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
