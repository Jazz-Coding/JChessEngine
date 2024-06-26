package com.jazz.engine.utils.datastructures;

import java.util.HashMap;

/**
 * Common hash map set up when we are interested in the number of occurrences of an object.
 * Maps objects to their frequency.
 */
public class HistogramHashMap<K> extends HashMap<K,Integer> {
    public void increment(K key){
        put(key, getOrDefault(key, 0)+1);
    }
    public void decrement(K key){
        if(containsKey(key)) {
            int occurrences = get(key);
            if (occurrences == 1) {
                // If this had just one occurrence, remove it from the map entirely to save space.
                remove(key);
            } else {
                put(key, occurrences - 1);
            }
        }
    }

    public int occurrences(K key){
        return getOrDefault(key,0);
    }
}
