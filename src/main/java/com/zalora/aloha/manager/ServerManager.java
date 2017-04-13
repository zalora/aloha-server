package com.zalora.aloha.manager;

import com.zalora.aloha.config.ServerConfig;
import javax.annotation.PostConstruct;

import com.zalora.aloha.memcached.MemcachedItem;
import lombok.Getter;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class ServerManager {

    @Getter
    private EmbeddedCacheManager embeddedCacheManager;

    @Getter
    private final HotRodServer hotRodServer = new HotRodServer();

    private ServerConfig serverConfig;

    @Autowired
    public ServerManager(ServerConfig cacheConfig) {
        Assert.notNull(cacheConfig, "Configuration could not be loaded");

        this.serverConfig = cacheConfig;
    }

    @PostConstruct
    public void init() {
        embeddedCacheManager = new DefaultCacheManager(serverConfig.getGlobalConfiguration());

        // Configure primary cache
        embeddedCacheManager.defineConfiguration(
            serverConfig.getPrimaryCacheName(),
            serverConfig.getPrimaryCacheConfiguration()
        );

        // Configure secondary cache
        embeddedCacheManager.defineConfiguration(
            serverConfig.getSecondaryCacheName(),
            serverConfig.getSecondaryCacheConfiguration()
        );

        // Start HotRod Server
        HotRodServer server = new HotRodServer();
        server.start(serverConfig.getHotRodServerConfiguration(), embeddedCacheManager);
    }

    public Cache<String, MemcachedItem> getPrimaryCache() {
        return embeddedCacheManager.getCache(serverConfig.getPrimaryCacheName());
    }

    public Cache<String, MemcachedItem> getSecondaryCache() {
        return embeddedCacheManager.getCache(serverConfig.getSecondaryCacheName());
    }

}
