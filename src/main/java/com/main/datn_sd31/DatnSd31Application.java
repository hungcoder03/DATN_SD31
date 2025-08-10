package com.main.datn_sd31;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatnSd31Application {

    public static void main(String[] args) {
        SpringApplication.run(DatnSd31Application.class, args);
    }

}
