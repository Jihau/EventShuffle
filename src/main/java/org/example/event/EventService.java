package org.example.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.example.event.EventDtos.EventResponse;
import org.example.event.EventDtos.EventSummary;
import org.example.event.EventDtos.ResultsResponse;
import org.example.event.EventDtos.VoteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Application service containing event scheduling rules.
 *
 * <p>An event is the aggregate being persisted. The service normalizes dates,
 * validates votes against the event's candidate dates, replaces repeated
 * participant submissions, and calculates dates accepted by everyone.</p>
 */
@Service
public class EventService {
    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    /**
     * Lists event summaries without exposing vote details.
     *
     * @return persisted event summaries
     */
    public List<EventSummary> list() {
        return repository.findAll().stream()
                .map(event -> new EventSummary(event.getId(), event.getName()))
                .toList();
    }

    @Transactional
    /**
     * Creates an event after removing duplicate dates while preserving order.
     *
     * @param name event name
     * @param dates candidate dates
     * @return generated event identifier
     */
    public Long create(String name, List<LocalDate> dates) {
        List<LocalDate> normalizedDates = unique(dates);
        if (normalizedDates.isEmpty()) {
            throw badRequest("dates must contain at least one date");
        }
        return repository.save(new Event(name.trim(), normalizedDates)).getId();
    }

    @Transactional(readOnly = true)
    /**
     * Loads an event or returns a 404 error when its identifier is unknown.
     *
     * @param id event identifier
     * @return event details
     */
    public EventResponse get(long id) {
        return toResponse(find(id));
    }

    @Transactional
    /**
     * Replaces a participant's previous submission with their latest vote.
     *
     * @param id event identifier
     * @param participantName participant name
     * @param selectedDates dates selected by the participant
     * @return updated event details
     */
    public EventResponse vote(long id, String participantName, List<LocalDate> selectedDates) {
        Event event = find(id);
        List<LocalDate> votes = unique(selectedDates);
        if (!new HashSet<>(event.getDates()).containsAll(votes)) {
            throw badRequest("votes must only contain dates offered by the event");
        }

        event.getVotes().removeIf(vote -> vote.getParticipantName().equals(participantName.trim()));
        event.getVotes().add(new EventVote(participantName.trim(), votes));
        return toResponse(repository.save(event));
    }

    @Transactional(readOnly = true)
    /**
     * Finds dates present in every participant's submission.
     *
     * @param id event identifier
     * @return dates suitable for all participants
     */
    public ResultsResponse results(long id) {
        Event event = find(id);
        if (event.getVotes().isEmpty()) {
            return new ResultsResponse(event.getId(), event.getName(), List.of());
        }
        List<VoteResponse> suitable = event.getDates().stream()
                .filter(date -> event.getVotes().stream().allMatch(vote -> vote.getDates().contains(date)))
                .map(date -> new VoteResponse(date, peopleFor(event, date)))
                .toList();
        return new ResultsResponse(event.getId(), event.getName(), suitable);
    }

    private Event find(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "event not found"));
    }

    private EventResponse toResponse(Event event) {
        List<VoteResponse> votes = event.getDates().stream()
                .map(date -> new VoteResponse(date, peopleFor(event, date)))
                .toList();
        return new EventResponse(event.getId(), event.getName(), List.copyOf(event.getDates()), votes);
    }

    private List<String> peopleFor(Event event, LocalDate date) {
        return event.getVotes().stream()
                .filter(vote -> vote.getDates().contains(date))
                .map(EventVote::getParticipantName)
                .toList();
    }

    private static List<LocalDate> unique(List<LocalDate> dates) {
        if (dates.stream().anyMatch(date -> date == null)) {
            throw badRequest("dates must not contain null values");
        }
        return new ArrayList<>(new LinkedHashSet<>(dates));
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Replaces the editable event fields and keeps the existing votes.
     *
     * @param id event identifier
     * @param name replacement event name
     * @param dates replacement candidate dates
     * @return updated event details
     */
    public EventResponse update(long id, @NotBlank(message = "name is required") String name, @NotEmpty(message = "dates must contain at least one date") List<LocalDate> dates) {
        Event event = find(id);
        event.setName(name);
        event.setDates(dates);
        return toResponse(repository.save(event));
    }

    /**
     * Removes an event by identifier; JPA cascades removal to its children.
     *
     * @param id event identifier
     */
    public void delete(long id) {
        repository.deleteById(id);
    }
}
