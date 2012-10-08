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
