package org.example.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public final class ApplicationProperties {

    private static final String RESOURCE_NAME = "application.properties";

    private final Properties file;

    private ApplicationProperties(Properties file) {
        this.file = file;
    }

    public static ApplicationProperties load() {
        Properties loaded = new Properties();
        ClassLoader cl = ApplicationProperties.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream(RESOURCE_NAME)) {
            if (is == null) {
                throw new IllegalStateException("Classpath resource not found: " + RESOURCE_NAME);
            }
            loaded.load(is);
        } catch (IOException e) {
            log.error("Failed to read {}", RESOURCE_NAME, e);
            throw new IllegalStateException("Failed to read " + RESOURCE_NAME, e);
        }
        return new ApplicationProperties(loaded);
    }

    private String effective(String key) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isEmpty()) {
            return fromSystem;
        }
        String fromFile = file.getProperty(key);
        if (fromFile == null) {
            throw new IllegalStateException("Missing property: " + key + " (set in " + RESOURCE_NAME + " or -D" + key + ")");
        }
        return fromFile;
    }

    public String apiBase() {
        return effective("api.base");
    }

    public String dbUrl() {
        return effective("db.url");
    }

    public String dbUsername() {
        return effective("db.username");
    }

    public String dbPassword() {
        return effective("db.password");
    }

    public int dbPoolSize() {
        return Integer.parseInt(effective("db.pool.size"));
    }
}
