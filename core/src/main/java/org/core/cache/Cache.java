package org.core.cache;

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

import java.util.HashMap;

public abstract class Cache {
	protected CacheElement[] cache;
	protected HashMap<Object, Object> reference;
	protected int maxSize;
	protected int nbEntries = 0;
	
	public Cache(int maxSize) {
		this.maxSize = maxSize;
		cache = new CacheElement[maxSize];
		reference = new HashMap<Object, Object>(maxSize);
	}
	
	/**
	 * Adds an element to the cache if it already exists replace it
	 * @param key the key the element is referred as
	 * @param value the value the element contains
	 */
	public synchronized void add(Object key, Object value) {
		CacheElement obj = getElement(key);
		if (obj != null) {
			obj.setKey(key);
			obj.setValue(value);
			return;
		} else {
			obj = new CacheElement(key, value);
		}
		int idx = getAvailableIndex();	
		putElement(idx, obj);
	}
	
	/**
	 * Returns the data contained in the cache for element Value 
	 * If value is not in the cache it will return null
	 * @param key
	 * @return
	 */
	public synchronized Object get(Object key) {
		CacheElement elt = getElement(key);
		return elt == null ? null : elt.getValue();
	}
	
	/**
	 * Tells whether the cache is full or not
	 * @return
	 */
	protected boolean isFull() {
		return nbEntries == maxSize;
	}
	
	/**
	 * Look in the data structure containing the cache for the element referenced by key
	 * Returns null if the key doesn't exist in the cache
	 * @param key 
	 * @return
	 */
	protected CacheElement getElement(Object key) {
		if (key == null) {
			return null;
		}
		Object o = reference.get(key);
		return o != null ? (CacheElement)o : null;
	}

	/**
	 * Add an element to the data structure containing the cache
	 * The element shouldn't not exist already
	 * @param idx the index available
	 * @param key
	 * @param value
	 */
	protected void putElement(int idx, CacheElement obj) {
		cache[idx] = obj;
		reference.put(obj.getKey(), obj);
		nbEntries++;
	}
	
	/**
	 * Return an index available in our cache 
	 */
	abstract protected int getAvailableIndex();
	
	/**
	 * Empty the cache completely
	 */
	public void clean() {
		reference.clear();
		for (int i = 0; i < cache.length; i++) {
			cache[i] = null;
		}
		nbEntries = 0;
	}
	
	/**
	 * Return the current size of the cache
	 * @return number of elements in the cache
	 */
	protected long size() {
		return nbEntries;
	}
	
	CacheElement[] getCache() {
		return cache;
	}
	
	HashMap<Object, Object> getReference() {
		return reference;
	}
	
	int getMaxSize() {
		return maxSize;
	}
}
