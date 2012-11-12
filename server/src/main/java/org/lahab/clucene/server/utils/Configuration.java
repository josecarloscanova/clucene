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

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


/**
 * This class parses a configuration file and make every parameter available
 * extend this class if you want to add a format different to JSON
 * @author charlymolter
 *
 */
public class Configuration {
	protected JSONObject _config = null;
	
	/**
	 * Parses the file and extracts each category of configuration
	 * @param configFile
	 * @throws Exception 
	 */
	public Configuration(String configFile) throws Exception {
		_config = parseConfig(configFile);
	}
	
	public Configuration(JSONObject config) {
		_config = config;
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
	
	public boolean containsKey(String key) {
		String[] keys = key.split("\\.");
		if (keys.length == 0) {
			return _config.containsKey(key);
		}
		JSONObject elt = _config; 
		for (int i = 0; i < keys.length - 1; i++) {
			if (elt.containsKey(keys[i]) && elt.get(keys[i]) instanceof JSONObject) {
				elt = elt.getJSONObject(keys[i]);
			} else {
				return false;
			}

		}
		return elt.containsKey(keys[keys.length - 1]);
	}
	
	public Configuration get(String key) throws Exception {
		if (!isCompound(key)) {
			throw new Exception("Key: \"" + key + "\" is not a compound");
		}
		return new Configuration((JSONObject)navigateDown(key));
	}
	
	public Object getObj(String key) throws Exception {
		return navigateDown(key);
	}
	
	public boolean isCompound(String key) throws Exception {
		return navigateDown(key) instanceof JSONObject;
	}

	protected Object navigateDown(String key) throws Exception {
		String[] keys = key.split("\\.");
		if (keys.length == 0) {
			return _config.get(key);
		}
		JSONObject elt = _config; 
		for (int i = 0; i < keys.length - 1; i++) {
			if (elt.containsKey(keys[i]) && elt.get(keys[i]) instanceof JSONObject) {
				elt = elt.getJSONObject(keys[i]);
			} else {
				throw new Exception("Key: \"" + keys[i] + "\" is not a compound can't go more down");
			}	
		}
		if (elt.containsKey(keys[keys.length - 1])) {
			return elt.get(keys[keys.length - 1]);
		} else {
			throw new Exception("Key: \"" + keys[keys.length - 1] + "\" doesn't exist");
		}
	}
}
