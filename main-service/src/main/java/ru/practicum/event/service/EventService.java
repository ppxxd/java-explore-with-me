package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getAllEventsByUser(Long userId, int from, int size);

    EventFullDto createNewEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByUser(Long userId, Long eventId);

    Event checkEventExistsById(Long catId);

    EventFullDto updateEventByUser(Long userId,
                                   Long eventId,
                                   UpdateUserEventRequest userRequest);

    List<EventFullDto> findAllEventsByAdmin(List<Long> userIds,
                                            List<Event.State> states,
                                            List<Long> categoryIds,
                                            LocalDateTime start,
                                            LocalDateTime end,
                                            Pageable pageable);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    EventFullDto findEventById(Long id, HttpServletRequest request);

    List<EventShortDto> findAllEventsByPublic(String text,
                                              List<Long> categories,
                                              Boolean paid,
                                              LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd,
                                              Boolean onlyAvailable,
                                              Sort sort,
                                              Pageable pageable,
                                              HttpServletRequest request);

}
