package com.zalora.aloha.compressor;

import com.zalora.aloha.memcached.MemcachedItem;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public interface Compressor {

    /**
     * Item is post-processed after it's fetched from the cache
     */
    void afterGet(MemcachedItem item);

    /**
     * Item is pre-processed before it's written to the cache
     */
    void beforePut(MemcachedItem item);

}
