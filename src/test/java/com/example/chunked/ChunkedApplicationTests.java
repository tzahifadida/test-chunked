package com.example.chunked;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
class ChunkedApplicationTests {
    @BeforeAll
    static void startApplication() {
        Thread appThread = new Thread(new ChunkedApplication());
        appThread.setDaemon(true); // Optional: makes the thread a daemon thread
        appThread.start();

        try {
            Thread.sleep(5000); // Wait 5 seconds for the app to start
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testChunked() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);

        schedule(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                    pos.write(("PostingData " + i + "\n").getBytes());
                    pos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                pos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        RequestCallback requestCallback = req -> {
            req.getHeaders().set("Content-Type", "application/octet-stream");
            if (pis != null) {
                try {
                    smartStreamCopy(pis, req.getBody());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        ResponseExtractor<Void> responseExtractor = res -> {
            String responseBody;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.getBody(), StandardCharsets.UTF_8))) {
                responseBody = reader.lines().collect(Collectors.joining("\n"));
            }
            log.info("Response: {}", responseBody);
            assert Integer.valueOf(responseBody) >= 4;
            return null;
        };

        restTemplate().execute("http://localhost:8080/post", HttpMethod.POST, requestCallback, responseExtractor);
    }

    public static String smartStreamCopy(InputStream is, OutputStream os) throws Exception {
        String quoteResultForLogging = "";
        int count = 0;

        while (true) {
            byte[] b = new byte[4096];
            int totalBytesRead = is.read(b);
            if (totalBytesRead == -1) {
                break;
            }

            count += totalBytesRead;
            if (count < 1000) {
                quoteResultForLogging += new String(b, 0, totalBytesRead);
            }
            os.write(b, 0, totalBytesRead);
            os.flush();
        }

        return quoteResultForLogging;
    }

    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;
    }

    public static Thread schedule(final Runnable r) {
        Thread t = new Thread(r);
        t.start();
        return t;
    }
}