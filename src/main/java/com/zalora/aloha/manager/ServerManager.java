package com.zalora.aloha.manager;

import javax.annotation.PostConstruct;

import com.zalora.aloha.config.ServerConfig;
import lombok.Getter;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.manager.*;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class ServerManager {

    @Autowired
    private HotRodServerConfiguration hotRodServerConfiguration;

    @Getter
    private HotRodServer hotRodServer = new HotRodServer();

    private EmbeddedCacheManager embeddedCacheManager;

    @Autowired
    public ServerManager(GlobalConfiguration globalConfig, ServerConfig serverConfig, Configuration mainConfig, Configuration sessionConfig) {
        Assert.notNull(globalConfig, "Global Configuration must not be null");
        Assert.notNull(serverConfig, "Server Configuration must not be null");
        Assert.notNull(mainConfig, "Main Cache Configuration must not be null");
        Assert.notNull(sessionConfig, "Secondary Cache Configuration must not be null");

        embeddedCacheManager = new DefaultCacheManager(globalConfig);
        embeddedCacheManager.defineConfiguration(serverConfig.getPrimaryCacheName(), mainConfig);
        embeddedCacheManager.defineConfiguration(serverConfig.getSecondaryCacheName(), sessionConfig);
    }

    @PostConstruct
    public void init() {
        // Start HotRod Server
        hotRodServer.start(hotRodServerConfiguration, embeddedCacheManager);
    }

}
