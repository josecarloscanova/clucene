package org.lahab.clucene.server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.lahab.clucene.server.indexer.IndexerNode;
import org.lahab.clucene.server.searcher.SearcherNode;
import org.lahab.clucene.server.utils.CloudStorage;
import org.lahab.clucene.server.utils.Configuration;
import org.lahab.clucene.server.utils.Parametizer;


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
	
	public static String CONFIG_FILE = "config.json";
	public static Configuration _config;
	public static Worker _worker;
	public static Server _server;
	public static CloudStorage _cloudStorage;
	
	public static Parametizer _params;

	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("port", 7050);
	}
	
	public static void main(String[] args) throws Exception {
		// Retrieve storage account from connection-string
		if (_config == null) {
			if (args.length != 0 ) {
				CONFIG_FILE = args[0];
			}
			_config = new Configuration(CONFIG_FILE);
		}

		HttpServlet servlet = null;
		String path = null;

		_params = new Parametizer(DEFAULTS, _config);		
		_cloudStorage = new CloudStorage(_config.get("azure"));
		// Creates an indexer or a searcher depending on the configuration
		boolean isIndexer = false;
		if (_config.containsKey("indexer") ^ _config.containsKey("searcher")) {
			isIndexer = _config.containsKey("indexer");
		} else {
			throw new Exception("Your configuration file must contain either the configuration " +
								"for an indexer or for a searcher " +
								"it can't contain both");
		}
		if (isIndexer) {
			LOGGER.info("starting indexer node");
			IndexerNode indexer = new IndexerNode(_cloudStorage, _config.get("indexer"));
			servlet = new IndexServlet(indexer);
			path = IndexServlet.PATH;
			_worker = indexer;
		} else {
			LOGGER.info("starting searcher node");
			SearcherNode searcher = new SearcherNode(_cloudStorage, _config.get("searcher"));
			servlet = new SearchServlet(searcher);
			path = SearchServlet.PATH;
			_worker = searcher;
		}
		
	    _server = new Server(_params.getInt("port"));
        _server.setGracefulShutdown(10000);
        _server.setStopAtShutdown(true);
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
		_worker.stop();
		LOGGER.info("Server off");
	}
}