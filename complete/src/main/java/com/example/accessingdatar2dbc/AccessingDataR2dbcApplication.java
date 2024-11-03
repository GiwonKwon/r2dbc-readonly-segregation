package com.example.accessingdatar2dbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccessingDataR2dbcApplication {

    private static final Logger log = LoggerFactory.getLogger(AccessingDataR2dbcApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AccessingDataR2dbcApplication.class, args);
    }
}
