package org.lahab.clucene.server;

import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.lahab.clucene.server.indexer.IndexerNode;
import org.lahab.clucene.server.searcher.SearcherNode;
import org.lahab.clucene.server.utils.Configuration;


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

public class NodeMain {
	private static final Logger LOGGER = Logger.getLogger(NodeMain.class.getName());
	
	public static final String CONFIG_FILE = "../config.json";
	public static Configuration _config;
	public static Worker _worker;
	public static Server _server;
	
	public static void main(String[] args) throws Exception {
		// Retrieve storage account from connection-string
		if (_config == null) {
			_config = new Configuration(CONFIG_FILE);
		}
		_config.configure();
		HttpServlet servlet = null;
		String path = null;
		
		// Creates an indexer or a searcher depending on the configuration
		if (_config.isIndexer()) {
			IndexerNode indexer = new IndexerNode(_config);
			servlet = new IndexServlet(indexer);
			path = IndexServlet.PATH;
			_worker = indexer;
		} else {
			SearcherNode searcher = new SearcherNode(_config);
			IndexerNode.DOWNLOAD_DIR = _config.getDownloadDir();
			servlet = new SearchServlet(searcher);
			path = SearchServlet.PATH;
			_worker = searcher;
		}
		
	    _server = new Server(_config.getPort());
        
	    // Creates the servlet for whatever our node is up to
        ServletContextHandler contextIndex = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextIndex.setContextPath(path);
        contextIndex.addServlet(new ServletHolder(servlet),"/*");
        
        // Creates the servlet for debug purpose
        ServletContextHandler contextDebug = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextDebug.setContextPath(DebugServlet.PATH);
        contextDebug.addServlet(new ServletHolder(new DebugServlet(_worker)),"/*");
 
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { contextIndex, contextDebug });
        
        // Launch the shutdown method when the server is stopped
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		        try {
					shutdown();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		});
		
		_server.setHandler(contexts);
	    _server.start();
		_server.join();
	}
	
	/**
	 * Gracefully shutdown the server by closing the index finishing the writes etc.
	 * @throws Exception
	 */
	protected static void shutdown() throws Exception {
		LOGGER.info("Shuting down the server");
		_server.stop();
		_worker.stop();
		LOGGER.info("Server off");
	}
}