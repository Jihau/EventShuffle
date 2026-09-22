package org.example.event;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A participant's availability submission for one event.
 */
@Entity
@Table(name = "event_votes")
public class EventVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String participantName;

    @ElementCollection
    @CollectionTable(name = "vote_dates", joinColumns = @JoinColumn(name = "vote_id"))
    private List<LocalDate> dates = new ArrayList<>();

    protected EventVote() {
    }

    public EventVote(String participantName, List<LocalDate> dates) {
        this.participantName = participantName;
        this.dates = new ArrayList<>(dates);
    }

    public String getParticipantName() { return participantName; }
    public List<LocalDate> getDates() { return dates; }
}
