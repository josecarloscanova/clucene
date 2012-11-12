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
 * This uses a JSON object to deal with the default/configurable points of a system
 * This is meant to be slow at initialization a quick in use
 * Thus making it ideal for parametizing deamons
 * @author charlymolter
 *
 */
public class Parametizer {
	public Map<String, Object> _parameters = new HashMap<String, Object>();
	
	public Parametizer(Map<String, Object> def, Configuration config) throws Exception {
		// Initialize our parameter
		if (config == null) {
			_parameters.putAll(def);
			return;
		}
		for (Map.Entry<String, Object> entry : def.entrySet()) {
			Object elt = entry.getValue();
			if (config.containsKey(entry.getKey())) {
				if (config.isCompound(entry.getKey())) {
					elt = true;
				} else {
					elt = config.getObj(entry.getKey());
				}
			}
			_parameters.put(entry.getKey(), elt);
		}
	}
	
	public int getInt(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof Integer && val != null) {
			return (Integer) val;
		}
		throw new ParametizerException();
	}

	public double getDouble(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof Double && val != null) {
			return (Double) val;
		}
		throw new ParametizerException();
	}
	
	public String getString(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof String && val != null) {
			return (String) val;
		}
		throw new ParametizerException();
	}
	
	public boolean getBoolean(String key) throws ParametizerException {
		Object val = _parameters.get(key);
		if (val instanceof Boolean && val != null) {
			return (Boolean) val;
		}
		throw new ParametizerException();
	}
}
