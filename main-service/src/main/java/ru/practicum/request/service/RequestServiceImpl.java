package ru.practicum.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.event.model.Event.State.PUBLISHED;
import static ru.practicum.request.model.Request.RequestStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;
    private final EventRepository eventRepository;

    @Override
    public List<RequestDto> getRequestByUserId(Long userId) {
        User user = userService.checkUserExistsById(userId);
        return requestRepository.findAllByRequester(user).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestDto createNewRequest(Long userId, Long eventId) {
        User user = userService.checkUserExistsById(userId);
        Event event = eventService.checkEventExistsById(eventId);

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.isRequestModeration() ? PENDING : CONFIRMED)
                .build();

        Optional<Request> req = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (req.isPresent()) {
            throw new ConflictException("Repeated request!");
        }
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Event owner can't participate in event");
        }
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("You can participate only in published events");
        }
        if (event.getParticipantsLimit() > 0) {
            if (event.getConfirmedRequests() >= event.getParticipantsLimit()) {
                throw new ConflictException("Participation limit is reached");
            }
        } else {
            request.setStatus(Request.RequestStatus.CONFIRMED);
        }

        if (request.getStatus() == CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public RequestDto updateRequestById(Long userId, Long requestId) {
        User user = userService.checkUserExistsById(userId);
        Request request = checkRequestExistsById(requestId);

        if (!request.getRequester().equals(user)) {
            throw new ConflictException("You can cancel only your request.");
        }

        request.setStatus(Request.RequestStatus.CANCELED);
        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsByUser(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {

        User user = userService.checkUserExistsById(userId);
        Event event = eventService.checkEventExistsById(eventId);

        if (!Objects.equals(event.getInitiator().getId(), user.getId())) {
            log.info(String.format("Throwing error: accept owner: %d, current user: %d",
                    event.getInitiator().getId(), user.getId()));
            throw new ValidationException("Only initiator can accept the request");
        }
        if (event.getParticipantsLimit() <= event.getConfirmedRequests() && event.getParticipantsLimit() != 0) {
            throw new ConflictException("Participation limit is reached");
        }

        List<Request> requestList = requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        if (requestList.size() != updateRequest.getRequestIds().size()) {
            throw new ConflictException("Events not found");
        }

        switch (updateRequest.getStatus()) {
            case CONFIRMED:
                return updateConfirmedStatus(requestList, event);
            case REJECTED:
                return updateRejectedStatus(requestList);
            default:
                throw new ValidationException("Incorrect status");
        }
    }

    @Override
    public List<RequestDto> getRequestsEventByUser(Long userId, Long eventId) {
        User user = userService.checkUserExistsById(userId);
        Event event = eventService.checkEventExistsById(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Only initiator can accept the request");
        }
        return requestRepository.findAllByEvent(event)
                .stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Request checkRequestExistsById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Request %d not found", requestId))
        );
    }

    private EventRequestStatusUpdateResult updateRejectedStatus(List<Request> requestList) {
        List<RequestDto> rejected = new ArrayList<>();

        for (Request request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Event status must be PENDING");
            }
            request.setStatus(REJECTED);
            requestRepository.save(request);
            rejected.add(RequestMapper.toDto(request));
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(rejected)
                .build();
    }

    private EventRequestStatusUpdateResult updateConfirmedStatus(List<Request> requestList, Event event) {
        List<RequestDto> confirmed = new ArrayList<>();
        List<RequestDto> rejected = new ArrayList<>();

        for (Request request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Event status must be PENDING");
            }
            if (event.getParticipantsLimit() <= event.getConfirmedRequests()) {
                request.setStatus(REJECTED);
                rejected.add(RequestMapper.toDto(request));
            } else {
                request.setStatus(CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmed.add(RequestMapper.toDto(request));
            }
            requestRepository.save(request);
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }
}
