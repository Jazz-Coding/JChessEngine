package com.jazz.engine.utils.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A hashmap that automatically removes its oldest entry when its size is exceeded.
 * Basic replacement strategy for a transposition table.
 */
public class CappedHashMap<K,V> extends LinkedHashMap<K,V> {
    private int maxSize;

    public CappedHashMap(int maxSize) {
        this.maxSize = maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}