package org.example.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

/**
 * JSON request and response contracts for the versioned event API.
 *
 * <p>DTOs prevent persistence entities from becoming a public API contract.
 * Records keep these immutable transport objects concise.</p>
 */
public final class EventDtos {
    private EventDtos() {}

    public record CreateEventRequest(
            @NotBlank(message = "name is required") String name,
            @NotEmpty(message = "dates must contain at least one date") List<LocalDate> dates) {}

    public record VoteRequest(
            @NotBlank(message = "name is required") String name,
            @NotEmpty(message = "votes must contain at least one date") List<LocalDate> votes) {}

    public record EventIdResponse(Long id) {}
    public record EventListResponse(List<EventSummary> events) {}
    public record EventSummary(Long id, String name) {}
    public record VoteResponse(LocalDate date, List<String> people) {}
    public record EventResponse(Long id, String name, List<LocalDate> dates, List<VoteResponse> votes) {}
    public record ResultsResponse(Long id, String name, List<VoteResponse> suitableDates) {}
    public record ErrorResponse(String error) {}
}
