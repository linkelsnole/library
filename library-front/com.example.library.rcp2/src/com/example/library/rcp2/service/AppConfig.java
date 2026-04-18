package com.example.library.rcp2.service;

public final class AppConfig {

    private static final String DEFAULT_API_BASE = "http://localhost:8080";

    private AppConfig() {}

    public static String apiBase() {
        String fromSystem = System.getProperty("api.base");
        if (fromSystem != null && !fromSystem.isEmpty()) {
            return fromSystem;
        }
        String fromEnv = System.getenv("API_BASE");
        if (fromEnv != null && !fromEnv.isEmpty()) {
            return fromEnv;
        }
        return DEFAULT_API_BASE;
    }
}
