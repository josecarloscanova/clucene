package org.core.cache;


import static org.junit.Assert.*;

import org.core.cache.DummyCache;
import org.junit.Test;

public class DummyCacheTest {

	@Test
	public void testDummyCache() {
		DummyCache c = new DummyCache();
		assertEquals("The maxSize should always be 0", 0, c.getMaxSize());
		assertTrue("The cache should be full at all time", c.isFull());
		Integer key = Integer.valueOf(1);
		Integer value = Integer.valueOf(2);
		c.add(key, value);
		assertTrue("The cache should be full at all time", c.isFull());
		assertNull("The dummy cache should always be empty", c.get(key));
	}

}
