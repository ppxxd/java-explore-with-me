package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestService {

    List<RequestDto> getRequestByUserId(Long userId);

    RequestDto createNewRequest(Long userId, Long eventId);

    RequestDto updateRequestById(Long userId, Long requestId);

    Request checkRequestExistsById(Long requestId);

    List<RequestDto> getRequestsEventByUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestsByUser(Long userId,
                                                        Long eventId,
                                                        EventRequestStatusUpdateRequest updateRequest);
}
