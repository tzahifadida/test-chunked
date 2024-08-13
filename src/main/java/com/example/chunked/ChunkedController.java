package com.example.chunked;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@RestController
public class ChunkedController {
    @PostMapping("/post")
    public ResponseEntity<String> receiveChunkedData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("EndPoint Controller /post");
        int counter = 0;
        long last = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                long delta = System.currentTimeMillis() - last;
                if (delta >= 800) {
                    counter++;
                }
                log.info("Reading from POST stream: {} , delta: {}, counter: {}", line, delta, counter);  // prints each chunk of data as it comes
                last = System.currentTimeMillis();
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("" + counter);
    }
}
