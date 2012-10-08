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


import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.lahab.clucene.core.cache.CacheElement;
import org.lahab.clucene.core.cache.RandomCache;

@RunWith(Theories.class)
public class RandomCacheTest {

	@Theory
	public void testCache(int candidate) {
		RandomCache c = new RandomCache(candidate);
		assertEquals("Size not 0", 0, c.size());
		assertEquals("Maxsize wrong", candidate, c.getMaxSize());
		assertNotNull("Assertion init reference collection failed", c.getReference());
		assertEquals("Cache size incorrect", 0, c.getReference().size());
		assertNotNull("Assertion init cache collection", c.getCache());
		assertEquals("Cache size incorrect", candidate, c.getCache().length);
		assertNull("Unknown element should return null", c.get(Integer.valueOf(2)));
		CacheElement[] cache = c.getCache();
		for (int j = 0; j < c.getCache().length; j++) {
			assertNull("All the elements of the cache should be null", cache[j]);
		}
	}

	@Theory
	public void testAdd(int candidate) {
		RandomCache c = new RandomCache(candidate);
		CacheElement[] cache = c.getCache();
		HashMap<Object, Object> ref = c.getReference();
		// We fill up fully the cache
		for (int i = 0; i < candidate; i++) {
			assertFalse("the cache shouldn't be full", c.isFull());
			assertNull("Unknown element should return null", c.get(Float.valueOf(2)));
			Integer key = Integer.valueOf(i);
			Integer value = Integer.valueOf(i);
			c.add(key, value);
			assertEquals("nbEntries not updated properly", i + 1, c.size());
			assertNotNull("cache not loaded well", cache[i]);
			assertNotNull("cache reference not loaded well", ref.get(key));
			assertTrue("the element hasn't been added where it should", 
						cache[i].getKey() == key &&
						cache[i].getValue() == value);
			assertSame("Object in cache and reference differ", 
					cache[i], ref.get(cache[i].getKey()));
			assertSame("Object should be similar when using get", value, c.get(key));
		}
		assertTrue("the cache should be full", c.isFull());
		// Now test that everything goes well on cache default
		for (int i = 0; i < candidate; i++) {
			assertTrue("the cache should be full", c.isFull());
			assertNull("Unknown element should return null", c.get(Float.valueOf(2)));
			Integer key = Integer.valueOf(i);
			Integer value = Integer.valueOf(i);
			c.add(key, value);
			assertEquals("nbEntries not updated properly", c.getMaxSize(), c.size());
			assertNotNull("cache reference not loaded well", ref.get(key));
			assertTrue("the element hasn't been added where it should", 
						((CacheElement) ref.get(key)).getKey() == key &&
						((CacheElement) ref.get(key)).getValue() == value);
			boolean found = false;
			for (int j = 0; j < c.getMaxSize(); j++) {
				assertNotNull("The cache should be full", cache[j]);
				if (cache[j] == ref.get(key)) {
					found = true;
					j = c.getMaxSize() + 2;
				}
			}
			assertSame("Object should be similar when using get", value, c.get(key));			
			assertTrue("The object should have been found in the cache", found);
		}		
	}
	
	@Theory
	public void testClean(int candidate) {
		RandomCache c = new RandomCache(candidate);
		for (int i = 0; i < candidate; i++) {
			Integer key = Integer.valueOf(i);
			Integer value = Integer.valueOf(i);
			c.add(key, value);
		}
		c.clean();
		
		assertEquals("Size not 0", 0, c.size());
		assertEquals("Maxsize wrong", candidate, c.getMaxSize());
		assertNotNull("Assertion init reference collection failed", c.getReference());
		assertEquals("Cache size incorrect", 0, c.getReference().size());
		assertNotNull("Assertion init cache collection", c.getCache());
		assertEquals("Cache size incorrect", candidate, c.getCache().length);
		CacheElement[] cache = c.getCache();
		for (int j = 0; j < c.getCache().length; j++) {
			assertNull("All the elements of the cache should be null", cache[j]);
		}	
	}
	
	public static @DataPoints int[] candidates = {0, 1, 5, 1024};
}
