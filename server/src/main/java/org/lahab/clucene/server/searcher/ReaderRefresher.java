package org.lahab.clucene.server.searcher;

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

import org.apache.lucene.search.SearcherManager;

public class ReaderRefresher implements Runnable {

	protected Thread _thread;
	protected SearcherManager _manager = null;
	
	public ReaderRefresher(SearcherManager manager) {
		_manager = manager;
		_thread = new Thread(this);
		_thread.setName("Reader refresher thread");
	}
	
	@Override
	public void run() {
		Thread curThread = Thread.currentThread();
		while (curThread == _thread) {
			try {
				_manager.maybeRefresh();
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			_manager.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void start() {
		_thread.start();
	}
	
	public void stop() {
		_thread = null;
	}
}
