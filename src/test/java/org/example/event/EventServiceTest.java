package org.example.event;

import org.example.event.EventDtos.ResultsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceTest {
    private final EventRepository repository = mock(EventRepository.class);
    private final EventService service = new EventService(repository);

    @Test
    void findsDatesSelectedByEveryParticipant() {
        Event event = new Event("Dinner", List.of(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2)));
        event.getVotes().add(new EventVote("John", List.of(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2))));
        event.getVotes().add(new EventVote("Julia", List.of(LocalDate.of(2026, 10, 1))));
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(event));

        ResultsResponse results = service.results(7L);

        assertEquals(List.of(LocalDate.of(2026, 10, 1)), 
                results.suitableDates().stream().map(vote -> vote.date()).toList());
        assertEquals(List.of("John", "Julia"), results.suitableDates().getFirst().people());
    }

    @Test
    void rejectsVotesForDatesNotOfferedByTheEvent() {
        Event event = new Event("Dinner", List.of(LocalDate.of(2026, 10, 1)));
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(event));

        assertThrows(ResponseStatusException.class, () ->
                service.vote(7L, "John", List.of(LocalDate.of(2026, 10, 2))));
    }
}
