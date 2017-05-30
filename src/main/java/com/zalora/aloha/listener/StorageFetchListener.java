package com.zalora.aloha.listener;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.*;
import org.infinispan.notifications.cachelistener.event.*;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Listener(observation = Listener.Observation.POST)
public class StorageFetchListener {

    @CacheEntryLoaded
    @SuppressWarnings("unchecked")
    public void attachMetadata(CacheEntryLoadedEvent event) {
        // useless, but happy :-)
    }

}
