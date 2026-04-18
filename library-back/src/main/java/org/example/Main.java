package org.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.example.config.AppModule;
import org.example.config.ApplicationProperties;
import org.example.config.ObjectMapperProvider;
import org.example.resource.BookResource;
import org.example.resource.BusinessExceptionMapper;
import org.example.resource.ExceptionMapper;
import org.example.resource.LoanResource;
import org.example.resource.NotFoundExceptionMapper;
import org.example.resource.ReaderResource;
import org.example.resource.ValidationExceptionMapper;
import org.example.service.BookService;
import org.example.service.LoanService;
import org.example.service.ReaderService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

@Slf4j
public final class Main {

    public static HttpServer startServer() {
        Injector injector = Guice.createInjector(new AppModule());
        ApplicationProperties config = injector.getInstance(ApplicationProperties.class);
        return startServer(URI.create(config.apiBase()), injector);
    }

    public static HttpServer startServer(URI baseUri) {
        Injector injector = Guice.createInjector(new AppModule());
        return startServer(baseUri, injector);
    }

    private static HttpServer startServer(URI baseUri, Injector injector) {
        ResourceConfig config = resourceConfig(injector);
        return GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
    }

    private static ResourceConfig resourceConfig(Injector injector) {
        ResourceConfig config = new ResourceConfig();
        config.register(BookResource.class);
        config.register(ReaderResource.class);
        config.register(LoanResource.class);
        config.register(NotFoundExceptionMapper.class);
        config.register(BusinessExceptionMapper.class);
        config.register(ValidationExceptionMapper.class);
        config.register(ExceptionMapper.class);
        config.register(ObjectMapperProvider.class);
        config.register(org.glassfish.jersey.jackson.JacksonFeature.class);

        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(injector.getInstance(BookService.class)).to(BookService.class);
                bind(injector.getInstance(ReaderService.class)).to(ReaderService.class);
                bind(injector.getInstance(LoanService.class)).to(LoanService.class);
            }
        });
        return config;
    }

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new AppModule());
        ApplicationProperties applicationProperties = injector.getInstance(ApplicationProperties.class);
        URI listenUri = URI.create(applicationProperties.apiBase());
        HttpServer server = startServer(listenUri, injector);
        log.info("Server started: {}", listenUri);
        log.info("Press Enter to stop...");

        System.in.read();
        server.shutdownNow();
    }
}
