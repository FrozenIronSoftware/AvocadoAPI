package com.frozenironsoftware.avocado.cacher.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Create a
 */
public class ApiCount {
    private final int maxPerMinute;
    private final List<Long> count;

    /**
     * Create a new API count per minute
     * @param maxPerMinute max requests per minute
     */
    public ApiCount(int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
        count = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Decrement the current count by one
     */
    public void use() {
        count.add(System.currentTimeMillis());
    }

    /**
     * Wait until the count is greater than 0
     * @param timeoutSeconds seconds to wait before timeout
     */
    public void waitForFree(int timeoutSeconds) throws TimeoutException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutSeconds * 1000) {
            if (getCount() > 0)
                return;
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                throw new TimeoutException();
            }
        }
        throw new TimeoutException();
    }

    /**
     * Get the current count remaining
     * @return count
     */
    private int getCount() {
        synchronized (count) {
            List<Long> updatedCount = new ArrayList<>();
            for (Long countTime : count)
                if (System.currentTimeMillis() - countTime < 60000)
                    updatedCount.add(countTime);
            count.clear();
            count.addAll(updatedCount);
            return Math.max(maxPerMinute - count.size(), 0);
        }
    }

    /**
     * Set the current count to zero
     */
    public void useAll() {
        long time = System.currentTimeMillis();
        synchronized (count) {
            count.clear();
            for (int countIndex = 0; countIndex < maxPerMinute; countIndex++)
                count.add(time);
        }
    }
}
