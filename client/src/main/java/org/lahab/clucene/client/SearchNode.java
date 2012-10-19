package org.lahab.clucene.client;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.mortbay.jetty.Server;

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
public class SearchNode 
{
	
	protected IndexReader _index;
	protected IndexSearcher _searcher;
	protected Server _server;
	protected SearchHandler _handler;
	protected QueryParser _queryParser;
	
    public static void main( String[] args ) throws Exception
    {
    	//TODO
    	throw new Exception("Not yet implemented");
    }
    
	static public SearchNode NEW_SearchNode(Analyzer analyzer, Directory dir, 
		Server server, SearchHandler handler) throws Exception {

		IndexReader r = IndexReader.open(dir);
		QueryParser qp = new QueryParser(Version.LUCENE_36, "title", analyzer);
		SearchNode node = new SearchNode(r, qp, server, handler);
		
		server.addHandler(handler);
		handler.setNode(node);
		
		return node;		
	}
	
	public SearchNode(IndexReader index, QueryParser qp, Server server, SearchHandler handler) throws Exception {
		_index = index;
		_searcher = new IndexSearcher(_index);
		_queryParser = qp;
		_server = server;
		_handler = handler;
	}
	
	public ScoreDoc[] search(String query) throws IOException, ParseException, org.apache.lucene.queryParser.ParseException {
		IndexReader tmp = IndexReader.openIfChanged(_index);
		if (tmp != null) {
			_index = tmp;
			_searcher = new IndexSearcher(_index);
		}
		
		Query q = _queryParser.parse(query);
		TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
		_searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		return hits;
	}
	
	public Document doc(int docId) throws CorruptIndexException, IOException {
		return _searcher.doc(docId);
	}    
}
