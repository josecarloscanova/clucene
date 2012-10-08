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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lahab.clucene.core.cache.CacheElement;

public class CacheElementTest {

	@Test
	public void testCacheElement() {
		Integer key = Integer.valueOf(3);
		Integer value = Integer.valueOf(4);
		CacheElement ce = new CacheElement(key, value);
		assertSame("Key not properly setted", key, ce.getKey());
		assertSame("value not properly setted", value, ce.getValue());
	}

	@Test
	public void testSetValue() {
		Integer key = Integer.valueOf(3);
		Integer value = Integer.valueOf(4);
		CacheElement ce = new CacheElement(key, value);
		Character newValue = Character.valueOf('c');
		ce.setValue(newValue);
		assertSame("value not properly setted", newValue, ce.getValue());		
	}

	@Test
	public void testSetKey() {
		Float key = Float.valueOf(3);
		Object value = Integer.valueOf(4);
		CacheElement ce = new CacheElement(key, value);
		String newKey = "jfieorjfefer";
		ce.setKey(newKey);
		assertSame("key not properly setted", newKey, ce.getKey());	
	}

	@Test
	public void testSetIdx() {
		Integer key = Integer.valueOf(3);
		Integer value = Integer.valueOf(4);
		CacheElement ce = new CacheElement(key, value);
		ce.setIdx(1);
		assertEquals("Index not properly setted", 1, ce.getIdx());	
	}

	@Test
	public void testSetHitCount() {
		Integer key = Integer.valueOf(3);
		Integer value = Integer.valueOf(4);
		CacheElement ce = new CacheElement(key, value);
		ce.setHitCount(100);
		assertEquals("Hit count not properly setted", 100, ce.getHitCount());
	}

	@Test
	public void testEqualsObject() {
		// Remember that on a cache element key equality is enough
		CacheElement key = new CacheElement(Integer.valueOf(3), Integer.valueOf(2));
		Integer value = Integer.valueOf(4);
		CacheElement ce = new CacheElement(key, value);
		Object key2 = new CacheElement(Integer.valueOf(3), Integer.valueOf(4));
		Object value2 = Character.valueOf('c');
		CacheElement ce2 = new CacheElement(key2, value2);
		assertNotSame(key, key2);
		assertTrue("Key equality should be sufficient", ce.equals(ce2));
	}

}
