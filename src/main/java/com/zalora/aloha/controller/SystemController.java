package com.zalora.aloha.controller;

import com.zalora.aloha.config.ServerConfig;
import com.zalora.aloha.manager.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@RestController
public class SystemController {

    @Autowired
    private ServerManager serverManager;

    @Autowired
    private ServerConfig serverConfig;

    @RequestMapping("/exit")
    public void gracefulShutdown() {
        serverManager.getPrimaryCache().stop();
        serverManager.getSecondaryCache().stop();

        serverManager.getHotRodServer().stop();
        serverManager.getEmbeddedCacheManager().stop();
    }

}
