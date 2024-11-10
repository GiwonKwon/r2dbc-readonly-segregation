package com.example.accessingdatar2dbc.config;

import com.example.accessingdatar2dbc.Customer;
import com.example.accessingdatar2dbc.CustomerRepository;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@DataR2dbcTest
@Import({
        WrongRoutingWithProxyConfiguration.class,
        ReadWriteConnectionFactoryConfiguration.class,
        ReadOnlyConnectionFactoryConfiguration.class,
        WrongRoutingWithProxyTest.TransactionalService.class,
})
public class WrongRoutingWithProxyTest {
    @Autowired private TransactionalService transactionalService;

    @SpyBean(name = ReadWriteConnectionFactoryConfiguration.READ_WRITE_CONNECTION_FACTORY)
    private ConnectionFactory readWriteConnectionFactory;

    @SpyBean(name = ReadOnlyConnectionFactoryConfiguration.READ_ONLY_CONNECTION_FACTORY)
    private ConnectionFactory readOnlyConnectionFactory;

    @TestComponent
    static class TransactionalService {
        @Autowired
        private CustomerRepository customerRepository;

        @Transactional()
        Flux<Customer> doWithReadWriteTransaction() {
            return customerRepository.findAll();
        }

        @Transactional(readOnly = true)
        Flux<Customer> doWithReadOnlyTransaction() {
            return customerRepository.findAll();
        }
    }

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