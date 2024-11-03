package com.example.accessingdatar2dbc.config;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.example.accessingdatar2dbc.config.ReadOnlyConnectionFactoryConfiguration.READ_ONLY_CONNECTION_FACTORY;
import static com.example.accessingdatar2dbc.config.ReadWriteConnectionFactoryConfiguration.READ_WRITE_CONNECTION_FACTORY;

@Configuration
public class ConnectionFactoryRoutingConfiguration {
    public static final String READ_ONLY_TRANSACTION_MANAGER = "readOnlyTransactionManager";
    public static final String READ_WRITE_TRANSACTION_MANAGER = "readWriteTransactionManager";

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
                        READ_WRITE_CONNECTION_FACTORY, new TxAwareConnectionFactory(readWriteConnectionFactory),
                        READ_ONLY_CONNECTION_FACTORY, new TxAwareConnectionFactory(readOnlyConnectionFactory)
                );
        txBasedRoutingConnectionFactory.setTargetConnectionFactories(factories);
        txBasedRoutingConnectionFactory.setDefaultTargetConnectionFactory(readWriteConnectionFactory);

        return txBasedRoutingConnectionFactory;
    }

    @Bean(READ_ONLY_TRANSACTION_MANAGER)
    public ReactiveTransactionManager readOnlyTransactionManager(
            @Qualifier(READ_ONLY_CONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean(READ_WRITE_TRANSACTION_MANAGER)
    public ReactiveTransactionManager readWriteTransactionManager(
            @Qualifier(READ_WRITE_CONNECTION_FACTORY) ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    private static class TxAwareConnectionFactory implements ConnectionFactory {
        private final ConnectionFactory connectionFactory;

        public TxAwareConnectionFactory(ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
        }

        @Override
        public Publisher<? extends Connection> create() {
            return ConnectionFactoryUtils.getConnection(connectionFactory);
        }

        @Override
        public ConnectionFactoryMetadata getMetadata() {
            return connectionFactory.getMetadata();
        }
    }
}
