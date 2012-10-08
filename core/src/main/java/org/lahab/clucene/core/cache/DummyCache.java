package org.lahab.clucene.core.cache;

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
 * Cache the is always full and therefore useless (except for tests)
 * @author charlymolter
 *
 */
public class DummyCache extends Cache {

	public DummyCache() {
		super(0);
	}

	@Override
	protected int getAvailableIndex() {
		return 0;
	}

	@Override
	public synchronized void add(Object key, Object value) {
	}
	
	@Override
	public synchronized Object get(Object key) {
		return null;
	}
}
