package com.zalora.aloha.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class PropertyConfigurator {

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
        System.setProperty("jgroups.aws_ping.tags", jgroupsTags);
        System.setProperty("jgroups.aws_ping.filters", jgroupsFilters);
        System.setProperty("jgroups.aws_ping.access_key", jgroupsAccessKey);
        System.setProperty("jgroups.aws_ping.secret_key", jgroupsSecretKey);
    }

}
