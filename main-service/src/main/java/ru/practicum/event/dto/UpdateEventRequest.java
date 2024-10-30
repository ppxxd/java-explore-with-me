package ru.practicum.event.dto;

import ru.practicum.event.model.Event;
import ru.practicum.location.model.Location;

import java.time.LocalDateTime;

public interface UpdateEventRequest {
    String getAnnotation();

    Long getCategory();

    String getDescription();

    LocalDateTime getEventDate();

    Location getLocation();

    Boolean getPaid();

    Integer getParticipantLimit();

    Boolean getRequestModeration();

    Event.StateAction getStateAction();

    String getTitle();
}