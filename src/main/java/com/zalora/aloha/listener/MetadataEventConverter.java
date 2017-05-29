package com.zalora.aloha.listener;

import com.zalora.aloha.compressor.Compressor;
import com.zalora.aloha.memcached.MemcachedItem;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.metadata.Metadata;
import org.infinispan.notifications.cachelistener.filter.*;
import org.springframework.util.Assert;
import java.io.Serializable;

/**
 * Compress data between database and cache
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
public class MetadataEventConverter implements CacheEventConverter<Object, Object, MemcachedItem>, Serializable {

    private Compressor compressor;

    public MetadataEventConverter(Compressor compressor) {
        Assert.notNull(compressor, "Compressor must not be null");
        this.compressor = compressor;
    }

    @Override
    public MemcachedItem convert(Object key, Object oldItem, Metadata oldMetadata, Object newItem, Metadata newMetadata, EventType eventType) {
        MemcachedItem memcachedItem = (MemcachedItem) newItem;
        compressor.beforePut(memcachedItem);
        return memcachedItem;
    }

}
