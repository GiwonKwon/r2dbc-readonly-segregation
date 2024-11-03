package com.example.accessingdatar2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.ModifiedR2dbcTransactionManager;
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.accessingdatar2dbc.config.ReadOnlyConnectionFactoryConfiguration.READ_ONLY_CONNECTION_FACTORY;
import static com.example.accessingdatar2dbc.config.ReadWriteConnectionFactoryConfiguration.READ_WRITE_CONNECTION_FACTORY;

@Configuration
@EnableTransactionManagement
public class ConnectionFactoryRoutingConfiguration {
    private static class TxBasedRoutingConnectionFactory extends AbstractRoutingConnectionFactory {
        @Override
        protected Mono<Object> determineCurrentLookupKey() {
            return TransactionSynchronizationManager.forCurrentTransaction()
                    .map(tx -> tx.isCurrentTransactionReadOnly() ? READ_ONLY_CONNECTION_FACTORY : READ_WRITE_CONNECTION_FACTORY)
                    .onErrorReturn(READ_WRITE_CONNECTION_FACTORY)
                    .cast(Object.class);
        }
    }

    @Primary
    @Bean
    @DependsOn({READ_WRITE_CONNECTION_FACTORY, READ_ONLY_CONNECTION_FACTORY})
    public ConnectionFactory connectionFactory(
            @Qualifier(READ_WRITE_CONNECTION_FACTORY) ConnectionFactory readWriteConnectionFactory,
            @Qualifier(READ_ONLY_CONNECTION_FACTORY) ConnectionFactory readOnlyConnectionFactory
    ) {
        TxBasedRoutingConnectionFactory txBasedRoutingConnectionFactory = new TxBasedRoutingConnectionFactory();

        Map<Object, Object> factories =
                Map.of(
                        READ_WRITE_CONNECTION_FACTORY, readWriteConnectionFactory,
                        READ_ONLY_CONNECTION_FACTORY, readOnlyConnectionFactory
                );
        txBasedRoutingConnectionFactory.setTargetConnectionFactories(factories);
        txBasedRoutingConnectionFactory.setDefaultTargetConnectionFactory(readWriteConnectionFactory);

        return txBasedRoutingConnectionFactory;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.r2dbc.use-modified-transaction-manager", havingValue = "true")
    public ReactiveTransactionManager modifiedTransactionManager(ConnectionFactory connectionFactory) {
        return new ModifiedR2dbcTransactionManager(connectionFactory);
    }
}
