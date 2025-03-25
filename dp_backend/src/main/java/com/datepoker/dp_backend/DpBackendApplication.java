package com.datepoker.dp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DpBackendApplication.class, args);
    }

}
