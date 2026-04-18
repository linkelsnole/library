package org.example.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new JpaModule());
    }

    @Provides
    @Singleton
    ApplicationProperties applicationProperties() {
        return ApplicationProperties.load();
    }
}
