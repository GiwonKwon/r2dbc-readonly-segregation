package com.example.accessingdatar2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.autoconfigure.sql.init.SqlR2dbcScriptDatabaseInitializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
public class ReadWriteConnectionFactoryConfiguration {
    private static final String READ_WRITE_PROPERTIES = "readWriteProperties";
    public static final String READ_WRITE_CONNECTION_FACTORY = "readWriteConnectionFactory";

    @Configuration(READ_WRITE_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.r2dbc.read-write")
    public static class ReadWriteProperties extends R2dbcProperties {}

    @Bean(READ_WRITE_CONNECTION_FACTORY)
    public ConnectionFactory readWriteConnectionFactory(@Qualifier(READ_WRITE_PROPERTIES) ReadWriteProperties readWriteProperties) {
        ConnectionFactoryOptions.Builder options =
                ConnectionFactoryOptions.parse(readWriteProperties.getUrl())
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, readWriteProperties.getUsername())
                        .option(ConnectionFactoryOptions.PASSWORD, readWriteProperties.getPassword());

        return ConnectionFactoryBuilder.withOptions(options).build();
    }

    @Bean
    SqlR2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer(
            @Qualifier(READ_WRITE_PROPERTIES) ReadWriteProperties readWriteProperties,
            SqlInitializationProperties properties) {

        ConnectionFactoryOptions.Builder options =
                ConnectionFactoryOptions.parse(readWriteProperties.getUrl())
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, readWriteProperties.getUsername())
                        .option(ConnectionFactoryOptions.PASSWORD, readWriteProperties.getPassword());

        return new SqlR2dbcScriptDatabaseInitializer(ConnectionFactoryBuilder.withOptions(options).build(), properties);
    }
}
