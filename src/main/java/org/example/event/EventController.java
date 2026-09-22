package org.example.event;

import jakarta.validation.Valid;
import org.example.event.EventDtos.CreateEventRequest;
import org.example.event.EventDtos.EventIdResponse;
import org.example.event.EventDtos.EventResponse;
import org.example.event.EventDtos.EventSummary;
import org.example.event.EventDtos.ResultsResponse;
import org.example.event.EventDtos.VoteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP adapter for event scheduling operations.
 *
 * <p>This class should stay focused on HTTP concerns: route mapping, request
 * validation, and delegation to {@link EventService}. Business rules belong
 * in the service so they can be tested without starting the web server.</p>
 */
@RestController
@RequestMapping("/api/v1/event")
public class EventController {
    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping("/list")
    /**
     * Returns the lightweight summary of every persisted event.
     *
     * @return event identifiers and names
     */
    public EventDtos.EventListResponse list() {
        return new EventDtos.EventListResponse(service.list());
    }

    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    /**
     * Creates an event and returns the generated identifier.
     *
     * @param request event name and candidate dates
     * @return generated event identifier
     */
    public EventIdResponse create(@Valid @RequestBody CreateEventRequest request) {
        return new EventIdResponse(service.create(request.name(), request.dates()));
    }

    @PutMapping("/{id}")
    /**
     * Replaces the event name and candidate dates while preserving its identifier.
     *
     * @param id event identifier
     * @param request replacement event fields
     * @return updated event
     */
    public EventResponse update(@PathVariable("id") long id, @Valid @RequestBody CreateEventRequest request) {
        return service.update(id, request.name(), request.dates());
    }

    @DeleteMapping("/{id}")
    /**
     * Deletes an event and its associated dates and votes.
     *
     * @param id event identifier
     */
    public void delete(@PathVariable("id") long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    /**
     * Returns the event together with each candidate date and its voters.
     *
     * @param id event identifier
     * @return event details
     */
    public EventResponse get(@PathVariable("id") long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/vote")
    /**
     * Adds or replaces one participant's availability for an event.
     *
     * @param id event identifier
     * @param request participant name and selected dates
     * @return updated event details
     */
    public EventResponse vote(@PathVariable("id") long id, @Valid @RequestBody VoteRequest request) {
        return service.vote(id, request.name(), request.votes());
    }

    @GetMapping("/{id}/results")
    /**
     * Returns candidate dates selected by every participant.
     *
     * @param id event identifier
     * @return dates suitable for all participants
     */
    public ResultsResponse results(@PathVariable("id") long id) {
        return service.results(id);
    }

}
