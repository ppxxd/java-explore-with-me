package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;
import ru.practicum.location.model.Location;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 2000, nullable = false)
    private String annotation;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "event_description", length = 7000, nullable = false)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "location_id")
    private Location location;

    private boolean paid;

    @Column(name = "participants_limit")
    private int participantsLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private boolean requestModeration;

    @Column(name = "event_state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(nullable = false)
    private String title;

    private Long views;

    public enum State {
        PENDING,
        PUBLISHED,
        CANCELED
    }

    public enum StateAction {
        PUBLISH_EVENT,
        REJECT_EVENT,
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }

}
