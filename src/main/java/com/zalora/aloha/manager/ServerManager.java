package com.zalora.aloha.manager;

import javax.annotation.PostConstruct;
import com.zalora.aloha.compressor.Compressor;
import com.zalora.aloha.config.ServerConfig;
import com.zalora.aloha.listener.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.manager.*;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryLoaded;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
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

        // Compress entry before putting it in memory
        if (serverConfig.isReadthroughEnabled() && !serverConfig.getCompressorClass().isEmpty()) {
            initCompressor(serverConfig.getCompressorClass());
        }
    }

    @Bean
    EmbeddedCacheManager embeddedCacheManager() {
        return embeddedCacheManager;
    }

    @PostConstruct
    public void init() {
        // Start HotRod Server
        hotRodServer.start(hotRodServerConfiguration, embeddedCacheManager);
    }

    private void initCompressor(String compressorClass) {
        final Compressor compressor;

        try {
            compressor = (Compressor) Class.forName(compressorClass).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            log.error("Could not instantiate {}, disabling compression", compressorClass);
            return;
        }

        Set<Class<? extends Annotation>> events = new HashSet<>(1);
        events.add(CacheEntryLoaded.class);

        embeddedCacheManager.getCache("main").addFilteredListener(
            new StorageFetchListener(), null, new DataCompressionEventConverter(compressor), events
        );

        log.info("Read-through compressor {} added to main cache", compressor.getClass().getSimpleName());
    }

}
