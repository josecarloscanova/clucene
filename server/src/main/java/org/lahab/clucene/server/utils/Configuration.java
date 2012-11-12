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


/**
 * This class parses a configuration file and make every parameter available
 * @author charlymolter
 *
 */
abstract public class Configuration {

	public Configuration() {
	}

	/**
	 * tells whether or not this key exists
	 * @param key
	 * @return
	 */
	abstract public boolean containsKey(String key);
	
	/**
	 * Returns a Configuration for the path of this object
	 * @param key
	 * @return
	 * @throws Exception if key doesn't exist
	 */
	abstract public Configuration get(String key) throws Exception;
	
	/**
	 * Returns the object corresponding to the tree
	 * @param key
	 * @return
	 * @throws Exception if key doesn't exist
	 */
	abstract public Object getObj(String key) throws Exception;
	
	/**
	 * Returns whether or not this key contains one or more configuration string
	 * (in the case of a configuration tree like JSON objects)
	 * @param key
	 * @return
	 * @throws Exception if key doesn't exist
	 */
	abstract public boolean isCompound(String key) throws Exception;
	
	/** 
	 * Extracts the configuration from a text and do what is necessary to save the configuration
	 * @throws IOException
	 */
	abstract protected void parseConfig(String configTxt) throws Exception;
}
