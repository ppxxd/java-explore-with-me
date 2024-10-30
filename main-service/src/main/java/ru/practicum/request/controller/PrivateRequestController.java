package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping
    public List<RequestDto> getRequestByUserId(@PathVariable Long userId) {
        log.info("Call getRequestByUserId endpoint.");
        return requestService.getRequestByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createNewRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Call createNewRequest endpoint.");
        return requestService.createNewRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public RequestDto updateRequestById(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Call updateRequestById endpoint.");
        return requestService.updateRequestById(userId, requestId);
    }
}
