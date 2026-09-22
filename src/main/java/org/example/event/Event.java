package org.example.event;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistent event aggregate containing candidate dates and participant votes.
 */
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "event_dates", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "date_order")
    private List<LocalDate> dates = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "event_id", nullable = false)
    private List<EventVote> votes = new ArrayList<>();

    protected Event() {
    }

    public Event(String name, List<LocalDate> dates) {
        this.name = name;
        this.dates = new ArrayList<>(dates);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<LocalDate> getDates() { return dates; }
    public List<EventVote> getVotes() { return votes; }

    public void setName(@NotBlank(message = "name is required") String name) {
        this.name = name;
    }

    public void setDates(List<LocalDate> dates) {
        this.dates = new ArrayList<>(dates);
    }
}
