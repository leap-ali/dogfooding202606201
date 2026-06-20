package com.ddz.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SocketIOConfig {

    @Value("${socketio.port}")
    private int port;

    @Value("${socketio.upgrade-timeout}")
    private int upgradeTimeout;

    @Value("${socketio.ping-interval}")
    private int pingInterval;

    @Value("${socketio.ping-timeout}")
    private int pingTimeout;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setPort(port);
        config.setUpgradeTimeout(upgradeTimeout);
        config.setPingInterval(pingInterval);
        config.setPingTimeout(pingTimeout);
        return new SocketIOServer(config);
    }
}
