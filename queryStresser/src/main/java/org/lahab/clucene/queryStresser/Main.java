package org.lahab.clucene.queryStresser;

/*
 * #%L
 * queryStresser
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
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.lahab.clucene.utils.Configuration;
import org.lahab.clucene.utils.JSONConfiguration;
import org.lahab.clucene.utils.Parametizer;

/**
 * Hello world!
 *
 */
public class Main 
{
	
	public static String CONFIG_FILE = "config.json";
	
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("filename", "queries");
		DEFAULTS.put("nbOut", 1000000);
	}
	
	protected static Parametizer _params;

	private static QueryParser _queryParser;
	
    public static void main( String[] args ) throws Exception
    {
    	if (args.length != 0 ) {
			CONFIG_FILE = args[0];
		}
    	
		Configuration config = new JSONConfiguration(CONFIG_FILE);
		_params = new Parametizer(DEFAULTS, config);
		
		_queryParser = new QueryParser(config);
		//_queryManager = new QueryManager(config, _queryParser);
		int nbGroup = 1;
		
	    File[] files = new File[nbGroup];
        FileWriter[] writers = new FileWriter[nbGroup];
	    for (int i = 0; i < files.length; i++) {
	    	files[i] = new File(_params.getString("filename") + "_" + i + ".csv");
	    	files[i].createNewFile();
	    	writers[i] = new FileWriter(files[i]);
	    }
	    try {
			for (int i = _params.getInt("nbOut"); i > 0; i--) {
				for (int j = 0; j < files.length; j++) {
					writers[j].write(_queryParser.getQuery() + ";\n");
				}
			}
			for (int j = 0; j < files.length; j++) {
				writers[j].flush();
				writers[j].close();
			}
	    } finally {
	    	
	    }
		/*Statable[] statables = {_queryManager};
		_stats = new StatRecorder(config.get("stats"), statables);
		
		// Launch the stats and then tell the query manager to just run
		_stats.start();
		_queryManager.start();
		_stats.stop();*/
    }
}
