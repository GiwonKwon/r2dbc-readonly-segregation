package com.example.accessingdatar2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.accessingdatar2dbc.config.ReadOnlyConnectionFactoryConfiguration.READ_ONLY_CONNECTION_FACTORY;
import static com.example.accessingdatar2dbc.config.ReadWriteConnectionFactoryConfiguration.READ_WRITE_CONNECTION_FACTORY;

@Configuration
public class WrongRoutingWithProxyConfiguration {
    private static final String ROUTING_CONNECTION_FACTORY = "routingConnectionFactory";

    private static class TxBasedRoutingConnectionFactory extends AbstractRoutingConnectionFactory {
        @Override
        protected Mono<Object> determineCurrentLookupKey() {
            return TransactionSynchronizationManager.forCurrentTransaction()
                    .map(tx -> tx.isCurrentTransactionReadOnly() ? READ_ONLY_CONNECTION_FACTORY : READ_WRITE_CONNECTION_FACTORY)
                    .onErrorReturn(READ_WRITE_CONNECTION_FACTORY)
                    .cast(Object.class);
        }
    }

    @Bean(ROUTING_CONNECTION_FACTORY)
    @DependsOn({READ_WRITE_CONNECTION_FACTORY, READ_ONLY_CONNECTION_FACTORY})
    public ConnectionFactory routingConnectionFactory(
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

    @Primary
    @Bean
    public ConnectionFactory connectionFactory(
            @Qualifier(ROUTING_CONNECTION_FACTORY) ConnectionFactory routingConnectionFactory) {
        return new TransactionAwareConnectionFactoryProxy(routingConnectionFactory);
    }
}
