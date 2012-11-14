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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.lahab.clucene.utils.Configuration;
import org.lahab.clucene.utils.Parametizer;
import org.lahab.clucene.utils.ParametizerException;
import org.lahab.clucene.utils.Statable;

public class QueryManager extends Thread implements Statable {
	private static Map<String, Object> DEFAULTS = new HashMap<String, Object>();
	static {
		DEFAULTS.put("remoteAdress", null);
		DEFAULTS.put("nbQuery", -1);
		DEFAULTS.put("maxTime", 1000);
		DEFAULTS.put("nbThreads", 4);
		DEFAULTS.put("queueSize", 10);
	}
	
	protected ThreadPoolExecutor _pool;
	protected QueryParser _queryParser;
	protected Parametizer _params;
	
	protected int _nbQueryExecuted = 0;
	protected long _cumulatedTime = 0;
	protected long _recentTime = 0;
	protected int _recentNbQuery;
	

	public QueryManager(Configuration config, QueryParser queryParser) throws Exception {
		_params = new Parametizer(DEFAULTS, config);
		_queryParser = queryParser;
		QueryJob.MANAGER = this;
		QueryJob.setRemote(_params.getString("remoteAddress"));

		
		int nb = _params.getInt("nbThreads");
		_pool = new ThreadPoolExecutor(nb, nb, 0, TimeUnit.SECONDS,
				   new ArrayBlockingQueue<Runnable>(_params.getInt("queueSize"), false),
				   new ThreadPoolExecutor.CallerRunsPolicy());
	}

	@Override
	public String[] header() {
		String[] stats = {"nbQuery", "cumulatedTime", "avgTime", "avgTimeLastInterval"};
		return stats;
	}
	
	public synchronized void addStat(long time) {
		_recentTime += time;
		_recentNbQuery++;
	}

	@Override
	public synchronized String[] record() {
		_cumulatedTime += _recentTime;
		_nbQueryExecuted += _recentNbQuery;
		double avgTime = (_nbQueryExecuted != 0) ? _cumulatedTime / _nbQueryExecuted : 0;
		double avgLastIntervalTime = (_recentNbQuery != 0) ? _recentTime / (_recentNbQuery): 0;
		String[] stats = {String.valueOf(_nbQueryExecuted), String.valueOf(_cumulatedTime), 
						  String.valueOf(avgTime), String.valueOf(avgLastIntervalTime)};
		_recentNbQuery = 0;
		_recentTime = 0;
		return stats;
	}

	public void start() {
		long endTime;
		try {
			endTime = System.currentTimeMillis() + _params.getInt("maxTime") * 1000;
			while (endTime > System.currentTimeMillis()) {
				String query = _queryParser.getQuery();
				_pool.execute(new QueryJob(query));
			}
			_pool.shutdown();
		} catch (ParametizerException e) {
			System.err.println(e.getMessage());
		}
	}

}
