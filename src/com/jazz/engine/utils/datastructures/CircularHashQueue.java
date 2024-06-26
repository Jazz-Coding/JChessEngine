package com.jazz.engine.utils.datastructures;

import java.util.LinkedList;

/**
 * Implementation of a FIFO queue that self-regulates its size.
 */
public class CircularHashQueue<E> extends LinkedList<E> {
    private final int capacity;

    public CircularHashQueue(int capacity){
        this.capacity = capacity;
    }

    @Override
    public boolean add(E e) {
        if(size() >= capacity) removeFirst();
        return super.add(e);
    }

    public void addSmart(E element) {
        if(!contains(element)) {
            super.add(element);
        }
    }
}
