package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateUserEventRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public List<EventFullDto> getAllEvents(@PathVariable Long userId,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                           @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Call private getAllEvents endpoint.");
        return eventService.getAllEventsByUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createNewEvent(@PathVariable Long userId,
                                           @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Call private createNewEvent endpoint.");
        return eventService.createNewEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long userId,
                                           @PathVariable Long eventId) {
        log.info("Call private getEventById endpoint.");
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventById(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @Valid @RequestBody UpdateUserEventRequest userRequest) {
        log.info("Call private updateEventById endpoint.");
        return eventService.updateEventByUser(userId, eventId, userRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getRequestsEventByUser(@PathVariable Long userId,
                                                   @PathVariable Long eventId) {
        log.info("Call private getRequestsEventByUser endpoint.");
        return requestService.getRequestsEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Call private updateRequests endpoint.");
        return requestService.updateRequestsByUser(userId, eventId, updateRequest);
    }
}
