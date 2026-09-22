package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the EventShuffle HTTP service.
 */
@SpringBootApplication
public class EventShuffleApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventShuffleApplication.class, args);
    }
}
