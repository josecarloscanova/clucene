package org.lahab.clucene.server.indexer;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

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

public class CommitThread implements Runnable{

	protected Thread _thread;
	protected int _freq;
	private volatile boolean _isCommiting = false;
	
	public CommitThread(int freq) {
		_freq = freq;
		_thread = new Thread(this);
		_thread.setName("Commit thread");
	}
	
	@Override
	public void run() {
		Thread curThread = Thread.currentThread();
		while (curThread == _thread) {
			try {
				_isCommiting = true;
				PoolJobs.INDEX.commit();
				_isCommiting = false;
				Thread.sleep(_freq);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void start() {
		_thread.start();
	}
	
	public void stop() {
		_thread = null;
	}

	public boolean isCommiting() {
		return _isCommiting;
	}
}
