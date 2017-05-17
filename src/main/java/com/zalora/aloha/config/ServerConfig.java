package com.zalora.aloha.config;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.configuration.cache.*;
import org.infinispan.configuration.global.*;
import org.infinispan.persistence.jpa.configuration.JpaStoreConfigurationBuilder;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class ServerConfig {

    private static final String CACHE_MODE_DISTRIBUTED = "DISTRIBUTED";

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

    // Primary cache read-through configuration
    @Getter
    @Value("${infinispan.cache.primary.readthrough.enabled}")
    private boolean readthroughEnabled;

    @Value("${infinispan.cache.primary.readthrough.preload}")
    private boolean readthroughPreload;

    @Value("${infinispan.cache.primary.readthrough.preloadPageSize}")
    private int readthroughPreloadPageSize;

    @Value("${infinispan.cache.primary.readthrough.entityClass}")
    private String readthroughEntityClass;

    @Value("${infinispan.cache.primary.readthrough.persistenceUnitName}")
    private String readthroughPersistenceUnitName;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

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

    // HotRod server configuration
    @Value("${infinispan.hotrod.topologyLockTimeout}")
    private long topologyLockTimeout;

    @Value("${infinispan.hotrod.topologyReplTimeout}")
    private long topologyReplTimeout;

    @PostConstruct
    public void init() {
        System.setProperty("readthrough.entityClass", readthroughEntityClass);
        System.setProperty("spring.datasource.url", dbUrl);
        System.setProperty("spring.datasource.username", dbUsername);
        System.setProperty("spring.datasource.password", dbPassword);
    }

    @Bean
    public GlobalConfiguration globalConfig() {
        GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();
        gcb.transport().defaultTransport()
            .clusterName(clusterName)
            .globalJmxStatistics().enabled(globalStatisticsEnabled);

        gcb.shutdown().hookBehavior(ShutdownHookBehavior.REGISTER);

        if (jgroupsConfig.equals("")) {
            log.info("Using default jgroups discovery");
        } else {
            gcb.transport().addProperty("configurationFile", jgroupsConfig);
            log.info("Using {} for discovery", jgroupsConfig);
        }

        return gcb.build();
    }

    @Bean
    public Configuration mainConfig() {
        ConfigurationBuilder mainConfigBuilder = new ConfigurationBuilder();
        mainConfigBuilder
            .clustering().cacheMode(primaryCacheMode)
            .stateTransfer().chunkSize(primaryStateTransferChunkSize)
            .compatibility().enable()
            .jmxStatistics().enable()
            .locking()
                .lockAcquisitionTimeout(primaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(primaryCacheLockConcurrency);

        if (primaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            mainConfigBuilder.clustering().hash().numOwners(primaryCacheNumOwners);
        }

        if (readthroughEnabled) {
            Class<?> entityClass;
            try {
                    entityClass = Class.forName(readthroughEntityClass);
                } catch (ClassNotFoundException ex) {
                    log.error("Entity Class not found, continuing without read-through", ex);
                    return mainConfigBuilder.build();
                }

            mainConfigBuilder.persistence()
                .passivation(false)
                .addStore(JpaStoreConfigurationBuilder.class)
                    .shared(true)
                    .preload(readthroughPreload)
                    .persistenceUnitName(readthroughPersistenceUnitName)
                    .storeMetadata(false)
                    .entityClass(entityClass)
                    .ignoreModifications(true);

            log.info("Enabled read through for {}", readthroughEntityClass);
        }

        return mainConfigBuilder.build();
    }

    @Bean
    public Configuration sessionConfig() {
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

        return secondaryCacheConfigurationBuilder.build();
    }

    @Bean
    private HotRodServerConfiguration hotRodServerConfiguration() {
        HotRodServerConfigurationBuilder builder = new HotRodServerConfigurationBuilder();
        builder.defaultCacheName(primaryCacheName)
            .authentication().disable()
            .topologyLockTimeout(topologyLockTimeout)
            .topologyReplTimeout(topologyReplTimeout);

        if (!networkAddress.equals("")) {
            builder.host(networkAddress);
            log.info("Hot Rod Network Address: {}", networkAddress);
        }

        return builder.build();
    }

}
