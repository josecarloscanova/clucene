package org.lahab.clucene.server.utils;

/*
 * #%L
 * core
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;

/**
 * This class parses a configuration file and make every parameter available
 * extend this class if you want to add a format different to JSON
 * @author charlymolter
 *
 */
public class Configuration {
	protected JSONObject _config = null;
	protected CloudStorageAccount _storageAccount = null;
	protected JSONObject _azureConf = null;
	protected JSONObject _crawlerConf = null;
	protected JSONObject _loggingConf = null;
	protected JSONObject _nodeConf = null;
	protected boolean _isIndexer;
	
	/**
	 * Parses the file and extracts each category of configuration
	 * @param configFile
	 * @throws Exception 
	 */
	public Configuration(String configFile) throws Exception {
		_config = parseConfig(configFile);
		_azureConf = _config.getJSONObject("azure");
		if (_config.containsKey("logging")) {
			_loggingConf = _config.getJSONObject("logging");
		}
		if (_config.containsKey("indexer") ^ _config.containsKey("searcher")) {
			if (_config.containsKey("indexer")) {
				_isIndexer = true;
				_nodeConf = _config.getJSONObject("indexer");
				_crawlerConf = _nodeConf.getJSONObject("crawler");
			} else {
				_isIndexer = false;
				_nodeConf = _config.getJSONObject("searcher");
			}
		} else {
			throw new Exception("Your configuration file must contain either the configuration " +
								"for an indexer or for a searcher " +
								"it can't contain both");
		}
		
	}
	/** 
	 * Extracts the configuration from the json file specified by CONFIG_FILE 
	 * and put it all in the jsonObject config
	 * @throws IOException
	 */
	protected JSONObject parseConfig(String confFile) throws IOException {
        InputStream is = new FileInputStream(new File(confFile));
        String jsonTxt = IOUtils.toString(is);
        return (JSONObject) JSONSerializer.toJSON(jsonTxt);
	}
	
	/**
	 * Actually applies and load the configuration
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 */
	public void configure() throws InvalidKeyException, URISyntaxException {
		configureLogging();
	}
	
	/**
	 * Set all the necessary logging for each object;
	 */
	protected void configureLogging() {
		java.util.logging.Handler[] handlers =
	    		Logger.getLogger( "" ).getHandlers();
	    for ( int index = 0; index < handlers.length; index++ ) {
	    	handlers[index].setLevel( Level.FINEST );
	    }
	    // TODO find a nice way to change the logging from the config file
	}
	
	public String getContainer() {
		return _azureConf.getString("container");
	}
	
	public CloudStorageAccount getStorageAccount() throws InvalidKeyException, URISyntaxException {
		return CloudStorageAccount.parse("DefaultEndpointsProtocol="+ 
				   						 _azureConf.getString("defaultEndpointsProtocol") +
				   						 ";AccountName=" +
				   						 _azureConf.getString("accountName") +
				   						 ";AccountKey=" +
				   						 _azureConf.getString("accountKey") + ";");
	}
	
	public String getCrawlerFolder() {
		return _crawlerConf.getString("folder");
	}
	
	public String getSeed() {
		return _crawlerConf.getString("seed");
	}
	
	public int getNbCrawler() {
		return _crawlerConf.getInt("nbCrawler");
	}
	
	public int getNbIndexer() {
		return _nodeConf.getInt("nbThread");
	}
	
	public int getCommitFreq() {
		return _nodeConf.getInt("commitFreq");
	}
	
	public JSONObject getMainConfig() {
		return _config;
	}
	
	public int getPort() {
		return _config.getInt("port");
	}
	
	public String getDownloadDir() {
		return _nodeConf.getString("downloadFolder");
	}
	
	public boolean isIndexer() {
		return _isIndexer;
	}
	
	public String getCrawlerDomain() {
		return _crawlerConf.getString("domain");
	}

}
