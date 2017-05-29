package com.zalora.aloha.compressor;

import com.zalora.aloha.memcached.MemcachedItem;

/**
 * Just sit back and relax
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public class NoCompressor implements Compressor {

    @Override
    public void afterGet(MemcachedItem item) {

    }

    @Override
    public void beforePut(MemcachedItem item) {

    }

}
