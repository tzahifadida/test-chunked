package com.example.chunked;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChunkedApplication implements Runnable {

    public static void main(String[] args) {
        SpringApplication.run(ChunkedApplication.class, args);
    }

    @Override
    public void run() {
        SpringApplication.run(ChunkedApplication.class);
    }
}