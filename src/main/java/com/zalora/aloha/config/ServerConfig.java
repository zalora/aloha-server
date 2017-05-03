package com.zalora.aloha.config;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.ShutdownHookBehavior;
import org.infinispan.persistence.jpa.configuration.JpaStoreConfigurationBuilder;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class ServerConfig {

    private static final String CACHE_MODE_DISTRIBUTED = "DISTRIBUTED";

    @Getter
    private GlobalConfiguration globalConfiguration;

    @Getter
    private Configuration primaryCacheConfiguration;

    @Getter
    private Configuration secondaryCacheConfiguration;

    @Getter
    private Configuration readthroughCacheConfiguration;

    @Getter
    private HotRodServerConfiguration hotRodServerConfiguration;

    // General cluster configuration
    @Value("${infinispan.cluster.name}")
    private String clusterName;

    @Value("${infinispan.cluster.network.address}")
    private String networkAddress;

    @Getter
    @Value("${infinispan.cluster.statistics.enabled}")
    private boolean globalStatisticsEnabled;

    @Value("${infinispan.cluster.jgroups.config}")
    private String jgroupsConfig;

    // Primary cache configuration
    @Getter
    @Value("${infinispan.cache.primary.name}")
    private String primaryCacheName;

    @Value("${infinispan.cache.primary.mode}")
    private CacheMode primaryCacheMode;

    @Value("${infinispan.cache.primary.numOwners}")
    private int primaryCacheNumOwners;

    @Value("${infinispan.cache.primary.lock.timeout}")
    private int primaryCacheLockTimeout;

    @Value("${infinispan.cache.primary.lock.concurrency}")
    private int primaryCacheLockConcurrency;

    @Value("${infinispan.cache.primary.stateTransferChunkSize}")
    private int primaryStateTransferChunkSize;

    // Secondary cache configuration
    @Getter
    @Value("${infinispan.cache.secondary.name}")
    private String secondaryCacheName;

    @Value("${infinispan.cache.secondary.mode}")
    private CacheMode secondaryCacheMode;

    @Value("${infinispan.cache.secondary.numOwners}")
    private int secondaryCacheNumOwners;

    @Value("${infinispan.cache.secondary.lock.timeout}")
    private int secondaryCacheLockTimeout;

    @Value("${infinispan.cache.secondary.lock.concurrency}")
    private int secondaryCacheLockConcurrency;

    @Value("${infinispan.cache.secondary.stateTransferChunkSize}")
    private int secondaryStateTransferChunkSize;

    // Read-Through cache configuration
    @Getter
    @Value("${infinispan.cache.readthrough.name}")
    private String readthroughCacheName;

    @Value("${infinispan.cache.readthrough.mode}")
    private CacheMode readthroughCacheMode;

    @Value("${infinispan.cache.readthrough.stateTransferChunkSize}")
    private int readthroughStateTransferChunkSize;

    @Value("${infinispan.cache.readthrough.lock.timeout}")
    private int readthroughCacheLockTimeout;

    @Value("${infinispan.cache.readthrough.lock.concurrency}")
    private int readthroughCacheLockConcurrency;

    @Getter
    @Value("${infinispan.cache.readthrough.enabled}")
    private boolean readthroughEnabled;

    @Value("${infinispan.cache.readthrough.preload}")
    private boolean readthroughPreload;

    @Value("${infinispan.cache.readthrough.preloadPageSize}")
    private int readthroughPreloadPageSize;

    @Value("${infinispan.cache.readthrough.entityClass}")
    private String readthroughEntityClass;

    @Value("${infinispan.cache.readthrough.persistenceUnitName}")
    private String readthroughPersistenceUnitName;

    // HotRod server configuration
    @Value("${infinispan.hotrod.topologyLockTimeout}")
    private long topologyLockTimeout;

    @Value("${infinispan.hotrod.topologyReplTimeout}")
    private long topologyReplTimeout;

    @Autowired
    private PropertyConfigurator propertyConfigurator;

    @PostConstruct
    public void init() {
        GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();
        gcb.transport().defaultTransport()
            .clusterName(clusterName)
            .globalJmxStatistics().enabled(globalStatisticsEnabled);

        gcb.shutdown().hookBehavior(ShutdownHookBehavior.REGISTER);

        if (!jgroupsConfig.equals("")) {
            gcb.transport().addProperty("configurationFile", jgroupsConfig);
        }

        configurePrimaryCache();
        configureSecondaryCache();

        try {
            configureReadthrough();
        } catch (ClassNotFoundException ex) {
            log.error("Read-Through Cache Configuration failed", ex);
        }

        configureHotRodServer();

        globalConfiguration = gcb.build();
    }

    private void configurePrimaryCache() {
        ConfigurationBuilder primaryCacheConfigurationBuilder = new ConfigurationBuilder();

        primaryCacheConfigurationBuilder
            .clustering().cacheMode(primaryCacheMode)
            .stateTransfer().chunkSize(primaryStateTransferChunkSize)
            .jmxStatistics().enable()
            .locking()
                .lockAcquisitionTimeout(primaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(primaryCacheLockConcurrency);

        if (primaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            primaryCacheConfigurationBuilder.clustering().hash().numOwners(primaryCacheNumOwners);
        }

        primaryCacheConfiguration = primaryCacheConfigurationBuilder.build();
    }

    private void configureSecondaryCache() {
        ConfigurationBuilder secondaryCacheConfigurationBuilder = new ConfigurationBuilder();
        secondaryCacheConfigurationBuilder
            .clustering().cacheMode(secondaryCacheMode)
            .jmxStatistics().enable()
            .locking()
                .lockAcquisitionTimeout(secondaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(secondaryCacheLockConcurrency);

        if (secondaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            secondaryCacheConfigurationBuilder.clustering().hash().numOwners(secondaryCacheNumOwners);
        }

        secondaryCacheConfiguration = secondaryCacheConfigurationBuilder.build();
    }

    private void configureReadthrough() throws ClassNotFoundException {
        if (!readthroughEnabled) {
            return;
        }

        ConfigurationBuilder readthroughCacheConfigurationBuilder = new ConfigurationBuilder();
        readthroughCacheConfigurationBuilder
            .clustering().cacheMode(readthroughCacheMode)
            .locking()
                .lockAcquisitionTimeout(readthroughCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(readthroughCacheLockConcurrency)
            .jmxStatistics().enable()
            .persistence()
                .passivation(false)
                .addStore(JpaStoreConfigurationBuilder.class)
                    .shared(true)
                    .preload(false)
                    .batchSize(5000)
                    .persistenceUnitName(readthroughPersistenceUnitName)
                    .storeMetadata(false)
                    .entityClass(Class.forName(readthroughEntityClass))
                    .ignoreModifications(true);

        readthroughCacheConfiguration = readthroughCacheConfigurationBuilder.build();
    }

    private void configureHotRodServer() {
        HotRodServerConfigurationBuilder builder = new HotRodServerConfigurationBuilder();
        builder.defaultCacheName(primaryCacheName)
            .authentication().disable()
            .topologyLockTimeout(topologyLockTimeout)
            .topologyReplTimeout(topologyReplTimeout);

        if (!networkAddress.equals("")) {
            builder.host(networkAddress);
            log.info("Hot Rod Network Address: {}", networkAddress);
        }

        hotRodServerConfiguration = builder.build();
    }

}
