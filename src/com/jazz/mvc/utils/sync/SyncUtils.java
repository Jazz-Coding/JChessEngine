package com.jazz.mvc.utils.sync;

public class SyncUtils {
    public static void waitForLock(Object lock) {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
