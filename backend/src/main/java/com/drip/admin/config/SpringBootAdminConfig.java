package com.drip.admin.config;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAdminServer
@Profile({"dev", "monitor"})
public class SpringBootAdminConfig {
}
