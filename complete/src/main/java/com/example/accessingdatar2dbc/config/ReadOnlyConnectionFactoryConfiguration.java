package com.example.accessingdatar2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
public class ReadOnlyConnectionFactoryConfiguration {
    private static final String READ_ONLY_PROPERTIES = "readOnlyProperties";
    public static final String READ_ONLY_CONNECTION_FACTORY = "readOnlyConnectionFactory";

    @Configuration(READ_ONLY_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.r2dbc.read-only")
    public static class ReadOnlyProperties extends R2dbcProperties {}

    @Bean(READ_ONLY_CONNECTION_FACTORY)
    public ConnectionFactory readOnlyConnectionFactory(@Qualifier(READ_ONLY_PROPERTIES) ReadOnlyProperties readOnlyProperties) {
        ConnectionFactoryOptions.Builder options =
                ConnectionFactoryOptions.parse(readOnlyProperties.getUrl())
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, readOnlyProperties.getUsername())
                        .option(ConnectionFactoryOptions.PASSWORD, readOnlyProperties.getPassword());

        return ConnectionFactoryBuilder.withOptions(options).build();
    }
}
