package com.example.accessingdatar2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;

@DataR2dbcTest
@Import({
        ConnectionFactoryRoutingConfiguration.class,
        ReadWriteConnectionFactoryConfiguration.class,
        ReadOnlyConnectionFactoryConfiguration.class,
        TransactionalService.class,
})
public class RoutingTest {
    @Autowired private TransactionalService transactionalService;

    @SpyBean(name = ReadWriteConnectionFactoryConfiguration.READ_WRITE_CONNECTION_FACTORY)
    private ConnectionFactory readWriteConnectionFactory;

    @SpyBean(name = ReadOnlyConnectionFactoryConfiguration.READ_ONLY_CONNECTION_FACTORY)
    private ConnectionFactory readOnlyConnectionFactory;

    @Test
    void testReadWriteTransaction() {
        // when
        transactionalService.doWithReadWriteTransaction().subscribe();

        // then
        Mockito.verify(readWriteConnectionFactory, Mockito.times(1)).create();
        Mockito.verify(readOnlyConnectionFactory, Mockito.never()).create();
    }

    @Test
    void testReadOnlyTransaction() {
        // when
        transactionalService.doWithReadOnlyTransaction().subscribe();

        // then
        Mockito.verify(readOnlyConnectionFactory, Mockito.times(1)).create();
        Mockito.verify(readWriteConnectionFactory, Mockito.never()).create();
    }
}