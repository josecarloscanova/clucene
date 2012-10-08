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


import java.util.Random;

public class RandomCache extends Cache {
	
	public RandomCache(int maxSize) {
		super(maxSize);
	}

	@Override
	protected int getAvailableIndex() {
		int index = maxSize + 1; // to be sure we will always crash
		
		if (isFull()) {
			index = (int) (cache.length * new Random().nextFloat());
			reference.remove(cache[index].getKey());
			nbEntries--;
		} else {
			index = nbEntries;
		}
		return index;
	}

}
