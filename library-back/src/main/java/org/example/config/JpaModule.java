package org.example.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JpaModule extends AbstractModule {

    @Provides
    @Singleton
    EntityManagerFactory entityManagerFactory(ApplicationProperties properties) {
        var hk = new HikariConfig();
        hk.setJdbcUrl(properties.dbUrl());
        hk.setUsername(properties.dbUsername());
        hk.setPassword(properties.dbPassword());
        hk.setMaximumPoolSize(properties.dbPoolSize());

        var ds = new HikariDataSource(hk);

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("jakarta.persistence.nonJtaDataSource", ds);

        return Persistence.createEntityManagerFactory("library", jpaProps);
    }
}
