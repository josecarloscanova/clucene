package org.core.cache;


public class CacheElement {
	private Object value;
	private Object key;
	
	private int idx;
	
	private int hitCount;
	
	public CacheElement (Object offset, Object value) {
		assert offset != null && value != null: "trying to create an CacheElement with null content";
		this.key = offset;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CacheElement)) {
			return false;
		}
		CacheElement that = (CacheElement)obj;
		return (that.value != null && key.equals(that.key)) || key == null;
		
	}
	
	public String toString() {
		return key + ":" + value;
	}
}
