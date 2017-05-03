package com.zalora.aloha.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class PropertyConfigurator {

    // Spring Datasource & JPA properties
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.jpa.show-sql}")
    private String showSql;

    @Value("${spring.jpa.hbm2ddl-auto}")
    private String autoGenerate;

    @Value("${infinispan.cache.readthrough.entityClass}")
    private String entityClass;

    @Value("${spring.jpa.properties.hibernate.dialect}")
    private String hibernateDialect;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    // JGroups (JDBC discovery) configuration
    @Value("${jgroups.jdbc.connection_url}")
    private String jgroupsConnectionUrl;

    @Value("${jgroups.jdbc.connection_username}")
    private String jgroupsUsername;

    @Value("${jgroups.jdbc.connection_password}")
    private String jgroupsPassword;

    // JGroups (AWS API discovery) configuration
    @Value("${jgroups.aws_ping.tags}")
    private String jgroupsTags;

    @Value("${jgroups.aws_ping.filters}")
    private String jgroupsFilters;

    @Value("${jgroups.aws_ping.access_key}")
    private String jgroupsAccessKey;

    @Value("${jgroups.aws_ping.secret_key}")
    private String jgroupsSecretKey;

    @PostConstruct
    public void init() {
        System.setProperty("spring.datasource.url", dbUrl);
        System.setProperty("spring.datasource.username", dbUsername);
        System.setProperty("spring.datasource.password", dbPassword);
        System.setProperty("spring.datasource.driver-class-name", driverClassName);

        System.setProperty("spring.jpa.show-sql", showSql);
        System.setProperty("spring.jpa.hbm2ddl-auto", autoGenerate);
        System.setProperty("infinispan.cache.readthrough.entityClass", entityClass);
        System.setProperty("spring.jpa.properties.hibernate.dialect", hibernateDialect);

        System.setProperty("jgroups.jdbc.connection_url", jgroupsConnectionUrl);
        System.setProperty("jgroups.jdbc.connection_username", jgroupsUsername);
        System.setProperty("jgroups.jdbc.connection_password", jgroupsPassword);

        System.setProperty("jgroups.aws_ping.tags", jgroupsTags);
        System.setProperty("jgroups.aws_ping.filters", jgroupsFilters);
        System.setProperty("jgroups.aws_ping.access_key", jgroupsAccessKey);
        System.setProperty("jgroups.aws_ping.secret_key", jgroupsSecretKey);
    }

}
