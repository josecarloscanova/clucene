package org.lahab.clucene.server;


import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.lahab.clucene.indexer.IndexerNode;

import com.microsoft.windowsazure.services.core.storage.*;
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

public class IndexerMain {
	private static final Logger LOGGER = Logger.getLogger(IndexerMain.class.getName());
	
	public static final String CONFIG_FILE = "../config.json";
	public static JSONObject config;
	public static IndexerNode _indexer;
	public static Server _server;
	private static CloudStorageAccount _storageAccount = null;
	
	public static void main(String[] args) throws Exception {
		// Retrieve storage account from connection-string
		if (config == null) {
			parseConfig();
			configure();
		}
		
		_indexer = new IndexerNode(_storageAccount, config.getString("container"), 
								   "http://en.wikipedia.org", 2, 
								   config.getString("crawlerFolder"));
	    _server = new Server(7050);
        
	    // Creates the servlet for indexing purpose
        ServletContextHandler contextIndex = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextIndex.setContextPath(IndexServlet.PATH);
        contextIndex.addServlet(new ServletHolder(new IndexServlet(_indexer)),"/*");
        
        // Creates the servlet for debug purpose
        ServletContextHandler contextDebug = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextDebug.setContextPath(DebugServlet.PATH);
        contextDebug.addServlet(new ServletHolder(new DebugServlet(_indexer)),"/*");
 
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
	 * Extracts the configuration from the json file specified by CONFIG_FILE 
	 * and put it all in the jsonObject config
	 * @throws IOException
	 */
	protected static void parseConfig() throws IOException {
        InputStream is = new FileInputStream(new File(CONFIG_FILE));
        String jsonTxt = IOUtils.toString(is);
        config = (JSONObject) JSONSerializer.toJSON(jsonTxt);
	}
	
	/**
	 * Gracefully shutdown the server by closing the index finishing the writes etc.
	 * @throws Exception
	 */
	protected static void shutdown() throws Exception {
		LOGGER.info("Shuting down the server");
		_server.stop();
		_indexer.stop();
		LOGGER.info("Server off");
	}
	
	/**
	 * Use the previously parsed JSON to configure the server
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 */
	protected static void configure() throws InvalidKeyException, URISyntaxException {
		JSONObject azureConf = config.getJSONObject("azure");
		_storageAccount = CloudStorageAccount.parse("DefaultEndpointsProtocol="+ 
												   azureConf.getString("defaultEndpointsProtocol") +
												   ";AccountName=" +
												   azureConf.getString("accountName") +
												   ";AccountKey=" +
												   azureConf.getString("accountKey") + ";");
		IndexerNode.DOWNLOAD_DIR = config.getString("downloadFolder");
		
		configureLogging();
	}

	/**
	 * Set all the necessary logging for each object;
	 */
	protected static void configureLogging() {
		java.util.logging.Handler[] handlers =
	    		Logger.getLogger( "" ).getHandlers();
	    for ( int index = 0; index < handlers.length; index++ ) {
	    	handlers[index].setLevel( Level.FINEST );
	    }
		//WikipediaParser.LOGGER.setLevel(Level.FINEST);
	}
}