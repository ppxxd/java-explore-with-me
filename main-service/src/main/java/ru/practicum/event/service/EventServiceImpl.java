package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.service.CategoryService;
import ru.practicum.client.StatsClient;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.event.enums.Sort.VIEWS;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final String APP_NAME = "ewm-service";
    private static final int DESCRIPTION_MAX = 7000;
    private static final int DESCRIPTION_MIN = 20;
    private static final int ANNOTATION_MAX = 2000;
    private static final int ANNOTATION_MIN = 20;
    private static final int TITLE_MAX = 120;
    private static final int TITLE_MIN = 3;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private static final LocalDateTime CURRENT_TIME = LocalDateTime.now();

    @Override
    public List<EventFullDto> getAllEventsByUser(Long userId, int from, int size) {
        userService.checkUserExistsById(userId);
        PageRequest pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createNewEvent(Long userId, NewEventDto newEventDto) {
        User user = userService.checkUserExistsById(userId);
        categoryService.checkCategoryExistsById(newEventDto.getCategory());

        if (newEventDto.getEventDate().isBefore(CURRENT_TIME.plusHours(2))) {
            throw new ValidationException("Datetime of the event must be in two hours from now");
        }

        Event event = EventMapper.fromNewDto(newEventDto, categoryService);
        event.setState(Event.State.PENDING);
        event.setCreatedOn(CURRENT_TIME);
        event.setConfirmedRequests(0L);
        event.setInitiator(user);
        return EventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = checkEventExistsById(eventId);
        User user = userService.checkUserExistsById(userId);

        checkEventOwner(event, user);

        return EventMapper.toDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId,
                                          Long eventId,
                                          UpdateUserEventRequest request) {

        Event event = EventMapper.fromFullDto(getEventByUser(userId, eventId));

        checkEventStatus(event, request, List.of(Event.State.PENDING, Event.State.CANCELED));

        checkDate(request);

        if (Event.StateAction.SEND_TO_REVIEW == request.getStateAction()) {
            event.setState(Event.State.PENDING);
        }
        if (Event.StateAction.CANCEL_REVIEW == request.getStateAction()) {
            event.setState(Event.State.CANCELED);
        }


        return EventMapper.toDto(eventRepository.save(updateEvent(event, request)));
    }

    @Override
    public List<EventFullDto> findAllEventsByAdmin(List<Long> users,
                                                   List<Event.State> states,
                                                   List<Long> categories,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd,
                                                   Pageable pageable) {
        if (states == null && rangeStart == null && rangeEnd == null) {
            return eventRepository.findAll(pageable)
                    .stream()
                    .map(EventMapper::toDto)
                    .collect(Collectors.toList());
        }
        if (states == null) {
            states = Stream.of(Event.State.values())
                    .collect(Collectors.toList());
        }

        rangeStart = rangeStart == null ? CURRENT_TIME.minusYears(5) : rangeStart;
        rangeEnd = rangeEnd == null ? CURRENT_TIME.plusYears(5) : rangeEnd;


        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Start can't be after the end");
        }

        List<Event> events = eventRepository.findByParams(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                pageable);

        return events.stream()
                .map(EventMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = checkEventExistsById(eventId);

        if (request.getEventDate() != null) {
            checkStartTime(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }

        log.info("REQUEST ACTION: {}", request.getStateAction());
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case REJECT_EVENT:
                    log.info("CASE REJECT");
                    checkEventStatus(event, request, List.of(Event.State.PENDING));
                    event.setState(Event.State.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    log.info("CASE PUBLISH");
                    checkEventStatus(event, request, List.of(Event.State.PENDING));
                    event.setState(Event.State.PUBLISHED);
                    event.setPublishedOn(CURRENT_TIME);
                    break;
            }
        }

        log.info("CALL TO UPDATE EVENT: {}", event);

        return EventMapper.toDto(eventRepository.save(updateEvent(event, request)));
    }

    @Override
    @Transactional
    public EventFullDto findEventById(Long eventId, HttpServletRequest request) {
        Event event = checkEventExistsById(eventId);

        if (event.getState() != Event.State.PUBLISHED) {
            throw new ObjectNotFoundException(String.format("Event %d is not published", event.getId()));
        }

        statsClient.postHit(APP_NAME, request.getRequestURI(), request.getRemoteAddr(), CURRENT_TIME);
        List<ViewStatsDto> viewStatsList = statsClient.getStats(LocalDateTime.now().minusYears(5),
                LocalDateTime.now().plusYears(5),
                List.of(eventId.toString()), true);
        long hits = viewStatsList
                .stream()
                .filter(s -> Objects.equals(s.getUri(), request.getRequestURI()))
                .count();

        event.setViews(hits + 1);
        event.setConfirmedRequests((long) requestRepository.findAllByEventIdInAndStatus(List.of(eventId),
                Request.RequestStatus.CONFIRMED).size());
        return EventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> findAllEventsByPublic(String text,
                                                     List<Long> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     Sort sort,
                                                     Pageable pageable,
                                                     HttpServletRequest request) {
        rangeStart = rangeStart == null ? CURRENT_TIME : rangeStart;
        rangeEnd = rangeEnd == null ? CURRENT_TIME.plusYears(15) : rangeEnd;
        text = text == null ? "" : text;

        checkDateTimeOfStartAndEnd(rangeStart, rangeEnd);

        List<Event> events = eventRepository.findByParamsOrderByDate(
                text.toLowerCase(),
                List.of(Event.State.PUBLISHED),
                categories,
                paid,
                rangeStart,
                rangeEnd,
                pageable);

        statsClient.postHit(APP_NAME, request.getRequestURI(), request.getRemoteAddr(), CURRENT_TIME);
        List<Event> eventList = setViewsAndConfirmedRequests(events);

        if (sort != null && sort.equals(VIEWS)) {
            eventList.sort((e1, e2) -> e2.getViews().compareTo(e1.getViews()));
        }

        if (onlyAvailable) {
            return eventList.stream()
                    .filter(event -> event.getParticipantsLimit() <= event.getConfirmedRequests())
                    .map(EventMapper::toShortDto)
                    .collect(Collectors.toList());
        } else {
            return eventList.stream()
                    .map(EventMapper::toShortDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Event checkEventExistsById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event %d not found", eventId))
        );
    }

    private void checkEventOwner(Event event, User user) {
        if (!event.getInitiator().equals(user)) {
            throw new ObjectNotFoundException(String.format("User %s not the owner of the event %d",
                    user.getName(), event.getId()));
        }
    }

    private void checkDate(UpdateEventRequest request) {
        if ((request != null && request.getEventDate() != null) &&
                !request.getEventDate().isAfter(CURRENT_TIME.plusHours(2))) {
            throw new ValidationException("Datetime of the event must be in two hours from now");
        }
    }

    private void checkEventStatus(Event event, UpdateEventRequest request, List<Event.State> allowedState) {
        boolean valid = false;
        for (Event.State state : allowedState) {
            if (event.getState() == state) {
                log.info("CHECKED EVENT STATUS TRUE;");
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new ConflictException(String.format("Event can be published only from %s status", allowedState) +
                    request.getStateAction());
        }
    }

    private Event updateEvent(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) {
            checkAnnotaion(request);
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            event.setCategory(categoryService.checkCategoryExistsById(request.getCategory()));
        }
        if (request.getDescription() != null) {
            checkDescription(request);
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantsLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            checkTitle(request);
            event.setTitle(request.getTitle());
        }
        return event;
    }

    private void checkDescription(UpdateEventRequest request) {
        if (request.getDescription().length() > DESCRIPTION_MAX || request.getDescription().length() < DESCRIPTION_MIN) {
            throw new ValidationException("Can't be shorter than " + DESCRIPTION_MIN + " and longer than " + DESCRIPTION_MAX);
        }
    }

    private void checkAnnotaion(UpdateEventRequest request) {
        if (request.getAnnotation().length() > ANNOTATION_MAX || request.getAnnotation().length() < ANNOTATION_MIN) {
            throw new ValidationException("Can't be shorter than " + ANNOTATION_MIN + " and longer than " + ANNOTATION_MAX);
        }
    }

    private void checkTitle(UpdateEventRequest request) {
        if (request.getTitle().length() < TITLE_MIN || request.getTitle().length() > TITLE_MAX) {
            throw new ValidationException("Can't be shorter than " + TITLE_MIN + " and longer than " + TITLE_MAX);
        }
    }

    private void checkStartTime(LocalDateTime time) {
        if (CURRENT_TIME.isAfter(time)) {
            throw new ValidationException("Start must be before the end");
        }
    }

    private List<Event> setViewsAndConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events
                .stream()
                .map(Event::getId)
                .toList();

        List<ViewStatsDto> viewStatsList = statsClient
                .getStats(LocalDateTime.now().minusYears(5),
                LocalDateTime.now().plusYears(5),
                eventIds.stream().map(String::valueOf).collect(Collectors.toList()), false);
        Map<Long, Long> views;
        if (viewStatsList != null && !viewStatsList.isEmpty()) {
            views = viewStatsList
                    .stream()
                    .collect(Collectors.toMap(this::getEventIdFromURI, ViewStatsDto::getHits));
        } else {
            views = Collections.emptyMap();
        }

        events.forEach(event ->
                event.setViews(views.getOrDefault(event.getId(), 0L)));

        events.forEach(event ->
                event.setConfirmedRequests((long) requestRepository.findAllByEventIdInAndStatus(new ArrayList<>(eventIds),
                        Request.RequestStatus.CONFIRMED).size()));

        return events
                .stream()
                .map(eventRepository::save)
                .collect(Collectors.toList());
    }

    private Long getEventIdFromURI(ViewStatsDto viewStats) {
        return Long.parseLong(viewStats.getUri().substring(viewStats.getUri().lastIndexOf("/") + 1));
    }

    public void checkDateTimeOfStartAndEnd(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Start can't be after the end");
        }
    }
}
