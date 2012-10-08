package org.core.cache;

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
