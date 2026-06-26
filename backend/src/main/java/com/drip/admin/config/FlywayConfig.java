package com.drip.admin.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {
    @Bean
    public ApplicationRunner flywayMigrationRunner(DataSource dataSource) {
        return args -> Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .encoding("UTF-8")
            .baselineOnMigrate(true)
            .load()
            .migrate();
    }
}
