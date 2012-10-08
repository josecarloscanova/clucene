package org.core.cache;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.core.cache.CacheElement;
import org.junit.Test;

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
