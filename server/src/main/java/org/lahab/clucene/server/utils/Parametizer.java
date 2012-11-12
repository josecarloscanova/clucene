package org.lahab.clucene.server.utils;

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

import java.util.HashMap;
import java.util.Map;

/**
 * This uses a Configuration object to deal with the default/configurable points of a system
 * This is meant to be slow at initialization but quick in use
 * Thus making it ideal for parametizing deamons
 * @author charlymolter
 *
 */
public class Parametizer {
	public Map<String, Object> _parameters = new HashMap<String, Object>();
	
	public Parametizer(Map<String, Object> def, Configuration config) throws Exception {
		assert config != null;
		// Overrides if the parameter is redefined
		for (Map.Entry<String, Object> entry : def.entrySet()) {
			Object elt = entry.getValue();
			if (config.containsKey(entry.getKey())) {
				if (config.isCompound(entry.getKey())) {
					elt = true;
				} else {
					elt = config.getObj(entry.getKey());
				}
			} else if (entry.getValue() == null) {
				throw new ParametizerException("This value for key:" + entry.getKey() + " is compulsory");
			}
			_parameters.put(entry.getKey(), elt);
		}
	}
	
	public Parametizer(Map<String, Object> def) throws ParametizerException {
		// Overrides if the parameter is redefined
		for (Map.Entry<String, Object> entry : def.entrySet()) {
			Object elt = entry.getValue();
			if (entry.getValue() == null) {
				throw new ParametizerException("This value is compulsory");
			}
			_parameters.put(entry.getKey(), elt);
		}
	}

	/**
	 * Returns the int value of the key
	 * @param key
	 * @return
	 * @throws ParametizerException if the value is not set or is not an int
	 */
	public int getInt(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof Integer && val != null) {
			return (Integer) val;
		}
		throw new ParametizerException("Invalid demand");
	}

	/**
	 * Returns the double value of the key
	 * @param key
	 * @return
	 * @throws ParametizerException if the value is not set or is not a double
	 */
	public double getDouble(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof Double && val != null) {
			return (Double) val;
		}
		throw new ParametizerException("Invalid demand");
	}
	
	/**
	 * Returns the string value of the key
	 * @param key
	 * @return
	 * @throws ParametizerException if the value is not set or is not an string
	 */
	public String getString(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof String && val != null) {
			return (String) val;
		}
		throw new ParametizerException("Invalid demand");
	}
	
	/**
	 * Returns the boolean value of the key
	 * @param key
	 * @return
	 * @throws ParametizerException if the value is not set or is not an boolean
	 */
	public boolean getBoolean(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof Boolean && val != null) {
			return (Boolean) val;
		}
		throw new ParametizerException("Invalid demand");
	}
}
