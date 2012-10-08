package org.core.cache;


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
