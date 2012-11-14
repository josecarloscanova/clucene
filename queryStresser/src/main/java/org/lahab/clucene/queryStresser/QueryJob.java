package org.lahab.clucene.queryStresser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

public class QueryJob implements Runnable {

	public static QueryManager MANAGER;
	public static URL REMOTE;
	
	public static void setRemote(String uri) throws MalformedURLException {
		REMOTE = new URL(uri);
	}

	public QueryJob(String query) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		long time = System.currentTimeMillis();
		try {
			REMOTE.getContent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MANAGER.addStat(System.currentTimeMillis() - time);
	}

}
