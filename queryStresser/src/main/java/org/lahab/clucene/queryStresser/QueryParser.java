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
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.lahab.clucene.utils.Configuration;
import org.lahab.clucene.utils.Parametizer;
import org.lahab.clucene.utils.ParametizerException;

public class QueryParser {
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("queryFile", "queries");
		DEFAULTS.put("nbQueries", 4);
		DEFAULTS.put("uniform", false);
		DEFAULTS.put("gaussian.mean", null);
		DEFAULTS.put("gaussian.stddev", null);		
	}
	protected String[] _queries;
	protected Parametizer _params;
	protected double _mean;
	protected double _stddev;
	protected boolean _uniform = false;
	private Random _generator;
	
	
	public QueryParser(Configuration config) throws Exception {
		_params = new Parametizer(DEFAULTS, config);
		File queryFile = new File(_params.getString("queryFile"));
		if (!queryFile.exists() || !queryFile.canRead()) {
			throw new Exception("Can't read file:" + _params.getString("queryFile"));
		}
		initQueryArray(queryFile);
		
		if (_params.getBoolean("uniform")) {
			_uniform  = true;
		} else {
			_mean = _params.getDouble("gaussian.mean");
			_stddev = _params.getDouble("gaussian.stddev");
		}
		_generator = new Random();
	}

	private void initQueryArray(File file) throws ParametizerException, FileNotFoundException {
		int max = _params.getInt("nbQueries");
		_queries = new String[max];
		Scanner scanner = new Scanner(file);
		scanner.useDelimiter("\n");
		for (int i = 0; scanner.hasNext() && i < max; i++) {
			_queries[i] = scanner.next();
		}
	}

	public String getQuery() {
		int idx;
		if (_uniform) { 
			idx = _generator.nextInt(_queries.length - 1);
		} else {
			do {
				idx = (int) (_generator.nextGaussian() * _stddev + _mean);
			} while (idx < 0 || idx >= _queries.length);
		}
		return _queries[idx];
	}
	
}
