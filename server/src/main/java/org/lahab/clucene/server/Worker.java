package org.lahab.clucene.server;

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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lahab.clucene.server.utils.CloudStorage;
import org.lahab.clucene.server.utils.Configuration;
import org.lahab.clucene.server.utils.Parametizer;
import org.lahab.clucene.server.utils.StatRecorder;
import org.lahab.clucene.server.utils.Statable;

public abstract class Worker {

	public Parametizer _params;
	protected static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	protected StatRecorder _stats = null;
	static {
		DEFAULTS.put("stats", false);
	}
	
	public Worker(CloudStorage storage, Configuration config) throws Exception {
		_params = new Parametizer(DEFAULTS, config);
	}
	
	/** 
	 * Gracefully stops whatever our worker is doing
	 * @throws IOException 
	 */
	abstract public void stop() throws IOException;

	/**
	 * Starts the workers' work
	 * @throws IOException
	 */
	abstract public void start() throws IOException;
	
	protected void initStats(Configuration config, Statable[] statables) throws Exception {
		if (_params.getBoolean("stats")) {
			_stats = new StatRecorder(config, statables);
		}
	}

}
