package ru.practicum.event.mapper;


import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.category.mapper.CategoryMapper;

public class EventMapper {
    public static Event fromNewDto(NewEventDto dto, CategoryService categoryService) {
        if (dto == null) {
            return null;
        }


        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(categoryService.checkCategoryExistsById(dto.getCategory()))
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(dto.getLocation())
                .paid(dto.isPaid())
                .participantsLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .build();
    }

    public static Event fromFullDto(EventFullDto dto) {
        if (dto == null) {
            return null;
        }

        return Event.builder()
                .id(dto.getId())
                .annotation(dto.getAnnotation())
                .category(CategoryMapper.fromFullDto(dto.getCategory()))
                .confirmedRequests(dto.getConfirmedRequests())
                .createdOn(dto.getCreatedOn())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(UserMapper.fromFullDto(dto.getInitiator()))
                .location(dto.getLocation())
                .paid(dto.isPaid())
                .participantsLimit(dto.getParticipantLimit())
                .publishedOn(dto.getPublishedOn())
                .requestModeration(dto.isRequestModeration())
                .state(dto.getState())
                .title(dto.getTitle())
                .views(dto.getViews())
                .comments(dto.getComments())
                .build();
    }

    public static EventFullDto toDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantsLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .comments(event.getComments())
                .build();
    }

    public static EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .comments(event.getComments())
                .build();
    }
}
