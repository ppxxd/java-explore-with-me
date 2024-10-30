package ru.practicum.request.mapper;

import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

public class RequestMapper {

    public static RequestDto toDto(Request request) {
        if (request == null) {
            return null;
        }

        return RequestDto.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}
