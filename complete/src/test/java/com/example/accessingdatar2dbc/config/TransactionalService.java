package com.example.accessingdatar2dbc.config;

import com.example.accessingdatar2dbc.Customer;
import com.example.accessingdatar2dbc.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@TestComponent
public class TransactionalService {
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional(transactionManager = ConnectionFactoryRoutingConfiguration.READ_WRITE_TRANSACTION_MANAGER)
    Flux<Customer> doWithReadWriteTransaction() {
        return customerRepository.findAll();
    }

    @Transactional(transactionManager = ConnectionFactoryRoutingConfiguration.READ_ONLY_TRANSACTION_MANAGER, readOnly = true)
    Flux<Customer> doWithReadOnlyTransaction() {
        return customerRepository.findAll();
    }
}
