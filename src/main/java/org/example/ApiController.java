package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides a small root response so a browser visit confirms that the service is running.
 */
@RestController
public class ApiController {
    @GetMapping("/")
    public ApiInfo home() {
        return new ApiInfo("EventShuffle API", "/api/v1/event/list");
    }

    public record ApiInfo(String name, String eventsEndpoint) {
    }
}
